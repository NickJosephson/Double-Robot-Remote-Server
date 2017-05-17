import processing.core.PApplet;
import processing.core.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

/**
 * Created by Nicholas on 2017-05-12.
 *
 */
public class Sketch extends PApplet {
    private static final int DEFAULT_PORT = 4000;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataInputStream reader;
    private DataOutputStream writer;
    private PImage frame;
    private boolean connected = false;

    static public void main(String[] args) {
        String[] appletArgs = new String[]{"Sketch"};
        PApplet.main(appletArgs);
    }

    public void settings() {
        size(640 , 480);
    }

    public void setup() {
        frameRate(30);
        background(0);
        setupServer();
    }

    public void draw() {
        if (connected) {
            updateFrame();
        } else {
            connect();
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
                header.append(parseChar(ch));
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
            connected = false;
        }

        //create and display image
        if (bytes != null) {
            frame = new PImage(new ImageIcon(bytes).getImage());
            image(frame, 0, 0, 640, 480);
        }
    }

    private void setupServer() {

        //JOptionPane prompt = new JOptionPane();
        //portNumber = prompt.
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void connect() {
        System.out.println("Server.java: waiting for connection");
        try {
            clientSocket = serverSocket.accept();
            reader = new DataInputStream(clientSocket.getInputStream());
            writer = new DataOutputStream(clientSocket.getOutputStream());

            connected = true;
            System.out.println("Connected to " + clientSocket.getInetAddress());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}


/*

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

    /*
    public void keyReleased() {
        if (lastKey != key) {
        char toSend = 'z';

        switch (key) {
            case 'w':
                toSend = 's';
                break;
            case 's':
                toSend = 's';
                break;
            case 'a':
                toSend = 'x';
                break;
            case 'd':
                toSend = 'x';
                break;
        }

        if (toSend != 'z') {

            try {
                writer.write('x');
                writer.write('\n');
                writer.flush();
                lastKey = 't';
                //System.out.println("   t");
                //System.out.println(toSend);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        //}
        //}
    }

    public void keyPressed() {
        if (key != lastKey) {
            char toSend = 'z';

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
            }

            if (toSend != 'z') {
                try {
                    writer.write(toSend);
                    writer.write('\n');
                    writer.flush();
                    lastKey = key;
                    System.out.println(toSend);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}




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