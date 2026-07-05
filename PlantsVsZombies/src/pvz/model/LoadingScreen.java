package pvz.model;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

import pvz.util.AssetLoader;

public class LoadingScreen
{
	private static AnchorPane root;

	private static final int LOADING_SCREEN_TIME = 7;

	private static final String[] LOADING_SCREEN_PATHS = {
			"images/loadingScreens/feastivus_loadingscreen.png",
			"images/loadingScreens/pvz2_loadingscreen.png"
	};

	private static String getRandomLoadingScreen()
	{
		Random random = new Random();
		int randomIndex = random.nextInt(LOADING_SCREEN_PATHS.length);
		return LOADING_SCREEN_PATHS[randomIndex];
	}

	public static void show(Stage stage, Runnable onFinished)
	{
		// Root AnchorPane
		root = new AnchorPane();
		root.setPrefSize(Yard.WIDTH, Yard.HEIGHT);

		// Child AnchorPane
		AnchorPane childPane = new AnchorPane();
		childPane.setLayoutX(0.0);
		childPane.setPrefSize(Yard.WIDTH, Yard.HEIGHT);

		// ImageView
		ImageView backgroundImage = new ImageView();
		backgroundImage.setFitWidth(Yard.WIDTH);
		backgroundImage.setFitHeight(Yard.HEIGHT);
		backgroundImage.setLayoutX(0.0);
		backgroundImage.setPickOnBounds(true);

		// Select a random image path
		String randomImagePath = getRandomLoadingScreen();
		backgroundImage.setImage(AssetLoader.loadImage(randomImagePath));

		// Add ImageView to child AnchorPane
		childPane.getChildren().add(backgroundImage);

		// Add child AnchorPane to root
		root.getChildren().add(childPane);

		// Set the scene to the stage
		Scene scene = new Scene(root, Yard.WIDTH, Yard.HEIGHT);
		Platform.runLater(() -> stage.setScene(scene));

		// Pause for LOADING_SCREEN_TIME seconds
		PauseTransition pause = new PauseTransition(Duration.seconds(LOADING_SCREEN_TIME));
		pause.setOnFinished(event -> {
			System.out.println("Exited Loading Screen");
			if (onFinished != null) {
				Platform.runLater(onFinished);
			}
		});

		pause.play();
	}

	public static void showStartScreen(AnchorPane root)
	{
		// Add loading screen
		Platform.runLater(() -> {
			ImageView loadingScreenImage = new ImageView(AssetLoader.loadImage("images/others/loadingScreen.png"));
			loadingScreenImage.setFitWidth(810);
			loadingScreenImage.setFitHeight(598);
			loadingScreenImage.setPreserveRatio(false);

			root.getChildren().add(loadingScreenImage);


			root.setOnMouseClicked(e->{
				root.getChildren().remove(loadingScreenImage);
			});

		});
	}
}
