package pvz;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import pvz.model.LoadingScreen;
import pvz.model.AudioSettings;
import pvz.model.PlayerStore;
import pvz.model.Yard;
import pvz.ui.AuthFormPane;
import pvz.ui.GameMenuPane;
import pvz.ui.ImageMenuPane;
import pvz.ui.WallNutBowlingPane;

/**
 * Application entry point. Shows the image-based main menu and opens small
 * dialogs for Sign In / Sign Up when the corresponding slab is clicked.
 *
 * Keys:
 * - F2 toggles a visual overlay (drawn by ImageMenuPane) so you can see/adjust
 *   hotspot bounds while tuning.
 */
public class Main extends Application {
    private MediaPlayer menuMusicPlayer;
    private final PlayerStore store = new PlayerStore();
    private boolean debug = true;
    private StackPane rootPane;
    private Scene menuScene;

    @Override
    public void start(Stage stage) {
        playMenuMusic();
        store.load();

        try {
            var in = getClass().getResourceAsStream("/pvz/images/GameIcon/pfp.jpg");
            if (in != null) {
                stage.getIcons().add(new Image(in));
            }
        } catch (Exception ignored) {
        }

        rootPane = new StackPane();
        ImageMenuPane menu = new ImageMenuPane();
        rootPane.getChildren().add(menu);

        menu.setHandler(new ImageMenuPane.Handler() {
            @Override public void onSignIn() { showAuth(rootPane, stage, AuthFormPane.Mode.SIGN_IN); }
            @Override public void onSignUp() { showAuth(rootPane, stage, AuthFormPane.Mode.SIGN_UP); }
            @Override public void onExit() { Platform.exit(); }
        });

        menuScene = new Scene(rootPane, menu.getPrefWidth(), menu.getPrefHeight());
        stage.setScene(menuScene);
        stage.setTitle("PvZ Menu");
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();
    }

    /** Convenience to show an information/error popup. */
    private void show(Alert.AlertType t, String msg) {
        Alert a = new Alert(t, msg); a.setHeaderText(null); a.showAndWait();
    }

    private void showAuth(StackPane root, Stage stage, AuthFormPane.Mode mode) {
        playMenuMusic();
        Pane dim = new Pane();
        dim.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        dim.setMinSize(Double.MAX_VALUE, Double.MAX_VALUE);
        dim.prefWidthProperty().bind(root.widthProperty());
        dim.prefHeightProperty().bind(root.heightProperty());
        dim.setOnMouseClicked(e -> {});

        AuthFormPane form = new AuthFormPane(mode);
        form.setHandler(new AuthFormPane.Handler() {
            @Override public void onSubmit(String username, String password) {
                boolean ok = switch (mode) {
                    case SIGN_IN -> store.signIn(username, password);
                    case SIGN_UP -> store.createAccount(username, password);
                };
                System.out.println("DEBUG: mode=" + mode + ", username=" + username + ", password=" + password + ", ok=" + ok);
                if (ok) {
                    if (mode == AuthFormPane.Mode.SIGN_UP) {
                        store.signIn(username, password);
                    }
                    show(Alert.AlertType.INFORMATION, (mode==AuthFormPane.Mode.SIGN_IN?"Signed in":"Account created") + " successfully.");
                    root.getChildren().removeAll(dim, form);
                    if (mode == AuthFormPane.Mode.SIGN_IN || mode == AuthFormPane.Mode.SIGN_UP) {
                        System.out.println("DEBUG: Showing game menu for " + username);
                        showGameMenu(root, stage, username);
                    }
                } else {
                    show(Alert.AlertType.ERROR, mode==AuthFormPane.Mode.SIGN_IN ? "Invalid username or password." : "Username exists or invalid.");
                }
            }
            @Override public void onBack() { root.getChildren().removeAll(dim, form); }
        });
        root.getChildren().addAll(dim, form);
        StackPane.setAlignment(form, Pos.CENTER);
        form.requestFocus();
    }

    private void showGameMenu(StackPane root, Stage stage, String username) {
        if (menuScene != null && stage.getScene() != menuScene) {
            stage.setScene(menuScene);
        }
        stage.setTitle("PvZ Menu");
        stage.setResizable(false);
        stage.sizeToScene();
        root.getChildren().clear();
        GameMenuPane gameMenu = new GameMenuPane(username);
        root.getChildren().add(gameMenu);
        gameMenu.setUsernamePosition(55, 100, 18, javafx.scene.paint.Color.WHITE);
        gameMenu.setHandler(new GameMenuPane.Handler() {
            @Override public void onPlay() {
                // Play image is now shown directly in GameMenuPane
            }
            @Override public void onOptions() {
                // Options overlay is handled directly by GameMenuPane.
            }
            @Override public void onMore() {
                // The minigames overlay already appears when the More hotspot is clicked.
            }
            @Override public void onWalnutMinigame() {
                showWalnutMiniGame(rootPane, stage, username);
            }
            @Override public void onLogout() {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Logout");
                confirm.setHeaderText("Logout");
                confirm.setContentText("Are you sure you want to logout?");
                if (confirm.showAndWait().isPresent() && 
                    confirm.getResult() == javafx.scene.control.ButtonType.OK) {
                    store.signOut();
                    root.getChildren().clear();
                    ImageMenuPane menu = new ImageMenuPane();
                    root.getChildren().add(menu);
                    menu.setHandler(new ImageMenuPane.Handler() {
                        @Override public void onSignIn() { showAuth(root, stage, AuthFormPane.Mode.SIGN_IN); }
                        @Override public void onSignUp() { showAuth(root, stage, AuthFormPane.Mode.SIGN_UP); }
                        @Override public void onExit() { Platform.exit(); }
                    });
                    playMenuMusic();
                    show(Alert.AlertType.INFORMATION, "Logged out successfully.");
                }
            }
            @Override public void onDeleteAccount() {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Account");
                confirm.setHeaderText("Delete Account");
                confirm.setContentText("Are you sure you want to delete your account? This cannot be undone.");
                if (confirm.showAndWait().isPresent() && 
                    confirm.getResult() == javafx.scene.control.ButtonType.OK) {
                    if (store.deleteAccount(username, "")) {
                        show(Alert.AlertType.INFORMATION, "Account deleted successfully.");
                        store.signOut();
                        root.getChildren().clear();
                        ImageMenuPane menu = new ImageMenuPane();
                        root.getChildren().add(menu);
                        menu.setHandler(new ImageMenuPane.Handler() {
                            @Override public void onSignIn() { showAuth(root, stage, AuthFormPane.Mode.SIGN_IN); }
                            @Override public void onSignUp() { showAuth(root, stage, AuthFormPane.Mode.SIGN_UP); }
                            @Override public void onExit() { Platform.exit(); }
                        });
                        playMenuMusic();
                    } else {
                        show(Alert.AlertType.ERROR, "Failed to delete account.");
                    }
                }
            }
            @Override public void onExit() {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Exit");
                confirm.setHeaderText("Exit Application");
                confirm.setContentText("Are you sure you want to exit the application?");
                if (confirm.showAndWait().isPresent() && 
                    confirm.getResult() == javafx.scene.control.ButtonType.OK) {
                    Platform.exit();
                }
            }
            @Override public void onLevelSelected(int level) {
                stopMenuMusic();
                Yard.setActivePlayerUsername(username);
                Level lvl = new Level(level);
                lvl.startLevel(stage);
            }
        });
    }

    private void showWalnutMiniGame(StackPane root, Stage stage, String username) {
        stopMenuMusic();
        LoadingScreen.show(stage, () -> Platform.runLater(() -> showWalnutScene(stage, username)));
    }

    private void showWalnutScene(Stage stage, String username) {
        WallNutBowlingPane pane = new WallNutBowlingPane(stage, () -> {
            showGameMenu(rootPane, stage, username);
            playMenuMusic();
        });
        Scene minigameScene = new Scene(pane, WallNutBowlingPane.WIDTH, WallNutBowlingPane.HEIGHT);
        stage.setScene(minigameScene);
        stage.setTitle("PvZ - Wall-nut Bowling");
        stage.setResizable(false);
        stage.sizeToScene();
    }

    private void playMenuMusic() {
        if (menuMusicPlayer == null) {
            try {
                String musicPath = getClass().getResource("/pvz/music/menu_music.mp3").toString();
                Media media = new Media(musicPath);
                menuMusicPlayer = new MediaPlayer(media);
                menuMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } catch (Exception e) {
                System.err.println("Could not load menu music: " + e);
                menuMusicPlayer = null;
            }
        }
        if (menuMusicPlayer != null) {
            AudioSettings.register(menuMusicPlayer);
            AudioSettings.apply(menuMusicPlayer);
            menuMusicPlayer.play();
        }
    }

    private void stopMenuMusic() {
        if (menuMusicPlayer != null) menuMusicPlayer.stop();
    }

    public static void main(String[] args) { launch(args); }
}
