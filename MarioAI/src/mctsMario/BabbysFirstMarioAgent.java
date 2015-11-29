/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package mctsMario;

import java.util.ArrayList;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import mctsMario.level.Level;
import mctsMario.sprites.Mario;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 28, 2009
 * Time: 10:37:18 PM
 * Package: ch.idsia.controllers.agents.controllers;
 */
public class BabbysFirstMarioAgent extends BasicMarioAIAgent implements Agent
{
	private float lastX = 0;
	private float lastY = 0;
//	private mctsSimulator simulator;
	
	public BabbysFirstMarioAgent()
	{
		super("Babby");
		//babby
		reset();
	}

	public boolean[] getAction()
	{
		Environment environment = MarioEnvironment.getInstance();
		byte[][] cloned = environment.getLevelSceneObservationZ(1);
		float[] enemies = environment.getEnemiesFloatPos();
		float[] realMarioPos = environment.getMarioFloatPos();
		
		LevelScene clonedLevel  = new LevelScene();
		clonedLevel.level = new Level(1500,15);
		clonedLevel.resetDefault();
		
		clonedLevel.mario.x = realMarioPos[0];		
		clonedLevel.mario.xa = (realMarioPos[0] - lastX) *0.89f;		
		if (Math.abs(clonedLevel.mario.y - realMarioPos[1]) > 0.1f)
			clonedLevel.mario.ya = (realMarioPos[1] - lastY) * 0.85f;// + 3f;
		clonedLevel.mario.y = realMarioPos[1];
		
		LevelScene levelTest = null;
		try
		{
			levelTest = (LevelScene) clonedLevel.clone();
		} catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		
		
//		//Get colliders to work.
//		simulator.advanceStep(action);		
		
//		simulator.levelScene.mario.x = realMarioPos[0];		
//		simulator.levelScene.mario.xa = (realMarioPos[0] - lastX) *0.89f;		
//		if (Math.abs(simulator.levelScene.mario.y - realMarioPos[1]) > 0.1f)
//			simulator.levelScene.mario.ya = (realMarioPos[1] - lastY) * 0.85f;// + 3f;
//		simulator.levelScene.mario.y = realMarioPos[1];
		
		System.out.println("xa: " +clonedLevel.mario.xa);
		System.out.println("ya: " +clonedLevel.mario.ya);
		

		clonedLevel.setLevelScene(cloned);
		clonedLevel.setEnemies(enemies);
		lastX = realMarioPos[0];
		lastY = realMarioPos[1];
		
		action[Mario.KEY_RIGHT] = true;
		action[Mario.KEY_SPEED] = true;
		action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
		
		System.out.println("Before: "+levelTest.mario.x+","+levelTest.mario.y);
		System.out.println("before: "+levelTest.isMarioOnGround());
		levelTest.advanceStep(action);
		System.out.println("After: "+levelTest.mario.x+","+levelTest.mario.y);
		System.out.println("testeru: "+levelTest.isMarioOnGround());
		
//		System.out.println("Environment: Jump: "+isMarioAbleToJump+", Grounded: "+!isMarioOnGround);
//		System.out.println("Simulator: Jump: "+clonedLevel.mario.mayJump()+", Grounded: "+!clonedLevel.mario.isOnGround());
		
//		for(int y=0; y<cloned.length; y++)
//		{
//			String meow = "";
//			for(int x=0; x<cloned[y].length; x++)
//			{
//				meow += cloned[y][x]+",";
//			}
//			System.out.println(meow);
//		}
		
		
		//	if(isMarioAbleToJump)
		//		action[Mario.KEY_JUMP] = true;
		//	else
		//		action[Mario.KEY_JUMP] = false;
		//action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;

//		for (int i = 0; i < action.length; i++) {
//			
//			action[i] = false;
//		}
		
		
		System.out.println("Action: [" 
				+ (action[Mario.KEY_DOWN] ? "d" : "") 
				+ (action[Mario.KEY_RIGHT] ? "r" : "")
				+ (action[Mario.KEY_LEFT] ? "l" : "")
				+ (action[Mario.KEY_JUMP] ? "j" : "")
				+ (action[Mario.KEY_SPEED] ? "s" : "") + "]");
		return action;
	}

	@Override
	public void reset()
	{
//		simulator = new mctsSimulator();
		for (int i = 0; i < action.length; ++i)
			action[i] = false;
		//action[Mario.KEY_JUMP] = true;
		action[Mario.KEY_RIGHT] = true;
		action[Mario.KEY_SPEED] = true;
		action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
	}
}
