package game;

import engine.core.GameEngine;
import engine.core.Window;
import engine.interfaces.IGameLogic;

public class Main {

    public static void main(String[] args)
    {

        try
        {
            IGameLogic gameLogic = new Game();
            GameEngine gameEng = new GameEngine("Game", 1024, 768, gameLogic);
            gameEng.start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
