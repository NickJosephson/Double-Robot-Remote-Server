import controlP5.*;
import org.opencv.core.*;
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

public class Sketch extends PApplet implements ControlListener {
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
    private Filter[] filterTypes;
    private Filter[] filters = new Filter[NUM_FILTERS];
    private ScrollableList[] dropdownLists = new ScrollableList[NUM_FILTERS];

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
        setConnected(false);
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void draw() {
        background(BACKGROUND_CLR);
        if (connected) {
            //draw current  frame
            if (frameToDraw != null) {
                image(frameToDraw, 0, 0, WINDOW_WIDTH, VIDEO_HEIGHT);
            }
        } else {
            //draw disconnected indicator
            stroke(255);
            fill(255);
            textSize(50);
            text("Not connected", 10, 50);
            textSize(20);
            text("Port: " + DEFAULT_PORT, 12, 80);
        }

        //draw footer dividers
        stroke(255);
        fill(255);
        line(0, VIDEO_HEIGHT, WINDOW_WIDTH, VIDEO_HEIGHT);
        for (int i = 1; i < filters.length; i++) {
            line(i * (WINDOW_WIDTH/filters.length) , VIDEO_HEIGHT, i * (WINDOW_WIDTH/filters.length), WINDOW_HEIGHT);
        }
    }

    /*************************************
     *              CP5 GUI              *
     *************************************/

    private void setupGUI() {
        filterTypes = new Filter[] {
                new Dilate(cp5, this),
                new Erode(cp5, this),
                new Bilateral(cp5, this),
                new CannyEdge(cp5, this),
                new SobelEdge(cp5, this),
                new ScharrEdge(cp5, this),
                new Threshold(cp5, this),
                new Contrast(cp5, this),
                null
        };
        String[] filterNames = new String[filterTypes.length];
        for (int j = 0; j < filterNames.length - 1; j++) {
            if (filterTypes[j] != null) {
                filterNames[j] = filterTypes[j].getName();
            }
        }
        filterNames[filterNames.length - 1] = "None";

        cp5 = new ControlP5(this);


        cp5.addButton("toggleFiltering")
                .setBroadcast(false)
                .setLabel("Filter On /Off")
                .setPosition(5,VIDEO_HEIGHT - 20)
                .setSize(75,15)
                .setBroadcast(true)
        ;


        for (int i = 0; i < filters.length; i++) {
            dropdownLists[i] = cp5.addScrollableList(""+i)
                    .setBroadcast(false)
                    .setLabel("Filter "+ (i+1))
                    .setPosition((i * (WINDOW_WIDTH/filters.length)) + 5, VIDEO_HEIGHT + 5)
                    .setSize((WINDOW_WIDTH/filters.length) - 10, WINDOW_HEIGHT - VIDEO_HEIGHT - 10)
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

            if (filters[filterIndex] != null) {
                filters[filterIndex].destroyUI();
            }

            if (filterTypes[newTypeIndex] != null) {
                filters[filterIndex] = filterTypes[newTypeIndex].newInstance(cp5, this);
                //filters[filterIndex] = filterTypes[newTypeIndex].getClass().newInstance();  //cp5, this);
                filters[filterIndex].createUI(filterIndex * (WINDOW_WIDTH / NUM_FILTERS), VIDEO_HEIGHT + 25, (WINDOW_WIDTH / NUM_FILTERS), WINDOW_HEIGHT - VIDEO_HEIGHT);
            } else {
                filters[filterIndex] = null;
            }

            ((ScrollableList) theEvent.getController()).bringToFront();
        }
    }

    /*************************************
     *         Frame Processing          *
     *************************************/

    private synchronized void processFrame() {
        for (Filter currFilter : filters) {
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

}
