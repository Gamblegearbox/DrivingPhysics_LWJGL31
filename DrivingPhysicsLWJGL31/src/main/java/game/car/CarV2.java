package game.car;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class CarV2 {

    private final Engine engine;
    private final DriveTrain driveTrain;

    public Vector3f position = new Vector3f(0,0,0);
    public Vector3f rotation = new Vector3f(0,0,0);

    private Vector2f direction = new Vector2f(0,0);
    private Vector2f velocity = new Vector2f(0,0);

    public float speed = 0;
    private float cDrag;
    private float cRoll;
    private float mass = 1500;
    private float wheelRadius = 0.34f;
    private float brakeTorque = 20000;
    private float wheelBase = 3.15f;
    private float steeringAngle;
    private float max_steeringAngle = 25f;

    public CarV2()
    {
        float[] torqueChart = new float[] {0, 395, 435, 455, 480, 470, 395, 300};           //from 0 to 7000 rpm in 1000rpm steps
        engine = new Engine(800, 6400, torqueChart);
        float[] gearRatios = new float[] {2.90f, 2.66f, 1.78f, 1.30f, 1.0f, 0.74f, 0.50f }; //index 0 = reverse gear
        driveTrain = new DriveTrain(0.7f, 3.42f, gearRatios);

        float cw = 0.30f;
        float frontArea = 2.2f;
        float airDensity = 1.29f;
        cDrag = 0.5f * cw * frontArea * airDensity;
        cRoll = 30 * cDrag; // that is an approximation, also contains mechanical resistance;
    }

    public void update(float throttleInput, float brakeInput, float steeringInput, float handbrake, float interval)
    {
        steeringAngle = steeringInput * max_steeringAngle;
        speed = velocity.length();
        float maxTorque = getDrivingForce(2500, driveTrain.getGear(speed));
        float fTraction;
        if(brakeInput > 0)
        {
            fTraction = -brakeTorque * brakeInput;
        }
        else
        {
            fTraction = maxTorque * throttleInput;
        }

        float fDrag = -cDrag * speed * speed;
        float fRoll = -cRoll * speed;
        float fLong = fTraction + fDrag + fRoll;
        float accell = fLong / mass;
        speed = speed + accell *interval;

        float turnRadius = wheelBase / (float)Math.sin(Math.toRadians(steeringAngle));
        float angularVelo = speed / turnRadius;
        rotation.y += angularVelo;
        direction.x = (float) Math.cos(Math.toRadians(rotation.y));
        direction.y = (float) -Math.sin(Math.toRadians(rotation.y));

        velocity = new Vector2f(direction).mul(speed);

        updatePosition(interval);
    }

    private void updatePosition(float interval)
    {
        position.x = position.x + velocity.x * interval;
        position.z = position.z + velocity.y * interval;
    }

    private float getDrivingForce(float rpm, int gear)
    {
        if(rpm < engine.idleRpm)
        {
            rpm = engine.idleRpm;
        }
        if(rpm > engine.maxRpm)
        {
            rpm = engine.maxRpm;
        }
        int index = (int)(rpm / 1000);

        // interpolate torque
        float torque_1 = engine.torqueChart[index];
        float torque_2 = engine.torqueChart[index + 1];
        float rpm_1 = (index) * 1000;
        float rpm_2 = (index + 1) * 1000;

        float torque = torque_1 + (((rpm - rpm_1) / (rpm_2 - rpm_1)) * (torque_2 - torque_1));
        return torque * driveTrain.gearRatios[gear] * driveTrain.diffRatio * driveTrain.transmissionEfficiency / wheelRadius;
    }



}
