package game.car;

import engine.core.EngineOptions;
import engine.physics.Physics;
import game.environment.GroundType;
import game.environment.GroundTypes;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Car {

    private final float max_steeringAngle;
    private final float wheelBase;
    private final float halfWheelBase;
    public final float halfTrackWidth;
    private final float mass;
    private final float maxBrakeForce;
    public final float wheelRadius;
    private final float suspensionHeight;

    public float steeringAngle;
    private float carDirectionAngle;
    public float frontWheelSpinAngle;
    public float rearWheelSpinAngle;

    public Vector3f position;
    public Vector3f frontWheelsPosition;
    public Vector3f rearWheelsPosition;
    public Vector3f frontWheelsForward;
    public Vector3f frontWheelsLeft;
    public Vector3f rearWheelsForward;
    public Vector3f rearWheelsLeft;
    private Vector3f carUp;
    public Vector3f rotation;
    public Vector3f[] wheelPositions;
    public Vector2f frontCombinedForces;
    public Vector2f rearCombinedForces;

    private GroundType currentGround;
    public float maxFrontAxleForce;
    public float maxRearAxleForce;
    public float weightShiftModifier;

    public boolean isFrontBlocking;
    public boolean isRearBlocking;
    public boolean isFrontSliding;
    public boolean isRearSliding;

    private float cDrag;
    private float cRoll;
    public float forwardAcceleration;
    public float forwardForce;
    public float frontForwardForce;
    public float rearForwardForce;
    public float speed;
    public int gear;
    public float radialForce;

    private Engine engine;
    private DriveTrain driveTrain;

    public Car()
    {
        float[] torqueChart = new float[] {0, 395, 435, 455, 480, 470, 395, 300};           //from 0 to 7000 rpm in 1000rpm steps
        engine = new Engine(800, 6400, torqueChart);
        float[] gearRatios = new float[] {2.90f, 2.66f, 1.78f, 1.30f, 1.0f, 0.74f, 0.50f }; //index 0 = reverse gear
        driveTrain = new DriveTrain(0.7f, 3.42f, gearRatios);

        float cw = 0.41f;
        float frontArea = 2.75f;
        float airDensity = 1.29f;
        cDrag = 0.5f * cw * frontArea * airDensity;

        max_steeringAngle = 20f;
        wheelBase = 3.2f;
        halfWheelBase = wheelBase / 2f;
        halfTrackWidth = 0.785f;

        wheelRadius = 0.43f;
        suspensionHeight = 0.1f;
        mass = 2000;
        weightShiftModifier = 0.0f;
        maxBrakeForce = 24000;
        position = new Vector3f();
        frontWheelsForward = new Vector3f();
        frontWheelsLeft = new Vector3f();
        rearWheelsForward = new Vector3f();
        rearWheelsLeft = new Vector3f();
        carUp = new Vector3f(0,1,0);
        rotation = new Vector3f(0,0,0);
        wheelPositions = new Vector3f[4];
        currentGround = getGroundType();
    }

    public void update(float throttleInput, float brakeInput, float steeringInput, float handbrake, float interval)
    {
        //6 * interval IS ONE REV PER MIN ON THE WHEEL!! (6 degrees per second * 60(360 per minute) * interval)
        // get the current ground surface to calculate friction and stuff
        if(currentGround != getGroundType())
        {
            currentGround = getGroundType();
            if(EngineOptions.DEBUG)
            {
                System.out.println(currentGround.getType());
                System.out.println("C_ROLL: " + currentGround.getRollingFriction());
                System.out.println("U_SLIDE: " + currentGround.getSlidingFriction());
                System.out.println("U_STATIC: " + currentGround.getStaticFriction());
                System.out.println();
            }
        }

        // calculate maximum force the axles can put on the current ground
        float weightInNewton = Physics.calcWeight(mass);
        maxFrontAxleForce = currentGround.getStaticFriction() * (weightInNewton / 2f);
        maxRearAxleForce = currentGround.getStaticFriction() * (weightInNewton / 2f);

        // apply weight shift modifier
        /*
        weightShiftModifier = (-throttleInput + brakeInput) / 2;
        float temp = maxFrontAxleForce * weightShiftModifier;
        maxFrontAxleForce += temp;
        maxRearAxleForce -= temp;
        */

        if(gear != driveTrain.getGear(speed))
        {
            gear = driveTrain.getGear(speed);
            System.out.println(gear);
        }
        float maxTorque = getDrivingForce(2500, gear);

        steeringAngle = steeringInput * max_steeringAngle;
        float turningRadius = wheelBase / (float)Math.sin(Math.toRadians(steeringAngle));
        radialForce = Physics.calcRadialForce(mass, speed, turningRadius);

        boolean breaking = brakeInput > 0 ? true : false;
        isFrontBlocking = false;
        isRearBlocking = false;
        float rollFrictionForce = currentGround.getRollingFriction() * weightInNewton;
        float slideFrictionForce = currentGround.getSlidingFriction() * weightInNewton;

        // CALC FRONT AXLE FORWARD FORCE
        frontForwardForce = (maxTorque * throttleInput) * 0.5f;
        float tempForce;
        if(breaking)
        {
            tempForce = (maxBrakeForce * brakeInput) * 0.5f;
            if(tempForce > maxFrontAxleForce)
            {
                isFrontBlocking = true;
                tempForce = slideFrictionForce * 0.5f;
            }
        }
        else
        {
            tempForce = rollFrictionForce * 0.5f;
        }
        frontForwardForce -= tempForce;

        // CALC REAR AXLE FORWARD FORCE
        rearForwardForce = (maxTorque * throttleInput) * 0.5f;
        if(breaking)
        {
            tempForce = (maxBrakeForce * brakeInput) * 0.5f;
            if(tempForce > maxRearAxleForce)
            {
                isRearBlocking = true;
                tempForce = slideFrictionForce * 0.5f;
            }
        }
        else
        {
            tempForce = rollFrictionForce * 0.5f;
        }
        rearForwardForce -= tempForce;

        frontCombinedForces = new Vector2f(frontForwardForce, radialForce * 0.5f);
        rearCombinedForces = new Vector2f(rearForwardForce, radialForce * 0.5f);
        float fDrag = -cDrag * speed * speed;
        forwardForce = frontForwardForce + rearForwardForce + fDrag;

        forwardAcceleration = Physics.calcAcceleration(mass, forwardForce);
        speed += forwardAcceleration * interval;

        if(speed < 0 )
        {
            speed = 0;
        }

        // calculate rotation in radians for upcoming Wheel forward vector calculation
        float degToRad = (float)Math.toRadians(carDirectionAngle);
        float degToRadInclSteering = (float)Math.toRadians(carDirectionAngle - steeringAngle);

        // calculate frontWheelsForward and frontWheelsLeft direction of the steered wheel
        frontWheelsForward.x = (float) Math.cos(degToRadInclSteering);
        frontWheelsForward.z = (float) Math.sin(degToRadInclSteering);
        frontWheelsLeft = new Vector3f(new Vector3f(carUp).cross(new Vector3f(frontWheelsForward)));

        // calculate rearWheelsForward and rearWheelsLeft direction of the car
        rearWheelsForward.x = (float) Math.cos(degToRad);
        rearWheelsForward.z = (float) Math.sin(degToRad);
        rearWheelsLeft = new Vector3f(new Vector3f(carUp).cross(new Vector3f(rearWheelsForward)));

        isFrontSliding = frontCombinedForces.length() > maxFrontAxleForce ? true : false;
        isRearSliding = rearCombinedForces.length() > maxRearAxleForce ? true : false;

        frontWheelsPosition = new Vector3f(position).add(new Vector3f(rearWheelsForward).mul(halfWheelBase));
        if(isFrontBlocking)
        {
            frontWheelsPosition.add(new Vector3f(frontWheelsForward).mul(speed * interval));
        }
        else if(isFrontSliding)
        {
            frontWheelsPosition.add(new Vector3f(frontWheelsForward).mul(speed * interval));
        }
        else
        {
            frontWheelsPosition.add(new Vector3f(frontWheelsForward).mul(speed * interval));
        }

        rearWheelsPosition = new Vector3f(position).add(new Vector3f(rearWheelsForward).mul(-halfWheelBase));
        if(isRearBlocking)
        {
            rearWheelsPosition.add(new Vector3f(rearWheelsForward).mul(speed * interval));
        }
        else if(isRearSliding)
        {
            rearWheelsPosition.add(new Vector3f(rearWheelsForward).mul(speed * interval));
        }
        else
        {
            rearWheelsPosition.add(new Vector3f(rearWheelsForward).mul(speed * interval));
        }

        Vector3f tempV = new Vector3f(frontWheelsPosition).add(rearWheelsPosition);
        tempV.div(2f);
        position = new Vector3f(tempV);

        // set car angles
        float newCarAngleInRad = (float)Math.atan2(frontWheelsPosition.z - rearWheelsPosition.z, frontWheelsPosition.x - rearWheelsPosition.x);
        carDirectionAngle = (float) Math.toDegrees(newCarAngleInRad);
        carDirectionAngle %= 360f;

        calcWheelPositions();
        setWheelSpinAngle();
        applyPositionAndFixFakeDynamics(throttleInput, brakeInput, steeringInput);
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

    private GroundType getGroundType()
    {
        float temp = position.x;
        GroundType result;

        if(temp <= -10){ result = GroundTypes.ROAD; }
        else if(temp > -10 && temp <= 10) {result = GroundTypes.SAND_HARD; }
        else if(temp > 10 && temp <= 30) {result = GroundTypes.SAND_SOFT; }
        else {result = GroundTypes.SNOW; }

        return GroundTypes.ROAD;
    }

    private void calcWheelPositions()
    {
        frontWheelsPosition.y = wheelRadius;
        rearWheelsPosition.y = wheelRadius;
        wheelPositions[0] = new Vector3f(frontWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(-halfTrackWidth));
        wheelPositions[1] = new Vector3f(frontWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(halfTrackWidth));
        wheelPositions[2] = new Vector3f(rearWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(-halfTrackWidth));
        wheelPositions[3] = new Vector3f(rearWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(halfTrackWidth));
    }

    private void setWheelSpinAngle()
    {
        if(isFrontBlocking)
        {
            frontWheelSpinAngle -= 0;
        }
        else
        {
            frontWheelSpinAngle -= speed;
        }

        if(isRearBlocking)
        {
            rearWheelSpinAngle -= 0;
        }
        else
        {
            rearWheelSpinAngle -= speed;
        }
    }

    private void applyPositionAndFixFakeDynamics( float throttleInput, float isBrakeInput, float steeringInput)
    {
        float maxWeightShiftFrontBack = 3;
        float maxWeightShiftLeftRight = 5;
        float weightShiftFrontBackAngle = throttleInput * maxWeightShiftFrontBack - isBrakeInput * maxWeightShiftFrontBack;
        float weightShiftLeftRightAngle = speed / 4;
        if(weightShiftLeftRightAngle > maxWeightShiftLeftRight)
        {
            weightShiftLeftRightAngle = maxWeightShiftLeftRight;
        }
        weightShiftLeftRightAngle *= steeringInput;

        position.y = wheelRadius + suspensionHeight;
        rotation.set(weightShiftLeftRightAngle, -carDirectionAngle, weightShiftFrontBackAngle);
    }

}
