package mctsMario;

import java.util.Random;

public class mcts 
{
	// Max allowed time (in ms) to run the search. Algorithm needs a little time to select best child and exit.
	private int timeLimit = 19;
	
	// Exploration coefficient (default ~0.707107...)
	private static final float C = (float) (1.0/Math.sqrt(2));
	
	// The minimum number of visits every node should have before it will be rated by UCT.
	private static final int CONFIDENCE_THRESHOLD = 1;
	
	private static final int MAX_TICKS = 5;
	
	// Small float to break ties between equal UCT values.
	// Idea of tiebreaker inspired by http://mcts.ai/code/java.html
	private Random rng = new Random();
	private float tieBreaker = (float)1e-6;
	
	
	/**
	 * 
	 * @param levelScene The current real-game levelscene
	 * @return The best action found using MCTS
	 */
	public boolean[] search(LevelScene levelScene)
	{
		Node rootNode = new Node(levelScene, null);
		
		while (System.currentTimeMillis() < System.currentTimeMillis() + timeLimit)
		{
			Node v1 = treePolicy(rootNode);
			
			float reward = defaultPolicy(v1, MAX_TICKS);
			
			backpropagate(v1, reward);
		}
		
		// Get child with the highest reward (by using value of 0 for C).
		Node bestChild = getBestChild(rootNode, 0);

		// Return action corresponding to best child.
		return bestChild.parentAction;
	}
	
	/**
	 * Selects the next node to simulate from.
	 * @param v
	 * @return 
	 */
	private Node treePolicy(Node v)
	{
		while (!isTerminalState(v))
		{
			// if v is not fully expanded, find and add new child.
			if (!v.isFullyExpanded())
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
	 * TODO
	 * Get an untried action from state represented by this node, and add it as a child to this node.
	 * @param v
	 * @return The child representing the untried action of Node v.
	 */
	private Node expand(Node v)
	{
		// Get untried action (assumed that Node v is not fully expanded)
		boolean[] action = getUntriedAction(v);
		
		// Create new child representing the world state after performing the untried action.
		// TODO: Advance a levelscene clone performing untried action
		
		// TODO: Create child node with parentAction = action and state = levelscene clone
		
		// TODO: Return child
		return v;
	}
	
	
	/**
	 * TODO
	 * Gets an untried action from Node v.
	 * @param v
	 * @return An untried action from Node v.
	 */
	private boolean[] getUntriedAction(Node v)
	{
		
		boolean[] action = new boolean[6];
		return action;
	}
	
	/**
	 * Checks whether the node represents a state terminal game state (e.g. Mario dead or won).
	 * @param v
	 * @return
	 */
	private boolean isTerminalState(Node v)
	{
		return true;
	}
	
	
	/**
	 * TODO
	 * Simulate playout from the state of Node v and return the reward obtained.
	 * @param v
	 * @param maxTicks The max number of ticks to simulate before terminating
	 *  (since it is unlikely that we reach a terminal state after few moves)
	 * @return Reward after playout terminates.
	 */
	private float defaultPolicy(Node v, int maxTicks)
	{
		// TODO: Clone levelscene
		// TODO: Advance levelScene using random possible actions until maxTicks budget is reached.
		// TODO: Calculate the reward when playout is finished (e.g. based on how far mario got)
		return 0;
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
