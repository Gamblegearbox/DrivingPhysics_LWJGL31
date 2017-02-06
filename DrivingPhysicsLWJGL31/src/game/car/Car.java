package game.car;

import engine.core.EngineOptions;
import engine.physics.Physics;
import game.environment.GroundType;
import game.environment.GroundTypes;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Car {

    private final float maxSteeringAngle;
    private final float wheelBase;
    private final float halfWheelBase;
    public final float halfTrackWidth;
    private final float mass;
    private final float maxEngineForce;
    private final float maxBrakeForce;
    public final float wheelRadius;
    private final float suspensionHeight;

    public float steeringAngle;
    private float carDirectionAngle;
    public float wheelSpinAngle;

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
    public Vector2f combinedForces;

    private GroundType currentGround;
    public float maxFrontAxleForce;
    public float maxRearAxleForce;
    public float weightShiftModifier;

    public float forwardAcceleration;
    public float forwardForce;
    public float speed;
    public float radialForce;
    public float turningRadius;

    public Car()
    {
        maxSteeringAngle = 20f;
        wheelBase = 3.15f;
        halfWheelBase = wheelBase / 2f;
        halfTrackWidth = 0.83f;

        wheelRadius = 0.43f;
        suspensionHeight = 0.1f;
        mass = 2000;
        weightShiftModifier = 0.0f;
        maxEngineForce = 12000;
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

    public void update(float throttleInput, float brakeInput, float steeringInput, int gear, float handbrake, float interval)
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
        maxFrontAxleForce = currentGround.getStaticFriction() * weightInNewton;
        maxRearAxleForce = currentGround.getStaticFriction() * weightInNewton;

        // apply weight shift modifier
        /*
        weightShiftModifier = (-throttleInput + brakeInput) / 3;
        float temp = maxFrontAxleForce * weightShiftModifier;
        maxFrontAxleForce += temp;
        maxRearAxleForce -= temp;
        */

        // use input to adjust steering and calculate force acceleration and speed in longitudinal direction
        steeringAngle = maxSteeringAngle * steeringInput;
        forwardForce = maxEngineForce * throttleInput;

        // calculate turning radius and radial force
        turningRadius = Physics.calcTurningRadius(wheelBase, steeringAngle);
        radialForce = Physics.calcRadialForce(mass, speed, turningRadius);

        // REAR AXLE FORWARD FORCE


        float rollFrictionForce = currentGround.getRollingFriction() * weightInNewton;
        if(speed > 0)
        {
            forwardForce -= rollFrictionForce;
        }

        if(brakeInput > 0)
        {
            if(speed > 0)
            {
                forwardForce -= maxBrakeForce * brakeInput;
            }
        }
        combinedForces = new Vector2f(forwardForce, -radialForce);

        forwardAcceleration = Physics.calcAcceleration(mass, forwardForce);
        speed += forwardAcceleration * interval;


        // calculate rotation in radians for upcoming rearWheelsForward vector calculation
        float degToRad = (float)Math.toRadians(carDirectionAngle);
        float degToRadInclSteering = (float)Math.toRadians(carDirectionAngle - steeringAngle);

        // calculate rearWheelsForward and rearWheelsLeft direction of the steered wheel
        frontWheelsForward.x = (float) Math.cos(degToRadInclSteering);
        frontWheelsForward.z = (float) Math.sin(degToRadInclSteering);
        frontWheelsLeft = new Vector3f(new Vector3f(carUp).cross(new Vector3f(frontWheelsForward)));

        // calculate rearWheelsForward and rearWheelsLeft direction of the car
        rearWheelsForward.x = (float) Math.cos(degToRad);
        rearWheelsForward.z = (float) Math.sin(degToRad);
        rearWheelsLeft = new Vector3f(new Vector3f(carUp).cross(new Vector3f(rearWheelsForward)));

        frontWheelsPosition = new Vector3f(position).add(new Vector3f(rearWheelsForward).mul(halfWheelBase));
        if(combinedForces.length() > maxFrontAxleForce)
        {
            frontWheelsPosition.add(new Vector3f(rearWheelsForward).mul(speed * interval));
        }
        else
        {
            frontWheelsPosition.add(new Vector3f(frontWheelsForward).mul(speed * interval));
        }

        rearWheelsPosition = new Vector3f(position).add(new Vector3f(rearWheelsForward).mul(-halfWheelBase));
        if(combinedForces.length() > maxRearAxleForce)
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

        // set wheel positions
        frontWheelsPosition.y = wheelRadius;
        rearWheelsPosition.y = wheelRadius;
        wheelPositions[0] = new Vector3f(frontWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(-halfTrackWidth));
        wheelPositions[1] = new Vector3f(frontWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(halfTrackWidth));
        wheelPositions[2] = new Vector3f(rearWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(-halfTrackWidth));
        wheelPositions[3] = new Vector3f(rearWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(halfTrackWidth));

        // PROTOTYPED STUFF
        // Fake dynamics and wheelSpin
        float maxWeightShiftFrontBack = 3;
        float maxWeightShiftLeftRight = 7;
        float weightShiftFrontBackAngle = maxWeightShiftFrontBack * forwardForce / maxEngineForce;
        float weightShiftLeftRightAngle = speed / 4;
        if(weightShiftLeftRightAngle > maxWeightShiftLeftRight)
        {
            weightShiftLeftRightAngle = maxWeightShiftLeftRight;
        }
        weightShiftLeftRightAngle *= steeringInput;
        wheelSpinAngle -= speed;

        // Final position and rotation fixes
        position.y = wheelRadius + suspensionHeight;
        rotation.set(weightShiftLeftRightAngle, -carDirectionAngle, weightShiftFrontBackAngle);
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


}
