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
    private Socket clientSocket;
    private DataInputStream reader;
    private BufferedWriter writer;
    private PImage frame;
    private boolean connected = false;
    private char lastKey = 'z';
    private Map<Character, Key> keyMap;
    private boolean filterOn = false;

    static public void main(String[] args) {
        String[] appletArgs = new String[]{"Sketch"};
        PApplet.main(appletArgs);
    }

    public void settings() {
        size((int)(640*1.5), (int)(480*1.5));
    }

    public void setup() {
        frameRate(30);

        setupKeys();
        setupServer();
        setConnected(false);
    }

    public void draw() {
        //background(0);
        if (connected) {
            if (frame != null) {
                if (filterOn) {
                    frame = addFilter(frame, Filter.LOG, 1);
                    //frame = addFilter(frame, Filter.ERODE, 1);
                    frame = addFilter(frame, Filter.DILATE, 2);
                }

                image(frame, 0, 0, width, height);
                frame = null;
            }
        } else {
            textSize(50);
            text("Not connected", 10, 50);
            //textSize(20);
            //text("Port: " + DEFAULT_PORT, 12, 80);
        }

        handleKeys();
    }

    public void setConnected(boolean newVal) {
        connected = newVal;
        if (!newVal) {
            new ConnectThread(serverSocket, this).start();
        }
    }

    public void setFrame(PImage newFrame) {
        frame = newFrame;
    }

    private void setupServer() {
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setStreams(Socket newClientSocket) {
        try {
            clientSocket = newClientSocket;
            reader = new DataInputStream(clientSocket.getInputStream());
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            new InputThread(reader, this).start();

            setConnected(true);
            System.out.println("Connected to " + clientSocket.getInetAddress());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleKeys() {
        char toSend = 'z';

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
            filterOn = !filterOn;
        }

        if (connected && toSend != 'z') {
            try {
                writer.write(toSend);
                writer.newLine();
                writer.flush();
                System.out.print(toSend);
            } catch (IOException e) {
                System.out.println("Disconnected: "+ e.getMessage());
                setConnected(false);
            }
        }
    }

    public void keyReleased() {
        Key toUP = keyMap.get(key);
        if (toUP != null) {
            toUP.setUp();
        }
    }

    public void keyPressed() {
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
    }

    private static final float[][] logMatrix = {
            { 0, 0,  1 , 0, 0 },
            { 0, 1,  2 , 1, 0 },
            { 1, 2, -16, 2, 1 },
            { 0, 1,  2 , 1, 0 },
            { 0, 0,  1 , 0, 0 }
    };


    public enum Filter {
        DILATE,
        ERODE,
        LOG,
        GRAY
    }

    public PImage addFilter(PImage image, Filter filter, int times) {
        PImage newImage = new PImage(image.width, image.height); //createImage(toDraw.width, toDraw.height, JPEG);
        PImage temp;
        boolean firstTIme = true;

        if (filter == Filter.GRAY || filter == Filter.DILATE) {
            image.filter(PApplet.GRAY); //turn to gray scale
        }

        if (filter != Filter.GRAY) {
            newImage.loadPixels();
            image.loadPixels();
            for (int i = 0; i < times; i++) {
                if (!firstTIme) {
                    temp = image;
                    image = newImage;
                    newImage = temp;
                } else {
                    firstTIme = false;
                }

                for (int x = 0; x < image.width; x++) {
                    for (int y = 0; y < image.height; y++) {
                        switch (filter) {
                            case ERODE:
                                newImage.pixels[x + y * image.width] = erode(x, y, image);
                                break;
                            case DILATE:
                                newImage.pixels[x + y * image.width] = dilate(x, y, image);
                                break;
                            case LOG:
                                newImage.pixels[x + y * image.width] = convolution(x, y, logMatrix, logMatrix.length, image);
                                break;
                        }
                    }
                }
            }
        } else {
            return image;
        }

        newImage.updatePixels();
        return newImage;
    }

    private boolean getbinary(int loc, PImage img) {
        return red(img.pixels[loc]) > 64;
    }

    private int getColour(boolean value) {
        if (value) {
            return color(255, 255, 255);
        } else {
            return color(0, 0, 0);
        }
    }

    private int erode(int x, int y, PImage img) {
        boolean centerVal = getbinary(x + y*img.width, img);

        int[] rVal = {0, 1, 2, 1};
        int[] cVal = {1, 0, 1, 2};

        for (int i = 0; i < rVal.length && centerVal; i++) {
            for (int j= 0; j < cVal.length && centerVal; j++) {
                // What pixel are we testing
                int xloc = x+rVal[i]-1;
                int yloc = y+cVal[j]-1;
                int loc = xloc + img.width*yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = constrain(loc,0,img.pixels.length-1);
                boolean otherVal = getbinary(loc, img);

                if (!otherVal) {
                    centerVal = false;
                }
            }
        }

        return getColour(centerVal);
    }

    private int dilate(int x, int y, PImage img) {
        boolean centerVal = getbinary(x + y*img.width, img);

        int[] rVal = {0, 1, 2, 1};
        int[] cVal = {1, 0, 1, 2};

        for (int i = 0; i < rVal.length && !centerVal; i++) {
            for (int j= 0; j < cVal.length && !centerVal; j++) {
                // What pixel are we testing
                int xloc = x+rVal[i]-1;
                int yloc = y+cVal[j]-1;
                int loc = xloc + img.width*yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = constrain(loc,0,img.pixels.length-1);
                boolean otherVal = getbinary(loc, img);

                if (otherVal) {
                    centerVal = true;
                }
            }
        }

        return getColour(centerVal);
    }

    private int convolution(int x, int y, float[][] matrix, int matrixSize, PImage img) {
        float rtotal = (float) 0.0;
        float gtotal = (float) 0.0;
        float btotal = (float) 0.0;
        int offset = matrixSize / 2;

        for (int i = 0; i < matrixSize; i++){
            for (int j= 0; j < matrixSize; j++){
                // What pixel are we testing
                int xloc = x+i-offset;
                int yloc = y+j-offset;
                int loc = xloc + img.width*yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = constrain(loc,0,img.pixels.length-1);
                // Calculate the convolution
                rtotal += (red(img.pixels[loc]) * matrix[i][j]);
                gtotal += (green(img.pixels[loc]) * matrix[i][j]);
                btotal += (blue(img.pixels[loc]) * matrix[i][j]);
            }
        }
        // Make sure RGB is within range
        rtotal = constrain(rtotal, 0, 255);
        gtotal = constrain(gtotal, 0, 255);
        btotal = constrain(btotal, 0, 255);
        // Return the resulting color
        return color(rtotal, gtotal, btotal);
    }


}
