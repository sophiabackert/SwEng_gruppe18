package mm.utils;

import org.jbox2d.common.Vec2;

/**
 * Utility-Klasse für mathematische Operationen.
 */
public final class MathUtils {
    private static final float EPSILON = 1e-6f;
    
    private MathUtils() {
        // Verhindere Instanziierung
    }
    
    /**
     * Interpoliert linear zwischen zwei Werten.
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * clamp01(t);
    }
    
    /**
     * Interpoliert linear zwischen zwei Vektoren.
     */
    public static Vec2 lerpVec2(Vec2 a, Vec2 b, float t) {
        float x = lerp(a.x, b.x, t);
        float y = lerp(a.y, b.y, t);
        return new Vec2(x, y);
    }
    
    /**
     * Begrenzt einen Wert auf den Bereich [0, 1].
     */
    public static float clamp01(float value) {
        return clamp(value, 0f, 1f);
    }
    
    /**
     * Begrenzt einen Wert auf einen bestimmten Bereich.
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Berechnet den Winkel zwischen zwei Vektoren in Grad.
     */
    public static float angleBetween(Vec2 a, Vec2 b) {
        float dot = a.x * b.x + a.y * b.y;
        float det = a.x * b.y - a.y * b.x;
        float angle = (float) Math.toDegrees(Math.atan2(det, dot));
        return angle < 0 ? angle + 360 : angle;
    }
    
    /**
     * Rotiert einen Vektor um einen bestimmten Winkel in Grad.
     */
    public static Vec2 rotateVector(Vec2 v, float angleDegrees) {
        float rad = toRadians(angleDegrees);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        return new Vec2(
            v.x * cos - v.y * sin,
            v.x * sin + v.y * cos
        );
    }
    
    /**
     * Prüft, ob zwei Werte ungefähr gleich sind.
     */
    public static boolean approximately(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }
    
    /**
     * Prüft, ob zwei Vektoren ungefähr gleich sind.
     */
    public static boolean approximatelyVec2(Vec2 a, Vec2 b) {
        return approximately(a.x, b.x) && approximately(a.y, b.y);
    }
    
    /**
     * Berechnet die Distanz zwischen zwei Vektoren.
     */
    public static float distance(Vec2 a, Vec2 b) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Konvertiert Grad in Radianten.
     */
    public static float toRadians(float degrees) {
        return (float) Math.toRadians(degrees);
    }
    
    /**
     * Konvertiert Radianten in Grad.
     */
    public static float toDegrees(float radians) {
        return (float) Math.toDegrees(radians);
    }
} 