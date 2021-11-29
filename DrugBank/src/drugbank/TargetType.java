package drugbank;
/**
 * <copyright>
 *
 * Copyright (c) Alexandru Topirceanu. alex.topirceanu@yahoo.com All rights
 * reserved.
 *
 * File created by Alexander Mar 27, 2020 11:17:32 AM </copyright>
 */


import java.util.HashMap;
import java.util.Map;

public class TargetType
{
	private String ID;
	private String name;
	private Map<String, Object> properties = new HashMap<String, Object>();

	public TargetType(String ID, String name)
	{
		this.ID = ID;
		this.name = name;
	}

	public void setPropertyValue(String name, Object value)
	{
		properties.put(name, value);
	}

	public String getName()
	{
		return name;
	}

	public String getID()
	{
		return ID;
	}

	public Map<String, Object> getProperties()
	{
		return properties;
	}

	public Object getPropertyValue(String name)
	{
		return properties.get(name);
	}

	@Override
	public String toString()
	{
		return name + " [" + ID + "]";
	}
}
