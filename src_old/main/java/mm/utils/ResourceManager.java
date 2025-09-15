package mm.utils;

import javafx.scene.image.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton-Klasse zum Verwalten und Cachen von Ressourcen.
 */
public class ResourceManager {
    private static ResourceManager instance;
    private final Map<String, Image> imageCache;
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String APP_DIR = ".mm-gruppe-18";
    
    private ResourceManager() {
        imageCache = new HashMap<>();
        initializeAppDirectory();
    }
    
    /**
     * Gibt die einzige Instanz des ResourceManagers zurück.
     * @return Die ResourceManager-Instanz
     */
    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }
    
    /**
     * Initialisiert das Anwendungsverzeichnis.
     */
    private void initializeAppDirectory() {
        File appDir = new File(USER_HOME, APP_DIR);
        if (!appDir.exists() && !appDir.mkdirs()) {
            System.err.println("Konnte Anwendungsverzeichnis nicht erstellen: " + appDir.getAbsolutePath());
        }
    }
    
    /**
     * Gibt das Anwendungsverzeichnis zurück.
     * @return Das Anwendungsverzeichnis als File-Objekt
     */
    public static File getResourceDirectory() {
        return new File(USER_HOME, APP_DIR);
    }
    
    /**
     * Lädt ein Bild aus den Ressourcen. Bereits geladene Bilder werden aus dem Cache zurückgegeben.
     * @param path Der Pfad zum Bild relativ zum resources-Verzeichnis
     * @return Das geladene Image-Objekt oder null bei Fehler
     */
    public Image loadImage(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        
        try {
            String resourcePath = "/" + path;
            Image image = new Image(getClass().getResourceAsStream(resourcePath));
            imageCache.put(path, image);
            return image;
        } catch (Exception e) {
            System.err.println("Fehler beim Laden des Bildes " + path + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Leert den Ressourcen-Cache.
     */
    public void clearCache() {
        imageCache.clear();
    }
    
    /**
     * Entfernt ein Bild aus dem Cache.
     */
    public void unloadImage(String path) {
        imageCache.remove(path);
    }
    
    /**
     * Prüft, ob ein Bild im Cache ist.
     */
    public boolean isImageCached(String path) {
        return imageCache.containsKey(path);
    }
    
    /**
     * Gibt die Anzahl der gecachten Bilder zurück.
     */
    public int getCachedImagesCount() {
        return imageCache.size();
    }
} 