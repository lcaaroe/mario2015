package mctsMario;

import mctsMario.sprites.Mario;

public class mctsSimulator {
	public LevelScene levelScene;
	
	public mctsSimulator(LevelScene levelScene)
	{
		this.levelScene = levelScene;
	}
	
	public void simulate(int iteration)
	{
		for (int i = 0; i < iteration; i++) 
		{	
			System.out.println("Iteration: "+i);
			System.out.println("Simulated Enemies: "+levelScene.getEnemiesFloatPos().length/3);
			float[] simEnemies = levelScene.getEnemiesFloatPos();
			
			float[] simMarioPos = levelScene.getMarioFloatPos();			
			System.out.println("Simulated MarioPos before Tick: "+simMarioPos[0]+","+simMarioPos[1]);
			
//			levelScene.mario.keys[Mario.KEY_RIGHT] = true;
			System.out.println("[" 
    				+ (levelScene.mario.keys[Mario.KEY_DOWN] ? "d" : "") 
    				+ (levelScene.mario.keys[Mario.KEY_RIGHT] ? "r" : "")
    				+ (levelScene.mario.keys[Mario.KEY_LEFT] ? "l" : "")
    				+ (levelScene.mario.keys[Mario.KEY_JUMP] ? "j" : "")
    				+ (levelScene.mario.keys[Mario.KEY_SPEED] ? "s" : "") + "]");
			levelScene.tick();
			levelScene.mario.move();
			simEnemies = levelScene.getEnemiesFloatPos();
			System.out.println("Simulated Enemies After Tick: "+simEnemies.length/3);
			if(simEnemies.length > 0)
				System.out.println("First Entry after Tick: "+ simEnemies[0]);
			
			simMarioPos = levelScene.getMarioFloatPos();
			System.out.println("Simulated MarioPos after Tick: "+simMarioPos[0]+","+simMarioPos[1]);
			System.out.println("----------------------");
		}
		System.out.println("=======================");
	}
}
