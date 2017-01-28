package game.car;

import engine.Physics;
import game.Game;
import game.environment.GroundType;
import game.environment.GroundTypes;
import org.joml.Math;
import org.joml.Vector3f;

import java.lang.invoke.SwitchPoint;

public class Car {

    private final float maxSteeringAngle = 35f;
    private final float wheelBase = 3.15f;
    private final float halfWheelBase = wheelBase / 2f;
    private final float trackWidth = 0.8f;
    private final float mass;
    private final float maxEngineForce;
    private final float maxBrakeForce;

    private float steeringAngle;
    private float wheelRadius;
    private float suspensionHeight;
    private float carAngle;

    private Vector3f position;
    private Vector3f forward;
    private Vector3f steeringWheelForward;
    private Vector3f carUp;
    private Vector3f left;
    private Vector3f[] wheelPositions;

    private GroundType currentGround;

    public float acceleration;
    public float speed;
    public float currentForce;


    public Car()
    {
        wheelRadius = 0.43f;
        suspensionHeight = 0.1f;
        mass = 2000f;
        maxEngineForce = 6000f;
        maxBrakeForce = 12000f;
        steeringAngle = 0;
        carAngle = 0;

        position = new Vector3f();
        forward = new Vector3f();
        steeringWheelForward = new Vector3f();
        carUp = new Vector3f(0,1,0);
        left = new Vector3f();
        wheelPositions = new Vector3f[4];
        currentGround = getGroundType();
    }

    public void update(float throttleInput, float brakeInput, float steeringInput, int gear, float handbrake, float interval, float debugValue_0, float debugValue_1)
    {
        // get the current ground surface to calculate friction and stuff

        if(currentGround != getGroundType())
        {
            currentGround = getGroundType();
            if(Game.DEBUG)
            {
                System.out.println(currentGround.getType());
            }
        }

        // use input to adjust steering and calculate force
        steeringAngle = maxSteeringAngle * steeringInput;
        currentForce = maxEngineForce * throttleInput - maxBrakeForce * brakeInput;

        // change speed using the current acceleration
        acceleration = Physics.calcAcceleration(mass, currentForce);
        speed += acceleration * interval;

        /*
        //TODO: MASSE MUSS MIT REIN!!
        //6 * interval IS ONE REV PER MIN ON THE WHEEL!! (6 degrees per second * 60(360 per minute) * interval)
        float C_R = 0.4f;
        float C_H = 0.5f;

        if (Math.abs(speed) > (0.5f * C_H * Physics.G * interval))
        {
            if (speed > 0)
            {
                speed = speed - ((0.5f * C_R * Physics.G * interval));
            }
        }
        else
        {
            speed = 0;
        }*/


        float degToRad = (float)Math.toRadians(carAngle);
        float degToRadInclSteering = (float)Math.toRadians(carAngle - steeringAngle);

        steeringWheelForward.x = (float)Math.cos(degToRadInclSteering);
        steeringWheelForward.z = (float)Math.sin(degToRadInclSteering);

        forward.x = (float)Math.cos(degToRad);
        forward.z = (float)Math.sin(degToRad);
        left = new Vector3f(new Vector3f(carUp).cross(new Vector3f(forward)));

        Vector3f frontWheel = new Vector3f(position).add(new Vector3f(forward).mul(halfWheelBase));
        Vector3f rearWheel = new Vector3f(position).add(new Vector3f(forward).mul(-halfWheelBase));

        frontWheel.add(new Vector3f(steeringWheelForward).mul(speed * interval));
        rearWheel.add(new Vector3f(forward).mul(speed * interval));

        //push axis to side!
        //System.out.println(debugValue_0);
        //System.out.println(debugValue_1);

        Vector3f temp = new Vector3f(frontWheel).add(rearWheel);
        temp.div(2f);
        position = new Vector3f(temp);
        float newCarAngleInRad = (float)Math.atan2(frontWheel.z - rearWheel.z, frontWheel.x - rearWheel.x);
        carAngle = (float) Math.toDegrees(newCarAngleInRad);
        carAngle %= 360f;
        
        frontWheel.y = wheelRadius;
        rearWheel.y = wheelRadius;

        wheelPositions[0] = new Vector3f(frontWheel).add(new Vector3f(left).mul(-trackWidth));
        wheelPositions[1] = new Vector3f(frontWheel).add(new Vector3f(left).mul(trackWidth));
        wheelPositions[2] = new Vector3f(rearWheel).add(new Vector3f(left).mul(-trackWidth));
        wheelPositions[3] = new Vector3f(rearWheel).add(new Vector3f(left).mul(trackWidth));

        position.y = wheelRadius + suspensionHeight;

    }

    private GroundType getGroundType()
    {
        float temp = position.x;
        GroundType result;

        if(temp <= -10){ result = GroundTypes.ROAD; }
        else if(temp > -10 && temp <= 10) {result = GroundTypes.SAND_HARD; }
        else if(temp > 10 && temp <= 30) {result = GroundTypes.SAND_SOFT; }
        else {result = GroundTypes.SNOW; }

        return result;

    }

    public float getSteeringAngle()
    {
        return steeringAngle;
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public Vector3f getRotation()
    {
        return new Vector3f(0, carAngle, 0);
    }

    public Vector3f[] getWheelPositions()
    {
        return wheelPositions;
    }

    public float getWheelRadius()
    {
        return wheelRadius;
    }

    public void setWheelRadius(float wheelRadius)
    {
        this.wheelRadius = wheelRadius;
    }

    public float getSuspensionHeight()
    {
        return suspensionHeight;
    }

    public void setSuspensionHeight(float suspensionHeight)
    {
        this.suspensionHeight = suspensionHeight;
    }

    public float getSpeed()
    {
        return speed;
    }

}
