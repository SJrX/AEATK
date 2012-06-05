package ca.ubc.cs.beta.config;


import java.lang.annotation.*;

/**
 * Mark a parameter as a ParameterFile if it's File reference should be parsed as a property file, turned to a String[] and then reparsed by JCommander
 * @author seramage
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ParameterFile {

	
}
