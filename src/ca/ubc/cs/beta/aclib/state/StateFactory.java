package ca.ubc.cs.beta.aclib.state;

import java.util.List;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.exceptions.StateSerializationException;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;



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
	 * @param id						An identifier for this state
	 * @param restoreIteration			Iteration to restore
	 * @param configSpace				Configuration Space to Restore Into
	 * @param intraInstanceObjective	Objective Function to combine instance seed pairs
	 * @param interInstanceObjective	Objective Function to combine instances 
	 * @param runObj 					Individual Run Objective
	 * @param instances					List of Instances we are configuring over
	 * @param execConfig 				Execution Config of the target algorithm
	 * @return object capable of restoring state
	 * @throws StateSerializationException when an error occurs restoring the state
	 */
	public StateDeserializer getStateDeserializer(String id, int restoreIteration, 	ParamConfigurationSpace configSpace, OverallObjective intraInstanceObjective, OverallObjective interInstanceObjective, RunObjective runObj, List<ProblemInstance> instances, AlgorithmExecutionConfig execConfig) throws StateSerializationException;
	
	
	/**
	 * Gets a State Serializer
	 * @param id			An Identifier for this state
	 * @param iteration		Iteration to restore
	 * @return Object which can be serialized
	 * @throws StateSerializationException when an error occurs saving the state
	 */
	public StateSerializer getStateSerializer(String id, int iteration) throws StateSerializationException;
	



	
}
