import java.io.IOException;
import java.net.Socket;

public class ConnectionServer {

     public Socket socket;

    public Socket getSocket() throws IOException {
        if ( socket == null) {
            socket = new Socket("localhost", 8089);
        }
        return socket;
    }
}
