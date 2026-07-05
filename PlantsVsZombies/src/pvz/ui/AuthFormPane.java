package pvz.ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Image-based auth form overlay (popover) with two input fields and a back area.
 *
 * - Shows a background image (tombstone panel) with two slots.
 * - Places TextField (username) and PasswordField (password) exactly over the slots.
 * - Press Enter to submit.
 * - Click the bottom bar ("Back to Main Menu") to go back.
 *
 * The coordinates use normalized rectangles so you can tweak easily if your
 * artwork changes. If the image fails to load, the form still works with
 * default sizes.
 */
public class AuthFormPane extends StackPane {
    public enum Mode { SIGN_IN, SIGN_UP }
    public interface Handler {
        void onSubmit(String username, String password);
        void onBack();
    }

    private final Mode mode;
    private final ImageView bgView;
    private final TextField username;
    private final PasswordField password;
    private final Button backButton;
    private final Pane overlay; // container for inputs/click area

    private Handler handler;

    // Normalized placements tuned to the provided artwork (adjust if needed)
    // These values were tweaked to sit inside the inner slots and the bottom bar.
    private Rectangle2D rUser   = rect(0.32, 0.34, 0.99, 0.2);
    private Rectangle2D rPass   = rect(0.32, 0.48, 0.99, 0.2);
    private Rectangle2D rBack   = rect(0.3, 0.650, 1, 0.105);

    public AuthFormPane(Mode mode) {
        this.mode = mode;

        // Try to load the panel image from resources. If missing, size will
        // come from a fallback (500x320) and the inputs still work.
    // Use the provided tombstone image that has two slots and a
    // "BACK TO MAIN MENU" bar.
    // Path points to an existing asset in this project:
    //   src/pvz/images/menu/bgup-in.png
    Image bg = load("/pvz/images/menu/bgup-in.png");
        if (bg == null) {
            // Fallback size
            setPrefSize(500, 320);
            bgView = new ImageView();
        } else {
            bgView = new ImageView(bg);
            bgView.setPreserveRatio(false);
            bgView.setFitWidth(bg.getWidth());
            bgView.setFitHeight(bg.getHeight());
            setPrefSize(bg.getWidth(), bg.getHeight());
        }

        username = new TextField();
        username.setPromptText("Username");
        username.setStyle("-fx-background-color: rgba(0,0,0,0.25); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.6); -fx-font-size: 16px; -fx-background-radius: 6;");

    password = new PasswordField();
        password.setPromptText("Password");
        password.setStyle("-fx-background-color: rgba(0,0,0,0.25); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.6); -fx-font-size: 16px; -fx-background-radius: 6;");
        
    backButton = new Button("                                                     ");
    backButton.setFocusTraversable(false);
    backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;-fx-font-weight: bold; -fx-font-size: 16px;");
    backButton.setOnMouseEntered(e -> setCursor(Cursor.HAND));
    backButton.setOnMouseExited(e -> setCursor(Cursor.DEFAULT));
    backButton.setOnAction(e -> back());

        overlay = new Pane();
        overlay.setPickOnBounds(false); // only click on actual children

        getChildren().addAll(bgView, overlay);

        // Add fields to the overlay and position them over the slots
        overlay.getChildren().addAll(username, password, backButton);
        layoutChildrenToImage();


        // Enter submits
        setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> submit();
                default -> {}
            }
        });

        // Focus first field when shown
        username.requestFocus();

        widthProperty().addListener((obs,o,n) -> layoutChildrenToImage());
        heightProperty().addListener((obs,o,n) -> layoutChildrenToImage());
    }

    public void setHandler(Handler handler) { this.handler = handler; }

    public TextField getUsernameField() { return username; }
    public PasswordField getPasswordField() { return password; }

    private void submit() {
        if (handler != null) handler.onSubmit(username.getText()==null?"":username.getText().trim(), password.getText());
    }

    private void back() {
        if (handler != null) handler.onBack();
    }

    private void layoutChildrenToImage() {
        double iw = bgView.getImage()==null? getWidth(): bgView.getImage().getWidth();
        double ih = bgView.getImage()==null? getHeight(): bgView.getImage().getHeight();
        if (iw <= 0 || ih <= 0) { iw = Math.max(500, getWidth()); ih = Math.max(320, getHeight()); }

        // Keep the pane sized to the image dimensions if we have one
        if (bgView.getImage()!=null) {
            bgView.setFitWidth(iw);
            bgView.setFitHeight(ih);
            setMinSize(iw, ih); setMaxSize(iw, ih); setPrefSize(iw, ih);
        }

        place(username, rUser, iw, ih);
        place(password, rPass, iw, ih);
        place(backButton, rBack, iw, ih);
    }

    private void place(javafx.scene.control.Control c, Rectangle2D r, double iw, double ih) {
        double x = r.getMinX()*iw, y = r.getMinY()*ih, w = r.getWidth()*iw, h = r.getHeight()*ih;
        c.resizeRelocate(x, y, w, h);
    }

    private boolean containsNormalized(Rectangle2D r, double x, double y) {
        double iw = getWidth();
        double ih = getHeight();
        double rx = r.getMinX()*iw, ry = r.getMinY()*ih, rw = r.getWidth()*iw, rh = r.getHeight()*ih;
        return x>=rx && x<=rx+rw && y>=ry && y<=ry+rh;
    }

    private static Rectangle2D rect(double x, double y, double w, double h) { return new Rectangle2D(x,y,w,h); }
    private Image load(String path) { var in = getClass().getResourceAsStream(path); return in==null?null:new Image(in); }
}
