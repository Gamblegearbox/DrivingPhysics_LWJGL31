package game;

import engine.*;
import engine.Window;
import game.car.Car;
import engine.gameItem.GameItem;
import engine.light.DirectionalLight;
import engine.light.SceneLight;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game implements IGameLogic{

    private static final boolean DEBUG = true;
    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float INPUT_PEDAL_INCREASE = 0.025f;
    private static final float INPUT_PEDAL_DECREASE = 0.05f;
    private static final float INPUT_STEERING_SPEED = 0.1f;
    private static final float INPUT_MAX_VALUE = 1.0f;

    private final Renderer renderer;
    private final Camera camera;
    private final Vector3f cameraIncrement;

    private Car car;
    private GameItem carMesh;
    private GameItem frontLeftMesh;
    private GameItem frontRightMesh;
    private GameItem rearLeftMesh;
    private GameItem rearRightMesh;

    private Vector3f lightDirection;
    private Scene scene;
    private Hud hud;

    private float directionalLightAngle;

    private float throttleInput = 0;
    private float brakeInput = 0;
    private float steeringInput = 0;
    private float handbrakeInput = 0;
    private int gear = 0;
    private boolean isNight = false;

    //DEBUG VALUES
    private int totalUpdates = 0;
    private int totalRenderCycles = 0;
    private int totalInputCalls = 0;
    private float debugValue_0 = 0;
    private float debugValue_1 = 0;
    private final float debugValueIncrease = 0.01f;


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

        debugValue_0 = car.getSuspensionHeight();
        debugValue_1 = car.getWheelRadius();

        setupGameItems();
        setupLight();

        camera.setPosition(5, 2, 10);
        camera.setRotation(0,-45,0);
        setupHUD();
    }

    private void setupGameItems() throws Exception
    {
        Material material = new Material(new Vector3f(0.5f, 0.5f, 0.5f), 0f);
        Mesh mesh = OBJLoader.loadMesh("/models/GroundPlane.obj");
        mesh.setMaterial(material);
        GameItem ground = new GameItem(mesh);
        ground.setPosition(0, 0, 0);

        material = new Material(new Vector3f(1f, 0f, 0f), 1f);
        mesh = OBJLoader.loadMesh("/models/Wheel_Offroad.obj");
        mesh.setMaterial(material);
        frontLeftMesh = new GameItem(mesh);
        frontRightMesh = new GameItem(mesh);
        rearLeftMesh = new GameItem(mesh);
        rearRightMesh = new GameItem(mesh);

        material = new Material(new Vector3f(0.2f, 0.2f, 0.2f), 1f);
        mesh = OBJLoader.loadMesh("/models/Car_Offroad.obj");
        mesh.setMaterial(material);
        carMesh = new GameItem(mesh);
        carMesh.setPosition(car.getPosition());
        carMesh.setScale(1);

        scene.setGameItems(new GameItem[]{carMesh, ground, frontLeftMesh, frontRightMesh, rearLeftMesh, rearRightMesh});
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
        hud = new Hud("");
    }

    @Override
    public void input(Window window, MouseInput mouseInput)
    {
        if(DEBUG)
        {
            totalInputCalls++;

            if (window.isKeyPressed(GLFW_KEY_R))
            {
                debugValue_0 += debugValueIncrease;
            }
            else if (window.isKeyPressed(GLFW_KEY_F))
            {
                debugValue_0 -= debugValueIncrease;
            }

            if (window.isKeyPressed(GLFW_KEY_T))
            {
                debugValue_1 += debugValueIncrease;
            }
            else if (window.isKeyPressed(GLFW_KEY_G))
            {
                debugValue_1 -= debugValueIncrease;
            }
        }

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
            if(throttleInput < 0.0f)
            {
                throttleInput = 0.0f;
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
        else
        {
            if(steeringInput < -0.1f)
            {
                steeringInput += INPUT_STEERING_SPEED;
            }
            else if (steeringInput > 0.1f)
            {
                steeringInput -= INPUT_STEERING_SPEED;
            }
            else
            {
                steeringInput = 0;
            }
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput)
    {
        if(DEBUG)
        {
            totalUpdates++;
        }

        updateCameraAndCompass(mouseInput, interval);
        updateDirectionalLight();

        car.setSuspensionHeight(debugValue_0);
        car.setWheelRadius(debugValue_1);
        car.update(throttleInput, brakeInput, steeringInput, gear, handbrakeInput, interval);

        carMesh.setPosition(car.getPosition());
        carMesh.setRotation(car.getRotation());

        Vector3f[] wheelPositions = car.getWheelPositions();
        Vector3f carRotation = car.getRotation();
        frontLeftMesh.setPosition(wheelPositions[0]);
        frontLeftMesh.setRotation(carRotation.x, carRotation.y - car.getCurrentSteeringAngle(), carRotation.z);
        frontLeftMesh.getRotation().y += 180;
        frontLeftMesh.setScale(car.getWheelRadius() * 2.0f);

        frontRightMesh.setPosition(wheelPositions[1]);
        frontRightMesh.setRotation(carRotation.x, carRotation.y - car.getCurrentSteeringAngle(), carRotation.z);
        frontRightMesh.setScale(car.getWheelRadius() * 2.0f);

        rearLeftMesh.setPosition(wheelPositions[2]);
        rearLeftMesh.setRotation(carRotation);
        rearLeftMesh.getRotation().y += 180;
        rearLeftMesh.setScale(car.getWheelRadius() * 2.0f);

        rearRightMesh.setPosition(wheelPositions[3]);
        rearRightMesh.setRotation(carRotation);
        rearRightMesh.setScale(car.getWheelRadius() * 2.0f);

        hud.setStatusText("v: " + car.velocity + " / a: " + car.acceleration + " / Forward: " + car.carForward.length());
        //hud.setStatusText("Steering: " + steeringInput + " / Throttle: " + throttleInput + " / Brake: " + brakeInput);
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
        if(DEBUG)
        {
            totalRenderCycles++;
        }

        hud.updateSize(window);
        renderer.render(window, camera, scene, hud);
    }

    @Override
    public void cleanup()
    {
        renderer.cleanup();
        scene.cleanup();
        hud.cleanup();

        if(DEBUG)
        {
            System.out.println("Input Cycles: " + totalInputCalls);
            System.out.println("Update Cycles: " + totalUpdates);
            System.out.println("Render Cycles: " + totalRenderCycles);
        }
    }


}
