package libs.libCore.modules;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.winium.DesktopOptions;
import org.openqa.selenium.winium.WiniumDriver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WiniumDriverFactory {

    private Context globalCtx;
    private Context scenarioCtx;
    private Storage Storage;
    private FileCore FileCore;
    private WinRSCore WinRSCore;
    private StepCore StepCore;
    private ExecutorCore ExecutorCore;

    public WiniumDriverFactory() {
        this.globalCtx = GlobalCtxSingleton.getInstance();
        this.scenarioCtx = globalCtx.get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.Storage = scenarioCtx.get("Storage", Storage.class);
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.WinRSCore = scenarioCtx.get("WinRSCore", WinRSCore.class);
        this.StepCore = scenarioCtx.get("StepCore", StepCore.class);
        this.ExecutorCore = scenarioCtx.get("ExecutorCore", ExecutorCore.class);
    }


    public WiniumDriver create(String node){
        String port = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.port");
        String path = Storage.get("Environment.Active.App.path");
        String args = Storage.get("Environment.Active.App.args");

        WiniumDriver driver;

        if ( node == null || node.equals("") || node.equalsIgnoreCase("localhost") ){
            driver = createOnLocal(port, path, args);
        } else {
            driver = createOnRemote(node, port, path, args);
        }

        return driver;
    }

    public void closeWiniumResources(){
        String rdpProcessId = scenarioCtx.get("WiniumRemoteRDPProcessId", String.class);
        if ( rdpProcessId != null ) {
            killRdpSession(rdpProcessId);
        }

        String node = scenarioCtx.get("WiniumRemoteNodeId", String.class);
        if ( node == null ){
            Log.debug("Killing Winium driver process on local host");
            killWiniumDesktopDriverProcessOnLocal();
        }

        if ( node != null && isDriverRunningOnRemote(node) ){
            Log.debug("Killing Winium driver process on remote host " + node);
            killWiniumDesktopDriverProccessOnRemote(node);
            deleteWiniumDesktopDriverScheduledTaskOnRemote(node);
        }
    }

    public void closeDriver(WiniumDriver driver){
        Log.debug("Try to close an application");
        try {
            driver.close();
            driver.quit();
            scenarioCtx.put("WiniumDesktopDriver", WiniumDriver.class, null);
        } catch (WebDriverException e) {
            Log.warn("Application is already closed");
            scenarioCtx.put("WiniumDesktopDriver", WiniumDriver.class, null);
        }
    }

    private void uploadDriverOnRemote(String node){
        Boolean isAvailable = WinRSCore.checkThatFileExists(node, WinRSCore.getUserDir(node) + "\\Winium.Desktop.Driver.exe");
        if ( isAvailable ) {
            Log.debug("WiniumDesktopDriver already available on remote node " + node);
            return;
        }

        Log.debug("Uploading file");
        String item = FileCore.getProjectPath() + File.separator + Storage.get("Environment.Active.WebDrivers.WiniumDesktop.path");
        WinRSCore.uploadFile(node, item);
    }

    private void downloadDriverOnRemote(String node){
        Log.debug("Downloading Winium.Desktop.Driver.zip file");
        String cmd = "Invoke-WebRequest -Uri \"https://github.com/2gis/Winium.Desktop/releases/download/v1.6.0/Winium.Desktop.Driver.zip\" -OutFile \"Winium.Desktop.Driver.zip\"";
        WinRSCore.executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 300);

        Log.debug("Extracting it");
        WinRSCore.executeSingleCommandOnRemote("unzip -e -o Winium.zip", node, 60);
    }

    private boolean isDriverRunningOnRemote(String node){
        String cmd = "Get-CimInstance Win32_Process | Where {$_.name -match '.*Winium.*'} | Select Caption, CommandLine, ProcessId | Format-list";
        String result = WinRSCore.executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);

        return result.contains("Winium.Desktop.Driver.exe");
    }

    private String openRdpToRemote(String node){
        return openRdpSession(node, true);
    }

    private void openFirewallOnRemote(String node){
       String port = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.port");
       Log.debug("Opening firewall port " + port + " for domain profile on node " + node);
       String cmd = "New-NetFirewallRule -DisplayName 'HTTP(S) Inbound' -Profile Domain -Direction Inbound" +
                    " -Action Allow -Protocol TCP -LocalPort '" + port + "';";
       WinRSCore.executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
       cmd = "New-NetFirewallRule -DisplayName 'HTTP(S) Outbound' -Profile Domain -Direction Outbound" +
                    " -Action Allow -Protocol TCP -LocalPort '" + port + "';";
       WinRSCore.executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
    }

    private void startDriverOnRemote(String node){
        if ( isDriverRunningOnRemote(node)) {
            Log.debug("WiniumDesktopDriver already running on remote node " + node);
            return;
        }

        Log.debug("Trying to start WiniumDesktopDriver on node " + node);
        String port = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.port");
        String userDir = WinRSCore.getUserDir(node);
        String script = "WiniumDriverStarter.ps1";
        String cmd = "If (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] \"Administrator\"))" +
            "{$arguments = \"& '\" + $myinvocation.mycommand.definition + \"'\";Start-Process powershell -WindowStyle Hidden -Verb runAs -ArgumentList $arguments;Break};" +
            "$port = \"--port " + port + "\";Start-Process \"" + userDir + "\\Winium.Desktop.Driver.exe\" $port -Wait -NoNewWindow;";
        WinRSCore.transferScript(node, cmd, script);
        String pathToScript = userDir + "\\" + script;
        WinRSCore.runScriptAsScheduledTask(node, pathToScript);
    }

    private void awaitForDriverToStartOnRemote(String node){
        Log.debug("Checking if WiniumDesktopDriver was started with timeout 60 seconds");
        String port = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.port");
        String cmd = "netstat -an | findstr \"" + port + "\"";
        int i = 0;
        while ( i < 30 ){
            String result = WinRSCore.executeSingleCommandOnRemote(cmd, node, 60);
            if (result.contains("LISTENING")){
                break;
            }
            StepCore.sleep(2);
            i++;
        }

        if ( i == 30 ){
            Log.error("Port " + port + " not in state LISTENING on node " + node + "!" +
                    "Please make sure that it is not blocked by firewall " +
                    "and that Winium.Desktop.Driver.exe process was started!");
        }

    }

    private void awaitForDriverToStartOnLocal(String port){
        Log.debug("Checking if WiniumDesktopDriver was started with timeout 60 seconds");
        File workingDir = FileCore.getTempDir();
        String cmd = "netstat -an | findstr " + port;
        int i = 0;
        while ( i < 30 ){
            String result = ExecutorCore.execute("Powershell.exe \"" +cmd + "\"", workingDir, 60).getStdOut();
            if (result.contains("LISTENING")){
                break;
            }
            StepCore.sleep(2);
            i++;
        }

        if ( i == 30 ){
            Log.error("Port " + port + " not in state LISTENING on localhost!" +
                    "Please make sure that it is not blocked by firewall " +
                    "and that Winium.Desktop.Driver.exe process was started!");
        }

    }


    private WiniumDriver createOnLocal(String port, String path, String args){
        startWiniumResourcesOnLocal(port);
        URL url = buildUrl("localhost", port);
        DesktopOptions options = buildOptions("localhost", path, args);

        WiniumDriver driver = startApp(url, options, path, args);

        //initialize winiumCore with driver
        WiniumCore winiumCore = new WiniumCore();
        scenarioCtx.put("WiniumCore", WiniumCore.class, winiumCore);

        return driver;
    }

    private WiniumDriver createOnRemote(String node, String port, String path, String args){
        startWiniumResourcesOnRemote(node);

        URL url = buildUrl(node, port);
        DesktopOptions options = buildOptions(node, path, args);

        WiniumDriver driver = startApp(url, options, path, args);

        //initialize winiumCore with driver
        WiniumCore winiumCore = new WiniumCore();
        scenarioCtx.put("WiniumCore", WiniumCore.class, winiumCore);

        return driver;
    }


    //check if app we want to run exists otherwise unknown error or access denied error can be thrown!
    private void verifyPathOnRemote(String node, String path){
        Log.debug("Checking that path " + path + " exists on the node " + node);
        if ( path.contains(":\\") || path.contains(":/") || path.contains("$")){
            if ( ! WinRSCore.checkThatFileExists(node, path) ) {
                Log.error("File " + path + " does not exists!");
            }
        } else{
            Log.warn("Full path to app not provided!");
        }
    }


    //check if app we want to run exists otherwise unknown error or access denied error can be thrown!
    private void verifyPathOnLocal(String path){
        Log.debug("Checking that path " + path + " exists");
        if ( path.contains(":\\") || path.contains(":/") || path.contains("$")){
            File workingDir = FileCore.getTempDir();
            String cmd = "Test-Path -Path '" + path + "'";
            ExecResult out = ExecutorCore.execute("Powershell.exe \"" + cmd + "\"", workingDir, 60);
            if ( ! out.getStdOut().contains("True")) {
                Log.error("File " + path + " does not exists!");
            }
        } else{
            Log.warn("Full path to app not provided!");
        }
    }


    private void startWiniumResourcesOnLocal(String port){
        File workingDir = FileCore.getTempDir();
        String path = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.path");
        //Set winium driver path
        //System.setProperty("webdriver.winium.driver.desktop", FileCore.getProjectPath() + File.separator + path);
        //WiniumDriverService service = new WiniumDriverService.Builder()
        //        .usingPort(Integer.valueOf(port)).withVerbose(true).withSilent(false).build();
        //service.start(); //Build and Start a Winium Driver service

        //Builder seems to be not working correctly with custom port:/

        String cmd = "Start-Process -WindowStyle Hidden -FilePath 'powershell.exe' -ArgumentList '-command \"& " + FileCore.getProjectPath() + File.separator + path + " --port " + port + "\"'";
        Log.debug("Command to execute is " + cmd);
        ExecutorCore.execute("powershell.exe \"" + cmd + "\"", workingDir, 60);

        /*
        File script = FileCore.createTempFile("WiniumDriverStarter", "ps1");
        String cmd = "If (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] \"Administrator\"))" +
                "{$arguments = \"& '\" + $myinvocation.mycommand.definition + \"'\";Start-Process powershell -WindowStyle Hidden -Verb runAs -ArgumentList $arguments;Break};" +
                "Start-Job {" + FileCore.getProjectPath() + File.separator + path + " --port " + port + "}";
        FileCore.writeToFile(script, cmd);
        ExecutorCore.execute("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File '" + script.getAbsolutePath() + "'", workingDir, 60);
        */
        awaitForDriverToStartOnLocal(port);
    }

    private void startWiniumResourcesOnRemote(String node){
        Log.debug("Starting winium resources");

        scenarioCtx.put("WiniumRemoteNodeId", String.class, node);
        String rdpProcessId = openRdpToRemote(node);
        scenarioCtx.put("WiniumRemoteRDPProcessId", String.class, rdpProcessId);

        boolean isAvailable = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.uploadAndStart");
        if ( !isAvailable ) {
            uploadDriverOnRemote(node);
        }

        closeAllOpenWindows(node);
        minimizeAllWindows(node, "minimizeWindowsTask");

        startDriverOnRemote(node);
        awaitForDriverToStartOnRemote(node);
    }

    /**
     * Opens remote desktop session towards remote host<br>
     * Returns processId of a RDP session
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     *
     * @return String
     */
    public String openRdpSession(String node, Boolean forceLogOff){
        File workingDir = FileCore.getTempDir();

        Map<String, String> conn = WinRSCore.verifyConnectionDetails(node);
        String address = conn.get("address");
        String domain = conn.get("domain");
        String user = conn.get("user");
        String passwd = conn.get("password");

        address = WinRSCore.getIpOfHost(address);

        Log.debug("Checking if there is RDP session open towards node " + address);

        String cmd = "powershell.exe \"Get-CimInstance Win32_Process | Where {$_.name -match '.*mstsc.*'" +
                " -and $_.CommandLine -match '.*" + address + ".*'} | Select ProcessId | Format-list\"";
        ExecResult out = ExecutorCore.execute(cmd, workingDir, 10);
        String output = out.getStdOut();
        String rdpProcessId = "";
        rdpProcessId = output.trim().replace("ProcessId :","").trim();
        if ( ! rdpProcessId.equals("") ) {
            Log.debug("Rdp session is open towards " + address + " with process id " + rdpProcessId);

            killRdpSession(rdpProcessId);
        }

        //add certificate hash to the registry so we can avoid any popUp with invalid cert confirmation
        Boolean isCertificateSet = globalCtx.get("CertificateHash_" + address, Boolean.class);

        if ( isCertificateSet == null){
            isCertificateSet = false;
        }

        if ( !isCertificateSet ) {
            Log.debug("Checking cert hash for node " + address + " in windows registry");
            cmd = "wmic /node:" + address + " /namespace:\\\\root\\CIMV2\\TerminalServices PATH Win32_TSGeneralSetting get SSLCertificateSHA1Hash";
            out = ExecutorCore.execute(cmd, workingDir, 10);
            output = out.getStdOut();
            String hashCode = output.trim().replaceAll("SSLCertificateSHA1Hash", "").trim();

            Log.debug("Node's " + address + " cert hashCode is " + hashCode);
            if (!hashCode.equals("")) {
                Log.debug("Adding cert hash to the registry");
                cmd = "REG ADD \"HKEY_CURRENT_USER\\Software\\Microsoft\\Terminal Server Client\\Servers\\" + address + "\" /v \"CertHash\" /t REG_BINARY /d " + hashCode + " /f";
                ExecutorCore.execute(cmd, workingDir, 10);
            }
        }

        //set global variable per node
        globalCtx.put("CertificateHash_" + address, Boolean.class, true);

        //switch off scaling so we do not run into issues when winium is started
        Boolean isScalingSet = globalCtx.get("ScalingSet_" + address, Boolean.class);

        if ( isScalingSet == null){
            isScalingSet = false;
        }

        if ( !isScalingSet ) {
            Log.debug("Setting Display Settings for Current User (96 DPI/100%)");
            List<String> cmds = new ArrayList<>();
            cmds.add("$path = \"HKCU:\\Control Panel\\Desktop\";$name='LogPixels';$val='96';$type='DWORD'");
            cmds.add("if(!(Test-Path $path))");
            cmds.add("{New-Item -Path $path -Force;New-ItemProperty -Path $path -Name $name -Value $val -PropertyType $type -Force}");
            cmds.add("else{New-ItemProperty -Path $path -Name $name -Value $val -PropertyType $type -Force}");
            cmds.add("$path = \"HKCU:\\Control Panel\\Desktop\";$name='Win8DpiScaling';$val='1';$type='DWORD'");
            cmds.add("if(!(Test-Path $path))");
            cmds.add("{New-Item -Path $path -Force;New-ItemProperty -Path $path -Name $name -Value $val -PropertyType $type -Force}");
            cmds.add("else{New-ItemProperty -Path $path -Name $name -Value $val -PropertyType $type -Force}");
            cmds.add("$path = \"HKCU:\\Software\\Microsoft\\Windows\\DWM\";$name='UseDpiScaling';$val='0';$type='DWORD'");
            cmds.add("if(!(Test-Path $path))");
            cmds.add("{New-Item -Path $path -Force;New-ItemProperty -Path $path -Name $name -Value $val -PropertyType $type -Force}");
            cmds.add("else{New-ItemProperty -Path $path -Name $name -Value $val -PropertyType $type -Force}");

            String script = "fixScalingSettings.ps1";
            cmd = WinRSCore.joinCommands(cmds, "\r\n", false);
            WinRSCore.transferScript(node, cmd, script);
            WinRSCore.executeSingleCommandOnRemote("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);

            //force log off so new settings will be applicable to the next session
            if (forceLogOff) {
                Log.debug("Force logoff");
                cmds = new ArrayList<>();
                cmds.add("$serverName = 'localhost'");
                cmds.add("$sessions = qwinsta /server $serverName| ?{ $_ -notmatch '^ SESSIONNAME' } | %{");
                cmds.add("$item = \"\" | Select \"Active\", \"SessionName\", \"Username\", \"Id\", \"State\", \"Type\", \"Device\"");
                cmds.add("$item.Active = $_.Substring(0,1) -match '>'");
                cmds.add("$item.SessionName = $_.Substring(1,18).Trim()");
                cmds.add("$item.Username = $_.Substring(19,20).Trim()");
                cmds.add("$item.Id = $_.Substring(39,9).Trim()");
                cmds.add("$item.State = $_.Substring(48,8).Trim()");
                cmds.add("$item.Type = $_.Substring(56,12).Trim()");
                cmds.add("$item.Device = $_.Substring(68).Trim()");
                cmds.add("$item");
                cmds.add("}");
                cmds.add("foreach ($session in $sessions){");
                cmds.add("if($session.Username -ne \"\" -or $session.Username.Length -gt 1){");
                cmds.add("Write-Host $session.Username is going to be logged off");
                cmds.add("logoff /server $serverName $session.Id");
                cmds.add("}");
                cmds.add("}");

                script = "forceLogoffUsers.ps1";
                cmd = WinRSCore.joinCommands(cmds, "\r\n", false);
                WinRSCore.transferScript(node, cmd, script);
                WinRSCore.executeSingleCommandOnRemote("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            }
        }

        //set global variable per node
        globalCtx.put("ScalingSet_" + address, Boolean.class, true);

        String size = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.size");
        cmd = "";
        if ( size == null
                || size.equals("")
                || StringUtils.containsIgnoreCase(size, "Default") ) {
            //default is set to maximise browser window
            Log.debug("Going to use default resolution of RDP window");
            cmd = "powershell.exe \"Start-Job{$Server='" + address + "';$User='" + domain + "\\" + user + "';" +
                    "$Pass='" + passwd + "';cmdkey /generic:$Server /user:$User /pass:$Pass;mstsc /v:$Server}\"";
        } else {
            Log.debug("Going to use " + size + " resolution of RDP window");
            //expected format is width x height
            String tmp = StringUtils.deleteWhitespace(size).trim();
            String[] dimensions = tmp.split("[xX]");
            Integer width = null;
            Integer height = null;
            try {
                width = Integer.parseInt(dimensions[0]);
                height = Integer.parseInt(dimensions[1]);
            } catch (NumberFormatException e) {
                Log.error(e.getMessage());
            }
            cmd = "powershell.exe \"Start-Job{$Server='" + address + "';$User='" + domain + "\\" + user + "';" +
                    "$Pass='" + passwd + "';cmdkey /generic:$Server /user:$User /pass:$Pass;mstsc /v:$Server /h:" + height + " /w:" + width + "}\"";
        }
        Log.debug("Opening Rdp session to " + node);

        //RDP session needs to be started in the background
        ExecutorCore.execute(cmd, workingDir, 10);

        Log.debug("Extracting process id of RDP session");

        cmd = "powershell.exe \"Get-CimInstance Win32_Process | Where {$_.name -match '.*mstsc.*'" +
                " -and $_.CommandLine -match '.*" + address + ".*'} | Select ProcessId | Format-list\"";
        out = ExecutorCore.execute(cmd, workingDir, 10);
        output = out.getStdOut();
        rdpProcessId = output.trim().replace("ProcessId :","").trim();
        Log.debug("Rdp session process id is " + rdpProcessId);

        //RDP needs few seconds to establish a desktop connection
        StepCore.sleep(3);

        return rdpProcessId;
    }

    /**
     * Minimizes all open windows on a remote windows host by calling powershell script as a scheduled task
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param taskName String, name that shall be assigned to the task
     */
    public void minimizeAllWindows (String node, String taskName){

        Map<String, String> conn = WinRSCore.verifyConnectionDetails(node);
        String domain = conn.get("domain");
        String user = conn.get("user");
        String password = conn.get("password");

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding ){
            password = StepCore.decodeString(password);
        }

        String userDir = WinRSCore.getUserDir(node);

        Log.debug("Minimizing all open windows");

        String script = "temp_minimize_windows.ps1";

        List<String> cmdList = new ArrayList<>();
        cmdList.add("$shell = New-Object -ComObject \"Shell.Application\";");
        cmdList.add("$shell.MinimizeAll()");

        String cmd = WinRSCore.joinCommands(cmdList, "\r\n", true);
        WinRSCore.transferScript(node, cmd, script);

        String pathToScript = userDir + "\\" + script;

        Log.debug("Creating new scheduled task " + taskName +
                "Assigning normal priority to it. " +
                "Recreating task with priority");

        //this script is used to run the original one
        //it will be called from a scheduled task and in turn it will call the original one:)
        script = "wrapperTaskmw.ps1";

        cmd = "Start-process powershell -WindowStyle Hidden -Wait" +
                " -ArgumentList \"-NoProfile -ExecutionPolicy Bypass" +
                " -Command " + pathToScript + "\"";
        WinRSCore.transferScript(node, cmd, script);

        //this script is used to create scheduled task
        script = "tempTaskmw.ps1";

        cmdList = new ArrayList<>();
        cmdList.add("schtasks /CREATE /TN '" + taskName + "' /SC MONTHLY /RL HIGHEST " +
                "/RU \"" + domain + "\\" + user + "\" /IT " +
                "/RP \"" + password + "\" " +
                "/TR \"powershell -windowstyle hidden -NoProfile -ExecutionPolicy Bypass -File " + userDir + "\\wrapperTaskmw.ps1\" " +
                "/F;");
        cmdList.add("$taskFile = \"" + userDir + "\\RemoteTask.txt\";");
        cmdList.add("Remove-Item $taskFile -Force -ErrorAction SilentlyContinue;");
        cmdList.add("[xml]$xml = schtasks /QUERY /TN '" + taskName + "' /XML;");
        cmdList.add("$xml.Task.Settings.Priority=\"4\";");
        cmdList.add("$xml.Save($taskFile);");
        cmdList.add("schtasks /CREATE /TN '" + taskName + "' /RU \"" + domain + "\\" + user + "\" " +
                "/IT /RP \"" + password + "\" " +
                "/XML $taskFile /F;");

        cmd = WinRSCore.joinCommands(cmdList, "\r\n", true);
        WinRSCore.transferScript(node, cmd, script);

        String result = WinRSCore.executeSingleCommandOnRemote("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
        if ( ! result.contains("SUCCESS:") ){
            Log.error("Failed to create new scheduled task TempAutomationTask");
        } else {
            Log.debug("New scheduled task TempAutomationTask created");
        }

        Log.debug("Running scheduled task");
        cmd = "schtasks /RUN /I /TN '" + taskName + "';";
        result = WinRSCore.executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
        if ( ! result.contains("SUCCESS:") ){
            Log.error("Failed to run scheduled task TempAutomationTask");
        }

        StepCore.sleep(2);

        Log.warn("Deleting scheduled task " + taskName);
        cmd = "schtasks /DELETE /TN '" + taskName + "' /F;";
        WinRSCore.transferScript(node, cmd, script);

        result = WinRSCore.executeSingleCommandOnRemote("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
        if ( ! result.contains("SUCCESS:") ){
            Log.warn("Failed to remove scheduled task " + taskName);
        }

    }


    /**
     * Checks if an RDP session is open towards particular node
     *
     * @param rdpProcessId String, processID of an RDP session
     * @return Boolean
     */
    private Boolean checkIfRdpSessionIsOpen(String rdpProcessId){
        Log.debug("Checking RDP session with process id  " + rdpProcessId);

        boolean exitValue = false;
        File workingDir = FileCore.getTempDir();

        String cmd = "powershell.exe \"$process=Get-process -Id " + rdpProcessId + " -ErrorAction SilentlyContinue;if($process -eq $null){Write-Host NotRunning}\"";
        ExecResult out = ExecutorCore.execute(cmd, workingDir, 10);
        String result = out.getStdOut();
        if ( ! result.contains("NotRunning") ){
            exitValue = true;
        }

        return exitValue;
    }

    /**
     * Kills RDP session
     *
     * @param rdpProcessId String, processId of an open RDP session
     */
    private void killRdpSession(String rdpProcessId){
        Log.debug("Killing RDP session with process id  " + rdpProcessId);

        File workingDir = FileCore.getTempDir();

        String cmd = "powershell.exe \"Stop-process -Id " + rdpProcessId + " -Force\"";
        ExecutorCore.execute(cmd, workingDir, 10);
    }

    /**
     * Closes all open windows on a remote host
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     */
    private void closeAllOpenWindows(String node){
        Log.debug("Closing all open windows");

        String cmd = "(New-Object -comObject Shell.Application).Windows() | foreach-object {$_.quit()};" +
                "(Get-Process | Where-Object {$_.MainWindowTitle -ne \"\"}).CloseMainWindow();";

        String script = "temp.ps1";
        WinRSCore.transferScript(node, cmd, script);
        String userDir = WinRSCore.getUserDir(node);
        WinRSCore.runScriptAsScheduledTask(node, userDir + "\\" + script);
    }

    private URL buildUrl(String node, String port){
        String host = "localhost";
        if ( ! node.equalsIgnoreCase("localhost") ) {
            host = WinRSCore.getIpOfHost(Storage.get("Environment.Active.WinRM." + node + ".host"));
        }
        String url = "http://" + host + ":" + port;
        Log.debug("Url is " + url);
        URL uri = null;
        try {
            uri = new URL(url);
        } catch (MalformedURLException e) {
            Log.error(e.getMessage());
        }

        return uri;
    }


    private DesktopOptions buildOptions(String node, String path, String args){
        if ( ! node.equalsIgnoreCase("localhost") ) {
            verifyPathOnRemote(node, path);
        } else {
            verifyPathOnLocal(path);
        }

        Log.debug("Preparing app options");
        DesktopOptions options = new DesktopOptions();
        options.setApplicationPath(path);

        if ( args != null && (! args.equals("")) ) {
            Log.debug("Setting application arguments " + args);
            options.setArguments(args);
        } else {
            Log.debug("No application arguments provided " + args);
        }

        return options;
    }


    private WiniumDriver startApp(URL uri, DesktopOptions options, String path, String args){
        WiniumDriver driver = new WiniumDriver(uri, options);
        scenarioCtx.put("WiniumDesktopDriver", WiniumDriver.class, driver);

        if ( args != null && (! args.equals("")) ){
            Log.debug("Started an app from " + path + " " + args);
        } else {
            Log.debug("Started an app from " + path);
        }

        driver = scenarioCtx.get("WiniumDesktopDriver", WiniumDriver.class);
        if ( driver == null ){
            Log.error("Winium.Desktop.Driver was not initialized properly!");
        }

        return driver;
    }


    private String getWiniumDesktopDriverProcessIdOnRemote(String node){
        String cmd = "Get-CimInstance Win32_Process | Where {$_.name -match '.*Winium.*'} | Select Caption, CommandLine, ProcessId | Format-list";
        String result = WinRSCore.executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
        String[] tmp = StringUtils.deleteWhitespace(result.trim()).split("ProcessId:");

        return tmp[tmp.length - 1].trim();
    }

    private void killWiniumDesktopDriverProccessOnRemote(String node){
        Log.debug("Closing Winium.Desktop.Driver.exe");

        String processId = getWiniumDesktopDriverProcessIdOnRemote(node);
        String cmd = "Stop-Process -Id " + processId + " -Force -passThru";
        WinRSCore.executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
    }

    private void deleteWiniumDesktopDriverScheduledTaskOnRemote(String node){
        Log.debug("Deleting scheduled task used to run Winium driver");

        String script = "tempTask.ps1";
        String cmd = "schtasks /DELETE /TN 'TempAutomationTask' /F;";
        WinRSCore.transferScript(node, cmd, script);

        String result = WinRSCore.executeSingleCommandOnRemote("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
        if ( !result.contains("SUCCESS:") ) {
            Log.error("Failed to remove scheduled task TempAutomationTask");
        }
    }

    private void killWiniumDesktopDriverProcessOnLocal(){
        File workingDir = FileCore.getTempDir();

        String cmd = "Get-CimInstance Win32_Process | Where {$_.name -match '.*Winium.*'} | Select Caption, CommandLine, ProcessId | Format-list";
        ExecResult out = ExecutorCore.execute("Powershell.exe \"" + cmd + "\"", workingDir, 60);
        String result = out.getStdOut();
        if ( ! result.contains("Winium.Desktop.Driver.exe") ) {
            return;
        }

        Log.debug("Closing Winium.Desktop.Driver.exe");
        String[] tmp = StringUtils.deleteWhitespace(result.trim()).split("ProcessId:");
        String processId = tmp[tmp.length - 1].trim();
        cmd = "Stop-Process -Id " + processId + " -Force -passThru";
        ExecutorCore.execute("Powershell.exe \"" + cmd + "\"", workingDir, 60);
    }

}