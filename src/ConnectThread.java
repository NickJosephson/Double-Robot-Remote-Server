import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by Nicholas on 2017-05-19.
 */
public class ConnectThread extends Thread {
    private ServerSocket serverSocket;
    private Sketch sketch;

    public ConnectThread(ServerSocket serverSocket, Sketch sketch) {
        super("ConnectThread");
        this.serverSocket = serverSocket;
        this.sketch = sketch;
    }

    public void run() {
        System.out.println("Server.java: waiting for connection");
        try {
            sketch.setStreams(serverSocket.accept());
        } catch (IOException e) {
            System.out.println("Connection error: "+ e.getMessage());
        }
    }

}
