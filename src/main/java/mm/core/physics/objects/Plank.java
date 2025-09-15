package mm.core.physics.objects;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

/**
 * Statisches Brett (Plank).
 * Länge: 2.0 m, Breite: 0.2 m, Reibung 0.5, kein Rückprall
 */
public class Plank implements PhysicsObjects_Schnittstelle {
    private final float x, y;
    private final float length;
    private final float width;
    private final float angle;    // in Radiant
    private final float friction = 0.5f;

    /**
     * @param x       Zentrum X in m
     * @param y       Zentrum Y in m
     * @param length  Länge in m
     * @param width   Breite in m
     * @param angle   Rotation in Radiant
     */
    public Plank(float x, float y, float length, float width, float angle) {
        this.x = x;
        this.y = y;
        this.length = length;
        this.width = width;
        this.angle = angle;
    }

    @Override
    public Body createBody(World world) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.STATIC;
        bd.position.set(new Vec2(x, y));
        bd.angle = angle;
        Body body = world.createBody(bd);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(length / 2, width / 2);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.friction = friction;
        fd.restitution = 0.0f;

        body.createFixture(fd);
        return body;
    }
}