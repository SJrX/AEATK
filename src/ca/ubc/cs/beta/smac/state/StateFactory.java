package ca.ubc.cs.beta.smac.state;

import java.util.List;

import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.smac.OverallObjective;
import ca.ubc.cs.beta.smac.RunObjective;
import ca.ubc.cs.beta.smac.exceptions.StateSerializationException;



/**
 * Factory for getting objects related to state. The semantics of the id string are up to individual implementations. In theory clients should
 * have the property that the id something is saved with can be used to retrieve it. But I suppose this might be too strong. Primarily the user should
 * be informed of the state being saved, and it should be clear (like a filename), how to use that id to get it back.
 * 
 * 
 * @author seramage
 *
 */
public interface StateFactory {
	
	/**
	 * Gets a State Deserializer 
	 * 
	 * @param id - An identifier for this state
	 * @param restoreIteration - Iteration to restore
	 * @param configSpace - Configuration Space to Restore Into
	 * @param overallObj - Overall Objective
	 * @param runObjective - Individual Run Objective
	 * @param instances - List of Instances we are configuring over
	 * @param Execution Config - Execution Config of the target algorithm
	 * @return
	 */
	public StateDeserializer getStateDeserializer(String id, int restoreIteration, 	ParamConfigurationSpace configSpace, OverallObjective overallObj, RunObjective runObj, List<ProblemInstance> instances, AlgorithmExecutionConfig execConfig) throws StateSerializationException;
	
	
	/**
	 * Gets a State Serializer
	 * @param id - An Identifier for this state
	 * @param iteration - Iteration to restore
	 * @return Object which can be serialized
	 * @throws StateSerializationException
	 */
	public StateSerializer getStateSerializer(String id, int iteration) throws StateSerializationException;
	



	
}
