package game.environment;


public class GroundType {

    private final String type;
    private final float rollingFriction;
    private final float slidingFriction;
    private final float staticFriction;

    public GroundType(String type, float rollingFriction, float slidingFriction, float staticFriction )
    {
        this.type = type;
        this.rollingFriction = rollingFriction;
        this.slidingFriction = slidingFriction;
        this.staticFriction = staticFriction;
    }

    public String getType()
    {
        return type;
    }

    public float getRollingFriction()
    {
        return rollingFriction;
    }

    public float getSlidingFriction()
    {
        return slidingFriction;
    }

    public float getStaticFriction()
    {
        return staticFriction;
    }
}
