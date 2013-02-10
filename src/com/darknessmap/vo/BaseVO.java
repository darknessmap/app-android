package com.darknessmap.vo;

import com.google.gson.Gson;



/**
 * Base Value Object class. 
 * We use this to serialize our POJO as a JSON string...
 * 
 * @author emilianoburgos
 *
 */
public abstract class BaseVO {
	
	static protected Gson GSON = new Gson();
	
	public BaseVO()
	{
		super();
	}
	
	/**
	 * Returns the json string representation
	 * of this object.
	 * 
	 * @return	String
	 */
	public String toJson()
	{
		// convert java object to JSON format,
		// and returned as JSON formatted string
		return GSON.toJson(this);
	}
}