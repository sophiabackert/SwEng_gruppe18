package mm.domain.storage;

import mm.domain.config.ObjectConf;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Datenklasse, die alle Informationen eines Levels kapselt.
 * <p>
 * Enthält Name, Schwierigkeitsgrad, Zielbeschreibung, die Liste der platzierten Objekte
 * sowie die Limits für die Anzahl bestimmter Objekttypen. Wird für die Serialisierung,
 * Speicherung und den Austausch von Leveldaten verwendet.
 * </p>
 */
public class LevelData {
    /** Name des Levels */
    private final String name;
    /** Schwierigkeitsgrad des Levels */
    private final Difficulty difficulty;
    /** Zielbeschreibung für das Level */
    private final String objective;
    /** Liste aller platzierten Objekte (Konfigurationen) */
    private final List<ObjectConf> objects;
    /** Limits für die Anzahl bestimmter Objekttypen */
    private final Map<String, Integer> limits;

    /**
     * Vollparametrisierter Konstruktor für Leveldaten.
     * @param name Name des Levels
     * @param difficulty Schwierigkeitsgrad
     * @param objective Zielbeschreibung
     * @param objects Liste der platzierten Objekte
     * @param limits Objekt-Limits
     */
    public LevelData(String name, Difficulty difficulty, String objective, List<ObjectConf> objects, Map<String, Integer> limits) {
        this.name = name;
        this.difficulty = difficulty;
        this.objective = objective;
        this.objects = objects;
        this.limits = limits != null ? limits : new HashMap<>();
    }

    /**
     * Konstruktor ohne Limits (setzt leere Limits).
     * @param name Name des Levels
     * @param difficulty Schwierigkeitsgrad
     * @param objective Zielbeschreibung
     * @param objects Liste der platzierten Objekte
     */
    public LevelData(String name, Difficulty difficulty, String objective, List<ObjectConf> objects) {
        this(name, difficulty, objective, objects, new HashMap<>());
    }

    /**
     * Leerer Standardkonstruktor (setzt alles auf Standardwerte).
     */
    public LevelData() {
        this("", Difficulty.EASY, "", List.of(), new HashMap<>());
    }

    /**
     * @return Name des Levels
     */
    public String getName() {
        return name;
    }

    /**
     * @return Schwierigkeitsgrad
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * @return Zielbeschreibung
     */
    public String getObjective() {
        return objective;
    }

    /**
     * @return Liste der platzierten Objekte
     */
    public List<ObjectConf> getObjects() {
        return objects;
    }

    /**
     * @return Objekt-Limits
     */
    public Map<String, Integer> getLimits() {
        return limits;
    }
}