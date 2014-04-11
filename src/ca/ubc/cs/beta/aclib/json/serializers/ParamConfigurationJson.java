package ca.ubc.cs.beta.aclib.json.serializers;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;










import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ParamConfigurationJson
{

	public static final String PC_DEFAULT = "pc-default";

	public static final String PC_FORBIDDEN = "pc-forbidden";

	public static final String PC_ACTIVE_PARAMETERS = "pc-active-parameters";

	public static final String PC_SETTINGS = "pc-settings";

	public static final String PC_PCS = "pc-pcs";
	
	public static final String PC_ID = "@pc-id";
	
	public static final String PCS_SUBSPACE = "pcs-subspace";

	public static final String PCS_TEXT = "pcs-text";

	public static final String PCS_FILENAME = "pcs-filename";
	
	public static final String PCS_ID = "@pcs-id";
	

	private static final String JACKSON_PC_CONTEXT = "PC_CONTEXT";
	
	private static final String JACKSON_PCS_CONTEXT = "PCS_CONTEXT";
	
	public static class ParamConfigurationDeserializer extends StdDeserializer<ParamConfiguration>
	{
		
		private final ParamConfigurationSpaceDeserializer pcsd = new ParamConfigurationSpaceDeserializer();

		protected ParamConfigurationDeserializer() {
			super(ParamConfiguration.class);
		}

		@Override
		public ParamConfiguration deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
		
			
			if(jp.getCurrentToken()==JsonToken.START_OBJECT)
			{
				jp.nextToken();
			}
			
			
			@SuppressWarnings("unchecked")
			Map<Integer, ParamConfiguration> cache = (Map<Integer, ParamConfiguration>) ctxt.getAttribute(JACKSON_PC_CONTEXT);
			
			if(cache == null)
			{
				cache = new ConcurrentHashMap<Integer, ParamConfiguration>();
				ctxt.setAttribute(JACKSON_PC_CONTEXT, cache);
			}
			
		
			ParamConfigurationSpace configSpace = null;
			Map<String, String> settings = new HashMap<>();
			int pc_id = 0;
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
					case PC_DEFAULT:
					case PC_FORBIDDEN:
					case PC_ACTIVE_PARAMETERS:
						//We don't care about these
						break;
					
						
					case PC_PCS:
						configSpace = pcsd.deserialize(jp, ctxt);
						
						if(configSpace == null)
						{
							throw new JsonParseException("Couldn't resolve ParamConfigurationSpace from child", jp.getCurrentLocation());
						}
						break;

					case PC_SETTINGS:
						
						JsonNode node = jp.getCodec().readTree(jp);
						
						Iterator<Entry<String, JsonNode>> i = node.fields();
						
						while(i.hasNext())
						{
							Entry<String, JsonNode> ent = i.next();
							settings.put(ent.getKey(), ent.getValue().asText());
						}
						
						break;
					case PC_ID:
						pc_id = jp.getValueAsInt();
						break;
				default:
					break;
					
				}
			}
			
			
			
				
				
				if(cache.get(pc_id) != null)
				{
					return cache.get(pc_id); 
				} else
				{
				
					if(configSpace == null)
					{
						throw new JsonParseException("Couldn't find ParamConfigurationSpace", jp.getCurrentLocation());
					} 
				
					ParamConfiguration config = configSpace.getDefaultConfiguration();
					config.putAll(settings);
				
					if(pc_id >0)
					{
						cache.put(pc_id, config);
					}
					return config;
				}
			
		
			
		
		}

	}
	
	public static class ParamConfigurationSerializer extends StdSerializer<ParamConfiguration>
	{

		


		protected ParamConfigurationSerializer() {
			super(ParamConfiguration.class);
		}


		private final ConcurrentHashMap<ParamConfiguration, Integer> map = new ConcurrentHashMap<ParamConfiguration, Integer>();
		
		private final AtomicInteger idMap = new AtomicInteger(1);
		

		@Override
		public void serialize(ParamConfiguration value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException 
		{
			jgen.writeStartObject();
			
	
			boolean firstWrite = (map.putIfAbsent(value,idMap.incrementAndGet()) == null);
			
			Integer id = map.get(value);		
			jgen.writeObjectField(PC_ID,id);
		
			if(firstWrite)
			{
				jgen.writeObjectField(PC_PCS, value.getConfigurationSpace());
				jgen.writeObjectField(PC_SETTINGS, new LinkedHashMap<String,String>(value));
				jgen.writeObjectField(PC_ACTIVE_PARAMETERS, value.getActiveParameters());
				
				jgen.writeObjectField(PC_FORBIDDEN, value.isForbiddenParamConfiguration());
				
				jgen.writeObjectField(PC_DEFAULT, value.getConfigurationSpace().getDefaultConfiguration().equals(value));
				
			} 
			
			jgen.writeEndObject();
		}
		
	}
	
	public static class ParamConfigurationSpaceDeserializer extends StdDeserializer<ParamConfigurationSpace>
	{

		
		protected ParamConfigurationSpaceDeserializer() {
			super(ParamConfigurationSpace.class);
		
		}

		@Override
		public ParamConfigurationSpace deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			
			
			if(!jp.getCurrentToken().equals(JsonToken.START_OBJECT))
			{
				throw new JsonParseException("Expected start object", jp.getCurrentLocation());
			}
			
			@SuppressWarnings("unchecked")
			Map<Integer, ParamConfigurationSpace> cache = (Map<Integer, ParamConfigurationSpace>) ctxt.getAttribute(JACKSON_PCS_CONTEXT);
			
			if(cache == null)
			{
				cache = new ConcurrentHashMap<Integer, ParamConfigurationSpace>();
				ctxt.setAttribute(JACKSON_PCS_CONTEXT, cache);
			}
			
			
			
			String pcsText = null;
			String pcsFilename = null;
			Map<String, String> subspace = new TreeMap<String,String>();
			
			int pcs_id = 0;
			
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
					case PCS_TEXT:
						pcsText = jp.getValueAsString();
						break;
					case PCS_FILENAME:
						pcsFilename = jp.getValueAsString();
						break;
					case PCS_SUBSPACE:
						
						JsonNode node = jp.getCodec().readTree(jp);
						
						Iterator<Entry<String, JsonNode>> i = node.fields();
						
						while(i.hasNext())
						{
							Entry<String, JsonNode> ent = i.next();
							subspace.put(ent.getKey(), ent.getValue().asText());
						}
						
						break;
						
					case PCS_ID:
						pcs_id = jp.getValueAsInt();
						break;
				default:
					break;
					
				}
			}
			
			
			
			
			
			
			
			
			if(cache.get(pcs_id) != null)
			{
				return cache.get(pcs_id);
					
			} else
			{
			
				
				ParamConfigurationSpace configSpace =  new ParamConfigurationSpace(new StringReader(pcsText), pcsFilename, subspace);
				
				
				if(pcs_id > 0)
				{
					cache.put(pcs_id, configSpace);
				}
				
				
				
				return configSpace;
			}
			
		}

	}
	
	public static class ParamConfigurationSpaceSerializer extends StdSerializer<ParamConfigurationSpace>
	{


		

		protected ParamConfigurationSpaceSerializer() {
			super(ParamConfigurationSpace.class);
		}

		private final ConcurrentHashMap<ParamConfigurationSpace, Integer> map = new ConcurrentHashMap<ParamConfigurationSpace, Integer>();
		
		private final AtomicInteger idMap = new AtomicInteger(1);
		
		@Override
		public void serialize(ParamConfigurationSpace value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException 
		{
			

			boolean firstWrite = (map.putIfAbsent(value,idMap.incrementAndGet()) == null);
					
			Integer id = map.get(value);		
			jgen.writeStartObject();
			jgen.writeObjectField(PCS_ID,id);
			if(firstWrite)
			{
				jgen.writeObjectField(PCS_FILENAME, value.getParamFileName());
				jgen.writeObjectField(PCS_TEXT, value.getPCSFile());
				jgen.writeObjectField(PCS_SUBSPACE, value.getSearchSubspace());
			} 
			
			
			jgen.writeEndObject();
			
		}
		
	}
	
}
