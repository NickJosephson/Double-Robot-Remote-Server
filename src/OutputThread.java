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
        char toSend = 'z';

        //while (toSend != 'y') {
            //toSend = 'z';
            if (Key.park.wasPressed()) {
                toSend = 'p';
            } else if (Key.stop.wasPressed()) {
                toSend = 'x';
            } else if (Key.forward.wasReleased()) {
                toSend = 's';
            } else if (Key.back.wasReleased()) {
                toSend = 's';
            } else if (Key.left.wasReleased()) {
                toSend = 't';
            } else if (Key.right.wasReleased()) {
                toSend = 't';
            } else if (Key.forward.wasPressed()) {
                toSend = 'f';
            } else if (Key.back.wasPressed()) {
                toSend = 'b';
            } else if (Key.left.wasPressed()) {
                toSend = 'l';
            } else if (Key.right.wasPressed()) {
                toSend = 'r';
            } else if (Key.up.wasReleased()) {
                toSend = 'h';
            } else if (Key.up.wasPressed()) {
                toSend = 'u';
            } else if (Key.down.wasReleased()) {
                toSend = 'h';
            } else if (Key.down.wasPressed()) {
                toSend = 'd';
            } else if (Key.filterToggle.wasReleased()) {
                //sketch.toggleFilter();
            } else if (Key.blendToggle.wasReleased()) {
                //sketch.toggleBlend();
            } else {
                toSend = 'y';
            }

            if (toSend != 'z' && toSend != 'y') {
                try {
                    writer.write(toSend);
                    writer.newLine();
                    writer.flush();
                    System.out.print(toSend);
                } catch (IOException e) {
                    System.out.println("Disconnected: " + e.getMessage());
                    keepFetching = false;
                    sketch.setConnected(false);
                }
            }
     //   }
    }
}
