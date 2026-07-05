package pvz.model;

import javafx.scene.image.ImageView;

import pvz.util.AssetLoader;

public class TorchWood extends Plant {
    public TorchWood() {
        super(175, 30, 200);
    }

    public TorchWood(int x, int y) {
        this();
        this.x = x;
        this.y = y;
        String spritePath = Yard.isPixelArtMode()
            ? "images/pixelart/pixelartplants/PixelTorchWood.gif"
            : "images/plants/torchWood.gif";
        ImageView sprite = new ImageView(AssetLoader.loadImage(spritePath));
        sprite.setFitWidth(80);
        sprite.setFitHeight(100);
        sprite.setPreserveRatio(true);
        sprite.setLayoutX(x - sprite.getFitWidth() / 2.0);
        sprite.setLayoutY(y - sprite.getFitHeight());
        setSprite(sprite);
    }

    @Override
    public void run() {
        // Passive buff plant – logic can be added later.
    }
}
