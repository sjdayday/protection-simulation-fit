/* Copyright (c) 2013, Regents of the University of California.  See License.txt for details */

package edu.uci.imbs.actor.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.grayleaves.utility.DummyInput;
import org.grayleaves.utility.Result;
import org.grayleaves.utility.TestingFileBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.imbs.actor.AllInteractionPattern;
import edu.uci.imbs.actor.InteractionPattern;
import edu.uci.imbs.actor.ProtectionEquilibriumSeekerMultipleBanditsPeasants;
import edu.uci.imbs.actor.ProtectionParameters;


public class ProtectionModelTest
{
	private ProtectionModel<String> model;
	@BeforeClass
	public static void setUpLog4J() throws Exception
	{
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ERROR);
	}
	@Before
	public void setUp() throws Exception
	{
		TestingFileBuilder.cleanUpDirectory("protection_scenarios"); 
		ProtectionParameters.resetForTesting(); 
		buildModel();
	}
	protected void buildModel()
	{
		model = new ProtectionModel<String>(); 
		model.setInput(new DummyInput()); 
		model.setName("protection"); 
//		model.setOutputFileBuilder(new OutputFileBuilder("protection_scenarios"));
	}
	
	@Test
	public void verifyModelRunsWithResults() throws Exception
	{
		assertEquals(0, model.getSeeker().getProtectionStatistics().getStatisticsRecords().size());
		Result<String> result = model.run(); 
		assertEquals(3, model.getSeeker().getProtectionStatistics().getStatisticsRecords().size());
		assertTrue(result instanceof ProtectionResult);
		assertEquals(result.getList().get(4),"Run stopped because:  Run limit reached.");
		assertEquals("initial protection proportion distribution + 3 stats records + final protection proportion distribution+stop reason code",6, result.getList().size()); 
		assertTrue(result.getSummaryData().startsWith("Stop Reason Code=7,Period=3")); 
	}
	@Test
	public void verifyModelBuildsSeekerWithDefaultTestingParameters() throws Exception
	{
		assertEquals(5, model.getSeeker().getPeasantList().size()); 
		assertEquals(5, model.getSeeker().getBanditList().size()); 
		assertEquals(1, model.getSeeker().getDynamics().size()); 
		assertEquals(model.getSeeker().getBanditList(), model.getSeeker().getInteractionPattern().getSourceList()); 
		assertEquals(ProtectionParameters.PAYOFF_DISCREPANCY_TOLERANCE, model.getSeeker().getProtectionStatistics().getPayoffDiscrepancyTolerance(), .001); 
	}
	@Test
	public void verifySeekerAndPatternSetForMultiplePeasantsBandits() throws Exception
	{
		ProtectionParameters.MULTIPLE_BANDITS_PREY_ON_MULTIPLE_PEASANTS = true; 
		ProtectionModel<String> model =  new ProtectionModel<String>(); 
		assertTrue(model.getSeeker() instanceof ProtectionEquilibriumSeekerMultipleBanditsPeasants); 
	}
	@Test
	public void verifyAllInteractionPatternSet() throws Exception
	{
		model =  new ProtectionModel<String>(); 
		assertTrue(model.getSeeker().getInteractionPattern() instanceof InteractionPattern); 
		ProtectionParameters.NORMAL_INTERACTION_PATTERN = false; 
		buildModel(); 
		assertTrue(model.getSeeker().getInteractionPattern() instanceof AllInteractionPattern); 
	}
	@Test
	public void verifyForcedPeasantAllocationBetweenHighAndLowProtectionProportion() throws Exception
	{
		ProtectionParameters.NUMBER_PEASANTS = 10; 
		ProtectionParameters.FORCE_PEASANT_ALLOCATION_TO_HIGH_LOW = true; 
		ProtectionParameters.FORCE_PEASANT_ALLOCATION_HIGH_PROPORTION = 0.9d; 
		ProtectionParameters.FORCE_PEASANT_ALLOCATION_LOW_PROPORTION = 0.1d; 
		ProtectionParameters.FORCE_PEASANT_ALLOCATION_LOW_INITIAL_PEASANTS = 0; 
		model =  new ProtectionModel<String>(); 
		assertEquals(10,model.getPeasants().size()); 
		checkLowHighProportionsOfPeasantsAllocated(0,10);
		ProtectionParameters.FORCE_PEASANT_ALLOCATION_LOW_INITIAL_PEASANTS = 3; 
		model =  new ProtectionModel<String>(); 
		model.buildPeasants(); // needed to avoid automatic shuffling of the order
		checkLowHighProportionsOfPeasantsAllocated(3,7);
		ProtectionParameters.FORCE_PEASANT_ALLOCATION_LOW_INITIAL_PEASANTS = 11; 
		assertEquals("forced to NUMBER_PEASANTS",10,model.getPeasants().size()); 
		model =  new ProtectionModel<String>(); 
		model.buildPeasants(); 
		checkLowHighProportionsOfPeasantsAllocated(10,0);
	}
	private void checkLowHighProportionsOfPeasantsAllocated(int low, int high)
	{
		for (int i = 0; i < low; i++)
		{
			assertEquals(ProtectionParameters.FORCE_PEASANT_ALLOCATION_LOW_PROPORTION, 
					model.getPeasants().get(i).getProtectionProportion(), .001); 
		}
		for (int i = low; i < high; i++)
		{
			assertEquals(ProtectionParameters.FORCE_PEASANT_ALLOCATION_HIGH_PROPORTION, 
					model.getPeasants().get(i).getProtectionProportion(), .001); 
		}
	}
}
