package game.car;

public class Engine {

    public final float idleRpm;
    public final float maxRpm;
    public final float[] torqueChart;

    public Engine(float idleRpm, float maxRpm, float[] torqueChart)
    {
        this.idleRpm = idleRpm;
        this.maxRpm = maxRpm;
        this.torqueChart = torqueChart;
    }

}
