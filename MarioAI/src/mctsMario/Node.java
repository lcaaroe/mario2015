package mctsMario;

import java.util.ArrayList;

import mctsMario.sprites.Mario;

public class Node 
{
	public ArrayList<Node> children;
	public Node parent;
	
	public LevelScene levelScene;
	public boolean[] parentAction;
	
	public int timesVisited = 0;
	public int reward = 0;
	
	public ArrayList<boolean[]> actions;
	
	
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
		
		possibleActions.add(createAction(false, false, false, true, false)); // Jump
//		possibleActions.add(createAction(false, false, false, true, true)); // Jump + run
		
		possibleActions.add(createAction(false, true, false, false, false)); // Right
		possibleActions.add(createAction(false, true, false, false, true)); // Right + run
		possibleActions.add(createAction(false, true, false, true, false)); // Right + jump
		possibleActions.add(createAction(false, true, false, true, true)); // Right + jump + run
		
		possibleActions.add(createAction(true, false, false, false, false)); // Left
		possibleActions.add(createAction(true, false, false, false, true)); //Left + run
		possibleActions.add(createAction(true, false, false, true, false)); // Left + jump
		possibleActions.add(createAction(true, false, false, true, true)); //Left + jump + run
		
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
	private boolean[] createAction(boolean left, boolean right, boolean down, boolean jump, boolean run)
	{
		boolean[] action = new boolean[5];
    	action[Mario.KEY_DOWN] = down;
    	action[Mario.KEY_JUMP] = jump;
    	action[Mario.KEY_LEFT] = left;
    	action[Mario.KEY_RIGHT] = right;
    	action[Mario.KEY_SPEED] = run;
    	
    	return action;
	}
}
