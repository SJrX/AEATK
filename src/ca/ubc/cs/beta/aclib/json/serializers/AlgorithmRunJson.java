package ca.ubc.cs.beta.aclib.json.serializers;

import java.io.IOException;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.json.serializers.RunConfigJson.RunConfigDeserializer;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AlgorithmRunJson  {

	public static final String R_WALLCLOCK_TIME = "r-wallclock-time";
	public static final String R_ADDL_RUN_DATA = "r-addl-run-data";
	public static final String R_RESULT_SEED = "r-result-seed";
	public static final String R_QUALITY = "r-quality";
	public static final String R_RUN_LENGTH = "r-run-length";
	public static final String R_RUNTIME = "r-runtime";
	public static final String R_RUN_RESULT = "r-run-result";
	public static final String R_RC = "r-rc";
	
	public static class AlgorithmRunDeserializer extends StdDeserializer<AlgorithmRun>
	{

		private RunConfigDeserializer rcd = new RunConfigDeserializer();
		
		protected AlgorithmRunDeserializer() {
			super(AlgorithmRun.class);
		}

		@Override
		public AlgorithmRun deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
		
			if(jp.getCurrentToken()==JsonToken.START_OBJECT)
			{
				jp.nextToken();
			}
			RunResult runResult = null;;
			double runtime = -1;
			double quality = -1;
			double runlength = -1;
			String addlRunData = null;
			double wallclock = -1;
			long seed = Long.MIN_VALUE;
			
			RunConfig rc = null;
			
			while(jp.nextValue() != null)
			{
				
				if(jp.getCurrentToken() == JsonToken.END_OBJECT)
				{
					break;
				}
				
				if(jp.getCurrentName() == null)
				{
					continue;
				}
				switch(jp.getCurrentName())
				{
				case R_RUNTIME:
					runtime = jp.getValueAsDouble();
					break;
				case R_RUN_LENGTH:
					runlength = jp.getValueAsDouble();
					break;
				case R_RESULT_SEED:
					seed = jp.getValueAsLong();
					break;
				case R_RUN_RESULT:
					runResult = RunResult.getAutomaticConfiguratorResultForKey(jp.getValueAsString());
					break;
				case R_QUALITY:
					quality = jp.getValueAsDouble();
					break;
				case R_WALLCLOCK_TIME:
					wallclock = jp.getValueAsDouble();
					break;
				case R_ADDL_RUN_DATA:
					addlRunData = jp.getValueAsString();
					break;
				case R_RC:
					
					rc = rcd.deserialize(jp, ctxt);
				default:
					break;
					
				}
			}
			
				
			return new ExistingAlgorithmRun( rc, runResult, runtime, runlength, quality, seed, addlRunData, wallclock);
		}

	}
	
	public static class AlgorithmRunSerializer extends StdSerializer<AlgorithmRun>	{

		

		protected AlgorithmRunSerializer() {
			super(AlgorithmRun.class);
		}

		//Unlike most everything else 
		//We won't cache these because AlgorithmRun equality is based on the RunConfig, not the AlgorithmRun
		@Override
		public void serialize(AlgorithmRun value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException 
		{
			jgen.writeStartObject();
			
			jgen.writeObjectField(R_RC,value.getRunConfig());
			jgen.writeObjectField(R_RUN_RESULT, value.getRunResult());
			jgen.writeObjectField(R_RUNTIME,value.getRuntime());
			jgen.writeObjectField(R_RUN_LENGTH,value.getRunLength());
			jgen.writeObjectField(R_QUALITY,value.getQuality());
			jgen.writeObjectField(R_RESULT_SEED,value.getResultSeed());
			jgen.writeObjectField(R_ADDL_RUN_DATA,value.getAdditionalRunData());
			jgen.writeObjectField(R_WALLCLOCK_TIME,value.getWallclockExecutionTime());
			
			jgen.writeEndObject();
		}
		
	}
	
	
	
}
