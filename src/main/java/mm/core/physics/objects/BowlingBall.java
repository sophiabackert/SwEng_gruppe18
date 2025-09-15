// Datei: BowlingBall.java
package mm.core.physics.objects;

/**
 * Bowlingkugel: Ø ≈216 mm, hohe Dichte, geringer Rückprall.
 */
public class BowlingBall extends Ball {

    public BowlingBall(float x, float y) {
        super(
                x, y,
                0.108f,     // Radius 108 cm (Ø 216 cm)
                2.5f,       // hohe Dichte
                0.5f,       // stärkere Reibung
                0.1f        // sehr geringer Rückprall
        );
    }
}