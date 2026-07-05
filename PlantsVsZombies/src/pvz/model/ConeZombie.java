package pvz.model;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import pvz.util.AssetLoader;

public class ConeZombie extends Zombie {
    public ConeZombie() {
        super(10, 0.45, 135);
        String spritePath = Yard.isPixelArtMode()
            ? "images/pixelart/pixelartzombies/PixelConeHeadZombie.gif"
            : "images/zombies/ConeZombie.gif";
        ImageView view = new ImageView(AssetLoader.loadImage(spritePath));
        view.setFitHeight(155);
        view.setFitWidth(134);
        view.setPreserveRatio(true);
        this.elementImage = view;
    }

    public ConeZombie(int x, int y) {
        this();
        this.x = x;
        this.y = y;
    }
        @Override
    public void appear(Pane root) {

    }
}
