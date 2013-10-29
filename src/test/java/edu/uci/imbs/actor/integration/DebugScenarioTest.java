/* Copyright (c) 2013, Regents of the University of California.  See License.txt for details */

package edu.uci.imbs.actor.integration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.grayleaves.utility.Result;
import org.grayleaves.utility.ScenarioException;
import org.junit.Before;

import edu.uci.imbs.actor.ProtectionParameters;

public class DebugScenarioTest 
{
	private ProtectionModel<String> model;
	private FileAppender appender;
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DebugScenarioTest.class);
	@Before
	public void setUp() throws Exception
	{
		//FIXME debug statements not showing up....
		setParameters(); 
//		configureAppender();
//		BasicConfigurator.configure(); 
//		Logger.getRootLogger().setLevel(Level.DEBUG); 
        model = new ProtectionModel<String>();
	}
//	INFO utility.ScenarioLog:  Parameter point: use role shifting dynamic=true, actors who do worse shift to role of actors who do better=true, multiple bandits prey on multiple peasants=false, number of runs to reach equilibrium or stop=100, maximum population size before run stops=25000, number of consecutive periods without adjustment defines equilibrium=4, threshold at which actors survive=0.2, threshold at which actors thrive=0.3, bandit-peasant payoff discrepancy tolerance=0.01, percent of lowest surviving actors to shift roles=0.1, initial number of peasants=1500, initial number of bandits=1500, maximum number of peasants a bandit will prey upon=1, cost to bandit to prey on one peasant=0.0, use matching function to limit number of peasants bandit successfully preys on=false, matching function exponent for number of bandits=0.5, matching function exponent for number of peasants=0.5, matching function constant multiplier=1.0, gamma for contest function=0.725, random number seed=7562077152359325602 

	
	private void setParameters() 
	{
		ProtectionParameters.PROTECTION_PROPORTION_INTERVAL_SIZE = .05; 
		ProtectionParameters.PROTECTION_PROPORTION_NUMBER_INTERVALS = 21; 
		ProtectionParameters.CONTEST_FUNCTION_GAMMA = 0.625; 
		ProtectionParameters.ROLE_SHIFTING = true;
		ProtectionParameters.SURVIVE_THRESHOLD = 0.15; 
		ProtectionParameters.THRIVE_THRESHOLD = 0.3; 
		ProtectionParameters.PAYOFF_DISCREPANCY_TOLERANCE = .01; 
		ProtectionParameters.ADJUSTMENT_FACTOR_PERCENTAGE = 0.05;
		ProtectionParameters.NUMBER_PEASANTS = 1000; 
		ProtectionParameters.NUMBER_BANDITS = 1000; 
		ProtectionParameters.RUN_LIMIT = 1000; 
		ProtectionParameters.MIMIC_BETTER_PERFORMING_POPULATION = true; 
		ProtectionParameters.MAX_PEASANTS_TO_PREY_UPON = 1; 
		ProtectionParameters.MULTIPLE_BANDITS_PREY_ON_MULTIPLE_PEASANTS = false;
		ProtectionParameters.EQUILIBRIUM_NUMBER_PERIODS_WITHOUT_ADJUSTMENT = 20; 
		ProtectionParameters.MAXIMUM_POPULATION_SIZE = 25000; 
		ProtectionParameters.COST_TO_PREY_ON_SINGLE_PEASANT = 0; 
		ProtectionParameters.MATCHING_FUNCTION_ALPHA_EXPONENT = 0.5;
		ProtectionParameters.MATCHING_FUNCTION_BETA_EXPONENT = 0.5;
		ProtectionParameters.MATCHING_FUNCTION_MU = 1; 
		ProtectionParameters.BANDITS_USE_MATCHING_FUNCTION = false; 
		ProtectionParameters.NORMAL_INTERACTION_PATTERN = true; 
		ProtectionParameters.FORCE_PEASANT_ALLOCATION_TO_HIGH_LOW = false; 
		ProtectionParameters.FORCE_PEASANT_ALLOCATION_HIGH_PROPORTION = 0.7; 
		ProtectionParameters.FORCE_PEASANT_ALLOCATION_LOW_PROPORTION = 0.1; 
		ProtectionParameters.FORCE_PEASANT_ALLOCATION_LOW_INITIAL_PEASANTS = 5200; 
		ProtectionParameters.RANDOM_SEED = 1234567890123456789l; 
		ProtectionParameters.RANDOM = new Random(ProtectionParameters.RANDOM_SEED); 

	}
//	@Test
	public void debugAnIndividualParameterPoint() throws Exception
	{
		Result<String> result = model.run(); 
//		logger.info(result.getSummaryData()); 
		System.out.println(result.getSummaryData());
		writeRecordsFromLog(result.getList()); 
		for (String msg : result.getList())
		{
//			logger.info(msg); 
			System.out.println(msg);
		}
	}
	protected void configureAppender() throws ScenarioException 
	{
		File file = new File("debug.txt");
		if (file.exists()) file.delete(); 
		System.out.println(file.getAbsolutePath());
		try
		{
			appender = new FileAppender(new EnhancedPatternLayout("%p %c{2}:  %m %n"), file.getName());
			appender.setName(this.toString());
		} 
		catch (IOException e)
		{
			throw new ScenarioException("ScenarioException:  IOException in ScenarioLog.configureAppender:  "+e.getMessage()); 
		}
		BasicConfigurator.resetConfiguration(); 
		Logger.getRootLogger().setLevel(Level.DEBUG);
		Logger.getRootLogger().addAppender(appender); 
	}
	private void writeRecordsFromLog(List<String> records) throws ScenarioException
	{
		BufferedWriter writer = null; 
		try
		{
			writer = new BufferedWriter(new FileWriter(new File("debug.txt")));
			System.out.println("about to write "+records.size()+" records");
			for (String string : records)
			{
				writer.write(string);
			}
			writer.flush(); 
			writer.close(); 
		} 
		catch (FileNotFoundException e)
		{
			throw new ScenarioException("ScenarioLog.buildRecordsFromLog File not found:  "+e.getMessage());
		} 
		catch (IOException e)
		{
			throw new ScenarioException("ScenarioLog.buildRecordsFromLog IO exception:  "+e.getMessage());
		} 
	}

	
	
}
