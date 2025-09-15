package mm.objects.balls;

import javafx.scene.paint.Color;
import mm.objects.GameObject;
import mm.objects.components.PhysicsComponent;
import mm.objects.components.RenderComponent;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

/**
 * Abstrakte Basisklasse für alle Balltypen im Spiel.
 * Stellt gemeinsame Ball-Eigenschaften und Verhalten bereit.
 */
public abstract class Ball extends GameObject {
    
    protected final float radius;
    protected final float density;
    protected final float friction;
    protected final float restitution;
    protected final Color color;
    
    /**
     * Erstellt einen neuen Ball mit den angegebenen Eigenschaften.
     * @param position Die Startposition des Balls
     * @param radius Der Radius des Balls in Metern
     * @param density Die Dichte des Balls (kg/m²)
     * @param friction Der Reibungskoeffizient (0-1)
     * @param restitution Der Restitutionskoeffizient (Elastizität, 0-1)
     * @param color Die Farbe des Balls
     */
    protected Ball(Vec2 position, float radius, float density, float friction, float restitution, Color color) {
        super(position);
        this.radius = radius;
        this.density = density;
        this.friction = friction;
        this.restitution = restitution;
        this.color = color;
        
        initComponents();
    }
    
    /**
     * Erstellt einen neuen Ball mit den Standardeigenschaften des jeweiligen Balltyps.
     * @param position Die Startposition des Balls
     */
    protected Ball(Vec2 position) {
        super(position);
        this.radius = getDefaultRadius();
        this.density = getDefaultDensity();
        this.friction = getDefaultFriction();
        this.restitution = getDefaultRestitution();
        this.color = getDefaultColor();
        
        initComponents();
    }
    
    /**
     * Gibt den Standard-Radius für diesen Balltyp zurück.
     */
    protected abstract float getDefaultRadius();
    
    /**
     * Gibt die Standard-Dichte für diesen Balltyp zurück.
     */
    protected abstract float getDefaultDensity();
    
    /**
     * Gibt den Standard-Reibungskoeffizienten für diesen Balltyp zurück.
     */
    protected abstract float getDefaultFriction();
    
    /**
     * Gibt den Standard-Restitutionskoeffizienten für diesen Balltyp zurück.
     */
    protected abstract float getDefaultRestitution();
    
    /**
     * Gibt die Standardfarbe für diesen Balltyp zurück.
     */
    protected abstract Color getDefaultColor();
    
    /**
     * Gibt das Präfix für die Soundeffekte dieses Balltyps zurück.
     */
    protected abstract String getSoundPrefix();
    
    private void initComponents() {
        // Physik-Komponente
        PhysicsComponent physics = new PhysicsComponent(this);
        physics.createCircleBody(getPosition(), radius, BodyType.DYNAMIC);
        physics.getFixtureDef().density = density;
        physics.getFixtureDef().friction = friction;
        physics.getFixtureDef().restitution = restitution;
        addComponent(physics);

        // Render-Komponente
        RenderComponent render = new RenderComponent(this);
        render.setFillColor(color);
        addComponent(render);
    }
    
    /**
     * Gibt die aktuelle Position des Balls zurück.
     * @return Die Position als Vec2
     */
    public Vec2 getPosition() {
        PhysicsComponent physics = getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            return physics.getBody().getPosition();
        }
        return new Vec2(0, 0);
    }
    
    /**
     * Setzt die Position des Balls
     * @param position Die neue Position
     */
    public void setPosition(Vec2 position) {
        super.setPosition(position.x, position.y);
        
        // Physik-Körper aktualisieren, falls vorhanden
        PhysicsComponent physics = getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            physics.getBody().setTransform(position, physics.getBody().getAngle());
        }
    }
    
    // Getters for ball properties
    public float getRadius() { return radius; }
    public float getDensity() { return density; }
    public float getRestitution() { return restitution; }
    public float getFriction() { return friction; }
}
