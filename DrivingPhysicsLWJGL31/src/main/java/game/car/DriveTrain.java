package game.car;

import engine.core.EngineOptions;

public class DriveTrain {

    public final float transmissionEfficiency;
    public final float diffRatio;
    public final float[] gearRatios;

    public DriveTrain(float transmissionEfficiency, float diffRatio, float[] gearRatios)
    {
        this.transmissionEfficiency = transmissionEfficiency;
        this.diffRatio = diffRatio;
        this.gearRatios = gearRatios;
    }


    /**
     * @description
     * Return a suitable gear for a speed
     * The 6.gear is usually an overdrive to save fuel.
     * It outputs not enough torque to accelerate the car any further
     * (at the point of automated shifting)
     * This simple automatic gearbox will always drop back to 5th gear
     * after shifting into the 6th because it simply delivers more torque
     * @param speed
     * @return gear
     */
    public int getGear(float speed)
    {
        int gear;
        if(speed < 24){ gear = 1; }
        else if(speed < 33){ gear = 2; }
        else if(speed < 46){ gear = 3; }
        else if(speed < 54){ gear = 4; }
        else if(speed < 58){ gear = 5; }
        else { gear = 6; }
        return gear;
    }
}
