/*
 * Stolen from https://stackoverflow.com/users/1276856/basically-alan-turing
 */

public class Key{
    public static Key forward = new Key();
    public static Key back = new Key();
    public static Key left = new Key();
    public static Key right = new Key();
    public static Key park = new Key();

    public static Key special = new Key();

    public boolean isDown;
    public boolean wasDown;

    public boolean isDown() {
        return isDown();
    }

    public boolean isUpdate() {
        boolean result = isDown && !wasDown;
        wasDown = isDown;
        return result;
    }

    public void setDown() {
        wasDown = isDown;
        isDown = true;
    }

    public void setUp() {
        wasDown = isDown;
        isDown = false;
    }
}