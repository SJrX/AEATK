package ca.ubc.cs.beta.aclib.misc.options;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface UsageTextField {

	String description() default "" ;
	
	String defaultValues()  default "<NOT SET>";

	String title() default "";

	String domain() default "<NOT SET>";

	boolean hiddenSection() default false;
	
}
