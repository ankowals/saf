package libs.libCore.modules;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.regexp;

@SuppressWarnings("unchecked")
public class SshCore {

    private Context scenarioCtx;
    private SshClientObjectPool sshClientObjectPool;

    private String stdOut;
    private String stdErr;
    private Integer exitCode;

    public SshCore() {
        this.scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.sshClientObjectPool = GlobalCtxSingleton.getInstance().get("SshClientObjectPool", SshClientObjectPool.class);
    }

    public String getStdOut(){
        return stdOut;
    }

    public String getStdErr(){
        return stdErr;
    }

    public Integer getExitCode(){
        return exitCode;
    }

    /**
     * Starts Ssh session that can be used for single command execution
     * helper function used by execute
     *
     */
    private Session startSession(SSHClient client) {
        try {
            Session session = client.startSession();
            session.allocateDefaultPTY();

            return session;
        } catch (ConnectionException | TransportException e) {
            Log.error(e.getMessage());
        }

        return null;
    }

    private void stopSession(Session session){
        try {
            session.close();
        } catch (ConnectionException | TransportException e) {
            Log.error(e.getMessage());
        }
    }


    /**
     * Executes a single command in a session. For each command new session is open and closed
     * after command execution
     *
     * @param cmd, String, command to execute
     * @param timeout, Integer, timeout
     *
     * @return SshResult, result set that contains stdout, stderr and exit status code
     */
    public void execute(String node, String cmd, Integer timeout) {
        SSHClient client = sshClientObjectPool.checkOut(node);
        Session session = startSession(client);
        try {
            Log.debug("Going to execute following command via ssh " + cmd);
            Session.Command command = session.exec(cmd);
            command.join(timeout, TimeUnit.SECONDS);
            exitCode = command.getExitStatus();
            stdOut = IOUtils.readFully(command.getInputStream()).toString();
            stdErr = IOUtils.readFully(command.getErrorStream()).toString();

        } catch (IOException e) {
            Log.error(e.getMessage());
        } finally {
            stopSession(session);
            sshClientObjectPool.checkIn(node, client);
        }

    }


    /**
     * Checks that particular file exists on remote host
     *
     * @param node, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToFile, String, path to the file on remote host
     *
     * @return Boolean, true if file exits, false otherwise
     */
    public Boolean checkThatFileExists(String node, String pathToFile) {
        execute(node, "test -e " + pathToFile,60);
        if ( getExitCode() == 0 ) {
            return true;
        }

        return false;
    }


    /**
     * Waits for a file to be present on a remote host for defined time duration
     *
     * @param node, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToFile, String, path to the file on remote host
     * @param timeout, Integer, timeout
     *
     * @return Boolean, true if file exists, false otherwise
     */
    public Boolean waitForFile(String node, String pathToFile, Integer timeout) {
        if ( timeout < 0 ) {
            return false;
        }
        Boolean result = checkThatFileExists(node, pathToFile);
        if ( result.equals(false) ) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //don't do anything
            }
            return waitForFile(node, pathToFile, timeout - 1);
        }

        return result;
    }


    /**
     * Checks that node is accessible
     *
     * @param node, String, node name as defined in configuration Environment.Active.Ssh.node
     *
     * @return Boolean, true if alive, false otherwise
     */
    public Boolean checkThatNodeIsAlive(String node){
        execute(node, "echo alive", 60);
        if ( getExitCode() == 0 ) {
            return true;
        }

        return false;
    }


    /**
     * Downloads file from remote node using scp
     *
     * @param node, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToFileOnRemote, String, path to the file on remote host
     * @param pathToLocalDir, String, path to the directory where file shall be downloaded
     *
     * @return File, file handle
     */
    public File downloadFileViaScp(String node, String pathToFileOnRemote, String pathToLocalDir){
        SSHClient client = sshClientObjectPool.checkOut(node);
        String fileName = FilenameUtils.getName(pathToFileOnRemote);

        try {
            Log.debug("Downloading file " + pathToFileOnRemote + " to " + pathToLocalDir + " via scp");
            client.newSCPFileTransfer().download(pathToFileOnRemote, new FileSystemFile(pathToLocalDir));
        } catch (IOException e) {
            Log.error(e.getMessage());
        } finally {
            sshClientObjectPool.checkIn(node, client);
        }

        return new File(pathToLocalDir + File.separator + fileName);
    }


    /**
     * Uploads file via scp
     *
     * @param node, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToLocalFile, String, path to the file on local host
     * @param pathToUploadDirOnRemote, String, path to the directory where file shall be uploaded on remote host
     *
     * @return Boolean, true if upload was successful, false otherwise
     */
    public Boolean uploadFileViaScp(String node, String pathToLocalFile, String pathToUploadDirOnRemote) {
        SSHClient client = sshClientObjectPool.checkOut(node);

        try {
            Log.debug("Uploading file " + pathToLocalFile + " to " + pathToUploadDirOnRemote + " via scp");
            client.newSCPFileTransfer().upload(new FileSystemFile(pathToLocalFile), pathToUploadDirOnRemote);

            return true;

        } catch (IOException e) {
            Log.error(e.getMessage());
        } finally {
            sshClientObjectPool.checkIn(node, client);
        }

        return false;
    }


    /**
     * Downloads file from remote node using sftp
     *
     * @param node, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToFileOnRemote, String, path to the file on remote host
     * @param pathToLocalDir, String, path to the directory where file shall be downloaded
     *
     * @return File, file handle
     */
    public File downloadFileViaSftp(String node, String pathToFileOnRemote, String pathToLocalDir) {
        SSHClient client = sshClientObjectPool.checkOut(node);
        String fileName = FilenameUtils.getName(pathToFileOnRemote);

        try {
            SFTPClient sftp = client.newSFTPClient();
            Log.debug("Downloading file " + pathToFileOnRemote + " to " + pathToLocalDir + " via sftp");
            sftp.get(pathToFileOnRemote, new FileSystemFile(pathToLocalDir));
            sftp.close();
        } catch (IOException e) {
            Log.error(e.getMessage());
        } finally {
            sshClientObjectPool.checkIn(node, client);
        }

        return new File(pathToLocalDir + File.separator + fileName);
    }


    /**
     * Uploads file via sftp
     *
     * @param node, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param pathToLocalFile, String, path to the file on local host
     * @param pathToUploadDirOnRemote, String, path to the directory where file shall be uploaded on remote host
     *
     * @return Boolean, true if upload was successful, false otherwise
     */
    public Boolean uploadFileViaSftp(String node, String pathToLocalFile, String pathToUploadDirOnRemote) {
        SSHClient client = sshClientObjectPool.checkOut(node);

        try {
            SFTPClient sftp = client.newSFTPClient();
            Log.debug("Uploading file " + pathToLocalFile + " to " + pathToUploadDirOnRemote + " via sftp");
            sftp.put(new FileSystemFile(pathToLocalFile), pathToUploadDirOnRemote);
            sftp.close();

            return true;

        } catch (IOException e) {
            Log.error(e.getMessage());
        } finally {
            sshClientObjectPool.checkIn(node, client);
        }

        return false;
    }


    /**
     * Starts interactive shell
     *
     * @param timeout, Integer, timeout used for supervision of each command
     */
    public void startShell(String node, Integer timeout){
        SSHClient client = sshClientObjectPool.checkOut(node);
        Session session = startSession(client);
        try {
            Session.Shell shell = session.startShell();
            Expect expect = new ExpectBuilder()
                    .withOutput(shell.getOutputStream())
                    .withInputs(shell.getInputStream(), shell.getErrorStream())
                    .withEchoInput(System.err)
                    .withEchoOutput(System.out)
                    .withTimeout(timeout, TimeUnit.SECONDS)
                    .withInputFilters(removeColors(), removeNonPrintable())
                    .withExceptionOnFailure()
                    .build();

            scenarioCtx.put("SshExpect_" + node, Expect.class, expect);
            scenarioCtx.put("SshShellClient_" + node, SSHClient.class, client);

        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }


    /**
     * Executes a command in am interactive shell. Shell has to be open to make use of this method.
     * It shall be closed when all commands are executed using separate method.
     *
     * @param node, String, node name as defined in configuration Environment.Active.Ssh.node
     * @param cmd, String, command to execute
     * @param expectedOutput, String, expected output in stdout, it can be prompt or string
     */
    public void executeInShell(String node, String cmd, String expectedOutput) {
        Expect expect = scenarioCtx.get("SshExpect_" + node, Expect.class);
        if ( expect == null ){
            Log.error("Expect object null or empty! Please make sure that interactive shall was started!");
        }
        try {
            Log.debug("Command to execute via interactive ssh shell is " + cmd);
            expect.sendLine(cmd);
            stdOut  = expect.expect(regexp(expectedOutput)).getInput();
            stdErr = "";
            exitCode = 0;
            Log.debug(stdOut);
        } catch (Exception e) {
            stopShell(node);
            Log.error(e.getMessage());
        }
    }


    /**
     * Closes interactive shell
     */
    public void stopShell(String node) {
        Expect expect = scenarioCtx.get("SshExpect_" + node, Expect.class);
        SSHClient client = scenarioCtx.get("SshShellClient_" + node, SSHClient.class);
        if ( expect == null ){
            Log.error("Expect object null or empty! Please make sure that interactive shall was started!");
        }
        try {
            expect.close();
        } catch (IOException e) {
            Log.error(e.getMessage());
        } finally {
            sshClientObjectPool.checkIn(node, client);
        }
    }

}