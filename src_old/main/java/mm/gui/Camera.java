package mm.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.jbox2d.common.Vec2;

/**
 * Verwaltet die Kamera-Transformation für die Spielansicht.
 */
public class Camera {
    private Vec2 position;
    private float zoom;
    private final Canvas canvas;
    private static final float M_TO_PX = 80.0f; // 1 Meter = 80 Pixel
    
    /**
     * Erstellt eine neue Kamera.
     * @param canvas Das Canvas, auf dem gerendert wird
     */
    public Camera(Canvas canvas) {
        this.canvas = canvas;
        this.position = new Vec2(0, 0);
        this.zoom = 1.0f;
    }
    
    /**
     * Wendet die Kamera-Transformation auf den GraphicsContext an.
     * @param gc Der GraphicsContext
     */
    public void apply(GraphicsContext gc) {
        gc.save();
        
        // Zentriere die Kamera auf dem Canvas
        gc.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
        
        // Wende Zoom an
        gc.scale(zoom, zoom);
        
        // Wende Kamera-Position an (invertiert, da wir die Welt bewegen)
        gc.translate(-position.x * M_TO_PX, position.y * M_TO_PX);
    }
    
    /**
     * Stellt die ursprüngliche Transformation wieder her.
     * @param gc Der GraphicsContext
     */
    public void restore(GraphicsContext gc) {
        gc.restore();
    }
    
    /**
     * Konvertiert Weltkoordinaten in Bildschirmkoordinaten.
     * @param worldPos Die Position in der Spielwelt
     * @return Die Position auf dem Bildschirm
     */
    public Vec2 worldToScreen(Vec2 worldPos) {
        float screenX = (worldPos.x - position.x) * M_TO_PX * zoom + (float)(canvas.getWidth() / 2);
        float screenY = (-worldPos.y + position.y) * M_TO_PX * zoom + (float)(canvas.getHeight() / 2);
        return new Vec2(screenX, screenY);
    }
    
    /**
     * Konvertiert Bildschirmkoordinaten in Weltkoordinaten.
     * @param screenPos Die Position auf dem Bildschirm
     * @return Die Position in der Spielwelt
     */
    public Vec2 screenToWorld(Vec2 screenPos) {
        float worldX = (screenPos.x - (float)(canvas.getWidth() / 2)) / (M_TO_PX * zoom) + position.x;
        float worldY = -(screenPos.y - (float)(canvas.getHeight() / 2)) / (M_TO_PX * zoom) - position.y;
        return new Vec2(worldX, worldY);
    }
    
    /**
     * Bewegt die Kamera.
     * @param dx Die Änderung in X-Richtung (in Metern)
     * @param dy Die Änderung in Y-Richtung (in Metern)
     */
    public void move(float dx, float dy) {
        position.x += dx;
        position.y += dy;
    }
    
    /**
     * Setzt die Kamera-Position.
     * @param x Die X-Position in Metern
     * @param y Die Y-Position in Metern
     */
    public void setPosition(float x, float y) {
        position.set(x, y);
    }
    
    /**
     * Setzt den Zoom-Faktor.
     * @param zoom Der neue Zoom-Faktor
     */
    public void setZoom(float zoom) {
        if (zoom > 0) {
            this.zoom = zoom;
        }
    }
    
    /**
     * Gibt die aktuelle Kamera-Position zurück.
     * @return Die Kamera-Position
     */
    public Vec2 getPosition() {
        return position;
    }
    
    /**
     * Gibt den aktuellen Zoom-Faktor zurück.
     * @return Der Zoom-Faktor
     */
    public float getZoom() {
        return zoom;
    }
} 