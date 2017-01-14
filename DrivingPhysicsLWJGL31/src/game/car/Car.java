package game.car;

import org.joml.Math;
import org.joml.Vector3f;

import java.util.Vector;

public class Car {

    private final float maxSteeringAngle = 35f;

    private final float frontAxlePos = 1.55f;
    private final float rearAxlePos = -1.45f;
    private final float trackWidth = 0.867f;

    private float currentSteeringAngle;
    private float currentEngineRpm;
    private float wheelRadius;
    private float suspensionHeight;
    private float currentCarAngle = 0;

    private Vector3f carPosition;
    private Vector3f carForward;
    private Vector3f carUp;
    private Vector3f carLeft;

    public Car()
    {
        wheelRadius = 0.43f;
        suspensionHeight = 0.2f;
        currentSteeringAngle = 0;
        currentEngineRpm = 0;

        carPosition = new Vector3f(0,0,0);
        carForward = new Vector3f(0,0,1);
        carUp = new Vector3f(0,1,0);
        carLeft = new Vector3f(1,0,0);
    }

    public void update(float throttleInput, float brakeInput, float steeringInput, int gear, float handbrake, float interval)
    {
        //6 * interval IS ONE REV PER MIN!! (6 degrees per second * 60(360 per minute) * interval)
        if(Math.abs(currentSteeringAngle) <= maxSteeringAngle)
        {
            currentSteeringAngle = maxSteeringAngle * steeringInput;
        }

        currentCarAngle -= steeringInput;
        currentCarAngle %= 360f;

        float degToRad = (float)Math.toRadians(currentCarAngle);

        carForward.x = -(float)Math.sin(degToRad);
        carForward.z = (float)Math.cos(degToRad);
        carForward.normalize();

        Vector3f movement = new Vector3f(carForward).mul(throttleInput * 10).mul(interval);
        carPosition.add(movement);

        carPosition.y = wheelRadius + suspensionHeight;
    }

    private float calcCurrentEngineTorque()
    {
        //todo: find a good formula for a torpque/rpm curve
        return currentEngineRpm / 10;
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
