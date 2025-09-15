// Datei: BilliardBall.java
package mm.core.physics.objects;

/**
 * Billiard-Ball: Ø ≈57 mm, mittlere Dichte/Reibung, moderater Rückprall.
 */
public class BilliardBall extends Ball {

    public BilliardBall(float x, float y) {
        super(
                x, y,
                0.028575f,  // Radius 28,575 cm (Ø 57,15 cm)
                1.0f,       // mittlere Dichte
                0.2f,       // mittlere Reibung
                0.3f        // moderater Rückprall
        );
    }
}