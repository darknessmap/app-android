package com.darknessmap.uid;

import java.util.UUID;

public class Session {
	
	private static String sID = null;

    public synchronized static String id() 
    {
        if (sID == null) sID = UUID.randomUUID().toString();
        
        return sID;
    }
}
