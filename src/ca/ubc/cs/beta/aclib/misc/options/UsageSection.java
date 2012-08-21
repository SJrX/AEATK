package ca.ubc.cs.beta.aclib.misc.options;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Stores everything for a "Section" of the Usage Screen
 * 
 * @author Steve Ramage 
 *
 */
public class UsageSection implements Iterable<String> {
	private final String name;
	private final String description;
	private final Map<String, String> attributesToDescriptionMap = new TreeMap<String, String>();
	private final Set<String> requiredAttributes = new HashSet<String>();
	private final Map<String, String> defaultValues = new TreeMap<String, String>();
	private final Map<String, String> domainMap = new TreeMap<String, String>();
	private final Map<String, String> aliasMap = new TreeMap<String, String>();
	private final boolean hidden;
	
	/**
	 * Constructs a new usage section
	 * @param sectionName 			The name of this section
	 * @param sectionDescription 	The Description of this section
	 * @param hidden				<code>true</code> if we shouldn't display the sectionName or description when displaying options.
	 */
	public UsageSection(String name, String description, boolean hidden)
	{
		this.name = name;
		this.description = description;
		this.hidden = hidden;
	}
	
	
	public String getSectionName()
	{
		return name;
	}
	
	public String getSectionDescription()
	{
		return description;
	}
	
	/**
	 * Add an attribute to this section
	 * @param name			Name of the attribute
	 * @param description	Description of the attribute
	 * @param defaultValue	Default value of the attribute
	 * @param required		<code>true</code> if this attribute is required
	 * @param domain		A human readable string that tells us what arguments are allowed
	 * @param allAliases	A human readable string that tells us about all the aliases for the name
	 */
	public void addAttribute(String name, String description, String defaultValue, boolean required, String domain, String allAliases)
	{
		if(name == null) throw new IllegalArgumentException("name can't be null");
		name = name.trim();
		if(description != null)	description = description.trim();
		if(defaultValue != null) defaultValue = defaultValue.trim();
		
		attributesToDescriptionMap.put(name, description);
		defaultValues.put(name, defaultValue);
		if(required)
		{
			requiredAttributes.add(name);
		}
		
		domainMap.put(name, domain);
		aliasMap.put(name,allAliases);
	}

	@Override
	public Iterator<String> iterator() {
		return attributesToDescriptionMap.keySet().iterator();
	}
	
	public boolean isAttributeRequired(String name)
	{
		return requiredAttributes.contains(name);
	}
	
	public String getAttributeDescription(String name)
	{
		return attributesToDescriptionMap.get(name);
	}
	
	public String getAttributeDefaultValues(String name)
	{
		return defaultValues.get(name);
	}
	
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Name:").append(name).append("\n");
		sb.append("Description:").append(description).append("\n");
		
		for(String s : this)
		{
			sb.append("\t").append(s).append(" ").append(getAttributeDescription(s)).append("\n");
			sb.append("\t");
			if(isAttributeRequired(s))
			{
				sb.append("[R] ");
			}
			sb.append("Default Value:").append(getAttributeDefaultValues(s)).append("\n");
			sb.append("\tAliases:").append(getAttributeAliases(s)).append("\n");
			if(getAttributeDomain(s).length() > 0)
			{
				
				sb.append("\tDomain:").append(getAttributeDomain(s)).append("\n");
			}
			sb.append("\n");
		}
		
		
		
		return sb.toString();
	}

	public String getAttributeAliases(String s) {

		return aliasMap.get(s);
	}
	
	public String getAttributeDomain(String s)
	{
		return domainMap.get(s);
	}

	public boolean isSectionHidden() {

		return hidden;
	}

}
