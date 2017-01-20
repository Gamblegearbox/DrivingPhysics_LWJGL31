package engine;


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

    public static float metersPerSecondToKilometersPerHour(float metersPerSecond)
    {
        return metersPerSecond * 3.6f;
    }


}
