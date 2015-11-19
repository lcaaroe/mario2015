package mctsMario;

import java.util.ArrayList;

import mctsMario.baumgarten.LevelScene;

public class Node 
{
	public ArrayList<Node> children;
	public Node parent;
	
	public LevelScene state;
	
	public int timesVisited = 0;
	public int reward = 0;
	
	
	public Node(LevelScene state, Node parent)
	{
		this.state = state;
		this.parent = parent;
	}
}
