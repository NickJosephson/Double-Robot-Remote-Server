import processing.core.PImage;
import processing.core.*;

/**
 * Created by Nicholas on 2017-05-26.
 */
public class ImageProcessor {
    private PApplet sketch;

    public ImageProcessor(PApplet sketch) {
        this.sketch = sketch;
    }

    /*
  /*
    private class SimpleButton extends ActiveElement {
        private boolean on;

        public SimpleButton ( float x, float y, float w, float h ) {
            // this registers this button with the "manager" and sets "hot area"
            super( x,y,w,h );
            on = false;
            Interactive.add(this);
        }

        // one possible callback, automatically called
        // by manager when button clicked
        public void mousePressed() {
            on = !on;
        }

        public void draw() {
            if (hover) {
                stroke(255);
            } else {
                noStroke();
            }

            if (on) {
                fill(200);
            } else {
                fill(100);
            }
            rect(x, y, width, height);
        }
    }

    public class Slider {
        private float x, y, width, height;
        private float valueX = 0;
        private float value; // change this one to type double if you need the extra precision

        public Slider ( float xx, float yy, float ww, float hh ) {
            x = xx;
            y = yy;
            width = ww;
            height = hh;

            valueX = x;

            // register it
            Interactive.add( this );
        }

        // called from manager
        public void mouseDragged ( float mx, float my, float dx, float dy ) {
            valueX = mx - height/2;

            if ( valueX < x ) valueX = x;
            if ( valueX > x+width-height ) valueX = x+width-height;

            value = map( valueX, x, x+width-height, 0, 1 );
        }

        public void draw () {
            noStroke();

            fill( 100 );
            rect(x, y, width, height);

            fill( 120 );
            rect( valueX, y, height, height );
        }
    }
    */
    /*
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + mat.dump());


    public void toggleFilter() {
        filterOn = !filterOn;
    }

    public void toggleBlend() {
        blendOn = !blendOn;
    }

        private boolean filterOn = false;
    private boolean blendOn = false;



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

    /*
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

    private PImage savedImage;
    private PImage scratchImage;
    private boolean[][] booleanImage;
    private boolean[][] booleanScratch;

    public ImageProcessor(PImage image) {
        savedImage = image;
        scratchImage = new PImage(image.width, image.height);
        booleanImage = new boolean[image.height][image.width];
        booleanScratch = new boolean[image.height][image.width];
    }

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

    public boolean[][] dilateFilter(boolean[][] image) {
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

    public boolean[][] erodeFilter(boolean[][] image) {
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

    public boolean[][] cleanFilter(boolean[][] image) {
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

    */



    /*


    private PImage applyErode(PImage image) {
        PImage newImage = new PImage(image.width, image.height);
        newImage.loadPixels();
        image.loadPixels();

        for (int x = 0; x < image.width; x++) {
            for (int y = 0; y < image.height; y++) {
                newImage.pixels[x + y * image.width] = erode(x, y, image);
            }
        }

        newImage.updatePixels();
        return newImage;
    }

    private int erode(int x, int y, PImage img) {
        final int[] rVal = {0, 1, 2, 1};
        final int[] cVal = {1, 0, 1, 2};
        boolean centerVal = getbinary(x + y*img.width, img);

        for (int i = 0; i < rVal.length && centerVal; i++) {
            for (int j= 0; j < cVal.length && centerVal; j++) {
                // What pixel are we testing
                int xloc = x+rVal[i]-1;
                int yloc = y+cVal[j]-1;
                int loc = xloc + img.width*yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = constrain(loc,0,img.pixels.length-1);
                boolean otherVal = getbinary(loc, img);

                if (!otherVal) {
                    centerVal = false;
                }
            }
        }

        return getColour(centerVal);
    }

    private PImage applyDilate(PImage image) {
        PImage newImage = new PImage(image.width, image.height);
        newImage.loadPixels();
        image.loadPixels();

        for (int x = 0; x < image.width; x++) {
            for (int y = 0; y < image.height; y++) {
                newImage.pixels[x + y * image.width] = dilate(x, y, image);
            }
        }

        newImage.updatePixels();
        return newImage;
    }

    private int dilate(int x, int y, PImage img) {
        final int[] rVal = {0, 1, 2, 1};
        final int[] cVal = {1, 0, 1, 2};
        boolean centerVal = getbinary(x + y*img.width, img);

        for (int i = 0; i < rVal.length && !centerVal; i++) {
            for (int j= 0; j < cVal.length && !centerVal; j++) {
                // What pixel are we testing
                int xloc = x+rVal[i]-1;
                int yloc = y+cVal[j]-1;
                int loc = xloc + img.width*yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = constrain(loc,0,img.pixels.length-1);
                boolean otherVal = getbinary(loc, img);

                if (otherVal) {
                    centerVal = true;
                }
            }
        }

        return getColour(centerVal);
    }

    private int getColour(boolean value) {
        if (value) {
            return color(255, 255, 255);
        } else {
            return color(0, 0, 0);
        }
    }

    private boolean getbinary(int loc, PImage img) {
        return red(img.pixels[loc]) > 64;
    }


    private static final float[][] logMatrix = {
            { 0, 0,  1 , 0, 0 },
            { 0, 1,  2 , 1, 0 },
            { 1, 2, -16, 2, 1 },
            { 0, 1,  2 , 1, 0 },
            { 0, 0,  1 , 0, 0 }
    };


    public enum Filter {
        DILATE,
        ERODE,
        LOG,
        GRAY
    }

    private void convertToGrey(PImage image) {
        image.filter(PApplet.GRAY); //turn to gray scale
        image.loadPixels();
        for (int x = 0; x < image.width; x++) {
            for (int y = 0; y < image.height; y++) {
                image.pixels[x + y * image.width] = (int)red(image.pixels[x + y * image.width]);
            }
        }
        image.updatePixels();
    }

    public PImage addFilter(PImage image, Filter filter, int times) {
        PImage newImage = new PImage(image.width, image.height); //createImage(toDraw.width, toDraw.height, JPEG);
        PImage temp;
        boolean firstTIme = true;

        if (filter == Filter.GRAY || filter == Filter.LOG) {
            convertToGrey(image);
        }

        if (filter != Filter.GRAY) {
            newImage.loadPixels();
            image.loadPixels();
            for (int i = 0; i < times; i++) {
                if (!firstTIme) {
                    temp = image;
                    image = newImage;
                    newImage = temp;
                } else {
                    firstTIme = false;
                }

                for (int x = 0; x < image.width; x++) {
                    for (int y = 0; y < image.height; y++) {
                        switch (filter) {
                            case ERODE:
                                newImage.pixels[x + y * image.width] = erode(x, y, image);
                                break;
                            case DILATE:
                                newImage.pixels[x + y * image.width] = dilate(x, y, image);
                                break;
                            case LOG:
                                newImage.pixels[x + y * image.width] = convolutionGrey(x, y, logMatrix, logMatrix.length, image);
                                break;
                        }
                    }
                }
            }
        } else {
            return image;
        }

        newImage.updatePixels();
        return newImage;
    }

    private boolean getbinary(int loc, PImage img) {
        return red(img.pixels[loc]) > 64;
    }

    private boolean[][] getBinaryMatrix(PImage image) {
        boolean[][] result = new boolean[image.height][image.width];
        image.loadPixels();

        for (int r = 0; r < result.length; r++) {
            for (int c = 0; c < result[r].length; c++) {
                result[r][c] = image.pixels[c + image.width*r] > 64;
            }
        }

        return result;
    }

    private PImage getPImage(boolean[][] pixels) {
        PImage result = null;
        if (pixels.length >= 0) {
            result = new PImage(pixels.length, pixels[0].length);
            result.loadPixels();

            for (int r = 0; r < pixels.length; r++) {
                for (int c = 0; c < pixels[r].length; c++) {
                    result.pixels[c + result.width * r] = (pixels[r][c]) ? 255 : 0;
                }
            }

            result.updatePixels();
        }
        return result;
    }

    public PImage dilateFilter(PImage image) {
        final int[] rVal = {0, 1, 2, 1};
        final int[] cVal = {1, 0, 1, 2};
        boolean[][] pixels = getBinaryMatrix(image);
        boolean[][] result = new boolean[pixels.length][pixels[0].length];
        boolean centerVal;
        boolean otherVal;

        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[r].length; c++) {
                centerVal = pixels[r][c];

                for (int i = 0; i < rVal.length && !centerVal; i++) {
                    for (int j = 0; j < cVal.length && !centerVal; j++) {
                        int rLoc = constrain(r+rVal[i]-1, 0, pixels.length-1);
                        int cLoc = constrain(c+cVal[j]-1,0, pixels[rLoc].length-1);
                        otherVal = pixels[rLoc][cLoc];

                        if (otherVal) {
                            centerVal = true;
                        }
                    }
                }

                result[r][c] = centerVal;
            }
        }

        return getPImage(result);
    }

    private int getColour(boolean value) {
        if (value) {
            return color(255, 255, 255);
        } else {
            return color(0, 0, 0);
        }
    }

    private int erode(int x, int y, PImage img) {
        boolean centerVal = getbinary(x + y*img.width, img);
        boolean isSomething = false;
        int[] rVal = {0, 1, 2, 1};
        int[] cVal = {1, 0, 1, 2};

        for (int i = 0; i < rVal.length && centerVal; i++) {
            for (int j= 0; j < cVal.length && centerVal; j++) {
                // What pixel are we testing
                int xloc = x+rVal[i]-1;
                int yloc = y+cVal[j]-1;
                int loc = xloc + img.width*yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = constrain(loc,0,img.pixels.length-1);
                boolean otherVal = getbinary(loc, img);

                if (otherVal) {
                    isSomething = true;
                    //centerVal = false;
                }
            }
        }

        if (isSomething) {
            centerVal = centerVal;
        } else {
            centerVal = false;
        }

        return getColour(centerVal);
    }



    private int dilate(int x, int y, PImage img) {
        boolean centerVal = getbinary(x + y*img.width, img);

        int[] rVal = {0, 1, 2, 1};
        int[] cVal = {1, 0, 1, 2};

        for (int i = 0; i < rVal.length && !centerVal; i++) {
            for (int j= 0; j < cVal.length && !centerVal; j++) {
                // What pixel are we testing
                int xloc = x+rVal[i]-1;
                int yloc = y+cVal[j]-1;
                int loc = xloc + img.width*yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = constrain(loc,0,img.pixels.length-1);
                boolean otherVal = getbinary(loc, img);

                if (otherVal) {
                    centerVal = true;
                }
            }
        }

        return getColour(centerVal);
    }

    private int convolutionRGB(int x, int y, float[][] matrix, int matrixSize, PImage img) {
        float rtotal = (float) 0.0;
        float gtotal = (float) 0.0;
        float btotal = (float) 0.0;
        int offset = matrixSize / 2;

        for (int i = 0; i < matrixSize; i++){
            for (int j= 0; j < matrixSize; j++){
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

    private int convolutionGrey(int x, int y, float[][] matrix, int matrixSize, PImage img) {
        int total = 0;
        int offset = matrixSize / 2;

        for (int i = 0; i < matrixSize; i++){
            for (int j= 0; j < matrixSize; j++){
                // What pixel are we testing
                int xloc = x+i-offset;
                int yloc = y+j-offset;
                int loc = xloc + img.width*yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = constrain(loc,0,img.pixels.length-1);

                // Calculate the convolution
                total += ((img.pixels[loc]) * matrix[i][j]);
            }
        }

        // Return the resulting color
        return total;
    }
    */
}
