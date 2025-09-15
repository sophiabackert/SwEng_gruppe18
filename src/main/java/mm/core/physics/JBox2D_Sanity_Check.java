package mm.core.physics;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

public class JBox2D_Sanity_Check {
    public static void main(String[] args) {
        // 1. Welt erstellen mit Schwerkraft
        Vec2 gravity = new Vec2(0.0f, -10.0f);
        World world = new World(gravity);

        // 2. Boden erstellen (statisch)
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(0.0f, -10.0f);
        Body groundBody = world.createBody(groundBodyDef);
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(50.0f, 10.0f);
        groundBody.createFixture(groundBox, 0.0f);

        // 3. Fallender Körper (dynamisch)
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.set(0.0f, 4.0f);
        Body body = world.createBody(bodyDef);
        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(1.0f, 1.0f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicBox;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        body.createFixture(fixtureDef);

        // 4. Simulation starten
        float timeStep = 1.0f / 60.0f;
        int velocityIterations = 6;
        int positionIterations = 2;

        System.out.println("Position vor Simulation: " + body.getPosition());

        // 60 Schritte simulieren (~1 Sekunde)
        for (int i = 0; i < 60; ++i) {
            world.step(timeStep, velocityIterations, positionIterations);
        }

        System.out.println("Position nach Simulation: " + body.getPosition());
    }
}