public class Key{
    public static Key forward = new Key();
    public static Key back = new Key();
    public static Key left = new Key();
    public static Key right = new Key();
    public static Key park = new Key();
    public static Key stop = new Key();
    public static Key up = new Key();
    public static Key down = new Key();

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

    private void print() {
        System.out.println();
        System.out.println("isDown: "+ isDown);
        System.out.println("wasDown: "+ wasDown);
        System.out.println("dealtWith: "+ dealtWith);
    }

}