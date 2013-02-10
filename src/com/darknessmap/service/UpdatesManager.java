package com.darknessmap.service;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.paulm.jsignal.Signal;
import com.paulm.jsignal.SignalException;

import android.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * @see http://www.androidsnippets.com/check-for-updates-once-a-day
 * @author emilianoburgos
 *
 */
public class UpdatesManager {
	
	private Context _context;
	
	private String _appLink;
	private String _api;
	private String _vn;
	private int _vc;
	
	/**
	 * 
	 * @param context
	 * @param api
	 */
	public UpdatesManager(Context context, String api){
		_api = api;
		_context = context;
	}
	
	/**
	 * 
	 */
	public void checkForUpdates()
	{
		_getAppVersion();
		_checkForUpdates();
	}
	
	protected void _getAppVersion()
	{
		String pname = _context.getPackageName();
		PackageInfo pInfo;
		try {
			pInfo = _context.getPackageManager().getPackageInfo(pname, 0);
			_vn = pInfo.versionName;
			_vc = pInfo.versionCode; 
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	/**
	 * 
	 */
	protected void _checkForUpdates()
	{
		//ping the API to check for the latest api.
		PingAPI service = new PingAPI();
		
		try
		{
			// on response, check version...
			service.response.add(this, "checkVersion");
		}
		catch (SignalException e)
		{
			e.printStackTrace();
		}
	    service.execute(_api);
		
	}
	
	/**
	 * 
	 * @param latest
	 */
	public void checkVersion(String latest){
		//not ideal, basically we want to be in sync.
		if( _vn == latest) return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(_context);
		builder.setTitle("Upgrade");
		builder.setIcon(R.drawable.ic_dialog_info);
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(_appLink));
				_context.startActivity(intent); 
			}
		});
		
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private class PingAPI extends AsyncTask<String, Void, String> {
		
		
        private final HttpClient _client = new DefaultHttpClient();
        private String _content;
        private String _error = null;
        
        /**
		 * 
		 */
		protected final Signal response = new Signal();
		
		/**
		 * 
		 */
		protected String doInBackground(String... urls) {
            try {
                HttpGet httpget = new HttpGet(urls[0]);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                
                _content = _client.execute(httpget, responseHandler);
                
            } catch (ClientProtocolException e) {
            	_error = e.getMessage();
                cancel(true);
            } catch (IOException e) {
            	_error = e.getMessage();
                cancel(true);
            }
            
            return _content;
        }
        
		/**
		 * 
		 */
        protected void onPostExecute(String result) {
            if (_error != null) {
            	try
    			{
            		response.dispatch(_content);
    			}
    			catch (SignalException e){}
            } else {
            	System.out.println("UpdaetdManager Source: " + _error.toString());
            }
        }
        
    }
}
