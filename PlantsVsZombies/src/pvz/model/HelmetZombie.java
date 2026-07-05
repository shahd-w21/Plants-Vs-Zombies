package pvz.model;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import pvz.util.AssetLoader;

public class HelmetZombie extends Zombie {
    public HelmetZombie() {
        super(10, 0.4, 150);
        String spritePath = Yard.isPixelArtMode()
            ? "images/pixelart/pixelartzombies/PixelBucketHeadZombie.gif"
            : "images/zombies/BucketheadZombie.gif";
        elementImage = new ImageView(AssetLoader.loadImage(spritePath));
        elementImage.setFitWidth(135);
        elementImage.setFitHeight(120);
        elementImage.setPreserveRatio(true);
    }

    public HelmetZombie(int x, int y) {
        this();
        super.x = x;
        super.y = y;
        elementImage.setLayoutX(x);
        elementImage.setLayoutY(y);
    }

        @Override
    public void appear(Pane root) {

    }
}
