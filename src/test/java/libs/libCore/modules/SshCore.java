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


    /**
     * Creates new Ssh client and opens connection. Uses password authentication.
     *
     * @param node, String, node name as defined in configuration Environment.Active.Ssh.node
     */
    public void createClient(String node) {

        String address = Storage.get("Environment.Active.Ssh." + node + ".host");
        Integer port = Storage.get("Environment.Active.Ssh." + node + ".port");
        String user = Storage.get("Environment.Active.Ssh." + node + ".user");
        String passwd = Storage.get("Environment.Active.Ssh." + node + ".password");

        if ( address == null ) {
            Log.error("Environment.Active.Ssh. " + node + ".host " + " is null or empty!");
        }
        if ( port == null ) {
            port = 22;
        }
        if ( user == null ) {
            Log.error("Environment.Active.Ssh. " + node + ".user " + " is null or empty!");
        }
        if ( passwd == null ) {
            Log.error("Environment.Active.Ssh. " + node + ".password " + " is null or empty!");
        }

        try {
            client = new SSHClient();
            client.addHostKeyVerifier(dummyHostKeyVerifier());
            client.connect(address, port);
        } catch (IOException e) {
            Log.error("Unable to connect via ssh to " + node + " as " + user
                    + " on " + address + " and port " + port, e);
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

        Log.debug("Connected via ssh to " + node + " as " + user + " on " + address + " and port " + port);
    }


    /**
     * Starts Ssh session that can be used for single command execution
     * helper function used by execute
     *
     */
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


    /**
     * Executes a single command in a session. For each command new session is open and closed
     * after command execution
     *
     * @param cmd, String, command to execute
     * @param timeout, Integer, timeout
     *
     * @return SSHResult, result set that contains stdout, stderr and exit status code
     */
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


    /**
     * Checks that particular file exists on remote host
     *
     * @param nodeName, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToFile, String, path to the file on remote host
     *
     * @return Boolean, true if file exits, false otherwise
     */
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


    /**
     * Waits for a file to be present on a remote host for defined time duration
     *
     * @param nodeName, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToFile, String, path to the file on remote host
     * @param timeout, Integer, timeout
     *
     * @return Boolean, true if file exists, false otherwise
     */
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


    /**
     * Checks that node is accessible
     *
     * @param nodeName, String, node name as defined in configuration Environment.Active.Ssh.node
     *
     * @return Boolean, true if alive, false otherwise
     */
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


    /**
     * Downloads file from remote node using scp
     *
     * @param nodeName, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToFileOnRemote, String, path to the file on remote host
     * @param pathToLocalDir, String, path to the directory where file shall be downloaded
     *
     * @return File, file handle
     */
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


    /**
     * Uploads file via scp
     *
     * @param nodeName, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToLocalFile, String, path to the file on local host
     * @param pathToUploadDirOnRemote, String, path to the directory where file shall be uploaded on remote host
     *
     * @return Boolean, true if upload was successful, false otherwise
     */
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


    /**
     * Downloads file from remote node using sftp
     *
     * @param nodeName, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToFileOnRemote, String, path to the file on remote host
     * @param pathToLocalDir, String, path to the directory where file shall be downloaded
     *
     * @return File, file handle
     */
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


    /**
     * Uploads file via sftp
     *
     * @param nodeName, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToLocalFile, String, path to the file on local host
     * @param pathToUploadDirOnRemote, String, path to the directory where file shall be uploaded on remote host
     *
     * @return Boolean, true if upload was successful, false otherwise
     */
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


    /**
     * Starts interactive shell
     *
     * @param timeout, Integer, timeout used for supervision of each command
     */
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


    /**
     * Executes a command in am interactive shell. Shell has to be open to make use of this method.
     * It shall be closed when all commands are executed using separate method.
     *
     * @param cmd, String, command to execute
     * @param expectedOutput, String, expected output in stdout, it can be prompt or string
     *
     * @return SSHResult, result set that contains stdout, stderr="" and exit status code=0
     */
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


    /**
     * Closes interactive shell
     */
    public void closeShell() {
        try {
            expect.close();
        } catch (IOException e) {
            Log.error("", e);
        }
        closeSession();
    }


    /**
     * Closes session
     * helper function used by execute method
     */
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


    /**
     * Closes ssh client and connection to remote host
     */
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


    /**
     * creates a blank host key verifier
     * helper function used by createClient method to always pass key verification
     */
    private HostKeyVerifier dummyHostKeyVerifier() {
        return new HostKeyVerifier() {
            @Override
            public boolean verify(String arg0, int arg1, PublicKey arg2) {
                return true;
            }
        };
    }

}