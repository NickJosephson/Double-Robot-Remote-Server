import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Nicholas on 2017-07-11.
 */
public class KeyThread extends Thread{
    private Sketch sketch;
    private boolean keepFetching = true;

    public KeyThread(Sketch sketch) {
        super("KeyThread");
        this.sketch = sketch;
    }

    public void run() {
        while (keepFetching) {
            handleKeys();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void handleKeys() {
        if (Key.filterToggle.wasReleased()) {
            sketch.toggleFiltering();
        } else if (Key.blendToggle.wasReleased()) {
            //sketch.toggleBlend();
        } else if (Key.fav1.wasReleased()) {
            sketch.switchFav(0);
        } else if (Key.fav2.wasReleased()) {
            sketch.switchFav(1);
        } else if (Key.fav3.wasReleased()) {
            sketch.switchFav(2);
        } else if (Key.fav4.wasReleased()) {
            sketch.switchFav(3);
        } else if (Key.fav5.wasReleased()) {
            sketch.switchFav(4);
        } else if (Key.setFav.wasReleased()) {
            sketch.setFavorite();
        }
    }
}

