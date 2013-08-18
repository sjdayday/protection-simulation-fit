/* Copyright (c) 2013, Regents of the University of California.  See License.txt for details */

package edu.uci.imbs.actor.fit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.grayleaves.utility.Constants;
import org.grayleaves.utility.DummyInput;
import org.grayleaves.utility.Environment;
import org.grayleaves.utility.MockClock;
import org.grayleaves.utility.MockHibernateScenarioSet;
import org.grayleaves.utility.NameValuePair;
import org.grayleaves.utility.NameValuePairBuilder;
import org.grayleaves.utility.ParameterSpace;
import org.grayleaves.utility.ParameterSpacePersister;
import org.grayleaves.utility.ScenarioException;
import org.grayleaves.utility.ScenarioSet;
import org.grayleaves.utility.fit.ParameterSpaceFixture;

import edu.uci.imbs.actor.ProtectionParameters;
import edu.uci.imbs.actor.VariablePopulationProtectionStatistics;
import edu.uci.imbs.actor.integration.ProtectionModel;
import fitlibrary.DoFixture;

public class ProtectionFixture extends DoFixture
{
	private static Logger logger = Logger.getLogger(ProtectionFixture.class);
	public static int INTERMEDIATE_NODES = 10; // default 
	private FileAppender appender;
	private ParameterSpaceFixture protectionParameterSpaceFixture;
	private static final String LAYOUT = "%p %c{2}:  %m %n";
	private ScenarioSet<String, DummyInput> scenarioSet;

	
	public ProtectionFixture() 
	{
		try
		{
			//TODO rework this so logging works as you'd expect
			configureAppender();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		} 
	}
	
	public void protectionParameterSpace()
	{
		protectionParameterSpaceFixture = new ParameterSpaceFixture(); 
		setSystemUnderTest(protectionParameterSpaceFixture); 
	}
	protected void configureAppender() throws IOException  
	{
		appender = new FileAppender(new EnhancedPatternLayout(LAYOUT), "ProtectionFixture.log");
		appender.setName(this.toString());
		BasicConfigurator.resetConfiguration(); 
		Logger.getRootLogger().setLevel(Level.INFO);
		logger.addAppender(appender); 
	}

	public void runScenarios() throws ScenarioException
	{
		ProtectionParameters.resetForTesting(); //FIXME replace once Range Parameter supported for doubles...
		scenarioSet = new MockHibernateScenarioSet<String, DummyInput>(false);		
		scenarioSet.setName("protection"); // perhaps a different name  
		scenarioSet.setModel(new ProtectionModel<String>());
		scenarioSet.setInput(new DummyInput());
		scenarioSet.setParameterSpace(protectionParameterSpaceFixture.getParameterSpace()); 
		scenarioSet.setCalendar(MockClock.getCalendar());
		scenarioSet.setId(buildMockId());
		buildSummaryHeader(scenarioSet);
		logger.info("Starting: scenarioSet.run(); ");
		scenarioSet.batchRun(); 
		saveParameterSpace(scenarioSet); 
	}

	protected void buildSummaryHeader(
			ScenarioSet<String, DummyInput> scenarioSet)
	{
		List<String> headings = ((VariablePopulationProtectionStatistics) ((ProtectionModel<String>) scenarioSet.getModel()).getSeeker().getProtectionStatistics()).getPeasantProportionHeadings(); //TODO a little less cryptic 
		NameValuePairBuilder b = new NameValuePairBuilder(); 
		List<NameValuePair<?>> pairs = new ArrayList<NameValuePair<?>>(); 
		pairs.add(b.integerPair("Stop Reason Code"));
		pairs.add(b.integerPair("Period"));
		pairs.add(b.integerPair("Number Bandits"));
		pairs.add(b.integerPair("Number Peasants"));
		pairs.add(b.integerPair("Number Bandits After Replication"));
		pairs.add(b.integerPair("Number Peasants After Replication"));
		pairs.add(b.doublePair("Average Bandit Payoff"));
		pairs.add(b.doublePair("Average Peasant Payoff"));
		pairs.add(b.doublePair("Bandit-Peasant Payoff Delta"));
		pairs.add(b.integerPair("Actor Adjustment"));
		pairs.add(b.doublePair("Average Protection Proportion"));
		pairs.add(b.doublePair("Median Protection Proportion"));
		pairs.add(b.doublePair("Mode Protection Proportion"));
		pairs.add(b.doublePair("Average Number Peasants To Prey Upon"));
		pairs.add(b.doublePair("Median Number Peasants To Prey Upon"));
		pairs.add(b.integerPair("Mode Number Peasants To Prey Upon"));
		Iterator<String> it = headings.iterator();
		while (it.hasNext())
		{
			pairs.add(b.integerPair(it.next()));
			pairs.add(b.doublePair(it.next()));
		}
		scenarioSet.buildSummaryHeader(pairs.toArray(new NameValuePair<?>[pairs.size()])); 
	}
	private void saveParameterSpace(ScenarioSet<String, DummyInput> scenarioSet) throws ScenarioException
	{
		try
		{
			String name = "parameterSpaceScenario_"+scenarioSet.getId(); 
			String filename = scenarioSet.getOutputFileBuilder().getRootDirectoryFullPathName()+Constants.SLASH+name+".xml"; 
			scenarioSet.getParameterSpace().setFilename(filename); 
			ParameterSpacePersister<ParameterSpace> spacePersister = new ParameterSpacePersister<ParameterSpace>();
			scenarioSet.getParameterSpace().loadProperties("org/grayleaves/utility/testing.properties");  // kinda dumb
			spacePersister.save(scenarioSet.getParameterSpace(), filename);  //
		}
		catch (Exception e)
		{
			throw new ScenarioException("ProtectionFixture.saveParameterSpace: "+e.getMessage());
		}
	}
	private int buildMockId() throws ScenarioException
	{
		String pathname = "";
		String envPath = Environment.getEnv("SCENARIO_ROOT");
		if (envPath != null) pathname = envPath+Constants.SLASH;
		int id = 1;
		File file = null; 
		boolean idNotAvailable = true;
		while (idNotAvailable)
		{
			file = new File(pathname+"ScenarioSet_"+id);
			if (!file.exists())
			{
				idNotAvailable = false; 
			}
			else id++; 
		}
		return id;
	}
}
