/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.idsia.scenarios;

import java.io.*;
import java.util.ArrayList;

import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.MarioCustomSystemOfValues;
import ch.idsia.tools.MarioAIOptions; /**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 5, 2009
 * Time: 12:46:43 PM
 */
import ch.idsia.utils.statistics.StatisticalTests;
import ch.idsia.utils.statistics.Stats;
import mctsMario.BabbysFirstMarioAgent;
import mctsMario.Util;

/**
 * The <code>Play</code> class shows how simple is to run a MarioAI Benchmark.
 * It shows how to set up some parameters, create a task,
 * use the CmdLineParameters class to set up options from command line if any.
 * Defaults are used otherwise.
 *
 * @author Julian Togelius, Sergey Karakovskiy
 * @version 1.0, May 5, 2009
 */

public final class Play
{
	/* Fields related to testing BabbysFirstMario */
	
	// Number of different levels to test.
	static int levels = 2;
	
	// Number of times to repeat each level before getting the score.
	static int repetitionsPerLevel = 5;
	
/**
 * <p>An entry point of the class.</p>
 *
 * @param args input parameters for customization of the benchmark.
 * @see ch.idsia.scenarios.oldscenarios.MainRun
 * @see ch.idsia.tools.MarioAIOptions
 * @see ch.idsia.benchmark.mario.simulation.SimulationOptions
 * @since MarioAI-0.1
 */
public static void main(String[] args)
{
    final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
//    System.out.println(marioAIOptions.getAgentFullLoadName());
    marioAIOptions.setAgent(new BabbysFirstMarioAgent());
    final BasicTask basicTask = new BasicTask(marioAIOptions);
    
    // Options for the test
//    marioAIOptions.setVisualization(true);
    marioAIOptions.setFlatLevel(false);
    marioAIOptions.setLevelDifficulty(2);
    marioAIOptions.setLevelRandSeed(10);
    marioAIOptions.setLevelType(0);
    marioAIOptions.setFPS(100);
    
    final MarioCustomSystemOfValues m = new MarioCustomSystemOfValues();
//    basicTask.runSingleEpisode(2);
    // run 1 episode with same options, each time giving output of Evaluation info.
    // verbose = false
    
    // Lists containing the scores of the agent being tested. 
    // Every element in the list represents the results for one level
    // so index 0 contains a float containing the distance reward for every run of level 0
    ArrayList<float[]> distanceScoresPerLevel = new ArrayList<float[]>();
    ArrayList<float[]> timeLeftScoresPerLevel = new ArrayList<float[]>();
    
    for (int i = 0; i < levels; i++) 
    {
    	// Set level
    	marioAIOptions.setLevelRandSeed(i);
    	
    	// Array to hold the distance achieved for every repetition of the current level.
    	float[] distances = new float[repetitionsPerLevel];
    	float[] times = new float[repetitionsPerLevel];
    	
    	for (int j = 0; j < repetitionsPerLevel; j++) 
    	{
    		System.out.println("Running seed " + i + ", round " + j);
    		basicTask.runSingleEpisode(1);
    		
    		float distancePassed = basicTask.getEvaluationInfo().computeDistancePassed();
    		distances[j] = distancePassed;
    		
    		float timeLeft = (float) basicTask.getEvaluationInfo().timeLeft;
    		times[j] = timeLeft;
    	}
    	distanceScoresPerLevel.add(distances);
    	timeLeftScoresPerLevel.add(times);
	}
    
    ArrayList<Double> allDistances = new ArrayList<Double>();
    for (float[] level : distanceScoresPerLevel) {
    	for (float f : level) {
    		allDistances.add((double)f);
		}
	}
    
    ArrayList<Double> allTimes = new ArrayList<Double>();
    for (float[] level : timeLeftScoresPerLevel) {
    	for (float f : level) {
    		allTimes.add((double)f);
		}
	}
    
    // Convert to double[] array so it can be used for stats...
    double[] allDistancesArray = new double[allDistances.size()];
	double[] allTimesArray = new double[allTimes.size()];
    for (int i = 0; i < allDistancesArray.length; i++) {
		allDistancesArray[i] = allDistances.get(i);
		allTimesArray[i] = allTimes.get(i); // We can do this since the two lists are always same size.
	}
    
    
    
    // Calculate overall mean
//    double mean = Stats.mean(allDistancesArray);
//    double variance = Stats.variance(allDistancesArray);
//    double stdDev = Stats.sdev(allDistancesArray);
//    double stdErr = Stats.stderr(allDistancesArray);
    
    String agentName = marioAIOptions.getAgent().getName();
    // Print distance results.
    writeResults(distanceScoresPerLevel, agentName, " distance", Util.paramsAsString(), Stats.mean(allDistancesArray),
    		Stats.variance(allDistancesArray), Stats.sdev(allDistancesArray), Stats.stderr(allDistancesArray));
   
    // Print timeLeft results.
    writeResults(timeLeftScoresPerLevel, agentName, " timeLeft", Util.paramsAsString(), Stats.mean(allTimesArray), 
    		Stats.variance(allTimesArray), Stats.sdev(allTimesArray), Stats.stderr(allTimesArray));
    
    System.out.println("Test finished - exiting.");
    System.exit(0);
}

/**
 * Writes one file with scores formatted by levels, plus information regarding parameters and statistics.
 * Writes another file with just the scores obtained.
 */
public static void writeResults(ArrayList<float[]> results, String agentName, String suffix, String params, double mean, double variance, double stdDev, double stdErr)
{
	String fileName = agentName + suffix + " stats.txt";
    String fileName2 = agentName + suffix + " stats clean.txt";
    try{
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
    BufferedWriter writerClean = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName2), "utf-8"));

    writer.write("Agent: " + agentName + ", " + params); writer.newLine();
    writer.write("Mean = " + mean); writer.newLine();
    writer.write("Variance = " + variance); writer.newLine();
    writer.write("Standard deviation = " + stdDev); writer.newLine();
    writer.write("Standard error = " + stdErr); writer.newLine();
    
    for(int i = 0; i < results.size(); i++)
	{
		writer.write(agentName + " - LevelSeed " + i);
		writer.newLine();
		for (float distanceScore : results.get(i)) {
			writer.write(Float.toString(distanceScore));
			writer.newLine();
			
			writerClean.write(Float.toString(distanceScore));
			writerClean.newLine();
		}
	}
	writer.close();
	writerClean.close();
    } catch (IOException e){
    	e.printStackTrace();
    }
}
}
