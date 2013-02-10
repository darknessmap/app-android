package com.darknessmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

public class SplashActivity extends Activity {
	
	//how long until we go to the next activity
    protected int _splashTime = 1000; 

    Handler _handler;
    Runnable _action;
    
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash);
		
		final SplashActivity sPlashScreen = this; 
		
		_handler = new Handler();
	    _action  = new Runnable(){
	        @Override
	        public void run(){
	            Intent intent = new Intent(sPlashScreen,DarknessMap.class);
	            startActivity(intent);
	            finish();
	        }
	    };

	    _handler.postDelayed(_action, _splashTime);
	}
	
	//Function that will handle the touch
    @Override
    public boolean onTouchEvent(MotionEvent event) 
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN) 
        {
            _action.run();
            _handler.removeCallbacks(_action);
        }
        
        return true;
    }

}
