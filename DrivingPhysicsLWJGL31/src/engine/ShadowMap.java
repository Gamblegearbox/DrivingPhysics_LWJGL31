package engine;


import engine.texture.Texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShadowMap {

    public static final int SHADOW_MAP_WIDTH = 2024;
    public static final int SHADOW_MAP_HEIGHT = 2024;

    private final int depthMapFBO;
    private final Texture depthMap;

    public ShadowMap() throws Exception
    {
        depthMapFBO = glGenFramebuffers();
        depthMap = new Texture(SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, GL_DEPTH_COMPONENT);

        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap.getId(), 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
        {
            throw new Exception("could not create Framebuffer");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public Texture getDepthMapTexture()
    {
        return depthMap;
    }

    public int getDepthMapFBO()
    {
        return depthMapFBO;
    }

    public void cleanup()
    {
        glDeleteFramebuffers(depthMapFBO);
        depthMap.cleanup();
    }
}
