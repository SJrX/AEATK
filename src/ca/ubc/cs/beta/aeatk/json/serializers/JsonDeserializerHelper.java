package ca.ubc.cs.beta.aeatk.json.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonDeserializerHelper {

	@SuppressWarnings("unchecked")
	public static <T> T getDeserializedVersion(JsonParser jp, DeserializationContext ctxt, Class<?> cls) throws JsonMappingException, JsonProcessingException, IOException
	{
		if(jp.getCodec() instanceof ImprovedObjectMapper)
		{
			
			ImprovedObjectMapper mapper = (ImprovedObjectMapper) jp.getCodec();
			return (T) mapper.getDeserializer(ctxt, ctxt.getTypeFactory().constructType(cls)).deserialize(jp, ctxt);
		} else
		{
			throw new JsonMappingException("Unfortuntately these convertors only work if the codec is an Improved Object Mapper at this time. The problem is that the caches are associated with specific instances of the serializer");
		}
	}
}
