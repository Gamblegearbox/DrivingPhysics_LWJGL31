package engine.gameEntities;

import engine.mesh.Mesh;
import engine.utils.Physics;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GameEntity {

    private Mesh mesh;
    private final Vector3f position;
    private final Quaternionf rotation;
    private float scale;

    public GameEntity()
    {
        position = new Vector3f(0, 0, 0);
        scale = 1;
        rotation = new Quaternionf();
    }

    public GameEntity(Mesh mesh)
    {
        this();
        this.mesh = mesh;
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public void setPosition(float x, float y, float z)
    {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public void setPosition(Vector3f position)
    {
        this.position.x = position.x;
        this.position.y = position.y;
        this.position.z = position.z;
    }

    public Quaternionf getRotation()
    {
        return rotation;
    }

    public void setRotation(float angle, Vector3f rotation)
    {
        this.rotation.setAngleAxis(angle, rotation.x, rotation.y, rotation.z);
    }

    public void setRotation(Vector3f rotation)
    {
        this.rotation.set(Physics.eulerToQuaternion(rotation.x, rotation.y, rotation.z));
    }

    public void setRotation(float xRot, float yRot, float zRot)
    {
        this.rotation.set(Physics.eulerToQuaternion(xRot, yRot, zRot));
    }

    public float getScale()
    {
        return scale;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
    }

    public Mesh getMesh()
    {
        return mesh;
    }

    public void setMesh(Mesh mesh)
    {
        this.mesh = mesh;
    }

}
