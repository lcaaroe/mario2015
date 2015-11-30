package mctsMario;

import java.util.ArrayList;

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
	
	
	public Node(LevelScene levelScene, Node parent)
	{
		this.levelScene = levelScene;
		this.parent = parent;
		
		actions = createActions();
	}
	
	/**
	 * Returns a list of all viable combinations of button presses. Doesn't care which actions are actually
	 * possible.
	 * @return List of all viable button combinations (excluding the up and down buttons).
	 */
	private ArrayList<boolean[]> createActions()
	{
		ArrayList<boolean[]> possibleActions = new ArrayList<boolean[]>();
		
		possibleActions.add(createAction(false, false, false, false, false, false)); // Do nothing
		
		possibleActions.add(createAction(false, false, false, true, false, false)); // Jump
		
		possibleActions.add(createAction(false, true, false, false, false, false)); // Right
		possibleActions.add(createAction(false, true, false, false, true, false)); // Right + run
		possibleActions.add(createAction(false, true, false, true, false, false)); // Right + jump
		possibleActions.add(createAction(false, true, false, true, true, false)); // Right + jump + run
		
		possibleActions.add(createAction(true, false, false, false, false, false)); // Left
		possibleActions.add(createAction(true, false, false, false, true, false)); //Left + run
		possibleActions.add(createAction(true, false, false, true, false, false)); // Left + jump
		possibleActions.add(createAction(true, false, false, true, true, false)); //Left + jump + run
		
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
	private boolean[] createAction(boolean left, boolean right, boolean down, boolean jump, boolean run, boolean up)
	{
		boolean[] action = new boolean[6];
    	action[Mario.KEY_DOWN] = down;
    	action[Mario.KEY_JUMP] = jump;
    	action[Mario.KEY_LEFT] = left;
    	action[Mario.KEY_RIGHT] = right;
    	action[Mario.KEY_SPEED] = run;
    	action[Mario.KEY_UP] = up;
    	
    	return action;
	}
	
	
	
	public boolean[] createInvalidAction()
	{
		boolean[] action = null;
		
		return action;
	}
	
	/**
	 * @return String of the combination of buttons represented by parent action.
	 */
	public String parentActionAsString()
	{
		String s = "[" 
				+ (parentAction[Mario.KEY_DOWN] ? "d" : "") 
				+ (parentAction[Mario.KEY_RIGHT] ? "r" : "")
				+ (parentAction[Mario.KEY_LEFT] ? "l" : "")
				+ (parentAction[Mario.KEY_JUMP] ? "j" : "")
				+ (parentAction[Mario.KEY_SPEED] ? "s" : "") + "]";
		
		return s;
	}
	
	/**
	 * @return String of this node's children. Each child on a new line.
	 */
	public String childrenAsString()
	{
		String s = "[";
		
		for (int i = 0; i < children.size(); i++) 
		{
			Node child = children.get(i);
			s += "Action: " + child.parentActionAsString()
			+ ", visited: " + child.timesVisited
			+ ", reward: " + child.reward
			+ ", reward/visits: " + (child.reward/child.timesVisited)
			+ ", children: " + child.children.size()
			+ "]";
			
			if (child.reward/child.timesVisited > 1)
			{
				s+= "WARNING";
			}
			
			if (i != children.size()-1)
			{
				s+= '\n';
			}
			
			
		}

		return s;
	}
}
