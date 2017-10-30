package libs.libCore.modules;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.contains;

public class SshCore {

    private SharedContext ctx;
    private Storage Storage;
    private SSHClient client;
    private Session session;
    private Expect expect;

    // PicoContainer injects class SharedContext
    public SshCore(SharedContext ctx) {
        this.ctx = ctx;
        this.Storage = ctx.Object.get("Storage", Storage.class);
    }

    public void createClient(String node) {
        client = new SSHClient();

        String address = Storage.get("Environment.Active.Ssh." + node + ".host");
        Integer port = Storage.get("Environment.Active.Ssh." + node + ".port");
        String user = Storage.get("Environment.Active.Ssh." + node + ".user");
        String passwd = Storage.get("Environment.Active.Ssh." + node + ".password");

        if ( address == null ) {
            Log.error("Environment.Active.Ssh. " + node + ".host " + " is null or empty!");
        }
        if ( port == null ) {
            Log.error("Environment.Active.Ssh. " + node + ".port " + " is null or empty!");
        }
        if ( user == null ) {
            Log.error("Environment.Active.Ssh. " + node + ".user " + " is null or empty!");
        }
        if ( passwd == null ) {
            Log.error("Environment.Active.Ssh. " + node + ".password " + " is null or empty!");
        }

        try {
            client.addHostKeyVerifier(dummyHostKeyVerifier());
            client.connect(address, port);
        } catch (IOException e) {
            Log.error("", e);
        }

        try {
            client.authPassword(user, passwd);
        } catch (UserAuthException e) {
            closeClient();
            Log.error("", e);
        } catch (TransportException e) {
            closeClient();
            Log.error("", e);
        }

        Log.debug("Connected to " + node + " as " + user + " on " + address + " and port " + port);
    }

    private void startSession() {
        try {
            session = client.startSession();
            session.allocateDefaultPTY();
        } catch (ConnectionException e) {
            closeClient();
            Log.error("", e);
        } catch (TransportException e) {
            closeClient();
            Log.error("", e);
        }
    }


    public SSHResult execute(String cmd, Integer timeout) {
        startSession();
        Session.Command command;
        SSHResult result = null;
        try {
            command = session.exec(cmd);
            command.join(timeout, TimeUnit.SECONDS);
            Integer exitStatus = command.getExitStatus();
            String stdout = IOUtils.readFully(command.getInputStream()).toString();
            String stderr = IOUtils.readFully(command.getErrorStream()).toString();
            result = new SSHResult(stdout, stderr, exitStatus);
        } catch (ConnectionException e) {
            Log.error("", e);
        } catch (TransportException e) {
            Log.error("", e);
        } catch (IOException e) {
            Log.error("", e);
        } finally {
            closeSession();
        }

        return result;
    }

    public Boolean checkThatFileExists(String nodeName, String pathToFile) {
        Boolean result = false;

        createClient(nodeName);
        SSHResult output = execute("test -e " + pathToFile,60);
        closeClient();

        if ( output.getExitCode() == 0 ) {
            result = true;
        }

        return result;

    }

    public Boolean waitForFile(String nodeName, String pathToFile, Integer timeout) {

        if ( timeout < 0 ) {
            return false;
        }
        Boolean result = checkThatFileExists(nodeName, pathToFile);
        if ( result.equals(false) ) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.error("", e);
            }
            return waitForFile(nodeName, pathToFile, timeout - 1);
        }

        return result;
    }

    public Boolean checkThatNodeIsAlive(String nodeName){
        Boolean result = false;

        createClient(nodeName);
        String cmd = "echo alive";
        SSHResult output = execute(cmd, 60);
        closeClient();

        if ( output.getExitCode() == 0 ) {
            result = true;
        }

        return result;
    }

    public File downloadFileViaScp(String nodeName, String pathToFileOnRemote, String pathToLocalDir){
        createClient(nodeName);
        String fileName = FilenameUtils.getName(pathToFileOnRemote);

        try {
            client.newSCPFileTransfer().download(pathToFileOnRemote, new FileSystemFile(pathToLocalDir));
        } catch (IOException e) {
            Log.error("", e);
        } finally {
            closeClient();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.error("", e);
        }

        return new File(pathToLocalDir + File.separator + fileName);
    }

    public Boolean uploadFileViaScp(String nodeName, String pathToLocalFile, String pathToUploadDirOnRemote) {
        Boolean result = false;

        createClient(nodeName);
        try {
            client.newSCPFileTransfer().upload(new FileSystemFile(pathToLocalFile), pathToUploadDirOnRemote);
            result = true;
        } catch (IOException e) {
            Log.error("", e);
        } finally {
            closeClient();
        }

        return result;
    }

    public File downloadFileViaSftp(String nodeName, String pathToFileOnRemote, String pathToLocalDir) {
        createClient(nodeName);
        String fileName = FilenameUtils.getName(pathToFileOnRemote);
        try {
            SFTPClient sftp = client.newSFTPClient();
            try {
                sftp.get(pathToFileOnRemote, new FileSystemFile(pathToLocalDir));
            } finally {
                sftp.close();
            }
        } catch (IOException e) {
            Log.error("", e);
        } finally {
            closeClient();
        }

        return new File(pathToLocalDir + File.separator + fileName);

    }

    public Boolean uploadFileViaSftp(String nodeName, String pathToLocalFile, String pathToUploadDirOnRemote) {
        Boolean result = false;

        createClient(nodeName);
        try {
            SFTPClient sftp = client.newSFTPClient();
            try {
                sftp.put(new FileSystemFile(pathToLocalFile), pathToUploadDirOnRemote);
                result = true;
            } finally {
                sftp.close();
            }
        } catch (IOException e) {
            Log.error("", e);
        } finally {
            closeClient();
        }

        return result;
    }

    public void startShell(Integer timeout){
        startSession();
        try {
            Session.Shell shell = session.startShell();
            expect = new ExpectBuilder()
                    .withOutput(shell.getOutputStream())
                    .withInputs(shell.getInputStream(), shell.getErrorStream())
                    .withTimeout(timeout, TimeUnit.SECONDS)
                    .withInputFilters(removeColors(), removeNonPrintable())
                    .withExceptionOnFailure()
                    .build();
        } catch (ConnectionException e) {
            closeClient();
            Log.error("", e);
        } catch (TransportException e) {
            closeClient();
            Log.error("", e);
        } catch (IOException e) {
            closeClient();
            Log.error("", e);
        }

    }

    public SSHResult executeInShell(String cmd, String expectedOutput) {
        String stdout;
        SSHResult result = null;
        try {
            expect.sendLine(cmd);
            stdout  = expect.expect(contains(expectedOutput)).getInput();
            Log.debug(stdout);
            result = new SSHResult(stdout, "", 0);
        } catch (Exception e) {
            closeShell();
            closeSession();
            closeClient();
            Log.error("", e);
        }

        return result;
    }

    public void closeShell() {
        try {
            expect.close();
        } catch (IOException e) {
            Log.error("", e);
        }
        closeSession();
    }

    private void closeSession() {
        try {
            if ( session != null ) {
                Boolean isOpen = session.isOpen();
                if (isOpen) {
                    session.close();
                }
            }
        } catch (TransportException e) {
            Log.error("", e);
        } catch (ConnectionException e) {
            Log.error("", e);
        }
    }

    public void closeClient() {
        try {
            if ( client != null ) {
                Boolean isConnected = client.isConnected();
                if (isConnected) {
                    client.disconnect();
                }
            }
        } catch (IOException e) {
            Log.error("", e);
        }
    }

    private HostKeyVerifier dummyHostKeyVerifier() {
        return new HostKeyVerifier() {
            @Override
            public boolean verify(String arg0, int arg1, PublicKey arg2) {
                return true;
            }
        };
    }

}