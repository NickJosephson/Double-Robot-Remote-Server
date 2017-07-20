import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Nicholas on 2017-05-30.
 */
public class OutputThread extends Thread{
    private BufferedWriter writer;
    private Sketch sketch;
    private boolean keepFetching = true;

    public OutputThread(BufferedWriter writer, Sketch sketch) {
        super("OutputThread");
        this.writer = writer;
        this.sketch = sketch;
    }

    public void run() {
        while (keepFetching) {
            handleKeys();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void handleKeys() {
        String toSend = null;
        String toLog = null;

        if (Key.park.wasPressed()) {
            toSend = "p";
            toLog = "Park (\"p\")";
        } else if (Key.stop.wasPressed()) {
            toSend = "x";
            toLog = "Stop All (\"x\")";
        } else if (Key.forward.wasReleased()) {
            toSend = "s";
            toLog = "Stop driving (\"s\")";
        } else if (Key.back.wasReleased()) {
            toSend = "s";
            toLog = "Stop driving (\"s\")";
        } else if (Key.left.wasReleased()) {
            toSend = "t";
            toLog = "Stop turning (\"t\")";
        } else if (Key.right.wasReleased()) {
            toSend = "t";
            toLog = "Stop turning (\"t\")";
        } else if (Key.forward.wasPressed()) {
            toSend = "f";
            toLog = "Move forward (\"f\")";
        } else if (Key.back.wasPressed()) {
            toSend = "b";
            toLog = "Move backward (\"b\")";
        } else if (Key.left.wasPressed()) {
            toSend = "l";
            toLog = "Move left (\"l\")";
        } else if (Key.right.wasPressed()) {
            toSend = "r";
            toLog = "Move right (\"r\")";
        } else if (Key.up.wasReleased()) {
            toSend = "h";
            toLog = "Pole stop (\"h\")";
        } else if (Key.up.wasPressed()) {
            toSend = "u";
            toLog = "Pole up (\"u\")";
        } else if (Key.down.wasReleased()) {
            toSend = "h";
            toLog = "Pole stop (\"h\")";
        } else if (Key.down.wasPressed()) {
            toSend = "d";
            toLog = "Pole down (\"d\")";
        }

        if (toSend != null) {
            try {
                writer.write(toSend);
                writer.flush();
                System.out.println("Command sent: "+ toLog);
            } catch (IOException e) {
                System.out.println("Disconnected: " + e.getMessage());
                keepFetching = false;
                sketch.setConnected(false);
            }
        }
    }
}
