import controlP5.*;
import gab.opencv.OpenCV;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Created by Nicholas on 2017-06-12.
 *
 */
public abstract class Filter {
    private ControlP5 cp5;
    private PApplet sketch;

    public Filter(ControlP5 cp5, PApplet sketch) {
        this.cp5 = cp5;
        this.sketch = sketch;
    }

    abstract PImage applyFilter(PImage source, PImage destination);

    abstract void createUI(int x, int y, int width, int height);
    abstract void destroyUI();

    abstract Filter newInstance(ControlP5 cp5, PApplet sketch);

    abstract String getName();

    public ControlP5 getCP5() {
        return cp5;
    }

    public PApplet getSketch() {
        return sketch;
    }

}

class Erode extends Filter implements ControlListener {
    private OpenCV cv;
    private Slider test;
    private int strength = 1;

    public Erode(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public PImage applyFilter(PImage source, PImage destination) {
        if (cv == null) {
            cv = new OpenCV(getSketch(), source);
        } else {
            cv.loadImage(source);
        }

        for (int i = 0; i < strength; i++) {
            cv.erode();
        }

        return cv.getOutput();
    }

    public void createUI(int x, int y, int width, int height) {
        test = getCP5().addSlider("erode"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 30)
                .setSize(width - 10,20)
                .setLabel("Strength")
                .setRange(1,10)
                .setNumberOfTickMarks(10)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;

        test.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Slider) {
                strength = (int) theEvent.getController().getValue();
            }
        }
    }

    public void destroyUI() {
        test.remove();
    }

    public String getName() {
        return "Erode";
    }

    public Filter newInstance(ControlP5 cp5, PApplet sketch) {
        return new Erode(cp5, sketch);
    }

}

class Dilate extends Filter implements ControlListener {
    private OpenCV cv;
    private Slider test;
    private int strength = 1;

    public Dilate(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public PImage applyFilter(PImage source, PImage destination) {
        if (cv == null) {
            cv = new OpenCV(getSketch(), source);
        } else {
            cv.loadImage(source);
        }

        for (int i = 0; i < strength; i++) {
            cv.dilate();
        }

        return cv.getOutput();
    }

    public void createUI(int x, int y, int width, int height) {
        test = getCP5().addSlider("dilate"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 30)
                .setSize(width - 10,20)
                .setLabel("Strength")
                .setRange(1,10)
                .setNumberOfTickMarks(10)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;

        test.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Slider) {
                strength = (int) theEvent.getController().getValue();
            }
        }
    }

    public void destroyUI() {
        test.remove();
    }

    public String getName() {
        return "Dilate";
    }

    public Filter newInstance(ControlP5 cp5, PApplet sketch) {
        return new Dilate(cp5, sketch);
    }

}

class Bilateral extends Filter implements ControlListener {
    private OpenCV cv;
    private Slider dSlider;
    private Slider sigmaColourSlider;
    private Slider sigmaSpaceSlider;
    private int d = 5;
    private double sigmaColour = 150;
    private double sigmaSpace = 150;


    public Bilateral(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public PImage applyFilter(PImage source, PImage destination) {
        if (cv == null) {
            cv = new OpenCV(getSketch(), source);
        } else {
            cv.loadImage(source);
        }

        cv.bilateralFilter(d, sigmaColour, sigmaSpace);

        return cv.getOutput();
    }

    public void createUI(int x, int y, int width, int height) {
        dSlider = getCP5().addSlider("d"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,10)
                .setLabel("d")
                .setRange(1,10)
                .setNumberOfTickMarks(10)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        dSlider.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.RIGHT).setPaddingX(0);

        sigmaColourSlider = getCP5().addSlider("sigmaColour"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 30)
                .setSize(width - 10,10)
                .setLabel("Sigma Colour")
                .setRange(0,300)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        sigmaColourSlider.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.RIGHT).setPaddingX(0);

        sigmaSpaceSlider = getCP5().addSlider("sigmaSpace"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 45)
                .setSize(width - 10,10)
                .setLabel("Sigma Space")
                .setRange(0,300)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        sigmaSpaceSlider.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.RIGHT).setPaddingX(0);
    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Slider) {
                if (theEvent.getController().getLabel().equals("d")) {
                    d = (int) theEvent.getController().getValue();
                } else if (theEvent.getController().getLabel().equals("Sigma Colour")) {
                    sigmaColour = theEvent.getController().getValue();
                } else {
                    sigmaSpace = theEvent.getController().getValue();
                }
            }
        }
    }

    public void destroyUI() {
        dSlider.remove();
        sigmaColourSlider.remove();
        sigmaSpaceSlider.remove();
    }

    public String getName() {
        return "Bilateral";
    }

    public Filter newInstance(ControlP5 cp5, PApplet sketch) {
        return new Bilateral(cp5, sketch);
    }

}

class CannyEdge extends Filter implements ControlListener {
    private OpenCV cv;
    private Slider lowerSlider;
    private Slider upperSlider;
    private int lower = 0;
    private int upper = 0;

    public CannyEdge(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public PImage applyFilter(PImage source, PImage destination) {
        if (cv == null) {
            cv = new OpenCV(getSketch(), source);
        } else {
            cv.loadImage(source);
        }

        cv.findCannyEdges(lower, upper);

        return cv.getOutput();
    }

    public void createUI(int x, int y, int width, int height) {
        lowerSlider = getCP5().addSlider("lowerSlider"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,20)
                .setLabel("Lower bound")
                .setRange(1,255)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        lowerSlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

        upperSlider = getCP5().addSlider("upperSlider"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 50)
                .setSize(width - 10,20)
                .setLabel("Upper bound")
                .setRange(1,255)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        upperSlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Slider) {
                if (theEvent.getController().getLabel().equals("Lower bound")) {
                    lower = (int) theEvent.getController().getValue();
                } else {
                    upper = (int) theEvent.getController().getValue();
                }
            }
        }
    }

    public void destroyUI() {
        lowerSlider.remove();
        upperSlider.remove();
    }

    public String getName() {
        return "Canny Edge";
    }

    public Filter newInstance(ControlP5 cp5, PApplet sketch) {
        return new CannyEdge(cp5, sketch);
    }

}

class SobelEdge extends Filter implements ControlListener {
    private OpenCV cv;
    private Slider dxSlider;
    private Slider dySlider;
    private int dx = 1;
    private int dy = 1;

    public SobelEdge(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public PImage applyFilter(PImage source, PImage destination) {
        if (cv == null) {
            cv = new OpenCV(getSketch(), source);
        } else {
            cv.loadImage(source);
        }

        cv.findSobelEdges(dx, dy);

        return cv.getOutput();
    }

    public void createUI(int x, int y, int width, int height) {
        dxSlider = getCP5().addSlider("dx"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,20)
                .setLabel("dx")
                .setRange(0,2)
                .setNumberOfTickMarks(3)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        dxSlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

        dySlider = getCP5().addSlider("dy"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 50)
                .setSize(width - 10,20)
                .setLabel("dy")
                .setRange(0,2)
                .setNumberOfTickMarks(3)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        dySlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Slider) {
                if (theEvent.getController().getLabel().equals("dx")) {
                    dx = (int) theEvent.getController().getValue();
                } else {
                    dy = (int) theEvent.getController().getValue();
                }
            }
        }
    }

    public void destroyUI() {
        dxSlider.remove();
        dySlider.remove();
    }

    public String getName() {
        return "Sobel Edge";
    }

    public Filter newInstance(ControlP5 cp5, PApplet sketch) {
        return new SobelEdge(cp5, sketch);
    }

}

class ScharrEdge extends Filter implements ControlListener {
    private OpenCV cv;
    private Slider directionSlider;
    private int direction = -1;

    public ScharrEdge(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public PImage applyFilter(PImage source, PImage destination) {
        if (cv == null) {
            cv = new OpenCV(getSketch(), source);
        } else {
            cv.loadImage(source);
        }

        cv.findScharrEdges(direction);

        return cv.getOutput();
    }

    public void createUI(int x, int y, int width, int height) {
        directionSlider = getCP5().addSlider("directionSlider"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,20)
                .setLabel("Direction")
                .setRange(-1,1)
                .setNumberOfTickMarks(3)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        directionSlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Slider) {
                direction = (int) theEvent.getController().getValue();
            }
        }
    }

    public void destroyUI() {
        directionSlider.remove();
    }

    public String getName() {
        return "Scharr Edge";
    }

    public Filter newInstance(ControlP5 cp5, PApplet sketch) {
        return new ScharrEdge(cp5, sketch);
    }

}

class Threshold extends Filter implements ControlListener {
    private OpenCV cv;
    private Slider thresholdSlider;
    private int threshold = -1;

    public Threshold(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public PImage applyFilter(PImage source, PImage destination) {
        if (cv == null) {
            cv = new OpenCV(getSketch(), source);
        } else {
            cv.loadImage(source);
        }

        cv.threshold(threshold);

        return cv.getOutput();
    }

    public void createUI(int x, int y, int width, int height) {
        thresholdSlider = getCP5().addSlider("amount"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,20)
                .setLabel("Threshold")
                .setRange(0,255)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        thresholdSlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Slider) {
                threshold = (int) theEvent.getController().getValue();
            }
        }
    }

    public void destroyUI() {
        thresholdSlider.remove();
    }

    public String getName() {
        return "Thresholde";
    }

    public Filter newInstance(ControlP5 cp5, PApplet sketch) {
        return new Threshold(cp5, sketch);
    }

}

class Contrast extends Filter implements ControlListener {
    private OpenCV cv;
    private Slider amountSlider;
    private float amount = 0;

    public Contrast(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public PImage applyFilter(PImage source, PImage destination) {
        if (cv == null) {
            cv = new OpenCV(getSketch(), source);
        } else {
            cv.loadImage(source);
        }

        cv.contrast(amount);

        return cv.getOutput();
    }

    public void createUI(int x, int y, int width, int height) {
        amountSlider = getCP5().addSlider("amount"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,20)
                .setLabel("Contrast")
                .setRange(0,30)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        amountSlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Slider) {
                amount = (float) theEvent.getController().getValue();
            }
        }
    }

    public void destroyUI() {
        amountSlider.remove();
    }

    public String getName() {
        return "Contrast";
    }

    public Filter newInstance(ControlP5 cp5, PApplet sketch) {
        return new Contrast(cp5, sketch);
    }

}
