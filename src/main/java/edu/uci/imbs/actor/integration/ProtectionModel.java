/* Copyright (c) 2013, Regents of the University of California.  See License.txt for details */

package edu.uci.imbs.actor.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.grayleaves.utility.ModelException;
import org.grayleaves.utility.PersistentModel;
import org.grayleaves.utility.Result;

import edu.uci.imbs.actor.AllInteractionPattern;
import edu.uci.imbs.actor.Bandit;
import edu.uci.imbs.actor.BehaviorException;
import edu.uci.imbs.actor.FitnessFunction;
import edu.uci.imbs.actor.InteractionPattern;
import edu.uci.imbs.actor.MultipleBehaviorInteractionPattern;
import edu.uci.imbs.actor.Peasant;
import edu.uci.imbs.actor.ProtectionEquilibriumSeeker;
import edu.uci.imbs.actor.ProtectionEquilibriumSeekerMultipleBanditsPeasants;
import edu.uci.imbs.actor.ProtectionFunctionEnum;
import edu.uci.imbs.actor.ProtectionParameters;
import edu.uci.imbs.actor.ProtectionReplicatorDynamic;
import edu.uci.imbs.actor.DieSurviveThriveDynamic;
import edu.uci.imbs.actor.RoleShiftingDynamic;
import edu.uci.imbs.actor.RunGovernorEnum;
import edu.uci.imbs.actor.StatisticsRecord;
import edu.uci.imbs.actor.VariablePopulationProtectionStatistics;

public class ProtectionModel<T> extends PersistentModel<T> 
{

	private ProtectionEquilibriumSeeker seeker;
	private List<Peasant> peasants;
	private List<Bandit> bandits;
	private FitnessFunction fitnessFunction;
	private InteractionPattern<Bandit, Peasant> pattern;
	private VariablePopulationProtectionStatistics statistics;
	private DieSurviveThriveDynamic replicatorDynamic;
	private ProtectionResult<T> result;
	public ProtectionModel()
	{
		build(); 
	}
	
	private void build()
	{
		buildPeasants();
		buildBandits(); 
		buildFitnessFunction(); 
		buildInteractionPattern(); 
		buildStatistics(); 
		buildReplicatorDynamic(); 
		buildSeeker(); 
	}
	@SuppressWarnings("unchecked")
	@Override
	public Result<T> run() throws ModelException
	{
		build();
		result = new ProtectionResult<T>(); 
		RunGovernorEnum reason = null; 
		logProtectionProportionDistribution();
		try
		{
			reason = seeker.runToEquilibriumOrLimit();
		}
		catch (BehaviorException e)
		{
			result.add((T) e.getStackTrace());  //FIXME: can't return the result out of catch 
			throw new ModelException("ModelException:  "+e.getMessage());
		} 
		logStatisticsRecordsAndSummaryData(reason); 
		logProtectionProportionDistribution();
		return result; 
	}
	@SuppressWarnings("unchecked")
	public void logProtectionProportionDistribution()
	{
		result.add((T) ((VariablePopulationProtectionStatistics) statistics).printPeasantProtectionProportionDistribution());
		if (ProtectionParameters.MULTIPLE_BANDITS_PREY_ON_MULTIPLE_PEASANTS)
		{
			result.add((T) ((VariablePopulationProtectionStatistics) statistics).printBanditPredationEffortDistribution());
		}
	}
	@SuppressWarnings("unchecked")
	private void logStatisticsRecordsAndSummaryData(RunGovernorEnum reason)
	{
		List<? extends StatisticsRecord> records = statistics.getStatisticsRecords(); 
		for (StatisticsRecord statisticsRecord : records)
		{
			result.add((T) statisticsRecord.toString());   
		}
		result.add((T) ("Run stopped because:  "+reason.getReasonDescription())); 
		addSummaryDataToResult(records, reason);
	}

	public void addSummaryDataToResult(
			List<? extends StatisticsRecord> records, RunGovernorEnum reason)
	{
		int lastRecord = statistics.getStatisticsRecords().size()-1;
		String summaryStopCode = "Stop Reason Code="+reason.getReason()+","; 
		result.setSummaryData(summaryStopCode+records.get(lastRecord).toString());
	}

	private void buildReplicatorDynamic()
	{
		replicatorDynamic = new DieSurviveThriveDynamic();
		replicatorDynamic.setFitnessFunction(fitnessFunction); 
	}

	private void buildSeeker()
	{
		buildSeekerAndPatternPerMultiplePeasantsBanditsParm();
		seeker.setPeasantList(peasants); 
		seeker.setBanditList(bandits); 
		seeker.setProtectionStatistics(statistics);
		seeker.addDynamic(new ProtectionReplicatorDynamic(replicatorDynamic)); 
		if (ProtectionParameters.ROLE_SHIFTING) seeker.addDynamic(new RoleShiftingDynamic(statistics)); 
		seeker.setRunLimit(ProtectionParameters.RUN_LIMIT); 

//		seeker = new VariablePopulationProtectionEquilibriumSeeker(); 
////seeker.setReplicatorDynamic(replicatorDynamic); 
	}

	public void buildSeekerAndPatternPerMultiplePeasantsBanditsParm()
	{
		if (ProtectionParameters.MULTIPLE_BANDITS_PREY_ON_MULTIPLE_PEASANTS) 
		{
			seeker = new ProtectionEquilibriumSeekerMultipleBanditsPeasants(); 
			((ProtectionEquilibriumSeekerMultipleBanditsPeasants) seeker).setMultipleBehaviorInteractionPattern(new MultipleBehaviorInteractionPattern(bandits, peasants));
		}
		else
		{
			seeker = new ProtectionEquilibriumSeeker(); 
			seeker.setInteractionPattern(pattern); 
		}
	}

	private void buildStatistics()
	{
		statistics = new VariablePopulationProtectionStatistics(bandits, peasants, ProtectionParameters.PROTECTION_PROPORTION_NUMBER_INTERVALS, 
				ProtectionParameters.PROTECTION_PROPORTION_INTERVAL_SIZE); 
		statistics.setPayoffDiscrepancyTolerance(ProtectionParameters.PAYOFF_DISCREPANCY_TOLERANCE);
		statistics.setAdjustmentFactorPercentage(ProtectionParameters.ADJUSTMENT_FACTOR_PERCENTAGE); 
	}

	public void buildInteractionPattern()
	{
		if (ProtectionParameters.NORMAL_INTERACTION_PATTERN)
		{
			pattern = new InteractionPattern<Bandit, Peasant>(bandits, peasants);
			pattern.permute();
		}
		else
		{
			pattern = new AllInteractionPattern(bandits, peasants); 
		}
	}

	public void buildFitnessFunction()
	{
		fitnessFunction = new FitnessFunction(); 
		fitnessFunction.setSurviveThreshold(ProtectionParameters.SURVIVE_THRESHOLD); 
		fitnessFunction.setThriveThreshold(ProtectionParameters.THRIVE_THRESHOLD);
	}
	protected void buildPeasants()  
	{
		peasants = new ArrayList<Peasant>(); 
		if (ProtectionParameters.FORCE_PEASANT_ALLOCATION_TO_HIGH_LOW) buildForcedPeasantAllocation(); 
		else buildNormalPeasantDistribution();
	}
	private void buildNormalPeasantDistribution()
	{
		Random r  = new Random(ProtectionParameters.RANDOM_SEED); 
		Peasant peasant = null; 
		for (int i = 0; i < ProtectionParameters.NUMBER_PEASANTS; i++)
		{
			peasant = Peasant.buildPeasantWithContestFunctionAndRandomProtectionProportion(r);
//			peasant = buildPeasantWithRandomProtectionProportion(r);
			peasants.add(peasant);
		}
	}

//	protected Peasant buildPeasantWithRandomProtectionProportion(Random r) {
//		Peasant peasant = new Peasant(); 
//		peasant.setFunction(ProtectionFunctionEnum.CONTEST.buildFunction(ProtectionParameters.CONTEST_FUNCTION_GAMMA)); 
//		peasant.setProtectionProportion(ProtectionParameters.PROTECTION_PARAMETER_INTERVAL_SIZE * r.nextInt(ProtectionParameters.PROTECTION_PARAMETER_NUMBER_INTERVALS));
//		return peasant;
//	}
	private void buildForcedPeasantAllocation()
	{
		int low = (ProtectionParameters.NUMBER_PEASANTS >= ProtectionParameters.FORCE_PEASANT_ALLOCATION_LOW_INITIAL_PEASANTS) 
				 ? ProtectionParameters.FORCE_PEASANT_ALLOCATION_LOW_INITIAL_PEASANTS : ProtectionParameters.NUMBER_PEASANTS; 
		int high = ProtectionParameters.NUMBER_PEASANTS - low;  
		buildForcedPeasants(low, ProtectionParameters.FORCE_PEASANT_ALLOCATION_LOW_PROPORTION);
		buildForcedPeasants(high, ProtectionParameters.FORCE_PEASANT_ALLOCATION_HIGH_PROPORTION);
	}

	private void buildForcedPeasants(int number, double proportion)
	{
		Peasant peasant = null;
		for (int i = 0; i < number; i++)
		{
			peasant = new Peasant(); 
			peasant.setFunction(ProtectionFunctionEnum.CONTEST.buildFunction(ProtectionParameters.CONTEST_FUNCTION_GAMMA)); 
			peasant.setProtectionProportion(proportion);
			peasants.add(peasant);
		}
	}
	protected void buildBandits()
	{
		bandits = new ArrayList<Bandit>(); 
		Bandit bandit = null;
		for (int i = 0; i < ProtectionParameters.NUMBER_BANDITS; i++)
		{
			bandit = new Bandit();
			bandits.add(bandit);
		}
	}
	
	public ProtectionEquilibriumSeeker getSeeker()
	{
		return seeker;
	}
	protected List<Peasant> getPeasants()
	{
		return peasants;
	}

}
