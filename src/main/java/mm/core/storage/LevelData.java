package mm.core.storage;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import mm.core.config.ObjectConf;

public class LevelData {
    private final String name;
    private final Difficulty difficulty;
    private final String objective;
    private final List<ObjectConf> objects;
    private final Map<String, Integer> limits;

    public LevelData(String name, Difficulty difficulty, String objective, List<ObjectConf> objects, Map<String, Integer> limits) {
        this.name = name;
        this.difficulty = difficulty;
        this.objective = objective;
        this.objects = objects;
        this.limits = limits != null ? limits : new HashMap<>();
    }

    // Constructor for compatibility (without limits)
    public LevelData(String name, Difficulty difficulty, String objective, List<ObjectConf> objects) {
        this(name, difficulty, objective, objects, new HashMap<>());
    }

    // Default constructor for Jackson
    public LevelData() {
        this("", Difficulty.EASY, "", List.of(), new HashMap<>());
    }

    public String getName() {
        return name;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getObjective() {
        return objective;
    }

    public List<ObjectConf> getObjects() {
        return objects;
    }

    public Map<String, Integer> getLimits() {
        return limits;
    }
}