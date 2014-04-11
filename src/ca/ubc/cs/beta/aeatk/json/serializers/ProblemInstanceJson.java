package ca.ubc.cs.beta.aeatk.json.serializers;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ProblemInstanceJson  {

	public static final String PI_ID = "@pi-id";

	public static final String PI_INSTANCE_ID_DEPRECATED = "pi-instance-id(deprecated)";

	public static final String PI_FEATURES = "pi-features";

	public static final String PI_INSTANCE_SPECIFIC_INFO = "pi-instance-specific-info";

	public static final String PI_NAME = "pi-name";

	public static final String PISP_SEED = "pisp-seed";

	public static final String PISP_PI = "pisp-pi";

	public static final String PISP_ID = "@pisp-id";
	
	
	
	private static final String JACKSON_PI_CONTEXT = "PI_CONTEXT";
	
	private static final String JACKSON_PISP_CONTEXT = "PISP_CONTEXT";
	public static class ProblemInstanceDeserializer extends StdDeserializer<ProblemInstance>
	{

		protected ProblemInstanceDeserializer() {
			super(ProblemInstance.class);
		}

		@Override
		public ProblemInstance deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			
			
			@SuppressWarnings("unchecked")
			Map<Integer, ProblemInstance> cache = (Map<Integer, ProblemInstance>) ctxt.getAttribute(JACKSON_PI_CONTEXT);
			
			if(cache == null)
			{
				cache = new ConcurrentHashMap<Integer, ProblemInstance>();
				ctxt.setAttribute(JACKSON_PI_CONTEXT, cache);
			}
			
			
		
			if(jp.getCurrentToken()==JsonToken.START_OBJECT)
			{
				jp.nextToken();
			}
	
		
			
			
			String instanceName = null;
			int instanceDeprecatedId = 0;
			Map<String, Double> features = new TreeMap<String, Double>();
			String instanceSpecificInformation = null;
			
			int pi_id = 0;
			
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
					case PI_NAME:
						instanceName = jp.getValueAsString();
						break;
					case PI_INSTANCE_SPECIFIC_INFO:
						instanceSpecificInformation = jp.getValueAsString();
						break;
					case PI_INSTANCE_ID_DEPRECATED:
						instanceDeprecatedId = jp.getValueAsInt();
						break;
					case PI_FEATURES:
						JsonNode node = jp.getCodec().readTree(jp);
						
						Iterator<Entry<String, JsonNode>> i = node.fields();
						
						while(i.hasNext())
						{
							Entry<String, JsonNode> ent = i.next();
							features.put(ent.getKey(), ent.getValue().asDouble());
						}
						
						break;
						
					case PI_ID:
						pi_id = jp.getValueAsInt();
						break;
				default:
					break;
					
				}
			}
			
			
			
			if( cache.get(pi_id) != null)
			{
				return cache.get(pi_id);
			} else
			{
				ProblemInstance pi = new ProblemInstance(instanceName, instanceDeprecatedId, features, instanceSpecificInformation);
				
				if(pi_id >0 )
				{
					cache.put(pi_id, pi);
				}
				
				return pi;
			}
		}

	}
	
	public static class ProblemInstanceSerializer extends JsonSerializer<ProblemInstance>	{


		
		private final ConcurrentHashMap<ProblemInstance, Integer> map = new ConcurrentHashMap<ProblemInstance, Integer>();
		
		private final AtomicInteger idMap = new AtomicInteger(1);
		

		@Override
		public void serialize(ProblemInstance value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException 
		{
			jgen.writeStartObject();
			
	
			boolean firstWrite = (map.putIfAbsent(value,idMap.incrementAndGet()) == null);
			
			Integer id = map.get(value);		
			jgen.writeObjectField(PI_ID,id);
			
			if(firstWrite)
			{
				jgen.writeObjectField(PI_NAME, value.getInstanceName());
				jgen.writeObjectField(PI_INSTANCE_SPECIFIC_INFO, value.getInstanceSpecificInformation());
				jgen.writeObjectField(PI_FEATURES, value.getFeatures());
				jgen.writeObjectField(PI_INSTANCE_ID_DEPRECATED, value.getInstanceID());
			} 
			
			jgen.writeEndObject();
		}
		
	}
	
	public static class ProblemInstanceSeedPairDeserializer extends StdDeserializer<ProblemInstanceSeedPair>
	{
		
		ProblemInstanceDeserializer pid = new ProblemInstanceDeserializer();

		protected ProblemInstanceSeedPairDeserializer() {
			super(ProblemInstanceSeedPair.class);
		}

		@Override
		public ProblemInstanceSeedPair deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException 
		{

			@SuppressWarnings("unchecked")
			Map<Integer, ProblemInstanceSeedPair> cache = (Map<Integer, ProblemInstanceSeedPair>) ctxt.getAttribute(JACKSON_PISP_CONTEXT);
			
			if(cache == null)
			{
				cache = new ConcurrentHashMap<Integer, ProblemInstanceSeedPair>();
				ctxt.setAttribute(JACKSON_PISP_CONTEXT, cache);
			}
			
			
		
			if(jp.getCurrentToken() == JsonToken.START_OBJECT)
			{
				jp.nextToken();
			}
			
		
			ProblemInstance pi = null;
			long seed = Integer.MIN_VALUE;;
			
			int pisp_id = -1;
			
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
					case PISP_PI:
						pi = pid.deserialize(jp, ctxt);
						break;
					case PISP_SEED:
						seed = jp.getValueAsLong();
						break;
					case PISP_ID:
						pisp_id = jp.getValueAsInt();
						break;
					default:
						break;
				}
			}
			
			
			
			if( cache.get(pisp_id) != null)
			{
				return cache.get(pisp_id);
			} else
			{
				ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, seed);
				
				if(pisp_id >0)
				{
					cache.put(pisp_id, pisp);
				}
				
				return pisp;
			}
			
		}

	}
	
	
	public static class ProblemInstanceSeedPairSerializer extends JsonSerializer<ProblemInstanceSeedPair>	{


		private final ConcurrentHashMap<ProblemInstanceSeedPair, Integer> map = new ConcurrentHashMap<ProblemInstanceSeedPair, Integer>();
		
		private final AtomicInteger idMap = new AtomicInteger(1);
		

		@Override
		public void serialize(ProblemInstanceSeedPair value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException 
		{
			jgen.writeStartObject();
			
	
			boolean firstWrite = (map.putIfAbsent(value,idMap.incrementAndGet()) == null);
			
			Integer id = map.get(value);		
			jgen.writeObjectField(PISP_ID,id);
			
			
			if(firstWrite)
			{
				jgen.writeObjectField(PISP_PI, value.getInstance());
				jgen.writeObjectField(PISP_SEED, value.getSeed());
			} 
			
			jgen.writeEndObject();
		}
		
	}
	
}
