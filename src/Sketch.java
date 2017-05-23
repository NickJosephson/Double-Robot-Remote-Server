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
    private char[] commands = {'f', 'b', 'l', 'r', 'p', 'u', 'd', 'x', 's', 'h'};
    private char[] keyChars = {'w', 's', 'a', 'd', 'p', 'u', 'j', 'x'};
    private boolean[] keysDown = new boolean[keyChars.length];
    private Map<Character, String>

    static public void main(String[] args) {
        String[] appletArgs = new String[]{"Sketch"};
        PApplet.main(appletArgs);
    }

    public void settings() {
        size((int)(640*1.5), (int)(480*1.5));
    }

    public void setup() {
        frameRate(30);
        background(0);
        setupServer();
        setConnected(false);
    }

    public void draw() {
        background(0);
        if (connected) {
            if (frame != null) {
                drawFrame(frame);
            }
        } else {
            textSize(50);
            text("Not connected", 10, 50);
            //textSize(20);
            //text("Port: " + DEFAULT_PORT, 12, 80);
        }
    }

    private float[][] gMatrix = {
        { 0, 0,  1 , 0, 0 },
        { 0, 1,  2 , 1, 0 },
        { 1, 2, -16, 2, 1 },
        { 0, 1,  2 , 1, 0 },
        { 0, 0,  1 , 0, 0 }
    };

    private void drawFrame(PImage toDraw) {
        PImage newImage = new PImage(toDraw.width, toDraw.height); //createImage(toDraw.width, toDraw.height, JPEG);
        newImage.loadPixels();
        toDraw.filter(GRAY); //turn to gray scale
        toDraw.loadPixels();

        for (int x = 0; x < toDraw.width; x++) {
            for (int y = 0; y < toDraw.height; y++ ) {
                newImage.pixels[x + y*toDraw.width] = convolution(x, y, gMatrix, gMatrix.length, toDraw);
            }
        }

        newImage.updatePixels();
        image(newImage, 0, 0, width, height);
    }

    private int convolution(int x, int y, float[][] matrix, int matrixsize, PImage img) {
        float rtotal = (float) 0.0;
        float gtotal = (float) 0.0;
        float btotal = (float) 0.0;
        int offset = matrixsize / 2;

        for (int i = 0; i < matrixsize; i++){
            for (int j= 0; j < matrixsize; j++){
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

    public void keyReleased() {
        if (connected) {
            if (lastKey != 'x' && lastKey != 'p') {
                try {
                    writer.write('x');
                    writer.newLine();
                    writer.flush();
                    lastKey = 'x';
                    System.out.println("   x");
                    //System.out.println(toSend);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void keyPressed() {
        keysDown


        switch (key) {
            case 'w':
                keysDown[0] = 'f';
                break;
            case 's':
                toSend = 'b';
                break;
            case 'a':
                toSend = 'l';
                break;
            case 'd':
                toSend = 'r';
                break;
            case 'p':
                toSend = 'p';
                break;
            case 'u':
                toSend = 'u';
                break;
            case 'j':
                toSend = 'd';
                break;
            case 'h':
                toSend = 'h';
                break;
        }




    }

}


/*


                switch (key) {
                    case 'w':
                        toSend = 'f';
                        break;
                    case 's':
                        toSend = 'b';
                        break;
                    case 'a':
                        toSend = 'l';
                        break;
                    case 'd':
                        toSend = 'r';
                        break;
                    case 'p':
                        toSend = 'p';
                        break;
                    case 'u':
                        toSend = 'u';
                        break;
                    case 'j':
                        toSend = 'd';
                        break;
                    case 'h':
                        toSend = 'h';
                        break;
                }


            }
        }
    }
if (toSend != 'z') {
                    try {
                        writer.write(toSend);
                        writer.newLine();
                        writer.flush();
                        lastKey = key;
                        System.out.println(toSend);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
    private void updateMouse() {

        char toSend = 'x';

        if (mousePressed) {
            if (mouseY < height / 3) {
                toSend = 'f';
            } else if (mouseY > 2 * (height / 3)) {
                toSend = 'b';
            } else if (mouseX < width / 3) {
                toSend = 'l';
            } else if (mouseX > 2 * (width / 3)) {
                toSend = 'r';
            }
        } else {
            toSend = 'x';
        }

        if (lastKey != toSend) {
            try {
                writer.writeChar(toSend);
                writer.writeChar('\n');
                writer.flush();
                lastKey = toSend;
                System.out.println(toSend);
            } catch (IOException e) {
                System.out.println("Disconnected: "+ e.getMessage());
                connected = false;
            }
        }
    }

textSize(50);
        text("Not connected", 10, 50);

/noFill();
        //rect(width/3, height/3, (width/3),(height/3));
        char toSend = 'x';

        if (mousePressed) {
            toSend = 'p';
        } else if (mouseY < height/3) {
            toSend = 'f';
        } else if (mouseY > 2*(height/3)) {
            toSend = 'b';
        } else if (mouseX < width/3) {
            toSend = 'l';
        } else if (mouseX > 2*(width/3)) {
            toSend = 'r';
        }

        if (lastKey != toSend) {
            try {
                writer.write(toSend);
                writer.write('\n');
                writer.flush();
                lastKey = toSend;
                System.out.println(toSend);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        */


/*p

    while (true) {
            try {
                // check for input request type

                try {
                    fileOut = new FileWriter("img.txt");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                int ch = reader.read();

                if (ch != -1) {

                    while (ch != -1) {
                        fileOut.write(parseChar(ch));

                        ch = reader.read();
                    }
                }

                fileOut.flush();
                fileOut.close();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            frame = loadImage("img.jpg");
            image(frame, 0, 0, 1280, 960);

            if (frame.width > 0) {
                // Image is ready to go, draw it
                //frame = requestImage("img.jpg");
            }
        }

        try {
            // check for input request type

            String line = reader.readLine();

            while (line != null) {

                fileOut.write(line);

                line = reader.readLine();
            }

            fileOut.flush();
            fileOut.close();

            try {
                fileOut = new FileWriter("img.jpg");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (frame.width > 0) {
            // Image is ready to go, draw it
            image(frame, 0, 0, 1280,960);
            frame = requestImage("img.jpg");
        }
        */