/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  Neither the name of the Mario AI nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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



import java.awt.*;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.Replayer;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.MarioAIOptions;
import mctsMario.level.Level;
import mctsMario.level.SpriteTemplate;
import mctsMario.sprites.BulletBill;
import mctsMario.sprites.CoinAnim;
import mctsMario.sprites.Enemy;
import mctsMario.sprites.FireFlower;
import mctsMario.sprites.Fireball;
import mctsMario.sprites.FlowerEnemy;
import mctsMario.sprites.GreenMushroom;
import mctsMario.sprites.Mario;
import mctsMario.sprites.Mushroom;
import mctsMario.sprites.Particle;
import mctsMario.sprites.Shell;
import mctsMario.sprites.Sparkle;
import mctsMario.sprites.Sprite;
import mctsMario.sprites.SpriteContext;

public final class LevelScene implements SpriteContext, Cloneable
{
	public static final boolean[] defaultKeys = new boolean[Environment.numberOfKeys];
	public static final String[] keysStr = {"<<L ", "R>> ", "\\\\//", "JUMP", " RUN", "^UP^"};

	public static final int cellSize = 16;

	public List<Sprite> sprites = new ArrayList<Sprite>();
	final private List<Sprite> spritesToAdd = new ArrayList<Sprite>();
	final private List<Sprite> spritesToRemove = new ArrayList<Sprite>();

	public Level level;
	public Mario mario;
	public float xCam, yCam, xCamO, yCamO;

	public int tickCount;

	public int startTime = 0;
	private int timeLeft;
	private int width;
	private int height;

	private static boolean onLadder = false;

	private Random randomGen = new Random(0);

	final private List<Float> enemiesFloatsList = new ArrayList<Float>();
	final private float[] marioFloatPos = new float[2];
	final private int[] marioState = new int[11];
	private int numberOfHiddenCoinsGained = 0;

	private int greenMushroomMode = 0;

	public String memo = "";
	private Point marioInitialPos;
	private int bonusPoints = -1;

	//    public int getTimeLimit() {  return timeLimit; }

	public void setTimeLimit(int timeLimit)
	{ this.timeLimit = timeLimit; }

	private int timeLimit = 200;

	private long levelSeed;
	private int levelType;
	private int levelDifficulty;
	private int levelLength;
	private int levelHeight;
	public static int killedCreaturesTotal;
	public static int killedCreaturesByFireBall;
	public static int killedCreaturesByStomp;
	public static int killedCreaturesByShell;

	private Replayer replayer;

	//    private int[] args; //passed to reset method. ATTENTION: not cloned.

	public LevelScene()
	{
		try
		{
			//            System.out.println("Java::LevelScene: loading tiles.dat...");
			//            System.out.println("LS: System.getProperty(\"user.dir()\") = " + System.getProperty("user.dir"));
			Level.loadBehaviors(new DataInputStream(LevelScene.class.getResourceAsStream("resources/tiles.dat")));
		} catch (IOException e)
		{
			System.err.println("[MarioAI ERROR] : error loading file resources/tiles.dat ; ensure this file exists in ch/idsia/benchmark/mario/engine ");
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static List<Sprite> cloneList(List<Sprite> list) throws CloneNotSupportedException {
        List<Sprite> clone = new ArrayList<Sprite>(list.size());
        for(Sprite item: list) clone.add((Sprite) item.clone());
        return clone;
    }
    
    @Override protected Object clone() throws CloneNotSupportedException 
    {
    	LevelScene c = (LevelScene) super.clone();
    	c.mario = (Mario) this.mario.clone();
    	c.level = (Level) this.level.clone();
    	
    	List<Sprite> clone = new ArrayList<Sprite>(this.sprites.size());
        for(Sprite item: this.sprites) 
        {
        	if (item == mario)
        	{
        		clone.add(c.mario);
        	}
        	else
        	{
        		Sprite s = (Sprite) item.clone();
        		if (s.kind == Sprite.KIND_SHELL && ((Shell) s).carried && c.mario.carried != null)
        			c.mario.carried = s;
        		clone.add(s);
        	}
        }

        c.sprites = clone;
    	return c;
    }


	// TODO: !H!: Move to MarioEnvironment !!

	public float[] getEnemiesFloatPos()
	{
		enemiesFloatsList.clear();
		for (Sprite sprite : sprites)
		{
			// TODO:[M]: add unit tests for getEnemiesFloatPos involving all kinds of creatures
			if (sprite.isDead()) continue;
			switch (sprite.kind)
			{
			case Sprite.KIND_GOOMBA:
			case Sprite.KIND_BULLET_BILL:
			case Sprite.KIND_ENEMY_FLOWER:
			case Sprite.KIND_GOOMBA_WINGED:
			case Sprite.KIND_GREEN_KOOPA:
			case Sprite.KIND_GREEN_KOOPA_WINGED:
			case Sprite.KIND_RED_KOOPA:
			case Sprite.KIND_RED_KOOPA_WINGED:
			case Sprite.KIND_SPIKY:
			case Sprite.KIND_SPIKY_WINGED:
			case Sprite.KIND_SHELL:
			{
				enemiesFloatsList.add((float) sprite.kind);
				enemiesFloatsList.add(sprite.x - mario.x);
				enemiesFloatsList.add(sprite.y - mario.y);
			}
			}
		}

		float[] enemiesFloatsPosArray = new float[enemiesFloatsList.size()];

		int i = 0;
		for (Float F : enemiesFloatsList)
			enemiesFloatsPosArray[i++] = F;

		return enemiesFloatsPosArray;
	}

	public int fireballsOnScreen = 0;

	List<Shell> shellsToCheck = new ArrayList<Shell>();

	public void checkShellCollide(Shell shell)
	{
		shellsToCheck.add(shell);
	}

	List<Fireball> fireballsToCheck = new ArrayList<Fireball>();

	public void checkFireballCollide(Fireball fireball)
	{
		fireballsToCheck.add(fireball);
	}

	public void tick()
	{
		if (GlobalOptions.isGameplayStopped)
			return;

		timeLeft--;
		if (timeLeft == 0)
			mario.die("Time out!");
		xCamO = xCam;
		yCamO = yCam;

		if (startTime > 0)
		{
			startTime++;
		}

		float targetXCam = mario.x - 160;

		xCam = targetXCam;

		if (xCam < 0) xCam = 0;
		if (xCam > level.length * cellSize - GlobalOptions.VISUAL_COMPONENT_WIDTH)
			xCam = level.length * cellSize - GlobalOptions.VISUAL_COMPONENT_WIDTH;

		fireballsOnScreen = 0;

		for (Sprite sprite : sprites)
		{
			if (sprite != mario)
			{
				float xd = sprite.x - xCam;
				float yd = sprite.y - yCam;
				if (xd < -64 || xd > GlobalOptions.VISUAL_COMPONENT_WIDTH + 64 || yd < -64 || yd > GlobalOptions.VISUAL_COMPONENT_HEIGHT + 64)
				{
					removeSprite(sprite);
				} else
				{
					if (sprite instanceof Fireball)
						fireballsOnScreen++;
				}
			}
		}

		tickCount++;
		level.tick();

		//            boolean hasShotCannon = false;
		//            int xCannon = 0;

		for (int x = (int) xCam / cellSize - 1; x <= (int) (xCam + this.width) / cellSize + 1; x++)
			for (int y = (int) yCam / cellSize - 1; y <= (int) (yCam + this.height) / cellSize + 1; y++)
			{
				int dir = 0;

				if (x * cellSize + 8 > mario.x + cellSize) dir = -1;
				if (x * cellSize + 8 < mario.x - cellSize) dir = 1;

				SpriteTemplate st = level.getSpriteTemplate(x, y);

				if (st != null)
				{
					//                        if (st.getType() == Sprite.KIND_SPIKY)
					//                        {
					//                            System.out.println("here");
					//                        }

					if (st.lastVisibleTick != tickCount - 1)
					{
						if (st.sprite == null || !sprites.contains(st.sprite))
							st.spawn(this, x, y, dir);
					}

					st.lastVisibleTick = tickCount;
				}

				if (dir != 0)
				{
					byte b = level.getBlock(x, y);
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0)
					{
						if ((b % cellSize) / 4 == 3 && b / cellSize == 0)
						{
							if ((tickCount - x * 2) % 100 == 0)
							{
								//                                    xCannon = x;
								for (int i = 0; i < 8; i++)
								{
									addSprite(new Sparkle(x * cellSize + 8, y * cellSize + (int) (Math.random() * cellSize), (float) Math.random() * dir, 0, 0, 1, 5));
								}
								addSprite(new BulletBill(this, x * cellSize + 8 + dir * 8, y * cellSize + 15, dir));

								//                                    hasShotCannon = true;
							}
						}
					}
				}
			}
		for (Sprite sprite : sprites)
			sprite.tick();

		byte levelElement = level.getBlock(mario.mapX, mario.mapY);
		if (levelElement == (byte) (13 + 3 * 16) || levelElement == (byte) (13 + 5 * 16))
		{
			if (levelElement == (byte) (13 + 5 * 16))
				mario.setOnTopOfLadder(true);
			else
				mario.setInLadderZone(true);
		} else if (mario.isInLadderZone())
		{
			mario.setInLadderZone(false);
		}


		for (Sprite sprite : sprites)
			sprite.collideCheck();

		for (Shell shell : shellsToCheck)
		{
			for (Sprite sprite : sprites)
			{
				if (sprite != shell && !shell.dead)
				{
					if (sprite.shellCollideCheck(shell))
					{
						if (mario.carried == shell && !shell.dead)
						{
							mario.carried = null;
							mario.setRacoon(false);
							//System.out.println("sprite = " + sprite);
							shell.die();
							++this.killedCreaturesTotal;
						}
					}
				}
			}
		}
		shellsToCheck.clear();

		for (Fireball fireball : fireballsToCheck)
			for (Sprite sprite : sprites)
				if (sprite != fireball && !fireball.dead)
					if (sprite.fireballCollideCheck(fireball))
						fireball.die();
		fireballsToCheck.clear();


		sprites.addAll(0, spritesToAdd);
		sprites.removeAll(spritesToRemove);
		spritesToAdd.clear();
		spritesToRemove.clear();
	}

	//Update internal level representation to what we get from the API.
	// includes some gap detection code
	public void setLevelScene(byte[][] data)
	{
	    int HalfObsWidth = data.length/2;
    	int HalfObsHeight = data[data.length-1].length/2;
    	//System.out.println(HalfObsWidth+","+HalfObsHeight);
		int MarioXInMap = (int)mario.x/16;
		int MarioYInMap = (int)mario.y/16;
		for (int x = 0; x < data.length; x++) {
			for (int y = 0; y < data[x].length; y++) {
				int tempx = y-HalfObsWidth;
				int tempy = x-HalfObsHeight;
				int correctedX = tempx+MarioXInMap;
				int correctedY = tempy+MarioYInMap;
				
				if(correctedX >= 0 && correctedY >=0 && correctedX < level.length && correctedY < level.height)
				{
					level.setBlock(correctedX, correctedY, data[x][y]);
				}
				
			}
		}
		
		boolean gapFound = false;
		int gapX = -1;
		int gapY = -1;
		for (int i = 0; i < data[data.length-1].length; i++) {
			String lvl = "";
			boolean test = true;
			for (int j = 0; j < data.length; j++) {
				if(data[j][i] != 0)
				{
					test = false;
				}
				if(i >0)
				{
					if(data[j][i-1] != 0 && gapY == -1)
					{
						gapY = j;
						System.out.println(gapY);
					}
						
				}
				else
				{
					if(data[j][i+1] != 0 && gapY == -1)
						gapY = j;
				}
				lvl += data[j][i];
			}
			if(test)
			{
				gapFound = true;
				if(gapX == -1)
					gapX = i;
			}
//			System.out.println(lvl);
		}
		if(gapFound)
			System.out.println("Gap Start: X: "+(gapX-HalfObsWidth)+", Y: "+ (gapY-HalfObsHeight));
		//    int HalfObsWidth = 11;
		//    int HalfObsHeight = 11;
		//    int MarioXInMap = (int)mario.x/16;
		//    int MarioYInMap = (int)mario.y/16;
		//    boolean gapAtLast = true;
		//    boolean gapAtSecondLast = true;
		//    int lastEventX = 0;
		//    int[] heights = new int[22];
		//    for(int i = 0; i < heights.length; i++)
		//    	heights[i] = 0;
		//    
		//    int gapBorderHeight = 0;
		//    int gapBorderMinusOneHeight = 0;
		//    int gapBorderMinusTwoHeight = 0;
		//    
		//    for (int y = MarioYInMap - HalfObsHeight, obsX = 0; y < MarioYInMap + HalfObsHeight; y++, obsX++)
		//    {
		//        for (int x = MarioXInMap - HalfObsWidth, obsY = 0; x < MarioXInMap + HalfObsWidth; x++, obsY++)
		//        {
		//            if (x >=0 && x <= level.xExit && y >= 0 && y < level.height)
		//            {
		////            	System.out.println(obsX+","+obsY);
		//            	byte datum = data[obsX][obsY];
		//            	
		//             	if (datum != 0 && datum != -10 && datum != 1 && obsY > lastEventX)
		//            	{
		//             		lastEventX = obsY;
		//            	}
		//             	if (datum != 0 && datum != 1)
		//            	{
		//            		if (heights[obsY] == 0)
		//            		{
		//            			heights[obsY] = y;
		//            		}
		//            	}
		//             	
		//            	// cannon detection: if there's a one-block long hill, it's a cannon!
		//             	// i think this is not required anymore, because we get the cannon data straight from the API.
		//            	if (x == MarioXInMap + HalfObsWidth - 3 &&
		//            			datum != 0 && y > 5)
		//            	{
		//
		//            		if (gapBorderMinusTwoHeight == 0)
		//            			gapBorderMinusTwoHeight = y;
		//            	}
		//            	if (x == MarioXInMap + HalfObsWidth - 2 &&
		//            			datum != 0 && y > 5)
		//            	{
		//            		if (gapBorderMinusOneHeight == 0)
		//            			gapBorderMinusOneHeight = y;
		//            		gapAtSecondLast = false;
		//            	}
		//            	if (x == MarioXInMap + HalfObsWidth - 1 &&
		//            			datum != 0 && y > 5)
		//            	{
		//
		//            		if (gapBorderHeight == 0)
		//            			gapBorderHeight = y;
		//            		gapAtLast = false;
		//            	}
		//            	
		//                if (datum != 1 && level.getBlock(x, y) != 14) 
		//                	level.setBlock(x, y, datum);
		//            }
		//        }
		//    }
		//    if (gapBorderHeight == gapBorderMinusTwoHeight && gapBorderMinusOneHeight < gapBorderHeight)
		//    {
		//    	// found a cannon!
		//    	//System.out.println("Got a cannon!");
		//    	level.setBlock(MarioXInMap + HalfObsWidth - 2,gapBorderMinusOneHeight, (byte)14);
		//    }
		//    if (gapAtLast && !gapAtSecondLast)
		//    {
		//    	// found a gap. 
		//    	int holeWidth = 3;
		//
		//		// make the gap wider before we see the end to allow ample time for the 
		//    	// planner to jump over.
		//    	for(int i = 0; i < holeWidth; i++)
		//    	{
		//        	for(int j = 0; j < 15; j++)
		//        	{
		//        		level.setBlock(MarioXInMap + HalfObsWidth + i, j, (byte) 0);
		//        	}
		////        	level.isGap[MarioXInMap + HalfObsWidth + i] = true;
		////        	level.gapHeight[MarioXInMap + HalfObsWidth + i] = gapBorderMinusOneHeight;
		//    	}
		//    	for(int j = gapBorderMinusOneHeight; j < 16; j++)
		//    	{
		//    		level.setBlock(MarioXInMap + HalfObsWidth + holeWidth, gapBorderMinusOneHeight, (byte) 4);
		//    	}
		//    	return true;
		//    }
		//	return false;
	}

	public void addSprite(Sprite sprite)
	{
		spritesToAdd.add(sprite);
		sprite.tick();
	}

	public void removeSprite(Sprite sprite)
	{
		spritesToRemove.add(sprite);
	}

	public void bump(int x, int y, boolean canBreakBricks)
	{
		byte block = level.getBlock(x, y);

		if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0)
		{
			if (block == 1)
				Mario.gainHiddenBlock();
			bumpInto(x, y - 1);
			byte blockData = level.getBlockData(x, y);
			if (blockData < 0)
				level.setBlockData(x, y, (byte) (blockData + 1));

			if (blockData == 0)
			{
				level.setBlock(x, y, (byte) 4);
				level.setBlockData(x, y, (byte) 4);
			}

			if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
			{
				if (randomGen.nextInt(5) == 0 && level.difficulty > 4)
				{
					addSprite(new GreenMushroom(this, x * cellSize + 8, y * cellSize + 8));
					++level.counters.greenMushrooms;
				} else
				{
					if (!Mario.large)
					{
						addSprite(new Mushroom(this, x * cellSize + 8, y * cellSize + 8));
						++level.counters.mushrooms;
					} else
					{
						addSprite(new FireFlower(this, x * cellSize + 8, y * cellSize + 8));
						++level.counters.flowers;
					}
				}
			} else
			{
				Mario.gainCoin();
				addSprite(new CoinAnim(x, y));
			}
		}

		if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
		{
			bumpInto(x, y - 1);
			if (canBreakBricks)
			{
				level.setBlock(x, y, (byte) 0);
				for (int xx = 0; xx < 2; xx++)
					for (int yy = 0; yy < 2; yy++)
						addSprite(new Particle(x * cellSize + xx * 8 + 4, y * cellSize + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));
			} else
			{
				level.setBlockData(x, y, (byte) 4);
			}
		}
	}

	public void bumpInto(int x, int y)
	{
		byte block = level.getBlock(x, y);
		if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
		{
			Mario.gainCoin();
			level.setBlock(x, y, (byte) 0);
			addSprite(new CoinAnim(x, y + 1));
		}

		for (Sprite sprite : sprites)
		{
			sprite.bumpCheck(x, y);
		}
	}

	public int getTimeSpent() { return startTime / GlobalOptions.mariosecondMultiplier; }

	public int getTimeLeft() { return timeLeft / GlobalOptions.mariosecondMultiplier; }

	public int getKillsTotal()
	{
		return killedCreaturesTotal;
	}

	public int getKillsByFire()
	{
		return killedCreaturesByFireBall;
	}

	public int getKillsByStomp()
	{
		return killedCreaturesByStomp;
	}

	public int getKillsByShell()
	{
		return killedCreaturesByShell;
	}

	public int[] getMarioState()
	{
		marioState[0] = this.getMarioStatus();
		marioState[1] = this.getMarioMode();
		marioState[2] = this.isMarioOnGround() ? 1 : 0;
		marioState[3] = this.isMarioAbleToJump() ? 1 : 0;
		marioState[4] = this.isMarioAbleToShoot() ? 1 : 0;
		marioState[5] = this.isMarioCarrying() ? 1 : 0;
		marioState[6] = this.getKillsTotal();
		marioState[7] = this.getKillsByFire();
		marioState[8] = this.getKillsByStomp();
		marioState[9] = this.getKillsByShell();
		marioState[10] = this.getTimeLeft();
		return marioState;
	}

	public void performAction(boolean[] action)
	{
		// might look ugly , but arrayCopy is not necessary here:
		this.mario.keys = action;
	}

	public boolean isLevelFinished()
	{
		return (mario.getStatus() != Mario.STATUS_RUNNING);
	}

	public boolean isMarioAbleToShoot()
	{
		return mario.isAbleToShoot();
	}

	public int getMarioStatus()
	{
		return mario.getStatus();
	}

	/**
	 * first and second elements of the array are x and y Mario coordinates correspondingly
	 *
	 * @return an array of size 2*(number of creatures on screen) including mario
	 */
	public float[] getCreaturesFloatPos()
	{
		float[] enemies = this.getEnemiesFloatPos();
		float ret[] = new float[enemies.length + 2];
		System.arraycopy(this.getMarioFloatPos(), 0, ret, 0, 2);
		System.arraycopy(enemies, 0, ret, 2, enemies.length);
		return ret;
	}

	public boolean isMarioOnGround()
	{ return mario.isOnGround(); }

	public boolean isMarioAbleToJump()
	{ return mario.mayJump(); }

	public void resetDefault()
	{
		// TODO: set values to defaults
		reset(MarioAIOptions.getDefaultOptions());
	}

	public void reset(MarioAIOptions marioAIOptions)
	{
		//        System.out.println("\nLevelScene RESET!");
		//        this.gameViewer = setUpOptions[0] == 1;
		//        System.out.println("this.mario.isMarioInvulnerable = " + this.mario.isMarioInvulnerable);
		//    this.levelDifficulty = marioAIOptions.getLevelDifficulty();
		//        System.out.println("this.levelDifficulty = " + this.levelDifficulty);
		//    this.levelLength = marioAIOptions.getLevelLength();
		//        System.out.println("this.levelLength = " + this.levelLength);
		//    this.levelSeed = marioAIOptions.getLevelRandSeed();
		//        System.out.println("levelSeed = " + levelSeed);
		//    this.levelType = marioAIOptions.getLevelType();
		//        System.out.println("levelType = " + levelType);


		GlobalOptions.FPS = marioAIOptions.getFPS();
		//        System.out.println("GlobalOptions.FPS = " + GlobalOptions.FPS);
		GlobalOptions.isPowerRestoration = marioAIOptions.isPowerRestoration();
		//        System.out.println("GlobalOptions.isPowerRestoration = " + GlobalOptions.isPowerRestoration);
		//    GlobalOptions.isPauseWorld = marioAIOptions.isPauseWorld();
		GlobalOptions.areFrozenCreatures = marioAIOptions.isFrozenCreatures();
		//        System.out.println("GlobalOptions = " + GlobalOptions.isPauseWorld);
		//        GlobalOptions.isTimer = marioAIOptions.isTimer();
		//        System.out.println("GlobalOptions.isTimer = " + GlobalOptions.isTimer);
		//        isToolsConfigurator = setUpOptions[11] == 1;
		this.setTimeLimit(marioAIOptions.getTimeLimit());
		//        System.out.println("this.getTimeLimit() = " + this.getTimeLimit());
		//        this.isViewAlwaysOnTop() ? 1 : 0, setUpOptions[13]
		GlobalOptions.isVisualization = marioAIOptions.isVisualization();
		//        System.out.println("visualization = " + visualization);

		killedCreaturesTotal = 0;
		killedCreaturesByFireBall = 0;
		killedCreaturesByStomp = 0;
		killedCreaturesByShell = 0;

		marioInitialPos = marioAIOptions.getMarioInitialPos();
		greenMushroomMode = marioAIOptions.getGreenMushroomMode();

		if (replayer != null)
		{
			try
			{
				//            replayer.openNextReplayFile();
				replayer.openFile("level.lvl");
				level = (Level) replayer.readObject();
				level.counters.resetUncountableCounters();
				//            replayer.closeFile();
				//            replayer.closeRecorder();
			} catch (IOException e)
			{
				System.err.println("[Mario AI Exception] : level reading failed");
				e.printStackTrace();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		} //else
		//level = LevelGenerator.createLevel(marioAIOptions);

		String fileName = marioAIOptions.getLevelFileName();
		if (!fileName.equals(""))
		{
			try
			{
				Level.save(level, new ObjectOutputStream(new FileOutputStream(fileName)));
			} catch (IOException e)
			{
				System.err.println("[Mario AI Exception] : Cannot write to file " + fileName);
				e.printStackTrace();
			}
		}
		this.levelSeed = level.randomSeed;
		this.levelLength = level.length;
		this.levelHeight = level.height;
		this.levelType = level.type;
		this.levelDifficulty = level.difficulty;

		Sprite.spriteContext = this;
		sprites.clear();
		this.width = GlobalOptions.VISUAL_COMPONENT_WIDTH;
		this.height = GlobalOptions.VISUAL_COMPONENT_HEIGHT;

		Sprite.setCreaturesGravity(marioAIOptions.getCreaturesGravity());
		Sprite.setCreaturesWind(marioAIOptions.getWind());
		Sprite.setCreaturesIce(marioAIOptions.getIce());
		Mario.resetStatic(marioAIOptions);

		bonusPoints = -1;

		mario = new Mario(this);
		//System.out.println("mario = " + mario);
		memo = "";

		sprites.add(mario);
		startTime = 1;
		timeLeft = timeLimit * GlobalOptions.mariosecondMultiplier;

		tickCount = 0;
	}

	public float[] getMarioFloatPos()
	{
		marioFloatPos[0] = this.mario.x;
		marioFloatPos[1] = this.mario.y;
		return marioFloatPos;
	}

	public int getMarioMode()
	{ return mario.getMode(); }

	public boolean isMarioCarrying()
	{ return mario.carried != null; }

	public int getLevelDifficulty()
	{ return levelDifficulty; }

	public long getLevelSeed()
	{ return levelSeed; }

	public int getLevelLength()
	{ return levelLength; }

	public int getLevelHeight()
	{ return levelHeight; }

	public int getLevelType()
	{ return levelType; }


	public void addMemoMessage(final String memoMessage)
	{
		memo += memoMessage;
	}

	public Point getMarioInitialPos() {return marioInitialPos;}

	public void setReplayer(Replayer replayer)
	{
		this.replayer = replayer;
	}

	public int getGreenMushroomMode()
	{
		return greenMushroomMode;
	}

	public int getBonusPoints()
	{
		return bonusPoints;
	}

	public void setBonusPoints(final int bonusPoints)
	{
		this.bonusPoints = bonusPoints;
	}

	public void appendBonusPoints(final int superPunti)
	{
		bonusPoints += superPunti;
	}

	public void setEnemies(float[] enemies) 
	{
		List<Sprite> newSprites = new ArrayList<Sprite>();
		for (int i = 0; i < enemies.length; i += 3)
		{
			int kind = (int) enemies[i];
			float x = enemies[i+1]+mario.x;
			float y = enemies[i+2]+mario.y;
			int type = -1;
	        boolean winged = false;
	        switch(kind)
	        {
	        case(Sprite.KIND_BULLET_BILL): type = -2; break;
	        case(Sprite.KIND_GOOMBA): type = Enemy.KIND_GOOMBA; break;
	        case(Sprite.KIND_SHELL): type = Enemy.KIND_SHELL; break;
	        case(Sprite.KIND_GOOMBA_WINGED): type = Enemy.KIND_GOOMBA; winged = true; break;
	        case(Sprite.KIND_GREEN_KOOPA): type = Enemy.KIND_GREEN_KOOPA; break;
	        case(Sprite.KIND_GREEN_KOOPA_WINGED): type = Enemy.KIND_GREEN_KOOPA; winged = true; break;
	        case(Sprite.KIND_RED_KOOPA): type = Enemy.KIND_RED_KOOPA; break;
	        case(Sprite.KIND_RED_KOOPA_WINGED): type = Enemy.KIND_RED_KOOPA; winged = true; break;
	        case(Sprite.KIND_SPIKY): type = Enemy.KIND_SPIKY; break;
	        case(Sprite.KIND_SPIKY_WINGED): type = Enemy.KIND_SPIKY; winged = true; break;
//	        case(Sprite.KIND_FLOWER_ENEMY): type = Enemy.KIND_FLOWER; break;
	        }
	        if (!(type == -1 && kind == 0))
	        {
	        	//System.out.println("Enemy received: Type: "+type+" (Kind: "+kind+") Pos: "+x+" "+y);
	        }
	        if (type == -1)
	        	continue;
	        
        	Sprite sprite;
        	if (type == Enemy.KIND_ENEMY_FLOWER)
        	{
        		int flowerTileX = (int) x/16;
        		int flowerTileY = (int) y/16;
        		sprite = new FlowerEnemy(this, (int)x * 16 + 15, (int)y * 16 + 24, (int)x, (int)y);
		        //sprite = new FlowerEnemy(this, flowerTileX+15, (int)y, flowerTileX, flowerTileY, (int)y);
        	}
        	else if (kind == Sprite.KIND_BULLET_BILL)
        	{
        		System.out.println("Adding Bullet Bill!");
        		int dir = -1;
                sprite = new BulletBill(this, x, y, dir);
        	}
        	else
        	{
        		// Add new enemy to the system.
        		sprite = new Enemy(this, x, y, -1, type, winged, (int) x/16, (int) y/16);
        		sprite.xa = 2;
        	}
        
        	sprite.x = x;
        	sprite.y = y;
	        sprite.spriteTemplate =  new SpriteTemplate(type);
	        newSprites.add(sprite);
		}
		for (Sprite sprite:sprites)
        {
			if (sprite.kind == Sprite.KIND_FIREBALL)
				newSprites.add(sprite);
        }
		newSprites.add(mario);
		sprites = newSprites;
	}
	
	public void advanceStep(boolean[] action)
	{
		this.mario.setKeys(action); 
		this.tick();
	}
}

//    public void update(boolean[] action)
//    {
//        System.arraycopy(action, 0, mario.keys, 0, 6);
//    }
