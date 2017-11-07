package libs.libCore.modules;

import com.google.common.io.BaseEncoding;
import io.cloudsoft.winrm4j.client.WinRmClient;
import io.cloudsoft.winrm4j.client.WinRmClientContext;
import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import io.restassured.filter.log.LogDetail;
import org.apache.http.client.config.AuthSchemes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class WinRMCore {

    private SharedContext ctx;
    private Storage Storage;
    private WinRmClientContext context;
    private WinRmTool tool;
    private WinRmClient client;

    // PicoContainer injects class SharedContext
    public WinRMCore(SharedContext ctx) {
        this.ctx = ctx;
        this.Storage = ctx.Object.get("Storage", Storage.class);
    }


    /**
     * Creates winRM client and connects to specified node
     *
     * @param node, String, node name as specified in Environment.Active.WinRM.node configuration
     */
    public void createClient(String node) {

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        Integer port = Storage.get("Environment.Active.WinRM." + node + ".port");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");
        Boolean useHttps = Storage.get("Environment.Active.WinRM." + node + ".useHttps");
        String cmdWorkingDirectory = Storage.get("Environment.Active.WinRM." + node + ".workingDirectory");
        String sAuthScheme = Storage.get("Environment.Active.WinRM." + node + ".AuthenticationScheme");
        String domain = Storage.get("Environment.Active.WinRM." + node + ".domain");

        if (address == null) {
            Log.error("Environment.Active.WinRM. " + node + ".host " + " is null or empty!");
        }
        if (port == null) {
            port = 5985;
        }
        if (user == null) {
            Log.error("Environment.Active.WinRM. " + node + ".user " + " is null or empty!");
        }
        if (passwd == null) {
            Log.error("Environment.Active.WinRM. " + node + ".password " + " is null or empty!");
        }
        if (useHttps == null) {
            useHttps = false;
        }
        if (sAuthScheme == null) {
            sAuthScheme = AuthSchemes.NTLM;
        } else if (sAuthScheme.equalsIgnoreCase("BASIC")) {
            sAuthScheme = AuthSchemes.BASIC;
        } else if (sAuthScheme.equalsIgnoreCase("NTLM")) {
            sAuthScheme = AuthSchemes.NTLM;
        } else if (sAuthScheme.equalsIgnoreCase("KERBEROS")){
            sAuthScheme = AuthSchemes.KERBEROS;
        } else {
            Log.error("Please provide one of supported authentication schemas " +
                    "BASIC, NTLM, KERBEROS");
        }

        try {
            Log.debug("Creating new winRM client with following configuration");
            Log.debug("WinRM." + node + ".host: " + address);
            Log.debug("WinRM." + node + ".port: " + port);
            Log.debug("WinRM." + node + ".user: " + user);
            Log.debug("WinRM." + node + ".password: " + passwd);
            Log.debug("WinRM." + node + ".useHttps: " + useHttps);
            if ( cmdWorkingDirectory != null ){
                Log.debug("WinRM." + node + ".workingDirectory: " + cmdWorkingDirectory);
            }
            Log.debug("WinRM." + node + ".AuthenticationScheme: " + sAuthScheme);
            if ( domain != null ) {
                Log.debug("WinRM." + node + ".domain: " + domain);
            }
            context = WinRmClientContext.newInstance();

            WinRmTool.Builder builder;
            if ( domain != null ) {
                builder = WinRmTool.Builder.builder(address, domain, user, passwd);
            } else {
                builder = WinRmTool.Builder.builder(address, user, passwd);
            }
            builder.setAuthenticationScheme(sAuthScheme);
            builder.port(port);
            builder.useHttps(useHttps);
            builder.disableCertificateChecks(true);

            if ( cmdWorkingDirectory != null ){
                builder.workingDirectory(cmdWorkingDirectory);
            }

            builder.context(context);

            tool = builder.build();
            tool.setRetriesForConnectionFailures(1);
        } catch (Exception e){
            Log.error("", e);
        }
    }


    /**
     * Executes single windows native command
     *
     * @param cmd, String, command to be executed in windows cmd
     */
    public WinRmToolResponse executeCommand(String cmd, Integer timeout){
        WinRmToolResponse result = null;

        try {
            Long lTimeout = timeout.longValue();
            tool.setOperationTimeout(lTimeout * 1000);
            result = tool.executeCommand(cmd);
        } catch (Exception e){
            Log.error("", e);
        }

        return result;
    }


    /**
     * Executes a list of windows native commands concatenated with &
     *
     * @param cmd, List, commands list to be executed in windows cmd
     */
    public WinRmToolResponse executeCommand(List<String> cmd, Integer timeout){
        WinRmToolResponse result = null;

        try {
            Long lTimeout = timeout.longValue();
            tool.setOperationTimeout(lTimeout * 1000);
            result = tool.executeCommand(cmd);
        } catch ( Exception e){
            Log.error("", e);
        }

        return result;
    }


    /**
     * Executes a PowerShell command with the native windows command
     *
     * @param cmd, String, command to be executed in windows powershell
     */
    public WinRmToolResponse executePs(String cmd, Integer timeout){
        WinRmToolResponse result = null;

        try {
            Long lTimeout = timeout.longValue();
            tool.setOperationTimeout(lTimeout * 1000);
            result = tool.executePs(cmd);
        } catch ( Exception e ){
            Log.error("", e);
        }

        return result;
    }


    /**
     * Executes a list of PowerShell commands
     *
     * @param cmd, List, command list to be executed in windows powershell
     */
    public WinRmToolResponse executePs(List<String> cmd, Integer timeout){
        WinRmToolResponse result = null;

        try {
            Long lTimeout = timeout.longValue();
            tool.setOperationTimeout(lTimeout * 1000);
            result = tool.executePs(cmd);
        } catch ( Exception e){
            Log.error("", e);
        }

        return result;
    }


    /**
     * Closes and disconnects winRM client from remote host
     *
     */
    public void closeClient() {
        if ( context != null ){
            context.shutdown();
        }
    }


    /**
     * Creates powershell script under provided path and executes it
     *
     * @param script, String, script content
     * @param scriptPath, String, path where script file shall be created, for example "C:\\myscript-example.ps1";
     * @param args, String, optionl script arguments
     *
     * @return true is successful, false otherwise
     */
    public WinRmToolResponse executePsScriptFromString(String script, String scriptPath, String args){
        try {
            copyTo(new ByteArrayInputStream(script.getBytes()), scriptPath);
        } catch(Exception e){
            Log.error("", e);
        }

        //return tool.executePs("PowerShell -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -Command " + scriptPath + args);
        return tool.executePs("PowerShell -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File " + scriptPath + args);

    }


    /**
     * Creates batch script under provided path and executes it
     *
     * @param script, String, script content
     * @param scriptPath, String, path where script file shall be created, for example "C:\\myscript-example.bat";
     * @param args, String, optional arguments
     *
     * @return true is successful, false otherwise
     */
    public WinRmToolResponse executeBatchScriptFromString(String script, String scriptPath, String args){
        try {
            copyTo(new ByteArrayInputStream(script.getBytes()), scriptPath);
        } catch(Exception e){
            Log.error("", e);
        }

        return tool.executeCommand(scriptPath + args);

    }


    /**
     * creates file from InputStream
     * helper function
     *
     * @param source, InputStream, file string content
     * @param destination, String, path to output file
     * @throws Exception
     */
    private void copyTo(InputStream source, String destination) throws Exception {
        int chunkSize = 1024;

        byte[] inputData = new byte[chunkSize];
        int bytesRead;
        int expectedFileSize = 0;
        while ((bytesRead = source.read(inputData)) > 0) {
            byte[] chunk;
            if (bytesRead == chunkSize) {
                chunk = inputData;
            } else {
                chunk = Arrays.copyOf(inputData, bytesRead);
            }
            tool.executePs("If ((!(Test-Path " + destination + ")) -or ((Get-Item '" + destination + "').length -eq " +
                    expectedFileSize + ")) {Add-Content -Encoding Byte -path " + destination +
                    " -value ([System.Convert]::FromBase64String(\"" + new String(BaseEncoding.base64().encode(chunk)) + "\"))}");
            expectedFileSize += bytesRead;
        }
    }

}
