package game.environment;


import engine.gameEntities.GameEntity;

public class GroundType {

    private final String type;
    private final float rollingFriction;
    private final float slidingFriction;
    private final float staticFriction;
    private final GameEntity[] skidMeshes;

    public GroundType(String type, float rollingFriction, float slidingFriction, float staticFriction, GameEntity[] skidMeshes)
    {
        this.type = type;
        this.rollingFriction = rollingFriction;
        this.slidingFriction = slidingFriction;
        this.staticFriction = staticFriction;
        this.skidMeshes = skidMeshes;
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

    public GameEntity[] getSkidMeshes()
    {
        return skidMeshes;
    }

}
