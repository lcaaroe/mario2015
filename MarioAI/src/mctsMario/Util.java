package mctsMario;

import mctsMario.sprites.Mario;

public class Util 
{
	public static final boolean lcaDebug = false;
	
	// For spam control
	public static final boolean ornDebug = false;
	
	
	// Max allowed time (in ms) to run the search. Algorithm needs a little time to select best child and exit.
	public static final int TIME_LIMIT = 19;
	
	// Exploration coefficient (default ~0.707107...)
	// "the value (...) was shown to satisfy the Hoeffding ineqality with rewards in the range [0,1]" (Browne et al., 2012)
	public static final float C = 0.5f;//(float) (1.0/Math.sqrt(2));
	
	// The minimum number of visits every node should have before it will be rated by UCT.
	public static final int CONFIDENCE_THRESHOLD = 1;
	
	// Number of random steps to perform when simulating in default policy.
	public static final int MAX_SIMULATION_TICKS = 8;
	
	// The number of times the same action should be repeated in a row while simulating.
	public static final int REPETITIONS = 1;
	
	// Whether to select best child based on max value (rather than average value).
	public static final boolean VALUE_BY_MAX = false;
	
	/**
	 * Gets all parameter names and their values in a one line string.
	 * @return
	 */
	public static String paramsAsString()
	{
		String s = "";
		s+= "TIME_LIMIT = " + TIME_LIMIT;
		s+= ", C = " + C;
		s+= ", CONFIDENCE_THRESHOLD = " + CONFIDENCE_THRESHOLD;
		s+= ", MAX_SIMULATION_TICKS = " + MAX_SIMULATION_TICKS;
		s+= ", REPETITIONS = " + REPETITIONS;
		s+= ", VALUE_BY_MAX = " + VALUE_BY_MAX;
		
		return s;
	}
	
	/**
	 * @param a
	 * @return Action a as string in the format [drljsu]
	 */
	public static String actionAsString(boolean[] a)
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
	
	/**
	 * @param left
	 * @param right
	 * @param down
	 * @param jump
	 * @param run
	 * @return A combination of button presses making up an action, represented by a boolean array.
	 */
	public static boolean[] createAction(boolean left, boolean right, boolean down, boolean jump, boolean run, boolean up)
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
}
