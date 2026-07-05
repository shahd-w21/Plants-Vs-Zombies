package pvz.model;

import javafx.scene.image.ImageView;

import pvz.util.AssetLoader;

public class DefaultZombie extends Zombie {
    public DefaultZombie() {
        super(10, 0.5, 90);
        String spritePath = Yard.isPixelArtMode()
            ? "images/pixelart/pixelartzombies/PixelDefaultZombie.gif"
            : "images/zombies/Zombie.gif";
        ImageView view = new ImageView(AssetLoader.loadImage(spritePath));
        view.setFitWidth(134);
        view.setFitHeight(155);
        view.setPreserveRatio(true);
        this.elementImage = view;
    }

    public DefaultZombie(int x, int y) {
        this();
        this.x = x;
        this.y = y;
    }
}
