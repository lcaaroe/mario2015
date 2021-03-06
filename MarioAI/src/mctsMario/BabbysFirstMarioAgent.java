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
	//Variables that are needed for the simulator to work properly
	private float lastX = 0;
	private float lastY = 0;
	private int invulnerableTime = 0;
	private int prevMarioState = 0;	
	MCTS mcts;
	
	//Constructor.
	public BabbysFirstMarioAgent()
	{
		super("Babby");
		//babby
		reset();
		
		mcts = new MCTS();
	}

	public boolean[] getAction()
	{
		//We get the All the information regarding the environment from the MarioEnvironment
		Environment environment = MarioEnvironment.getInstance();
		byte[][] cloned = environment.getLevelSceneObservationZ(1);
		float[] enemies = environment.getEnemiesFloatPos();
		float[] realMarioPos = environment.getMarioFloatPos();

		//Create a new Levelscene with a default size and reset it to initialize everything needed.
		LevelScene clonedLevel  = new LevelScene();
		clonedLevel.level = new Level(1500,15);
		clonedLevel.resetDefault();
		
		//Set the Mario position in the new levelScene according to the environment.
		//Calculate Marios current x and y acceleration by using the current X and Y and LastX and LastY variables.
		clonedLevel.mario.x = realMarioPos[0];		
		clonedLevel.mario.xa = (realMarioPos[0] - lastX) *0.89f;		
		if (Math.abs(clonedLevel.mario.y - realMarioPos[1]) > 0.1f)
			clonedLevel.mario.ya = (realMarioPos[1] - lastY) * 0.85f;// + 3f;
		clonedLevel.mario.y = realMarioPos[1];
		
		
		//These are custom function which we got from Baumgarten, we had to change them a bit
		//in order for them to work for the new framework.
		//These functions create the levelScene you can see on the screen as well as setting the enemies to the correct positions.
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
		
		//Checks the previous Mario Sate and checks if that has changed since Last getAction.
		//This is to ensure that the simulator gets the correct Invulerable Timer for Mario.
		if(environment.getMarioMode() != prevMarioState)
		{
			invulnerableTime = 32;
		}

		//At everyTick make sure that mario gets the invulerableTime as well as tick it down.
		if(invulnerableTime > 0)
		{
			clonedLevel.mario.invulnerableTime = invulnerableTime;
			invulnerableTime--;
		}
		
		//Save the currentPosition of Mario for future use.
		lastX = realMarioPos[0];
		lastY = realMarioPos[1];
		
		//Get the best action from the MCTS algorithm
		boolean[] newAction = mcts.search(clonedLevel);
		
		//Save the current State of Mario for future use.
		prevMarioState = environment.getMarioMode();
		return newAction;
	}

	@Override
	public void reset()
	{
		for (int i = 0; i < action.length; ++i)
			action[i] = false;
		
		prevMarioState = 2;
	}
}
