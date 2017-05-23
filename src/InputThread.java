import processing.core.PApplet;
import processing.core.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

/**
 * Created by Nicholas on 2017-05-19.
 */
public class InputThread extends Thread {
    private DataInputStream reader;
    private Sketch sketch;
    private boolean keepFetching = true;

    public InputThread(DataInputStream reader, Sketch sketch) {
        super("InputThread");
        this.reader = reader;
        this.sketch = sketch;
    }

    public void run() {
        while (keepFetching) {
            updateFrame();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                keepFetching = false;
                System.out.println(e.getMessage());
            }
        }
    }

    private void updateFrame() {
        StringBuilder header = new StringBuilder();
        int bytesReceived = 0;
        byte[] bytes = null;

        try {
            //parse byte count from header
            byte ch = reader.readByte();
            while (ch != '\n') {
                header.append(PApplet.parseChar(ch));
                ch = reader.readByte();
            }
            int byteCount = Integer.parseInt(header.toString());

            //fill array with image data
            bytes = new byte[byteCount];
            while (bytesReceived < byteCount) {
                int result = reader.read(bytes, bytesReceived, byteCount - bytesReceived);
                if (result > 0) {
                    bytesReceived += result;
                } else {
                    throw new IOException("Disconnected: stream ended");
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected: "+ e.getMessage());
            sketch.setConnected(false);
            keepFetching = false;
        }

        //create and display image
        if (bytes != null) {
            sketch.setFrame(new PImage(new ImageIcon(bytes).getImage()));
        }
    }
}
