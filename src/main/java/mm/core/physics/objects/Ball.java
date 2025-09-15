// Datei: Ball.java (statt Balls.java)
package mm.core.physics.objects;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

/**
 * Standard-Ball: konfigurierbar durch Radius, Dichte, Reibung, Rückprall.
 */
public class Ball implements PhysicsObjects_Schnittstelle {
    private final float x, y, radius, density, friction, restitution;

    public Ball(float x, float y, float radius, float density, float friction) {
        this(x, y, radius, density, friction, 0.3f);
    }

    public Ball(float x, float y, float radius, float density, float friction, float restitution) {
        this.x = x; this.y = y;
        this.radius = radius;
        this.density = density;
        this.friction = friction;
        this.restitution = restitution;
    }

    @Override
    public Body createBody(World world) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(new Vec2(x, y));
        Body body = world.createBody(bd);

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