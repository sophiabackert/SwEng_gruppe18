package mm.core.physics.objects;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

/**
 * Dynamisches Domino-Objekt.
 * Maße: 0.1 m × 0.8 m, Dichte 2 kg/m², Reibung 0.4, Rückprall 0.05
 */
public class Domino implements PhysicsObjects_Schnittstelle {
    private final float x, y;
    private final float halfWidth = 0.05f;
    private final float halfHeight = 0.4f;
    private final float density = 2.0f;
    private final float friction = 0.4f;
    private final float restitution = 0.05f;

    public Domino(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Body createBody(World world) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(new Vec2(x, y));
        Body body = world.createBody(bd);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfWidth, halfHeight);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = density;
        fd.friction = friction;
        fd.restitution = restitution;

        body.createFixture(fd);
        return body;
    }
}