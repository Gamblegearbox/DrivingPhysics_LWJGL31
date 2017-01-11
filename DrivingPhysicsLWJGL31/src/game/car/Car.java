package game.car;

import engine.Material;
import engine.Mesh;
import engine.OBJLoader;
import engine.gameItem.GameItem;
import org.joml.Math;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class Car {


    private final float maxSteeringAngle = 35f;

    private final float frontAxlePos = 1.55f;
    private final float rearAxlePos = -1.45f;
    private final float trackWidth = 0.867f;

    private final Wheel frontLeft;
    private final Wheel frontRight;
    private final Wheel rearLeft;
    private final Wheel rearRight;

    private final GameItem carBody;
    private final GameItem wheelFrontLeft;
    private final GameItem wheelFrontRight;
    private final GameItem wheelRearLeft;
    private final GameItem wheelRearRight;

    private float currentSteeringAngle;
    private float currentEngineRpm;
    private float wheelRadius;
    private float suspensionHeight;

    private Vector3f carBodyPosition;
    private Vector3f wheelFLPosition;
    private Vector3f wheelFRPosition;
    private Vector3f wheelRLPosition;
    private Vector3f wheelRRPosition;


    public Car() throws Exception
    {
        wheelRadius = 0.43f;
        suspensionHeight = 0.2f;
        currentSteeringAngle = 0;
        currentEngineRpm = 0;

        frontLeft = new Wheel(wheelRadius);
        frontRight = new Wheel(wheelRadius);
        rearLeft = new Wheel(wheelRadius);
        rearRight = new Wheel(wheelRadius);

        carBodyPosition = new Vector3f(0,0,0);
        wheelFLPosition = new Vector3f(trackWidth, frontLeft.getRadius(), frontAxlePos);
        wheelFRPosition = new Vector3f(-trackWidth, frontRight.getRadius(), frontAxlePos);
        wheelRLPosition = new Vector3f(trackWidth, rearLeft.getRadius(), rearAxlePos);
        wheelRRPosition = new Vector3f(-trackWidth, rearRight.getRadius(), rearAxlePos);

        Mesh mesh = OBJLoader.loadMesh("/models/Car_Offroad.obj");
        Material material = new Material(new Vector3f(0.5f, 0.5f, 0.5f), 1f);
        mesh.setMaterial(material);
        carBody = new GameItem(mesh);
        carBody.setPosition(0, 0, 0);

        material = new Material(new Vector3f(0.2f, 0.2f, 0.2f), 0.5f);
        mesh = OBJLoader.loadMesh(("/models/Wheel_Offroad.obj"));
        mesh.setMaterial(material);

        wheelFrontLeft = new GameItem(mesh);
        wheelFrontLeft.setPosition(wheelFLPosition);
        wheelFrontLeft.setScale(frontLeft.getDiameter());

        wheelFrontRight = new GameItem(mesh);
        wheelFrontRight.setPosition(wheelFRPosition);
        wheelFrontRight.setScale(frontRight.getDiameter());
        wheelFrontRight.setRotation(0, 0, 180);

        wheelRearLeft = new GameItem(mesh);
        wheelRearLeft.setPosition(wheelRLPosition);
        wheelRearLeft.setScale(rearLeft.getDiameter());

        wheelRearRight = new GameItem(mesh);
        wheelRearRight.setPosition(wheelRRPosition);
        wheelRearRight.setScale(rearRight.getDiameter());
        wheelRearRight.setRotation(0, 0, 180);
    }

    public void update(float throttleInput, float brakeInput, float steeringInput, int gear, float handbrake, float interval, float debugValue_0, float debugValue_1)
    {
        if(Math.abs(currentSteeringAngle) <= maxSteeringAngle)
        {
            currentSteeringAngle = maxSteeringAngle * steeringInput;
        }

        carBodyPosition.z += throttleInput;
        carBodyPosition.z -= brakeInput;

        float tempSuspensionHeight = suspensionHeight * debugValue_0;
        carBodyPosition.y = wheelRadius + tempSuspensionHeight;
        carBody.setPosition(carBodyPosition);


        float tempWheelRadius = wheelRadius * debugValue_1;
        frontLeft.setRadius(tempWheelRadius);
        frontRight.setRadius(tempWheelRadius);
        rearLeft.setRadius(tempWheelRadius);
        rearRight.setRadius(tempWheelRadius);

        wheelFLPosition.y = frontLeft.getRadius();
        wheelFRPosition.y = frontRight.getRadius();
        wheelRLPosition.y = rearLeft.getRadius();
        wheelRRPosition.y = rearRight.getRadius();

        wheelFrontLeft.getRotation().y = -currentSteeringAngle;
        wheelFrontRight.getRotation().y = -currentSteeringAngle;

        //6 * interval IS ONE REV PER MIN!! (6 degrees per second * 60(360 per minute) * interval)
        wheelRearLeft.getRotation().x -= 60 * interval;
        wheelRearRight.getRotation().x -= 60 * interval;

        wheelFrontLeft.setPosition(wheelFLPosition);
        wheelFrontLeft.setScale(frontLeft.getDiameter());

        wheelFrontRight.setPosition(wheelFRPosition);
        wheelFrontRight.setScale(frontRight.getDiameter());

        wheelRearLeft.setPosition(wheelRLPosition);
        wheelRearLeft.setScale(rearLeft.getDiameter());

        wheelRearRight.setPosition(wheelRRPosition);
        wheelRearRight.setScale(rearRight.getDiameter());
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
