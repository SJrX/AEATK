package ca.ubc.cs.beta.smac.model.builder;

import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ca.ubc.cs.beta.config.RandomForestConfig;
import ca.ubc.cs.beta.smac.exceptions.TrajectoryDivergenceException;
import ca.ubc.cs.beta.smac.history.RunHistory;
import ca.ubc.cs.beta.smac.model.data.SanitizedModelData;
/**
 * Model Hash Codes are not persistent.
 * @author seramage
 *
 */
public class HashCodeVerifyingModelBuilder extends BasicModelBuilder {

	//TODO REMOVE THIS AWFULNESS
	public static Queue<Integer> modelHashes = new LinkedList<Integer>();
	public static Queue<Integer> preprocessedHashes = new LinkedList<Integer>();
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Marker runHash = MarkerFactory.getMarker("RUN_HASH");
	
	
	public HashCodeVerifyingModelBuilder(SanitizedModelData mds,
			RandomForestConfig rfConfig, RunHistory runHistory) {
		super(mds, rfConfig, runHistory);
		
		int forestCode = forest.matlabHashCode();
		log.info("Random Forest Built with Hash Code: {}", forestCode);
		
		
		if(!modelHashes.isEmpty())
		{
			int expected = modelHashes.poll();
			if(forestCode != expected)
			{
				throw new TrajectoryDivergenceException("Expected Random Forest To Be Built With Hash Code: "+expected+ " vs. " + forestCode);
			} else
			{
				log.info("Random Forest Hash Code Matched");
			}
		}
		
		if(preprocessedForest != null)
		{
			int preprocessedCode = preprocessedForest.matlabHashCode();
			log.info(runHash,"Preprocessed Forest Built with Hash Code: {}",preprocessedCode);
			
			if(!preprocessedHashes.isEmpty())
			{
				int expected = preprocessedHashes.poll();
				if(preprocessedCode != expected)
				{
					throw new TrajectoryDivergenceException("Expected Preprocessed Random Forest To Be Built With Hash Code: "+expected+ " vs. " + preprocessedCode);
				} else
				{
					log.info("Preprocessed Hash Code Matched");
				}

			}
		}
		
		
		
		
		
	}

}
