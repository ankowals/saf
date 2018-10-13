package libs.libCore.modules;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.winium.DesktopOptions;
import org.openqa.selenium.winium.WiniumDriver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class WinRSCore {

    private Context scenarioCtx;
    private Context globalCtx;
    private FileCore FileCore;
    private StepCore StepCore;
    private ExecutorCore ExecutorCore;
    private Storage Storage;
    private CloudDirectorCore CloudDirectorCore;

    public WinRSCore() {
        this.scenarioCtx = ThreadContext.getContext("Scenario");
        this.globalCtx = ThreadContext.getContext("Global");
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.ExecutorCore = scenarioCtx.get("ExecutorCore",ExecutorCore.class);
        this.Storage = scenarioCtx.get("Storage",Storage.class);
        this.StepCore = scenarioCtx.get("StepCore",StepCore.class);
        this.CloudDirectorCore = scenarioCtx.get("CloudDirectorCore",CloudDirectorCore.class);
    }

    /**
     * Verifies connection details to be used by WinRS client. Checks if all are defined and not null
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @return Map, Verified properties of a connection
     */
    private Map<String, String> verifyConnectionDetails(String node){

        //check if connection details were already verified
        Map<String, String> connectionDetails = globalCtx.get("WinRsConnectionDetails_" + node, Map.class);
        if ( connectionDetails != null ){
            return connectionDetails;
        }

        connectionDetails = new HashMap();

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

        connectionDetails.put("address", address);
        connectionDetails.put("port", Integer.toString(port));
        connectionDetails.put("user", user);
        connectionDetails.put("domain", domain);
        connectionDetails.put("password", passwd);

        //set global variable per node
        globalCtx.put("WinRsConnectionDetails_" + node, Map.class, connectionDetails);

        return connectionDetails;

    }


    /**
     * Retrieves ip address of a host using DNS query
     *
     * @param name String, FQDN of a remote host
     * @return String, ip version 4 of a remote host
     */
    private String getIpOfHost(String name){
        Log.debug("Checking ip address of host " + name);

        //validate if name is an ip address if so no need to do anything else
        String ipAddressPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        Pattern pattern = Pattern.compile(ipAddressPattern);
        Matcher matcher = pattern.matcher(name);

        if ( matcher.matches() ){
            Log.warn("An ip address instead of FQDN was provided!");
            addToTrustedHosts(name);

            return name;
        }

        Boolean isConnectViaIp = Storage.get("Environment.Active.WinRS.connectUsingIpAddress");
        if ( ! isConnectViaIp ){
            Boolean isConnectViaHostName = Storage.get("Environment.Active.WinRS.connectUsingHostName");
            if ( isConnectViaHostName ){
                String tmp[] = name.split("[.]");
                name = tmp[0];
            }
            addToTrustedHosts(name);

            return name;
        }

        //check if ip is already known
        String ip = globalCtx.get("IpAddressOfHostWithName_" + name, String.class);

        if ( ip != null ){
            Log.debug("Ip address of host " + name + " is " + ip);
            //if ip is known host name was already added to trusted host list so there is no need to do it again
            return ip;
        }

        File workingDir = FileCore.getTempDir();
        ByteArrayOutputStream out = ExecutorCore.execute("Powershell.exe \"$ip = (Resolve-DnsName '" + name + "').IPAddress; Write-Host $ip\"" , workingDir, 60, true);
        String result = new String(out.toByteArray(), Charset.defaultCharset());

        if ( result.contains("DNS name does not exist") ){
            Log.warn("Can't resolve fqdn to an ip address");
            result = "";
        }


        ip = result.trim();
        addToTrustedHosts(ip);

        //set global variable per node
        globalCtx.put("IpAddressOfHostWithName_" + name, String.class, ip);

        return ip;

    }


    /**
     * Adds host to a trusted hosts list
     *
     * @param name String, name of the host to be added to trusted hosts list
     */
    public void addToTrustedHosts(String name){
        Log.debug("Adding host " + name + " to trusted hosts list");

        //check if host was already added to trusted hosts list else add it
        Boolean isHostTrusted = globalCtx.get("TrustedHost_" + name, Boolean.class);

        if ( isHostTrusted == null){
            isHostTrusted = false;
        }

        if ( isHostTrusted ) {
            Log.debug("Host was already added to trust host list");
            return;
        }

        File workingDir = FileCore.getTempDir();

        String runQuickConf = "Enable-PSRemoting -force;";
        String addToTrustedHots = "$curVal=(Get-Item WSMan:\\localhost\\Client\\TrustedHosts).Value;if($curVal -notlike '*" + name + "*'){Set-Item WSMan:\\localhost\\Client\\TrustedHosts -Value \"$curVal, " + name + "\" -Force};";
        String checkClient = "$curVal=(Get-Item WSMan:\\localhost\\Client\\TrustedHosts).Value; Write-Host \"TrustedHosts: $curVal\";";

        String cmd = "If (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] \"Administrator\"))" +
                "{$arguments = \"& '\" + $myinvocation.mycommand.definition + \"'\";Start-Process powershell -WindowStyle Hidden -Verb runAs -ArgumentList $arguments;Break};" +
                runQuickConf + addToTrustedHots + checkClient;

        File script = FileCore.createTempFile("addTrustedHost","ps1");
        FileCore.writeToFile(script, cmd);
        ExecutorCore.execute("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File '" + script.getAbsolutePath() + "'", workingDir, 60, true);
        FileCore.removeFile(script);

        //set global variable per node
        globalCtx.put("TrustedHost_" + name, Boolean.class, true);

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

        Map<String, String> conn = verifyConnectionDetails(node);

        String address = conn.get("address");
        String port = conn.get("port");
        String domain = conn.get("domain");
        String user = conn.get("user");
        String passwd = conn.get("password");

        address = getIpOfHost(address);

        String invocation = "winrs -r:http://" + address + ":" + port + " -u:" + domain + "\\" + user + " -p:" + passwd;
        cmd =  invocation + " " + cmd;

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, timeout, true);
        return new String(out.toByteArray(), Charset.defaultCharset());
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
        return executeSingleCommandOnVM("call " + script, node, timeout);
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

        String dbUser = Storage.get("TestData.MsSql.User");
        String dbpass = Storage.get("TestData.MsSql.Pass");

        Map<String, String> conn = verifyConnectionDetails(node);
        String address = conn.get("address");

        address = getIpOfHost(address);

        String instanceName = Storage.get("TestData.MsSql.InstanceName");
        if ( ! instanceName.equals("MSSQLSERVER") ){
            address = address + "\\" + instanceName;
        }

        String cmd = joinCommands(cmdList, "\r\n", true);
        String script = "temp.sql";
        transferScript(node, cmd, script);
        return executeSingleCommandOnVM(" SQLCMD -b -S " + address + " -U " + dbUser + " -P " + dbpass + " -i " + script, node, timeout);
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

        Map<String, String> conn = verifyConnectionDetails(node);
        String address = conn.get("address");
        String port = conn.get("port");
        String domain = conn.get("domain");
        String user = conn.get("user");
        String passwd = conn.get("password");

        //check if user dir per node is known else retrieve it from node
        String userDir = globalCtx.get("userDir_" + node, String.class);

        if ( userDir == null || userDir.equals("") ) {
            Log.warn("userDir not known. Going to check it on remote host");

            address = getIpOfHost(address);

            String invocation = "winrs -r:http://" + address + ":" + port + " -u:" + domain + "\\" + user + " -p:" + passwd;
            ByteArrayOutputStream out = ExecutorCore.execute(invocation + " cd", workingDir, 120, true);
            String result = new String(out.toByteArray(), Charset.defaultCharset());

            String[] lines = result.split(System.getProperty("line.separator"));
            userDir = lines[0].trim();

            //set global variable per node
            globalCtx.put("userDir_" + node, String.class, userDir);

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

        address = getIpOfHost(address);

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
            String script = "adjustWinRmConfig.bat";
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

        Map<String, String> conn = verifyConnectionDetails(node);
        String address = conn.get("address");

        address = getIpOfHost(address);

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

            Boolean useCloudDirector = Storage.get("Environment.Active.Cloud.useCloudDirector");
            if (useCloudDirector) {
                Boolean isReboot = detectRebootViaCloudDirector(address);
                //force network disconnection via cloud director because RDP connection can't be set
                if (i == 20 && isReboot) {
                    reconnectNetworkToVmViaCloudDirector();
                }
                //force reboot via cloud director because winRM service does not want to start
                if (i == 40 && isReboot) {
                    rebootVmViaCloudDirector();
                }
            }
        }
    }


    /**
     * Checks if reboot via cloudDirector api can be done. Useful in case of connectivity issues.
     *
     * @param address String, FQDN of remote host
     * @return Boolean
     */
    private Boolean detectRebootViaCloudDirector(String address){

        //validate if address is fqdn or ip
        String ipAddressPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        Pattern pattern = Pattern.compile(ipAddressPattern);
        Matcher matcher = pattern.matcher(address);

        if ( ! address.contains(".") || matcher.matches() ){
            Log.warn("host address is not FQDN! Can't perform reboot via cloud director api");
            return false;
        }

        String sVmName = Storage.get("Environment.Active.Cloud.CloudDirector.VmNameForReboot");
        if ( sVmName != null ){
            if ( !sVmName.equals("") ){
                return true;
            }
        }

        String vMkey = "";
        //find config entity that contains VmApp key and check if host equals NewVmName key
        HashMap<String, Object> map = Storage.get("TestData");
        for ( HashMap.Entry<String, Object> entry : map.entrySet() ) {
            if ( entry.getValue() instanceof HashMap) {
                if ( ((HashMap) entry.getValue()).containsKey("VApp") ) {
                    vMkey = entry.getKey();
                    String name = ((HashMap) entry.getValue()).get("NewVmName").toString();
                    if ( name != null || (!name.equals("")) ) {
                        String[] tmp = address.split("[.]");
                        if ( name.equalsIgnoreCase(tmp[0]) ) {
                            Log.debug("Cloud configuration for Vm has been found. Rebooting");
                            Storage.set("Environment.Active.Cloud.CloudDirector.VmNameForReboot", vMkey);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    /**
     * Disconnects and connects back network via CloudDirector. Useful to restart connection in case of problems.
     */
    private void reconnectNetworkToVmViaCloudDirector(){
        Log.debug("Going to disconnect network connection");

        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vMkey = Storage.get("Environment.Active.Cloud.CloudDirector.VmNameForReboot");
        String vm_name = Storage.get("TestData." + vMkey + ".NewVmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + vMkey + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + vMkey + ".Vdc");

        if (vm_name == null) {
            Log.warn("Value of TestData." + vMkey + ".NewVmName null or empty!");
            return;
        }
        if (vapp == null) {
            Log.warn("Value of TestData." + vMkey + ".VApp null or empty!");
            return;
        }
        if (vdc == null) {
            Log.warn("Value of TestData." + vMkey + ".Vdc null or empty!");
            return;
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.disconnectNetworkConnection(newVmUrl);
        CloudDirectorCore.logout();

        Log.debug("Going to connect network connection back");
        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        vAppUrl = CloudDirectorCore.getVApp(vapp);
        newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.connectNetworkConnection(newVmUrl);
        CloudDirectorCore.logout();
    }


    /**
     * Reboots remote host via CLoudDirector api in case connection via WinRM is not possible.
     */
    private void rebootVmViaCloudDirector(){

        Log.debug("Going to reboot host via cloudDirector api");

        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vMkey = Storage.get("Environment.Active.Cloud.CloudDirector.VmNameForReboot");
        String vm_name = Storage.get("TestData." + vMkey + ".NewVmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + vMkey + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + vMkey + ".Vdc");

        if (vm_name == null) {
            Log.warn("Value of TestData." + vMkey + ".NewVmName null or empty!");
            return;
        }
        if (vapp == null) {
            Log.warn("Value of TestData." + vMkey + ".VApp null or empty!");
            return;
        }
        if (vdc == null) {
            Log.warn("Value of TestData." + vMkey + ".Vdc null or empty!");
            return;
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.rebootVm(newVmUrl);
        CloudDirectorCore.logout();
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

        address = getIpOfHost(address);

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

            if (result.contains("TcpTestSucceeded:True")) {
                break;
            }

            StepCore.sleep(60);
            i++;

            if (i == 55) {
                Log.error("RDP on host not accessible! Timeout of 55 minutes reached!");
            }

            Boolean useCloudDirector = Storage.get("Environment.Active.Cloud.useCloudDirector");
            if (useCloudDirector) {
                Boolean isReboot = detectRebootViaCloudDirector(address);
                //force network disconnection via cloud director because RDP connection can't be set
                if (i == 20 && isReboot) {
                    reconnectNetworkToVmViaCloudDirector();
                }
                //force reboot via cloud director because winRM service does not want to start
                if (i == 40 && isReboot) {
                    rebootVmViaCloudDirector();
                }
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

        String result = executeSingleCommandOnVM("if exist '" + path + "' (echo exist)", node, 60);
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

        String script = "verifyThatFilesExist.ps1";
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

        Map<String, String> conn = verifyConnectionDetails(node);

        String address = conn.get("address");
        String domain = conn.get("domain");
        String user = conn.get("user");
        String passwd = conn.get("password");

        address = getIpOfHost(address);
        awaitForHostAvailability(node);

        //create file in temp dir
        File dir = FileCore.createTempDir();
        String dirPath = dir.getAbsolutePath();
        File file = new File(dirPath + File.separator + name);
        //write content to newly created file
        FileCore.writeToFile(file, content);
        String item = file.getAbsolutePath();

        //extract file name to upload
        String fileName = file.getName();

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

        Map<String, String> conn = verifyConnectionDetails(node);
        String address = conn.get("address");
        String domain = conn.get("domain");
        String user = conn.get("user");
        String passwd = conn.get("password");

        address = getIpOfHost(address);

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

        Map<String, String> conn = verifyConnectionDetails(node);
        String address = conn.get("address");
        String domain = conn.get("domain");
        String user = conn.get("user");
        String passwd = conn.get("password");

        address = getIpOfHost(address);

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
                "if($stream){$stream.Close();Write-Host 'File not locked'}";
        String results = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

        if (results.contains("File not locked")){
            isLocked = false;
        }

        return isLocked;

    }


    /**
     * Runs script (batch file or powershell script) locally on a remote host as a scheduled task.
     * Returns processId of a scheduled task which can be used for supervision purposes
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param pathToScript String, path to script
     *
     * @return String
     */
    public String runScriptAsScheduledTask (String node, String pathToScript){

        Map<String, String> conn = verifyConnectionDetails(node);
        String domain = conn.get("domain");
        String user = conn.get("user");
        String password = conn.get("password");

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
        String cmd = "Start-process powershell -WindowStyle Hidden -Wait" +
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
                "/TR \"powershell -windowstyle hidden -NoProfile -ExecutionPolicy Bypass -File " + userDir + "\\wrapperTask.ps1\" " +
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
        while( i < 2 ) {
            Log.debug("Extracting process id of a scheduled task TempAutomationTask");
            String tmp = pathToScript.replaceAll("\\\\", "\\\\\\\\");
            cmd = "$proc = Get-CimInstance Win32_Process | Where {$_.CommandLine -match 'Command " + tmp + "' -or $_.CommandLine -match ' & .*" + tmp + "'} | Select Caption, CommandLine, ProcessId;" +
                    "Write-Host $proc.ProcessId, $proc.CommandLine -Separator ',';";
            result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
            processId = "";
            if ( result.contains("-Command " + pathToScript) || result.contains(" & \'" + pathToScript) ) {
                String[] t_processId = result.split(",");
                processId = t_processId[0];
                break;
            }
            StepCore.sleep(2);
            i++;
        }

        //if script was executed immediately we may struggle to find a processId.
        //in this case if stdErr is empty we assume execution was successful and return empty processId
        //supervision shall not be used
        if ( processId.equals("")){
            Log.debug("Checking StdErr");
            String stdErrOutput = executeSingleCommandOnVM("type " + stdErr, node, 60);
            if ( stdErrOutput.equals("") ){
                Log.warn("Script execution didn't return any error but processId was not found! " +
                        "Going to return empty processId");
            }
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

        Map<String, String> conn = verifyConnectionDetails(node);
        String domain = conn.get("domain");
        String user = conn.get("user");
        String password = conn.get("password");

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
        script = "wrapperTaskmw.ps1";

        cmd = "Start-process powershell -WindowStyle Hidden -Wait" +
                " -ArgumentList \"-NoProfile -ExecutionPolicy Bypass" +
                " -Command " + pathToScript + "\"";
        transferScript(node, cmd, script);

        //this script is used to create scheduled task
        script = "tempTaskmw.ps1";

        cmdList = new ArrayList();
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

        if ( timeout < 0 || processId.equals("") ) {
            if ( timeout < 0 ) {
                Log.warn("Killing scheduled task TempAutomationTask due to timeout");
            } else {
                String userDir = getUserDir(node);
                String stdErr = userDir + "\\" + "tempTask.stdErr";
                String stdOut = userDir + "\\" + "tempTask.stdOut";

                Log.warn("Probably an error happen during script execution. Printing stdErr output");
                executeSingleCommandOnVM("type " + stdErr, node, 60);
                Log.warn("Probably an error happen during script execution. Printing stdOut output");
                executeSingleCommandOnVM("type " + stdOut, node, 60);
                Log.warn("Killing scheduled task TempAutomationTask");
            }

            String script = "tempTask.ps1";
            String cmd = "schtasks /END /TN 'TempAutomationTask';";
            transferScript(node, cmd, script);

            String result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            if ( ! result.contains("SUCCESS:") ){
                Log.warn("Failed to kill scheduled task TempAutomationTask");
            }

            Log.warn("Deleting scheduled task TempAutomationTask");
            cmd = "schtasks /DELETE /TN 'TempAutomationTask' /F;";
            transferScript(node, cmd, script);

            result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            if ( ! result.contains("SUCCESS:") ){
                Log.warn("Failed to remove scheduled task TempAutomationTask");
            }

            if ( timeout < 0 ) {
                Log.error("Timeout reached for scheduled task execution supervision!");
            } else {
                Log.error("Task process id not found! Script didn't run. Please check its content. " +
                        "Maybe an error happen during execution?");
            }
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
    private String uploadAndStartWiniumDriver(String node){

        Map<String, String> conn = verifyConnectionDetails(node);
        String address = conn.get("address");

        address = getIpOfHost(address);
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
            executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 300);

            Log.debug("Extracting it");
            executeSingleCommandOnVM("unzip -e -o Winium.zip", node, 60);
            */

            }

        String rdpProcessId = openRdpSession(node, true);
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
            String script = "WiniumDriverStarter.ps1";
            cmd = "If (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] \"Administrator\"))" +
                    "{$arguments = \"& '\" + $myinvocation.mycommand.definition + \"'\";Start-Process powershell -WindowStyle Hidden -Verb runAs -ArgumentList $arguments;Break};" +
                    "$port = \"--port " + port + "\";Start-Process \"" + userDir + "\\Winium.Desktop.Driver.exe\" $port -Wait -NoNewWindow;";
            transferScript(node, cmd, script);

            String pathToScript = userDir + "\\" + script;
            runScriptAsScheduledTask(node, pathToScript);

            StepCore.sleep(5);

            Log.debug("Checking if WiniumDesktopDriver was started");
            Integer i = 0;
            while ( i < 90 ){
                cmd = "netstat -an | findstr \"" + port + "\"";
                result = executeSingleCommandOnVM(cmd, node, 120);
                if (result.contains("LISTENING")){
                    break;
                }
                StepCore.sleep(2);
                i++;
            }

            if ( i >= 90 ){
                Log.error("Port " + port + " not in state LISTENING on host " + address +
                "! Please make sure that it is not blocked by firewall and that Winium.Desktop.Driver.exe process was started!");
            }

        }

        return rdpProcessId;

    }


    /**
     * Starts an Gui app on local or remote host<br>
     * Use it with winiumCore to handle automation of GUI apps under windows
     *
     * @param node String, node identifier, or localhost
     * @param pathToApp String, path to an app that shall be started
     * @param args String, additional arguments that shall be passed to an app
     */
    public void startApp(String node, String pathToApp, String args){

        Log.debug("Going to start an app on a remote host " + node);
        Map<String, String> conn = verifyConnectionDetails(node);
        String host = conn.get("address");

        host = getIpOfHost(host);

        //check if app we want to run exists otherwise unknown error or access denied error can be thrown!
        Log.debug("Checking that path " + pathToApp + " exists on the host " + host);
        if ( pathToApp.contains(":\\") || pathToApp.contains(":/") || pathToApp.contains("$")){
            Boolean isAvailable = checkThatFileExists(node, pathToApp);
            if ( ! isAvailable ) {
                Log.error("File " + pathToApp + " does not exists!");
            }
        }

        Log.debug("Starting winium resources");
        scenarioCtx.put("WiniumRemoteNodeId", String.class, node);
        String rdpProcessId = uploadAndStartWiniumDriver(node);
        scenarioCtx.put("WiniumRemoteRDPProcessId", String.class, rdpProcessId);
        minimizeAllWindows (node, "minimizeWindowsTask");

        String port = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.port");
        String url = "http://" + host + ":" + port;
        Log.debug("Url is " + url);
        URL uri = null;
        try {
            uri = new URL(url);
        } catch (MalformedURLException e) {
            Log.error("", e);
        }

        Log.debug("Trying to run an app from " + pathToApp);
        DesktopOptions options = new DesktopOptions();
        options.setApplicationPath(pathToApp);
        if ( args != null && (! args.equals("")) ) {
            Log.debug("Setting application arguments " + args);
            options.setArguments(args);
        } else {
            Log.debug("No application arguments provided " + args);
        }

        WiniumDriver App = new WiniumDriver(uri, options);
        scenarioCtx.put("App", WiniumDriver.class, App);

        //initialize winiumCore with driver
        WiniumCore winiumCore = new WiniumCore();
        scenarioCtx.put("WiniumCore", WiniumCore.class, winiumCore);

        if ( args != null && (! args.equals("")) ){
            Log.debug("Started an app from " + pathToApp + " " + args);
        } else {
            Log.debug("Started an app from " + pathToApp);
        }

        App = scenarioCtx.get("App", WiniumDriver.class);
        if ( App == null ){
            Log.error("Winium.Desktop.Driver was not initialized properly!");
        }

    }


    /**
     * Closes winium resources on a remote host
     *
     */
    public void closeWiniumResources(){

        //WiniumDriver driver = ctx.Object.get("App", WiniumDriver.class);
        WiniumDriver App = scenarioCtx.get("App", WiniumDriver.class);
        String rdpProcessId = scenarioCtx.get("WiniumRemoteRDPProcessId", String.class);
        String node = scenarioCtx.get("WiniumRemoteNodeId", String.class);

        if ( App != null ) {
            Log.debug("Try to close an application");
            try {
                App.close();
                scenarioCtx.put("App", WiniumDriver.class, null);
            } catch (WebDriverException e){
                Log.warn("Application is already closed");
                scenarioCtx.put("App", WiniumDriver.class, null);
            }
        }

        if ( rdpProcessId != null ) {
            Log.debug("Killing Winium driver process on remote host " + node);
            String cmd = "Get-CimInstance Win32_Process | Where {$_.name -match '.*Winium.*'} | Select Caption, CommandLine, ProcessId | Format-list";
            String result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

            if ( result.contains("Winium.Desktop.Driver.exe") ) {
                Log.debug("Closing Winium.Desktop.Driver.exe");
                String[] tmp = StringUtils.deleteWhitespace(result.trim()).split("ProcessId:");
                String processId = tmp[tmp.length - 1].trim();
                cmd = "Stop-Process -Id " + processId + " -Force -passThru";
                executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

                Log.debug("Deleting scheduled task used to run Winium driver");

                String script = "tempTask.ps1";
                cmd = "schtasks /DELETE /TN 'TempAutomationTask' /F;";
                transferScript(node, cmd, script);

                result = executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
                if (!result.contains("SUCCESS:")) {
                    Log.error("Failed to remove scheduled task TempAutomationTask");
                }

                killRdpSession(rdpProcessId);
            }
        }

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
    public String openRdpSession(String node, Boolean forceLogOff){
        File workingDir = FileCore.getTempDir();

        Map<String, String> conn = verifyConnectionDetails(node);
        String address = conn.get("address");
        String domain = conn.get("domain");
        String user = conn.get("user");
        String passwd = conn.get("password");

        address = getIpOfHost(address);

        Log.debug("Checking if there is RDP session open towards node " + address);

        //Just in case Skilui is in use -> we don't want to allow to open new RDP window if tests are run in parallel on same host
        StepCore.lockBrowser();

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
        Boolean isCertificateSet = globalCtx.get("CertificateHash_" + address, Boolean.class);

        if ( isCertificateSet == null){
            isCertificateSet = false;
        }

        if ( !isCertificateSet ) {
            Log.debug("Checking cert hash for node " + address + " in windows registry");
            cmd = "wmic /node:" + address + " /namespace:\\\\root\\CIMV2\\TerminalServices PATH Win32_TSGeneralSetting get SSLCertificateSHA1Hash";
            out = ExecutorCore.execute(cmd, workingDir, 10, true);
            output = new String(out.toByteArray(), Charset.defaultCharset());
            String hashCode = output.trim().replaceAll("SSLCertificateSHA1Hash", "").trim();

            Log.debug("Node's " + address + " cert hashCode is " + hashCode);
            if (!hashCode.equals("")) {
                Log.debug("Adding cert hash to the registry");
                cmd = "REG ADD \"HKEY_CURRENT_USER\\Software\\Microsoft\\Terminal Server Client\\Servers\\" + address + "\" /v \"CertHash\" /t REG_BINARY /d " + hashCode + " /f";
                ExecutorCore.execute(cmd, workingDir, 10, true);
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
            cmd = joinCommands(cmds, "\r\n", false);
            transferScript(node, cmd, script);
            executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);

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
                cmd = joinCommands(cmds, "\r\n", false);
                transferScript(node, cmd, script);
                executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
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
            cmd = "powershell.exe \"$Server='" + address + "';$User='" + domain + "\\" + user + "';" +
                    "$Pass='" + passwd + "';cmdkey /generic:$Server /user:$User /pass:$Pass;mstsc /v:$Server\"";
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
                Log.error("", e);
            }
            cmd = "powershell.exe \"$Server='" + address + "';$User='" + domain + "\\" + user + "';" +
                    "$Pass='" + passwd + "';cmdkey /generic:$Server /user:$User /pass:$Pass;mstsc /v:$Server /h:" + height + " /w:" + width + "\"";
        }
        Log.debug("Opening Rdp session to " + node);

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
     * Checks if an RDP session is open towards particular node
     *
     * @param rdpProcessId String, processID of an RDP session
     * @return Boolean
     */
    public Boolean checkIfRdpSessionIsOpen(String rdpProcessId){
        Log.debug("Checking RDP session with process id  " + rdpProcessId);

        Boolean exitValue = false;
        File workingDir = FileCore.getTempDir();

        String cmd = "powershell.exe \"$process=Get-process -Id " + rdpProcessId + " -ErrorAction SilentlyContinue;if($process -eq $null){Write-Host NotRunning}\"";
        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 10, true);
        String result = new String(out.toByteArray(), Charset.defaultCharset());
        if ( ! result.contains("NotRunning") ){
            exitValue = true;
        }

        return exitValue;
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

        String cmd = "Stop-Service '" + serviceName + "' -Force -passThru";
        String result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, timeout);
        if ( result.contains("Cannot find any service with service name")){
            Log.debug(serviceName + " service not running");
            output = true;
        } else {
            result = getServiceStatus(node, serviceName, 120);

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

                result = getServiceStatus(node, serviceName, 120);
            }


            if ( result.contains("Stopped") ) {
                Log.debug(serviceName + " was successfully stopped");
                output = true;
            } else {
                Log.warn(serviceName + " couldn't be stopped!");
            }
        }

        return output;
    }


    /**
     * Disables particular service (sets StartupType to Disabled)
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param serviceName String, name of the service
     */
    public void disableService(String node, String serviceName) {
        Log.debug("Disabling " + serviceName + " service");

        String cmd = "Set-Service '" + serviceName + "' -StartupType Disabled -passThru";
        executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
    }


    /**
     * Enables particular service (sets StartupType to Disabled)
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param serviceName String, name of the service
     */
    public void enableService(String node, String serviceName) {
        Log.debug("Enabling " + serviceName + " service");

        String cmd = "Set-Service '" + serviceName + "' -StartupType Automatic -passThru";
        executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
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
        String cmd = "Start-Service '" + serviceName + "';";
        String result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, timeout);
        if ( result.contains("Cannot find any service with service name")){
            Log.debug(serviceName + " service not running");
        } else {
            result = getServiceStatus(node, serviceName, 120);
            if ( result.contains("Running") ) {
                Log.debug(serviceName + " service was successfully started");
                output = true;
            } else {
                Log.warn(serviceName + " service couldn't be started!");
            }
        }

        return output;
    }


    /**
     * Retrieves service status from a remote windows host and returns is value or NotFound
     * An error will be printed if service status will not be found within a defined timeout
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param serviceName String, name of the service
     * @param timeout Integer, timeout in seconds
     *
     * @return Boolean
     */
    public String getServiceStatus(String node, String serviceName, Integer timeout){
        Log.debug("Getting service status of " + serviceName + " service");

        String output = "NotFound";
        String cmd = "$ScStatus=Get-Service \'" + serviceName + "\' | Select Status;Write-Host $ScStatus";
        String result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, timeout);
        if ( result.contains("Cannot find any service with service name")){
            Log.warn(serviceName + " service not found!");
        } else {
            output = result.replace("@{Status=","").replace("}","").trim();
        }

        Log.debug("Service " + serviceName + " status is " + output);

        return output;
    }


    /**
     * Awaits for particular service to reach desired state. Maximum timeout is 15 minutes
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param serviceName String, display name of the service that shall be checked, for example SQL Server (MSSQLSERVER)
     * @param desiredStatus String,  desired state of service, for example Running, Stopped
     * @param timeout String, timeout in format 00:00:30 which can't be higher than 15 minutes
     */
    public void awaitForServiceStatus(String node, String serviceName, String desiredStatus, String timeout){

        //Get all services where DisplayName matches serviceName and loop through each of them
        //Wait for the service to reach desiredStatus or a maximum timeout
        String cmd = "foreach($service in (Get-Service -DisplayName \'" + serviceName + "\')){" +
                "$service.WaitForStatus(\'" + desiredStatus + "\', \'" + timeout +"\')}";
        String result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 900);

        if ( result.contains("Exception") || result.contains("Time out has expired") ){
            Log.error("Timeout has been reached!");
        }

    }


    /**
     * Returns a list of exact names of available services. Can be a partial name
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param serviceName String, name of the service, can be regex, for example Tomcat can return Tomcat8 or Tomcat7
     * @return List
     */
    public List<String> getServiceList(String node, String serviceName){
        Log.debug("Creating list of services");
        String cmd = "Get-Service '" + serviceName + "' | Select Name";
        String result = executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
        String[] lines = result.split(System.getProperty("line.separator"));
        List<String> servicesList = new ArrayList<>();
        if ( ! result.contains("Cannot find any service") ) {
            for (String line : lines) {
                String tmp = line.trim();
                if (!tmp.equals("") && !tmp.equals("Name") && !tmp.matches(".*----.*")) {
                    servicesList.add(tmp);
                }
            }
        }

        return servicesList;
    }


    /**
     * Closes all open windows on a remote host
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     */
    public void closeAllOpenWindows(String node){
        Log.debug("Closing all open windows");

        String cmd = "(New-Object -comObject Shell.Application).Windows() | foreach-object {$_.quit()};" +
                "(Get-Process | Where-Object {$_.MainWindowTitle -ne \"\"}).CloseMainWindow();Start-sleep 3;";

        String script = "temp.ps1";
        transferScript(node, cmd, script);
        String userDir = getUserDir(node);
        runScriptAsScheduledTask(node, userDir + "\\" + script);
    }

}