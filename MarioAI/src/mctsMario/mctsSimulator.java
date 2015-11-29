package mctsMario;

import mctsMario.level.Level;
import mctsMario.sprites.Mario;

public class mctsSimulator {
	public LevelScene levelScene;
	
	public mctsSimulator()
	{
		
		levelScene = new LevelScene();
		levelScene.level = new Level(1500,15);
		levelScene.resetDefault();
		// increase max level length here if you want to run longer levels
		
	}
	
	public void initialize()
	{
		
	}
	
	public boolean[] simulate(boolean[] action)
	{
//		boolean[] action = new boolean[6];
//		for (int i = 0; i < iteration; i++) 
//		{	
			//System.out.println("Iteration: "+i);
//			System.out.println("Simulated Enemies: "+levelScene.getEnemiesFloatPos().length/3);
//			float[] simEnemies = levelScene.getEnemiesFloatPos();
//			float[] simMarioPos = levelScene.getMarioFloatPos();			
			//System.out.println("Simulated MarioPos before Tick: "+(int)simMarioPos[0]/16+","+(int)simMarioPos[1]/16);
			
//			for (int x = 0; x < action.length; ++x)
//				action[x] = false;
//			action[Mario.KEY_RIGHT] = true;
			//System.out.println(levelScene.isMarioAbleToJump()+","+!levelScene.isMarioOnGround());
//			action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = levelScene.isMarioAbleToJump() || !levelScene.isMarioOnGround();
			advanceStep(action);
			
//			simEnemies = levelScene.getEnemiesFloatPos();
//			System.out.println("Simulated Enemies After Tick: "+simEnemies.length/3);
//			if(simEnemies.length > 0)
//				System.out.println("First Entry after Tick: "+ simEnemies[0]);
			
//			simMarioPos = levelScene.getMarioFloatPos();
			//System.out.println("Simulated MarioPos after Tick: "+(int)simMarioPos[0]/16+","+(int)simMarioPos[1]/16);
//			if((int)simMarioPos[1]/16 < levelScene.level.height)
//				System.out.println(levelScene.level.map[(int)simMarioPos[0]/16][(int)simMarioPos[1]/16]);
//			System.out.println("----------------------");
//		}
//		System.out.println("=======================");
		return action;

	}
	
	public void advanceStep(boolean[] action)
	{
		levelScene.mario.setKeys(action); 
		System.out.println("[" 
				+ (action[Mario.KEY_DOWN] ? "d" : "") 
				+ (action[Mario.KEY_RIGHT] ? "r" : "")
				+ (action[Mario.KEY_LEFT] ? "l" : "")
				+ (action[Mario.KEY_JUMP] ? "j" : "")
				+ (action[Mario.KEY_SPEED] ? "s" : "") + "]");
		levelScene.tick();
	}
}
