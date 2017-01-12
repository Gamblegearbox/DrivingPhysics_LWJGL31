package game.car;

import engine.Material;
import engine.Mesh;
import engine.OBJLoader;
import engine.gameItem.GameItem;
import org.joml.Math;
import org.joml.Vector3f;

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

    public Car() throws Exception
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

    public void update(float throttleInput, float brakeInput, float steeringInput, int gear, float handbrake, float interval, float debugValue_0, float debugValue_1)
    {
        //6 * interval IS ONE REV PER MIN!! (6 degrees per second * 60(360 per minute) * interval)
        if(Math.abs(currentSteeringAngle) <= maxSteeringAngle)
        {
            currentSteeringAngle = maxSteeringAngle * steeringInput;
        }

        currentCarAngle += steeringInput;
        currentCarAngle %= 360f;

        float tempSuspensionHeight = suspensionHeight * debugValue_0;
        carPosition.y = wheelRadius + tempSuspensionHeight;
        carPosition.z += throttleInput;
        carPosition.z -= brakeInput;

        float tempWheelRadius = wheelRadius * debugValue_1;
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

}
