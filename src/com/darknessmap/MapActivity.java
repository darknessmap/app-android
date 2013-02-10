package com.darknessmap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 
 * @author emilianoburgos
 *
 */
public class MapActivity extends Activity {
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.map);
		this._setUpImageHandler();
	}
	
	private void _setUpImageHandler() {
		// TODO Auto-generated method stub
		ImageView img = (ImageView) findViewById(R.id.splashImage);
		img.setOnClickListener(new View.OnClickListener(){
		    public void onClick(View v){
		        Intent intent = new Intent();
		        intent.setAction(Intent.ACTION_VIEW);
		        intent.addCategory(Intent.CATEGORY_BROWSABLE);
		        //TODO: Ugly!! Make all Activities extend base, which
		        //loads config object.
		        intent.setData(Uri.parse("http://darknessmap.com"));
		        startActivity(intent);
		    }
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		//menu.add(1, 1, Menu.FIRST+1, "Stop");
		menu.add(1, 1, Menu.FIRST+1, "Start");
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case 1:
				Toast.makeText(this, "Button Start", Toast.LENGTH_SHORT).show();
				this._goToDarknessmap();
				return true;
				
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void _goToDarknessmap() {
		// TODO Auto-generated method stub
		Intent darkActivity = new Intent(MapActivity.this, DarknessMap.class);
        startActivity(darkActivity);
	}

	@Override
	public void onBackPressed() {
		this._goToDarknessmap();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	//Below API level 5
	    	this._goToDarknessmap();
	    }

	    return super.onKeyDown(keyCode, event);
	}
}
