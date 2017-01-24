package game;

import engine.Physics;
import org.joml.Vector3f;

/**
 * Created by Pete on 23/01/2017.
 */
public class Rigidbody {

    private Vector3f position;
    private Vector3f direction = new Vector3f(0, -1, 0);
    private float speed = 0;
    private float mass = 10;


    public Rigidbody(Vector3f position, float mass)
    {
        this.position = position;
        this.mass = mass;
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public void update(float interval)
    {
        float force = Physics.calcForce(mass, Physics.G);
        float acceleration = Physics.calcAcceleration(mass, force);
        speed += acceleration * interval;

        Vector3f velocity = new Vector3f(direction).mul(speed);
        position.add(velocity.mul(interval));


        if(position.y < 0.5f)
        {
            position.y = 0.5f;
        }
    }


}
