import controlP5.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Nicholas on 2017-06-12.
 *
 */

public abstract class Filter implements java.io.Serializable {
    public static Object cv;

    public abstract void applyFilter(Mat source, Mat destination);
    public abstract void setParameters(String parameters);
    public abstract String getParameters();
    public abstract void createUI(int x, int y, int width, int height);
    public abstract void destroyUI();
}

class Erode extends Filter implements ControlListener {
    private Slider strengthSlider;
    private int strength = 1;

    public void applyFilter(Mat source, Mat destination) {
        Mat temp;

        for (int i = 0; i < strength; i++) {
            Imgproc.erode(source, destination, new Mat());

            temp = source;
            source = destination;
            destination = temp;
        }
    }

    public void createUI(int x, int y, int width, int height) {
        strengthSlider = Sketch.sharedCP5.addSlider("erode"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 30)
                .setSize(width - 10,20)
                .setLabel("Strength")
                .setRange(1,10)
                .setNumberOfTickMarks(10)
                .setValue(strength)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;

        strengthSlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Slider) {
                strength = (int) theEvent.getController().getValue();
            }
        }
    }

    public void destroyUI() {
        strengthSlider.remove();
    }

    public void setParameters(String parameters) {
        strength = Integer.parseInt(parameters.split(":")[1]);
    }

    public String getParameters() {
        return "strength:" + strength;
    }

}

class Dilate extends Filter implements ControlListener {
    private Slider strengthSlider;
    private int strength = 1;

    public void applyFilter(Mat source, Mat destination) {
        Mat temp;

        for (int i = 0; i < strength; i++) {
            Imgproc.dilate(source, destination, new Mat());

            temp = source;
            source = destination;
            destination = temp;
        }
    }

    public void createUI(int x, int y, int width, int height) {
        strengthSlider = Sketch.sharedCP5.addSlider("dilate"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 30)
                .setSize(width - 10,20)
                .setLabel("Strength")
                .setRange(1,10)
                .setNumberOfTickMarks(10)
                .setValue(strength)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;

        strengthSlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.getController() instanceof Slider) {
                strength = (int) theEvent.getController().getValue();
            }
        }
    }

    public void destroyUI() {
        strengthSlider.remove();
    }

    public void setParameters(String parameters) {
        strength = Integer.parseInt(parameters.split(":")[1]);
    }

    public String getParameters() {
        return "strength:" + strength;
    }

}

class Bilateral extends Filter implements ControlListener {
    private Slider dSlider;
    private Slider sigmaColourSlider;
    private Slider sigmaSpaceSlider;
    private int d = 5;
    private float sigmaColour = 150;
    private float sigmaSpace = 150;

    public void applyFilter(Mat source, Mat destination) {
        Imgproc.bilateralFilter(source, destination, d, sigmaColour, sigmaSpace);
    }

    public void createUI(int x, int y, int width, int height) {
        dSlider = Sketch.sharedCP5.addSlider("d"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,10)
                .setLabel("d")
                .setRange(1,10)
                .setNumberOfTickMarks(10)
                .setValue(d)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        dSlider.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.RIGHT).setPaddingX(0);

        sigmaColourSlider = Sketch.sharedCP5.addSlider("sigmaColour"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 30)
                .setSize(width - 10,10)
                .setLabel("Sigma Colour")
                .setRange(0,300)
                .setValue(sigmaColour)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        sigmaColourSlider.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.RIGHT).setPaddingX(0);

        sigmaSpaceSlider = Sketch.sharedCP5.addSlider("sigmaSpace"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 45)
                .setSize(width - 10,10)
                .setLabel("Sigma Space")
                .setRange(0,300)
                .setValue(sigmaSpace)
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

    public void setParameters(String parameters) {
        String[] values = parameters.split(",");
        d = Integer.parseInt(values[0].split(":")[1]);
        sigmaColour = Float.parseFloat(values[1].split(":")[1]);
        sigmaSpace = Float.parseFloat(values[2].split(":")[1]);
    }

    public String getParameters() {
        return "d:"+ d +",sigmaColour:"+ sigmaColour +",sigmaSpace:"+ sigmaSpace;
    }

}

class CannyEdge extends Filter implements ControlListener {
    private Slider lowerSlider;
    private Slider upperSlider;
    private int lower = 0;
    private int upper = 0;

    public void applyFilter(Mat source, Mat destination) {
        Imgproc.Canny(source, destination, lower, upper);
    }

    public void createUI(int x, int y, int width, int height) {
        lowerSlider = Sketch.sharedCP5.addSlider("lowerSlider"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,20)
                .setLabel("Lower bound")
                .setRange(1,255)
                .setValue(lower)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        lowerSlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

        upperSlider = Sketch.sharedCP5.addSlider("upperSlider"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 50)
                .setSize(width - 10,20)
                .setLabel("Upper bound")
                .setRange(1,255)
                .setValue(upper)
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

    public void setParameters(String parameters) {
        String[] values = parameters.split(",");
        lower = Integer.parseInt(values[0].split(":")[1]);
        upper = Integer.parseInt(values[1].split(":")[1]);
    }

    public String getParameters() {
        return "lower:"+ lower +",upper:"+ upper;
    }

}

class SobelEdge extends Filter implements ControlListener {
    private Slider dxSlider;
    private Slider dySlider;
    private int dx = 1;
    private int dy = 1;

    public void applyFilter(Mat source, Mat destination) {
        Mat sobeled = new Mat(source.height(), source.width(), CvType.CV_32F);
        Imgproc.Sobel(source, sobeled, CvType.CV_32F, dx, dy);
        sobeled.convertTo(destination, source.type());
    }

    public void createUI(int x, int y, int width, int height) {
        dxSlider = Sketch.sharedCP5.addSlider("dx"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,20)
                .setLabel("dx")
                .setRange(0,2)
                .setNumberOfTickMarks(3)
                .setValue(dx)
                .setSliderMode(Slider.FLEXIBLE)
                .addListener(this)
                .setBroadcast(true)
        ;
        dxSlider.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

        dySlider = Sketch.sharedCP5.addSlider("dy"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 50)
                .setSize(width - 10,20)
                .setLabel("dy")
                .setValue(dy)
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

    public void setParameters(String parameters) {
        String[] values = parameters.split(",");
        dx = Integer.parseInt(values[0].split(":")[1]);
        dy = Integer.parseInt(values[1].split(":")[1]);
    }

    public String getParameters() {
        return "dx:"+ dx +",dy:"+ dy;
    }

}

class ScharrEdge extends Filter implements ControlListener {
    private Slider directionSlider;
    private int direction = -1;

    public void applyFilter(Mat source, Mat destination) {
        if(direction == 1){
            Imgproc.Scharr(source, destination, -1, 1, 0 );
        }

        if(direction == 0){
            Imgproc.Scharr(source, destination, -1, 0, 1 );
        }

        if(direction == -1){
            Mat hMat = new Mat(source.height(), source.width(), source.type());
            Mat vMat = new Mat(source.height(), source.width(), source.type());
            Imgproc.Scharr(source, hMat, -1, 1, 0 );
            Imgproc.Scharr(source, vMat, -1, 0, 1 );
            Core.add(vMat,hMat, destination);
        }
    }

    public void createUI(int x, int y, int width, int height) {
        directionSlider = Sketch.sharedCP5.addSlider("directionSlider"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,20)
                .setLabel("Direction")
                .setRange(-1,1)
                .setNumberOfTickMarks(3)
                .setValue(direction)
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

    public void setParameters(String parameters) {
        direction = Integer.parseInt(parameters.split(":")[1]);
    }

    public String getParameters() {
        return "direction:" + direction;
    }

}

class Threshold extends Filter implements ControlListener {
    private Slider thresholdSlider;
    private int threshold = 0;

    public void applyFilter(Mat source, Mat destination) {
        Imgproc.threshold(source, destination, threshold, 255, Imgproc.THRESH_BINARY);
    }

    public void createUI(int x, int y, int width, int height) {
        thresholdSlider = Sketch.sharedCP5.addSlider("amount"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,20)
                .setLabel("Threshold")
                .setRange(0,255)
                .setValue(threshold)
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

    public void setParameters(String parameters) {
        threshold = Integer.parseInt(parameters.split(":")[1]);
    }

    public String getParameters() {
        return "threshold:" + threshold;
    }

}

class Contrast extends Filter implements ControlListener {
    private Slider amountSlider;
    private float amount = 0;

    public void applyFilter(Mat source, Mat destination) {
        Scalar modifier;
        modifier = new Scalar(amount,amount,amount,1);

        Core.multiply(source, modifier, destination);
    }

    public void createUI(int x, int y, int width, int height) {
        amountSlider = Sketch.sharedCP5.addSlider("amount"+x)
                .setBroadcast(false)
                .setPosition(x + 5,y + 15)
                .setSize(width - 10,20)
                .setLabel("Contrast")
                .setRange(0,30)
                .setValue(amount)
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

    public void setParameters(String parameters) {
        amount = Float.parseFloat(parameters.split(":")[1]);
    }

    public String getParameters() {
        return "amount:" + amount;
    }

}
