package game.car;

import engine.core.EngineOptions;
import engine.gameEntities.GameEntity;
import engine.physics.Physics;
import game.environment.GroundType;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Car {

    private final GameEntity[] gameEntities;
    private final float maxSteeringAngle;
    private final float wheelBase;
    private final float halfWheelBase;
    private final float halfTrackWidth;
    private final float mass;
    private final float maxBrakeForce;
    private final float wheelRadius;
    private final float wheelDiameter;
    private final float suspensionOffset;
    private final Engine engine;
    private final DriveTrain driveTrain;
    private final boolean isFrontAxlePowered;
    private final boolean isRearAxlePowered;

    private float carDirectionAngle;
    private float frontWheelSpinAngle;
    private float rearWheelSpinAngle;

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
    private GameEntity[] currentSkidMeshes;
    private int skidMeshesIndexCounter = 0;

    private Vector3f position;
    public Vector2f frontCombinedForces;
    public Vector2f rearCombinedForces;
    public float maxFrontAxleForce;
    public float maxRearAxleForce;
    private Vector3f[] wheelPositions;
    private boolean isFrontBlocking;
    private boolean isRearBlocking;
    private boolean isFrontSliding;
    private boolean isRearSliding;
    private boolean isFrontSpinning;
    private boolean isRearSpinning;

    private float cDrag;
    private float speed;
    private float slideSpeed;
    private float weightInNewton;
    private float rollFrictionForce;
    private float slideFrictionForce;


    public Car(float cw, float frontArea, float maxSteeringAngle, float wheelBase,
               float trackWidth, float suspensionOffset,
               float mass, float wheelRadius, Vector3f position, Engine engine,
               DriveTrain driveTrain, GameEntity[] gameEntities, boolean isFrontAxlePowered, boolean isRearAxlePowered)
    {
        float airDensity = 1.23f;
        cDrag = 0.5f * cw * frontArea * airDensity;

        this.maxSteeringAngle = maxSteeringAngle;

        this.wheelBase = wheelBase;
        halfWheelBase = wheelBase / 2f;

        halfTrackWidth = trackWidth / 2f;

        this.wheelRadius = wheelRadius;
        wheelDiameter = wheelRadius * 2f;

        this.suspensionOffset = suspensionOffset;

        this.mass = mass;
        weightInNewton = mass * Physics.G;

        this.position = new Vector3f(position);
        this.gameEntities = gameEntities;
        this.engine = engine;
        this.driveTrain = driveTrain;

        this.isFrontAxlePowered = isFrontAxlePowered;
        this.isRearAxlePowered = isRearAxlePowered;

        maxBrakeForce = mass * 10;
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
    }

    public void update(float throttleInput, float brakeInput, float steeringInput, float handbrake, float interval)
    {
        int gear = driveTrain.getGear(speed);
        float maxTorque = getDrivingForce(4400, gear);

        boolean breaking = brakeInput > 0;
        isFrontBlocking = false;
        isRearBlocking = false;

        /*
        // weight shift modifier
        maxFrontAxleForce = currentGround.getStaticFriction() * (weightInNewton * 0.5f);
        maxRearAxleForce = currentGround.getStaticFriction() * (weightInNewton * 0.5f);
        float weightShiftModifier = (-throttleInput + brakeInput) * 0.5f;
        float temp = maxFrontAxleForce * weightShiftModifier;
        maxFrontAxleForce += temp;
        maxRearAxleForce -= temp;
        */

        // CALC AXLE FORWARD FORCES
        isFrontSpinning = false;
        isRearSpinning = false;
        float frontForwardForce = 0;
        float rearForwardForce = 0;
        float tempForce;
        float motorForce = (maxTorque * throttleInput);
        if(isFrontAxlePowered && isRearAxlePowered)
        {
            if(motorForce > maxFrontAxleForce + maxRearAxleForce)
            {
                isFrontSpinning = true;
                isRearSpinning = true;
                frontForwardForce = slideFrictionForce  * 0.5f;
                rearForwardForce = slideFrictionForce * 0.5f;
            }
            else
            {
                frontForwardForce = motorForce * 0.5f;
                rearForwardForce = motorForce * 0.5f;
            }
        }
        else if(isFrontAxlePowered)
        {
            if(motorForce > maxFrontAxleForce)
            {
                isFrontSpinning = true;
                frontForwardForce = slideFrictionForce * 0.5f;
            }
            else
            {
                frontForwardForce = (maxTorque * throttleInput);
            }
        }
        else if(isRearAxlePowered)
        {
            if(motorForce > maxRearAxleForce)
            {
                isRearSpinning = true;
                rearForwardForce = slideFrictionForce * 0.5f;
            }
            else
            {
                rearForwardForce = (maxTorque * throttleInput);
            }
        }

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
        float forwardForce = frontForwardForce + rearForwardForce + fDrag;

        float forwardAcceleration = forwardForce / mass;
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

        /*
        //speed sensitive steering adjustment
        float steeringAngle;
        if(speed < 0.00001f || slideSpeed > 0.00001f)
        {
            steeringAngle = steeringInput * maxSteeringAngle;
        }
        else
        {
            steeringAngle = (1511.5069f * (float) java.lang.Math.pow(speed, -2.0173f)) * currentGround.getStaticFriction() * steeringInput;
            if(steeringAngle > maxSteeringAngle)
            {
                steeringAngle = maxSteeringAngle;
            }
            if(steeringAngle < -maxSteeringAngle)
            {
                steeringAngle = -maxSteeringAngle;
            }
        }
        */
        //speed sensitive steering adjustment
        float lateralForce = 0;
        float turnRadius = 0;
        float steeringAngle = steeringInput * maxSteeringAngle;
        if(steeringInput != 0)
        {
            do
            {
                if (steeringInput > 0)
                {
                    steeringAngle -= 0.1f;
                }
                else
                {
                    steeringAngle += 0.1f;
                }
                turnRadius = wheelBase / (float) Math.sin(Math.toRadians(steeringAngle));
                float lateralAcceleration = (speed * speed) / turnRadius;
                lateralForce = mass * lateralAcceleration;
            }
            while (Math.abs(lateralForce) * 0.5f > maxFrontAxleForce);
        }

        frontCombinedForces = new Vector2f(frontForwardForce, -lateralForce * 0.5f);
        rearCombinedForces = new Vector2f(rearForwardForce, -lateralForce * 0.5f);

        float gierRate;
        if(speed < 0.0001)
        {
            gierRate = 0.0f;
        }
        else
        {
            gierRate = speed / turnRadius;
        }

        float carRotationDegToRad = (float)Math.toRadians(carDirectionAngle);
        float carRotationInclSteeringDegToRad = (float)Math.toRadians(carDirectionAngle - steeringAngle);

        frontWheelsForward.x = (float) Math.cos(carRotationInclSteeringDegToRad);
        frontWheelsForward.z = (float) Math.sin(carRotationInclSteeringDegToRad);
        frontWheelsLeft = new Vector3f(new Vector3f(carUp).cross(new Vector3f(frontWheelsForward)));
        frontWheelsPosition = new Vector3f(position).add(new Vector3f(rearWheelsForward).mul(halfWheelBase));

        if(isFrontBlocking && steeringInput != 0)
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
        if(isRearBlocking && steeringInput != 0)
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
        applyPositionRotationAndFakeDynamics(throttleInput, brakeInput, steeringInput);
        updateVisuals(steeringAngle);
        updateGroundEffects();
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

        return torque * driveTrain.gearRatios[gear] * driveTrain.diffRatio * driveTrain.driveTrainEfficiency / wheelRadius;
    }

    public void setGroundType(GroundType groundType)
    {
        // get the current ground surface to calculate friction forces
        if(currentGround != groundType)
        {
            currentGround = groundType;
            currentSkidMeshes = groundType.getSkidMeshes();

            // calculate friction forces
            maxFrontAxleForce = currentGround.getStaticFriction() * (weightInNewton * 0.5f);
            maxRearAxleForce = currentGround.getStaticFriction() * (weightInNewton * 0.5f);
            rollFrictionForce = currentGround.getRollingFriction() * weightInNewton;
            slideFrictionForce = currentGround.getSlidingFriction() * weightInNewton;

            if(EngineOptions.DEBUG)
            {
                System.out.println();
                System.out.println("Current ground type: " + currentGround.getType());
                System.out.println("C_ROLL: " + currentGround.getRollingFriction());
                System.out.println("U_SLIDE: " + currentGround.getSlidingFriction());
                System.out.println("U_STATIC: " + currentGround.getStaticFriction());
                System.out.println();
            }
        }
    }

    private void calcWheelPositions()
    {
        frontWheelsPosition.y = wheelRadius;
        rearWheelsPosition.y = wheelRadius;
        wheelPositions[0] = new Vector3f(frontWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(halfTrackWidth));
        wheelPositions[1] = new Vector3f(frontWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(-halfTrackWidth));
        wheelPositions[2] = new Vector3f(rearWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(halfTrackWidth));
        wheelPositions[3] = new Vector3f(rearWheelsPosition).add(new Vector3f(rearWheelsLeft).mul(-halfTrackWidth));
    }

    private void setWheelSpinAngle(float interval)
    {
        float circumference = 2.0f * (float)Math.PI * wheelRadius;
        float rotationAngle = speed / circumference * 360 * interval;
        float fakeSpinAngle = rotationAngle * 5;

        if(isFrontBlocking)
        {
            frontWheelSpinAngle -= 0;
        }
        else if(isFrontSpinning)
        {
            frontWheelSpinAngle -= fakeSpinAngle;
        }
        else
        {
            frontWheelSpinAngle -= rotationAngle;
        }

        if(isRearBlocking)
        {
            rearWheelSpinAngle -= 0;
        }
        else if(isRearSpinning)
        {
            rearWheelSpinAngle -= fakeSpinAngle;
        }
        else
        {
            rearWheelSpinAngle -= rotationAngle;
        }
    }

    private void applyPositionRotationAndFakeDynamics(float throttleInput, float isBrakeInput, float steeringInput)
    {
        float maxWeightShiftFrontBack = 2 * mass * 0.001f * 0.5f;
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

    private void updateGroundEffects()
    {
        float skidMeshesHeight = 0.05f;
        GameEntity skidMesh;
        if(isFrontBlocking || isFrontSliding || isFrontSpinning)
        {
            int index = skidMeshesIndexCounter %= currentSkidMeshes.length;
            skidMesh = currentSkidMeshes[index];
            skidMesh.setPosition(wheelPositions[0]);
            skidMesh.getPosition().y = skidMeshesHeight;
            skidMesh.setRotation(rotation);

            skidMeshesIndexCounter++;
            index = skidMeshesIndexCounter %= currentSkidMeshes.length;
            skidMesh = currentSkidMeshes[index];
            skidMesh.setPosition(wheelPositions[1]);
            skidMesh.getPosition().y = skidMeshesHeight;
            skidMesh.setRotation(rotation);
        }

        if(isRearBlocking || isRearSliding || isRearSpinning)
        {
            skidMeshesIndexCounter++;
            int index = skidMeshesIndexCounter %= currentSkidMeshes.length;
            skidMesh = currentSkidMeshes[index];
            skidMesh.setPosition(wheelPositions[2]);
            skidMesh.getPosition().y = skidMeshesHeight;
            skidMesh.setRotation(rotation);

            skidMeshesIndexCounter++;
            index = skidMeshesIndexCounter %= currentSkidMeshes.length;
            skidMesh = currentSkidMeshes[index];
            skidMesh.setPosition(wheelPositions[3]);
            skidMesh.getPosition().y = skidMeshesHeight;
            skidMesh.setRotation(rotation);

            skidMeshesIndexCounter++;
        }
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public float getSpeed()
    {
        return speed;
    }

}

