package engine.physics;


import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Physics
{
    public static final float G = 9.81f;

    public static float calcAcceleration(float mass, float force)
    {
        return force / mass;
    }

    public static float calcForce(float mass, float acceleration)
    {
        return mass * acceleration;
    }

    public static float calcTurningRadius(float wheelbase, float steeringAngle)
    {
        float magicOffset = steeringAngle / 50;
        return (wheelbase /(float)Math.sin(Math.toRadians(steeringAngle))) - magicOffset;
    }

    public static float calcRadialForce(float mass, float speed, float turningRadius)
    {
        return mass * ((speed * speed) / turningRadius);
    }

    public static float calcWeight(float mass)
    {
        return mass * G;
    }



    public static float convertMPStoKMH(float metersPerSecond)
    {
        return metersPerSecond * 3.6f;
    }

    public static Quaternionf convertEulerToQuaternion(float xRot, float yRot, float zRot) {
        xRot = (float)Math.toRadians(xRot);
        yRot = (float)Math.toRadians(yRot);
        zRot = (float)Math.toRadians(zRot);

        double yawOver2 = xRot * 0.5f;
        float cosYawOver2 = (float)Math.cos(yawOver2);
        float sinYawOver2 = (float)Math.sin(yawOver2);
        double pitchOver2 = yRot * 0.5f;
        float cosPitchOver2 = (float)Math.cos(pitchOver2);
        float sinPitchOver2 = (float)Math.sin(pitchOver2);
        double rollOver2 = zRot * 0.5f;
        float cosRollOver2 = (float)Math.cos(rollOver2);
        float sinRollOver2 = (float)Math.sin(rollOver2);
        Quaternionf result = new Quaternionf();
        result.w = cosYawOver2 * cosPitchOver2 * cosRollOver2 + sinYawOver2 * sinPitchOver2 * sinRollOver2;
        result.x = sinYawOver2 * cosPitchOver2 * cosRollOver2 + cosYawOver2 * sinPitchOver2 * sinRollOver2;
        result.y = cosYawOver2 * sinPitchOver2 * cosRollOver2 - sinYawOver2 * cosPitchOver2 * sinRollOver2;
        result.z = cosYawOver2 * cosPitchOver2 * sinRollOver2 - sinYawOver2 * sinPitchOver2 * cosRollOver2;

        return result;
    }
}
