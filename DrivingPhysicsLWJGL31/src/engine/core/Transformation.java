package engine.core;

import engine.camera.Camera;
import engine.gameEntities.GameEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

    private final Matrix4f projectionMatrix;
    private final Matrix4f modelMatrix;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f modelLightMatrix;
    private final Matrix4f modelLightViewMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f lightViewMatrix;
    private final Matrix4f orthoProjMatrix;
    private final Matrix4f ortho2DMatrix;
    private final Matrix4f orthoModelMatrix;


    public Transformation()
    {
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();
        modelLightMatrix = new Matrix4f();
        modelLightViewMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        orthoProjMatrix = new Matrix4f();
        ortho2DMatrix = new Matrix4f();
        orthoModelMatrix = new Matrix4f();
        lightViewMatrix = new Matrix4f();
    }

    public Matrix4f getProjectionMatrix()
    {
        return projectionMatrix;
    }

    public Matrix4f updateProjectionMatrix(float fov, float width, float height, float zNear, float zFar)
    {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);

        return projectionMatrix;
    }

    public final Matrix4f getOrthoProjectionMatrix()
    {
        return orthoProjMatrix;
    }

    public Matrix4f updateOrthoProjectionMatrix(float left, float right, float bottom, float top, float zNear, float zFar)
    {
        orthoProjMatrix.identity();
        orthoProjMatrix.setOrtho(left, right, bottom, top, zNear, zFar);

        return orthoProjMatrix;
    }

    public Matrix4f getViewMatrix()
    {
        return viewMatrix;
    }

    public Matrix4f updateViewMatrix(Camera camera)
    {
        return updateGenericViewMatrix(camera.getPosition(), camera.getRotation(), viewMatrix);
    }

    public Matrix4f getLightViewMatrix()
    {
        return lightViewMatrix;
    }

    public void setLightViewMatrix(Matrix4f lightViewMatrix)
    {
        this.lightViewMatrix.set(lightViewMatrix);
    }

    public Matrix4f updateLightViewMatrix(Vector3f position, Vector3f rotation)
    {
        return updateGenericViewMatrix(position, rotation, lightViewMatrix);
    }

    private Matrix4f updateGenericViewMatrix(Vector3f position, Vector3f rotation, Matrix4f matrix)
    {
        matrix.identity();
        // First do the rotation so camera rotates over its position
        matrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                .rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
        // Then do the translation
        matrix.translate(-position.x, -position.y, -position.z);

        return matrix;
    }

    public final Matrix4f getOrtho2DProjectionMatrix(float left, float right, float bottom, float top)
    {
        ortho2DMatrix.identity();
        ortho2DMatrix.setOrtho2D(left, right, bottom, top);

        return ortho2DMatrix;
    }

    public Matrix4f buildModelViewMatrix(GameEntity gameEntity, Matrix4f matrix)
    {
        Vector3f rotation = gameEntity.getRotation();
        modelMatrix.identity().translate(gameEntity.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameEntity.getScale());
        modelViewMatrix.set(matrix);

        return modelViewMatrix.mul(modelMatrix);
    }

    public Matrix4f buildModelLightViewMatrix(GameEntity gameEntity, Matrix4f matrix)
    {
        Vector3f rotation = gameEntity.getRotation();
        modelLightMatrix.identity().translate(gameEntity.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameEntity.getScale());
        modelLightViewMatrix.set(matrix);

        return modelLightViewMatrix.mul(modelLightMatrix);
    }

    public Matrix4f buildOrtoProjModelMatrix(GameEntity gameEntity, Matrix4f orthoMatrix)
    {
        Vector3f rotation = gameEntity.getRotation();
        modelMatrix.identity().translate(gameEntity.getPosition()).
                rotateX((float) Math.toRadians(-rotation.x)).
                rotateY((float) Math.toRadians(-rotation.y)).
                rotateZ((float) Math.toRadians(-rotation.z)).
                scale(gameEntity.getScale());
        orthoModelMatrix.set(orthoMatrix);
        orthoModelMatrix.mul(modelMatrix);

        return orthoModelMatrix;
    }
}