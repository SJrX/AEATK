package ca.ubc.cs.beta.models.surrogate.helpers.jcommander.validator;

import java.io.File;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class IsWriteableDirectoryValidator implements IStringConverter<File> {

	  public File convert(String value) {
		 
		File f = new File(value);
		
		if (!f.isDirectory())
		{
			throw new ParameterException(value + " is not a directory");
		}
		
		if (!f.canWrite())
		{
			throw new ParameterException(value + " is not writable");
		}
		
		return f;
	     
	  } 

}
