package game;

import engine.gameEntities.GameEntity;
import engine.interfaces.IHud;
import engine.gameEntities.TextEntity;
import engine.core.Window;
import engine.shading.Material;
import engine.mesh.Mesh;
import engine.utils.OBJLoader;
import org.joml.Vector3f;


public class Hud implements IHud {

    private static final int FONT_COLS = 16;
    private static final int FONT_ROWS = 16;

    private static final String FONT_TEXTURE = "/textures/Font_Menlo.png";
    private final GameEntity[] gameEntities;
    private final TextEntity statusTextItem;
    private final GameEntity compassItem;

    public Hud(String statusText) throws Exception
    {
        this.statusTextItem = new TextEntity(statusText, FONT_TEXTURE, FONT_COLS, FONT_ROWS);
        this.statusTextItem.getMesh().getMaterial().setColor(new Vector3f(1, 1, 1));
        this.statusTextItem.setScale(0.5f);

        // Create compass
        Mesh mesh = OBJLoader.loadMesh("/models/CompassNeedle.obj");
        Material material = new Material();
        material.setColor(new Vector3f(1, 0, 0));
        mesh.setMaterial(material);
        compassItem = new GameEntity(mesh);
        compassItem.setScale(10.0f);
        // Rotate to transform it to screen coordinates
        compassItem.setRotation(0f, 0f, 180f);

        // Create list that holds the items that compose the HUD
        gameEntities = new GameEntity[]{statusTextItem, compassItem};
    }

    public void setStatusText(String statusText)
    {
        this.statusTextItem.setText(statusText);
    }

    public void rotateCompass(float angle)
    {
        this.compassItem.setRotation(0, 0, 180 + angle);
    }

    //@Override
    public GameEntity[] getGameEntities()
    {
        return gameEntities;
    }

    public void updateSize(Window window)
    {
        this.statusTextItem.setPosition(10f, window.getHeight() - 50f, 0);
        this.compassItem.setPosition(window.getWidth() -40f, 50f, 0);
    }
}
