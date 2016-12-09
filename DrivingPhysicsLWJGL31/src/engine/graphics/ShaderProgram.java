package engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    private final int programId;
    private final Map<String, Integer> uniforms;

    private int vertexShaderId;
    private int fragmentShaderId;

    public ShaderProgram() throws Exception
    {
        programId = glCreateProgram();

        if(programId == 0)
        {
            throw new Exception("Could not create Shader program");
        }

        uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) throws Exception
    {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if(uniformLocation < 0)
        {
            throw new Exception("Could not find uniform: " + uniformName);
        }

        uniforms.put(uniformName, uniformLocation);
    }

    public void createPointLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".intensity");
        createUniform(uniformName + ".att.constant");
        createUniform(uniformName + ".att.linear");
        createUniform(uniformName + ".att.exponent");
    }

    public void createPointLightListUniform(String uniformName, int size) throws Exception
    {
        for(int i = 0; i < size; i++)
        {
            createPointLightUniform(uniformName + "[" + i + "]");
        }
    }

    public void createSpotLightUniform(String uniformName) throws Exception
    {
        createPointLightUniform(uniformName  + ".pl");
        createUniform(uniformName + ".conedir");
        createUniform(uniformName + ".cutoff");
    }

    public void createSpotLightListUniform(String uniformName, int size) throws Exception
    {
        for(int i = 0; i < size; i++)
        {
            createSpotLightUniform(uniformName + "[" + i + "]");
        }
    }

    public void createDirectionalLightUniform(String uniformName) throws Exception
    {
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".intensity");
    }

    public void createMaterialUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".useColor");
        createUniform(uniformName + ".reflectance");
    }

    public void setUniform(String uniformName, Matrix4f value)
    {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
        value.get(floatBuffer);
        glUniformMatrix4fv(uniforms.get(uniformName), false, floatBuffer);
    }

    public void setUniform(String uniformName, int value)
    {
        glUniform1i(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, float value)
    {
        glUniform1f(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, Vector3f value)
    {
        glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, PointLight pointLight)
    {
        setUniform(uniformName + ".color", pointLight.getColor() );
        setUniform(uniformName + ".position", pointLight.getPosition());
        setUniform(uniformName + ".intensity", pointLight.getIntensity());
        PointLight.Attenuation att = pointLight.getAttenuation();
        setUniform(uniformName + ".att.constant", att.getConstant());
        setUniform(uniformName + ".att.linear", att.getLinear());
        setUniform(uniformName + ".att.exponent", att.getExponent());
    }

    public void setUniform(String uniformName, PointLight[] pointLights)
    {
        int numLights = pointLights != null ? pointLights.length : 0;
        for (int i = 0; i < numLights; i++)
        {
            setUniform(uniformName, pointLights[i], i);
        }
    }

    public void setUniform(String uniformName, PointLight pointLight, int pos)
    {
        setUniform(uniformName + "[" + pos + "]", pointLight);
    }

    public void setUniform(String uniformName, SpotLight spotLight)
    {
        setUniform(uniformName + ".pl", spotLight.getPointLight() );
        setUniform(uniformName + ".conedir", spotLight.getConeDirection());
        setUniform(uniformName + ".cutoff", spotLight.getCutOff());
    }

    public void setUniform(String uniformName, SpotLight[] spotLights)
    {
        int numLights = spotLights != null ? spotLights.length : 0;
        for (int i = 0; i < numLights; i++)
        {
            setUniform(uniformName, spotLights[i], i);
        }
    }

    public void setUniform(String uniformName, SpotLight spotLight, int pos)
    {
        setUniform(uniformName + "[" + pos + "]", spotLight);
    }

    public void setUniform(String uniformName, DirectionalLight dirLight)
    {
        setUniform(uniformName + ".color", dirLight.getColor() );
        setUniform(uniformName + ".direction", dirLight.getDirection());
        setUniform(uniformName + ".intensity", dirLight.getIntensity());
    }

    public void setUniform(String uniformName, Material material)
    {
        setUniform(uniformName + ".color", material.getColor() );
        setUniform(uniformName + ".useColor", material.isTextured() ? 0 : 1);
        setUniform(uniformName + ".reflectance", material.getReflectance());
    }

    public void createVertexShader(String shaderCode) throws Exception
    {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception
    {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception
    {
        int shaderId = glCreateShader(shaderType);
        if(shaderId == 0)
        {
            throw new Exception("Error creating shader code: " + shaderId);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0)
        {
            throw new Exception("Error compiling shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void link() throws Exception
    {
        glLinkProgram(programId);
        if(glGetProgrami(programId, GL_LINK_STATUS) == 0)
        {
            throw new Exception("Error linking shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        //Seems to have no use here?!
        /*glValidateProgram(programId);
        if(glGetProgrami(programId, GL_VALIDATE_STATUS) == 0)
        {
            System.err.println("Warning validating shader code: " + glGetProgramInfoLog(programId, 1024));
        }*/

    }

    public void bind()
    {
        glUseProgram(programId);
    }

    public void unbind()
    {
        glUseProgram(0);
    }

    public void cleanup()
    {
        unbind();
        if(programId != 0)
        {
            if(vertexShaderId != 0)
            {
                glDetachShader(programId, vertexShaderId);
            }
            if(fragmentShaderId != 0)
            {
                glDetachShader(programId, fragmentShaderId);
            }
            glDeleteProgram(programId);
        }
    }

}
