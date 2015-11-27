package mctsMario;

import java.util.ArrayList;

public class Node 
{
	public ArrayList<Node> children;
	public Node parent;
	
	public LevelScene state;
	public boolean[] parentAction;
	
	public int timesVisited = 0;
	public int reward = 0;
	
	
	public Node(LevelScene state, Node parent)
	{
		this.state = state;
		this.parent = parent;
	}
	
	/**
	 * 
	 * @return Whether or not every possible action has been added as a child to this node.
	 */
	public boolean isFullyExpanded()
	{
		//TODO
		return true;
	}
}