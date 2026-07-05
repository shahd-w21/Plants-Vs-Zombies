package pvz.util;

import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;

public final class AssetLoader {
    private AssetLoader() {
    }

    public static Image loadImage(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("Image path must not be empty");
        }

        String normalizedPath = normalizePath(resourcePath);

        try (InputStream stream = AssetLoader.class.getResourceAsStream(normalizedPath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing image resource: " + normalizedPath);
            }
            return new Image(stream);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to load image resource: " + normalizedPath, ex);
        }
    }

    private static String normalizePath(String rawPath) {
        if (rawPath.startsWith("/")) {
            return rawPath;
        }
        if (rawPath.startsWith("pvz/")) {
            return "/" + rawPath;
        }
        return "/pvz/" + rawPath;
    }
}
