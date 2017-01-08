package engine.car;

import engine.Material;
import engine.Mesh;
import engine.OBJLoader;
import engine.gameItem.GameItem;
import org.joml.Vector3f;

public class Car {

    private final float engineMaxRpm = 6500;
    private final float engineIdleRpm = 750;
    private final float maxSteeringAngle = 35f;

    private final float frontAxlePos = 1.55f;
    private final float rearAxlePos = -1.45f;
    private final float wheelHeight = 0.40f;
    private final float trackWidth = 0.867f;

    private final GameItem carBody;
    private final GameItem wheelFrontLeft;
    private final GameItem wheelFrontRight;
    private final GameItem wheelRearLeft;
    private final GameItem wheelRearRight;

    private float currentSteeringAngle;
    private float currentEngineRpm;

    private Vector3f position;


    public Car() throws Exception
    {
        position = new Vector3f(0,0,0);
        currentSteeringAngle = 0;
        currentEngineRpm = 0;

        Mesh mesh = OBJLoader.loadMesh("/models/Car_Offroad.obj");
        Material material = new Material(new Vector3f(0.5f, 0.5f, 0.5f), 1f);
        mesh.setMaterial(material);
        carBody = new GameItem(mesh);
        carBody.setPosition(0, 0, 0);

        material = new Material(new Vector3f(0.2f, 0.2f, 0.2f), 0.5f);
        mesh = OBJLoader.loadMesh(("/models/Wheel_Offroad.obj"));
        mesh.setMaterial(material);
        wheelFrontLeft = new GameItem(mesh);
        wheelFrontRight = new GameItem(mesh);
        wheelRearLeft = new GameItem(mesh);
        wheelRearRight = new GameItem(mesh);
        wheelFrontLeft.setPosition(trackWidth, wheelHeight, frontAxlePos);
        wheelFrontRight.setPosition(-trackWidth, wheelHeight, frontAxlePos);
        wheelFrontRight.setRotation(0, 0, 180);
        wheelRearLeft.setPosition(trackWidth, wheelHeight, rearAxlePos);
        wheelRearRight.setPosition(-trackWidth, wheelHeight, rearAxlePos);
        wheelRearRight.setRotation(0, 0, 180);
    }

    public void update(float throttleInput, float brakeInput, float steeringInput, int gear, float handbrake, float interval)
    {
        if(Math.abs(currentSteeringAngle) <= maxSteeringAngle)
        {
            currentSteeringAngle = maxSteeringAngle * steeringInput;
        }

        position.z += throttleInput;
        position.z -= brakeInput;

        wheelFrontLeft.getRotation().y = -currentSteeringAngle;
        wheelFrontRight.getRotation().y = -currentSteeringAngle;

        wheelRearLeft.getRotation().x -= currentEngineRpm * interval;
        wheelRearRight.getRotation().x -= 6 * interval; //THIS IS ONE REV PER MIN!! (6 degrees per second * 60(360 per minute) * interval)

        carBody.setPosition(position);
    }

    private void raiseRpm()
    {
        if(currentEngineRpm < engineMaxRpm)
        {
            currentEngineRpm += 1;
        }
    }

    private void lowerRpm()
    {
        if(currentEngineRpm > engineIdleRpm)
        {
            currentEngineRpm -= 1;
        }
    }

    public float getCurrentEngineRpm()
    {
        return currentEngineRpm;
    }

    private float calcCurrentEngineTorque()
    {
        //todo: find a good formula for a torpque/rpm curve
        return currentEngineRpm / 10;
    }

    public GameItem[] getGameItems()
    {
        GameItem[] temp = new GameItem[]{carBody, wheelFrontLeft, wheelFrontRight, wheelRearLeft, wheelRearRight};

        return temp;
    }
}
