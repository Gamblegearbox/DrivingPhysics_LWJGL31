package engine.car;

public class Car {

    public final float engineMaxRpm = 6500;
    public final float engineIdleRpm = 750;
    public final float maxSteeringAngle = 35f;

    private float currentSteeringAngle;
    private float currentEngineRpm;
    private boolean isEngineRunning;

    public Car()
    {
        currentSteeringAngle = 0;
        currentEngineRpm = 0;
        isEngineRunning = false;
    }

    public void update(float throttleInput, float brakeInput, float steeringInput, int gear, float handbrake)
    {
        if(Math.abs(currentSteeringAngle) <= maxSteeringAngle)
        {
            currentSteeringAngle = maxSteeringAngle * steeringInput;
        }
    }

    public void startEngine()
    {
        isEngineRunning = true;
        currentEngineRpm = engineIdleRpm;
    }

    public void stopEngine()
    {
        isEngineRunning = false;
        currentEngineRpm = 0;
    }

    private void raiseRpm()
    {
        if(isEngineRunning && currentEngineRpm < engineMaxRpm)
        {
            currentEngineRpm += 1;
        }
    }

    private void lowerRpm()
    {
        if(isEngineRunning && currentEngineRpm > engineIdleRpm)
        {
            currentEngineRpm -= 1;
        }
    }

    public float getCurrentSteeringAngle()
    {
        return currentSteeringAngle;
    }

    public void setCurrentSteeringAngle(float steeringAngle)
    {
        this.currentSteeringAngle = steeringAngle;
    }

    public float getCurrentEngineRpm()
    {
        return currentEngineRpm;
    }

    public float calcCurrentEngineTorque()
    {
        //todo: find a good formula for a torpque/rpm curve
        return currentEngineRpm / 10;
    }

    public boolean isEngineRunning()
    {
        return isEngineRunning;
    }



}
