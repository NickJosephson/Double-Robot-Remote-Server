import java.awt.event.*;
import java.util.*;

/*
 * Stolen from https://stackoverflow.com/users/1276856/basically-alan-turing
 */
public class Controller implements KeyListener{
    private Sketch mainClass;

    public Controller(Sketch main){
        bind(KeyEvent.VK_W, Key.forward);
        bind(KeyEvent.VK_A, Key.left);
        bind(KeyEvent.VK_S, Key.back);
        bind(KeyEvent.VK_D, Key.right);
        bind(KeyEvent.VK_SPACE, Key.special);
        mainClass = main;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        other[e.getExtendedKeyCode()] = true;
        keyBindings.get(e.getKeyCode()).isDown = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        other[e.getExtendedKeyCode()] = false;
        keyBindings.get(e.getKeyCode()).isDown = false;
    }

    public boolean isKeyBinded(int extendedKey){
        return keyBindings.containsKey(extendedKey);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }


    public void bind(Integer keyCode, Key key){
        keyBindings.put(keyCode, key);
    }

    public void releaseAll(){
        for(Key key : keyBindings.values()){
            key.isDown = false;
        }
    }

    public HashMap<Integer, Key> keyBindings = new HashMap<Integer, Key>();
    public static boolean other[] = new boolean[256];
}