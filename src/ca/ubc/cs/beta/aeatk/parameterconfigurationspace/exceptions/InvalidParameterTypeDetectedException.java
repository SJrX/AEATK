package ca.ubc.cs.beta.aeatk.parameterconfigurationspace.exceptions;

/**
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 11/6/15
 */
public class InvalidParameterTypeDetectedException extends IllegalArgumentException {
    public InvalidParameterTypeDetectedException(String s) {
        super(s);
    }
}
