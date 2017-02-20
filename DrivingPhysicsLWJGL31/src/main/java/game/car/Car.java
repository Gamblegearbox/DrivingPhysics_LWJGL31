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
    //public final float airResistance_Cw;
    //public final float airResistance_k;

    public float steeringAngle;
    private float carDirectionAngle;
    public float frontWheelSpinAngle;
    public float rearWheelSpinAngle;

    public Vector3f position;
    private Vector3f oldForward;
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

    public float forwardAcceleration;
    public float forwardForce;
    public float frontForwardForce;
    public float rearForwardForce;
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
        oldForward = new Vector3f();
        frontWheelsForward = new Vector3f();
        frontWheelsLeft = new Vector3f();
        rearWheelsForward = new Vector3f();
        rearWheelsLeft = new Vector3f();
        carUp = new Vector3f(0,1,0);
        rotation = new Vector3f(0,0,0);
        wheelPositions = new Vector3f[4];
        currentGround = getGroundType();
    }
    int frontSlipCounter = 0;
    int rearSlipCounter = 0;

    public void update(float throttleInput, float isBrakeInput, float steeringInput, int gear, float handbrake, float interval)
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

        // use input to adjust steering and calculate force acceleration and speed in longitudinal direction
        steeringAngle = maxSteeringAngle * steeringInput;

        // calculate turning radius and radial force
        turningRadius = Physics.calcTurningRadius(wheelBase, steeringAngle);
        radialForce = Physics.calcRadialForce(mass, speed, turningRadius);

        boolean breaking = isBrakeInput > 0 ? true : false;
        isFrontBlocking = false;
        isRearBlocking = false;
        float rollFrictionForce = currentGround.getRollingFriction() * weightInNewton;
        float slideFrictionForce = currentGround.getSlidingFriction() * weightInNewton;

        // CALC FRONT AXLE FORWARD FORCE
        frontForwardForce = (maxEngineForce * throttleInput) / 2f;
        float tempForce;
        if(breaking)
        {
            tempForce = (maxBrakeForce * isBrakeInput) /2f;
            if(tempForce > maxFrontAxleForce)
            {
                isFrontBlocking = true;
                tempForce = slideFrictionForce / 2f;
            }
        }
        else
        {
            tempForce = rollFrictionForce / 2f;
        }
        frontForwardForce -= tempForce;

        // CALC REAR AXLE FORWARD FORCE
        rearForwardForce = (maxEngineForce * throttleInput) / 2f;
        if(breaking)
        {
            tempForce = (maxBrakeForce * isBrakeInput) / 2f;
            if(tempForce > maxRearAxleForce)
            {
                isRearBlocking = true;
                tempForce = slideFrictionForce / 2f;
            }
        }
        else
        {
            tempForce = rollFrictionForce / 2f;
        }
        rearForwardForce -= tempForce;

        frontCombinedForces = new Vector2f(frontForwardForce, radialForce / 2f);
        rearCombinedForces = new Vector2f(rearForwardForce, radialForce / 2f);
        forwardForce = frontForwardForce + rearForwardForce;

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
            frontWheelsPosition.add(new Vector3f(rearWheelsForward).mul(speed * interval));
        }
        else if(isFrontSliding)
        {
            if(frontSlipCounter > 1)
            {
                frontSlipCounter = 0;
                frontWheelsPosition.add(new Vector3f(frontWheelsForward).mul(speed * interval));
            }
            else
            {
                frontWheelsPosition.add(new Vector3f(rearWheelsForward).mul(speed * interval));
            }
            frontSlipCounter++;
        }
        else
        {
            frontWheelsPosition.add(new Vector3f(frontWheelsForward).mul(speed * interval));
        }

        rearWheelsPosition = new Vector3f(position).add(new Vector3f(rearWheelsForward).mul(-halfWheelBase));
        rearWheelsPosition.add(new Vector3f(rearWheelsForward).mul(speed * interval));


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

        // BEGIN PROTOTYPED STUFF
        // Fake dynamics and wheelSpin
        /*
        float maxWeightShiftFrontBack = 3;
        float maxWeightShiftLeftRight = 7;
        float weightShiftFrontBackAngle = maxWeightShiftFrontBack * forwardForce / maxEngineForce;
        float weightShiftLeftRightAngle = speed / 4;
        if(weightShiftLeftRightAngle > maxWeightShiftLeftRight)
        {
            weightShiftLeftRightAngle = maxWeightShiftLeftRight;
        }
        weightShiftLeftRightAngle *= steeringInput;
        */

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
        //END PROTOTYPED STUFF

        // Final position and rotation fixes
        position.y = wheelRadius + suspensionHeight;
        rotation.set(/*weightShiftLeftRightAngle*/0, -carDirectionAngle, 0/*weightShiftFrontBackAngle*/);
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
