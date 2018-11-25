package libs.libCore.modules;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import java.io.IOException;
import java.security.PublicKey;

public class SshClientFactory {

    private Context scenarioCtx;
    private Storage Storage;
    private StepCore StepCore;

    public SshClientFactory() {
        this.scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.Storage = scenarioCtx.get("Storage",Storage.class);
        this.StepCore = scenarioCtx.get("StepCore",StepCore.class);
    }

    public SSHClient create(String node) {
        String address = Storage.get("Environment.Active.Ssh." + node + ".host");
        Integer port = Storage.get("Environment.Active.Ssh." + node + ".port");
        String user = Storage.get("Environment.Active.Ssh." + node + ".user");
        String passwd = Storage.get("Environment.Active.Ssh." + node + ".password");

        if (address == null) {
            Log.error("Environment.Active.Ssh. " + node + ".host " + " is null or empty!");
        }
        if (port == null) {
            Log.error("Environment.Active.Ssh. " + node + ".port " + " is null or empty!");
        }
        if (user == null) {
            Log.error("Environment.Active.Ssh. " + node + ".user " + " is null or empty!");
        }
        if (passwd == null) {
            Log.error("Environment.Active.Ssh. " + node + ".password " + " is null or empty!");
        }

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding ){
            passwd = StepCore.decodeString(passwd);
        }

        try {
            SSHClient instance = new SSHClient();
            instance.addHostKeyVerifier(dummyHostKeyVerifier());
            instance.connect(address, port);
            instance.authPassword(user, passwd);
            Log.debug("Connected via ssh to " + node + " as " + user + " on " + address + " and port " + port);

            return instance;

        } catch (
                IOException e) {
            Log.error("Unable to connect via ssh to " + node + " as " + user
                    + " on " + address + " and port " + port + "! " + e.getMessage());
        }

        return null;
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