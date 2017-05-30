import processing.core.PApplet;
import processing.core.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
    private PImage frame = null;
    private PImage blendFrame = null;
    private boolean connected = false;
    private Map<Character, Key> keyMap;
    private boolean filterOn = false;
    private boolean blendOn = false;

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
        if (connected) {
            if (frame != null) {
                if (filterOn) {
                    if (blendOn) {
                        blendFrame = frame.copy();
                    }

                    //convertToGrey(frame);
                    //frame = applyLOG(frame);
                    //frame = applyL(frame);
                    //frame.filter(BLUR);
                    //boolean[][] matrix = convertToBinary(frame, 64);
                    //matrix = cleanFilter(matrix);
                    //matrix = erodeFilter(matrix);
                    //matrix = dilateFilter(matrix);
                    //matrix = dilateFilter(matrix);
                    //frame = convertToPImage(matrix);

                    if (blendOn) {
                        frame.blend(blendFrame, 0, 0, frame.width, frame.height, 0, 0, blendFrame.width, blendFrame.height, LIGHTEST);
                    }
                }

                image(frame, 0, 0, width, height);
                frame = null;
            }
        } else {
            background(0);
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

        while (toSend != 'y') {
            toSend = 'z';
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
            } else if (Key.blendToggle.wasReleased()) {
                blendOn = !blendOn;
            } else {
                toSend = 'y';
            }

            if (connected && toSend != 'z' && toSend != 'y') {
                try {
                    writer.write(toSend);
                    writer.newLine();
                    writer.flush();
                    System.out.print(toSend);
                } catch (IOException e) {
                    System.out.println("Disconnected: " + e.getMessage());
                    setConnected(false);
                }
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
        keyMap.put('v', Key.blendToggle);
    }

    private void convertToGrey(PImage image) {
        image.filter(PApplet.GRAY); //turn to gray scale
    }

    private boolean[][] convertToBinary(PImage image, int threshold) {
        boolean[][] result = new boolean[image.height][image.width];
        image.loadPixels();

        for (int r = 0; r < image.height; r++) {
            for (int c = 0; c < image.width; c++) {
                result[r][c] = red(image.pixels[c + image.width * r]) > threshold;
            }
        }

        return result;
    }

    private PImage convertToPImage(boolean[][] image) {
        PImage result = new PImage(image[0].length, image.length);
        result.loadPixels();

        for (int r = 0; r < result.height; r++) {
            for (int c = 0; c < result.width; c++) {
                result.pixels[c + result.width * r] = (image[r][c]) ? color(255) : color(0);
            }
        }

        result.updatePixels();
        return result;
    }

    private static final float[][] logMatrix = {
            { 0, 0,  1 , 0, 0 },
            { 0, 1,  2 , 1, 0 },
            { 1, 2, -16, 2, 1 },
            { 0, 1,  2 , 1, 0 },
            { 0, 0,  1 , 0, 0 }
    };

    private static final float[][] lMatrix = {
            { 0,  1,  0 },
            { 1, -4,  1 },
            { 0,  1,  0 }
    };

    private PImage applyL(PImage image) {
        PImage newImage = new PImage(image.width, image.height);
        newImage.loadPixels();
        image.loadPixels();

        for (int x = 0; x < image.width; x++) {
            for (int y = 0; y < image.height; y++) {
                newImage.pixels[x + y * image.width] = convolutionRGB(x, y, lMatrix, lMatrix.length, image);
            }
        }

        newImage.updatePixels();
        return newImage;
    }

    private PImage applyLOG(PImage image) {
        PImage newImage = new PImage(image.width, image.height);
        newImage.loadPixels();
        image.loadPixels();

        for (int x = 0; x < image.width; x++) {
            for (int y = 0; y < image.height; y++) {
                newImage.pixels[x + y * image.width] = convolutionRGB(x, y, logMatrix, logMatrix.length, image);
            }
        }

        newImage.updatePixels();
        return newImage;
    }

    private int convolutionRGB(int x, int y, float[][] matrix, int matrixSize, PImage image) {
        float rTotal = (float) 0.0;
        float gTotal = (float) 0.0;
        float bTotal = (float) 0.0;
        int offset = matrixSize / 2;

        for (int i = 0; i < matrixSize; i++){
            for (int j= 0; j < matrixSize; j++){
                // What pixel are we testing
                int xloc = x+i-offset;
                int yloc = y+j-offset;
                int loc = xloc + image.width*yloc;

                // Make sure we haven't walked off our image, we could do better here
                loc = constrain(loc,0,image.pixels.length-1);

                // Calculate the convolution
                rTotal += (red(image.pixels[loc]) * matrix[i][j]);
                gTotal += (green(image.pixels[loc]) * matrix[i][j]);
                bTotal += (blue(image.pixels[loc]) * matrix[i][j]);
            }
        }

        // Make sure RGB is within range
        rTotal = constrain(rTotal, 0, 255);
        gTotal = constrain(gTotal, 0, 255);
        bTotal = constrain(bTotal, 0, 255);

        // Return the resulting color
        return color(rTotal, gTotal, bTotal);
    }


    public boolean[][] dilateFilter(boolean[][] image) {
        final int[] rVal = {0, 1, 2, 1};
        final int[] cVal = {1, 0, 1, 2};
        boolean[][] result = new boolean[image.length][image[0].length];
        boolean centerVal;
        boolean otherVal;

        for (int r = 0; r < image.length; r++) {
            for (int c = 0; c < image[r].length; c++) {
                centerVal = image[r][c];

                for (int i = 0; i < rVal.length && !centerVal; i++) {
                    for (int j = 0; j < cVal.length && !centerVal; j++) {
                        int rLoc = constrain(r+rVal[i]-1, 0, image.length-1);
                        int cLoc = constrain(c+cVal[j]-1,0, image[rLoc].length-1);
                        otherVal = image[rLoc][cLoc];

                        if (otherVal) {
                            centerVal = true;
                        }
                    }
                }

                result[r][c] = centerVal;
            }
        }

        return result;
    }

    public boolean[][] erodeFilter(boolean[][] image) {
        final int[] rVal = {0, 1, 2, 1};
        final int[] cVal = {1, 0, 1, 2};
        boolean[][] result = new boolean[image.length][image[0].length];
        boolean centerVal;
        boolean otherVal;

        for (int r = 0; r < image.length; r++) {
            for (int c = 0; c < image[r].length; c++) {
                centerVal = image[r][c];

                for (int i = 0; i < rVal.length && !centerVal; i++) {
                    for (int j = 0; j < cVal.length && !centerVal; j++) {
                        int rLoc = constrain(r+rVal[i]-1, 0, image.length-1);
                        int cLoc = constrain(c+cVal[j]-1,0, image[rLoc].length-1);
                        otherVal = image[rLoc][cLoc];

                        if (!otherVal) {
                            centerVal = false;
                        }
                    }
                }

                result[r][c] = centerVal;
            }
        }

        return result;
    }

    public boolean[][] cleanFilter(boolean[][] image) {
        boolean[][] result = new boolean[image.length][image[0].length];
        boolean centerVal;
        boolean otherVal;
        boolean hasNeighbour;

        for (int r = 0; r < image.length; r++) {
            for (int c = 0; c < image[r].length; c++) {
                centerVal = image[r][c];
                hasNeighbour = false;

                for (int i = r - 1; i < r + 1; i++) {
                    for (int j = c - 1; j < c + 1; j++) {
                        if (i != r && j != c) {
                            int rLoc = constrain(i, 0, image.length - 1);
                            int cLoc = constrain(j, 0, image[rLoc].length - 1);
                            otherVal = image[rLoc][cLoc];

                            if (otherVal) {
                                hasNeighbour = true;
                            }
                        }
                    }
                }

                if (!hasNeighbour) {
                    result[r][c] = false;
                } else {
                    result[r][c] = centerVal;
                }
            }
        }

        return result;
    }

    private PImage applyErode(PImage image) {
        PImage newImage = new PImage(image.width, image.height);
        newImage.loadPixels();
        image.loadPixels();

        for (int x = 0; x < image.width; x++) {
            for (int y = 0; y < image.height; y++) {
                newImage.pixels[x + y * image.width] = erode(x, y, image);
            }
        }

        newImage.updatePixels();
        return newImage;
    }

    private int erode(int x, int y, PImage img) {
        final int[] rVal = {0, 1, 2, 1};
        final int[] cVal = {1, 0, 1, 2};
        boolean centerVal = getbinary(x + y*img.width, img);

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

    private PImage applyDilate(PImage image) {
        PImage newImage = new PImage(image.width, image.height);
        newImage.loadPixels();
        image.loadPixels();

        for (int x = 0; x < image.width; x++) {
            for (int y = 0; y < image.height; y++) {
                newImage.pixels[x + y * image.width] = dilate(x, y, image);
            }
        }

        newImage.updatePixels();
        return newImage;
    }

    private int dilate(int x, int y, PImage img) {
        final int[] rVal = {0, 1, 2, 1};
        final int[] cVal = {1, 0, 1, 2};
        boolean centerVal = getbinary(x + y*img.width, img);

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

    private int getColour(boolean value) {
        if (value) {
            return color(255, 255, 255);
        } else {
            return color(0, 0, 0);
        }
    }

    private boolean getbinary(int loc, PImage img) {
        return red(img.pixels[loc]) > 64;
    }

}
