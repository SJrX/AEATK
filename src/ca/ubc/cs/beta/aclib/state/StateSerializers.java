package ca.ubc.cs.beta.aclib.state;

/**
 * Enumeration that lists the various StateSerializers supported 
 * 
 * @author sjr
 *
 */
public enum StateSerializers {
	/**
	 * Null State Serializer basically returns empty objects and does nothing when saving
	 */
	NULL,
	
	/**
	 * State Serializers used for MATLAB format to disk 
	 */
	LEGACY
}
