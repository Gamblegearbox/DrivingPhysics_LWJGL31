package game;

import engine.Physics;
import org.joml.Vector3f;
import org.lwjgl.system.CallbackI;
import sun.security.x509.CertificateVersion;

/**
 * Created by Pete on 23/01/2017.
 */
public class Rigidbody {

    private Vector3f position;
    private Vector3f velocity = new Vector3f(0, 0, 0);
    private Vector3f gravity = new Vector3f(0, -Physics.G, 0);


    public Rigidbody(Vector3f position)
    {
        this.position = position;
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public void update(float interval)
    {
        position.add(new Vector3f(velocity).mul(interval));
        velocity.add(new Vector3f(gravity).mul(interval));



        if(position.y < 0.5f)
        {
            position.y = 0.5f;
        }
    }


}
