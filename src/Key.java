/**
 * Created by Nicholas on 2017-05-19.
 */
public class Key{
    public static Key forward = new Key();
    public static Key back = new Key();
    public static Key left = new Key();
    public static Key right = new Key();
    public static Key park = new Key();
    public static Key stop = new Key();
    public static Key up = new Key();
    public static Key down = new Key();
    public static Key filterToggle = new Key();
    public static Key blendToggle = new Key();
    public static Key fav1 = new Key();
    public static Key fav2 = new Key();
    public static Key fav3 = new Key();
    public static Key fav4 = new Key();
    public static Key fav5 = new Key();
    public static Key setFav = new Key();

    private boolean isDown = false;
    private boolean wasDown = false;
    private boolean dealtWith = true;

    public boolean wasPressed() {
        boolean result = isDown && !dealtWith;
        if (result) {
            dealtWith = true;
        }
        return result;
    }

    public boolean wasReleased() {
        boolean result = !isDown && !dealtWith;
        if (result) {
            dealtWith = true;
        }
        return result;
    }

    public void setDown() {
        wasDown = isDown;
        isDown = true;
        if (isDown != wasDown) {
            dealtWith = false;
        }
    }

    public void setUp() {
        wasDown = isDown;
        isDown = false;
        if (isDown != wasDown) {
            dealtWith = false;
        }
    }

}
