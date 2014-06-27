package ca.ubc.cs.beta.aeatk.json.serializers;

import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.Impl;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

public class ImprovedObjectMapper extends ObjectMapper {

	public ImprovedObjectMapper(JsonFactory jfactory) {
		super(jfactory,new DefaultSerializerProvider.Impl(), new ImplX(BeanDeserializerFactory.instance));
	}

	public JsonDeserializer<?> getDeserializer(DeserializationContext ctxt, JavaType valueType) throws JsonMappingException
	{
		return super._findRootDeserializer(ctxt, valueType);
	}

	/**
     * Actual full concrete implementation
     */
    public final static class ImplX extends DefaultDeserializationContext
    {
        private static final long serialVersionUID = 1L;

        private final ConcurrentHashMap<Class<?>, JsonDeserializer<Object>> cache = new ConcurrentHashMap<>();
        /**
         * Default constructor for a blueprint object, which will use the standard
         * {@link DeserializerCache}, given factory.
         */
        public ImplX(DeserializerFactory df) {
        	
            super(df, null);
            System.err.println("A");
        }

        protected ImplX(ImplX src,
                DeserializationConfig config, JsonParser jp, InjectableValues values) {
        	
            super(src, config, jp, values);
            System.err.println("B");
        }

        protected ImplX(ImplX src, DeserializerFactory factory) {
        	
            super(src, factory);
            System.err.println("C");
        }
        
        
        @Override
        public DefaultDeserializationContext createInstance(DeserializationConfig config,
                JsonParser jp, InjectableValues values) {
            return new ImplX(this, config, jp, values);
        }

        @Override
        public DefaultDeserializationContext with(DeserializerFactory factory) {
            return new ImplX(this, factory);
        } 
        
        @Override
        public JsonDeserializer<Object> deserializerInstance(Annotated annotated, Object deserDef) throws JsonMappingException
        {
        	
        	if(deserDef instanceof Class<?>)
        	{
        		Class<?> deserClass = (Class<?>) deserDef;
        		
        		if(deserClass.getPackage().getName().contains("ca.ubc.cs.beta.aeatk"))
        		{
        			synchronized(this)
        			{
        				if(cache.containsKey(deserClass))
        				{
        					System.err.println("Cache Hit");
        					return cache.get(deserClass);
        				} else
        				{
        					System.err.println("Cache Miss");
        					JsonDeserializer<Object> deserializer = super.deserializerInstance(annotated, deserDef);
        					cache.put(deserClass, deserializer);
        					return deserializer;
        				}
        			}
        		}
        	} 
        	
        	return super.deserializerInstance(annotated, deserDef);
        }
                
             
    }
}
