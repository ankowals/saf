package libs.libCore.modules;

import org.apache.commons.lang.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class WinRSCore {

    private SharedContext ctx;
    private FileCore FileCore;
    private StepCore StepCore;
    private ExecutorCore ExecutorCore;
    private Storage Storage;

    // PicoContainer injects class SharedContext
    public WinRSCore(SharedContext ctx) {
        this.ctx = ctx;
        this.FileCore = ctx.Object.get("FileCore",FileCore.class);
        this.ExecutorCore = ctx.Object.get("ExecutorCore",ExecutorCore.class);
        this.Storage = ctx.Object.get("Storage",Storage.class);
        this.StepCore = ctx.Object.get("StepCore",StepCore.class);
    }


    /**
     * Adds host to a trusted hosts list
     *
     * @param name String, name of the host to be added to trusted hosts list
     */
    public void addToTrustedHosts(String name){
        File workingDir = FileCore.getTempDir();

        String cmd = "cmd.exe /c \"Powershell.exe winrm get winrm/config/client\"";
        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 60, true);

        String result = new String(out.toByteArray(), Charset.defaultCharset());

        if ( ! (result.contains("Client") && result.contains("TrustedHosts = " + name)) ) {

            cmd = "cmd.exe /c \"winrm quickconfig -quiet\"";
            ExecutorCore.execute(cmd, workingDir, 60, true);

            cmd = "cmd.exe /c \"echo winrm set winrm/config/client @{TrustedHosts=^\"" + name + "^\"} > temp.bat\"";
            ExecutorCore.execute(cmd, workingDir, 60, true);

            cmd = "cmd.exe /c \"Powershell.exe (gc temp.bat ^| out-string).replace('TrustedHosts=', 'TrustedHosts=' + [char]34) ^| sc temp.bat\"";
            ExecutorCore.execute(cmd, workingDir, 60, true);

            cmd = "cmd.exe /c \"Powershell.exe (gc temp.bat ^| out-string).replace([char]125 + '', [char]34 + '' + [char]125) ^| sc temp.bat\"";
            ExecutorCore.execute(cmd, workingDir, 60, true);

            ExecutorCore.execute("cmd.exe /c \"type temp.bat\"", workingDir, 60, true);

            out = ExecutorCore.execute("cmd.exe /c \"call temp.bat\"", workingDir, 60, true);
            result = new String(out.toByteArray(), Charset.defaultCharset());

            if (!(result.contains("Client") && result.contains("TrustedHosts = " + name))) {
                Log.warn("Unable to add host " + name + " to trusted host list for winrm client");
            }
        } else {
            Log.debug("WinRM config already set. Nothing to do.");
        }
    }


    /**
     * Executes single cmd on a remote windows host using winrs client.<br>
     * Output will be printed to the log and returned as String.<br>
     * Command execution will be stopped when timeout is reached.
     * For example to run a powershell script cmd shall be set to<br>
     * "Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\""
     *
     * @param cmd String, command to be executed via windows cmd
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param timeout Integer, timeout in seconds
     *
     * @return String
     */
    public String executeSingleCommandOnVM (String cmd, String node, Integer timeout){
        File workingDir = FileCore.getTempDir();

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        Integer port = Storage.get("Environment.Active.WinRM." + node + ".port");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String domain = Storage.get("Environment.Active.WinRM." + node + ".domain");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }
        if ( port == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".port not found or null!");
        }
        if ( user == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".user not found or null!");
        }
        if ( domain == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".domain not found or null!");
        }
        if ( passwd == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".password not found or null!");
        }

        String invocation = "winrs -r:http://" + address + ":" + port + " -u:" + domain + "\\" + user + " -p:" + passwd;
        cmd =  invocation + " " + cmd;

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, timeout, true);
        String result = new String(out.toByteArray(), Charset.defaultCharset());

        return result;
    }


    /**
     * Executes batch file on a remote windows host using winRS client<br>
     * Batch file will be created from list of commands provided as input<br>
     * File will be created in users home directory
     *
     * @param cmdList List, list oc commands that shall be content of the batch files
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param timeout Integer, timeout in seconds
     *
     * @return String
     */
    public String executeBatchFileOnVM(List<String> cmdList, String node, Integer timeout){

        String cmd = joinCommands(cmdList, "\r\n", true);
        String script = "temp.bat";
        transferScript(node, cmd, script);
        String result = executeSingleCommandOnVM("call " + script, node, timeout);

        return result;
    }


    /**
     * Executes an sql script on a remote windows host.<br>
     * Sql script will be created from the list of commands provided as input.<br>
     * File will be created in users home directory.
     *
     * @param cmdList List, list oc commands that shall be content of the batch files
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param timeout Integer, timeout in seconds
     *
     * @return String
     */
    public String executeSqlFileOnVM(List<String> cmdList, String node, Integer timeout) {

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        String dbUser = Storage.get("TestData.MsSql.User");
        String dbpass = Storage.get("TestData.MsSql.Pass");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        String cmd = joinCommands(cmdList, "\r\n", true);
        String script = "temp.sql";
        transferScript(node, cmd, script);
        String result = executeSingleCommandOnVM(" SQLCMD -b -S " + address + " -U " + dbUser + " -P " + dbpass + " -i " + script, node, timeout);

        return result;
    }


    /**
     * Returns path to users home dir on a remote windows host.
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     *
     * @return String
     */
    public String getUserDir(String node){
        File workingDir = FileCore.getTempDir();

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        Integer port = Storage.get("Environment.Active.WinRM." + node + ".port");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String domain = Storage.get("Environment.Active.WinRM." + node + ".domain");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }
        if ( port == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".port not found or null!");
        }
        if ( user == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".user not found or null!");
        }
        if ( domain == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".domain not found or null!");
        }
        if ( passwd == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".password not found or null!");
        }

        //check if user dir per node is known else retrieve it from node
        Class clazz = ExecutionContext.executionContextObject().setType("java.lang.String");
        String userDir = ExecutionContext.executionContextObject().get("userDir_" + node, clazz);

        if ( userDir == null || userDir.equals("") ) {
            Log.warn("userDir not known. Going to check it on remote host");

            String invocation = "winrs -r:http://" + address + ":" + port + " -u:" + domain + "\\" + user + " -p:" + passwd;
            ByteArrayOutputStream out = ExecutorCore.execute(invocation + " cd", workingDir, 120, true);
            String result = new String(out.toByteArray(), Charset.defaultCharset());

            String[] lines = result.split(System.getProperty("line.separator"));
            userDir = lines[0].trim();

            //set global variable per node
            ExecutionContext.executionContextObject().put("userDir_" + node, clazz, userDir);

            Log.debug("userDir stored");
        }

        return userDir;
    }


    /**
     * Modifies winRM configuration on a remote windows host. Following parameters are modified<br>
     *     MaxShellsPerUser<br>
     *     IdleTimeout<br>
     *     MaxProcessesPerShell<br>
     *     MaxMemoryPerShellMB
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     */
    public void adjustWinRmConfig(String node){

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        Log.debug("Adjusting winRM config on client side");

        addToTrustedHosts(address);

        String maxMemoryPerShell = "16384";     //in MB 16 GB
        String maxProcessesPerShell = "100";
        String idleTimeout = "12600000";        //in ms 3,5 h
        String maxShellsPerUser = "50";

        Log.debug("Checking current winRS config on remote host " + address);
        String output = executeSingleCommandOnVM("winrm get winrm/config", node, 60);
        if ( output.contains("MaxShellsPerUser = " + maxShellsPerUser) &&
                output.contains("IdleTimeout = " + idleTimeout) &&
                output.contains("MaxProcessesPerShell = " + maxProcessesPerShell) &&
                output.contains("MaxMemoryPerShellMB = " + maxMemoryPerShell)
                ) {

            Log.debug("WinRS configuration on host " + address + " already set. Nothing to do.");

        } else {

            Log.debug("Adjusting winRS config on a remote host " + address);
            List<String> cmdList = new ArrayList();
            cmdList.add("call winrm set winrm/config/winrs @{MaxMemoryPerShellMB=\"" + maxMemoryPerShell + "\"}");
            cmdList.add("call winrm set winrm/config/winrs @{MaxProcessesPerShell=\"" + maxProcessesPerShell + "\"}");
            cmdList.add("call winrm set winrm/config/winrs @{MaxShellsPerUser=\"" + maxShellsPerUser + "\"}");
            cmdList.add("call winrm set winrm/config/winrs @{IdleTimeout=\"" + idleTimeout + "\"}");
            //cmdList.add("call winrm set winrm/config/service @{AllowUnencrypted=\"true\"}");
            //cmdList.add("call winrm set winrm/config/service/auth @{Basic=\"true\"}");

            String cmd = joinCommands(cmdList, "\r\n", true);
            String script = "temp.bat";
            transferScript(node, cmd, script);

            String result = executeSingleCommandOnVM("call " + script, node, 120);

            Log.debug("Validating winRS configuration");

            if (!result.contains("MaxMemoryPerShellMB = " + maxMemoryPerShell)) {
                Log.warn("Failed to set winRS configuration (parameter MaxMemoryPerShell)!");
            }

            if (!result.contains("MaxProcessesPerShell = " + maxProcessesPerShell)) {
                Log.warn("Failed to set winRS configuration (parameter MaxProcessesPerShell)!");
            }

            if (!result.contains("MaxShellsPerUser = " + maxShellsPerUser)) {
                Log.warn("Failed to set winRS configuration (parameter MaxShellsPerUser)!");
            }

            if (!result.contains("IdleTimeout = " + idleTimeout)) {
                Log.warn("Failed to set winRS configuration (parameter IdleTimeout)!");
            }

        }

    }


    /**
     * Awaits up to defined timeout of 55 minutes for host availability via winrm service<br>
     * Can be used in case of connectivity issues after remote host restart
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     */
    public void awaitForHostAvailability(String node){

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        Log.debug("Refreshing DNS cache to make connection quicker");
        File workingDir = FileCore.getTempDir();
        String cmd = "ipconfig /flushdns";
        ExecutorCore.execute(cmd, workingDir, 60, true);

        Log.debug("Checking if host " + address + " is accessible");

        String result = "";
        Integer i = 0;
        while ( ! result.contains("host " + address + " is accessible") ) {

            result = executeSingleCommandOnVM("'echo host " + address + " is accessible'", node, 60);

            if ( result.contains("host " + address + " is accessible")){
                break;
            }

            StepCore.sleep(60);
            i++;

            if ( i == 55 ){
                Log.error("Host not accessible! Timeout of 55 minutes reached! " + "" +
                        "Consider to add remote host address " + address + " to Trusted hosts list");
            }

        }

    }


    /**
     * Awaits up to defined timeout of 55 minutes for host availability via RDP<br>
     * Can be used in case of connectivity issues after remote host restart
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     */
    public void awaitForHostRdpAvailability(String node){

        File workingDir = FileCore.getTempDir();
        String address = Storage.get("Environment.Active.WinRM." + node + ".host");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        Log.debug("Refreshing DNS cache to make connection quicker");
        String cmd = "ipconfig /flushdns";
        ExecutorCore.execute(cmd, workingDir, 60, true);

        Log.debug("Checking if RDP session towards " + address + " can be open");
        Integer i = 0;
        String tmp = "";
        while ( ! tmp.contains("TcpTestSucceeded:True") ) {

            cmd = "cmd.exe /c \"Powershell.exe Test-NetConnection " + address + " -CommonTcpPort RDP\"";
            ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 120, true);

            tmp = new String(out.toByteArray(), Charset.defaultCharset());
            String result = StringUtils.deleteWhitespace(tmp).trim();

            if ( result.contains("TcpTestSucceeded:True")){
                break;
            }

            StepCore.sleep(60);
            i++;

            if ( i == 55 ){
                Log.error("RDP on host not accessible! Timeout of 55 minutes reached!");
            }
        }

    }


    /**
     * Checks that file exists on a remote host
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param path String, path to the file on a remote host, for example C:\\Users\\superuser\\remoteFile.txt
     *
     * @return Boolean
     **/
    public Boolean checkThatFileExists(String node, String path){

        if (path.length() == 0 || path.equals("")){
            Log.error("Provided path is empty!");
        }

        String result = executeSingleCommandOnVM("if exist " + path + " (echo exist)", node, 60);
        if ( ! result.contains("exist")) {
            return false;
        }

        return true;

    }


    /**
     * Checks that list of files exists on a remote window host.<br>
     * An error will be written if any of the files does not exist.
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param paths List, paths to files that shall exists on a remote host
     */
    public void verifyThatFilesExist(String node, List<String> paths){

        if (paths.size() == 0){
            Log.error("Provided list of paths is empty!");
        }

        String sPaths = "";
        for ( String path : paths) {
            sPaths = sPaths + ", \"" + path + "\"";
        }

        String script = "temp.ps1";
        String cmd = "Write-Host \"Checking files availability\";$paths=@(" + sPaths.replaceFirst(", ","") + ");foreach($path in $paths){" +
                "Write-Host $path;if (!(Test-Path $path)){Write-Host \"Error: Path '$path' not found\"}}";

        transferScript(node, cmd, script);
        String result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);

        if ( ! result.contains("Checking files availability") ){
            Log.error("File validation script not executed");
        }

        for ( String path : paths ) {
            if (result.trim().contains("Path '" + path + "' not found")) {
                Log.error("Path '" + path + "' does not exist on node " + node );
            }
        }

    }


    /**
     * Copies script to a remote windows host using powershell capabilities NewPsSession.<br>
     * Script will be available in users home dir.
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param content String, content of the script that shall be transferred to remote host
     * @param name String, name of the script
     */
    public void transferScript(String node, String content, String name){
        File workingDir = FileCore.getTempDir();

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        Integer port = Storage.get("Environment.Active.WinRM." + node + ".port");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String domain = Storage.get("Environment.Active.WinRM." + node + ".domain");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }
        if ( port == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".port not found or null!");
        }
        if ( user == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".user not found or null!");
        }
        if ( domain == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".domain not found or null!");
        }
        if ( passwd == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".password not found or null!");
        }

        //create file in temp dir
        File dir = FileCore.createTempDir();
        String dirPath = dir.getAbsolutePath();
        File file = new File(dirPath + File.separator + name);
        //write content to newly created file
        FileCore.writeToFile(file, content);
        String item = file.getAbsolutePath();

        //extract file name to upload
        int idx = item.lastIndexOf(File.separator);
        String fileName = item.substring(idx+1,item.length()).trim();

        //create target path
        String userDir = getUserDir(node);
        String target = "\\\\" + address + File.separator + userDir.replaceFirst(":","\\$") + File.separator + fileName;

        Integer i = 0;
        String output = "Connecting to remote server failed";
        while( i < 5 ) {
            String cmd = "cmd.exe /c \"Powershell.exe $U='" + domain + "\\" + user + "';" +
                    "$P='" + passwd + "';" +
                    "$pw=ConvertTo-SecureString $p -AsPlainText -Force;" +
                    "$cred=New-Object Management.Automation.PSCredential($U, $pw);" +
                    "$Src=[System.IO.File]::ReadAllBytes('" + item + "');" +
                    "$s=New-PSSession -computerName " + address + " -credential $cred;" +
                    "Enter-PSSession $s;" +
                    "Invoke-Command -Session $s -Scriptblock " +
                    "{if(test-path " + target + "){Remove-Item " + target + " -force -recurse}};" +
                    "Invoke-Command -Session $s -ArgumentList $Src -Scriptblock " +
                    "{[System.IO.File]::WriteAllBytes('" + target + "', $args)};" +
                    "Write-Host 'Content of created file " + userDir + "\\" + fileName + " is';" +
                    "Invoke-Command -Session $s -Scriptblock " +
                    "{type " + userDir + "\\" + fileName + "};" +
                    "Remove-PSSession $s\"";
            ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 60, true);
            output = new String(out.toByteArray(), Charset.defaultCharset());

            if ( ! output.matches(".*Connecting to remote server.*failed.*") &&
                    ! output.matches(".*The network name.*") &&
                    ! output.matches(".*Exception calling .*WriteAllBytes.*with.*") &&
                    ! output.matches(".*the server name cannot be resolved.*")) {
                break;
            }

            Log.warn("Connectivity issues detected. Going to retry script transfer in 60 seconds");
            StepCore.sleep(60);
            i++;
        }

        //remove temp dir because it is not needed any more
        FileCore.removeFile(dir);
    }


    /**
     * Uploads a file to the remote host.<br>
     * File will be uploaded to the home directory of the specified user, for example C:\\Users\\superuser
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param item String, path to the file to be uploaded on the local host, for example C:\\Users\\superuser\\remoteFile.txt
     */
    public void uploadFile(String node, String item) {

        File workingDir = FileCore.getTempDir();

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        Integer port = Storage.get("Environment.Active.WinRM." + node + ".port");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String domain = Storage.get("Environment.Active.WinRM." + node + ".domain");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }
        if ( port == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".port not found or null!");
        }
        if ( user == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".user not found or null!");
        }
        if ( domain == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".domain not found or null!");
        }
        if ( passwd == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".password not found or null!");
        }

        /*
        //turn off windows firewall on SUT for domain networks
        Log.debug("Turning off windows firewall for domain networks on remote host " + node);
        String cmd = "Set-NetFirewallProfile -Profile Domain -Enabled False";
        executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
        */

        /*
        Log.debug("Turn on Files and Printer sharing firewall rule");
        String cmd = "Set-NetFirewallRule -Name FPS-SMB-In-TCP -Enabled True";
        executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
        */

        //extract file name to upload
        int idx = item.lastIndexOf(File.separator);
        String fileName = item.substring(idx+1,item.length()).trim();

        /*
        //create target path
        String target = "\\\\" + address + File.separator + getUserDir(node).replaceFirst(":","\\$") + File.separator + fileName;
        */

        String userDir = getUserDir(node);
        String letter = Storage.get("TestData.MountPoint2");

        //using SMB protocol shall work as long as user has admin rights
        //
        String cmd = "cmd.exe /c \"Powershell.exe $U='" + domain + "\\" + user + "';" +
                "$P='" + passwd + "';" +
                "$pw=ConvertTo-SecureString $P -AsPlainText -Force;" +
                "$cred=New-Object Management.Automation.PSCredential($U, $pw);" +
                "New-PSDrive -name " + letter.replace(":","") + " -Root \\\\" + address +"\\" + userDir.replaceFirst(":","\\$") +
                " -Credential $cred -PSProvider filesystem;" +
                "Copy-Item " + item + " " + letter + "\\" + fileName + "\"";
        ExecutorCore.execute(cmd, workingDir, 60, true);

        //verify that file copy was successful
        String path = userDir + File.separator + fileName;
        Boolean exists = checkThatFileExists(node, path);

        if ( ! exists ){
            Log.error("File " + path + " does not exists on host " + address + "!");
        }

    }


    /**
     * Downloads a file from the remote host and returns path to it on a local host.<br>
     * File will be downloaded to the system temporary directory returned by method FileCore.getTempDir()<br>
     * Wait time in seconds can be used to await for a file before it will be available on a remote host
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param item String, path to the file to be uploaded on the local host, for example C:\\Users\\superuser\\remoteFile.txt
     * @param wait Integer, timeout used to await for a file to be available
     *
     * @return String
     */
    public String downloadFile(String node, String item, Integer wait){

        File workingDir = FileCore.createTempDir();

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        Integer port = Storage.get("Environment.Active.WinRM." + node + ".port");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String domain = Storage.get("Environment.Active.WinRM." + node + ".domain");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }
        if ( port == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".port not found or null!");
        }
        if ( user == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".user not found or null!");
        }
        if ( domain == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".domain not found or null!");
        }
        if ( passwd == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".password not found or null!");
        }

        if ( wait > 0 ){
            StepCore.sleep(wait);
        }

        /*
        //turn off windows firewall on SUT for domain networks
        Log.debug("Turning off windows firewall for domain networks on remote host " + node);
        String cmd = "Set-NetFirewallProfile -Profile Domain -Enabled False";
        executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
        */

        /*
        Log.debug("Turn on Files and Printer sharing firewall rule");
        String cmd = "Set-NetFirewallRule -Name FPS-SMB-In-TCP -Enabled True";
        executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
        */

        //extract file name to download
        int idx = item.lastIndexOf(File.separator);
        String fileName = item.substring(idx+1,item.length()).trim();

        //create target and source path
        String target = workingDir + File.separator + fileName;
        String source = "\\\\" + address + File.separator + item.replaceFirst(":","\\$");

        String letter = Storage.get("TestData.MountPoint2");

        //using SMB protocol shall work as long as user has admin rights
        //
        String cmd = "cmd.exe /c \"Powershell.exe $U='" + domain + "\\" + user + "';" +
                "$P='" + passwd + "';" +
                "$pw=ConvertTo-SecureString $P -AsPlainText -Force;" +
                "$cred=New-Object Management.Automation.PSCredential($U, $pw);" +
                "New-PSDrive -name " + letter.replace(":","") + " -Root " + source.replace(File.separator + fileName,"").replaceFirst(":","\\$") +
                " -Credential $cred -PSProvider filesystem;" +
                "Copy-Item " + letter + "\\" + fileName + " " + target + "\"";
        ExecutorCore.execute(cmd, workingDir, 60, true);

        //verify that file exists
        File file = new File(target);
        if ( ! file.exists() ){
            Log.error("File " + source + " copy from remote node " + node + " to " + target + " was not successful!");
        } else {
            Log.debug("File copied to " + target);
        }

        //return path to the destination
        return target;

    }


    /**
     * Checks if file on a remote host contains particular string
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param path String, path to text file
     * @param lookUpString String, string to be checked
     *
     * @return Boolean
     */
    public Boolean fileContains(String node, String path, String lookUpString){

        String cmd = "gc " + path + " | Where-Object {$_.contains('" + lookUpString + "')}";
        String results = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

        if ( ! results.contains(lookUpString) ){
            return false;
        }

        return true;
    }


    /**
     * Returns line of a file from remote host if it contains particular string
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param path String, path to text file
     * @param lookUpString String, string to be checked
     *
     * @return String
     */
    public String findInFile(String node, String path, String lookUpString){

        String cmd = "gc " + path + " | Where-Object {$_.contains('" + lookUpString + "')}";
        String results = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

        return results;
    }


    /**
     * Checks if particular file is locked on a remote host
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param path String, path to text file
     *
     * @return Boolean
     */
    public Boolean checkThatFileIsLocked (String node, String path){

        Boolean isLocked = true;

        String cmd = "$file=New-Object System.IO.FileInfo '" + path + "';" +
                "$stream=$file.Open([System.IO.FileMode]::Open, " +
                "[System.IO.FileAccess]::ReadWrite, " +
                "[System.IO.FileShare]::None);" +
                "if($stream){$stream.Close();Write-Host 'Yupi file not locked'}";
        String results = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

        if (results.contains("Yupi file not locked")){
            isLocked = false;
        }

        return isLocked;

    }


    /**
     * Runs script (batch file or powershell script) locally on a remote host as a scheduled task.
     * Returns processId of a scheduled task which can be used ofr supervision purposes
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param pathToScript String, path to script
     *
     * @return String
     */
    public String runScriptAsScheduledTask (String node, String pathToScript){

        String domain = Storage.get("Environment.Active.WinRM." + node + ".domain");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String password = Storage.get("Environment.Active.WinRM." + node + ".password");

        if ( domain == null ){
            Log.error("Environment.Active.WinRM." + node + ".domain not set or null");
        }
        if ( user == null ){
            Log.error("Environment.Active.WinRM." + node + ".user not set or null");
        }
        if ( password == null ){
            Log.error("Environment.Active.WinRM." + node + ".password not set or null");
        }

        String userDir = getUserDir(node);
        String stdErr = userDir + "\\" + "tempTask.stdErr";
        String stdOut = userDir + "\\" + "tempTask.stdOut";

        Log.debug("Removing old log files");
        Boolean isFilePresent = checkThatFileExists(node, userDir + "\\" + "tempTask.stdErr");
        if ( isFilePresent ){
            executeSingleCommandOnVM("\"Powershell.exe Remove-Item " + userDir + "\\" + "tempTask.stdErr\"", node, 120);
        }
        isFilePresent = checkThatFileExists(node, userDir + "\\" + "tempTask.stdOut");
        if ( isFilePresent ){
            executeSingleCommandOnVM("\"Powershell.exe Remove-Item " + userDir + "\\" + "tempTask.stdOut\"", node, 120);
        }

        Log.debug("Creating new scheduled task TempAutomationTask. " +
                "Assigning normal priority to it. " +
                "Recreating task with priority");

        //this script is used to run the original one and redirect outputs to file
        //it will be called from a scheduled task and in turn it will call the original one:)
        String script = "wrapperTask.ps1";
        String cmd = "Start-process powershell -NoNewWindow -Wait" +
                " -RedirectStandardError " + stdErr +
                " -RedirectStandardOutput " + stdOut +
                " -ArgumentList \" -ExecutionPolicy Bypass" +
                " -Command " + pathToScript + "\"";
        transferScript(node, cmd,script);

        //this script is used to create scheduled task
        script = "tempTask.ps1";
        List<String> cmdList = new ArrayList();
        cmdList.add("schtasks /CREATE /TN 'TempAutomationTask' /SC MONTHLY /RL HIGHEST " +
                "/RU \"" + domain + "\\" + user + "\" /IT " +
                "/RP \"" + password + "\" " +
                "/TR \"powershell -NoProfile -ExecutionPolicy Bypass -File " + userDir + "\\wrapperTask.ps1\" " +
                "/F;");
        cmdList.add("$taskFile = \"" + userDir + "\\RemoteTask.txt\";");
        cmdList.add("Remove-Item $taskFile -Force -ErrorAction SilentlyContinue;");
        cmdList.add("[xml]$xml = schtasks /QUERY /TN 'TempAutomationTask' /XML;");
        cmdList.add("$xml.Task.Settings.Priority=\"4\";");
        cmdList.add("$xml.Save($taskFile);");
        cmdList.add("schtasks /CREATE /TN 'TempAutomationTask' /RU \"" + domain + "\\" + user + "\" " +
                "/IT /RP \"" + password + "\" " +
                "/XML $taskFile /F;");

        cmd = joinCommands(cmdList, "\r\n", true);
        transferScript(node, cmd, script);

        String result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
        if ( ! result.contains("SUCCESS:") ){
            Log.error("Failed to create new scheduled task TempAutomationTask");
        } else {
            Log.debug("New scheduled task TempAutomationTask created");
        }

        Log.debug("Running scheduled task");
        cmd = "schtasks /RUN /I /TN 'TempAutomationTask';";
        result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
        if ( ! result.contains("SUCCESS:") ){
            Log.error("Failed to run scheduled task TempAutomationTask");
        }

        String processId = "";
        Integer i = 0;
        while( i < 11) {
            Log.debug("Extracting process id of a scheduled task TempAutomationTask");
            String tmp = pathToScript.replaceAll("\\\\", "\\\\\\\\");
            cmd = "$proc = Get-CimInstance Win32_Process | Where {$_.CommandLine -match 'Command " + tmp + "'} | Select Caption, CommandLine, ProcessId;" +
                    "Write-Host $proc.ProcessId, $proc.CommandLine -Separator ',';";
            result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
            processId = "";
            if (result.contains("-Command " + pathToScript)) {
                String[] t_processId = result.split(",");
                processId = t_processId[0];
                break;
            }
            StepCore.sleep(2);
            i++;
        }

        if ( i == 11 ){

            Log.warn("Probably an error happen during script execution. Printing stdErr output");
            executeSingleCommandOnVM("type " + stdErr, node, 60);
            Log.warn("Probably an error happen during script execution. Printing stdOut output");
            executeSingleCommandOnVM("type " + stdOut, node, 60);

            Log.warn("Killing scheduled task TempAutomationTask due to an error in script execution");
            script = "tempTask.ps1";
            cmd = "schtasks /END /TN 'TempAutomationTask'";
            transferScript(node, cmd, script);

            result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            if ( ! result.contains("SUCCESS:") ){
                Log.warn("Failed to kill scheduled task TempAutomationTask");
            }

            Log.warn("Deleting scheduled task TempAutomationTask due to an error in script execution");
            cmd = "schtasks /DELETE /TN 'TempAutomationTask' /F";
            transferScript(node, cmd, script);

            result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            if ( ! result.contains("SUCCESS:") ){
                Log.warn("Failed to remove scheduled task TempAutomationTask");
            }

            Log.error("Task process not found! Script didn't run. Please check its content. " +
                    "Maybe an error happen during execution?");
        }

        return processId;

    }


    /**
     * Minimizes all open windows on a remote windows host by calling powershell script as a scheduled task
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param taskName String, name that shall be assigned to the task
     */
    public void minimizeAllWindows (String node, String taskName){

        String domain = Storage.get("Environment.Active.WinRM." + node + ".domain");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String password = Storage.get("Environment.Active.WinRM." + node + ".password");

        if ( domain == null ){
            Log.error("Environment.Active.WinRM." + node + ".domain not set or null");
        }
        if ( user == null ){
            Log.error("Environment.Active.WinRM." + node + ".user not set or null");
        }
        if ( password == null ){
            Log.error("Environment.Active.WinRM." + node + ".password not set or null");
        }

        String userDir = getUserDir(node);

        Log.debug("Minimizing all open windows");

        String script = "temp_minimize_windows.ps1";

        List<String> cmdList = new ArrayList();
        cmdList.add("$shell = New-Object -ComObject \"Shell.Application\";");
        cmdList.add("$shell.MinimizeAll()");

        String cmd = joinCommands(cmdList, "\r\n", true);
        transferScript(node, cmd, script);

        String pathToScript = userDir + "\\" + script;

        Log.debug("Creating new scheduled task " + taskName +
                "Assigning normal priority to it. " +
                "Recreating task with priority");

        //this script is used to run the original one
        //it will be called from a scheduled task and in turn it will call the original one:)
        script = "wrapperTask.ps1";

        cmd = "Start-process powershell -NoNewWindow -Wait" +
                " -ArgumentList \"-NoProfile -ExecutionPolicy Bypass" +
                " -Command " + pathToScript + "\"";
        transferScript(node, cmd, script);

        //this script is used to create scheduled task
        script = "tempTask.ps1";

        cmdList = new ArrayList();
        cmdList.add("schtasks /CREATE /TN '" + taskName + "' /SC MONTHLY /RL HIGHEST " +
                "/RU \"" + domain + "\\" + user + "\" /IT " +
                "/RP \"" + password + "\" " +
                "/TR \"powershell -NoProfile -ExecutionPolicy Bypass -File " + userDir + "\\wrapperTask.ps1\" " +
                "/F;");
        cmdList.add("$taskFile = \"" + userDir + "\\RemoteTask.txt\";");
        cmdList.add("Remove-Item $taskFile -Force -ErrorAction SilentlyContinue;");
        cmdList.add("[xml]$xml = schtasks /QUERY /TN '" + taskName + "' /XML;");
        cmdList.add("$xml.Task.Settings.Priority=\"4\";");
        cmdList.add("$xml.Save($taskFile);");
        cmdList.add("schtasks /CREATE /TN '" + taskName + "' /RU \"" + domain + "\\" + user + "\" " +
                "/IT /RP \"" + password + "\" " +
                "/XML $taskFile /F;");

        cmd = joinCommands(cmdList, "\r\n", true);
        transferScript(node, cmd, script);

        String result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
        if ( ! result.contains("SUCCESS:") ){
            Log.error("Failed to create new scheduled task TempAutomationTask");
        } else {
            Log.debug("New scheduled task TempAutomationTask created");
        }

        Log.debug("Running scheduled task");
        cmd = "schtasks /RUN /I /TN '" + taskName + "';";
        result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
        if ( ! result.contains("SUCCESS:") ){
            Log.error("Failed to run scheduled task TempAutomationTask");
        }

        StepCore.sleep(5);

        Log.warn("Deleting scheduled task " + taskName);
        cmd = "schtasks /DELETE /TN '" + taskName + "' /F;";
        transferScript(node, cmd, script);

        result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
        if ( ! result.contains("SUCCESS:") ){
            Log.warn("Failed to remove scheduled task " + taskName);
        }

    }


    /**
     * Supervises scheduled task execution on a remote host.<br>
     * Error is indicated in the log if timeout will be reached.
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param processId String, processId of a scheduled task to be supervised
     * @param timeout Integer, timeout
     *
     * @return Boolean
     */
    public Boolean superviseScheduledTaskExecution(String node, String processId, Integer timeout){

        if (timeout < 0) {

            Log.warn("Killing scheduled task TempAutomationTask due to timeout");
            String script = "tempTask.ps1";
            String cmd = "schtasks /END /TN 'TempAutomationTask';";
            transferScript(node, cmd, script);

            String result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            if ( ! result.contains("SUCCESS:") ){
                Log.warn("Failed to kill scheduled task TempAutomationTask");
            }

            Log.warn("Deleting scheduled task TempAutomationTask due to timeout");
            cmd = "schtasks /DELETE /TN 'TempAutomationTask' /F;";
            transferScript(node, cmd, script);

            result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            if ( ! result.contains("SUCCESS:") ){
                Log.warn("Failed to remove scheduled task TempAutomationTask");
            }

            Log.error("Timeout reached for scheduled task execution supervision!");
        }

        String cmd = "Get-Process -Id " + processId;
        String result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

        if ( result.contains("Cannot find a process") ){

            Log.debug("Removing scheduled task TempAutomationTask");

            String script = "tempTask.ps1";
            cmd = "schtasks /DELETE /TN 'TempAutomationTask' /F;";
            transferScript(node, cmd, script);

            result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            if ( ! result.contains("SUCCESS:") ){
                Log.error("Failed to remove scheduled task TempAutomationTask");
            }

            return true;
        }

        StepCore.sleep(60);
        return superviseScheduledTaskExecution(node, processId, timeout - 60);
    }


    /**
     * Uploads winium driver and starts it on a remote windows host.<br>
     * Winium driver can be found in users home dir.<br>
     * Firewall settings are modified to allow for a traffic on port Environment.Active.WebDrivers.WiniumDesktop.port<br>
     * ServerManger will be closed if it is open on a remote host<br>
     * Returns processId of a driver process started on a remote host
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     *
     * @return String
     */
    public String uploadAndStartWiniumDriver(String node){

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String domain = Storage.get("Environment.Active.WinRM." + node + ".domain");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");

        if (address == null) {
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }
        if (user == null) {
            Log.error("Configuration for Environment.Active.WinRM." + node + ".user not found or null!");
        }
        if (domain == null) {
            Log.error("Configuration for Environment.Active.WinRM." + node + ".domain not found or null!");
        }
        if (passwd == null) {
            Log.error("Configuration for Environment.Active.WinRM." + node + ".password not found or null!");
        }

        String userDir = getUserDir(node);

        Boolean isAvailable = checkThatFileExists(node, userDir + "\\Winium.Desktop.Driver.exe");
        if ( ! isAvailable ) {
            Log.debug("Uploading file");
            //String item = FileCore.getProjectPath() + File.separator + "resources" + File.separator +
            //        "webDrivers" + File.separator + "Winium.Desktop.Driver.exe";
            String item = FileCore.getProjectPath() + File.separator + Storage.get("Environment.Active.WebDrivers.WiniumDesktop.path");
            uploadFile(node, item);

            /*
            Log.debug("Downloading Winium.Desktop.Driver.zip file");
            String cmd = "Invoke-WebRequest -Uri \"https://github.com/2gis/Winium.Desktop/releases/download/v1.6.0/Winium.Desktop.Driver.zip\" -OutFile \"Winium.Desktop.Driver.zip\"";
            RemoteExecution.executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 300);

            Log.debug("Extracting it");
            RemoteExecution.executeSingleCommandOnVM("unzip -e -o Winium.zip", node, 60);
            */

            }
        String rdpProcessId = openRdpSession(node);
        String port = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.port");

        String cmd = "Get-CimInstance Win32_Process | Where {$_.name -match '.*Winium.*'} | Select Caption, CommandLine, ProcessId | Format-list";
        String result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

        if ( ! result.contains("Winium.Desktop.Driver.exe")) {

            /*
            Log.debug("Opening firewall port " + port + " for domain profile on node " + node);
            cmd = "New-NetFirewallRule -DisplayName 'HTTP(S) Inbound' -Profile Domain -Direction Inbound" +
                    " -Action Allow -Protocol TCP -LocalPort '" + port + "';";
            executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
            cmd = "New-NetFirewallRule -DisplayName 'HTTP(S) Outbound' -Profile Domain -Direction Outbound" +
                    " -Action Allow -Protocol TCP -LocalPort '" + port + "';";
            executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
            */

            Log.debug("Running WiniumDriver on node " + node);
            String script = "temp.bat";
            cmd = "call " + userDir + "\\Winium.Desktop.Driver.exe --port " + port;
            transferScript(node, cmd, script);

            String pathToScript = userDir + "\\" + script;
            runScriptAsScheduledTask(node, pathToScript);

            StepCore.sleep(5);

        }

        return rdpProcessId;

    }


    /**
     * Joins commands from a List and creates a String out of it
     *
     * @param commands List, list of commands
     * @param delim String, delimiter used for join, for example \n\r
     * @param endWithDelim Boolean, indicates if delimiter shall be added at the end of the output String
     *
     * @return String
     */
    public String joinCommands(List<String> commands, String delim, boolean endWithDelim) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String command : commands){
            if (first){
                first = false;
            } else {
                builder.append(delim);
            }
            builder.append(command);
        }
        if (endWithDelim){
            builder.append(delim);
        }

        return builder.toString();
    }


    /**
     * Opens remote desktop session towards remote host<br>
     * Returns processId of a RDP session
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     *
     * @return String
     */
    public String openRdpSession(String node){
        File workingDir = FileCore.getTempDir();

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String domain = Storage.get("Environment.Active.WinRM." + node + ".domain");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");

        if (address == null) {
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }
        if (user == null) {
            Log.error("Configuration for Environment.Active.WinRM." + node + ".user not found or null!");
        }
        if (domain == null) {
            Log.error("Configuration for Environment.Active.WinRM." + node + ".domain not found or null!");
        }
        if (passwd == null) {
            Log.error("Configuration for Environment.Active.WinRM." + node + ".password not found or null!");
        }

        Log.debug("Checking if there is RDP session open towards node " + address);

        String cmd = "powershell.exe \"Get-CimInstance Win32_Process | Where {$_.name -match '.*mstsc.*'" +
                " -and $_.CommandLine -match '.*" + address + ".*'} | Select ProcessId | Format-list\"";
        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 10, true);
        String output = new String(out.toByteArray(), Charset.defaultCharset());
        String rdpProcessId = "";
        rdpProcessId = output.trim().replace("ProcessId :","").trim();
        if ( ! rdpProcessId.equals("") ) {
            Log.debug("Rdp session is open towards " + address + " with process id " + rdpProcessId);

            killRdpSession(rdpProcessId);
        }

        //add certificate hash to the registry so we can avoid any popUp with invalid cert confirmation
        Log.debug("Checking cert hash for node " + address + " in windows registry");
        cmd = "wmic /node:" + address + " /namespace:\\\\root\\CIMV2\\TerminalServices PATH Win32_TSGeneralSetting get SSLCertificateSHA1Hash";
        out = ExecutorCore.execute(cmd, workingDir, 10, true);
        output = new String(out.toByteArray(), Charset.defaultCharset());
        String hashCode = output.trim().replaceAll("SSLCertificateSHA1Hash","").trim();

        Log.debug("Node's " + address + " cert hashCode is " + hashCode);
        if (! hashCode.equals("") ) {
            Log.debug("Adding cert hash to the registry");
            cmd = "REG ADD \"HKEY_CURRENT_USER\\Software\\Microsoft\\Terminal Server Client\\Servers\\" + address + "\" /v \"CertHash\" /t REG_BINARY /d " + hashCode + " /f";
            ExecutorCore.execute(cmd, workingDir, 10, true);
        }

        //switch off scaling so we do not run into issues when winium is started
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

        String script = "temp.ps1";
        cmd = joinCommands(cmds,"\r\n",false);
        transferScript(node, cmd, script);
        executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);

        //force log off so new settings will ba applicable to the next session
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

        script = "temp.ps1";
        cmd = joinCommands(cmds,"\r\n",false);
        transferScript(node, cmd, script);
        executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);

        Log.debug("Opening Rdp session to " + node);
        cmd = "powershell.exe \"$Server='" + address + "';$User='" + domain + "\\" + user + "';" +
                "$Pass='" + passwd + "';cmdkey /generic:$Server /user:$User /pass:$Pass;mstsc /v:$Server\"";
        ExecutorCore.execute(cmd, workingDir, 10, false);

        Log.debug("Extracting process id of RDP session");

        cmd = "powershell.exe \"Get-CimInstance Win32_Process | Where {$_.name -match '.*mstsc.*'" +
                " -and $_.CommandLine -match '.*" + address + ".*'} | Select ProcessId | Format-list\"";
        out = ExecutorCore.execute(cmd, workingDir, 10, true);
        output = new String(out.toByteArray(), Charset.defaultCharset());
        rdpProcessId = output.trim().replace("ProcessId :","").trim();
        Log.debug("Rdp session process id is " + rdpProcessId);

        //RDP needs few seconds to establish a desktop connection
        StepCore.sleep(10);

        return rdpProcessId;
    }


    /**
     * Kills RDP session
     *
     * @param rdpProcessId String, processId of an open RDP session
     */
    public void killRdpSession(String rdpProcessId){
        Log.debug("Killing RDP session with process id  " + rdpProcessId);

        File workingDir = FileCore.getTempDir();

        String cmd = "powershell.exe \"Stop-process -Id " + rdpProcessId + " -Force\"";
        ExecutorCore.execute(cmd, workingDir, 10, true);
    }


    /**
     * Stops service on a remote windows host and returns true if service is not longer running
     * An error will be printed if service will not be stopped within a defined timeout
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param serviceName String, name of the service
     * @param timeout Integer, timeout in seconds
     *
     * @return Boolean
     */
    public Boolean stopService(String node, String serviceName, Integer timeout){
        Log.debug("Stopping " + serviceName + " service");

        Boolean output = false;

        String cmd = "Stop-Service " + serviceName;
        String result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, timeout);
        if ( result.contains("Cannot find any service with service name")){
            Log.debug(serviceName + " service not running");
            output = true;
        } else {
            cmd = "Get-Service " + serviceName;
            result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 300);

            if ( result.contains("StopPending") ){
                Log.warn("Service was not shutdown. Hanging state detected! Going to kill it");

                cmd = "sc queryex " + serviceName;
                String subResult = executeSingleCommandOnVM(cmd, node, 120);
                String[] lines = subResult.split(System.getProperty("line.separator"));
                String pid = "";
                for ( String line : lines) {
                    if ( line.contains("PID") ) {
                        Integer idx = line.indexOf(":");
                        pid = line.substring(idx+1).trim();
                        Log.debug("Service PID is " + pid);
                        break;
                    }
                }

                if ( ! pid.equals("") ) {
                    cmd = "taskkill /PID " + pid + " /F";
                    executeSingleCommandOnVM(cmd, node, 120);
                }

                cmd = "Get-Service " + serviceName + " | select status | format-list";
                result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
            }


            if (result.contains("Stopped")) {
                Log.debug(serviceName + " was successfully stopped");
                output = true;
            } else {
                Log.warn(serviceName + " couldn't be stopped!");
            }
        }

        return output;
    }


    /**
     * Starts service on a remote windows host and returns true if service is running
     * An error will be printed if service will not be started within a defined timeout
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param serviceName String, name of the service
     * @param timeout Integer, timeout in seconds
     *
     * @return Boolean
     */
    public Boolean startService(String node, String serviceName, Integer timeout){
        Log.debug("Starting " + serviceName + " service");

        Boolean output = false;
        String cmd = "Start-Service " + serviceName;
        String result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, timeout);
        if ( result.contains("Cannot find any service with service name")){
            Log.debug(serviceName + " service not running");
        } else {
            cmd = "Get-Service " + serviceName;
            result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 300);
            if (result.contains("Running")) {
                Log.debug(serviceName + " service was successfully started");
                output = true;
            } else {
                Log.warn(serviceName + " service couldn't be started!");
            }
        }

        return output;
    }


}