package mm.objects.containers;

import mm.objects.GameObject;
import mm.objects.components.PhysicsComponent;
import mm.objects.components.RenderComponent;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

/**
 * Abstract base class for all container-type objects in the game.
 * Provides common container properties and behaviors.
 */
public abstract class Container extends GameObject {
    
    protected float width;
    protected float height;
    protected float density;
    protected float restitution;
    protected float friction;
    
    /**
     * Creates a new container with specified properties.
     *
     * @param position Initial position of the container
     * @param width Width of the container in meters
     * @param height Height of the container in meters
     * @param density Mass density of the container
     * @param restitution Bounciness of the container (0-1)
     * @param friction Friction coefficient of the container
     */
    protected Container(Vec2 position, float width, float height, float density, float restitution, float friction) {
        super();
        this.width = width;
        this.height = height;
        this.density = density;
        this.restitution = restitution;
        this.friction = friction;
        
        // Add physics component with box shape
        PhysicsComponent physics = new PhysicsComponent(this);
        physics.createRectangleBody(position, width, height, BodyType.DYNAMIC);
        physics.getBody().getFixtureList().setDensity(density);
        physics.getBody().getFixtureList().setRestitution(restitution);
        physics.getBody().getFixtureList().setFriction(friction);
        physics.getBody().resetMassData();
        addComponent(physics);
        
        // Add render component
        RenderComponent render = new RenderComponent(this);
        addComponent(render);
    }
    
    // Getters for container properties
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getDensity() { return density; }
    public float getRestitution() { return restitution; }
    public float getFriction() { return friction; }
    
    // Setters for container properties
    public void setWidth(float width) {
        this.width = width;
        updatePhysicsBody();
    }
    
    public void setHeight(float height) {
        this.height = height;
        updatePhysicsBody();
    }
    
    public void setDensity(float density) {
        this.density = density;
        PhysicsComponent physics = getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            physics.getBody().getFixtureList().setDensity(density);
            physics.getBody().resetMassData();
        }
    }
    
    public void setRestitution(float restitution) {
        this.restitution = restitution;
        PhysicsComponent physics = getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            physics.getBody().getFixtureList().setRestitution(restitution);
        }
    }
    
    public void setFriction(float friction) {
        this.friction = friction;
        PhysicsComponent physics = getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            physics.getBody().getFixtureList().setFriction(friction);
        }
    }
    
    /**
     * Updates the physics body when width or height changes.
     */
    private void updatePhysicsBody() {
        PhysicsComponent physics = getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            Vec2 position = physics.getBody().getPosition();
            float angle = physics.getBody().getAngle();
            
            // Recreate the body with new dimensions
            physics.destroyBody();
            physics.createRectangleBody(position, width, height, BodyType.DYNAMIC);
            physics.getBody().setTransform(position, angle);
            
            // Restore properties
            physics.getBody().getFixtureList().setDensity(density);
            physics.getBody().getFixtureList().setRestitution(restitution);
            physics.getBody().getFixtureList().setFriction(friction);
            physics.getBody().resetMassData();
        }
    }
}
