import processing.core.PApplet;
import processing.core.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nicholas on 2017-05-12.
 *
 */
public class Sketch extends PApplet {
    private static final int DEFAULT_PORT = 4000;

    private ServerSocket serverSocket;
    private Map<Character, Key> keyMap;
    private PImage frame = null;
    private boolean connected = false;

    static public void main(String[] args) {
        String[] appletArgs = new String[]{"Sketch"};
        PApplet.main(appletArgs);
    }

    public void settings() {
        size((int) (640 * 1.5), (int) (480 * 1.5));
    }

    public void setup() {
        frameRate(15);

        setupKeys();
        setupServer();
        setConnected(false);
    }

    public void draw() {
        background(0);
        if (connected) {
            if (frame != null) {
                image(frame, 0, 0, width, height);
            }
        } else {
            textSize(50);
            text("Not connected", 10, 50);
            textSize(20);
            text("Port: " + DEFAULT_PORT, 12, 80);
        }
    }

    public void setFrame(PImage newFrame) {
        frame = newFrame;
    }

    /*************************************
     *            Networking             *
     *************************************/

    private void setupServer() {
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setStreams(Socket newClientSocket) {
        try {
            Socket clientSocket = newClientSocket;
            DataInputStream reader = new DataInputStream(clientSocket.getInputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            new InputThread(reader, this).start();
            new OutputThread(writer, this).start();

            setConnected(true);
            System.out.println("Connected to " + clientSocket.getInetAddress());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void setConnected(boolean newVal) {
        connected = newVal;
        if (!newVal) {
            new ConnectThread(serverSocket, this).start();
        }
    }

    /*************************************
     *             Keyboard              *
     *************************************/

    public void keyReleased() {
        //System.out.println("up");
        Key toUP = keyMap.get(key);
        if (toUP != null) {
            toUP.setUp();
        }
    }

    public void keyPressed() {
        //System.out.println("down");
        Key toDown = keyMap.get(key);
        if (toDown != null) {
            toDown.setDown();
        }
    }

    private void setupKeys() {
        keyMap = new HashMap<>();
        keyMap.put('w', Key.forward);
        keyMap.put('a', Key.left);
        keyMap.put('s', Key.back);
        keyMap.put('d', Key.right);
        keyMap.put('p', Key.park);
        keyMap.put('x', Key.stop);
        keyMap.put('u', Key.up);
        keyMap.put('j', Key.down);
        keyMap.put('f', Key.filterToggle);
        keyMap.put('v', Key.blendToggle);
    }

}
