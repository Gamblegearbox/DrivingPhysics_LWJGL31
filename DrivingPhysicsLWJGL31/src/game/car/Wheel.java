package game.car;

import org.joml.Math;
import org.joml.Vector3f;

public class Wheel {

    private float radius;
    private float diameter;
    private float circumference;

    public Wheel(float radius)
    {
        this.radius = radius;
        calcWheelData();
    }

    public void setRadius(float radius)
    {
        this.radius = radius;
        calcWheelData();
    }

    public float getCircumference()
    {
        return circumference;
    }

    public float getDiameter()
    {
        return diameter;
    }

    public float getRadius()
    {
        return radius;
    }

    private void calcWheelData()
    {
        circumference = (float)(2.0f * Math.PI * radius);
        diameter = radius *2.0f;
    }

}
