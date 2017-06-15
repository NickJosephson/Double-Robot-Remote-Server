import controlP5.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * Created by Nicholas on 2017-06-12.
 */
public abstract class Filter {
    public ControlP5 cp5;
    public PApplet sketch;

    public Filter(ControlP5 cp5, PApplet sketch) {
        this.cp5 = cp5;
        this.sketch = sketch;
    }

    abstract void applyFilter(PImage image);
    abstract void createUI(int x, int y, int width, int height);
    abstract void destroyUI();
    abstract Filter init(ControlP5 cp5,  PApplet sketch);
}

class Erode extends Filter implements ControlListener {
    private Button test;

    public Erode(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public void applyFilter(PImage image) {
        image.filter(PConstants.ERODE);
    }

    public void createUI(int x, int y, int width, int height) {
        test = super.cp5.addButton("test" + x).setBroadcast(false)
                .setLabel("test")
                .setPosition(x + 5, y + 20)
                .setSize(75,15)
                .addListener(this)
                .setBroadcast(true)
        ;
    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Button) {
                //sketch.exit();
            }
        }
    }

    public void destroyUI() {
        test.remove();
    }

    public Filter init(ControlP5 cp5,  PApplet sketch) {
        return new Erode(cp5, sketch);
    }

}

class Dilate extends Filter {
    public Dilate(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public void applyFilter(PImage image) {
        image.filter(PConstants.DILATE);
    }

    public void createUI(int x, int y, int width, int height) {

    }

    public void destroyUI() {

    }

    public Filter init(ControlP5 cp5,  PApplet sketch) {
        return new Dilate(cp5, sketch);
    }
}

class Bilateral extends Filter {
    public Bilateral(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public void applyFilter(PImage image) {
        image.filter(PConstants.GRAY);
    }

    public void createUI(int x, int y, int width, int height) {

    }

    public void destroyUI() {

    }

    public Filter init(ControlP5 cp5,  PApplet sketch) {
        return new Bilateral(cp5, sketch);
    }
}

class Line extends Filter {
    public Line(ControlP5 cp5,  PApplet sketch) {
        super(cp5, sketch);
    }

    public void applyFilter(PImage image) {
        image.filter(PConstants.THRESHOLD);
    }

    public void createUI(int x, int y, int width, int height) {

    }

    public void destroyUI() {

    }

    public Filter init(ControlP5 cp5,  PApplet sketch) {
        return new Line(cp5, sketch);
    }
}


     /*
        slider = cp5.addSlider("sliderValue")
                .setPosition(100,VIDEO_HEIGHT + 25)
                .setRange(0,10)
        ;


        range = cp5.addRange("rangeController")
                // disable broadcasting since setRange and setRangeValues will trigger an event
                .setBroadcast(false)
                .setPosition(50,VIDEO_HEIGHT + 50)
                .setSize(400,40)
                .setHandleSize(20)
                .setRange(0,255)
                .setRangeValues(50,100)
                // after the initialization we turn broadcast back on again
                .setBroadcast(true)
                .setColorForeground(color(255,40))
                .setColorBackground(color(255,40))
        ;
        */

//cv.getOutput();

//private OpenCV cv;

//if (cv == null) {
//  cv = new OpenCV(this, frameToProcess);
//}

//cv.loadImage(frameToProcess);
//for(Filter currFilter:filters)

//cv.contrast(sliderValue);
//cv.findCannyEdges((int)range.getArrayValue(0), (int)range.getArrayValue(1));

//cv.findCannyEdges(mouseX, mouseY);
//cv.findSobelEdges(1,1);
//cv.gray();
//cv.dilate();
//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
//System.out.println("mat = " + mat.dump());
//cv.bilateralFilter();
//Mat stuff = cv.getColor();
//cv.toCv(frameToProcess, stuff);

//Imgproc.bilateralFilter(stuff, stuff, 5, 150, 150);

//cv.toPImage(stuff, frameToDraw);