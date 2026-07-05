package pvz.model;

import javafx.scene.image.ImageView;

import pvz.util.AssetLoader;

public class Sunflower_Christmas extends Sunflower {
    public Sunflower_Christmas() {
        super();
    }

    public Sunflower_Christmas(int x, int y) {
        super(x, y);
        // Use the existing Christmas sunflower art available in assets
        ImageView sprite = new ImageView(AssetLoader.loadImage("images/plants/sunflowerChristmas.gif"));
        sprite.setFitWidth(73);
        sprite.setFitHeight(70);
        sprite.setPreserveRatio(true);
        sprite.setLayoutX((x - sprite.getFitWidth() / 2) + 5);
        sprite.setLayoutY((y - sprite.getFitHeight() / 2) - 15);
        setSprite(sprite);
    }
}
