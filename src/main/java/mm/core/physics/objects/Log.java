package mm.core.physics.objects;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

/**
 * Statischer Zylinder (Log).
 * Radius: 0.5 m, Reibung 0.6, kein Rückprall
 */
public class Log implements PhysicsObjects_Schnittstelle {
    private final float x, y;
    private final float radius;
    private final float friction = 0.6f;

    public Log(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    public Body createBody(World world) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.STATIC;
        bd.position.set(new Vec2(x, y));
        Body body = world.createBody(bd);

        CircleShape shape = new CircleShape();
        shape.m_radius = radius;

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.friction = friction;
        fd.restitution = 0.0f;

        body.createFixture(fd);
        return body;
    }
}