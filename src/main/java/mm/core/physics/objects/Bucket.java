// Datei: Bucket.java
package mm.core.physics.objects;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

/**
 * Spezialobjekt Eimer (Bucket).
 * Rechteckiger statischer Bereich, der als Sensor Bälle auffängt.
 */
public class Bucket implements PhysicsObjects_Schnittstelle {
    private final float x, y;
    private final float halfWidth;
    private final float halfHeight;

    /**
     * @param x          Zentrum X in m
     * @param y          Zentrum Y in m
     * @param halfWidth  Halbe Breite in m
     * @param halfHeight Halbe Höhe in m
     */
    public Bucket(float x, float y, float halfWidth, float halfHeight) {
        this.x = x;
        this.y = y;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
    }

    @Override
    public Body createBody(World world) {
        // Statischer Body
        BodyDef bd = new BodyDef();
        bd.type = BodyType.STATIC;
        bd.position.set(new Vec2(x, y));
        Body body = world.createBody(bd);

        // Rechteck-Form
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfWidth, halfHeight);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.isSensor = true;      // Nur Sensor, kein physikalischer Widerstand
        fd.density = 0.0f;
        fd.friction = 0.0f;
        fd.restitution = 0.0f;

        body.createFixture(fd);
        return body;
    }
}