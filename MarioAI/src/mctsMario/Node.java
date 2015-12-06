package mctsMario;

import java.util.ArrayList;
import java.util.Random;

import mctsMario.sprites.Mario;

public class Node 
{
	public ArrayList<Node> children = new ArrayList<Node>();
	public Node parent;
	
	public LevelScene levelScene;
	public boolean[] parentAction;
	
	public int timesVisited = 0;
	public float reward = 0;
	public float maxReward = 0;
	
	public ArrayList<boolean[]> actions = new ArrayList<boolean[]>();
	
 	private Random rng = new Random();
	
	
	// TEST
	public Node firstParent = null;
	public int descendants = 0;
	
	
	public Node(LevelScene levelScene, Node parent)
	{
		this.levelScene = levelScene;
		this.parent = parent;
		
		updateDescendants();
		
		actions = createActions();
	}
	
	private void updateDescendants()
	{
		Node v = this.parent;
		while (v != null)
		{
			v.descendants++;
			v = v.parent;
		}
	}
	
	/**
	 * @return String of this node's children. Each child on a new line.
	 */
	public String childrenAsString()
	{
		String s = "";
		
		for (int i = 0; i < children.size(); i++) 
		{
			Node child = children.get(i);
			s += "[Action: " + child.parentActionAsString()
			+ ", visited: " + child.timesVisited
			+ ", reward: " + child.reward
			+ ", reward/visits: " + (child.reward/child.timesVisited)
			+ ", descendants: " + child.descendants
			+ "]";
			
			// Add newline between each child.
			if (i != children.size()-1)
			{
				s+= '\n';
			}
		}

		return s;
	}
	
	/**
	 * @return One-line string of this node's children.
	 */
	public String childrenAsStringCondensed()
	{
		String s = "";
		
		for (int i = 0; i < children.size(); i++) 
		{
			Node child = children.get(i);
			s += "[" + child.parentActionAsString()
			+ ", v: " + child.timesVisited
			+ ", r: " + child.reward
			+ "]";
			
			// Add space between each child.
			if (i != children.size()-1)
			{
				s+= " ";
			}
		}
		return s;
	}
	
	/**
	 * @return Random action 
	 */
	public boolean[] getRandomAction()
	{
		boolean[] action = actions.get(rng.nextInt(actions.size()));
		
		return action;
	}
	
	/**
	 * @return Random action with 80% chance to be an action leading up or right.
	 */
	public boolean[] getRandomActionBiased()
	{
		if (rng.nextInt(100) < 80)
		{
			// Action indices 0-4 possible actions are right and/or jump.
			return actions.get(rng.nextInt(5));
		}
		else
		{
			// Action indices 5-8 are left and/or jump.
			
			return actions.get(rng.nextInt(4)+5);
		}
	}
	
	
	// -- Private methods --
	/**
	 * Returns a list of all viable combinations of button presses. Doesn't care which actions are actually
	 * possible.
	 * @return List of all viable button combinations (excluding the up and down buttons).
	 */
	private ArrayList<boolean[]> createActions()
	{
		ArrayList<boolean[]> possibleActions = new ArrayList<boolean[]>();
		

//		possibleActions.add(createAction(false, false, false, false, false, false)); // Do nothing

		// Useless test actions
//		possibleActions.add(createAction(false, false, true, false, false, false)); // Down
//		possibleActions.add(createAction(true, true, false, false, false, false)); // Right + Left
//		possibleActions.add(createAction(false, false, false, false, false, true)); // Up
//		possibleActions.add(createAction(false, false, true, false, false, true)); // Up + down
//		possibleActions.add(createAction(false, false, false, false, true, false)); // Shoot
		// Useless actions end
//		
		possibleActions.add(Util.createAction(false, false, false, true, false, false)); // Jump
//	
		possibleActions.add(Util.createAction(false, true, false, false, false, false)); // Right
		possibleActions.add(Util.createAction(false, true, false, false, true, false)); // Right + run
		possibleActions.add(Util.createAction(false, true, false, true, false, false)); // Right + jump
		possibleActions.add(Util.createAction(false, true, false, true, true, false)); // Right + jump + run
////	
		possibleActions.add(Util.createAction(true, false, false, false, false, false)); // Left
		possibleActions.add(Util.createAction(true, false, false, false, true, false)); //Left + run
		possibleActions.add(Util.createAction(true, false, false, true, false, false)); // Left + jump
		possibleActions.add(Util.createAction(true, false, false, true, true, false)); //Left + jump + run
		
		return possibleActions;
	}
	

	
	/**
	 * @return String of the combination of buttons represented by parent action.
	 */
	private String parentActionAsString()
	{
		return Util.actionAsString(parentAction);
	}
	
	
}
