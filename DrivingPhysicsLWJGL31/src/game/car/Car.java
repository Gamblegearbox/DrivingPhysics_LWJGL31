package game.car;

import engine.Physics;
import org.joml.Math;
import org.joml.Vector3f;

public class Car {

    private final float maxSteeringAngle = 35f;

    private final float frontAxlePos = 1.74f;
    private final float rearAxlePos = -1.41f;
    private final float trackWidth = 0.8f;
    private final float maxSpeed = 13.888888889f;
    private final float mass;
    private final float maxEngineForce;
    private final float maxBrakeForce;

    private float currentSteeringAngle;
    private float wheelRadius;
    private float suspensionHeight;
    private float currentCarAngle = 0;

    private Vector3f carPosition;
    public Vector3f carForward;
    private Vector3f carUp;
    private Vector3f carLeft;
    public float acceleration;
    public float speed;
    public float kilometersPerHour;
    public float currentForce;


    public Car()
    {
        wheelRadius = 0.43f;
        suspensionHeight = 0.2f;
        mass = 2000f;
        maxEngineForce = 6000f;
        maxBrakeForce = 12000f;
        currentSteeringAngle = 0;

        carPosition = new Vector3f(0,0,0);
        carForward = new Vector3f(0,0,1);
        carUp = new Vector3f(0,1,0);
        carLeft = new Vector3f(1,0,0);
    }

    public void update(float throttleInput, float brakeInput, float steeringInput, int gear, float handbrake, float interval)
    {
        //6 * interval IS ONE REV PER MIN ON THE WHEEL!! (6 degrees per second * 60(360 per minute) * interval)
        if(Math.abs(currentSteeringAngle) <= maxSteeringAngle)
        {
            currentSteeringAngle = maxSteeringAngle * steeringInput;
        }

        //make sure that the car turns in the right direction when driving in reverse
        if(speed >= 0)
        {
            currentCarAngle -= steeringInput;
        }
        else
        {
            currentCarAngle += steeringInput;
        }

        currentCarAngle %= 360f;

        float degToRad = (float)Math.toRadians(currentCarAngle);
        carForward.x = -(float)Math.sin(degToRad);
        carForward.z = (float)Math.cos(degToRad);
        carForward.normalize();


        currentForce = maxEngineForce * throttleInput - maxBrakeForce * brakeInput;
        acceleration = Physics.calcAcceleration(mass, currentForce);

        speed += acceleration * interval;
        if(speed > maxSpeed)
        {
            speed = maxSpeed;
        }
        else if(speed < -maxSpeed)
        {
            speed = -maxSpeed;
        }

        kilometersPerHour = Physics.metersPerSecondToKilometersPerHour(speed);

        /*
        //TODO: MASSE MUSS MIT REIN!!
        float C_R = 0.4f;
        float C_H = 0.5f;

        if (Math.abs(speed) > (0.5f * C_H * Physics.G * interval) * 4.0f)
        {
            if (speed > 0)
            {
                speed = speed - ((0.5f * C_R * Physics.G * interval) * 4.0f);
            }
        }
        else
        {
            speed = 0;
        }
        */

        Vector3f velocity = new Vector3f(carForward).mul(speed);
        carPosition.add(velocity.mul(interval));
        carPosition.y = wheelRadius + suspensionHeight;
    }

    public float getCurrentSteeringAngle()
    {
        return currentSteeringAngle;
    }

    public Vector3f getPosition()
    {
        return carPosition;
    }

    public Vector3f getRotation()
    {
        return new Vector3f(0, currentCarAngle, 0);
    }

    public Vector3f[] getWheelPositions()
    {
        carLeft = new Vector3f(new Vector3f(carUp).cross(new Vector3f(carForward)));

        Vector3f[] result = new Vector3f[4];
        Vector3f temp = new Vector3f(0,0,0);

        temp.set(carPosition);
        temp.add(new Vector3f(carForward).mul(frontAxlePos));
        temp.add(new Vector3f(carLeft).mul(-trackWidth));
        temp.y = wheelRadius;
        result[0] = new Vector3f(temp);

        temp.set(carPosition);
        temp.add(new Vector3f(carForward).mul(frontAxlePos));
        temp.add(new Vector3f(carLeft).mul(trackWidth));
        temp.y = wheelRadius;
        result[1] = new Vector3f(temp);

        temp.set(carPosition);
        temp.add(new Vector3f(carForward).mul(rearAxlePos));
        temp.add(new Vector3f(carLeft).mul(-trackWidth));
        temp.y = wheelRadius;
        result[2] = new Vector3f(temp);

        temp.set(carPosition);
        temp.add(new Vector3f(carForward).mul(rearAxlePos));
        temp.add(new Vector3f(carLeft).mul(trackWidth));
        temp.y = wheelRadius;
        result[3] = new Vector3f(temp);
        return result;
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



}
