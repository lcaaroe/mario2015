package mctsMario;

import java.util.ArrayList;
import java.util.Random;

import mctsMario.sprites.Mario;

public class MCTS 
{
	// Max allowed time (in ms) to run the search. Algorithm needs a little time to select best child and exit.
	private int timeLimit = 10;
	
	// Exploration coefficient (default ~0.707107...)
	// "the value (...) was shown to satisfy the Hoeffding ineqality with rewards in the range [0,1]" (Browne et al., 2012)
	private static final float C = (float) (1.0/Math.sqrt(2));
	
	// The minimum number of visits every node should have before it will be rated by UCT.
	private static final int CONFIDENCE_THRESHOLD = 1;
	
	private static final int MAX_SIMULATION_TICKS = 4;
	
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
//		System.out.println("In main search | rootNode children: " + rootNode.childrenAsString());
//		if (true)System.out.println("In main search | Best child with parent action = " + bestChild.parentActionAsString()); // TEST/DEBUG
//		System.out.println();
//		if (Util.lcaDebug)System.out.println("In main search | bestChild.ParentAction.length = " + bestChild.parentAction.length);
		return bestChild.parentAction;
	}
	
	/**
	 * Selects the next node to simulate from.
	 * @param v
	 * @return 
	 */
	private Node treePolicy(Node v)
	{
		treePolicyCounter++; // TEST/DEBUG
//		System.out.println("TreePolicy counter = " + treePolicyCounter); // TEST/DEBUG
		while (!isTerminalState(v.levelScene))
		{
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
		
		// TEST/DEBUG
//		String s = "[" 
//				+ (untriedAction[Mario.KEY_DOWN] ? "d" : "") 
//				+ (untriedAction[Mario.KEY_RIGHT] ? "r" : "")
//				+ (untriedAction[Mario.KEY_LEFT] ? "l" : "")
//				+ (untriedAction[Mario.KEY_JUMP] ? "j" : "")
//				+ (untriedAction[Mario.KEY_SPEED] ? "s" : "") + "]";
//		
//		if (!s.equals("[]"))
//		{
//			System.out.println("Untried action selected: " + s);
//			
//		}
		
		// Clone Node v's levelScene.
		LevelScene levelSceneClone = null;
		try{
			levelSceneClone = (LevelScene) v.levelScene.clone();
		}catch (CloneNotSupportedException e){
			e.printStackTrace();
		}
		
		// Advance the levelScene 1 tick with the new action. 
		// This child then represents the world state after taking that action.
		levelSceneClone.advanceStep(untriedAction);
		
		// Create new child representing the world state after performing the untried action.
		Node child = new Node(levelSceneClone, v);
		child.parentAction = untriedAction;
		if (!child.parentActionAsString().equals("[]"))
		{
			
//			System.out.println("Child added with action = " + child.parentActionAsString());
		}

		v.children.add(child);
		
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
		
//		String s= "[" //TEST/DEBUG
//				+ (a[Mario.KEY_DOWN] ? "d" : "") 
//				+ (a[Mario.KEY_RIGHT] ? "r" : "")
//				+ (a[Mario.KEY_LEFT] ? "l" : "")
//				+ (a[Mario.KEY_JUMP] ? "j" : "")
//				+ (a[Mario.KEY_SPEED] ? "s" : "") + "]";
//		System.out.println("Untried Action: " + s + " is empty?");
		
		// If the untried action returned is a 'do-nothing' action (all buttons false), then no untried actions exist.
		for (int i = 0; i < a.length; i++) {
			if (a[i] == true)
			{
//				System.out.println("False!"); //TEST/DEBUG
//				System.out.println(); //TEST/DEBUG
				return false;
			}
		}
//		System.out.println("True!"); //TEST/DEBUG
//		System.out.println(); //TEST/DEBUG
		// If no buttons in the actions are set to true, this is a do-nothing action, and node is fully expanded.
		return true;
		
	}
	
	
	/**
	 * Gets an untried action from Node v.
	 * @param v
	 * @return An untried action from Node v. Returns 'do-nothing' action if no untried actions exist.
	 */
	private boolean[] getUntriedAction(Node v)
	{
		// TEST/DEBUG prints
//		if (v == null) 
//		{
//			System.out.println("getUntriedAction: v == null");
//		}
//		
//		if (v.children == null) 
//		{
//			System.out.println("getUntriedAction: v.children == null");
//		}
//		else
//		{
//			System.out.println("getUntriedAction: v.children.size = " + v.children.size());
//		}
//		if (v.actions == null) 
//		{
//			System.out.println("getUntriedAction: v.actions == null");
//		}
//		else
//		{
//			System.out.println("getUntriedAction: v.actions.size = " + v.actions.size());
//		}
		outer:
		for (boolean[] possibleAction : v.actions) 
		{
			for (Node child : v.children) 
			{
				if (child.parentAction == possibleAction)
				{
					// A child representing this action already exist, so continue to next move.
					continue outer;
				}
			}
			// No child found representing this action, so it is untried.
			return possibleAction;
		}
		
		// If no untried actions exist, return a 'do-nothing' action. (Or null action?)
		boolean[] action = new boolean[6];
		for (int i = 0; i < action.length; i++) {
			action[i] = false;
		}
		return action;
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
		
		// Get random possible action.
		boolean[] randomAction = v.actions.get(rng.nextInt(v.actions.size()));
		
		// Get Mario's starting x and mode (fire, large, small)
		float marioFirstX = levelSceneClone.mario.x;
		int marioFirstMode = levelSceneClone.getMarioMode();
		
		// Advance levelScene using random possible actions until maxTicks budget is reached.
		int i = 0;
		while (i < maxTicks)
		{
			levelSceneClone.advanceStep(randomAction);
			
			// If game state is terminal after ticking, break out.
			if (isTerminalState(levelSceneClone))
			{
				break;
			}
			i++;
		}
			
		// Get reward for current state.
		float reward = calculateReward(levelSceneClone, marioFirstX, marioFirstMode, i);
		
		if (Util.lcaDebug) System.out.println("In defaultPolicy | After simulation. Reward = " + reward); //TEST/DEBUG
		return reward;
	}
	
	/**
	 * Checks the current state of Mario in the given LevelScene, and calculates the reward based on the state.
	 * Mario is currently rewarded for running to the right and winning.
	 * TODO: How to punish running backwards?
	 * TODO: Consider reducing impact of distance achieved. (So running full distance to the right is not awarded
	 * the same as actually winning the level.)
	 * TODO: Consider adding Death/shrinking as just a multiplier to distance achieved.
	 * TODO: Consider punishment for mario shrinking (i.e. hitting enemies without dying)
	 * TODO: Consider checking gaps and punish jumping to a gap.
	 * TODO: Is it viable to always assume that max number of units moved is 11? Obviously this only holds for unobstructed
	 * parts of the level.
	 * @param levelScene
	 * @param ticksSimulated How many ticks were simulated in default policy before terminating.
	 * @return The reward for the current state.
	 */
	private float calculateReward(LevelScene levelScene, float marioFirstX, int marioFirstMode, int ticksSimulated)
	{
		// If Mario is dead
		if (levelScene.getMarioStatus() == Mario.STATUS_DEAD)
		{
			return 0;
		}
		// If mario completed the level
		if (levelScene.getMarioStatus() == Mario.STATUS_WIN)
		{
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
			reward = 0.5f + distanceCovered/(11*ticksSimulated);
		}
		else
		{
			// If Mario has not made any progress to the right, there is no reward.
			reward = 0;
		}
		// If mario shrunk (was hit by enemy without dying)
		//System.out.println(marioFirstMode+","+levelScene.getMarioStatus());
		if (levelScene.getMarioStatus() < marioFirstMode)
		{
			System.out.println("STATUS CHANGE");
			reward = reward * 0.5f;
		}
				
		if (Util.lcaDebug)System.out.println("In calculateReward | FirstX = " + marioFirstX + ", currentX = " + levelScene.mario.x);
		if (Util.lcaDebug)System.out.println("In calculateReward | Distance covered = " + distanceCovered);
		
		return reward;
	}
	
	
	/**
	 * Backpropagate the given reward of Node v through all its ancestors in the tree.
	 * @param v
	 * @param reward
	 */
	private void backpropagate(Node v, float reward)
	{
		// Update all statistics of this node and all its parents (including root even tho its pointless)
		while (v != null)
		{
			v.timesVisited++;
			v.reward += reward;
			v = v.parent;
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
		float explorationTerm = (float) Math.sqrt((2 * Math.log(n.parent.timesVisited))/n.timesVisited);
		
		
		return valueTerm + c * explorationTerm + (rng.nextFloat() * tieBreaker);
	}
}
