// Datei: PhysicsObjects_Schnittstelle.java

package mm.core.physics.objects;

import mm.core.physics.objects.Plank;
import mm.core.physics.objects.Log;
import mm.core.physics.objects.Ball;
import mm.core.physics.objects.Box;
import mm.core.physics.objects.Domino;
import mm.core.physics.objects.Balloon;
import mm.core.physics.objects.Bucket;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

/**
 * Gemeinsames Interface für alle Physik-Objekte.
 * Stellt sicher, dass jedes Objekt eine createBody-Methode
 * zum Erzeugen seines JBox2D-Körpers im angegebenen World-Kontext
 * implementiert.
 */
public interface PhysicsObjects_Schnittstelle {

    /**
     * Erzeugt und registriert den JBox2D-Body dieses Physikobjekts in der Welt.
     *
     * @param world die JBox2D-Welt, in der der Body angelegt werden soll
     * @return der erzeugte Body
     */
    Body createBody(World world);

    /**
     * Factory method to create physics objects by type name and parameters.
     *
     * @param type   the object type (e.g. "Plank", "Ball", "Box", "Domino", "Balloon", "Bucket")
     * @param params the parameters required by the specific object constructor
     * @return a new instance of the specified physics object
     */
    static PhysicsObjects_Schnittstelle create(String type, float... params) {
        switch (type) {
            case "Plank":
                return new Plank(params[0], params[1], params[2], params[3], params[4]);
            case "Log":
                return new Log(params[0], params[1], params[2]);
            case "Balls":
                return new Ball(params[0], params[1], params[2], params[3], params[4]);
            case "Box":
                return new Box(params[0], params[1]);
            case "Domino":
                return new Domino(params[0], params[1]);
            case "Balloon":
                return new Balloon(params[0], params[1], params[2]);
            case "Bucket":
                return new Bucket(params[0], params[1], params[2], params[3]);
            default:
                throw new IllegalArgumentException("Unknown physics object type: " + type);
        }
    }
}