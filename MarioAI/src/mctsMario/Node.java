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
	
	public ArrayList<boolean[]> actions = new ArrayList<boolean[]>();
	
	private Random rng = new Random();
	
	
	// TEST
	public Node firstParent = null;
	
	
	public Node(LevelScene levelScene, Node parent)
	{
		this.levelScene = levelScene;
		this.parent = parent;
		
		actions = createActions();
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
			+ ", children: " + child.children.size()
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
	 * @return Random action with 75% chance to be an action leading up or right.
	 */
	public boolean[] getRandomActionBiased()
	{
		if (rng.nextInt(100) < 75)
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
	
	//TEST/DEBUG
	/**
	 * @return
	 */
	public boolean[] getRightAction()
	{
		boolean[] action = new boolean[6];
		
		action[Mario.KEY_LEFT] = false;
		action[Mario.KEY_RIGHT] = true;
		action[Mario.KEY_DOWN] = false;
    	action[Mario.KEY_JUMP] = false;
    	action[Mario.KEY_SPEED] = false;
    	action[Mario.KEY_UP] = false;
		
		return action;
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
		
		possibleActions.add(createAction(false, false, false, false, false, false)); // Do nothing

//		possibleActions.add(createAction(false, false, true, false, false, false)); // Down
//		
//		possibleActions.add(createAction(false, false, false, true, false, false)); // Jump
//		
//		possibleActions.add(createAction(false, true, false, false, false, false)); // Right
//		possibleActions.add(createAction(false, true, false, false, true, false)); // Right + run
//		possibleActions.add(createAction(false, true, false, true, false, false)); // Right + jump
//		possibleActions.add(createAction(false, true, false, true, true, false)); // Right + jump + run
//		
//		possibleActions.add(createAction(true, false, false, false, false, false)); // Left
//		possibleActions.add(createAction(true, false, false, false, true, false)); //Left + run
//		possibleActions.add(createAction(true, false, false, true, false, false)); // Left + jump
//		possibleActions.add(createAction(true, false, false, true, true, false)); //Left + jump + run
		
		return possibleActions;
	}
	
	/**
	 * @param left
	 * @param right
	 * @param down
	 * @param jump
	 * @param run
	 * @return A combination of button presses making up an action, represented by a boolean array.
	 */
	public boolean[] createAction(boolean left, boolean right, boolean down, boolean jump, boolean run, boolean up)
	{
		boolean[] action = new boolean[6];
		action[Mario.KEY_LEFT] = left;
		action[Mario.KEY_RIGHT] = right;
		action[Mario.KEY_DOWN] = down;
    	action[Mario.KEY_JUMP] = jump;
    	action[Mario.KEY_SPEED] = run;
    	action[Mario.KEY_UP] = up;
    	
    	return action;
	}
	
	/**
	 * @return String of the combination of buttons represented by parent action.
	 */
	private String parentActionAsString()
	{
		String s = "[" 
				+ (parentAction[Mario.KEY_LEFT] ? "l" : "")
				+ (parentAction[Mario.KEY_RIGHT] ? "r" : "")
				+ (parentAction[Mario.KEY_DOWN] ? "d" : "") 
				+ (parentAction[Mario.KEY_JUMP] ? "j" : "")
				+ (parentAction[Mario.KEY_SPEED] ? "s" : "")
				+ (parentAction[Mario.KEY_UP] ? "u" : "") + "]";
		
		return s;
	}
}
