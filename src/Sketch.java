import controlP5.*;
import processing.core.PApplet;
import processing.core.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;


/**
 * Created by Nicholas on 2017-05-12.
 *
 */

public class Sketch extends PApplet implements ControlListener {
    public static ControlP5 sharedCP5 = null;
    public static PApplet sharedSketch = null;

    private static final int DEFAULT_PORT = 4000;
    private static final int FPS = 15;
    private static int windowWidth;
    private static int videoHeight;
    private static int windowHeight;
    private static final int BACKGROUND_CLR = 0;
    private static final int NUM_FILTERS = 5;

    private ServerSocket serverSocket;
    private Map<Character, Key> keyMap;
    private PImage frameToDraw = null;
    private boolean connected = false;
    private boolean applyFilters = false;
    private boolean applyBlend = false;

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
        fullScreen();
        //size(windowWidth, windowHeight);
    }

    public void setup() {
        windowHeight = height;
        videoHeight = windowHeight - 100; //(int) (480 * 1.5);
        windowWidth = (int) ((videoHeight/480.0) * 640.0); // width; //(int) (640 * 1.5);

        frameRate(FPS);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("Using OpenCV 3.20");

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
        if (frameToDraw != null) {
            image(frameToDraw, 0, 0, windowWidth, videoHeight);
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
        line(0, videoHeight, windowWidth, videoHeight);
        for (int i = 1; i < currFilters.length; i++) {
            line(i * (windowWidth / currFilters.length) , videoHeight, i * (windowWidth / currFilters.length), windowHeight);
        }
    }

    private void drawFavSelection() {
        fill(255);
        noStroke();
        rect(
                (windowWidth /3) + (currFav*((windowWidth /3)/ favButtons.length)),
                videoHeight - 20,
                ((windowWidth /3)/ favButtons.length),
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
                .setPosition(5, videoHeight - 20)
                .setSize(75,15)
                .setBroadcast(true)
        ;

        cp5.addButton("loadFiltersGUI")
                .setBroadcast(false)
                .setLabel("Load")
                .setPosition(windowWidth - 160, videoHeight - 20)
                .setSize(75,15)
                .setBroadcast(true)
        ;

        cp5.addButton("saveFiltersGUI")
                .setBroadcast(false)
                .setLabel("Save")
                .setPosition(windowWidth - 80, videoHeight - 20)
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
                    .setPosition((i * (windowWidth / currFilters.length)) + 5, videoHeight + 5)
                    .setSize((windowWidth / currFilters.length) - 10, windowHeight - videoHeight - 10)
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
                .setPosition((windowWidth /3) - 30, videoHeight - 20)
                .setSize(20,15)
                .setBroadcast(true)
        ;

        cp5.addButton("resetFavorite")
                .setBroadcast(false)
                .setLabel("reset")
                .setPosition((windowWidth /3) - 65, videoHeight - 20)
                .setSize(30,15)
                .setBroadcast(true)
        ;

        for (int i = 0; i < favButtons.length; i++) {
            favButtons[i] = cp5.addButton("fav"+i)
                    .setBroadcast(false)
                    .setLabel(""+(i+1))
                    .setPosition((windowWidth /3) + (i*((windowWidth /3)/ favButtons.length)), videoHeight - 20)
                    .setSize(((windowWidth /3)/ favButtons.length),15)
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

                currFilters[filterIndex].createUI(filterIndex * (windowWidth / NUM_FILTERS), videoHeight + 25, (windowWidth / NUM_FILTERS), windowHeight - videoHeight);
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

    private synchronized Mat processMat(Mat newMat) {
        Mat src = newMat;
        Mat dst = new Mat(src.height(), src.width(), CvType.CV_8UC3);

        for (Filter currFilter : currFilters) {
            if (currFilter != null) {
                currFilter.applyFilter(src, dst);

                //swap
                newMat = src;
                src = dst;
                dst = newMat;
            }
        }

        return src; //after swap src is the last dst
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
                            i * (windowWidth / NUM_FILTERS),
                            videoHeight + 25, (windowWidth / NUM_FILTERS),
                            windowHeight - videoHeight
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

    public synchronized void setFrame(Mat newMat) {
        try {
            if (applyFilters) {
                if (applyBlend) {
                    Mat blend = new Mat(newMat.rows(), newMat.cols(), newMat.type());
                    newMat.copyTo(blend);
                    PImage tempframeToDraw = toPImage(processMat(newMat));
                    tempframeToDraw.blend(toPImage(blend), 0,0, frameToDraw.width, frameToDraw.height, 0, 0, frameToDraw.width, frameToDraw.height, PApplet.LIGHTEST);
                    frameToDraw = tempframeToDraw;
                } else {
                    frameToDraw = toPImage(processMat(newMat));
                }
            } else {
                frameToDraw = toPImage(newMat);
            }
        } catch (Exception e) {
            System.out.println("Couldn't set next frame: "+ e.getMessage());
        }
    }

    public synchronized void toggleFiltering() {
        applyFilters = !applyFilters;
    }

    public synchronized void toggleBlend() {
        applyBlend = !applyBlend;
    }

    public PImage toPImage(Mat m) throws Exception {
        PImage img = new PImage(m.width(), m.height());

        Mat m2 = new Mat();
        if (m.channels() == 3) {
            Imgproc.cvtColor(m, m2, Imgproc.COLOR_BGR2BGRA); //or Imgproc.COLOR_RGB2RGBA
        } else if (m.channels() == 1) {
            Imgproc.cvtColor(m, m2, Imgproc.COLOR_GRAY2BGRA); //or Imgproc.COLOR_RGB2RGBA
        } else if (m.channels() == 4) {
            m2 = m;
        } else {
            throw new Exception("source mat is not 3 | 4 | 1 channels");
        }

        int pImageChannels = 4;
        int numPixels = m2.width()*m2.height();
        int[] intPixels = new int[numPixels];
        byte[] matPixels = new byte[numPixels*pImageChannels];

        m2.get(0,0, matPixels);
        ByteBuffer.wrap(matPixels).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(intPixels);
        img.pixels = intPixels;

        img.updatePixels();
        return img;
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
