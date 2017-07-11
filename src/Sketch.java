import controlP5.*;
import processing.core.PApplet;
import processing.core.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Nicholas on 2017-05-12.
 *
 */

public class Sketch extends PApplet implements ControlListener {
    public static ControlP5 sharedCP5 = null;
    public static PApplet sharedSketch = null;

    private static final int DEFAULT_PORT = 4000;
    private static final int FPS = 15;
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
    private Button[] favButtons = new Button[5];
    private Filter[][] favFilters = new Filter[5][];
    private int currFav = 0;
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

        changeFilters(favFilters[currFav]);
    }

    public void draw() {
        background(BACKGROUND_CLR);
        drawCameraView();
        drawConnectionStatus();
        drawLines();
        drawFavSelection();
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

    private void drawFavSelection() {
        fill(255);
        noStroke();
        rect(
                (WINDOW_WIDTH/3) + (currFav*((WINDOW_WIDTH/3)/ favButtons.length)),
                VIDEO_HEIGHT - 20,
                ((WINDOW_WIDTH/3)/ favButtons.length),
                19
        );
    }

    /*************************************
     *              CP5 GUI              *
     *************************************/

    private void setupGUI() {
        cp5 = new ControlP5(this);
        setupButtons();
        setupFavButtons();
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

        cp5.addButton("loadFiltersGUI")
                .setBroadcast(false)
                .setLabel("Load")
                .setPosition(WINDOW_WIDTH - 160,VIDEO_HEIGHT - 20)
                .setSize(75,15)
                .setBroadcast(true)
        ;

        cp5.addButton("saveFiltersGUI")
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

    private void setupFavButtons() {
        cp5.addButton("setFavorite")
                .setBroadcast(false)
                .setLabel("set")
                .setPosition((WINDOW_WIDTH/3) - 30,VIDEO_HEIGHT - 20)
                .setSize(20,15)
                .setBroadcast(true)
        ;

        cp5.addButton("resetFavorite")
                .setBroadcast(false)
                .setLabel("reset")
                .setPosition((WINDOW_WIDTH/3) - 65,VIDEO_HEIGHT - 20)
                .setSize(30,15)
                .setBroadcast(true)
        ;

        for (int i = 0; i < favButtons.length; i++) {
            favButtons[i] = cp5.addButton("fav"+i)
                    .setBroadcast(false)
                    .setLabel(""+(i+1))
                    .setPosition((WINDOW_WIDTH/3) + (i*((WINDOW_WIDTH/3)/ favButtons.length)),VIDEO_HEIGHT - 20)
                    .setSize(((WINDOW_WIDTH/3)/ favButtons.length),15)
                    .addListener(this)
                    .setBroadcast(true)
            ;

            favFilters[i] = loadFilters(new File("./favorites/"+ (i+1)));
        }
    }

    public synchronized void setFavorite() {
        saveCurrFilters(new File("./favorites/"+ (currFav + 1)));
        favFilters[currFav] = loadFilters(new File("./favorites/"+ (currFav + 1)));
        changeFilters(favFilters[currFav]);
    }

    public synchronized void resetFavorite() {
        changeFilters(loadFilters(new File("./favorites/EMPTY")));
        saveCurrFilters(new File("./favorites/"+ (currFav + 1)));
        favFilters[currFav] = loadFilters(new File("./favorites/"+ (currFav + 1)));
        changeFilters(favFilters[currFav]);
    }

    public synchronized void switchFav(int newFav) {
        currFav = newFav;
        changeFilters(favFilters[currFav]);
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
                    System.out.println("Couldn't assign filter: " + e.getMessage());
                }

                currFilters[filterIndex].createUI(filterIndex * (WINDOW_WIDTH / NUM_FILTERS), VIDEO_HEIGHT + 25, (WINDOW_WIDTH / NUM_FILTERS), WINDOW_HEIGHT - VIDEO_HEIGHT);
            } else {
                currFilters[filterIndex] = null;
            }

            ((ScrollableList) theEvent.getController()).bringToFront();
        }

        if (theEvent.isController() && theEvent.getController() instanceof Button) {
            if (theEvent.getName().substring(0,3).equals("fav")) {
                currFav = Integer.parseInt(theEvent.getName().substring(3));
                changeFilters(favFilters[currFav]);
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

    private Filter getFilterInstance(String filterName) {
        Filter result = null;

        if (!filterName.equals("NONE")) {
            for (int i = 0; i < filterTypes.length && result == null; i++) {
                if (filterName.equals(filterTypes[i].getName())) {
                    try {
                        result = (Filter) filterTypes[i].newInstance();
                    } catch (ReflectiveOperationException e) {
                        System.out.println("Cannot instantiate filter named \""+ filterName +"\": "+ e.getMessage());
                        return null;
                    }
                }
            }
        }

        return result;
    }

    private void changeFilters(Filter[] newFilters) {
        try {
            if (newFilters.length != NUM_FILTERS) {
                throw new Exception("Number of filters in list doesn't match NUM_FILTERS");
            }

            for (int i = 0; i < newFilters.length; i++) {
                if (currFilters[i] != null) {
                    currFilters[i].destroyUI();
                }

                currFilters[i] = newFilters[i];

                dropdownLists[i].close();

                if (currFilters[i] == null) {
                    dropdownLists[i].setLabel("None");
                } else {
                    dropdownLists[i].setLabel(newFilters[i].getClass().getName());
                    currFilters[i].createUI(
                            i * (WINDOW_WIDTH / NUM_FILTERS),
                            VIDEO_HEIGHT + 25, (WINDOW_WIDTH / NUM_FILTERS),
                            WINDOW_HEIGHT - VIDEO_HEIGHT
                    );
                }

                dropdownLists[i].bringToFront();
            }
        } catch(Exception e) {
            System.out.println("Couldn't change filters: "+ e.getMessage());
        }
    }

    private Filter[] loadFilters(File file) {
        Filter[] newFilters = null;

        try {
            Scanner fileIn = new Scanner(file);

            newFilters = new Filter[fileIn.nextInt()];
            fileIn.nextLine();

            for (int i = 0; i < newFilters.length; i++) {
                newFilters[i] = getFilterInstance(fileIn.nextLine());
                if (newFilters[i] != null) {
                    newFilters[i].setParameters(fileIn.nextLine());
                }
            }

            fileIn.close();
        } catch(Exception e) {
            System.out.println("Couldn't process filter file: "+ e.getMessage());
            newFilters = null;
        }

        return newFilters;
    }

    private void saveCurrFilters(File file) {
        try {
            BufferedWriter fileOut = new BufferedWriter(new FileWriter(file));

            fileOut.write(""+ currFilters.length);
            fileOut.newLine();

            for (int i = 0; i < currFilters.length; i++) {
                if (currFilters[i] != null) {
                    fileOut.write(currFilters[i].getClass().getName());
                    fileOut.newLine();
                    fileOut.write(currFilters[i].getParameters());
                } else {
                    fileOut.write("NONE");
                }
                fileOut.newLine();
            }

            fileOut.close();
        }catch(IOException i) {
            System.out.println("Couldn't save filters: "+ i.getMessage());
        }
    }

    public void loadFiltersGUI() {
        //Create a file chooser
        JFileChooser fc = new JFileChooser();

        //In response to a button click:
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            changeFilters(loadFilters(fc.getSelectedFile()));
        }
    }

    public void saveFiltersGUI() {
        //Create a file chooser
        JFileChooser fc = new JFileChooser();

        //In response to a button click:
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            saveCurrFilters(fc.getSelectedFile());
        }
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
        keyMap.put('1', Key.fav1);
        keyMap.put('2', Key.fav2);
        keyMap.put('3', Key.fav3);
        keyMap.put('4', Key.fav4);
        keyMap.put('5', Key.fav5);
        keyMap.put('r', Key.setFav);
        keyMap.put('t', Key.resetFav);

        new KeyThread(this).start();
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
