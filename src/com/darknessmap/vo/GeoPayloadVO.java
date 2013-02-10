package com.darknessmap.vo;


import java.util.HashMap;

import android.location.Location;

/**
 * Basic geolocated payload. Stored with mongodb.
 * 
 * TODO: Review order, lat:lon or lon:lat. Also, what are the
 * formats on loc:[]? Perhaps better to save as Object.
 * 
 * @author emilianoburgos
 *
 */
public class GeoPayloadVO extends BaseVO {
	
	
	@SuppressWarnings("unused")
	private double time;
	
	@SuppressWarnings("unused")
	private float  accuracy;
	
	@SuppressWarnings("unused")
	private HashMap<String, Double> loc;
	
	@SuppressWarnings("unused")
	private String payloadType;
	
	private Object payload;
	
	
	private String uid;
	private String sid;
	
	/**
	 * 
	 * @param location
	 */
	public void setLocation(Location location)
	{
		this.time 		= location.getTime();
		this.accuracy 	= location.getAccuracy();
		
		double longitude 	= location.getLongitude();
		double latitude 	= location.getLatitude();
		
		this.loc = new HashMap<String, Double>();
		this.loc.put("lat", Double.valueOf(latitude));
		this.loc.put("lon", Double.valueOf(longitude));
		
		
		this.payloadType = "geo";
		
	}
	
	/**
	 * 
	 * @param uid
	 */
	public void setUid(String uid)
	{
		this.uid = uid;
	}
	
	/**
	 * 
	 * @param sid
	 */
	public void setSid(String sid)
	{
		this.sid = sid;
	}
	
	/**
	 * Returns the payload data object.
	 * @return	Object	
	 */
	public Object getPayload() { return payload; }
	
	public void setPayload(Object payload) {
		this.payload = payload;
	}

}
