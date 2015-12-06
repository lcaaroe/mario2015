package mctsMario;

import java.util.ArrayList;
import java.util.Random;

import ch.idsia.benchmark.mario.engine.MarioVisualComponent;
import mctsMario.sprites.Mario;

public class MCTS 
{
	// Max allowed time (in ms) to run the search. Algorithm needs a little time to select best child and exit.
	private int timeLimit = 19;
	
	// Exploration coefficient (default ~0.707107...)
	// "the value (...) was shown to satisfy the Hoeffding ineqality with rewards in the range [0,1]" (Browne et al., 2012)
	private static final float C = 0.7f;//(float) (1.0/Math.sqrt(2));
	
	// The minimum number of visits every node should have before it will be rated by UCT.
	private static final int CONFIDENCE_THRESHOLD = 1;
	
	// Number of random steps to perform when simulating in default policy.
	private static final int MAX_SIMULATION_TICKS = 10;
	
	// The number of times the same action should be repeated in a row while simulating.
	private static final int REPETITIONS = 1;
	
	// Whether to select best child based on max value (rather than average value).
	private static final boolean VALUE_BY_MAX = false;
	
	// Small float to break ties between equal UCT values.
	// Idea of tiebreaker inspired by http://mcts.ai/code/java.html
	private Random rng = new Random();
	private float tieBreaker = (float)1e-6;
	
	
	
	// TEST/DEBUG FIELDS
	int treePolicyCounter = 0;
	int searchCounter = 0;
	// The number of nodes in the deepest path of the tree.
	int maxDepth = 0;
	
	/**
	 * 
	 * @param levelScene The current real-game levelscene
	 * @return The best action found using MCTS
	 */
	public boolean[] search(LevelScene levelScene)
	{	
//		LevelScene levelSceneClone = null;
//		try{
//			levelSceneClone = (LevelScene) levelScene.clone();
//		}catch (CloneNotSupportedException e){
//			e.printStackTrace();
//		}
		Node rootNode = new Node(levelScene, null);
		
		searchCounter = 0;
		maxDepth = 0;
		
		long dueTime = System.currentTimeMillis() + timeLimit;
		while (System.currentTimeMillis() < dueTime)
		{
			searchCounter++; //TEST/DEBUG

			Node v1 = treePolicy(rootNode);
			float reward = defaultPolicy(v1, MAX_SIMULATION_TICKS);
			
			backpropagate(v1, reward);
		}
		//TEST/DEBUG
//		System.out.println("TIME'S UP - Iterations: " + searchCounter + "\t maxDepth = " + maxDepth);
		
		// Get child with the highest reward (by using value of 0 for C).
		Node bestChild = getBestChild(rootNode, 0);
//		Node mostVisitedChild = getMostVisitedChild(rootNode);

		// Return action corresponding to best child.
//		if (true) System.out.println("In main search | rootNode children: " + '\n' + rootNode.childrenAsString());
//		if (true) System.out.println("In main search | Best child with parent action = " + actionAsString(bestChild.parentAction)); // TEST/DEBUG
		
		return bestChild.parentAction;
//		return mostVisitedChild.parentAction;
	}
	
	/**
	 * Selects the next node to simulate from.
	 * @param v
	 * @return 
	 */
	private Node treePolicy(Node v)
	{
//		while (!isTerminalState(v.levelScene))
		int i = 0;
		while (true) // Arbitrary limit at the moment
		{
			i++;
			if (i==1000) // TEST/DEBUG
			{
				System.out.println("treepol limit reached");
			}
			// if v is not fully expanded, find and add new child.
			if (!isNodeFullyExpanded(v))
			{
				// Add new child to v and return it.
				return expand(v);
			}
			else
			{
				// Select best child according to UCT.
				v = getBestChild(v, C);
			}
		}

		// If current node represents a terminal state, no new child is added and the the current node is returned.
//		return v;
	}
	
	
	/**
	 * Get an untried action from state represented by this node, and add it as a child to this node.
	 * @param v
	 * @return The child representing the untried action of Node v.
	 */
	private Node expand(Node v)
	{
		// Get untried action (assumed that Node v is not fully expanded)
		boolean[] untriedAction = getUntriedAction(v);
		
		// Clone Node v's levelScene.
		LevelScene levelSceneClone = null;
		try{
			levelSceneClone = (LevelScene) v.levelScene.clone();
		}catch (CloneNotSupportedException e){
			e.printStackTrace();
		}
		
		// Create new child representing the world state after performing the untried action.
		Node child = new Node(levelSceneClone, v);
		child.parentAction = untriedAction;
		v.children.add(child);
		
		
		
		//--- TEST/DEBUG CODE
//		// This should be equal to parent since its fresh from the clone
		int marioModeBefore = child.levelScene.getMarioMode();
//
//		if (marioModeBefore != v.levelScene.getMarioMode())
//		{
//			// Should not trigger, since child Mode (and therefore marioModeBefore) is still an exact copy of parent Mode
//			System.out.println("parent Mode != marioModeBefore");
//		}
//		if (marioModeBefore != child.levelScene.getMarioMode())
//		{
//			// Should not trigger, since marioModeBefore has the value of child Mode
//			System.out.println("child Mode != marioModeBefore");
//		}
		//--- TEST/DEBUG CODE END ---
//
//		// Advance the levelScene 1 tick with the new action. 
//		// This child then represents the world state after taking that action.
		float mX = child.levelScene.getMarioFloatPos()[0];
		float mY = child.levelScene.getMarioFloatPos()[1];
		for (int i = 0; i < REPETITIONS; i++) 
		{
		}
		child.levelScene.advanceStep(untriedAction);

		
//		System.out.println("expand |\t" + child.levelScene + " | before: " + (int)mX + "," + (int)mY
//				+ " -- After " + REPETITIONS + " steps : " + actionAsString(untriedAction) + " " 
//				+ (int)child.levelScene.getMarioFloatPos()[0] + "," + (int)child.levelScene.getMarioFloatPos()[1]);
		
		// Necessary? this should already happen when cloning the levelscene...
		child.levelScene.mario.invulnerableTime = v.levelScene.mario.invulnerableTime;
//
		//--- TEST/DEBUG CODE
//		if (marioModeBefore != v.levelScene.getMarioMode())
//		{
////			 Should not trigger, since neither parent nor marioModeBefore should be affected by child tick
//			System.out.println("### After tick ### parent Mode (" + v.levelScene.getMarioMode()+")"
//					+ "!= marioModeBefore (" + marioModeBefore + ")");
//		}
		if (child.levelScene.getMarioMode() != marioModeBefore)
		{
//			 Should trigger! Because child was ticked and marioModeBefore never changes.
			System.out.println("--- After tick --- child Mode ("+child.levelScene.getMarioMode()+")" 
		+ " != marioModeBefore (" + marioModeBefore + ")");
		}
//
//		if (child.levelScene.getMarioMode() != v.levelScene.getMarioMode())
//		{
////			System.out.println("in expand | Mario was hit since parent");
//		}
		//--- TEST/DEBUG CODE END ---
		
		return child;
	}
	
	
	/**
	 * TODO: Find a better solution. getUntriedAction is called right after in expand as well...
	 * @param v
	 * @return Whether or not every possible action has been added as a child to this node.
	 */
	private boolean isNodeFullyExpanded(Node v)
	{
		boolean[] a = getUntriedAction(v);
		
		// TEST/DEBUG
		// If the untried action returned is a 'do-nothing' action (all buttons false), then no untried actions exist.
//		for (int i = 0; i < a.length; i++) {
//			if (a[i] == true)
//			{
////				System.out.println("False!"); //TEST/DEBUG
////				System.out.println(); //TEST/DEBUG
//				return false;
//			}
//		}
		// If action is null, no untried action exists, and node is fully expanded.
		if (a == null)
		{
			return true;
		}
		else
		{
			return false;
		}

			// If no buttons in the actions are set to true, this is a do-nothing action, and node is fully expanded.
//		return true;
		
	}
	
	
	/**
	 * Gets a random untried action from Node v.
	 * @param v
	 * @return An untried action from Node v. Returns null action if no untried actions exist.
	 */
	private boolean[] getUntriedAction(Node v)
	{
		ArrayList<boolean[]> untriedActions = new ArrayList<boolean[]>();
		
		outer:
		for (boolean[] a : v.actions) 
		{
			for (Node child : v.children) 
			{
				if (child.parentAction == a)
				{
					// A child representing this action already exist, so continue to next move.
					continue outer;
				}
			}
			// No child found representing this action, so it is untried.
//			return possibleAction;
			untriedActions.add(a);
		}
		
		// Return a random untried action
		if (untriedActions.size() > 0)
		{
			return untriedActions.get(rng.nextInt(untriedActions.size()));
		}
	
		// No untried action found so return null.
		return null;
	}
	
	
	/**
	 * Checks whether the node represents a state terminal game state (e.g. Mario dead or won).
	 * @param v
	 * @return Whether or not current states represents a terminal game state.
	 */
	private boolean isTerminalState(LevelScene levelScene)
	{
		// Level is finished is Mario won or is dead.
		return levelScene.isLevelFinished(); //v.levelScene.mario.isDead() ||
	}
	
	private boolean marioInGap(LevelScene levelScene)
	{
		// If gap is in sight
		if (levelScene.gapY != Integer.MIN_VALUE && levelScene.gapStartX != Integer.MIN_VALUE)
		{
			// If Mario's position is inside a gap (gap positions are relative to Mario).
			if (levelScene.gapY <= 1 && levelScene.gapStartX <= 0 && levelScene.gapEndX >= 0)
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean marioShrunk(int marioModeBefore, int marioModeAfter)
	{
		return (marioModeBefore > marioModeAfter);
	}
	
	
	/**
	 * Simulate playout from the state of Node v and return the reward obtained.
	 * @param v
	 * @param maxTicks The max number of ticks to simulate before terminating
	 *  (since it is unlikely that we reach a terminal state after few moves)
	 * @return Reward after playout terminates.
	 */
	private float defaultPolicy(Node v, int maxTicks)
	{
		// Clone Node v's levelScene.
		LevelScene levelSceneClone = null;
		try{
			levelSceneClone = (LevelScene) v.levelScene.clone();
		}catch (CloneNotSupportedException e){
			e.printStackTrace();
		}
		levelSceneClone.mario.invulnerableTime = v.levelScene.mario.invulnerableTime;

		/* Get Mario's starting position and mode (fire, large, small).
		 * Note that we are getting the values from the parent of v, since there might already be relevant 
		 * rewards from the one-step simulation used to create this Node in expand().
		 * */
		float marioFirstX = v.parent.levelScene.getMarioFloatPos()[0];
		float marioFirstY = v.parent.levelScene.getMarioFloatPos()[1];
		int marioFirstMode = v.parent.levelScene.getMarioMode();
		
		// For debugging purposes.
		ArrayList<boolean[]> actionsToSimulate = new ArrayList<boolean[]>();
		
		/* Advance levelScene using random possible actions until maxTicks budget is reached.
		 * Stop prematurely if level is finished (hard win or loss), or mario shrunk (was hit without dying).
		 * Not stopping for being in a gap, since finishing the playout might lead Mario out of the gap again. (Not sure!)
		 */
		int mX = (int) levelSceneClone.getMarioFloatPos()[0];
		int mY = (int) levelSceneClone.getMarioFloatPos()[1];

		int i = 0;
		boolean[] randomAction = null;
		while (i < maxTicks)
		{
			if (isTerminalState(levelSceneClone) || marioShrunk(marioFirstMode, levelSceneClone.getMarioMode()))
			{
				// If terminal state is reached, break out and just return 0
				return 0;
			}
			
			randomAction = v.getRandomAction();
			// Repeat the random action.
			for (int j = 0; j < REPETITIONS && i < maxTicks; j++) 
			{
				levelSceneClone.advanceStep(randomAction);
				
				actionsToSimulate.add(randomAction);
				i++;
			}
		}
		
		// Get reward for current state.
		float reward = calculateReward(levelSceneClone, marioFirstX, marioFirstY, marioFirstMode, i);
		
		// TEST/DEBUG print
//		String actionsSimulatedString = "";
//		for (boolean[] a : actionsToSimulate){
//			actionsSimulatedString += actionAsString(a);}
//		if (true) System.out.println("default |\t" + levelSceneClone + " | before: " +mX + "," +mY + " -- After " + i + " steps "
//				 + ": " + actionsSimulatedString + " " + 
//				+ (int)levelSceneClone.getMarioFloatPos()[0] + "," + (int)levelSceneClone.getMarioFloatPos()[1]
//						+ " (reward = " + reward);
		
		return reward;
	}
	
	
	/**
	 * Checks the current state of Mario in the given LevelScene, and calculates the reward based on the state.
	 * Mario gets reward of 0 for negative terminal states (ending up in a gap, shrinking, dying), and is rewarded
	 * some for standing still or going left, and more for going right.
	 * @param levelScene
	 * @param marioFirstX
	 * @param marioFirstMode
	 * @param marioParentMode
	 * @param ticksSimulated How many ticks were simulated in default policy before terminating.
	 * @return The reward for the current state.
	 */
	private float calculateReward(LevelScene levelScene, float marioFirstX, float marioFirstY, int marioFirstMode, int ticksSimulated)
	{
		// If Mario died, reward 0.
		if (levelScene.getMarioStatus() == Mario.STATUS_DEAD)
		{
//			System.out.println("DEAD. ticks = " + ticksSimulated);
			return 0;
		}
		// If Mario was hit without dying, reward 0.
		if (marioShrunk(marioFirstMode, levelScene.getMarioMode()))
		{
//			System.out.println("HIT. ticks = " + ticksSimulated + ". LS = " + levelScene);
			return 0;
		}
		// If mario completed the level
		if (levelScene.getMarioStatus() == Mario.STATUS_WIN)
		{
//			System.out.println("WON in simulation");
			return 1;
		}
		/* If simulation terminated before reaching terminal state, we approximate the reward.
		 * The maximum number of units Mario can run to the right is about 11. We normalize the achieved distance 
		 * by the max distance, given as 11 * (ticks simulated + REPETITIONS). 
		 * (+ REPETITIONS to account for the ticks done in expand().)
		 */
		// The minimum reward for non-terminal states (including standing still and going left) is 0.5.
		float reward = 0.3f;
		float xDistanceCovered = levelScene.getMarioFloatPos()[0] - marioFirstX;
		float yDistanceCovered = marioFirstY - levelScene.getMarioFloatPos()[1];
		
		// If Mario made progress towards the right, he is rewarded more.
		if (xDistanceCovered >= 0)
		{
			reward += 0.7f * (xDistanceCovered/(11*(ticksSimulated+REPETITIONS)));
		}
		// If Mario is in a gap, reduce his reward drastically. (Testing not having it set reward to 0).
		if (marioInGap(levelScene))
		{
//			if (ticksSimulated > 0)
			{
			}
//			reward *= 0.1f;
			// The maximum upwards y-movement Mario can make in one tick is about 13.3.
			if (yDistanceCovered >= 0)
			{
				reward = (yDistanceCovered/(13.3f*(ticksSimulated+REPETITIONS)));
			}
			else
			{
				reward = 0;
			}
			System.out.println("in gap = " + ticksSimulated + ", reward = " + reward );
		}
				
		if (Util.lcaDebug)System.out.println("In calculateReward | FirstX = " + marioFirstX + ", currentX = " + levelScene.mario.x);
		if (Util.lcaDebug)System.out.println("In calculateReward | Distance covered = " + xDistanceCovered);
//		System.out.println();
		
		return reward;
	}
	
	
	/**
	 * Backpropagate the given reward of Node v through all its ancestors in the tree. Accumulative and max.
	 * @param v
	 * @param reward
	 */
	private void backpropagate(Node v, float reward)
	{
		// Measure the depth of this path to root.
		int depth = 1;
		
		while (v != null)
		{
			v.timesVisited++;
			
			// Backpropagate accumulated reward.
			v.reward += reward;
			
			// Backpropagate the max reward.
			if (reward > v.maxReward)
			{
				v.maxReward = reward;
			}
			
			depth++;
			v = v.parent;
		}
		if (depth > maxDepth) maxDepth = depth;
	}
	
	
	/**
	 * Gets the child with the highest UCT value.
	 * @param v
	 * @param c Exploration coefficient
	 * @return The child of v with the highest UCT value.
	 */
	private Node getBestChild(Node v, float c)
	{
		if (v == null)
		{
			System.out.println("v is null in getbestchild");
		}
		if (v.children.size() == 0)
		{
			System.out.println("v has 0 children in getbestchild");
		}
		Node bestChild = null;

		// Find the child with the highest UCT value.
		float maxUCT = 0;
		for (Node child : v.children)
		{
			float thisUCT = calculateUCTValue(child, c, VALUE_BY_MAX);
			
			if (thisUCT > maxUCT)
			{
				maxUCT = thisUCT;
				bestChild = child;
			}
		}
		
		// Debug null check
		if (bestChild == null)
		{
			System.out.println("BestChild is NULL");
		}
		
		// return best child.
		return bestChild;
	}
	
	/**
	 * Gets the child with the highest visit count.
	 * @param v
	 * @param c Exploration coefficient
	 * @return The child of v with the highest visit count.
	 */
	private Node getMostVisitedChild(Node v)
	{
		Node mostVisitedChild = null;

		// Find the child with the highest UCT value.
		int maxVisits = 0;
		for (Node child : v.children)
		{
			if (child.timesVisited > maxVisits)
			{
				maxVisits = child.timesVisited;
				mostVisitedChild = child;
			}
		}
		return mostVisitedChild;
	}
	
	/**
	 * Calculates the UCT value for the Node n given the exploration coefficient c.
	 * The UCT value represents how interesting a node is to explore.
	 * @param n
	 * @param c exploration coefficient
	 * @return UCT value for Node n
	 */
	private float calculateUCTValue(Node n, float c, boolean goByMaxValue)
	{
		// Unvisited children should be assigned highest possible value to ensure that all children are considered at least once (Browne et al. 2012)
		if (n.timesVisited < CONFIDENCE_THRESHOLD)
		{
			return Float.MAX_VALUE - (rng.nextFloat() * tieBreaker);
		}
		
		// Approximation of the node's game-theoretic value (Browne et al. 2012)
		float valueTerm = n.reward/n.timesVisited;
		
		if (goByMaxValue) valueTerm = n.maxReward;
		
		// Gives higher value for less visited nodes.
		float explorationTerm = c * ((float) Math.sqrt((2 * Math.log(n.parent.timesVisited))/n.timesVisited));
		
		return valueTerm + explorationTerm + (rng.nextFloat() * tieBreaker);
	}
	
	/**
	 * @param a
	 * @return Action a as string in the format [drljsu]
	 */
	private String actionAsString(boolean[] a)
	{
		String s = "[" 
			+ (a[Mario.KEY_LEFT] ? "l" : "")
			+ (a[Mario.KEY_RIGHT] ? "r" : "")
			+ (a[Mario.KEY_DOWN] ? "d" : "") 
			+ (a[Mario.KEY_JUMP] ? "j" : "")
			+ (a[Mario.KEY_SPEED] ? "s" : "")
			+ (a[Mario.KEY_UP] ? "u" : "") + "]";
			
		return s;
	}
}
