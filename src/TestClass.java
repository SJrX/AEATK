import java.util.Arrays;

import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;


public class TestClass {
	public static void main(String[] args)
	{
	ParamConfigurationSpace configSpace = new ParamConfigurationSpace("/ubc/cs/home/s/seramage/disks/westgrid-home/cplex12-params-milp-mixed-cont-disc.txt");
	ParamConfiguration config1 = configSpace.getConfigurationFromString("-barrier_algorithm '0' -barrier_crossover '0' -barrier_limits_corrections '-1' -barrier_limits_growth '1.0E12' -barrier_ordering '0' -barrier_startalg '1' -emphasis_memory 'no' -emphasis_mip '0' -emphasis_numerical 'no' -feasopt_mode '0' -lpmethod '0' -mip_cuts_cliques '0' -mip_cuts_covers '0' -mip_cuts_disjunctive '0' -mip_cuts_flowcovers '0' -mip_cuts_gomory '0' -mip_cuts_gubcovers '0' -mip_cuts_implied '0' -mip_cuts_mcfcut '0' -mip_cuts_mircut '0' -mip_cuts_pathcut '0' -mip_cuts_zerohalfcut '0' -mip_limits_aggforcut '3' -mip_limits_cutpasses '0' -mip_limits_cutsfactor '4.0' -mip_limits_gomorycand '200' -mip_limits_gomorypass '0' -mip_limits_submipnodelim '500' -mip_ordertype '0' -mip_strategy_backtrack '0.9999' -mip_strategy_bbinterval '7' -mip_strategy_branch '0' -mip_strategy_dive '0' -mip_strategy_file '1' -mip_strategy_fpheur '0' -mip_strategy_heuristicfreq '0' -mip_strategy_lbheur 'no' -mip_strategy_nodeselect '1' -mip_strategy_presolvenode '0' -mip_strategy_probe '0' -mip_strategy_rinsheur '0' -mip_strategy_search '0' -mip_strategy_startalgorithm '0' -mip_strategy_subalgorithm '0' -mip_strategy_variableselect '0' -network_netfind '2' -network_pricing '0' -preprocessing_aggregator '-1' -preprocessing_boundstrength '-1' -preprocessing_coeffreduce '2' -preprocessing_dependency '-1' -preprocessing_dual '0' -preprocessing_fill '10' -preprocessing_linear '1' -preprocessing_numpass '-1' -preprocessing_reduce '3' -preprocessing_relax '-1' -preprocessing_repeatpresolve '-1' -preprocessing_symmetry '-1' -read_scale '0' -sifting_algorithm '0' -simplex_crash '1' -simplex_dgradient '0' -simplex_limits_perturbation '0' -simplex_limits_singularity '10' -simplex_perturbation_switch 'no' -simplex_pgradient '0' -simplex_pricing '0' -simplex_refactor '0' -simplex_tolerances_markowitz '0.01'", StringFormat.NODB_SYNTAX);
	
	int i=0;
	for(String s : configSpace.getParameterNamesInAuthorativeOrder())
	{
		i++;
		System.out.println(i + "=>" + s);
	}
	System.out.println(Arrays.toString(config1.toValueArray()));
	}
}
