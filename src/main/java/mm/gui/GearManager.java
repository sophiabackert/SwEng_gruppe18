package mm.gui;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.contacts.Contact;
import mm.core.config.GearConf;
import mm.core.config.DriveChainConf;

import java.util.*;

/**
 * Erweiterter Zahnrad-Manager für Rotationsübertragung und Antriebsstränge.
 */
public class GearManager {
    
    private final Map<Body, GearConf> gears = new HashMap<>();
    private final Map<Body, Set<Body>> connections = new HashMap<>();
    private final Map<String, DriveChainInfo> driveChains = new HashMap<>();
    private final Map<Body, String> gearIds = new HashMap<>();  // Mapping von Body zu eindeutiger ID
    private int nextGearId = 1;  // Zähler für eindeutige Zahnrad-IDs
    
    /**
     * Informationen über einen Antriebsstrang
     */
    public static class DriveChainInfo {
        public final Body gearA;
        public final Body gearB;
        public final DriveChainConf config;
        
        public DriveChainInfo(Body gearA, Body gearB, DriveChainConf config) {
            this.gearA = gearA;
            this.gearB = gearB;
            this.config = config;
        }
    }
    
    /**
     * Registriert ein Zahnrad
     */
    public void addGear(Body body, GearConf config) {
        gears.put(body, config);
        connections.put(body, new HashSet<>());
        
        // Generiere eindeutige ID für das Zahnrad
        String gearId = "gear" + nextGearId++;
        gearIds.put(body, gearId);
        
        // Stelle Verbindungen aus der Konfiguration wieder her
        for (String connectedId : config.getConnectedGearIds()) {
            for (Map.Entry<Body, String> entry : gearIds.entrySet()) {
                if (entry.getValue().equals(connectedId)) {
                    connect(body, entry.getKey());
                    break;
                }
            }
        }
    }
    
    /**
     * Entfernt ein Zahnrad
     */
    public void removeGear(Body body) {
        // Verbindungen entfernen
        Set<Body> connected = connections.get(body);
        if (connected != null) {
            for (Body other : connected) {
                connections.get(other).remove(body);
                // Entferne auch die ID-Verbindung aus der Konfiguration
                GearConf otherConfig = gears.get(other);
                otherConfig.removeConnection(gearIds.get(body));
            }
        }
        
        // Antriebsstränge entfernen
        driveChains.entrySet().removeIf(entry -> 
            entry.getValue().gearA == body || entry.getValue().gearB == body);
        
        gears.remove(body);
        connections.remove(body);
        gearIds.remove(body);
    }
    
    /**
     * Fügt einen Antriebsstrang zwischen zwei Zahnrädern hinzu
     */
    public void addDriveChain(Body gearA, Body gearB, DriveChainConf config) {
        if (!gears.containsKey(gearA) || !gears.containsKey(gearB)) {
            throw new IllegalArgumentException("Beide Bodies müssen registrierte Zahnräder sein");
        }
        
        String chainId = config.getGearAId() + "-" + config.getGearBId();
        driveChains.put(chainId, new DriveChainInfo(gearA, gearB, config));
    }
    
    /**
     * Entfernt einen Antriebsstrang
     */
    public void removeDriveChain(String chainId) {
        driveChains.remove(chainId);
    }
    
    /**
     * Behandelt Zahnrad-Kontakte
     */
    public void handleGearContact(Contact contact, boolean isBeginContact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();
        
        // Beide müssen Zahnräder sein
        if (!gears.containsKey(bodyA) || !gears.containsKey(bodyB)) {
            return;
        }
        
        if (isBeginContact) {
            if (areGearsOverlapping(bodyA, bodyB)) {
                connect(bodyA, bodyB);
            }
        } else {
            disconnect(bodyA, bodyB);
        }
    }
    
    /**
     * Prüft ob zwei Zahnräder überlappen
     */
    private boolean areGearsOverlapping(Body gearA, Body gearB) {
        Vec2 posA = gearA.getPosition();
        Vec2 posB = gearB.getPosition();
        float distance = posA.sub(posB).length();
        
        GearConf configA = gears.get(gearA);
        GearConf configB = gears.get(gearB);
        
        // Überlappung wenn Abstand kleiner als Summe der inneren Radien
        return distance < (configA.getInnerRadius() + configB.getInnerRadius());
    }
    
    /**
     * Verbindet zwei Zahnräder
     */
    private void connect(Body gearA, Body gearB) {
        connections.get(gearA).add(gearB);
        connections.get(gearB).add(gearA);
        
        // Speichere die Verbindung auch in den Konfigurationen
        GearConf configA = gears.get(gearA);
        GearConf configB = gears.get(gearB);
        configA.addConnection(gearIds.get(gearB));
        configB.addConnection(gearIds.get(gearA));
    }
    
    /**
     * Trennt zwei Zahnräder
     */
    private void disconnect(Body gearA, Body gearB) {
        connections.get(gearA).remove(gearB);
        connections.get(gearB).remove(gearA);
        
        // Entferne die Verbindung auch aus den Konfigurationen
        GearConf configA = gears.get(gearA);
        GearConf configB = gears.get(gearB);
        configA.removeConnection(gearIds.get(gearB));
        configB.removeConnection(gearIds.get(gearA));
    }
    
    /**
     * Aktualisiert Rotationen
     */
    public void updateGearRotations() {
        Set<Body> processed = new HashSet<>();
        
        // Normale Zahnrad-zu-Zahnrad Verbindungen
        for (Body gear : gears.keySet()) {
            if (!processed.contains(gear)) {
                float angularVelocity = gear.getAngularVelocity();
                if (Math.abs(angularVelocity) > 0.1f) {
                    propagateRotation(gear, angularVelocity, processed, true);
                }
            }
        }
        
        // Antriebsstrang-Verbindungen
        processed.clear();
        for (DriveChainInfo chain : driveChains.values()) {
            if (!processed.contains(chain.gearA)) {
                float velocityA = chain.gearA.getAngularVelocity();
                if (Math.abs(velocityA) > 0.1f) {
                    // Gleichgerichtete Rotation für Antriebsstränge
                    chain.gearB.setAngularVelocity(velocityA);
                    processed.add(chain.gearA);
                    processed.add(chain.gearB);
                    
                    // Weitere Verbindungen von gearB propagieren
                    propagateRotation(chain.gearB, velocityA, processed, true);
                }
            }
        }
    }
    
    /**
     * Propagiert Rotation durch verbundene Zahnräder
     */
    private void propagateRotation(Body source, float velocity, Set<Body> processed, boolean inverse) {
        processed.add(source);
        
        // Normale Zahnrad-Verbindungen (gegenläufig)
        Set<Body> connected = connections.get(source);
        if (connected != null) {
            for (Body target : connected) {
                if (!processed.contains(target)) {
                    float targetVelocity = inverse ? -velocity : velocity;
                    target.setAngularVelocity(targetVelocity);
                    propagateRotation(target, targetVelocity, processed, !inverse);
                }
            }
        }
        
        // Antriebsstrang-Verbindungen (gleichgerichtet)
        for (DriveChainInfo chain : driveChains.values()) {
            Body target = null;
            if (chain.gearA == source) {
                target = chain.gearB;
            } else if (chain.gearB == source) {
                target = chain.gearA;
            }
            
            if (target != null && !processed.contains(target)) {
                target.setAngularVelocity(velocity); // Gleiche Richtung
                propagateRotation(target, velocity, processed, inverse);
            }
        }
    }
    
    /**
     * Prüft ob Body ein Zahnrad ist
     */
    public boolean isGear(Body body) {
        return gears.containsKey(body);
    }
    
    /**
     * Gibt alle aktiven Antriebsstränge zurück
     */
    public Collection<DriveChainInfo> getDriveChains() {
        return driveChains.values();
    }
    
    /**
     * Gibt die ID eines Zahnrads zurück
     */
    public String getGearId(Body body) {
        return gearIds.get(body);
    }
} 