import controlP5.*;
import processing.core.PApplet;
import processing.core.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nicholas on 2017-05-12.
 *
 */

public class Sketch extends PApplet implements ControlListener {
    public static ControlP5 sharedCP5 = null;
    public static PApplet sharedSketch = null;

    private static final int DEFAULT_PORT = 4000;
    private static final int FPS = 30;
    private static final int WINDOW_WIDTH = (int) (640 * 1.5);
    private static final int VIDEO_HEIGHT = (int) (480 * 1.5);
    private static final int WINDOW_HEIGHT = VIDEO_HEIGHT + 100;
    private static final int BACKGROUND_CLR = 0;
    private static final int NUM_FILTERS = 5;

    private ServerSocket serverSocket;
    private Map<Character, Key> keyMap;
    private PImage frameToDraw = null;
    private PImage frameToProcess = null;
    private boolean connected = false;
    private boolean applyFilters = false;

    private ControlP5 cp5;
    private ScrollableList[] dropdownLists = new ScrollableList[NUM_FILTERS];
    private Filter[] currFilters = new Filter[NUM_FILTERS];
    private Class[] filterTypes = {
            Dilate.class,
            Erode.class,
            Bilateral.class,
            CannyEdge.class,
            SobelEdge.class,
            ScharrEdge.class,
            Threshold.class,
            Contrast.class
    };

    /*************************************
     *               Main()              *
     *************************************/

    static public void main(String[] args) {
        String[] appletArgs = new String[]{"Sketch"};
        PApplet.main(appletArgs);
    }

    /*************************************
     *         Processing Sketch         *
     *************************************/

    public void settings() {
        size(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    public void setup() {
        frameRate(FPS);

        setupKeys();
        setupGUI();
        setupServer();

        sharedSketch = this;
        sharedCP5 = cp5;
    }

    public void draw() {
        background(BACKGROUND_CLR);
        drawCameraView();
        drawConnectionStatus();
        drawLines();
    }

    private void drawCameraView() {
        //draw current frame
        if (frameToDraw != null) {
            image(frameToDraw, 0, 0, WINDOW_WIDTH, VIDEO_HEIGHT);
        }
    }

    private void drawConnectionStatus() {
        if (!connected) {
            //draw disconnected indicator
            stroke(255);
            fill(255);
            textSize(50);
            text("Not connected", 10, 50);
            textSize(20);
            text("Port: " + DEFAULT_PORT, 12, 80);
        }
    }

    private void drawLines() {
        //draw footer dividers
        stroke(255);
        fill(255);
        line(0, VIDEO_HEIGHT, WINDOW_WIDTH, VIDEO_HEIGHT);
        for (int i = 1; i < currFilters.length; i++) {
            line(i * (WINDOW_WIDTH/ currFilters.length) , VIDEO_HEIGHT, i * (WINDOW_WIDTH/ currFilters.length), WINDOW_HEIGHT);
        }
    }

    /*************************************
     *              CP5 GUI              *
     *************************************/

    private void setupGUI() {
        cp5 = new ControlP5(this);
        setupButtons();
        setupDropdowns();
    }

    private void setupButtons() {
        cp5.addButton("toggleFiltering")
                .setBroadcast(false)
                .setLabel("Filter On /Off")
                .setPosition(5,VIDEO_HEIGHT - 20)
                .setSize(75,15)
                .setBroadcast(true)
        ;

        cp5.addButton("loadFilters")
                .setBroadcast(false)
                .setLabel("Load")
                .setPosition(WINDOW_WIDTH - 160,VIDEO_HEIGHT - 20)
                .setSize(75,15)
                .setBroadcast(true)
        ;

        cp5.addButton("saveFilters")
                .setBroadcast(false)
                .setLabel("Save")
                .setPosition(WINDOW_WIDTH - 80,VIDEO_HEIGHT - 20)
                .setSize(75,15)
                .setBroadcast(true)
        ;
    }

    private void setupDropdowns() {
        String[] filterNames = new String[filterTypes.length + 1];
        for (int i = 0; i < filterTypes.length; i++) {
            filterNames[i] = filterTypes[i].getName();
        }
        filterNames[filterNames.length - 1] = "None";

        for (int i = 0; i < currFilters.length; i++) {
            dropdownLists[i] = cp5.addScrollableList(""+i)
                    .setBroadcast(false)
                    .setLabel("Filter "+ (i+1))
                    .setPosition((i * (WINDOW_WIDTH/ currFilters.length)) + 5, VIDEO_HEIGHT + 5)
                    .setSize((WINDOW_WIDTH/ currFilters.length) - 10, WINDOW_HEIGHT - VIDEO_HEIGHT - 10)
                    .setBarHeight(20)
                    .setItemHeight(20)
                    .addItems(filterNames)
                    .addListener(this)
                    .setBroadcast(true)
            ;
        }
    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController() && theEvent.getController() instanceof ScrollableList) {
            int filterIndex = Integer.parseInt(theEvent.getName());
            int newTypeIndex = (int) theEvent.getController().getValue();

            if (currFilters[filterIndex] != null) {
                currFilters[filterIndex].destroyUI();
            }

            if (newTypeIndex < filterTypes.length) {
                try {
                    currFilters[filterIndex] = (Filter) filterTypes[newTypeIndex].newInstance();
                } catch (ReflectiveOperationException e) {
                    System.out.println("Couldn't assign filter: "+ e.getMessage());
                }

                currFilters[filterIndex].createUI(filterIndex * (WINDOW_WIDTH / NUM_FILTERS), VIDEO_HEIGHT + 25, (WINDOW_WIDTH / NUM_FILTERS), WINDOW_HEIGHT - VIDEO_HEIGHT);
            } else {
                currFilters[filterIndex] = null;
            }

            ((ScrollableList) theEvent.getController()).bringToFront();
        }
    }

    public void loadFilters() {
        //Create a file chooser
        JFileChooser fc = new JFileChooser();

        //In response to a button click:
        //int returnVal = fc.showOpenDialog(null);
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                FileInputStream fileIn = new FileInputStream(fc.getSelectedFile());
                ObjectInputStream in = new ObjectInputStream(fileIn);

                for (int i = 0; i < currFilters.length; i++) {
                    try {
                        if (currFilters[i] != null) {
                            currFilters[i].destroyUI();
                        }
                        if (in.available() > 0) {
                            currFilters[i] = (Filter) in.readObject();
                        } else {
                            currFilters[i] = null;
                        }

                        if (currFilters[i] != null) {
                            currFilters[i].createUI(
                                    i * (WINDOW_WIDTH / NUM_FILTERS),
                                    VIDEO_HEIGHT + 25, (WINDOW_WIDTH / NUM_FILTERS),
                                    WINDOW_HEIGHT - VIDEO_HEIGHT
                            );
                        }
                    } catch(ClassNotFoundException c) {
                        currFilters[i] = null;
                    }
                }

                in.close();
                fileIn.close();
            }catch(IOException i) {
                System.out.println(i.getMessage());
                //i.printStackTrace();
            }
        }
    }

    public void saveFilters() {
        JFileChooser fc = new JFileChooser();

        //In response to a button click:
        //int returnVal = fc.showOpenDialog(null);
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                FileOutputStream fileOut = new FileOutputStream(fc.getSelectedFile());
                ObjectOutputStream out = new ObjectOutputStream(fileOut);


                for (int i = 0; i < currFilters.length; i++) {
                    if (currFilters[i] != null) {
                        out.writeObject(currFilters[i]);
                    }
                }

                out.close();
                fileOut.close();
            }catch(IOException i) {
                System.out.println(i.getMessage());
                //i.printStackTrace();
            }
        }
    }

    /*************************************
     *         Frame Processing          *
     *************************************/

    private synchronized void processFrame() {
        for (Filter currFilter : currFilters) {
            if (currFilter != null) {
                frameToProcess = currFilter.applyFilter(frameToProcess, null);
            }
        }

        frameToDraw = frameToProcess;
        frameToProcess = null;
    }

    public synchronized void setFrame(PImage newFrame) {
        if (applyFilters) {
            frameToProcess = newFrame;
            processFrame();
        } else {
            frameToDraw = newFrame;
        }
    }

    public synchronized void toggleFiltering() {
        applyFilters = !applyFilters;
    }

    /*************************************
     *            Networking             *
     *************************************/

    private void setupServer() {
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            setConnected(false);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            // TODO: 2017-06-15 Implement something more elegant for server socket creation errors
            exit();
        }
    }

    public void setStreams(Socket newClientSocket) {
        try {
            DataInputStream reader = new DataInputStream(newClientSocket.getInputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(newClientSocket.getOutputStream()));

            new InputThread(reader, this).start();
            new OutputThread(writer, this).start();

            setConnected(true);
            System.out.println("Connected to " + newClientSocket.getInetAddress());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            setConnected(false);
        }
    }

    public void setConnected(boolean newConnectionState) {
        connected = newConnectionState;
        if (!newConnectionState) {
            new ConnectThread(serverSocket, this).start();
        }
    }

    /*************************************
     *             Keyboard              *
     *************************************/

    private void setupKeys() {
        //map keyboard characters to the Key representing their associated action
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

    public void keyReleased() {
        //set Key instance representing the action corresponding to the released character to "up"
        Key released = keyMap.get(Character.toLowerCase(key));
        if (released != null) {
            released.setUp();
        }
    }

    public void keyPressed() {
        //set Key instance representing the action corresponding to the pressed character to "down"
        Key pressed = keyMap.get(Character.toLowerCase(key));
        if (pressed != null) {
            pressed.setDown();
        }
    }

}
