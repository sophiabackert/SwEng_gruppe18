package mm.resources;

import java.io.File;

public class ResourceManager {
    /**
     * Gibt das Ressourcenverzeichnis der Anwendung zurück.
     * Erstellt es, falls es nicht existiert.
     */
    public static File getResourceDirectory() {
        File resourceDir = new File(System.getProperty("user.home"), ".mm-gruppe-18");
        if (!resourceDir.exists() && !resourceDir.mkdirs()) {
            throw new RuntimeException("Konnte Ressourcenverzeichnis nicht erstellen: " + resourceDir);
        }
        return resourceDir;
    }
} 