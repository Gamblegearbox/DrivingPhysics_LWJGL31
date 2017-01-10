package game.car;

public class Engine {

    private final float maxRpm;
    private final float idleRpm;
    private final float rpmIncrement;

    private float currentRpm;
    private boolean isRunning;

    public Engine(float maxRpm, float idleRpm, float rpmIncrement)
    {
        currentRpm = 0;
        isRunning = false;
        this.maxRpm = maxRpm;
        this.idleRpm = idleRpm;
        this.rpmIncrement = rpmIncrement;
    }


    public void startEngine()
    {
        isRunning = true;
        currentRpm = idleRpm;
    }

    public void stopEngine()
    {
        isRunning =false;
        currentRpm = 0;
    }

    public boolean isRunning()
    {
        return isRunning;
    }


}
