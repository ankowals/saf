package libs.libCore.modules;

import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parallel script execution on remote host is currently not supported!
 */

@SuppressWarnings("unchecked")
public class WinRSCore {

    private Context globalCtx;
    private Context scenarioCtx;
    private FileCore FileCore;
    private StepCore StepCore;
    private ExecutorCore ExecutorCore;
    private Storage Storage;
    private CloudDirectorCore CloudDirectorCore;

    public WinRSCore() {
        this.globalCtx = GlobalCtxSingleton.getInstance();
        this.scenarioCtx = globalCtx.get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
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
    Map<String, String> verifyConnectionDetails(String node){
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
            Log.warn("Configuration for Environment.Active.WinRM." + node + ".domain not found or null!");
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
    String getIpOfHost(String name){
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
                String[] tmp = name.split("[.]");
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
        String cmd = "Powershell.exe \"$ip = (Resolve-DnsName '" + name + "').IPAddress; Write-Host $ip\"";
        //Log.debug("Command to execute is " + cmd);
        ExecResult out = ExecutorCore.execute(cmd , workingDir, 60);
        String result = out.getStdOut();

        if ( result.contains("DNS name does not exist") ){
            Log.warn("Can't resolve FQDN to an ip address");
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
    private void addToTrustedHosts(String name){
        Log.debug("Adding host " + name + " to trusted hosts list");

        //check if host was already added to trusted hosts list else add it
        Boolean isHostTrusted = globalCtx.get("TrustedHost_" + name, Boolean.class);

        if ( isHostTrusted == null){
            isHostTrusted = false;
        }

        if ( isHostTrusted ) {
            Log.debug("Host was already added to trusted host list");
            return;
        }

        File workingDir = FileCore.getTempDir();

        String runQuickConf = "Enable-PSRemoting -force;";
        String addToTrustedHots = "$curVal=(Get-Item WSMan:\\localhost\\Client\\TrustedHosts).Value;if($curVal -notlike '*" + name + "*'){Set-Item WSMan:\\localhost\\Client\\TrustedHosts -Value \"$curVal, " + name + "\" -Force};";
        String checkClient = "$curVal=(Get-Item WSMan:\\localhost\\Client\\TrustedHosts).Value; Write-Host \"TrustedHosts: $curVal\";";

        String cmd = "If (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] \"Administrator\"))" +
                "{$arguments = \"& '\" + $myinvocation.mycommand.definition + \"'\";Start-Process powershell -WindowStyle Hidden -Verb runAs -ArgumentList $arguments;Break};" +
                runQuickConf + addToTrustedHots + checkClient;

        File script = FileCore.createTempFile("temp","ps1");
        FileCore.writeToFile(script, cmd);

        Log.debug("Script " + script.getAbsolutePath() + " content is " + cmd);

        Log.debug("Going to execute Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File '" + script.getAbsolutePath() + "'");
        ExecutorCore.execute("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File '" + script.getAbsolutePath() + "'", workingDir, 60);
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
    public String executeSingleCommandOnRemote (String cmd, String node, Integer timeout){
        File workingDir = FileCore.getTempDir();

        Map<String, String> conn = verifyConnectionDetails(node);
        String address = conn.get("address");
        String port = conn.get("port");
        String domain = conn.get("domain");
        String user = conn.get("user");
        String passwd = conn.get("password");

        String encodedPass = passwd;

        address = getIpOfHost(address);

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding ){
            passwd = StepCore.decodeString(passwd);
        }

        String invocation = "winrs -r:http://" + address + ":" + port + " -u:" + user + " -p:" + passwd;
        if ( domain != null && !domain.equals("") ) {
            invocation = "winrs -r:http://" + address + ":" + port + " -u:" + domain + "\\" + user + " -p:" + passwd;
        }
        cmd =  invocation + " " + cmd;

        Log.debug("Going to execute " + cmd.replace(passwd, encodedPass).replaceAll(" -P \\w+ -i ", " -P ***** -i "));
        ExecResult out = ExecutorCore.execute(cmd, workingDir, timeout);
        return out.getStdOut();
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
    public String executeBatchFileOnRemote(List<String> cmdList, String node, Integer timeout){
        String cmd = joinCommands(cmdList, "\r\n", true);

        String script = FileCore.createTempFile("temp", "bat").getName();
        transferScript(node, cmd, script);

        Log.debug("Going to execute call " + script);
        return executeSingleCommandOnRemote("call " + script, node, timeout);
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
        String script = FileCore.createTempFile("temp", "sql").getName();
        transferScript(node, cmd, script);

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding ){
            dbpass = StepCore.decodeString(dbpass);
        }

        return executeSingleCommandOnRemote(" SQLCMD -b -S " + address + " -U " + dbUser + " -P " + dbpass + " -i " + script, node, timeout);
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

        String encodedPass = passwd;

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding ){
            passwd = StepCore.decodeString(passwd);
        }

        //check if user dir per node is known else retrieve it from node
        String userDir = globalCtx.get("userDir_" + node, String.class);

        if ( userDir == null || userDir.equals("") ) {
            Log.warn("userDir not known. Going to check it on remote host");

            address = getIpOfHost(address);

            String invocation = "winrs -r:http://" + address + ":" + port + " -u:" + user + " -p:" + passwd;
            if ( domain != null && !domain.equals("") ) {
                invocation = "winrs -r:http://" + address + ":" + port + " -u:" + domain + "\\" + user + " -p:" + passwd;
            }
            Log.debug("Going to execute " + invocation.replace(passwd, encodedPass));
            ExecResult out = ExecutorCore.execute(invocation + " cd", workingDir, 120);
            String result = out.getStdOut();

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
        String output = executeSingleCommandOnRemote("winrm get winrm/config", node, 60);
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
            String script = FileCore.createTempFile("temp", "bat").getName();
            transferScript(node, cmd, script);

            String result = executeSingleCommandOnRemote("call " + script, node, 120);

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

        //Log.debug("Refreshing DNS cache to make connection quicker");
        File workingDir = FileCore.getTempDir();
        String cmd = "ipconfig /flushdns";
        //Log.debug("Command to execute is " + cmd);
        ExecutorCore.execute(cmd, workingDir, 60);

        //Log.debug("Checking if host " + address + " is accessible");

        String result = "";
        int i = 0;
        while ( ! result.contains("host " + address + " is accessible") ) {

            result = executeSingleCommandOnRemote("'echo host " + address + " is accessible'", node, 60);

            if ( result.contains("host " + address + " is accessible")){
                break;
            }

            StepCore.sleep(60);
            i++;

            if ( i == 6 ){
                Log.error("Host not accessible! Timeout of 5 minutes reached! " + "" +
                        "Consider to add remote host address " + address + " to Trusted hosts list");
            }

            Boolean useCloudDirector = Storage.get("Environment.Active.Cloud.useCloudDirector");
            if (useCloudDirector) {
                Boolean isReboot = detectRebootViaCloudDirector(address);
                //force network disconnection via cloud director because RDP connection can't be set
                if (i == 2 && isReboot) {
                    reconnectNetworkToVmViaCloudDirector();
                }
                //force reboot via cloud director because winRM service does not want to start
                if (i == 4 && isReboot) {
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
                    if ( name != null && (!name.equals("")) ) {
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

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //disconnect default network from vm
        CloudDirectorCore.disconnectNetworkConnection(vmHref);

        //teardown session
        CloudDirectorCore.logout();

        Log.debug("Going to connect network connection back");
        //create new session
        session = CloudDirectorCore.login();

        //get organization using its name and login response
        orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //connect default network
        CloudDirectorCore.connectNetworkConnection(vmHref);

        //teardown session
        CloudDirectorCore.logout();
    }


    /**
     * Reboots remote host via CLoudDirector api in case connection via WinRM is not possible.
     */
    private void rebootVmViaCloudDirector(){
        Log.debug("Going to reboot host via cloudDirector api");

        String vMkey = Storage.get("Environment.Active.Cloud.CloudDirector.VmNameForReboot");
        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + vMkey + ".VmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + vMkey + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + vMkey + ".Vdc");

        if ( vm_name == null ) {
            Log.warn("Value of TestData." + vMkey + ".VmName null or empty!");
            return;
        }
        if ( vapp == null ) {
            Log.warn("Value of TestData." + vMkey + ".VApp null or empty!");
            return;
        }
        if ( vdc == null ) {
            Log.warn("Value of TestData." + vMkey + ".Vdc null or empty!");
            return;
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //reboot vm
        CloudDirectorCore.rebootVm(vmHref);

        //teardown session
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
        Log.debug("Going to execute " + cmd);
        ExecutorCore.execute(cmd, workingDir, 60);

        Log.debug("Checking if RDP session towards " + address + " can be open");
        int i = 0;
        String tmp = "";
        while ( ! tmp.contains("TcpTestSucceeded:True") ) {

            cmd = "cmd.exe /c \"Powershell.exe Test-NetConnection " + address + " -CommonTcpPort RDP\"";
            Log.debug("Going to execute " + cmd);
            ExecResult out = ExecutorCore.execute(cmd, workingDir, 120);

            tmp = out.getStdOut();
            String result = StringUtils.deleteWhitespace(tmp).trim();

            if (result.contains("TcpTestSucceeded:True")) {
                break;
            }

            StepCore.sleep(60);
            i++;

            if (i == 6) {
                Log.error("RDP on host not accessible! Timeout of 5 minutes reached!");
            }

            Boolean useCloudDirector = Storage.get("Environment.Active.Cloud.useCloudDirector");
            if (useCloudDirector) {
                Boolean isReboot = detectRebootViaCloudDirector(address);
                //force network disconnection via cloud director because RDP connection can't be set
                if (i == 2 && isReboot) {
                    reconnectNetworkToVmViaCloudDirector();
                }
                //force reboot via cloud director because winRM service does not want to start
                if (i == 4 && isReboot) {
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

        if (path.length() == 0 ){
            Log.error("Provided path is empty!");
        }

        String result = executeSingleCommandOnRemote("if exist '" + path + "' (echo exist)", node, 60);
        return result.contains("exist");
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

        StringBuilder sb = new StringBuilder();
        for ( String path : paths) {
            sb.append(", ");
            sb.append("\"");
            sb.append(path);
            sb.append("\"");
        }
        String sPaths = sb.toString();

        String script = FileCore.createTempFile("temp", "ps1").getName();
        String cmd = "Write-Host \"Checking files availability\";$paths=@(" + sPaths.replaceFirst(", ","") + ");foreach($path in $paths){" +
                "Write-Host $path;if (!(Test-Path $path)){Write-Host \"Error: Path '$path' not found\"}}";

        transferScript(node, cmd, script);
        String result = executeSingleCommandOnRemote("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);

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
        String port = conn.get("port");

        String encodedPass = passwd;

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding ){
            passwd = StepCore.decodeString(passwd);
        }

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

        int i = 0;
        String output = "Connecting to remote server failed";
        String error = output;

        String cred = "$U='" + user + "';$P='" + passwd + "';";
        if ( domain != null && !domain.equals("") ) {
            cred = "$U='" + domain + "\\" + user + "';$P='" + passwd + "';";
        }

        String cmd = "cmd.exe /c \"Powershell.exe " + cred +
                "$pw=ConvertTo-SecureString $p -AsPlainText -Force;" +
                "$cred=New-Object Management.Automation.PSCredential($U, $pw);" +
                "$Src=[System.IO.File]::ReadAllBytes('" + item + "');" +
                "$s=New-PSSession -computerName " + address + " -port " + port + " -credential $cred;" +
                "Enter-PSSession $s;" +
                "Invoke-Command -Session $s -Scriptblock " +
                "{if(test-path " + target + "){Remove-Item " + target + " -force -recurse}};" +
                "Invoke-Command -Session $s -ArgumentList $Src -Scriptblock " +
                "{[System.IO.File]::WriteAllBytes('" + target + "', $args)};" +
                //"Write-Host 'Content of created file " + userDir + "\\" + fileName + " is';" +
                //"Invoke-Command -Session $s -Scriptblock " +
                //"{type " + userDir + "\\" + fileName + "};" +
                "Remove-PSSession $s\"";
        while( i < 5 ) {
            Log.debug("Going to execute " + cmd.replace(passwd, encodedPass));
            ExecResult out = ExecutorCore.execute(cmd, workingDir, 60);
            output = out.getStdOut();
            error = out.getStdErr();

            if ( ! output.matches(".*Connecting to remote server.*failed.*") &&
                    ! output.matches(".*The network name.*") &&
                    ! output.matches(".*Exception calling .*WriteAllBytes.*with.*") &&
                    ! output.matches(".*the server name cannot be resolved.*")) {
                break;
            }

            if ( ! error.matches(".*Connecting to remote server.*failed.*") &&
                    ! error.matches(".*The network name.*") &&
                    ! error.matches(".*Exception calling .*WriteAllBytes.*with.*") &&
                    ! error.matches(".*the server name cannot be resolved.*")) {
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

        String encodedPass = passwd;

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding ){
            passwd = StepCore.decodeString(passwd);
        }

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
        String letter = Storage.get("Environment.Active.WinRS.mountPoint");

        //using SMB protocol shall work as long as user has admin rights
        //
        String cred = "$U='" + user + "';$P='" + passwd + "';";
        if ( domain != null && !domain.equals("") ) {
            cred = "$U='" + domain + "\\" + user + "';$P='" + passwd + "';";
        }

        String cmd = "cmd.exe /c \"Powershell.exe " + cred +
                "$pw=ConvertTo-SecureString $P -AsPlainText -Force;" +
                "$cred=New-Object Management.Automation.PSCredential($U, $pw);" +
                "New-PSDrive -name " + letter.replace(":","") + " -Root \\\\" + address +"\\" + userDir.replaceFirst(":","\\$") +
                " -Credential $cred -PSProvider filesystem;" +
                "Copy-Item " + item + " " + letter + "\\" + fileName + "\"";

        Log.debug("Going to execute " + cmd.replace(passwd, encodedPass));
        ExecutorCore.execute(cmd, workingDir, 60);

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

        String encodedPass = passwd;

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding ){
            passwd = StepCore.decodeString(passwd);
        }

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

        String letter = Storage.get("Environment.Active.WinRS.mountPoint");

        //using SMB protocol shall work as long as user has admin rights
        //

        String cred = "$U='" + user + "';$P='" + passwd + "';";
        if ( domain != null && !domain.equals("") ) {
            cred = "$U='" + domain + "\\" + user + "';$P='" + passwd + "';";
        }

        String cmd = "cmd.exe /c \"Powershell.exe " + cred +
                "$pw=ConvertTo-SecureString $P -AsPlainText -Force;" +
                "$cred=New-Object Management.Automation.PSCredential($U, $pw);" +
                "New-PSDrive -name " + letter.replace(":","") + " -Root " + source.replace(File.separator + fileName,"").replaceFirst(":","\\$") +
                " -Credential $cred -PSProvider filesystem;" +
                "Copy-Item " + letter + "\\" + fileName + " " + target + "\"";

        Log.debug("Going to execute " + cmd.replace(passwd, encodedPass));
        ExecutorCore.execute(cmd, workingDir, 60);

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
    public boolean fileContains(String node, String path, String lookUpString){
        String cmd = "Get-Content " + path + " | Where-Object {$_.contains('" + lookUpString + "')}";

        Log.debug("Going to execute " + cmd);
        String results = executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);

        return results.contains(lookUpString);
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
        String cmd = "Get-Content " + path + " | Where-Object {$_.contains('" + lookUpString + "')}";
        return executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
    }


    /**
     * Checks if particular file is locked on a remote host. Requires write access to the file!
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param path String, path to text file
     *
     * @return Boolean
     */
    public boolean checkThatFileIsLocked (String node, String path){

        boolean isLocked = true;

        String cmd = "$file=New-Object System.IO.FileInfo '" + path + "';" +
                "$stream=$file.Open([System.IO.FileMode]::Open, " +
                "[System.IO.FileAccess]::ReadWrite, " +
                "[System.IO.FileShare]::None);" +
                "if($stream){$stream.Close();Write-Host 'File not locked'}";
        String results = executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);

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
    public String runScriptAsScheduledTask (String node, String pathToScript, String taskName){

        Map<String, String> conn = verifyConnectionDetails(node);
        String domain = conn.get("domain");
        String user = conn.get("user");
        String password = conn.get("password");

        String encodedPass = password;

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding ){
            password = StepCore.decodeString(password);
        }

        String userDir = getUserDir(node);
        String stdErr = userDir + "\\" + taskName + ".stdErr";
        String stdOut = userDir + "\\" + taskName + ".stdOut";

        Log.debug("Removing old log files");
        Boolean isFilePresent = checkThatFileExists(node, userDir + "\\" + taskName + ".stdErr");
        if ( isFilePresent ){
            executeSingleCommandOnRemote("\"Powershell.exe Remove-Item " + userDir + "\\" + taskName + ".stdErr\"", node, 120);
        }
        isFilePresent = checkThatFileExists(node, userDir + "\\" + taskName + ".stdOut");
        if ( isFilePresent ){
            executeSingleCommandOnRemote("\"Powershell.exe Remove-Item " + userDir + "\\" + taskName + ".stdOut\"", node, 120);
        }

        Log.debug("Creating new scheduled task " + taskName + ". " +
                "Assigning normal priority to it. " +
                "Recreating task with priority");

        //this script is used to run the original one and redirect outputs to file
        //it will be called from a scheduled task and in turn it will call the original one:)
        String script = "wrapper" + taskName + ".ps1";
        String cmd = "Start-process powershell -WindowStyle Hidden -Wait" +
                " -RedirectStandardError " + stdErr +
                " -RedirectStandardOutput " + stdOut +
                " -ArgumentList \" -ExecutionPolicy Bypass" +
                " -Command " + pathToScript + "\"";
        Log.debug("Content of the script " + script + " to be created on remote node " + node + " is " + cmd);
        transferScript(node, cmd, script);

        //this script is used to create scheduled task
        script = "schTask_" + taskName + ".ps1";
        List<String> cmdList = new ArrayList();

        String ru = "/RU \"" + user + "\" /IT ";
        if ( domain != null && !domain.equals("") ) {
            ru = "/RU \"" + domain + "\\" + user + "\" /IT ";
        }

        cmdList.add("schtasks /CREATE /TN '" + taskName + "' /SC MONTHLY /RL HIGHEST " +
                ru +
                "/RP \"" + password + "\" " +
                "/TR \"powershell -windowstyle hidden -NoProfile -ExecutionPolicy Bypass -File " + userDir + "\\wrapper" + taskName + ".ps1\" " +
                "/F;");
        cmdList.add("$taskFile = \"" + userDir + "\\Remote" + taskName + ".txt\";");
        cmdList.add("Remove-Item $taskFile -Force -ErrorAction SilentlyContinue;");
        cmdList.add("[xml]$xml = schtasks /QUERY /TN '" + taskName + "' /XML;");
        cmdList.add("$xml.Task.Settings.Priority=\"4\";");
        cmdList.add("$xml.Save($taskFile);");
        cmdList.add("schtasks /CREATE /TN '" + taskName + "' " + ru +
                "/RP \"" + password + "\" " +
                "/XML $taskFile /F;");

        cmd = joinCommands(cmdList, "\r\n", true);
        Log.debug("Content of the script " + script + " to be created on remote node " + node + " is " + cmd.replaceAll(password, encodedPass));
        transferScript(node, cmd, script);

        String result = executeSingleCommandOnRemote("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
        if ( ! result.contains("SUCCESS:") ){
            Log.error("Failed to create new scheduled task " + taskName);
        } else {
            Log.debug("New scheduled task " + taskName + " created");
        }

        Log.debug("Running scheduled task");
        cmd = "schtasks /RUN /I /TN '" + taskName + "';";
        result = executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
        if ( ! result.contains("SUCCESS:") ){
            Log.error("Failed to run scheduled task " + taskName);
        }

        String processId = "";
        int i = 0;
        while( i < 2 ) {
            Log.debug("Extracting process id of a scheduled task " + taskName);
            String tmp = pathToScript.replaceAll("\\\\", "\\\\\\\\");
            cmd = "$proc = Get-CimInstance Win32_Process | Where {$_.CommandLine -match 'Command " + tmp +
                    "' -or $_.CommandLine -match ' & .*" + tmp + "'} | Select Caption, CommandLine, ProcessId;" +
                    "Write-Host $proc.ProcessId, $proc.CommandLine -Separator ',';";
            result = executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
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
            String stdErrOutput = executeSingleCommandOnRemote("type " + stdErr, node, 60);
            if ( stdErrOutput.equals("") ){
                Log.warn("Script execution didn't return any error but processId was not found! " +
                        "Going to return empty processId");
            }
        }

        scenarioCtx.put( processId + "_ScheduledTaskName", String.class, taskName);

        return processId;
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
    public boolean superviseScheduledTaskExecution(String node, String processId, Integer timeout){
        String taskName = scenarioCtx.get(processId + "_ScheduledTaskName", String.class);

        if ( timeout < 0 || processId.equals("") ) {
            String userDir = getUserDir(node);

            String stdErr = userDir + "\\" + taskName + ".stdErr";
            String stdOut = userDir + "\\" + taskName + ".stdOut";

            Log.warn("Probably an error happen during script execution. Printing stdErr output");
            executeSingleCommandOnRemote("type " + stdErr, node, 60);
            Log.warn("Probably an error happen during script execution. Printing stdOut output");
            executeSingleCommandOnRemote("type " + stdOut, node, 60);
            Log.warn("Killing scheduled task " + taskName);

            String script = taskName + ".ps1";
            String cmd = "schtasks /END /TN '" + taskName + "';";
            Log.debug("Content of the script " + script + " to be created on remote node " + node + " is " + cmd);
            transferScript(node, cmd, script);

            String result = executeSingleCommandOnRemote("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            if ( ! result.contains("SUCCESS:") ){
                Log.warn("Failed to kill scheduled task " + taskName);
            }

            Log.warn("Deleting scheduled task " + taskName);
            cmd = "schtasks /DELETE /TN '" + taskName + "' /F;";
            Log.debug("Content of the script " + script + " to be created on remote node " + node + " is " + cmd);
            transferScript(node, cmd, script);

            result = executeSingleCommandOnRemote("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            if ( ! result.contains("SUCCESS:") ){
                Log.warn("Failed to remove scheduled task " + taskName);
            }

            if ( timeout < 0 ) {
                Log.error("Timeout reached for scheduled task execution supervision!");
            } else {
                Log.error("Task process id not found! Script didn't run. Please check its content. " +
                        "Maybe an error happen during execution?");
            }
        }

        String cmd = "Get-Process -Id " + processId;
        String result = executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);

        if ( result.contains("Cannot find a process") ){
            Log.debug("Removing scheduled task " + taskName);

            String script = taskName + ".ps1";
            cmd = "schtasks /DELETE /TN '" + taskName + "' /F;";
            Log.debug("Content of the script " + script + " to be created on remote node " + node + " is " + cmd);
            transferScript(node, cmd, script);

            result = executeSingleCommandOnRemote("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
            if ( ! result.contains("SUCCESS:") ){
                Log.error("Failed to remove scheduled task " + taskName);
            }

            return true;
        }

        StepCore.sleep(60);
        return superviseScheduledTaskExecution(node, processId, timeout - 60);
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

        boolean output = false;

        String cmd = "Stop-Service '" + serviceName + "' -Force -passThru";
        String result = executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, timeout);
        if ( result.contains("Cannot find any service with service name")){
            Log.debug(serviceName + " service not running");
            output = true;
        } else {
            result = getServiceStatus(node, serviceName, 120);

            if ( result.contains("StopPending") ){
                Log.warn("Service was not shutdown. Hanging state detected! Going to kill it");

                cmd = "sc queryex " + serviceName;
                String subResult = executeSingleCommandOnRemote(cmd, node, 120);
                String[] lines = subResult.split(System.getProperty("line.separator"));
                String pid = "";
                for ( String line : lines) {
                    if ( line.contains("PID") ) {
                        int idx = line.indexOf(":");
                        pid = line.substring(idx+1).trim();
                        Log.debug("Service PID is " + pid);
                        break;
                    }
                }

                if ( ! pid.equals("") ) {
                    cmd = "taskkill /PID " + pid + " /F";
                    executeSingleCommandOnRemote(cmd, node, 120);
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
        executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
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
        executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
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

        boolean output = false;
        String cmd = "Start-Service '" + serviceName + "';";
        String result = executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, timeout);
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
        String result = executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, timeout);
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
        String result = executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 900);

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
        String result = executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
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
     * Kills all powershell process from particular user on remote host<br>
     *     Can be used to kill orphaned ps process from unsuccessful command execution
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     */
    public void killAllRemotePsProcess(String node){
        Log.debug("Killing ps process on remote node " + node);

        Map<String, String> conn = verifyConnectionDetails(node);
        String user = conn.get("user");
        String cmd = "Get-CimInstance -ClassName Win32_Process |" +
                " Where-Object {$_.Name -imatch 'wsmprovhost.exe'} |" +
                " Where-Object {(Invoke-CimMethod -InputObject $_ -MethodName GetOwner).User -eq " + user + "} |" +
                " Invoke-CimMethod -MethodName Terminate";

        executeSingleCommandOnRemote("Powershell.exe \"" + cmd + "\"", node, 120);
    }

}