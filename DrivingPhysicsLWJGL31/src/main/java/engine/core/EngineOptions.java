package engine.core;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION;


public class EngineOptions {

    public static final String OPERATING_SYSTEM = System.getProperty("os.name");;
    public static final String TITLE = "GAME";

    public static final int WINDOW_WIDTH = 1042;
    public static final int WINDOW_HEIGHT = 768;

    public static final int TARGET_FPS = 60;
    public static final int TARGET_UPS = 75;

    public static final boolean DEBUG = true;
    public static final boolean ANTIALIASING = true;
    public static final boolean V_SYNC = true;

    public static boolean SHOW_TRIANGLES = false;
    public static boolean CULLFACE = true;

    public static void printInfo()
    {
        printGeneralInfo();
        printResolutionAndTimerSettings();
        printOptionStatus();
    }

    private static void printGeneralInfo()
    {
        System.out.println();
        System.out.println("OPERATING SYSTEM:   " + OPERATING_SYSTEM);
        System.out.println("OPENGL VENDOR:      " + glGetString(GL_VENDOR));
        System.out.println("RENDERER:           " + glGetString(GL_RENDERER));
        System.out.println("OPENGL VERSION:     " + glGetString(GL_VERSION));
        System.out.println("GLSL VERSION:       " + glGetString(GL_SHADING_LANGUAGE_VERSION));
    }

    private static void printResolutionAndTimerSettings()
    {
        System.out.println();
        System.out.println("RESOLUTION:         " + WINDOW_WIDTH + " x " + WINDOW_HEIGHT);
        System.out.println("TARGET FPS:         " + TARGET_FPS);
        System.out.println("TARGET UPS:         " + TARGET_UPS);
    }

    private static void printOptionStatus()
    {
        System.out.println();
        System.out.println("DEBUG MODE:         " + convertBooleanToEnabledOrDisabled(DEBUG));
        System.out.println("ANTIALIASING:       " + convertBooleanToEnabledOrDisabled(ANTIALIASING));
        System.out.println("VSYNC:              " + convertBooleanToEnabledOrDisabled(V_SYNC));
        System.out.println("WIREFRAME MODE:     " + convertBooleanToEnabledOrDisabled(SHOW_TRIANGLES));
        System.out.println("CULLFACE:           " + convertBooleanToEnabledOrDisabled(CULLFACE));
        System.out.println();
    }

    private static String convertBooleanToEnabledOrDisabled(boolean status)
    {
        return status ? "Enabled" : "Disabled";
    }
}
