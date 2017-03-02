package game.car;


public class DriveTrain {

    public final float driveTrainEfficiency;
    public final float diffRatio;
    public final float[] gearRatios;

    public DriveTrain(float driveTrainEfficiency, float diffRatio, float[] gearRatios)
    {
        this.driveTrainEfficiency = driveTrainEfficiency;
        this.diffRatio = diffRatio;
        this.gearRatios = gearRatios;
    }

    /**
     * @description
     * Return a suitable gear for a speed
     * According to the cars aerodynamics and the motor rpm it might not stay in higher gears.
     * For example the if the 4th gear still delivers enough force to accelerate the car beyond
     * the shifting speed (in this case 54m/s) it might not output enough torque after shifting
     * into 5th gear.
     * As a result the speed drops below 54m/s, the gearbox shifts back into 4th gear, accelerates,
     * shifts up into 5th gear, and the circle repeats. (this marks the top speed for this vehicle at that rpm)
     *
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
