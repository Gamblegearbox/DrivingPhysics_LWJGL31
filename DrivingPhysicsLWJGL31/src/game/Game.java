package game;

import engine.*;
import engine.Window;
import engine.graphics.*;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic{

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float MAX_STEERING_ANGLE = 35f;

    private final Renderer renderer;
    private final Camera camera;
    private final Vector3f cameraIncrement;

    private GameItem[] gameItems;
    private Hud hud;

    private SceneLight sceneLight;
    private float directionalLightAngle;

    private float wheelSpinSpeed = 0;
    private float currentSteeringAngle = 0;
    private float steeringSpeed = 1;
    private boolean isNight = false;


    public Game()
    {
        renderer = new Renderer();
        camera = new Camera();
        cameraIncrement = new Vector3f(0, 0, 0);
        directionalLightAngle = 0;
    }

    @Override
    public void init(Window window) throws Exception
    {
        renderer.init();
        camera.setPosition(0, 2, 10);
        setupGameItems();
        setupLight();
        setupHUD();
    }

    private void setupGameItems() throws Exception
    {
        Mesh mesh = OBJLoader.loadMesh("/models/CustomCorvetteC2_V2.obj");
        Material material = new Material(new Vector3f(0.5f, 0.5f, 0.5f), 1f);
        mesh.setMaterial(material);
        GameItem car = new GameItem(mesh);
        car.setPosition(0, 1, 0);
        car.setScale(0.3f);

        material = new Material(new Vector3f(0.1f, 0.1f, 0.1f), 0.5f);
        mesh = OBJLoader.loadMesh(("/models/Wheel_Sport_FRONT.obj"));
        mesh.setMaterial(material);
        GameItem wheelFrontLeft = new GameItem(mesh);
        GameItem wheelFrontRight = new GameItem(mesh);

        float frontAxlePos = 2.4f;
        float rearAxlePos = -1.8f;
        float wheelHeight = 0.6f;
        float trackWidth = 1.25f;
        wheelFrontLeft.setPosition(trackWidth, wheelHeight, frontAxlePos);
        wheelFrontLeft.setScale(0.3f);
        wheelFrontRight.setPosition(-trackWidth, wheelHeight, frontAxlePos);
        wheelFrontRight.setScale(0.3f);
        wheelFrontRight.setRotation(0, 0, 180);

        mesh = OBJLoader.loadMesh(("/models/Wheel_Sport_REAR.obj"));
        mesh.setMaterial(material);
        GameItem wheelRearLeft = new GameItem(mesh);
        GameItem wheelRearRight = new GameItem(mesh);
        wheelRearLeft.setPosition(trackWidth, wheelHeight, rearAxlePos);
        wheelRearLeft.setScale(0.3f);
        wheelRearRight.setPosition(-trackWidth, wheelHeight, rearAxlePos);
        wheelRearRight.setScale(0.3f);
        wheelRearRight.setRotation(0, 0, 180);

        material = new Material(new Vector3f(0.5f, 0.5f, 0.5f), 0f);
        mesh = OBJLoader.loadMesh("/models/GroundPlane.obj");
        mesh.setMaterial(material);
        GameItem ground = new GameItem(mesh);
        ground.setPosition(0, 0, 0);

        gameItems = new GameItem[]{ car, ground, wheelFrontLeft, wheelFrontRight, wheelRearLeft, wheelRearRight};
    }

    private void setupLight()
    {
        sceneLight = new SceneLight();
        sceneLight.setAmbientLight(new Vector3f(0.1f, 0.1f, 0.1f));
        sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(-1, 0, 0), 1f));

        // Point Light
        /*
        PointLight light = new PointLight(
                new Vector3f(0, 0, 1),
                new Vector3f(1, 2, 1.5f),
                1f
        );
        light.setAttenuation(new PointLight.Attenuation(0.0f, 0.0f, 1.f));
        sceneLight.setPointLightList(new PointLight[]{light});
        */
        // Spot Light
        /*
        light = new PointLight(
                new Vector3f(0, 1, 0),
                new Vector3f(5f, 5f, 1),
                1f
        );
        light.setAttenuation(new PointLight.Attenuation(1.0f, 1.0f, 0.2f));
        Vector3f coneDir = new Vector3f(0, -1f, 0);
        float cutoff = (float) Math.cos(Math.toRadians(40));
        SpotLight spotLight0 = new SpotLight(light, coneDir, cutoff);
        sceneLight.setSpotLightList(new SpotLight[]{spotLight0});
        */
    }

    private void setupHUD() throws Exception
    {
        hud = new Hud("Press PAGE_UP / PAGE_DOWN to move the directional light");
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

        if(window.isKeyPressed(GLFW_KEY_1)) { directionalLightAngle += 0.5f; }
        else if(window.isKeyPressed(GLFW_KEY_2)) { directionalLightAngle -= 0.5f;}


        if (window.isKeyPressed(GLFW_KEY_UP))
        {
            wheelSpinSpeed--;
        }
        else if (window.isKeyPressed(GLFW_KEY_DOWN))
        {
            wheelSpinSpeed++;
        }


        if (window.isKeyPressed(GLFW_KEY_LEFT))
        {
            if(currentSteeringAngle > -MAX_STEERING_ANGLE)
            {
                currentSteeringAngle -= steeringSpeed;
            }
        }
        else if (window.isKeyPressed(GLFW_KEY_RIGHT))
        {
            if(currentSteeringAngle < MAX_STEERING_ANGLE)
            {
                currentSteeringAngle += steeringSpeed;
            }
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput)
    {
        camera.movePosition(cameraIncrement.x * interval, cameraIncrement.y * interval, cameraIncrement.z * interval);

        if (mouseInput.isRightButtonPressed())
        {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);

            hud.rotateCompass(camera.getRotation().y);
        }

        gameItems[2].getRotation().y = currentSteeringAngle;
        gameItems[3].getRotation().y = currentSteeringAngle;
        gameItems[4].getRotation().x += wheelSpinSpeed;
        gameItems[5].getRotation().x += wheelSpinSpeed;

        updateLight();
    }

    private void updateLight()
    {
        DirectionalLight directionalLight = sceneLight.getDirectionalLight();
        float absDirectionalAngle = Math.abs(directionalLightAngle);

        if (absDirectionalAngle > 90)
        {
            isNight = !isNight;
            if(directionalLightAngle > 90)
            {
                directionalLightAngle = -89.9f;
            }
            else
            {
                directionalLightAngle = 89.9f;
            }
        }

        calcLightIntensity(absDirectionalAngle,directionalLight);
        calcLightColor(absDirectionalAngle, directionalLight);
        setSkyColor(absDirectionalAngle);

        double angRad = Math.toRadians(directionalLightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    private void calcLightIntensity(float absDirectionalAngle, DirectionalLight directionalLight)
    {
        if (absDirectionalAngle > 80f)
        {
            float currentIntensity = 1 - (absDirectionalAngle - 80f) / 10.0f;
            directionalLight.setIntensity(currentIntensity);
        }
    }

    private void calcLightColor(float absDirectionalAngle, DirectionalLight directionalLight)
    {
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

    private void setSkyColor(float absDirectionalAngle)
    {
        if(isNight)
        {
            renderer.setClearColor(0.0f, 0.0f, 0.0f, 1f);
        }
        else
        {
//          renderer.setClearColor(0.5f, 0.7f, 1f, 1f);
            renderer.setClearColor(0.0f, 0.0f, 0.0f, 1f);

        }
    }

    @Override
    public void render(Window window)
    {
        hud.updateSize(window);
        renderer.render(window, camera, gameItems, sceneLight, hud);
    }

    @Override
    public void cleanup()
    {
        renderer.cleanup();
        for (GameItem gameItem : gameItems)
        {
            gameItem.getMesh().cleanUp();
        }
        hud.cleanup();
    }


}
