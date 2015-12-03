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
	private int invulnerableTime = 0;
	private int prevMarioState = 0;
	
	// -- LCA TEST
//	private int i = 0;
//	private float marioFirstX = 0;
	// -- LCA TEST END
	
	MCTS mcts;
	
//	private mctsSimulator simulator;
	
	public BabbysFirstMarioAgent()
	{
		super("Babby");
		//babby
		reset();
		
		mcts = new MCTS();
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
		
//		System.out.println("babby | clonedLevel = " + clonedLevel);
		clonedLevel.advanceStep(action);
		
		clonedLevel.mario.x = realMarioPos[0];		
		clonedLevel.mario.xa = (realMarioPos[0] - lastX) *0.89f;		
		if (Math.abs(clonedLevel.mario.y - realMarioPos[1]) > 0.1f)
			clonedLevel.mario.ya = (realMarioPos[1] - lastY) * 0.85f;// + 3f;
		clonedLevel.mario.y = realMarioPos[1];
		
		
		clonedLevel.setLevelScene(cloned);
		clonedLevel.setEnemies(enemies);
		//update the mario mode according to the environment.
		switch (environment.getMarioMode()) {
		case 1:
			clonedLevel.mario.large = true;
			clonedLevel.mario.fire = false;
			break;
		case 2:
			clonedLevel.mario.large = true;
			clonedLevel.mario.fire = true;
			break;
		default:
			clonedLevel.mario.large = false;
			clonedLevel.mario.fire = false;
			break;
		}
		
		if(environment.getMarioMode() != prevMarioState)
		{
			System.out.println("Lost health during last aciton");
			invulnerableTime = 32;
		}

		if(invulnerableTime > 0)
		{
			clonedLevel.mario.invulnerableTime = invulnerableTime;
			invulnerableTime--;
		}
		lastX = realMarioPos[0];
		lastY = realMarioPos[1];
		
//		if (Util.ornDebug) System.out.println("Action: [" 
//		+ (action[Mario.KEY_DOWN] ? "d" : "") 
//		+ (action[Mario.KEY_RIGHT] ? "r" : "")
//		+ (action[Mario.KEY_LEFT] ? "l" : "")
//		+ (action[Mario.KEY_JUMP] ? "j" : "")
//		+ (action[Mario.KEY_SPEED] ? "s" : "") + "]");
		
		
		// -- Lasses test agent shit BEGIN --
//		if (environment.getMarioStatus() == Mario.STATUS_DEAD || environment.getMarioStatus() == Mario.STATUS_WIN)
//		{
//			System.out.println("LOLOLOLO");
//			System.out.println("isLevelFinished = " + environment.isLevelFinished());
//		}
//
//		
////		if (Util.lcaDebug) System.out.println("i = " + i + " | i%10 = " + i%10);
//		
//		// Introducing ... shitty jumping agent! Jumps every 20 frames for 3 frames in a row.
//		if ((i % 20 == 0) || (i % 20 == 1) || (i % 20 == 2))
//		{
////			if (Util.lcaDebug) System.out.println("Jumping allowed");
//			action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
//		}
//		else
//		{
//			action[Mario.KEY_JUMP] = false;
//		}
//		i++;
//		action[Mario.KEY_RIGHT] = true;
//		
//		// Measure how far Mario got -FOR TESTING PURPOSES-
//		if (i % 2 == 0)
//		{
//			marioFirstX = realMarioPos[0];
//		}
//		// After one frame, see how far mario got
//		if (i % 2 == 1)
//		{
//			System.out.println("FirstX = " + marioFirstX + ", currentX = " + realMarioPos[0] + ". Mario covered: " + (realMarioPos[0] - marioFirstX)+  " units.");
//		}
		// -- Lasses test agent shit END --
//		System.out.println("Mario status = " + environment.getMarioStatus() + "(environment babby)");
//		System.out.println("Mario status = " + clonedLevel.getMarioStatus() + "(clonedLevel babby)");
		
		boolean[] newAction = mcts.search(clonedLevel);
//		System.out.println("Mario status = " + clonedL7evel.getMarioStatus() + "(clonedLevel babby after mcts)");
		
//		if (environment.getMarioStatus() == Mario.STATUS_WIN)
//		{
//			System.out.println("Babby saw the win");
//		}
//		action = mcts.search(clonedLevel);
//		//System.out.println("Action length = " + newAction.length);
//		for (int i = 0; i < newAction.length; ++i)
//			newAction[i] = false;
//		newAction[Mario.KEY_RIGHT] = true;
		
		prevMarioState = environment.getMarioMode();
		return newAction;
	}

	@Override
	public void reset()
	{
		for (int i = 0; i < action.length; ++i)
			action[i] = false;
		
		prevMarioState = 2;
//		action[Mario.KEY_RIGHT] = true;
//	    action[Mario.KEY_SPEED] = true;
	}
}
