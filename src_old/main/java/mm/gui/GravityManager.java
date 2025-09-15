package mm.gui;

import org.jbox2d.common.Vec2;
import java.util.ArrayList;
import java.util.List;

public class GravityManager {
    private static GravityManager instance;
    
    // Standardwerte für die Gravitation
    private static final double DEFAULT_GRAVITY_STRENGTH = 9.81;
    private static final double DEFAULT_GRAVITY_ANGLE = 270.0; // Nach unten (in Grad)
    
    private double gravityStrength;
    private double gravityAngle;
    private final List<SettingsObserver> observers;

    private GravityManager() {
        // Initialisiere mit Standardwerten
        gravityStrength = DEFAULT_GRAVITY_STRENGTH;
        gravityAngle = DEFAULT_GRAVITY_ANGLE;
        observers = new ArrayList<>();
    }

    public static GravityManager getInstance() {
        if (instance == null) {
            instance = new GravityManager();
        }
        return instance;
    }

    public void addObserver(SettingsObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(SettingsObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (SettingsObserver observer : observers) {
            observer.onGravityChanged(gravityStrength, gravityAngle);
        }
    }

    public double getGravityStrength() {
        return gravityStrength;
    }

    public void setGravityStrength(double strength) {
        if (strength < 0.0) {
            throw new IllegalArgumentException("Gravity strength cannot be negative");
        }
        if (this.gravityStrength != strength) {
            this.gravityStrength = strength;
            notifyObservers();
        }
    }

    public double getGravityAngle() {
        return gravityAngle;
    }

    public void setGravityAngle(double angle) {
        // Normalisiere den Winkel auf 0-360 Grad
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }
        if (this.gravityAngle != angle) {
            this.gravityAngle = angle;
            notifyObservers();
        }
    }

    public Vec2 getGravityVector() {
        // Konvertiere Winkel von Grad zu Radiant
        double angleRad = Math.toRadians(gravityAngle);
        
        // Berechne die x- und y-Komponenten der Gravitation
        double x = gravityStrength * Math.cos(angleRad);
        double y = gravityStrength * Math.sin(angleRad);
        
        return new Vec2((float)x, (float)y);
    }
} 