package mm.service.rendering;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import java.util.Map;

/**
 * Verantwortlich für das Zeichnen und Rendern der Spielobjekte auf dem Canvas.
 * <p>
 * Unterstützt verschiedene Objektarten (Kreis, Box, Bucket, Zonen), Skins, Schatten, Glanz und Farbverläufe.
 * </p>
 */
public class GameRenderer {
    
    private static final float SCALE = 100.0f;
    private final Canvas gameCanvas;
    
    /**
     * Erstellt einen GameRenderer für das angegebene Canvas.
     * @param gameCanvas Zeichenfläche
     */
    public GameRenderer(Canvas gameCanvas) {
        this.gameCanvas = gameCanvas;
    }
    
    /**
     * Rendert alle Objekte auf das Canvas.
     * @param bodies Map von Body zu RenderInfo
     */
    public void render(Map<Body, RenderInfo> bodies) {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        for (Map.Entry<Body, RenderInfo> entry : bodies.entrySet()) {
            Body body = entry.getKey();
            RenderInfo info = entry.getValue();
            
            Vec2 position = body.getPosition();
            float angle = body.getAngle();
            
            double x = position.x * SCALE;
            double y = position.y * SCALE;
            
            gc.save();
            gc.translate(x, y);
            gc.rotate(Math.toDegrees(angle));
            
            if (info.image == null) {
                gc.save();
                gc.translate(3, 3);
                gc.setGlobalAlpha(0.3);
                gc.setFill(Color.BLACK);
                renderObjectShape(gc, info, true);
                gc.restore();
            }
            
            renderObjectWithGradient(gc, info);
            
            if (info.image == null) {
                gc.save();
                gc.setGlobalAlpha(0.4);
                gc.setFill(Color.WHITE);
                renderGloss(gc, info);
                gc.restore();
            }
            
            gc.restore();
        }
    }
    
    /**
     * Rendert die Form eines Objekts (mit oder ohne Schatten).
     * @param gc GraphicsContext
     * @param info RenderInfo
     * @param isShadow true für Schatten
     */
    private void renderObjectShape(GraphicsContext gc, RenderInfo info, boolean isShadow) {
        if (info.image != null && !isShadow) {
            renderObjectWithImage(gc, info);
            return;
        }
        
        switch (info.type) {
            case CIRCLE:
                double radius = info.param1 * SCALE;
                
                // Spezielle Behandlung für Ballons (größerer Schatten für Schnur)
                if (isShadow && "balloon".equals(info.skinId)) {
                    double balloonSize = radius * 2.2; // Größer für Schnur
                    gc.fillOval(-balloonSize/2, -balloonSize/2, balloonSize, balloonSize);
                } else {
                    gc.fillOval(-radius, -radius, radius * 2, radius * 2);
                    if (!isShadow) {
                        gc.strokeOval(-radius, -radius, radius * 2, radius * 2);
                    }
                }
                break;
                
            case BOX:
                double width = info.param1 * SCALE;
                double height = info.param2 * SCALE;
                gc.fillRect(-width / 2, -height / 2, width, height);
                if (!isShadow) {
                    gc.strokeRect(-width / 2, -height / 2, width, height);
                }
                break;
                
            case BUCKET:
                if (!isShadow) {
                    renderBucket(gc, info);
                } else {
                    double w = info.param1 * SCALE;
                    double h = info.param2 * SCALE;
                    gc.fillRect(-w / 2, -h / 2, w, h / 4);
                }
                break;
            case GOALZONE:
                double goalWidth = info.param1 * SCALE;
                double goalHeight = info.param2 * SCALE;
                gc.fillRect(-goalWidth / 2, -goalHeight / 2, goalWidth, goalHeight);
                if (!isShadow) {
                    gc.strokeRect(-goalWidth / 2, -goalHeight / 2, goalWidth, goalHeight);
                }
                break;
            case RESTRICTIONZONE:
                double restrictionWidth = info.param1 * SCALE;
                double restrictionHeight = info.param2 * SCALE;
                gc.fillRect(-restrictionWidth / 2, -restrictionHeight / 2, restrictionWidth, restrictionHeight);
                if (!isShadow) {
                    gc.strokeRect(-restrictionWidth / 2, -restrictionHeight / 2, restrictionWidth, restrictionHeight);
                }
                break;
        }
    }
    
    /**
     * Rendert ein Objekt mit Bild (Skin), falls vorhanden.
     * @param gc GraphicsContext
     * @param info RenderInfo
     */
    private void renderObjectWithImage(GraphicsContext gc, RenderInfo info) {
        if (info.image == null) return;
        
        switch (info.type) {
            case CIRCLE:
                double radius = info.param1 * SCALE;
                
                // Spezielle Behandlung für Ballons (größere Darstellung für Schnur)
                if ("balloon".equals(info.skinId)) {
                    double balloonSize = radius * 2.2; // Größer für Schnur
                    gc.drawImage(info.image, -balloonSize/2, -balloonSize/2, balloonSize, balloonSize);
                } else if ("log".equals(info.skinId)) {
                    // Log als Kreis mit Bild
                    gc.drawImage(info.image, -radius, -radius, radius * 2, radius * 2);
                } else {
                    gc.drawImage(info.image, -radius, -radius, radius * 2, radius * 2);
                }
                break;
                
            case BOX:
                double width = info.param1 * SCALE;
                double height = info.param2 * SCALE;
                gc.drawImage(info.image, -width / 2, -height / 2, width, height);
                break;
                
            case GOALZONE:
                double goalWidth = info.param1 * SCALE;
                double goalHeight = info.param2 * SCALE;
                gc.setGlobalAlpha(0.7);
                gc.drawImage(info.image, -goalWidth / 2, -goalHeight / 2, goalWidth, goalHeight);
                gc.setGlobalAlpha(1.0);
                break;
            case RESTRICTIONZONE:
                double restrictionWidth = info.param1 * SCALE;
                double restrictionHeight = info.param2 * SCALE;
                gc.setGlobalAlpha(0.7);
                gc.drawImage(info.image, -restrictionWidth / 2, -restrictionHeight / 2, restrictionWidth, restrictionHeight);
                gc.setGlobalAlpha(1.0);
                break;
                
            case BUCKET:
                renderBucket(gc, info);
                break;
        }
    }
    
    /**
     * Rendert ein Objekt mit Farbverlauf.
     * @param gc GraphicsContext
     * @param info RenderInfo
     */
    private void renderObjectWithGradient(GraphicsContext gc, RenderInfo info) {
        if (info.image != null) {
            renderObjectWithImage(gc, info);
            return;
        }
        
        Color baseColor = info.color;
        Color lightColor = baseColor.brighter().brighter();
        Color darkColor = baseColor.darker();
        
        switch (info.type) {
            case CIRCLE:
                double radius = info.param1 * SCALE;
                
                var circleGradient = new javafx.scene.paint.RadialGradient(
                    0, 0, -radius * 0.3, -radius * 0.3, radius * 1.2, false,
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, lightColor),
                    new javafx.scene.paint.Stop(0.7, baseColor),
                    new javafx.scene.paint.Stop(1, darkColor)
                );
                gc.setFill(circleGradient);
                gc.fillOval(-radius, -radius, radius * 2, radius * 2);
                
                gc.setStroke(darkColor.darker());
                gc.setLineWidth(2);
                gc.strokeOval(-radius, -radius, radius * 2, radius * 2);
                break;
                
            case BOX:
                double width = info.param1 * SCALE;
                double height = info.param2 * SCALE;
                
                var boxGradient = new javafx.scene.paint.LinearGradient(
                    0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, lightColor),
                    new javafx.scene.paint.Stop(0.5, baseColor),
                    new javafx.scene.paint.Stop(1, darkColor)
                );
                gc.setFill(boxGradient);
                gc.fillRect(-width / 2, -height / 2, width, height);
                
                gc.setStroke(darkColor.darker());
                gc.setLineWidth(2);
                gc.strokeRect(-width / 2, -height / 2, width, height);
                break;
                
            case BUCKET:
                renderBucket(gc, info);
                break;
            case GOALZONE:
            case RESTRICTIONZONE:
                break;
        }
    }
    
    /**
     * Rendert Glanzeffekte auf Objekten.
     * @param gc GraphicsContext
     * @param info RenderInfo
     */
    private void renderGloss(GraphicsContext gc, RenderInfo info) {
        switch (info.type) {
            case CIRCLE:
                double radius = info.param1 * SCALE;
                double glossSize = radius * 0.4;
                gc.fillOval(-radius * 0.3, -radius * 0.3, glossSize, glossSize);
                break;
                
            case BOX:
                double width = info.param1 * SCALE;
                double height = info.param2 * SCALE;
                gc.fillRect(-width / 2, -height / 2, width, height * 0.2);
                break;
            case BUCKET:
            case GOALZONE:
            case RESTRICTIONZONE:
                break;
        }
    }
    
    /**
     * Rendert einen Bucket (Eimer) mit Linien.
     * @param gc GraphicsContext
     * @param info RenderInfo
     */
    private void renderBucket(GraphicsContext gc, RenderInfo info) {
        double width = info.param1 * SCALE;
        double height = info.param2 * SCALE;
        double thickness = info.param3 * SCALE;
        
        gc.setStroke(info.color);
        gc.setLineWidth(thickness);
        
        // Bodenlinie (horizontal, zentriert)
        gc.strokeLine(-width / 2, 0, width / 2, 0);
        
        // Schräge Seitenwände (85° Winkel)
        double wallAngle = Math.toRadians(85); // 85° in Radiant
        double sideOffsetX = height * Math.cos(wallAngle);
        double sideOffsetY = height * Math.sin(wallAngle);
        
        // Linke Seitenwand (schräg nach außen)
        gc.strokeLine(-width / 2, 0, -width / 2 - sideOffsetX, -sideOffsetY);
        
        // Rechte Seitenwand (schräg nach außen)
        gc.strokeLine(width / 2, 0, width / 2 + sideOffsetX, -sideOffsetY);
    }
    
    /**
     * Typen für darstellbare Objekte.
     */
    public enum RenderType {
        CIRCLE, BOX, BUCKET, GOALZONE, RESTRICTIONZONE
    }
    
    /**
     * Speichert Render-Informationen für ein Objekt.
     */
    public static class RenderInfo {
        final RenderType type;
        final double param1;
        final double param2;
        final double param3;
        final Color color;
        final Image image;
        final String skinId; // Hinzugefügt für die Spezialbehandlung

        /**
         * Erstellt eine RenderInfo für ein Objekt.
         * @param type RenderType
         * @param param1 Erster Parameter (z.B. Radius, Breite)
         * @param param2 Zweiter Parameter (z.B. Höhe)
         * @param param3 Dritter Parameter (z.B. Dicke)
         * @param color Grundfarbe
         * @param skinId Skin-ID für Bild
         */
        public RenderInfo(RenderType type, double param1, double param2, double param3, Color color, String skinId) {
            this.type = type;
            this.param1 = param1;
            this.param2 = param2;
            this.param3 = param3;
            this.color = color;
            this.image = loadImageForSkin(skinId);
            this.skinId = skinId; // Speichere die Skin-ID
        }

        /**
         * Lädt das Bild für einen Skin.
         * @param skinId Skin-ID
         * @return Image oder null
         */
        private Image loadImageForSkin(String skinId) {
            if (skinId == null || skinId.isEmpty()) return null;
            try {
                return new Image(GameRenderer.class.getResource("/assets/entities/" + skinId + ".png").toExternalForm());
            } catch (Exception e) {
                return null;
            }
        }
    }
} 