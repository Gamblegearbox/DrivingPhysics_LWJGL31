package game;

import engine.camera.Camera;
import engine.core.EngineOptions;
import engine.core.Window;
import engine.core.Renderer;
import engine.gameEntities.GameEntity;
import engine.mesh.Mesh;
import engine.interfaces.IGameLogic;
import engine.physics.PROTO_Rigidbody;
import engine.physics.Particle;
import engine.scene.Scene;
import engine.shading.Material;
import engine.texture.Texture;
import engine.input.MouseInput;
import engine.utils.DebugMeshes;
import engine.utils.OBJLoader;
import engine.physics.Physics;
import game.car.Car;
import engine.light.DirectionalLight;
import engine.scene.SceneLight;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {


    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float INPUT_PEDAL_INCREASE = 0.02f;
    private static final float INPUT_PEDAL_DECREASE = 0.06f;
    private static final float INPUT_STEERING_SPEED = 0.1f;
    private static final float INPUT_MAX_VALUE = 1.0f;
    private static final float CAMERA_SPEED = 5f;
    private static final float CAMERA_SPEED_FAST = 10f;

    private final Renderer renderer;
    private final Camera camera;
    private final Vector3f cameraIncrement;

    private Texture texture;

    private Car car;
    private GameEntity carMesh;
    private GameEntity axleMesh;
    private GameEntity frontLeftMesh;
    private GameEntity frontRightMesh;
    private GameEntity rearLeftMesh;
    private GameEntity rearRightMesh;
    private GameEntity[] smokeEntities;
    private Particle[] smokeParticles;

    private PROTO_Rigidbody testCube;
    private GameEntity testCubeMesh_1;

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

    private GameEntity debugArrowRadius;

    public Game()
    {
        renderer = new Renderer();
        camera = new Camera();
        cameraIncrement = new Vector3f(0, 0, 0);
        directionalLightAngle = 35;
    }

    @Override
    public void init(Window window) throws Exception
    {
        renderer.init();
        scene = new Scene();
        car = new Car();

        setupGameItems();
        setupLight();

        camera.setPosition(0, 5, 20);
        camera.setRotation(0,0,0);
        setupHUD();

        if(EngineOptions.DEBUG)
        {
            EngineOptions.printInfo();
        }

    }

    private void setupGameItems() throws Exception
    {
        // load color palette and apply it to material
        texture = new Texture("/textures/colorsFromPicture.png");
        Material material = new Material(texture, 0f);

        // create ground
        Mesh mesh = OBJLoader.loadMesh("/models/GroundPlane.obj");
        mesh.setMaterial(material);
        GameEntity ground = new GameEntity(mesh);
        ground.setPosition(0, 0, 0);

        // create car, axles and wheels
        mesh = OBJLoader.loadMesh("/models/Car_Offroad.obj");
        mesh.setMaterial(material);
        carMesh = new GameEntity(mesh);

        mesh = OBJLoader.loadMesh("/models/Car_Offroad_Axles.obj");
        mesh.setMaterial(material);
        axleMesh = new GameEntity(mesh);

        mesh = OBJLoader.loadMesh("/models/Wheel_Offroad.obj");
        mesh.setMaterial(material);
        frontLeftMesh = new GameEntity(mesh);
        frontRightMesh = new GameEntity(mesh);
        rearLeftMesh = new GameEntity(mesh);
        rearRightMesh = new GameEntity(mesh);

        smokeEntities = new GameEntity[100];
        smokeParticles = new Particle[smokeEntities.length];
        mesh = DebugMeshes.buildQuad();
        mesh.setMaterial(Materials.WHITE);
        Random random = new Random();
        for(int i = 0; i < smokeEntities.length; i++)
        {
            smokeParticles[i] = new Particle(random.nextInt(250), random.nextFloat(), random.nextFloat() * 0.5f, random.nextFloat() * 0.005f, random.nextFloat() * 0.001f);
            smokeEntities[i] = new GameEntity(mesh);
            smokeEntities[i].getPosition().y = 2f;
        }

        // add objects to List of GameItems
        ArrayList<GameEntity> gameEntities = new ArrayList<>();
        gameEntities.add(ground);
        gameEntities.add(carMesh);
        gameEntities.add(axleMesh);
        gameEntities.add(frontLeftMesh);
        gameEntities.add(frontRightMesh);
        gameEntities.add(rearLeftMesh);
        gameEntities.add(rearRightMesh);
        for(int i = 0; i < smokeEntities.length; i++)
        {
            gameEntities.add(smokeEntities[i]);
        }


        // create debug objects
        if(EngineOptions.DEBUG)
        {
            // RIGID BODY CUBE
            mesh = OBJLoader.loadMesh("/models/REF_ONE_CUBIC_METER.obj");
            mesh.setMaterial(Materials.RED);
            testCubeMesh_1 = new GameEntity(mesh);
            testCubeMesh_1.setPosition(0, 25f, -15);
            testCube = new PROTO_Rigidbody(testCubeMesh_1.getPosition(), 1f);

            mesh = DebugMeshes.buildline();
            mesh.setMaterial(Materials.WHITE);
            debugArrowRadius = new GameEntity(mesh);

            gameEntities.add(debugArrowRadius);
            gameEntities.add(testCubeMesh_1);
        }

        // add objects to scene
        scene.setGameItems(gameEntities);
    }

    private void setupLight()
    {
        SceneLight sceneLight = new SceneLight();
        sceneLight.setAmbientLight(new Vector3f(0.2f, 0.2f, 0.2f));

        lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, 1f);

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
        float cameraSpeed = window.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? CAMERA_SPEED_FAST : CAMERA_SPEED;

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

        if(EngineOptions.DEBUG)
        {
            totalInputCalls++;
        }

    }

    int counter = 0;

    @Override
    public void update(float interval, MouseInput mouseInput)
    {
        updateCamera(mouseInput, interval);
        hud.updateCompass(camera.getRotation().y);
        updateDirectionalLight();

        car.update(throttleInput, brakeInput, steeringInput, gear, handbrakeInput, interval);
        Vector3f carPosition = car.position;
        Vector3f carRotation = car.rotation;
        Vector3f[] wheelPositions = car.wheelPositions;
        float steeringAngle = car.steeringAngle;
        float frontWheelRotation = car.frontWheelSpinAngle;
        float rearWheelRotation = car.rearWheelSpinAngle;
        float wheelRadius = car.wheelRadius;
        float wheelDiameter = wheelRadius * 2;

        for(int i = 0; i < smokeParticles.length; i++)
        {
            Particle particle = smokeParticles[i];
            particle.update();

            if(smokeParticles[i].isActive)
            {
                smokeEntities[i].getPosition().y += particle.riseSpeed;
                smokeEntities[i].setRotation(0, particle.rotation, 0);
                smokeEntities[i].setScale(particle.size);
            }
            else
            {
                particle.isActive = true;
                smokeEntities[i].setPosition(wheelPositions[counter%4]);
                smokeEntities[i].getPosition().y = 0.001f;
            }
        }
        counter++;
        counter%= smokeEntities.length;

        carMesh.setPosition(carPosition);
        carMesh.setRotation(carRotation);
        axleMesh.setPosition(carPosition);
        axleMesh.getPosition().y = wheelRadius;
        axleMesh.setRotation(0, carRotation.y, 0);

        frontLeftMesh.setPosition(wheelPositions[1]);
        frontLeftMesh.setRotation(0, carRotation.y + steeringAngle, frontWheelRotation);
        frontLeftMesh.setScale(wheelDiameter);

        frontRightMesh.setPosition(wheelPositions[0]);
        frontRightMesh.setRotation(0, carRotation.y + 180 + steeringAngle, -frontWheelRotation);
        frontRightMesh.setScale(wheelDiameter);

        rearLeftMesh.setPosition(wheelPositions[3]);
        rearLeftMesh.setRotation(0, carRotation.y, rearWheelRotation);
        rearLeftMesh.setScale(wheelDiameter);

        rearRightMesh.setPosition(wheelPositions[2]);
        rearRightMesh.setRotation(0, carRotation.y + 180, -rearWheelRotation);
        rearRightMesh.setScale(wheelDiameter);

        hud.setStatusText("Speed: " + Physics.convertMPStoKMH(car.speed) + "KM/H");

        if(EngineOptions.DEBUG)
        {
            totalUpdates++;

            testCube.update(interval);
            testCubeMesh_1.setPosition(testCube.getPosition());

            debugArrowRadius.setPosition(car.rearWheelsPosition);
            debugArrowRadius.getPosition().y = 0.1f;
            debugArrowRadius.setRotation(0, carRotation.y + 90, 0);
            debugArrowRadius.setScale(car.turningRadius, 1, 1);

            hud.updateDebugHUD(car.combinedForces, car.maxFrontAxleForce, car.maxRearAxleForce);
        }
    }

    private void updateCamera(MouseInput mouseInput, float interval)
    {
        if (mouseInput.isRightButtonPressed())
        {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
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

        if(EngineOptions.DEBUG)
        {
            totalRenderCycles++;
        }
    }

    @Override
    public void cleanup()
    {
        renderer.cleanup();
        scene.cleanup();
        hud.cleanup();

        if(EngineOptions.DEBUG)
        {
            System.out.println("Input Cycles: " + totalInputCalls);
            System.out.println("Update Cycles: " + totalUpdates);
            System.out.println("Render Cycles: " + totalRenderCycles);
        }
    }

}
