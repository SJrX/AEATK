package ca.ubc.cs.beta.aeatk.json.serializers;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ca.ubc.cs.beta.aeatk.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aeatk.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aeatk.json.serializers.ParamConfigurationJson.ParamConfigurationSpaceDeserializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AlgorithmExecutionConfigJson  {

	public static final String ALGO_TAE_CONTEXT = "algo-tae-context";

	public static final String ALGO_DETERMINISTIC = "algo-deterministic";

	public static final String PCS_FILE = "algo-pcs";

	public static final String ALGO_EXEC_DIR = "algo-exec-dir";

	public static final String ALGO_EXEC = "algo-exec";

	public static final String ALGO_CUTOFF_TIME = "algo-cutoff";
	
	public static final String ALGO_EXEC_CONFIG_ID = "@algo-exec-config-id";

	private static final String JACKSON_ALGO_CONTEXT = "ALGO_CONTEXT";
	
	public static class AlgorithmExecutionConfigDeserializer extends StdDeserializer<AlgorithmExecutionConfig>
	{

		private final ParamConfigurationSpaceDeserializer pcsd = new ParamConfigurationSpaceDeserializer();
		
		protected AlgorithmExecutionConfigDeserializer() {
			super(AlgorithmExecutionConfig.class);
		}

		@Override
		public AlgorithmExecutionConfig deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			
			@SuppressWarnings("unchecked")
			Map<Integer, AlgorithmExecutionConfig> cache = (Map<Integer, AlgorithmExecutionConfig>) ctxt.getAttribute(JACKSON_ALGO_CONTEXT);
			
			if(cache == null)
			{
				cache = new ConcurrentHashMap<Integer, AlgorithmExecutionConfig>();
				ctxt.setAttribute(JACKSON_ALGO_CONTEXT, cache);
			}
			
			
		
			if(jp.getCurrentToken() == JsonToken.START_OBJECT)
			{
				jp.nextToken();
			}
			
		
			String algo = null;
			String algoDir = null;
			double cutoffTime = Double.NEGATIVE_INFINITY;
			ParamConfigurationSpace pcs = null;
			boolean deterministic = false;
			Map<String, String> taeContext = new TreeMap<String, String>();
			
			int execConfig_id = 0;
			
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
					case ALGO_EXEC:
						algo = jp.getValueAsString();
						break;
					case ALGO_EXEC_DIR:
						algoDir = jp.getValueAsString();
						break;
					case PCS_FILE:
						pcs = pcsd.deserialize(jp, ctxt);
						break;
						
					case ALGO_DETERMINISTIC:
						deterministic = jp.getValueAsBoolean();
						break;
					case ALGO_TAE_CONTEXT:
						JsonNode node = jp.getCodec().readTree(jp);
						
						Iterator<Entry<String, JsonNode>> i = node.fields();
						
						while(i.hasNext())
						{
							Entry<String, JsonNode> ent = i.next();
							taeContext.put(ent.getKey(), ent.getValue().asText());
						}
						
						break;
					case ALGO_CUTOFF_TIME:
						cutoffTime = jp.getDoubleValue();
						break;
					case ALGO_EXEC_CONFIG_ID:
						execConfig_id = jp.getValueAsInt();
						break;
					default:
						break;
				}
			}
			
			
			
			if(execConfig_id > 0 && cache.get(execConfig_id) != null)
			{
				return cache.get(execConfig_id);
			} else
			{
				AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig(algo, algoDir, pcs, false, deterministic, cutoffTime, taeContext);
				
				if(execConfig_id > 0)
				{
					cache.put(execConfig_id, execConfig);
				}
				
				return execConfig;
			}
			
		}

	}
	
	public static class AlgorithmExecutionConfigSerializer extends StdSerializer<AlgorithmExecutionConfig>
	{
		
		protected AlgorithmExecutionConfigSerializer()
		{
			super(AlgorithmExecutionConfig.class);
		}

		private final ConcurrentHashMap<AlgorithmExecutionConfig, Integer> map = new ConcurrentHashMap<AlgorithmExecutionConfig, Integer>();
		
		private final AtomicInteger idMap = new AtomicInteger(1);
		

		@Override
		public void serialize(AlgorithmExecutionConfig value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException 
		{
			jgen.writeStartObject();
			
	
			boolean firstWrite = (map.putIfAbsent(value,idMap.incrementAndGet()) == null);
			
			Integer id = map.get(value);		
			jgen.writeObjectField(ALGO_EXEC_CONFIG_ID,id);
			
			if(firstWrite)
			{
				jgen.writeObjectField(ALGO_EXEC, value.getAlgorithmExecutable());
				jgen.writeObjectField(ALGO_EXEC_DIR, value.getAlgorithmExecutionDirectory());
				jgen.writeObjectField(PCS_FILE, value.getParamFile());
				jgen.writeObjectField(ALGO_CUTOFF_TIME, value.getAlgorithmCutoffTime());
				jgen.writeObjectField(ALGO_DETERMINISTIC,value.isDeterministicAlgorithm());
				jgen.writeObjectField(ALGO_TAE_CONTEXT, value.getTargetAlgorithmExecutionContext());
			} 
			
			jgen.writeEndObject();
		}
		
	}
}
