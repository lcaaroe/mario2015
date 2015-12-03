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
	private static final float C = (float) (1.0/Math.sqrt(2));
	
	// The minimum number of visits every node should have before it will be rated by UCT.
	private static final int CONFIDENCE_THRESHOLD = 2;
	
	// Number of random steps to perform when simulating in default policy.
	private static final int MAX_SIMULATION_TICKS = 6;
	
	// Small float to break ties between equal UCT values.
	// Idea of tiebreaker inspired by http://mcts.ai/code/java.html
	private Random rng = new Random();
	private float tieBreaker = (float)1e-6;
	
	
	// TEST/DEBUG FIELDS
	int treePolicyCounter = 0;
	
	
	/**
	 * 
	 * @param levelScene The current real-game levelscene
	 * @return The best action found using MCTS
	 */
	public boolean[] search(LevelScene levelScene)
	{
		Node rootNode = new Node(levelScene, null);
		int searchCounter = 0;
		
		long dueTime = System.currentTimeMillis() + timeLimit;
		while (System.currentTimeMillis() < dueTime)
		{
			searchCounter++; //TEST/DEBUG
			if (Util.lcaDebug)System.out.println();
			if (Util.lcaDebug)System.out.println("Main search loop counter = " + searchCounter); //TEST/DEBUG
//			long currentTime = System.currentTimeMillis(); // TEST/DEBUG
//			System.out.println("Current time = " + currentTime + ". Time limit = " + dueTime + ". Time left = " + (dueTime - currentTime));

			
			Node v1 = treePolicy(rootNode);
			float reward = defaultPolicy(v1, MAX_SIMULATION_TICKS);
			
			backpropagate(v1, reward);
		}
		//TEST/DEBUG
//		System.out.println("Root statistics after backprop: Reward = " + rootNode.reward + " Times visited = " + rootNode.timesVisited);
		
		// Get child with the highest reward (by using value of 0 for C).
		Node bestChild = getBestChild(rootNode, 0);

		// Return action corresponding to best child.
//		if (true) System.out.println("In main search | rootNode children: " + '\n' + rootNode.childrenAsString());
//		if (true) System.out.println("In main search | Best child with parent action = " + actionAsString(bestChild.parentAction)); // TEST/DEBUG
//		System.out.println();
//		if (Util.lcaDebug)System.out.println("In main search | bestChild.ParentAction.length = " + bestChild.parentAction.length);
		
//		System.out.println("Mario status = " + levelScene.getMarioStatus() + "(mcts)");
		
		
		return bestChild.parentAction;

//		return rootNode.createAction(false, false, false, false, false, false);
	}
	
	/**
	 * Selects the next node to simulate from.
	 * @param v
	 * @return 
	 */
	private Node treePolicy(Node v)
	{
//		treePolicyCounter++; // TEST/DEBUG
//		System.out.println("TreePolicy counter = " + treePolicyCounter); // TEST/DEBUG
//		while (!isTerminalState(v.levelScene))
		int i = 0;
		while (i < 1000) // Arbitrary limit at the moment
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
		return v;
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
		
		System.out.println("Parent level, child level, clone level = " 
		+ v.levelScene + "," + child.levelScene + "," + levelSceneClone);
		
		// Necessary? this should already happen when cloning the levelscene...
		child.levelScene.mario.invulnerableTime = v.levelScene.mario.invulnerableTime;
		
		// This should be equal to parent since its fresh from the clone
		int marioModeBefore = child.levelScene.getMarioMode();

		if (marioModeBefore != v.levelScene.getMarioMode())
		{
			// Should not trigger, since child Mode (and therefore marioModeBefore) is still an exact copy of parent Mode
			System.out.println("parent Mode != marioModeBefore");
		}
		if (marioModeBefore != child.levelScene.getMarioMode())
		{
			// Should not trigger, since marioModeBefore has the value of child Mode
			System.out.println("child Mode != marioModeBefore");
		}
		
		float parentGoombaBefore = -1;
		if (v.levelScene.getEnemiesFloatPos().length > 0)
		{
			parentGoombaBefore = v.levelScene.getEnemiesFloatPos()[1];
		}
		
		// Advance the levelScene 1 tick with the new action. 
		// This child then represents the world state after taking that action.
		child.levelScene.advanceStep(untriedAction);

		float parentGoombaAfter = -1;
		if (v.levelScene.getEnemiesFloatPos().length > 0)
		{
			parentGoombaAfter = v.levelScene.getEnemiesFloatPos()[1];
		}
		
		if (parentGoombaBefore != parentGoombaAfter)
		{
			System.out.println("ParentGoombaBefore != ParentGoombaAfter");
		}
		if (marioModeBefore != child.parent.levelScene.getMarioMode())
		{
			// Should not trigger, since neither parent nor marioModeBefore should be affected by child tick
			System.out.println("### After tick ### parent Mode (" + v.levelScene.getMarioMode()+")"
					+ "!= marioModeBefore (" + marioModeBefore + ")");
		}
		if (child.levelScene.getMarioMode() != marioModeBefore)
		{
			// Should trigger! Because child was ticked and marioModeBefore never changes.
			System.out.println("--- After tick --- child Mode ("+child.levelScene.getMarioMode()+")" 
		+ " != marioModeBefore (" + marioModeBefore + ")");
		}

		if (marioModeBefore > child.levelScene.getMarioMode())
		{
			System.out.println("expand | Mario changed mode after tick" );
		}
//		if (marioDeadAfter)
//		{
//			System.out.println("Mario died in EXPAND");
//		}
//		if (marioDeadBefore)
//		{
//			System.out.println("Mario is already dead before simulating | " + levelSceneClone.getMarioStatus());
//		}
//		if (marioPosBefore != marioPosAfter)
//		{
//			System.out.println("Mario moved in EXPAND");
//		}
		

//		 TEST/DEBUG
		if (child.levelScene.getMarioMode() != v.levelScene.getMarioMode())
		{
			System.out.println("in expand | Mario was hit since parent");
		}
		
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

		// Get Mario's starting x and mode (fire, large, small)
		float firstMarioX = levelSceneClone.mario.x;
		int firstMarioMode = levelSceneClone.getMarioMode();
		int parentMarioMode = v.parent.levelScene.getMarioMode();
		boolean marioDeadBefore = levelSceneClone.getMarioStatus() == Mario.STATUS_DEAD;
		
		
		ArrayList<boolean[]> actionsToSimulate = new ArrayList<boolean[]>();
		
		// Advance levelScene using random possible actions until maxTicks budget is reached.
		int i = 0;
		while (i < maxTicks)
		{
			i++;
			// Get random possible action.
			boolean[] randomAction = v.getRandomAction();
			actionsToSimulate.add(randomAction);
			levelSceneClone.advanceStep(randomAction);
			
			// If game state is terminal after ticking, break out.
			if (isTerminalState(levelSceneClone))
			{
				break;
			}
		}
		
		// TEST/DEBUG: Check if Mario dies or loses Mode after simulating.
//		boolean marioDeadAfter = levelSceneClone.getMarioStatus() == Mario.STATUS_DEAD;
		
//		if (marioDeadBefore != marioDeadAfter)
//		{
////			System.out.println("Mario died while simulating in DEFAULT");
//		}

		
		//TEST/DEBUG
//		if (firstMarioMode != parentMarioMode)
//		{
//			System.out.println("Mario changed MODE since parent state in DEFAULT");
//		}
//		System.out.println("parent, first, current = " + parentMarioMode + "," + firstMarioMode + "," + levelSceneClone.getMarioMode());
		
//		System.out.println("DefaultPolicy mode after " + (i+1) + " ticks: " + levelSceneClone.getMarioMode());
		
		// Get reward for current state.
		float reward = calculateReward(levelSceneClone, firstMarioX, firstMarioMode, i, actionsToSimulate);
		
		//TEST/DEBUG - check Mario's and enemies' positions after step
//		for (int j = 0; j < levelSceneClone.getEnemiesFloatPos().length; j += 3) {
//			System.out.println("Simulated Enemy at: " + levelSceneClone.getEnemiesFloatPos()[j+1] 
//					+ " , " + levelSceneClone.getEnemiesFloatPos()[j+2]);
//		}
//		System.out.println("Simulated Mario at: " + levelSceneClone.getMarioFloatPos()[0]
//				+ " , " + levelSceneClone.getMarioFloatPos()[1]);
		//TEST/DEBUG
//		String actionsSimulated = "";
//		for (boolean[] a : actionsToSimulate){
//			actionsSimulated += actionAsString(a);}
//		if (true) System.out.println("In defaultPolicy | After simulating " + i + " steps after action " 
//		+ actionAsString(v.parentAction) + ": " + actionsSimulated + ". Reward = " + reward); //TEST/DEBUG
		return reward;
	}
	

	
//	/**
//	 * Checks if Mario has been hit by an enemy between the two given states.
//	 * Mario modes: small = 0, large = 1, fire = 2.
//	 * @param previous
//	 * @param current
//	 * @return
//	 */
//	private boolean wasMarioHit(LevelScene current, LevelScene previous)
//	{
//		if (current.getMarioMode() < previous.getMarioMode())
//		{
//			return true;
//		}
//		return false;
//	}
	
	/**
	 * Checks the current state of Mario in the given LevelScene, and calculates the reward based on the state.
	 * Mario is currently rewarded for running to the right and winning.
	 * TODO: Consider punishment for mario shrinking (i.e. hitting enemies without dying)
	 * TODO: Consider checking gaps and punish jumping to a gap.
	 * TODO: Is it viable to always assume that max number of units moved is 11? Obviously this only holds for unobstructed
	 * parts of the level.
	 * @param levelScene
	 * @param ticksSimulated How many ticks were simulated in default policy before terminating.
	 * @return The reward for the current state.
	 */
	private float calculateReward(LevelScene levelScene, float marioFirstX, int marioFirstMode, int ticksSimulated, ArrayList<boolean[]> actionsSimulated)
	{
		// If Mario is dead
		if (levelScene.getMarioStatus() == Mario.STATUS_DEAD)
		{
			return 0;
		}
		// If Mario was hit without dying
		if (levelScene.getMarioMode() < marioFirstMode)
		{
			System.out.println("Mario Mode change in CALCULATEREWARD. | InvulnerableTime = " + levelScene.mario.invulnerableTime);
			String actionsSimulatedString = "";
			for (boolean[] a : actionsSimulated){
				actionsSimulatedString += actionAsString(a);}
			if (true) System.out.println("In defaultPolicy | After simulating " + ticksSimulated + " steps"
					 + ": " + actionsSimulatedString + ". Reward = " + 0);
			return 0;
		}
		// If mario completed the level
		if (levelScene.getMarioStatus() == Mario.STATUS_WIN)
		{
			System.out.println("WON in simulation");
			return 1;
		}
		/* If simulation terminated before reaching terminal state, we approximate the reward.
		 * The maximum number of units Mario can run to the right is about 11. We normalize the achieved distance 
		 * by the max distance, given as 11 * ticks simulated.
		 */
		float distanceCovered = levelScene.mario.x - marioFirstX;
		float reward = 0;
		if (distanceCovered >= 0)
		{
			// Minimum reward is 0.1f so going left is never more desirable than going right unless it's to avoid death.
			reward = 0.1f + (distanceCovered/(11*ticksSimulated));
		}
		else
		{
			// If Mario has not made any progress to the right, reward is small.
			reward = 0.1f;
		}
				
		if (Util.lcaDebug)System.out.println("In calculateReward | FirstX = " + marioFirstX + ", currentX = " + levelScene.mario.x);
		if (Util.lcaDebug)System.out.println("In calculateReward | Distance covered = " + distanceCovered);
//		System.out.println();
		
		return reward;
	}
	
	
	/**
	 * Backpropagate the given reward of Node v through all its ancestors in the tree.
	 * @param v
	 * @param reward
	 */
	private void backpropagate(Node v, float reward)
	{
		Node current = v;
		// Update all statistics of this node and all its parents (including root even tho its pointless)
		while (current != null)
		{
			current.timesVisited++;
			current.reward += reward;
			current = current.parent;
		}
	}
	
	/**
	 * Gets the child with the highest UCT value.
	 * @param v
	 * @param c Exploration coefficient
	 * @return The child of v with the highest UCT value.
	 */
	private Node getBestChild(Node v, float c)
	{
		Node bestChild = null;

		// Find the child with the highest UCT value.
		float maxUCT = 0;
		for (Node child : v.children)
		{
			float thisUCT = calculateUCTValue(child, c);
			
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
	 * Calculates the UCT value for the Node n given the exploration coefficient c.
	 * The UCT value represents how interesting a node is to explore.
	 * @param n
	 * @param c exploration coefficient
	 * @return UCT value for Node n
	 */
	private float calculateUCTValue(Node n, float c)
	{
		// Unvisited children should be assigned highest possible value to ensure that all children are considered at least once (Browne et al. 2012)
		if (n.timesVisited < CONFIDENCE_THRESHOLD)
		{
			return Float.MAX_VALUE - (rng.nextFloat() * tieBreaker);
		}
		
		// Approximation of the node's game-theoretic value (Browne et al. 2012)
		float valueTerm = n.reward/n.timesVisited;
		
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
