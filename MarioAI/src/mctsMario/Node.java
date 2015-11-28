package mctsMario;

import java.util.ArrayList;

public class Node 
{
	public ArrayList<Node> children;
	public Node parent;
	
	public LevelScene levelScene;
	public boolean[] parentAction;
	
	public int timesVisited = 0;
	public int reward = 0;
	
	
	public Node(LevelScene levelScene, Node parent)
	{
		this.levelScene = levelScene;
		this.parent = parent;
	}
}
