package libs.libCore.modules;

import net.schmizz.sshj.SSHClient;
import java.io.IOException;
import java.util.HashMap;

public class SshClientObjectPool extends AbstractObjectPool<SSHClient> {

    SshClientObjectPool(){
        this.available = new HashMap<>();
        this.inUse = new HashMap<>();
    }

    @Override
    protected SSHClient create(String node) {return new SshClientFactory().create(node);}

    @Override
    protected void close(SSHClient instance) {
        if ( validate(instance) && instance.isConnected()){
            try {
                instance.disconnect();
            } catch (IOException e) {
                 Log.error(e.getMessage());
            }
        }
    }

}