package mm.game.level;

import java.util.List;

public class LevelData {
    public String name;
    public List<GameObject> objects;

    public static class GameObject {
        public String type;
        public double x;
        public double y;
    }
}
