package game;

import engine.camera.Camera;
import engine.core.EngineOptions;
import engine.core.Window;
import engine.core.Renderer;
import engine.gameEntities.GameEntity;
import engine.input.KeyboardInput;
import engine.mesh.Mesh;
import engine.interfaces.IGameLogic;
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
import game.car.DriveTrain;
import game.car.Engine;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float INPUT_PEDAL_INCREASE = 0.02f;
    private static final float INPUT_PEDAL_DECREASE = 0.06f;
    private static final float INPUT_STEERING_SPEED = 0.1f;
    private static final float INPUT_MAX_VALUE = 1.0f;

    private static final float CAMERA_SPEED = 5f;
    private static final float CAMERA_SPEED_FAST = 10f;
    private static final float FOLLOW_CAMERA_LERP_SPEED = 5f;
    private static final float MAX_CAMERA_DISTANCE = 100;
    private static final float MIN_CAMERA_DISTANCE = 2.5f;
    private float followCameraDistance = 15f;

    private final Renderer renderer;
    private final Camera camera;
    private final Vector3f cameraIncrement;
    private int cameraMode;

    private Texture texture;
    private Car[] cars;
    private int activeCar;

    private GameEntity[] skidMeshes;

    private int skidMeshesIndexCounter = 0;

    private Vector3f lightDirection;
    private Scene scene;
    private Hud hud;

    private float directionalLightAngle;
    private float throttleInput = 0;
    private float brakeInput = 0;
    private float steeringInput = 0;
    private float handbrakeInput = 0;
    private boolean isNight = false;

    //DEBUG VALUES
    private int totalUpdates = 0;
    private int totalRenderCycles = 0;
    private int totalInputCalls = 0;

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

        setupGameObjects();
        setupLight();

        camera.setPosition(0, followCameraDistance, 35);
        camera.setRotation(45,0,0);
        cameraMode = 0;
        setupHUD();

        if(EngineOptions.DEBUG)
        {
            EngineOptions.printInfo();
        }
    }

    private void setupGameObjects() throws Exception
    {
        // load color palette and apply it to material
        texture = new Texture("/textures/colorsFromPicture.png");
        Material material = new Material(texture, 0f);

        // create ground
        Mesh mesh = OBJLoader.loadMesh("/models/GroundPlane.obj");
        mesh.setMaterial(material);
        GameEntity ground = new GameEntity(mesh);
        ground.setPosition(0, 0, 0);

        // create offroad, axles and wheels
        mesh = OBJLoader.loadMesh("/models/Car_Offroad.obj");
        mesh.setMaterial(material);
        GameEntity car_Mesh_Offroad = new GameEntity(mesh);

        mesh = OBJLoader.loadMesh("/models/Car_Offroad_Axles.obj");
        mesh.setMaterial(material);
        GameEntity axle_Mesh_Offroad = new GameEntity(mesh);

        mesh = OBJLoader.loadMesh("/models/Wheel_Offroad.obj");
        mesh.setMaterial(material);
        GameEntity frontLeft_Mesh_Offroad = new GameEntity(mesh);
        GameEntity frontRight_Mesh_Offroad = new GameEntity(mesh);
        GameEntity rearLeft_Mesh_Offroad = new GameEntity(mesh);
        GameEntity rearRight_Mesh_Offroad = new GameEntity(mesh);
        GameEntity[] meshes_Offroad = new GameEntity[]{ car_Mesh_Offroad, axle_Mesh_Offroad, frontLeft_Mesh_Offroad, frontRight_Mesh_Offroad, rearLeft_Mesh_Offroad, rearRight_Mesh_Offroad };

        // create sport, axles and wheels
        mesh = OBJLoader.loadMesh("/models/Car_Sport.obj");
        mesh.setMaterial(material);
        GameEntity car_Mesh_Sport = new GameEntity(mesh);

        mesh = OBJLoader.loadMesh("/models/Car_Sport_Axles.obj");
        mesh.setMaterial(material);
        GameEntity axle_Mesh_Sport = new GameEntity(mesh);

        mesh = OBJLoader.loadMesh("/models/Wheel_Sport_Front.obj");
        mesh.setMaterial(material);
        GameEntity frontLeft_Mesh_Sport = new GameEntity(mesh);
        GameEntity frontRight_Mesh_Sport = new GameEntity(mesh);
        mesh = OBJLoader.loadMesh("/models/Wheel_Sport_Rear.obj");
        mesh.setMaterial(material);
        GameEntity rearLeft_Mesh_Sport = new GameEntity(mesh);
        GameEntity rearRight_Mesh_Sport = new GameEntity(mesh);
        GameEntity[] meshes_Sport = new GameEntity[]{ car_Mesh_Sport, axle_Mesh_Sport, frontLeft_Mesh_Sport, frontRight_Mesh_Sport, rearLeft_Mesh_Sport, rearRight_Mesh_Sport };

        // create effect meshes
        skidMeshes = new GameEntity[1000];
        mesh = DebugMeshes.buildQuad();
        mesh.setMaterial(Materials.WHITE);

        for(int i = 0; i < skidMeshes.length; i++)
        {
            skidMeshes[i] = new GameEntity(mesh);
            skidMeshes[i].setScale(0.5f, 1f, 0.5f);
            skidMeshes[i].getPosition().y = -1f;
        }

        // add objects to List of GameItems
        ArrayList<GameEntity> gameEntities = new ArrayList<>();
        gameEntities.add(ground);

        gameEntities.add(car_Mesh_Offroad);
        gameEntities.add(axle_Mesh_Offroad);
        gameEntities.add(frontLeft_Mesh_Offroad);
        gameEntities.add(frontRight_Mesh_Offroad);
        gameEntities.add(rearLeft_Mesh_Offroad);
        gameEntities.add(rearRight_Mesh_Offroad);

        gameEntities.add(car_Mesh_Sport);
        gameEntities.add(axle_Mesh_Sport);
        gameEntities.add(frontLeft_Mesh_Sport);
        gameEntities.add(frontRight_Mesh_Sport);
        gameEntities.add(rearLeft_Mesh_Sport);
        gameEntities.add(rearRight_Mesh_Sport);

        for(int i = 0; i < skidMeshes.length; i++)
        {
            gameEntities.add(skidMeshes[i]);
        }

        if(EngineOptions.DEBUG)
        {
            // use this section to create and add gameItem for debug purposes
        }

        // add objects to scene and cars
        scene.setGameItems(gameEntities);

        float[] torqueChart_Offroad = new float[] {0, 395, 435, 455, 480, 470, 395, 300};           //from 0 to 7000 rpm in 1000rpm steps
        Engine engine_Offroad = new Engine(800, 6400, torqueChart_Offroad);

        float[] gearRatios_Offroad = new float[] {-2.90f, 2.66f, 1.78f, 1.30f, 1.0f, 0.74f, 0.50f }; //index 0 = reverse gear
        DriveTrain driveTrain_Offroad = new DriveTrain(0.7f, 3.42f, gearRatios_Offroad);

        float[] torqueChart_Sport = new float[] {0, 250, 400, 445, 465, 500, 490, 460};             //from 0 to 7000 rpm in 1000rpm steps
        Engine engine_Sport = new Engine(800, 6900, torqueChart_Sport);

        float[] gearRatios_Sport = new float[] {-2.13f, 2.27f, 1.77f, 1.31f, 0.99f, 0.78f, 0.78f };   //index 0 = reverse gear
        DriveTrain driveTrain_Sport = new DriveTrain(0.7f, 4.09f, gearRatios_Sport);

        Car car_Offroad = new Car(0.41f, 2.75f, 30f, 3.2f, 1.57f, 1.57f,0.1f, 2000f, 0.43f, new Vector3f(0, 0, -2.5f), engine_Offroad, driveTrain_Offroad, meshes_Offroad);
        Car car_Sport = new Car(0.42f, 1.86f, 30f, 2.45f, 1.532f, 1.606f,-0.15f, 1490, 0.3f,new Vector3f(0,0, 2.5f), engine_Sport, driveTrain_Sport, meshes_Sport);
        cars = new Car[] {car_Offroad, car_Sport};

        for(int i = 0; i < cars.length; i++)
        {
            cars[i].update(0,0,0,0,0);
            cars[i].update(0,0,0,0,0);
        }
        activeCar = 0;
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
        // Camera controls
        float cameraSpeed = window.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? CAMERA_SPEED_FAST : CAMERA_SPEED;
        cameraIncrement.set(0, 0, 0);

        if (window.isKeyPressed(GLFW_KEY_W)) { cameraIncrement.z = -cameraSpeed; }
        else if (window.isKeyPressed(GLFW_KEY_S)) { cameraIncrement.z = cameraSpeed; }

        if (window.isKeyPressed(GLFW_KEY_A)) { cameraIncrement.x = -cameraSpeed; }
        else if (window.isKeyPressed(GLFW_KEY_D)) { cameraIncrement.x = cameraSpeed; }

        if (window.isKeyPressed(GLFW_KEY_Q)) { cameraIncrement.y = -cameraSpeed; }
        else if (window.isKeyPressed(GLFW_KEY_E)) { cameraIncrement.y = cameraSpeed; }

        if(KeyboardInput.isKeyReleased(GLFW_KEY_C))
        {
            cameraMode++;
            cameraMode %= 2;
            if(EngineOptions.DEBUG)
            {
                System.out.println("CameraMode: " + cameraMode);
            }
        }

        if(cameraMode == 1)
        {
            float distanceIncrease = 0.1f;
            if (window.isKeyPressed(GLFW_KEY_R))
            {
                if(followCameraDistance > MIN_CAMERA_DISTANCE)
                {
                    followCameraDistance -= distanceIncrease;
                }
            }
            else if (window.isKeyPressed(GLFW_KEY_F))
            {
                if(followCameraDistance < MAX_CAMERA_DISTANCE)
                {
                    followCameraDistance += distanceIncrease;
                }
            }
        }

        // Toggle active cars
        if(KeyboardInput.isKeyReleased(GLFW_KEY_V))
        {
            activeCar++;
            activeCar %= cars.length;
            if(EngineOptions.DEBUG)
            {
                System.out.println("Active Car: " + activeCar);
            }
        }

        // Light
        if(window.isKeyPressed(GLFW_KEY_1)) { directionalLightAngle += 1.0f; }
        else if(window.isKeyPressed(GLFW_KEY_2)) { directionalLightAngle -= 1.0f;}

        if(KeyboardInput.isKeyReleased(GLFW_KEY_ESCAPE))
        {
            glfwSetWindowShouldClose(window.getWindowHandle(), true);
        }

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

    @Override
    public void update(float interval, MouseInput mouseInput)
    {
        updateCamera(mouseInput, interval);
        hud.updateCompass(camera.getRotation().y);
        updateDirectionalLight();


        cars[activeCar].update(throttleInput, brakeInput, steeringInput, handbrakeInput, interval);

        Vector3f[] wheelPositions = cars[activeCar].wheelPositions;
        Vector3f carYRotation = cars[activeCar].getRotation();

        float skidMeshesHeight = 0.05f;
        GameEntity skidMesh;
        if(cars[activeCar].isFrontBlocking || cars[activeCar].isFrontSliding)
        {
            int index = skidMeshesIndexCounter %= skidMeshes.length;
            skidMesh = skidMeshes[index];
            skidMesh.setPosition(wheelPositions[0]);
            skidMesh.getPosition().y = skidMeshesHeight;
            skidMesh.setRotation(carYRotation);

            skidMeshesIndexCounter++;
            index = skidMeshesIndexCounter %= skidMeshes.length;
            skidMesh = skidMeshes[index];
            skidMesh.setPosition(wheelPositions[1]);
            skidMesh.getPosition().y = skidMeshesHeight;
            skidMesh.setRotation(carYRotation);
        }

        if(cars[activeCar].isRearBlocking || cars[activeCar].isRearSliding)
        {
            skidMeshesIndexCounter++;
            int index = skidMeshesIndexCounter %= skidMeshes.length;
            skidMesh = skidMeshes[index];
            skidMesh.setPosition(wheelPositions[2]);
            skidMesh.getPosition().y = skidMeshesHeight;
            skidMesh.setRotation(carYRotation);

            skidMeshesIndexCounter++;
            index = skidMeshesIndexCounter %= skidMeshes.length;
            skidMesh = skidMeshes[index];
            skidMesh.setPosition(wheelPositions[3]);
            skidMesh.getPosition().y = skidMeshesHeight;
            skidMesh.setRotation(carYRotation);

            skidMeshesIndexCounter++;
        }

        hud.setStatusText("Speed: " + Physics.convertMPStoKMH(cars[activeCar].speed) + "KM/H");

        if(EngineOptions.DEBUG)
        {
            totalUpdates++;
            hud.updateDebugHUD(cars[activeCar].frontCombinedForces, cars[activeCar].rearCombinedForces, cars[activeCar].maxFrontAxleForce, cars[activeCar].maxRearAxleForce);
        }
    }

    private void updateCamera(MouseInput mouseInput, float interval)
    {
        if (mouseInput.isRightButtonPressed())
        {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        if(cameraMode > 0)
        {
            Vector3f position = camera.getPosition();
            Vector3f carPosition = cars[activeCar].getPosition();
            position.x += (carPosition.x - position.x) * FOLLOW_CAMERA_LERP_SPEED * interval;
            position.z += (carPosition.z + followCameraDistance - position.z) * FOLLOW_CAMERA_LERP_SPEED * interval;
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
