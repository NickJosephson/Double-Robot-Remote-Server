import processing.core.PImage;
import processing.core.*;

/**
 * Created by Nicholas on 2017-05-26.
 */
public class ImageProcessor extends PApplet {
    /*
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
