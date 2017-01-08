package game;

import engine.*;
import engine.Window;
import engine.car.Car;
import engine.gameItem.GameItem;
import engine.light.DirectionalLight;
import engine.light.SceneLight;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic{

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float INPUT_PEDAL_INCREASE = 0.025f;
    private static final float INPUT_PEDAL_DECREASE = 0.05f;
    private static final float INPUT_STEERING_SPEED = 0.02f;
    private static final float INPUT_MAX_VALUE = 1.0f;


    private final Renderer renderer;
    private final Camera camera;
    private final Vector3f cameraIncrement;

    private Car car;
    private Vector3f lightDirection;
    private Scene scene;
    private GameItem carBody;
    private GameItem wheelFrontLeft;
    private GameItem wheelFrontRight;
    private GameItem wheelRearLeft;
    private GameItem wheelRearRight;
    private Hud hud;

    private float directionalLightAngle;

    private float throttleInput = 0;
    private float brakeInput = 0;
    private float steeringInput = 0;
    private float handbrakeInput = 0;
    private int gear = 0;
    private boolean isNight = false;


    public Game()
    {
        renderer = new Renderer();
        camera = new Camera();
        cameraIncrement = new Vector3f(0, 0, 0);
        directionalLightAngle = 45;
    }

    @Override
    public void init(Window window) throws Exception
    {
        renderer.init(window);
        scene = new Scene();
        car = new Car();

        setupGameItems();
        setupLight();

        camera.setPosition(0, 2, 10);
        setupHUD();
    }

    private void setupGameItems() throws Exception
    {
        float frontAxlePos = 1.55f;
        float rearAxlePos = -1.45f;
        float wheelHeight = 0.40f;
        float trackWidth = 0.867f;

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

        material = new Material(new Vector3f(0.5f, 0.5f, 0.5f), 0f);
        mesh = OBJLoader.loadMesh("/models/GroundPlane.obj");
        mesh.setMaterial(material);
        GameItem ground = new GameItem(mesh);
        ground.setPosition(0, 0, 0);

        material = new Material(new Vector3f(0.5f, 0.5f, 0.5f), 0f);
        mesh = OBJLoader.loadMesh("/models/REF_ONE_CUBIC_METER.obj");
        mesh.setMaterial(material);
        GameItem oneCubicMeter = new GameItem(mesh);
        oneCubicMeter.setPosition(0, 0.5f, 0);

        scene.setGameItems(new GameItem[]{carBody, ground, oneCubicMeter, wheelFrontLeft, wheelFrontRight, wheelRearLeft, wheelRearRight});
    }

    private void setupLight()
    {
        SceneLight sceneLight = new SceneLight();
        sceneLight.setAmbientLight(new Vector3f(0.2f, 0.2f, 0.2f));

        lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, 1f);
        directionalLight.setShadowPosMult(5);
        directionalLight.setOrthoCoords(-20.0f, 20.0f, -20.0f, 20.0f, -1.0f, 20.0f);

        sceneLight.setDirectionalLight(directionalLight);
        scene.setSceneLight(sceneLight);
    }

    private void setupHUD() throws Exception
    {
        hud = new Hud("LightAngle: ");
    }

    @Override
    public void input(Window window, MouseInput mouseInput)
    {
        float cameraSpeed = window.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 5f : 2f;

        cameraIncrement.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) { cameraIncrement.z = -cameraSpeed; }
        else if (window.isKeyPressed(GLFW_KEY_S)) { cameraIncrement.z = cameraSpeed; }

        if (window.isKeyPressed(GLFW_KEY_A)) { cameraIncrement.x = -cameraSpeed; }
        else if (window.isKeyPressed(GLFW_KEY_D)) { cameraIncrement.x = cameraSpeed; }

        if (window.isKeyPressed(GLFW_KEY_Q)) { cameraIncrement.y = -cameraSpeed; }
        else if (window.isKeyPressed(GLFW_KEY_E)) { cameraIncrement.y = cameraSpeed; }

        if(window.isKeyPressed(GLFW_KEY_1)) { directionalLightAngle += 1.0f; }
        else if(window.isKeyPressed(GLFW_KEY_2)) { directionalLightAngle -= 1.0f;}

        if (window.isKeyPressed(GLFW_KEY_UP))
        {
            throttleInput += INPUT_PEDAL_INCREASE;
            if(throttleInput > INPUT_MAX_VALUE)
            {
                throttleInput = INPUT_MAX_VALUE;
            }
        }
        else
        {
            throttleInput -= INPUT_PEDAL_DECREASE;
            if(throttleInput < 0f)
            {
                throttleInput = 0f;
            }
        }

        if (window.isKeyPressed(GLFW_KEY_DOWN))
        {
            brakeInput += INPUT_PEDAL_INCREASE;
            if(brakeInput > INPUT_MAX_VALUE)
            {
                brakeInput = INPUT_MAX_VALUE;
            }
        }
        else
        {
            brakeInput -= INPUT_PEDAL_DECREASE;
            if(brakeInput < 0.0f)
            {
                brakeInput = 0.0f;
            }
        }

        if (window.isKeyPressed(GLFW_KEY_LEFT))
        {
            steeringInput += INPUT_STEERING_SPEED;
            if(steeringInput > INPUT_MAX_VALUE)
            {
                steeringInput = INPUT_MAX_VALUE;
            }
        }
        else if (window.isKeyPressed(GLFW_KEY_RIGHT))
        {
            steeringInput -= INPUT_STEERING_SPEED;
            if(steeringInput < -INPUT_MAX_VALUE)
            {
                steeringInput = -INPUT_MAX_VALUE;
            }
        }

    }

    @Override
    public void update(float interval, MouseInput mouseInput)
    {
        updateCameraAndCompass(mouseInput, interval);
        updateDirectionalLight();

        car.update(throttleInput, brakeInput, steeringInput, gear, handbrakeInput);

        float temp = car.getCurrentSteeringAngle();
        wheelFrontLeft.getRotation().y = -temp;
        wheelFrontRight.getRotation().y = -temp;

        wheelRearLeft.getRotation().x -= car.getCurrentEngineRpm() * interval;
        wheelRearRight.getRotation().x -= 6 * interval; //THIS IS ONE REV PER MIN!! (6 * interval)

        hud.setStatusText("Steering: " + steeringInput + " / Throttle: " + throttleInput + " / Brake: " + brakeInput);
    }

    private void updateCameraAndCompass(MouseInput mouseInput, float interval)
    {
        if (mouseInput.isRightButtonPressed())
        {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);

            hud.rotateCompass(camera.getRotation().y);
        }
        camera.movePosition(cameraIncrement.x * interval, cameraIncrement.y * interval, cameraIncrement.z * interval);
    }

    private void updateDirectionalLight()
    {
        DirectionalLight directionalLight = scene.getSceneLight().getDirectionalLight();

        float zValue = (float)Math.cos(Math.toRadians(directionalLightAngle));
        float yValue = (float)Math.sin(Math.toRadians(directionalLightAngle));

        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();

        scene.getSceneLight().getDirectionalLight().setDirection(lightDirection);

        float absDirectionalAngle = Math.abs(directionalLightAngle - 90);

        if (absDirectionalAngle > 90)
        {
            isNight = !isNight;
            if(directionalLightAngle > 180)
            {
                directionalLightAngle = 0;
            }
            else if(directionalLightAngle < 0)
            {
                directionalLightAngle = 180;
            }
        }

        if (absDirectionalAngle > 80f)
        {
            float currentIntensity = 1 - (absDirectionalAngle - 80f) / 10.0f;
            directionalLight.setIntensity(currentIntensity);
        }

        if (absDirectionalAngle > 70f && !isNight)
        {
            float r = 1;
            float g = 1 - (absDirectionalAngle - 70f) / 20.0f;
            float b = 1 - (absDirectionalAngle - 70f) / 10.0f;

            if (g < 0) { g = 0; }
            if (b < 0) { b = 0; }

            directionalLight.getColor().x = r;
            directionalLight.getColor().y = g;
            directionalLight.getColor().z = b;
        }
        else
        {
            if(isNight)
            {
                directionalLight.getColor().x = 0.3f;
                directionalLight.getColor().y = 0.3f;
                directionalLight.getColor().z = 0.5f;
            }
            else
            {
                directionalLight.getColor().x = 1;
                directionalLight.getColor().y = 1;
                directionalLight.getColor().z = 1;
            }
        }
    }

    @Override
    public void render(Window window)
    {
        hud.updateSize(window);
        renderer.render(window, camera, scene, hud);
    }

    @Override
    public void cleanup()
    {
        renderer.cleanup();
        scene.cleanup();
        hud.cleanup();
    }


}
