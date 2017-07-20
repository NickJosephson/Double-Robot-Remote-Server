import processing.core.PApplet;
import org.opencv.core.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;

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
            Image image = new ImageIcon(bytes).getImage();

            // Create a buffered image without transparency
            BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);

            // Draw the image on to the buffered image
            Graphics2D bGr = bimage.createGraphics();
            bGr.drawImage(image, 0, 0, null);
            bGr.dispose();

            // Create Mat(rix) and cop raster data to it
            Mat mat = new Mat(bimage.getHeight(), bimage.getWidth(), CvType.CV_8UC3);
            byte[] data = ((DataBufferByte) bimage.getRaster().getDataBuffer()).getData();
            mat.put(0, 0, data);

            // Update sketch Frame Mat
            sketch.setFrame(mat); //new PImage(image));
        }
    }
}
