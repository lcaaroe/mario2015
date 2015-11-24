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

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 28, 2009
 * Time: 10:37:18 PM
 * Package: ch.idsia.controllers.agents.controllers;
 */
public class BabbysFirstMarioAgent extends BasicMarioAIAgent implements Agent
{
public BabbysFirstMarioAgent()
{
    super("Babby");
    //babby
    reset();
}

public boolean[] getAction()
{
	Environment environment = MarioEnvironment.getInstance();
	byte[][] cloned = environment.getLevelSceneObservationZ(0);
	float[] enemies = environment.getEnemiesFloatPos();
	float[] realMarioPos = environment.getMarioFloatPos();
	
	
	System.out.println(realMarioPos[0]+","+realMarioPos[1]);
	
	LevelScene test = new LevelScene();
	test.level = new Level(1500, 15);
	test.resetDefault();
	test.mario.x = realMarioPos[0];
	//test.mario.xa = (realMarioPos[0] - lastX) *0.89f;
	//if (Math.abs(test.mario.y - realMarioPos[1]) > 0.1f)
		//test.mario.ya = (realMarioPos[1] - lastY) * 0.85f;// + 3f;

	test.mario.y = realMarioPos[1];
	
	test.setLevelScene(cloned);
	test.setEnemies(enemies);
	System.out.println("Simulated Enemies: "+test.getEnemiesFloatPos().length);
	float[] testEnemies = test.getEnemiesFloatPos();
	if(testEnemies.length > 0)
		System.out.println("First Entry: "+ testEnemies[0]);
	float[] simMarioPos = test.getMarioFloatPos();
	if(enemies.length != test.getEnemiesFloatPos().length)
	{
		System.out.println("NOT THE SAME NUMBER OF ENEMIES!!");
	}
	System.out.println("Simulated MarioPos before Tick: "+simMarioPos[0]+","+simMarioPos[1]);
	test.tick();
	testEnemies = test.getEnemiesFloatPos();
	System.out.println("Counter: "+testEnemies.length);
	if(testEnemies.length > 0)
		System.out.println("First Entry after Tick: "+ testEnemies[0]);
	
	
	System.out.println("Simulated MarioPos after Tick: "+simMarioPos[0]+","+simMarioPos[1]);
	System.out.println("====");
    action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
    return action;
}

@Override
public void reset()
{
    for (int i = 0; i < action.length; ++i)
        action[i] = false;
    action[Mario.KEY_RIGHT] = true;
    action[Mario.KEY_SPEED] = true;
}
}
