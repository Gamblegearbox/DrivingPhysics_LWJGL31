package engine.physics;

/**
 * Created by Pete on 06/02/2017.
 */
public class Particle {

    private int timeToLive;
    private int liveTimeCounter;

    public float rotation;
    private float rotationSpeed;

    private float startSize;
    public float size;
    private float sizeIncrease;

    public float riseSpeed;
    public boolean isActive;

    public Particle(int timeToLive, float rotationSpeed, float startSize, float sizeIncrease, float riseSpeed)
    {
        this.timeToLive = timeToLive;
        this.rotationSpeed = rotationSpeed;
        this.sizeIncrease = sizeIncrease;
        this.riseSpeed = riseSpeed;
        liveTimeCounter = timeToLive;
        rotation = 0;
        this.startSize = startSize;
        size = startSize;
        isActive = true;
    }

    public void update()
    {
        size += sizeIncrease;
        rotation += rotationSpeed;
        liveTimeCounter--;
        if(liveTimeCounter <= 0)
        {
            isActive = false;
            liveTimeCounter = timeToLive;
            size = startSize;
        }

    }
}
