// Datei: Balloon.java
package mm.core.physics.objects;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

/**
 * Spezialobjekt mit Auftrieb (Balloon).
 * Kreisförmig, Radius einstellbar, negative Gravitation (steigt auf).
 */
public class Balloon implements PhysicsObjects_Schnittstelle {
    private final float x, y;
    private final float radius;
    private final float density = 0.1f;
    private final float friction = 0.2f;
    private final float restitution = 0.3f;
    private final float gravityScale = -1.0f; // Auftrieb

    /**
     * @param x       Start-Position X in m
     * @param y       Start-Position Y in m
     * @param radius  Radius in m
     */
    public Balloon(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    public Body createBody(World world) {
        // Body-Definition mit negativem gravityScale
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(new Vec2(x, y));
        bd.gravityScale = gravityScale;
        Body body = world.createBody(bd);

        // Kreis-Form
        CircleShape shape = new CircleShape();
        shape.m_radius = radius;

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = density;
        fd.friction = friction;
        fd.restitution = restitution;

        body.createFixture(fd);
        return body;
    }
}