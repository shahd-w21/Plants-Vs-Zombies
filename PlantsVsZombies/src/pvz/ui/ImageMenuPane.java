package pvz.ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.Objects;

public class ImageMenuPane extends StackPane {
    public interface Handler {
        void onSignIn();
        void onSignUp();
        void onExit();
    }

    /** Base background image. */
    private final Image baseImg;
    /** Hover image for the Sign In slab. */
    private final Image hover1; // Sign In
    /** Hover image for the Sign Up slab. */
    private final Image hover2; // Sign Up
    /** Hover image for the Exit slab. */
    private final Image hover3; // Exit

    private final ImageView view;
    private final Canvas overlay;

    // Normalized hotspot rects (x,y,w,h in 0..1)
    private Rectangle2D rSignIn;
    private Rectangle2D rSignUp;
    private Rectangle2D rExit;

    private Handler handler;

    public ImageMenuPane() {
        baseImg = load("/pvz/images/menu/signin.jpg");
        hover1 = load("/pvz/images/menu/hover1.jpg");
        hover2 = load("/pvz/images/menu/hover2.jpg");
        hover3 = load("/pvz/images/menu/hover3.jpg");
        if (baseImg == null || hover1 == null || hover2 == null || hover3 == null) {
            throw new IllegalStateException("Missing images under /pvz/images (menu_bg.jpg, hover1.jpg, hover2.jpg, hover3.jpg)");
        }

    // Approximate slab bounds (tuned to provided artwork). Adjust easily here.
    // Right-side tombstone region, three slabs stacked.
    // x: 52%..87%, y slices for each row.
    rSignIn = rect(0.52, 0.33, 0.35, 0.14);
    rSignUp = rect(0.52, 0.45, 0.35, 0.14);
    rExit   = rect(0.52, 0.56, 0.35, 0.14);

        view = new ImageView(baseImg);
        view.setPreserveRatio(false);
        view.setFitWidth(baseImg.getWidth());
        view.setFitHeight(baseImg.getHeight());

    overlay = new Canvas(baseImg.getWidth(), baseImg.getHeight());
    overlay.setMouseTransparent(true); // don't block mouse events to the ImageView
        getChildren().addAll(view, overlay);

        setPrefSize(baseImg.getWidth(), baseImg.getHeight());
        setMinSize(baseImg.getWidth(), baseImg.getHeight());
        setMaxSize(baseImg.getWidth(), baseImg.getHeight());

        // Hover and click logic
        view.setOnMouseMoved(e -> {
            int which = whichHotspot(e.getX(), e.getY());
            switch (which) {
                case 1 -> { view.setImage(hover1); setCursor(Cursor.HAND); }
                case 2 -> { view.setImage(hover2); setCursor(Cursor.HAND); }
                case 3 -> { view.setImage(hover3); setCursor(Cursor.HAND); }
                default -> { view.setImage(baseImg); setCursor(Cursor.DEFAULT); }
            }
        });
        view.setOnMouseExited(e -> { view.setImage(baseImg); setCursor(Cursor.DEFAULT); });

        view.setOnMouseClicked(e -> {
            if (handler == null) return;
            switch (whichHotspot(e.getX(), e.getY())) {
                case 1 -> handler.onSignIn();
                case 2 -> handler.onSignUp();
                case 3 -> handler.onExit();
                default -> {}
            }
        });

        widthProperty().addListener((obs, o, w) -> resizeToImage());
        heightProperty().addListener((obs, o, h) -> resizeToImage());
    }

    private void resizeToImage() {
        // Keep node sized exactly to the image; don't scale the image.
        view.setFitWidth(baseImg.getWidth());
        view.setFitHeight(baseImg.getHeight());
        overlay.setWidth(baseImg.getWidth());
        overlay.setHeight(baseImg.getHeight());
        setPrefSize(baseImg.getWidth(), baseImg.getHeight());
    }

    /** Register callbacks for click events. */
    public void setHandler(Handler handler) { this.handler = handler; }


    private int whichHotspot(double x, double y) {
        if (containsNormalized(rSignIn, x, y)) return 1;
        if (containsNormalized(rSignUp,  x, y)) return 2;
        if (containsNormalized(rExit,    x, y)) return 3;
        return 0;
    }

    /** Return true if (x,y) in pixels lies inside normalized rect r. */
    private boolean containsNormalized(Rectangle2D r, double x, double y) {
        double iw = baseImg.getWidth(), ih = baseImg.getHeight();
        double rx = r.getMinX() * iw;
        double ry = r.getMinY() * ih;
        double rw = r.getWidth() * iw;
        double rh = r.getHeight() * ih;
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    /** Load an image from classpath, returning null if not found. */
    private Image load(String path) {
        var in = getClass().getResourceAsStream(path);
        return in == null ? null : new Image(in);
    }

    /** Helper to build a normalized rectangle. */
    private Rectangle2D rect(double x, double y, double w, double h) {
        return new Rectangle2D(x, y, w, h);
    }
}