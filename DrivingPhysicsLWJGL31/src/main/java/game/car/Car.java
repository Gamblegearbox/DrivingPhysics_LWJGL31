package game.car;

import engine.core.EngineOptions;
import engine.gameEntities.GameEntity;
import engine.physics.Physics;
import game.environment.GroundType;
import game.environment.GroundTypes;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Car {

    private final GameEntity[] gameEntities;
    private final float max_steeringAngle;
    private final float wheelBase;
    private final float halfWheelBase;
    private final float halfTrackWidthFront;
    private final float halfTrackWidthRear;
    private final float mass;
    private final float maxBrakeForce;
    private final float wheelRadius;
    private final float suspensionOffset;
    private final float wheelDiameter;
    private final Engine engine;
    private final DriveTrain driveTrain;

    private float carDirectionAngle;
    private float frontWheelSpinAngle;
    private float rearWheelSpinAngle;
    private float lateralForce;

    private Vector3f frontWheelsPosition;
    private Vector3f rearWheelsPosition;
    private Vector3f frontWheelsForward;
    private Vector3f frontWheelsLeft;
    private Vector3f rearWheelsForward;
    private Vector3f rearWheelsLeft;
    private Vector3f carUp;
    private Vector3f rotation;
    private Vector3f frontSlideDirection;
    private Vector3f rearSlideDirection;
    private GroundType currentGround;
    private float weightShiftModifier;

    private Vector3f position;
    public Vector2f frontCombinedForces;
    public Vector2f rearCombinedForces;
    public float maxFrontAxleForce;
    public float maxRearAxleForce;
    public Vector3f[] wheelPositions;
    public boolean isFrontBlocking;
    public boolean isRearBlocking;
    public boolean isFrontSliding;
    public boolean isRearSliding;

    private float cDrag;
    private float forwardAcceleration;
    private float forwardForce;
    private float frontForwardForce;
    private float rearForwardForce;
    public float speed;
    private float slideSpeed;
    private int gear;



    public Car(float cw, float frontArea, float max_steeringAngle, float wheelBase,
               float trackWidthFront, float trackwidthRear, float suspensionOffset,
               float mass, float wheelRadius, Vector3f position, Engine engine,
               DriveTrain driveTrain, GameEntity[] gameEntities)
    {
        this.gameEntities = gameEntities;
        this.engine = engine;
        this.driveTrain = driveTrain;

        float airDensity = 1.29f;
        cDrag = 0.5f * cw * frontArea * airDensity;

        this.max_steeringAngle = max_steeringAngle;
        this.wheelBase = wheelBase;
        halfWheelBase = wheelBase / 2f;

        halfTrackWidthFront = trackWidthFront / 2f;
        halfTrackWidthRear = trackwidthRear / 2f;

        this.wheelRadius = wheelRadius;
        wheelDiameter = wheelRadius * 2f;

        this.suspensionOffset = suspensionOffset;
        this.mass = mass;
        weightShiftModifier = 0.0f;
        maxBrakeForce = 24000;
        this.position = new Vector3f(position);
        frontWheelsForward = new Vector3f();
        frontWheelsLeft = new Vector3f();
        rearWheelsForward = new Vector3f();
        rearWheelsLeft = new Vector3f();
        carUp = new Vector3f(0,1,0);
        rotation = new Vector3f(0,0,0);
        frontSlideDirection = null;
        rearSlideDirection = null;
        isFrontSliding = false;
        isRearSliding = false;
        wheelPositions = new Vector3f[4];
        currentGround = getGroundType();
    }

    public void update(float throttleInput, float brakeInput, float steeringInput, float handbrake, float interval)
    {
        float steeringAngle;
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
        float weightInNewton = mass * Physics.G;
        maxFrontAxleForce = currentGround.getStaticFriction() * (weightInNewton * 0.5f);
        maxRearAxleForce = currentGround.getStaticFriction() * (weightInNewton * 0.5f);
        float rollFrictionForce = currentGround.getRollingFriction() * weightInNewton;
        float slideFrictionForce = currentGround.getSlidingFriction() * weightInNewton;

        // weight shift modifier
        /*
        weightShiftModifier = (-throttleInput + brakeInput) * 0.5f;
        float temp = maxFrontAxleForce * weightShiftModifier;
        maxFrontAxleForce += temp;
        maxRearAxleForce -= temp;
        */

        gear = driveTrain.getGear(speed);
        float maxTorque = getDrivingForce(5000, gear);

        boolean breaking = brakeInput > 0 ? true : false;
        isFrontBlocking = false;
        isRearBlocking = false;

        // CALC FRONT AXLE FORWARD FORCE
        frontForwardForce = (maxTorque * throttleInput) * 0.5f;
        float tempForce;
        if(breaking && !isFrontSliding)
        {
            tempForce = (maxBrakeForce * brakeInput) * 0.5f;
            if(tempForce > maxFrontAxleForce)
            {
                isFrontBlocking = true;
                tempForce = slideFrictionForce * 0.5f;
            }
        }
        else if(isFrontSliding)
        {
            tempForce = slideFrictionForce * 0.5f;
        }
        else
        {
            tempForce = rollFrictionForce * 0.5f;
        }
        frontForwardForce -= tempForce;

        // CALC REAR AXLE FORWARD FORCE
        rearForwardForce = (maxTorque * throttleInput) * 0.5f;
        if(breaking && !isRearSliding)
        {
            tempForce = (maxBrakeForce * brakeInput) * 0.5f;
            if(tempForce > maxRearAxleForce)
            {
                isRearBlocking = true;

                tempForce = slideFrictionForce * 0.5f;
            }
        }
        else if(isRearSliding)
        {
            tempForce = slideFrictionForce * 0.5f;
        }
        else
        {
            tempForce = rollFrictionForce * 0.5f;
        }
        rearForwardForce -= tempForce;

        float fDrag = -cDrag * speed * speed;
        forwardForce = frontForwardForce + rearForwardForce + fDrag;

        forwardAcceleration = forwardForce / mass;
        speed += forwardAcceleration * interval;

        if(isFrontSliding || isRearSliding)
        {
            float slideDrag = -cDrag * slideSpeed * slideSpeed;
            float slideForce = -slideFrictionForce + slideDrag;
            float slideAcceleration = slideForce / mass;
            slideSpeed += slideAcceleration * interval;
        }

        if(speed < 0 )
        {
            speed = 0;
        }

        if(slideSpeed < 0)
        {
            slideSpeed = 0;
            isFrontSliding = false;
            isRearSliding = false;
            isFrontBlocking = false;
            isRearBlocking = false;
            frontSlideDirection = null;
            rearSlideDirection = null;
        }

        if(speed < 0.00001f)
        {
            steeringAngle = steeringInput * max_steeringAngle;
        }
        else
        {
            steeringAngle = (1511.5069f * (float) java.lang.Math.pow(speed, -2.0173f)) * currentGround.getStaticFriction() * steeringInput;
            if(steeringAngle > max_steeringAngle)
            {
                steeringAngle = max_steeringAngle;
            }
            if(steeringAngle < -max_steeringAngle)
            {
                steeringAngle = -max_steeringAngle;
            }
        }

        float turnRadius = wheelBase / (float)Math.sin(Math.toRadians(steeringAngle));
        float lateralAcceleration = (speed * speed) / turnRadius;
        lateralForce = mass * lateralAcceleration;

        frontCombinedForces = new Vector2f(frontForwardForce, -lateralForce / 2f);
        rearCombinedForces = new Vector2f(rearForwardForce, -lateralForce / 2f);

        float carRotationDegToRad = (float)Math.toRadians(carDirectionAngle);
        float carRotationInclSteeringDegToRad = (float)Math.toRadians(carDirectionAngle - steeringAngle);

        frontWheelsForward.x = (float) Math.cos(carRotationInclSteeringDegToRad);
        frontWheelsForward.z = (float) Math.sin(carRotationInclSteeringDegToRad);
        frontWheelsLeft = new Vector3f(new Vector3f(carUp).cross(new Vector3f(frontWheelsForward)));
        frontWheelsPosition = new Vector3f(position).add(new Vector3f(rearWheelsForward).mul(halfWheelBase));
        if(isFrontBlocking)
        {
            if(frontSlideDirection == null)
            {
                isFrontSliding = true;
                slideSpeed = speed;
                speed = 0;
                frontSlideDirection = new Vector3f(rearWheelsForward).add(new Vector3f(rearWheelsLeft).mul(steeringInput * 0.5f));
                frontSlideDirection.normalize();
            }

            frontWheelsPosition.add(new Vector3f(frontSlideDirection).mul(slideSpeed * interval));
        }
        else if(isFrontSliding)
        {
            frontWheelsPosition.add(new Vector3f(frontSlideDirection).mul(slideSpeed * interval));
            frontWheelsPosition.add(new Vector3f(frontWheelsForward).mul(speed * interval));
        }
        else
        {
            frontWheelsPosition.add(new Vector3f(frontWheelsForward).mul(speed * interval));
        }

        rearWheelsForward.x = (float) Math.cos(carRotationDegToRad);
        rearWheelsForward.z = (float) Math.sin(carRotationDegToRad);
        rearWheelsLeft = new Vector3f(new Vector3f(carUp).cross(new Vector3f(rearWheelsForward)));
        rearWheelsPosition = new Vector3f(position).add(new Vector3f(rearWheelsForward).mul(-halfWheelBase));
        if(isRearBlocking)
        {
            if(rearSlideDirection == null)
            {
                isRearSliding = true;
                rearSlideDirection = new Vector3f(rearWheelsForward);
                rearSlideDirection.normalize();
            }

            rearWheelsPosition.add(new Vector3f(rearSlideDirection).mul(slideSpeed * interval));
        }
        else if(isRearSliding)
        {
            rearWheelsPosition.add(new Vector3f(rearSlideDirection).mul(slideSpeed * interval));
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
        setWheelSpinAngle(interval);
        applyPositionAndFixFakeDynamics(throttleInput, brakeInput, steeringInput);

        updateVisuals(steeringAngle);
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

        System.out.println(torque_1 + " / " + torque_2);

        float torque = torque_1 + (((rpm - rpm_1) / (rpm_2 - rpm_1)) * (torque_2 - torque_1));

        return torque * driveTrain.gearRatios[gear] * driveTrain.diffRatio * driveTrain.driveTrainEfficiency / wheelRadius;
    }

    private GroundType getGroundType()
    {
        float xPosition = position.x;
        GroundType result;

        if(xPosition <= -250){ result = GroundTypes.ICE; }
        else if(xPosition > -250 && xPosition <= 25) {result = GroundTypes.ROAD; }
        else if(xPosition > 25 && xPosition <= 250) {result = GroundTypes.SAND_HARD; }
        else {result = GroundTypes.SAND_SOFT; }

        return result;
    }

    private void calcWheelPositions()
    {
        frontWheelsPosition.y = wheelRadius;
        rearWheelsPosition.y = wheelRadius;
        wheelPositions[0] = new Vector3f(frontWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(halfTrackWidthFront));
        wheelPositions[1] = new Vector3f(frontWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(-halfTrackWidthFront));
        wheelPositions[2] = new Vector3f(rearWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(halfTrackWidthRear));
        wheelPositions[3] = new Vector3f(rearWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(-halfTrackWidthRear));
    }

    private void setWheelSpinAngle(float interval)
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
        float maxWeightShiftFrontBack = 3 * mass * 0.001f * 0.5f;
        float maxWeightShiftLeftRight = 4 * mass * 0.001f * 0.5f;
        float weightShiftFrontBackAngle = throttleInput * maxWeightShiftFrontBack - isBrakeInput * maxWeightShiftFrontBack;
        float weightShiftLeftRightAngle = speed / 4;
        if(weightShiftLeftRightAngle > maxWeightShiftLeftRight)
        {
            weightShiftLeftRightAngle = maxWeightShiftLeftRight;
        }
        weightShiftLeftRightAngle *= steeringInput;

        position.y = wheelRadius + suspensionOffset;
        rotation.set(weightShiftLeftRightAngle, -carDirectionAngle, weightShiftFrontBackAngle);
    }

    private void updateVisuals(float steeringAngle)
    {
        //index: 0 = car_body; 1 = car_Axles; 2 = front left wheel; 3 = front right wheel; 4 = rear left wheel; 5 = rear right wheel;

        GameEntity carPart = gameEntities[0];
        carPart.setPosition(position);
        carPart.setRotation(rotation);

        carPart = gameEntities[1];
        carPart.setPosition(position);
        carPart.getPosition().y = wheelRadius;
        carPart.setRotation(0, rotation.y, 0);

        carPart = gameEntities[2];
        carPart.setPosition(wheelPositions[0]);
        carPart.setRotation(0, rotation.y + steeringAngle, frontWheelSpinAngle);
        carPart.setScale(wheelDiameter);

        carPart = gameEntities[3];
        carPart.setPosition(wheelPositions[1]);
        carPart.setRotation(0, rotation.y + 180 + steeringAngle, -frontWheelSpinAngle);
        carPart.setScale(wheelDiameter);

        carPart = gameEntities[4];
        carPart.setPosition(wheelPositions[2]);
        carPart.setRotation(0, rotation.y, rearWheelSpinAngle);
        carPart.setScale(wheelDiameter);

        carPart = gameEntities[5];
        carPart.setPosition(wheelPositions[3]);
        carPart.setRotation(0, rotation.y + 180, -rearWheelSpinAngle);
        carPart.setScale(wheelDiameter);
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public Vector3f getRotation()
    {
        return rotation;
    }
}
