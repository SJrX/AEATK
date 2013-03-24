package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.NonNegativeInteger;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.ReadableFileConverter;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

@UsageTextField(title="Target Algorithm Evaluator Options", description="Options that describe and control the policy and mechanisms for algorithm execution")
public class TargetAlgorithmEvaluatorOptions extends AbstractOptions {
		
	@UsageTextField(domain="")
	@Parameter(names={"--targetAlgorithmEvaluator","--tae"}, description="Target Algorithm Evaluator to use when making target algorithm calls")
	public String targetAlgorithmEvaluator = "CLI";
	
	@Parameter(names="--abortOnCrash", description="treat algorithm crashes as an ABORT (Useful if algorithm should never CRASH). NOTE:  This only aborts if all retries fail.")
	public boolean abortOnCrash = false;

	@Parameter(names="--abortOnFirstRunCrash", description="if the first run of the algorithm CRASHED treat it as an ABORT, otherwise allow crashes.")
	public boolean abortOnFirstRunCrash = true;

	@Parameter(names="--retryTargetAlgorithmRunCount", description="number of times to retry an algorithm run before eporting crashed (NOTE: The original crashes DO NOT count towards any time limits, they are in effect lost). Additionally this only retries CRASHED runs, not ABORT runs, this is by design as ABORT is only for cases when we shouldn't bother further runs", validateWith=NonNegativeInteger.class)
	public int retryCount = 0;

	@Parameter(names={"--cores","--numConcurrentAlgoExecs","--maxConcurrentAlgoExecs","--numberOfConcurrentAlgoExecs"}, description="maximum number of concurrent target algorithm executions", validateWith=PositiveInteger.class)
	public int maxConcurrentAlgoExecs = 1;
	
	@UsageTextField(defaultValues="")
	@Parameter(names="--runHashCodeFile", description="file containing a list of run hashes one per line: Each line should be: \"Run Hash Codes: (Hash Code) After (n) runs\". The number of runs in this file need not match the number of runs that we execute, this file only ensures that the sequences never diverge. Note the n is completely ignored so the order they are specified in is the order we expect the hash codes in this version. Finally note you can simply point this at a previous log and other lines will be disregarded", converter=ReadableFileConverter.class)
	public File runHashCodeFile;

	@Parameter(names="--leakMemoryAmount", hidden=true, description="amount of memory in bytes to leak")
	public int leakMemoryAmount = 1024;

	@Parameter(names="--leakMemory", hidden=true, description="leaks some amount of memory for every run")
	public boolean leakMemory = false;
	
	@Parameter(names="--verifySAT", description="Check SAT/UNSAT/UNKNOWN responses against Instance specific information (if null then performs check if every instance has specific information in the following domain {SAT, UNSAT, UNKNOWN, SATISFIABLE, UNSATISFIABLE}")
	public Boolean verifySAT;

}
