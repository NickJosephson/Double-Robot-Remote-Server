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
    private PImage blendFrame = null;
    private boolean connected = false;
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
            background(0);
            if (frame != null) {
                image(frame, 0, 0, width, height);
            }
            /*
            if (frame != null) {
                if (filterOn) {
                    if (blendOn) {
                        blendFrame = frame.copy();
                    }

                    convertToGrey(frame);
                    frame = applyLOG(frame);

                    //boolean[][] matrix = convertToBinary(frame, 255 * (mouseX/width));

                    //matrix = applyClean(matrix);
                    //matrix = applyErode(matrix);
                    //matrix = applyDilate(matrix);
                    //matrix = applyDilate(matrix);
                    //matrix = applyErode(matrix);
                    //matrix = applyErode(matrix);

                    //frame = convertToPImage(matrix);

                    if (blendOn) {
                        frame.blend(blendFrame, 0, 0, frame.width, frame.height, 0, 0, blendFrame.width, blendFrame.height, LIGHTEST);
                    }
                }

                frame = null;
            }*/
        } else {
            background(0);
            textSize(50);
            text("Not connected", 10, 50);
            //textSize(20);
            //text("Port: " + DEFAULT_PORT, 12, 80);
        }

        //handleKeys();
    }

    public void setFrame(PImage newFrame) {
        frame = newFrame;
    }

    public void toggleFilter() {
        filterOn = !filterOn;
    }

    public void toggleBlend() {
        blendOn = !blendOn;
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
        System.out.println("up");
        Key toUP = keyMap.get(key);
        if (toUP != null) {
            toUP.setUp();
        }
    }

    public void keyPressed() {
        System.out.println("down");
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

    /*************************************
     *         Image Processing          *
     *************************************/

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

    private boolean[][] applyDilate(boolean[][] image) {
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

    private boolean[][] applyErode(boolean[][] image) {
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

    private boolean[][] applyClean(boolean[][] image) {
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

}
