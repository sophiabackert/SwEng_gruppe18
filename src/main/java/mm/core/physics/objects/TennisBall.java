// Datei: TennisBall.java
package mm.core.physics.objects;

/**
 * Tennisball: Ø ≈67 mm, geringe Dichte, gute Elastizität.
 */
public class TennisBall extends Ball {

    public TennisBall(float x, float y) {
        super(
                x, y,
                0.0335f,    // Radius 33,5 cm (Ø 67 cm)
                0.6f,       // geringere Dichte
                0.4f,       // mittlere Reibung
                0.8f        // hoher Rückprall
        );
    }
}