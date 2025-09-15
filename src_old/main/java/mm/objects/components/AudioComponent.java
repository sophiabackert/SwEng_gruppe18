package mm.objects.components;

import javafx.scene.media.AudioClip;
import mm.objects.Component;
import mm.objects.GameObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Komponente für die Audiowiedergabe eines GameObjects.
 */
public class AudioComponent extends Component {
    private final Map<String, AudioClip> sounds;
    private float volume = 1.0f;
    private boolean muted = false;
    
    public AudioComponent(GameObject gameObject) {
        super(gameObject);
        this.sounds = new HashMap<>();
    }
    
    /**
     * Lädt einen Sound und speichert ihn unter dem angegebenen Namen.
     * @param name Der Name des Sounds
     * @param path Der Pfad zur Sounddatei
     */
    public void loadSound(String name, String path) {
        try {
            AudioClip clip = new AudioClip(getClass().getResource(path).toExternalForm());
            sounds.put(name, clip);
        } catch (Exception e) {
            System.err.println("Fehler beim Laden des Sounds '" + name + "': " + e.getMessage());
        }
    }
    
    /**
     * Spielt einen Sound ab.
     * @param name Der Name des Sounds
     */
    public void playSound(String name) {
        if (!muted && sounds.containsKey(name)) {
            AudioClip clip = sounds.get(name);
            clip.setVolume(volume);
            clip.play();
        }
    }
    
    /**
     * Setzt die Lautstärke für alle Sounds.
     * @param volume Die Lautstärke (0.0 - 1.0)
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    /**
     * Schaltet den Ton ein oder aus.
     * @param muted true für stumm, false für normal
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
    }
    
    @Override
    public void update(float deltaTime) {
        // Keine Update-Logik erforderlich
    }
    
    @Override
    public void reset() {
        // Alle Sounds stoppen
        for (AudioClip clip : sounds.values()) {
            clip.stop();
        }
    }
    
    @Override
    public void onAttach() {
        // Keine spezielle Initialisierung erforderlich
    }
    
    @Override
    public void onDetach() {
        // Alle Sounds stoppen und freigeben
        for (AudioClip clip : sounds.values()) {
            clip.stop();
        }
        sounds.clear();
    }
} 