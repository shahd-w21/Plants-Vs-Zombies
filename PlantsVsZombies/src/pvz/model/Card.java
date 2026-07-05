package pvz.model;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import pvz.util.AssetLoader;

public class Card
{
	private String cardImagePath;       // Path to the card image
	private String draggingImagePath;   // Path to the dragging PNG
	private String plantGifPath;        // Path to the plant GIF (optional)
	private Class<? extends Plant> plantType; // Class type of the Plant
	private int cost;

	private boolean onCooldown = false;
	private final int cooldownTime = 10 * 1000; // Cooldown duration in milliseconds
	private Rectangle cooldownOverlay; // Mask for the visual effect

	private ImageView cardImageView;    // Card ImageView for dragging
	private ImageView draggingImageView; // Temporary image for dragging
	private ImageView hoverImageView;

	// Constructor used for locked cards
	public Card(String cardImagePath)
	{
		this.cardImagePath = cardImagePath;
		this.cardImageView = new ImageView(AssetLoader.loadImage(cardImagePath));
	}

	// Constructor used for unlocked cards
	public Card(String cardImagePath, String draggingImagePath, Class<? extends Plant> plantType, int cost)
	{
		this.cardImagePath = cardImagePath;
		this.draggingImagePath = draggingImagePath;
		this.plantType = plantType;
		this.cost = cost;

		this.cardImageView = new ImageView(AssetLoader.loadImage(cardImagePath));
		this.draggingImageView = new ImageView(AssetLoader.loadImage(draggingImagePath));
		// Hovering image is the same as the dragging image.
		this.hoverImageView = new ImageView(AssetLoader.loadImage(draggingImagePath));
	}

	public void cardImageViewSetProperties(int layoutX, int layoutY, int fitWidth, int fitHeight, boolean preserveRatio, boolean setVisible)
	{
		// Configure card properties
		cardImageView.setLayoutX(layoutX);
		cardImageView.setLayoutY(layoutY);

		cardImageView.setFitWidth(fitWidth);
		cardImageView.setFitHeight(fitHeight);

		cardImageView.setPreserveRatio(preserveRatio);
		cardImageView.setVisible(setVisible);
	}

	public void draggingImageViewSetProperties(int fitWidth, int fitHeight, boolean preserveRatio, boolean setVisible)
	{
		draggingImageView.setFitWidth(fitWidth);
		draggingImageView.setFitHeight(fitHeight);

		draggingImageView.setPreserveRatio(preserveRatio);
		draggingImageView.setVisible(setVisible);
	}

	// Create a semi-transparent hover image
	public void hoverImageViewSetProperties(int fitWidth, int fitHeight, boolean preserveRatio, boolean setVisible)
	{
		hoverImageView.setFitWidth(fitWidth);
		hoverImageView.setFitHeight(fitHeight);

		hoverImageView.setPreserveRatio(preserveRatio);
		hoverImageView.setVisible(setVisible);
		hoverImageView.setOpacity(0.5); // Semi-transparent
	}

	public void addToYard(AnchorPane root, GridPane yardGrid, Yard yard)
	{
		// Add card to the root pane
		root.getChildren().add(cardImageView);

		cardImageView.setOnMousePressed(event ->
		{
			if (onCooldown) {
				return;
			}
			if (yard.sunCounter < cost)
			{
				cardUnavailableAudio();
				System.out.println("Not enough sun to select this card.");

				return;
			}

			// Placed the handling into a thread, since it updates the root pane continuously
			cardSelectedAudio();
			new Thread(() -> {
				try
				{
					Thread.sleep(20);
					Platform.runLater(() -> {
						// Activate dragging
						draggingImageView.setLayoutX(event.getSceneX() - 30);
						draggingImageView.setLayoutY(event.getSceneY() - 35);
						draggingImageView.setVisible(true);
						root.getChildren().add(draggingImageView);

						// Add hoverImageView to the root, but keep it initially hidden
						hoverImageView.setVisible(false);

						if (!root.getChildren().contains(hoverImageView))
							root.getChildren().add(hoverImageView);
					});
				} catch (InterruptedException e) {
					System.out.println("Exception: " + e);
				}
			}).start();

			event.consume(); // Consume the event to avoid propagation
		});

		// Update dragging and hover behavior
		cardImageView.setOnMouseDragged(event ->
		{
			if (yard.sunCounter < cost)
			{
				// If the sun counter is not sufficient, prevent dragging
				return;
			}
			if (onCooldown) {
				return;
			}

			new Thread(() ->
			{
				try
				{
					Thread.sleep(20);

					Platform.runLater(() -> {
						if (draggingImageView.isVisible())
						{
							// Update dragging image position
							draggingImageView.setLayoutX(event.getSceneX() - 30);
							draggingImageView.setLayoutY(event.getSceneY() - 35);

							// Track the closest grid cell and update hover image position
							double closestDistance = Double.MAX_VALUE;
							Button closestButton = null;

							for (Node node : yardGrid.getChildren()) {
								if (node instanceof Button button) {
									Bounds buttonBounds = button.localToScene(button.getBoundsInLocal());
									double centerX = buttonBounds.getMinX() + buttonBounds.getWidth() / 2;
									double centerY = buttonBounds.getMinY() + buttonBounds.getHeight() / 2;
									double distance = Math.hypot(centerX - event.getSceneX(), centerY - event.getSceneY());

									if (distance < closestDistance) {
										closestDistance = distance;
										closestButton = button;
									}
								}
							}

							if (closestButton != null) {
								int row = resolveRowIndex(closestButton);
								int col = resolveColumnIndex(closestButton);

								boolean canHighlight = (plantType == null)
										? yard.getPlantAt(row, col) != null
										: yard.isValidPosition(row, col);

								if (canHighlight) {
									Bounds buttonBounds = closestButton.localToScene(closestButton.getBoundsInLocal());
									hoverImageView.setLayoutX(buttonBounds.getMinX() + buttonBounds.getWidth() / 2 - hoverImageView.getFitWidth() / 2);
									hoverImageView.setLayoutY(buttonBounds.getMinY() + buttonBounds.getHeight() / 2 - hoverImageView.getFitHeight() / 2);

									hoverImageView.setOpacity(plantType == null ? 0.8 : 0.5);
									hoverImageView.setVisible(true);
								} else {
									hoverImageView.setVisible(false);
								}
							} else {
								hoverImageView.setVisible(false);
							}
						}

					});
				} catch (InterruptedException e) {
					System.out.println("Exception: " + e);
				}
			}).start();

			event.consume();
		});

		// Drop the plant when the mouse is released
		cardImageView.setOnMouseReleased(event ->
		{
			if (yard.sunCounter < cost)
			{
				return;
			}
			if (onCooldown) {
				return;
			}

			new Thread(() -> {
				try {
					Thread.sleep(20);

					Platform.runLater(() -> {
						for (Node node : yardGrid.getChildren()) {
							if (node instanceof Button) {
								Button button = (Button) node;
								Bounds buttonBounds = button.localToScene(button.getBoundsInLocal());

								if (buttonBounds.contains(event.getSceneX(), event.getSceneY())) {
									double centerX = buttonBounds.getMinX() + buttonBounds.getWidth() / 2;
									double centerY = buttonBounds.getMinY() + buttonBounds.getHeight() / 2;
									int row = resolveRowIndex(button);
									int col = resolveColumnIndex(button);

									draggingImageView.setVisible(false);
									root.getChildren().remove(draggingImageView);

									if (plantType == null) {
										if (yard.getPlantAt(row, col) != null) {
											System.out.println("Shovel used at (" + row + ", " + col + ")");
											yard.removePlant(root, row, col);
										} else {
											System.out.println("Shovel used on empty tile (" + row + ", " + col + ")");
										}
									} else {
										try {
											if (!yard.isValidPosition(row, col) || yard.grid[row][col] != null) {
												System.out.println("Tile occupied or invalid at (" + row + ", " + col + ")");
												break;
											}

											Plant plant = plantType.getDeclaredConstructor(int.class, int.class).newInstance((int) centerX, (int) centerY);
											yard.placePlant(plant, root, row, col);
											yard.sunCounter -= plant.getCost();
											yard.label.setText(String.valueOf(yard.sunCounter));
											startCooldown();
										} catch (Exception e) {
											System.out.println("An exception occurred: " + e);
										}
									}

									hoverImageView.setVisible(false);
									root.getChildren().remove(hoverImageView);

									return;
								}
							}
						}

						draggingImageView.setVisible(false);
						root.getChildren().remove(draggingImageView);
						hoverImageView.setVisible(false);
						root.getChildren().remove(hoverImageView);
					});
				} catch (InterruptedException e)
				{
					System.out.println("Exception: " + e);
				}
			}).start();

			event.consume();
		});
	}

	public ImageView getCardImageView()
	{
		return cardImageView;
	}

	public void setCardImageView(ImageView cardImageView)
	{
		this.cardImageView = cardImageView;
	}


	private void startCooldown()
	{
		onCooldown = true;

		cardImageView.setDisable(true);  // Disable the card to prevent interactions

		// Initialize the overlay if not already created
		if (cooldownOverlay == null)
		{
			cooldownOverlay = new Rectangle(cardImageView.getFitWidth(), cardImageView.getFitHeight());
			cooldownOverlay.setFill(Color.BLACK);
			cooldownOverlay.setOpacity(0.7); // Semi-transparent

			// Bind overlay position to the card's layout properties
			cooldownOverlay.setLayoutX(cardImageView.getLayoutX());
			cooldownOverlay.setLayoutY(cardImageView.getLayoutY());

			// Add the overlay to the parent if not already added
			if (!Yard.root.getChildren().contains(cooldownOverlay))
			{
				Yard.root.getChildren().add(cooldownOverlay);
			}
		}

		// Reset overlay height and make it visible
		cooldownOverlay.setHeight(cardImageView.getFitHeight());
		cooldownOverlay.setVisible(true);

		Timeline cooldownTimeline = new Timeline(
				new KeyFrame(Duration.ZERO, new KeyValue(cooldownOverlay.heightProperty(), cardImageView.getFitHeight())),
				new KeyFrame(Duration.millis(cooldownTime), new KeyValue(cooldownOverlay.heightProperty(), 0))
		);

		// When the animation finishes, reset the state
		cooldownTimeline.setOnFinished(event -> {
			onCooldown = false; // Cooldown is over
			cooldownOverlay.setVisible(false); // Hide the overlay

			cardImageView.setDisable(false); // Allow interactions again

		});

		// Start the animation
		cooldownTimeline.play();
	}


	public void cardSelectedAudio() {
		try {
			String path = getClass().getResource("/pvz/music/card selected.mp3").toExternalForm();
			System.out.println("Path: " + path);
			Media media = new Media(path);
			MediaPlayer mediaPlayer = new MediaPlayer(media);
			mediaPlayer.setVolume(0.02);

			mediaPlayer.play();
		} catch (Exception e) {
			System.out.println("Error playing card sound: " + e.getMessage());
		}
	}

	public void cardUnavailableAudio() {
		try {
			String path = getClass().getResource("/pvz/music/card unavailable.mp3").toExternalForm();
			Media media = new Media(path);
			MediaPlayer mediaPlayer = new MediaPlayer(media);
			mediaPlayer.setVolume(0.3);

			mediaPlayer.play();
		} catch (Exception e) {
			System.out.println("Error playing unavailable card sound: " + e.getMessage());
		}
	}

	private int resolveRowIndex(Node node) {
		Integer value = GridPane.getRowIndex(node);
		return value != null ? value : 0;
	}

	private int resolveColumnIndex(Node node) {
		Integer value = GridPane.getColumnIndex(node);
		return value != null ? value : 0;
	}
}

