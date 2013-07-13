package ca.ubc.cs.beta.aclib.options.docgen;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.ubc.cs.beta.aclib.misc.options.DomainDisplay;
import ca.ubc.cs.beta.aclib.misc.options.NoArgumentHandler;
import ca.ubc.cs.beta.aclib.misc.options.NoopNoArgumentHandler;
import ca.ubc.cs.beta.aclib.misc.options.UsageSection;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

public class UsageSectionGenerator {

	
	
	
	private static boolean hasSlept = false;	

	/**
	 * Retrieves the Usage Section objects for objects given in both sets of argument
	 * @param o			object to inspect
	 * @return
	 */
	public static List<UsageSection> getUsageSections(Object o) 
	{
			
		try {
		Set<Object> objectsToScan = new LinkedHashSet<Object>();
		Map<Object, Set<Object>> parentToChildMap = new LinkedHashMap<Object,Set<Object>>();
		
		Map<String, Object> classesToScan = new LinkedHashMap<String, Object>();
		Set<Object> related = new HashSet<Object>();
		
		getAllObjects(o, classesToScan, parentToChildMap, related);
		
		objectsToScan.addAll(classesToScan.values());
		
		Set<String> claimRequired = getAllClaimedRequired(objectsToScan);
		
		List<UsageSection> sections = new ArrayList<UsageSection>();
		
		for(Object obj : objectsToScan)
		{
			
			UsageTextField utf = getLatexField(obj);
			NoArgumentHandler handler = new NoopNoArgumentHandler();
			if(utf == null)
			{
				System.err.println("Class " + obj.getClass()  + " does not have a UsageTextField annotation, this is very ugly for users to deal. Sleeping for 5 seconds");
				
				if(!hasSlept)
				{
					try {
						Thread.sleep(5000);
					} catch(InterruptedException e)
					{
						Thread.currentThread().interrupt();
					}
					hasSlept = true;
				}
			} else
			{
				
				try {
					 handler = utf.noarg().newInstance();
				} catch(InstantiationException e)
				{
					System.err.println("Couldn't create no-argument handler, are you sure that it has default (zero-argument) constructor :"+  e.getMessage());
					throw e;
				} 
				
			}
			
			String title = getTitleForObject(obj);
			String titleBanner = getSectionBannerForObject(obj);
			String sectionDescription = getDescriptionForObject(obj);
			boolean isHidden = isHiddenSection(obj);
			
			
			UsageSection sec = new UsageSection(title, titleBanner, sectionDescription,isHidden, obj, handler, related.contains(obj));
			sections.add(sec);
			
			
			for(Field f : obj.getClass().getDeclaredFields())
			{
				boolean notAccessible = !f.isAccessible();
				
				if(notAccessible) f.setAccessible(true);
				
				if(f.isAnnotationPresent(Parameter.class))
				{
					
					Parameter param = getParameterAnnotation(f);
					
					String name = getNameForField(f);
					String defaultValue = getDefaultValueForField(f,obj);
					String description = getDescriptionForField(f,obj);
					boolean required = getRequiredForField(f,obj);
					
					
					String aliases = getAliases(f, obj);
					
					String[] possibleAliases = aliases.split(",\\s+");
					for(String possibleAlias : possibleAliases)
					{
						if(claimRequired.contains(possibleAlias.trim()))
						{
							required = true;
						}
					}
					
					
					
					String domain = getDomain(f,obj);
					boolean hidden = param.hidden();
					
					sec.addAttribute(name, description, defaultValue, required,domain, aliases , hidden);
					
				}
				
				if(f.isAnnotationPresent(DynamicParameter.class))
				{
					DynamicParameter dynamicParam = getDynamicParameterAnnotation(f);
					String name = getNameForDynamicField(f);
					String description = getDescriptionForDynamicField(f,obj);
					boolean required = getRequiredForDynamicField(f,obj);
					String aliases = getDynamicAliases(f, obj);
					
					String[] possibleAliases = aliases.split("\\s+");
					for(String possibleAlias : possibleAliases)
					{
						if(claimRequired.contains(possibleAlias.trim()))
						{
							required = true;
						}
					}
					
					
					String domain = getDomain(f,obj);
					boolean hidden = dynamicParam.hidden();
					
					sec.addAttribute(name, description, "", required,domain, aliases , hidden);
				}
				if(!notAccessible) f.setAccessible(false);
				
			}
			//System.out.println(sec);
			
		}
		
		//Merge hidden sections with there parent
		
		//This is buggy as multiple levels of the hierarchy won't get merged in but oh well
		List<UsageSection> returningSec = new ArrayList<UsageSection>();
		
		List<UsageSection> postSec = new ArrayList<UsageSection>();
		for(UsageSection sec : sections)
		{
			Object parent = sec.getObject();
			
			for(UsageSection sec2 : sections)
			{
				Object child = sec2.getObject();
				//Not related
				//System.out.println(parent.getClass() + " and " + child.getClass());
				
				if(parentToChildMap.get(parent) == null) continue;
				if(!parentToChildMap.get(parent).contains(child)) continue;
				
				if(sec2.isSectionHidden())
				{
					//System.out.println(sec2 + " is hidden adding to " + sec);
					for(String secName : sec2)
					{
						sec.addAttribute(secName, sec2.getAttributeDescription(secName), sec2.getAttributeDefaultValues(secName), sec2.isAttributeRequired(secName),sec2.getAttributeDomain(secName), sec2.getAttributeAliases(secName), sec2.isAttributeHidden(secName));
					}
				}
				
				
				
			}
			
			if(!sec.isSectionHidden())
			{
				if(related.contains(parent))
				{
					postSec.add(sec);
				} else
				{
					returningSec.add(sec);
				}
			}
			
		}
		
		
		

		returningSec.addAll(postSec);
		return returningSec;
		
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Unexpected Exception Occurred ", e);
		} catch (InstantiationException e) {
			throw new IllegalStateException("Unexpected Exception Occurred ", e);
		}
		
		
		
	}
	
	/**
	 * Retrieves the Usage Section objects for objects given in both sets of argument
	 * @param o			obj to inspect
	 * @param options	map whose values we will inspect
	 * @return
	 */
	public static List<UsageSection> getUsageSections(Object o, Map<String, AbstractOptions> options) 
	{
	
		ArrayList<Object> allOptions = new ArrayList<Object>();
		
		allOptions.add(o);
		for(Entry<String, AbstractOptions> ent : options.entrySet())
		{
			if(ent.getValue() != null)
			{
				allOptions.add(ent.getValue());
			}
		}
		return getUsageSections(allOptions.toArray());
	}
	

	private static void getAllObjects(Object o, Map<String, Object> objectsToScan, Map<Object, Set<Object>> parentToChildMap, Set<Object> related) 
	{
		if(parentToChildMap.get(o) == null)
		{
			parentToChildMap.put(o, new HashSet<Object>());
		}
		try {
			if(o.getClass().isArray())
			{
				for(int i=0; i < Array.getLength(o); i++)
				{
					getAllObjects(Array.get(o, i), objectsToScan, parentToChildMap, related);
					
				}
			} else 
			{	
				Object previousValue = objectsToScan.put(o.getClass().getCanonicalName(),o);
				if(previousValue != null)
				{ //We already added this value to the set
					return;
				}
				
				for(Field f : o.getClass().getDeclaredFields())
				{
					boolean markInaccessible = !f.isAccessible();
					f.setAccessible(true);
					if(f.isAnnotationPresent(ParametersDelegate.class))
					{	
						
						parentToChildMap.get(o).add(f.get(o));
						if(related.contains(o))
						{
							related.add(f.get(o));
						}
						getAllObjects(f.get(o), objectsToScan, parentToChildMap, related);
					}
					
					if(f.isAnnotationPresent(UsageTextField.class))
					{
						UsageTextField utf = f.getAnnotation(UsageTextField.class);
						if(utf.converterFileOptions().equals(Object.class))
						{
							continue;
						} else
						{
							try {
								Object o2 = utf.converterFileOptions().newInstance();
								//parentToChildMap.get(o).add(o2);
								related.add(o2);
								getAllObjects(o2, objectsToScan, parentToChildMap, related);
							} catch(InstantiationException e)
							{
								System.err.println("Couldn't create new instance of " + utf.converterFileOptions().getCanonicalName() + " this class needs to have a default (zero-arg) constructor if it is to be a related option");
							}
							
							
						}
						
					}
					f.setAccessible(markInaccessible);
				}
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Unexpected Exception Occurred ", e);
		}
		
	}
	

	
	private static Set<String> getAllClaimedRequired(Set<Object> objectsToScan) {
		HashSet<String> claimRequired = new HashSet<String>();
		for(Object o : objectsToScan)
		{
			if(o.getClass().isAnnotationPresent(UsageTextField.class))
			{
				UsageTextField t = (UsageTextField) o.getClass().getAnnotation(UsageTextField.class);
				claimRequired.addAll(Arrays.asList(t.claimRequired()));
			}
		}
		return claimRequired;
	}

	private static boolean getRequiredForField(Field f, Object o) {
		return getParameterAnnotation(f).required();
		
	}



	private static String getDescriptionForField(Field f, Object o) {
		return getParameterAnnotation(f).description();
	}
	
	private static boolean getRequiredForDynamicField(Field f, Object o) {
		return getDynamicParameterAnnotation(f).required();
		
	}



	private static String getDescriptionForDynamicField(Field f, Object o) {
		return getDynamicParameterAnnotation(f).description();
	}

	



	private static String getDefaultValueForField(Field f, Object o) throws IllegalArgumentException, IllegalAccessException {
		
		UsageTextField latexAnnotation = getLatexField(f);
		if((latexAnnotation == null) || latexAnnotation.defaultValues().equals("<NOT SET>"))
		{
			Object value = f.get(o);
			
			if(value != null)
			{
				return value.toString();
			} else
			{
				return "null";
			}
			
		} else
		{
			return latexAnnotation.defaultValues();
		}
		
		
		
	}


	private static String getNameForDynamicField(Field f) {

		return getDynamicParameterAnnotation(f).names()[0];
	}


	private static String getNameForField(Field f) {

		return getParameterAnnotation(f).names()[0];
	}

	
	private static String getAliases(Field f, Object o) {

		return Arrays.toString(getParameterAnnotation(f).names()).replaceAll("\\[", "").replaceAll("\\]","");
	}
	
	private static String getDynamicAliases(Field f, Object o) {

		return Arrays.toString(getDynamicParameterAnnotation(f).names()).replaceAll("\\[", "").replaceAll("\\]","");
	}


	private static String getDomain(Field f, Object o) throws InstantiationException, IllegalAccessException {

		
		UsageTextField latex = getLatexField(f);
		
		if(latex != null && !latex.domain().equals("<NOT SET>"))
		{
			return latex.domain();
		}
		
		if(getParameterAnnotation(f) != null)
		{
			if(DomainDisplay.class.isAssignableFrom(getParameterAnnotation(f).converter()))
			{
				return ((DomainDisplay) getParameterAnnotation(f).converter().newInstance()).getDomain();
			}
			
			if(DomainDisplay.class.isAssignableFrom(getParameterAnnotation(f).validateWith()))
			{
				return ((DomainDisplay) getParameterAnnotation(f).validateWith().newInstance()).getDomain();
			}
		}
		
		Object value = f.get(o);
		if(value != null)
		{
			return getDomainForClass(value.getClass());
		} else
		{
			return getDomainForClass(f.getType());
		}
		
		
		
		
		
	}

	private static String getDomainForClass(Class<?> x)
	{
		if(x.equals(Boolean.class))
		{
			return "{true, false}";
		}
		
		if(Enum.class.isAssignableFrom(x))
		{
			//SortedSet<String> options = new TreeSet<String>();
			
			//for(x.getDeclaredFields())
			return Arrays.toString(x.getEnumConstants()).replaceAll("\\[", "{").replaceAll("\\]","}");
		}
		
		return "";
	}
	private static Parameter getParameterAnnotation(Field f)
	{
		Parameter param  = (Parameter)f.getAnnotation(Parameter.class);
		return param;
	}

	private static DynamicParameter getDynamicParameterAnnotation(Field f)
	{
		DynamicParameter param  = (DynamicParameter)f.getAnnotation(DynamicParameter.class);
		return param;
	}

	
	private static String getTitleForObject(Object obj) {

		UsageTextField f = getLatexField(obj);
		if(f == null) return "";
		return f.title();
		
		}
	
	private static String getSectionBannerForObject(Object obj)
	{
		UsageTextField f = getLatexField(obj);
		if( f == null) return "*****[  ]*******";
		return f.titlebanner();
	}
	
	private static String getDescriptionForObject(Object obj) {

		UsageTextField f = getLatexField(obj);
		if(f == null) return "";
		return f.description();
		
		}
	

	private static boolean isHiddenSection(Object obj) {

		UsageTextField f = getLatexField(obj);
		if(f == null) return false;
		return f.hiddenSection();
		
		}
	
	

	private static UsageTextField getLatexField(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof Field)
		{
			UsageTextField f = ((Field)obj).getAnnotation(UsageTextField.class);
			return f;
		} else if(obj.getClass().isAnnotationPresent(UsageTextField.class))
		{
			UsageTextField f = obj.getClass().getAnnotation(UsageTextField.class);
			
			
			return f;
		} else
		{
			return null;
		}
	}
		
	
	
	
}
