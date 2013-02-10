package com.darknessmap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.darknessmap.service.Gateway;
import com.darknessmap.uid.Installation;
import com.darknessmap.uid.Session;
import com.darknessmap.vo.GeoPayloadVO;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.config.PropertyConfigurator;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.paulm.jsignal.Signal;
import com.paulm.jsignal.SignalException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
//import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * TODO: Add UI to start/stop app.
 * TODO: Throttle the location updates, slow down. Kills battery.
 * 		 @see: http://stackoverflow.com/questions/7872863/keeping-a-gps-service-alive-and-optimizing-battery-life
 * 		 We can use location.getSpeed() to know the rate at which the user moves, and then request updates based 
 * 		 on that speed.
 * 		 @see: http://code.google.com/p/krvarma-android-samples/source/browse/#svn%2Ftrunk%2FGPSLogger%2Fsrc%2Fcom%2Fvarma%2Fsamples%2Fgpslogger%2Fui
 * 
 * MONGODB GEOSPATIAL
 * RELATED: http://www.mongodb.org/display/DOCS/Geospatial+Indexing#GeospatialIndexing-TheEarthisRoundbutMapsareFlat
 * RELATED: https://openshift.redhat.com/community/blogs/spatial-mongodb-in-openshift-be-the-next-foursquare-part-1
 * 
 * NODEJS
 * RELATED: http://stackoverflow.com/questions/4518563/java-client-and-node-js-server
 * RELATED: http://www.ibm.com/developerworks/java/library/j-nodejs/index.html << Node.js, mongodb.
 * 
 * DRAWING OPTIMIZATION:
 * RELATED: http://stackoverflow.com/questions/2487148/drawing-to-the-canvas
 * RELATED: http://stackoverflow.com/questions/4329663/how-to-optimize-canvas-drawing-drawbitmap-on-android
 * 
 * HISTOGRAMS:
 * RELATED: http://code.google.com/p/histometer/source/browse/trunk/src/swn/nu/cg/histometer/ClassicHistometerView.java
 * RELATED: 
 * 
 * UI, VIEWPAGER:
 * RELATED: http://android-developers.blogspot.com/2011/08/horizontal-view-swiping-with-viewpager.html
 * 
 * UI, MENU SWAP ITEMS
 * http://thedevelopersinfo.wordpress.com/2009/10/20/dynamically-change-options-menu-items-in-android/
 * 
 * UI, GRAPH
 * http://www.jjoe64.com/p/graphview-library.html
 * 
 * @author emilianoburgos
 *
 */
public class DarknessMap extends Activity implements LocationListener
{    
	private Preview   mPreview;
	private DrawOnTop mDrawOnTop;

	//added from GPS Test
	private LocationManager lm;
//	private StringBuilder sb;
//	private TextView txtInfo;
	
	/*
	 * This should we moved into a separate
	 * entity.
	 * It will be the basis of our system.
	 */
	double time;
	float  accuracy;
	double longitude;
	double latitude;
	Object payload;
	
	/**
	 * Used to set the sampling rate
	 * on the GPS provider. Unit milliseconds.
	 * Average human walking speed 
	 * ~= 5km/h => 1.4m/s.
	 */
	private int minTime     = 10 * 1000; //14m/s
//	private int minTime     = 1 * 1000; //14m/s
	/**
	 * Unit meters.
	 */
	private int minDistance = 14;
	
	int 		noOfFixes = 0;
	
	private GeoPayloadVO _geoVO;
	
	private Properties 	_config;
	
	private Gateway 	_gateway;
	
	
	
	static final String tag = "Main"; // for Log
	
	private static final Logger logger = LoggerFactory.getLogger();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		this._init();
		this._createPayload();
		this._createGateway();
		//We need to request our first location.
		
		
		// Hide the window title.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Create our Preview view and set it as the content of our activity.
		// Create our DrawOnTop view.
		mDrawOnTop = new DrawOnTop(this);
		//register the signal listener.
		try
		{
			// add the app as a listener so that we can send regular updates
			//even if no location update happens.
			mDrawOnTop.requestUpdateSignal.add(this, "sendPayload");
		}
		catch (SignalException e)
		{
			logger.error("Request update signal add error.");
			e.printStackTrace();
		}
		
		mPreview   = new Preview(this, mDrawOnTop);
		
		setContentView(mPreview);
		
		addContentView(mDrawOnTop, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		// Location Manager for GPS
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, this.minTime, this.minDistance, this);
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(1, 1, Menu.FIRST+1, "Stop");
		//menu.add(1, 2, Menu.FIRST+1, "Start");
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case 1:
				Toast.makeText(this, "Button Stop", Toast.LENGTH_SHORT).show();
				Intent splashActivity = new Intent(DarknessMap.this, MapActivity.class);
		        startActivity(splashActivity);
		        return true;
				
			case 2:
				Toast.makeText(this, "Button Start", Toast.LENGTH_SHORT).show();
				Intent darkActivity = new Intent(DarknessMap.this, DarknessMap.class);
		        startActivity(darkActivity);
				return true;
				
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	/**
	 * Initialize payload object. Set unique app/device id and 
	 * the sessions id.
	 */
	private void _createPayload()
	{
		_geoVO = new GeoPayloadVO();
		String sid = Session.id();
		String uid = Installation.id(this.getBaseContext());
		_geoVO.setSid(sid);
		_geoVO.setUid(uid);
	}
	
	/**
	 * 
	 */
	private void _createGateway() {
		
		String api = _config.getProperty("API");
		_gateway = new Gateway();
		_gateway.setUrl(api);
		_gateway.initialize();
		
	}

	protected void _init()
	{
		//Configure our logger ;)
		//PropertyConfigurator.getConfigurator(this).configure();
		logger.debug("Ready to rumble!!");
		
		//load config file from the /res/raw directory
		Resources resources = this.getResources();
		
		try {
			InputStream rawResource = resources.openRawResource(R.raw.darknessmap);
		    _config = new Properties();
		    _config.load(rawResource);
		    System.out.println("The properties are now loaded");
		    System.out.println("properties: " + _config.getProperty("API"));
		    rawResource.close();
		    
		} catch (NotFoundException e) {
		    System.err.println("Did not find raw resource: "+e);
		} catch (IOException e) {
		    System.err.println("Failed to open microlog property file");
		}
	}
	
	@Override
	protected void onResume() {
		/*
		 * onResume is is always called after onStart, even if the app hasn't been
		 * paused
		 *
		 * add location listener and request updates every 1000ms or 10m
		 */
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, this.minTime, this.minDistance, this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		/* GPS, as it turns out, consumes battery like crazy */
		lm.removeUpdates(this);
		super.onResume();
	}


	@Override
	public void onLocationChanged(Location location) {
		logger.debug("Location changed, updated to: "+location.toString());
		
		noOfFixes++;
		
		//TODO: We have to remove dependencies on this
		//and use _geoVO.
		time      = location.getTime();
		longitude = location.getLongitude();
		latitude  = location.getLatitude();
		accuracy  = location.getAccuracy();
		
		_geoVO.setLocation(location);
		
		this.sendPayload();
		
	}
	
	/**
	 * We hit the DB with the geo info and the payload (av. brightness)
	 * It should happen every time the location changes. Also, we can 
	 * request updates periodically (i.e every x frames.)
	 */
	public void sendPayload()
	{
		logger.debug("Sending payload");
		
		//TODO: We need to include frame brightness avg as payload.
		double payload = mDrawOnTop.getAverageBrightness();
		_geoVO.setPayload(payload);
		_gateway.publish(_geoVO.toJson());
		
	}


	@Override
	public void onProviderDisabled(String provider) {
		/* this is called if/when the GPS is disabled in settings */
		Log.v(tag, "Disabled");

		/* bring up the GPS settings */
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}


	@Override
	public void onProviderEnabled(String provider) {
		Log.v(tag, "Enabled");
		Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		/* This is called when the GPS status alters */
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			Log.v(tag, "Status Changed: Out of Service");
			Toast.makeText(this, "Status Changed: Out of Service",
					Toast.LENGTH_SHORT).show();
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Log.v(tag, "Status Changed: Temporarily Unavailable");
			Toast.makeText(this, "Status Changed: Temporarily Unavailable",
					Toast.LENGTH_SHORT).show();
			break;
		case LocationProvider.AVAILABLE:
			Log.v(tag, "Status Changed: Available");
			Toast.makeText(this, "Status Changed: Available",
					Toast.LENGTH_SHORT).show();
			break;
		}
	}
	
	// ----------------------------------------------------------------------

	final class Preview extends SurfaceView implements SurfaceHolder.Callback {
			
			Camera 			mCamera;
			boolean		 	mFinished;
			DrawOnTop 		mDrawOnTop;
			SurfaceHolder 	mHolder;

			Preview(Context context, DrawOnTop drawOnTop) {
				
				super(context);

				mFinished  = false;
				mDrawOnTop = drawOnTop;

				// Install a SurfaceHolder.Callback so we get notified when the
				// underlying surface is created and destroyed.
				mHolder = getHolder();
				mHolder.addCallback(this);
				mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			}
			
			/**
			 * This is called immediately after the surface
			 * is first created. Implementations of this 
			 * should start up whatever rendering code they 
			 * desire. Note that only one thread can ever 
			 * draw into a Surface, so you should not draw 
			 * into the Surface here if your normal rendering
			 * will be in another thread.
			 */
			public void surfaceCreated(SurfaceHolder holder) {
				mCamera = Camera.open();
				try {
					
					this.setDisplayOrientation(mCamera, 90, "portrait");
					
					
//					mCamera.setDisplayOrientation(90);//Needs API level 8!
					
					mCamera.setPreviewDisplay(holder);

					// Preview callback used whenever new viewfinder frame is available
					mCamera.setPreviewCallback(new PreviewCallback() {
						public void onPreviewFrame(byte[] data, Camera camera)
						{
							if ( (mDrawOnTop == null) || mFinished )
								return;

							if (mDrawOnTop.mBitmap == null)
							{
								// Initialize the draw-on-top companion
								Camera.Parameters params = camera.getParameters();
								mDrawOnTop.mImageWidth   = params.getPreviewSize().width;
								mDrawOnTop.mImageHeight  = params.getPreviewSize().height;
								mDrawOnTop.mBitmap 		 = Bitmap.createBitmap(mDrawOnTop.mImageWidth, mDrawOnTop.mImageHeight, Bitmap.Config.RGB_565);
								mDrawOnTop.mRGBData = new int[mDrawOnTop.mImageWidth * mDrawOnTop.mImageHeight]; 
								mDrawOnTop.mYUVData = new byte[data.length];        			  
							}

							// Pass YUV data to draw-on-top companion
							System.arraycopy(data, 0, mDrawOnTop.mYUVData, 0, data.length);
							
							//We are forcing DrawOnTop to update.
							mDrawOnTop.invalidate();
						}
					});
					
				} catch (IOException exception) {
					mCamera.release();
					mCamera = null;
				}
			}
			
			/**
			 * Method to handle camera orientation on dif. API levels and dif. devices.
			 * 
			 * @param camera
			 * @param angle
			 * @param orientation
			 */
			protected void setDisplayOrientation(Camera camera, int angle, String orientation)
			{
				Method downPolymorphic;
				if (Integer.parseInt(Build.VERSION.SDK) >= 8){
					try{
						downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
						if(downPolymorphic != null)
							downPolymorphic.invoke(camera, new Object[]{angle});
					} catch( Exception e){
						
					}
				} else {
					Camera.Parameters parameters = mCamera.getParameters();
					//Version 1.6:  Fix rotation "issue". Maybe?
					parameters.set("orientation", orientation);
					parameters.set("rotation", angle);
				}
					
				
			}
			
			/**
			 * This is called immediately after any structural 
			 * changes (format or size) have been made to the surface. 
			 * You should at this point update the imagery
			 * in the surface. 
			 * This method is always called at least once, 
			 * after surfaceCreated(SurfaceHolder).
			 */
			public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
				
				//@see: http://stackoverflow.com/questions/1016896/android-how-to-get-screen-dimensions
				//Point size = new Point();
				WindowManager wm = getWindowManager();
				Display display = wm.getDefaultDisplay(); 
				int width = display.getWidth();  // deprecated
				int height = display.getHeight();  // deprecated
				logger.debug("SURFACE CHANGED!: w is "+w+" and h is "+h);
				
				// Now that the size is known, set up the camera parameters and begin
				// the preview.
				Camera.Parameters parameters = mCamera.getParameters();
				
				//Version 1.6:  Fix rotation "issue". Maybe?
				parameters.set("orientation", "portrait");
				parameters.set("rotation",90);
				
				//parameters.setPreviewSize(w, h);
				//parameters.setPreviewSize(width, height);
				parameters.setPreviewSize(320, 240);
				
				parameters.setPreviewFrameRate(15);
				
				parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			}
			
			/**
			 * 
			 */
			public void surfaceDestroyed(SurfaceHolder holder) {
				// Surface will be destroyed when we return, so stop the preview.
				// Because the CameraDevice object is not a shared resource, it's very
				// important to release it when the activity is paused.
				mFinished = true;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}

		}

	//----------------------------------------------------------------------

		final class DrawOnTop extends View {
		
		//Any reason why this are prefixed with m?!
		Bitmap 		mBitmap;
		Paint 		mPaintBlack;
		Paint 		mPaintYellow;
		Paint 		mPaintRed;
		Paint 		mPaintGreen;
		Paint 		mPaintBlue;
		Paint 		mPaintBrightness;
		
		byte[]		mYUVData;
		int[] 		mRGBData;
		int 		mImageWidth, mImageHeight;
		int[] 		mRedHistogram;
		int[] 		mGreenHistogram;
		int[] 		mBlueHistogram;
		double[] 	mBinSquared;
		
		double[] 	brightnessHistory = new double [256];
		int 		brightnessCounter = 0;
		
		int 		requestUpdateCap = 64;
		
		/**
		 * 
		 */
		protected final Signal requestUpdateSignal;
		
		/**
		 * @private
		 */
		private double _averageBrightness;
		
		public DrawOnTop(Context context) 
		{
			super(context);
			
			requestUpdateSignal = new Signal();
			
			
			this._init();
			//this._createGateway();
		}
		
		public double getAverageBrightness()
		{
			return this._averageBrightness;
		}
		
		/**
		 * 
		 */
		final protected void _init()
		{
			
			mPaintBlack = new Paint();
			mPaintBlack.setStyle(Paint.Style.FILL);
			mPaintBlack.setColor(Color.BLACK);
			mPaintBlack.setTextSize(25);

			mPaintYellow = new Paint();
			mPaintYellow.setStyle(Paint.Style.FILL);
			mPaintYellow.setColor(Color.YELLOW);
			mPaintYellow.setTextSize(25);

			mPaintRed = new Paint();
			mPaintRed.setStyle(Paint.Style.FILL);
			mPaintRed.setColor(Color.RED);
			mPaintRed.setTextSize(25);

			mPaintGreen = new Paint();
			mPaintGreen.setStyle(Paint.Style.FILL);
			mPaintGreen.setColor(Color.GREEN);
			mPaintGreen.setTextSize(25);

			mPaintBlue = new Paint();
			mPaintBlue.setStyle(Paint.Style.FILL);
			mPaintBlue.setColor(Color.BLUE);
			mPaintBlue.setTextSize(25);

			mPaintBrightness = new Paint();
			mPaintBrightness.setStyle(Paint.Style.FILL);


			mBitmap = null;
			mYUVData = null;
			mRGBData = null;
			mRedHistogram = new int[256];
			mGreenHistogram = new int[256];
			mBlueHistogram = new int[256];
			mBinSquared = new double[256];
			for (int bin = 0; bin < 256; bin++)
			{
				mBinSquared[bin] = ((double)bin) * bin;
			} // bin
		}
		
		@SuppressLint({ "DrawAllocation" })
		@Override
		protected void onDraw(Canvas canvas) {
			if (mBitmap != null)
			{
				int canvasWidth 	= canvas.getWidth();
				int canvasHeight 	= canvas.getHeight();
				int newImageWidth 	= canvasWidth;
				//int newImageHeight 	= canvasHeight;
				int marginWidth 	= (canvasWidth - newImageWidth)/2;

				// Convert from YUV to RGB
				decodeYUV420SP(mRGBData, mYUVData, mImageWidth, mImageHeight);

				// Calculate histogram
				calculateIntensityHistogram(mRGBData, mRedHistogram,  mImageWidth, mImageHeight, 0);
				calculateIntensityHistogram(mRGBData, mGreenHistogram,mImageWidth, mImageHeight, 1);
				calculateIntensityHistogram(mRGBData, mBlueHistogram, mImageWidth, mImageHeight, 2);

				// Calculate mean
				double imageRedMean 	= 0, imageGreenMean    = 0, imageBlueMean    = 0;
				double redHistogramSum  = 0, greenHistogramSum = 0, blueHistogramSum = 0;

				for (int bin = 0; bin < 256; bin++)
				{
					imageRedMean      += mRedHistogram[bin] * bin;
					redHistogramSum   += mRedHistogram[bin];
					
					imageGreenMean 	  += mGreenHistogram[bin] * bin;
					greenHistogramSum += mGreenHistogram[bin];
					
					imageBlueMean 	  += mBlueHistogram[bin] * bin;
					blueHistogramSum  += mBlueHistogram[bin];
				} // bin
				
				imageRedMean   /= redHistogramSum;
				imageGreenMean /= greenHistogramSum;
				imageBlueMean  /= blueHistogramSum;
				
				//Calculate averageBrightness -- How to do this the best way?
				_averageBrightness = 0;

				//_averageBrightness = (imageRedMean + imageGreenMean + imageBlueMean) / 3;
				
				//Let's use RGB -> Luma, using Y = 0.375 R + 0.5 G + 0.125 B, we have a quick way out: Y = (R+R+B+G+G+G)/6
				_averageBrightness = ((imageRedMean*2)+imageBlueMean+(imageGreenMean*3))/6;
				
				brightnessHistory[brightnessCounter%256] = _averageBrightness; 
				brightnessCounter++;
				
				
				// Draw Location
				this._drawLocationText(canvas, longitude, latitude, marginWidth);

				// Draw Time and AverageBrightness Value
				this._drawBrightnessText(canvas, _averageBrightness, marginWidth);

				// Draw brightness histogram
				this._drawBrightnessHistogram(canvas, newImageWidth, canvasHeight, marginWidth);
				
				
				/*
				 * TODO: We might want to have this capped to a 
				 * time unit and also use an accrued value. AccruedTimer?
				 */
				if (brightnessCounter % requestUpdateCap == (requestUpdateCap - 1))
				{
					this._doRequestUpdate();
				}

			} // end if statement

			super.onDraw(canvas);

		} // end onDraw method
		
		/**
		 * 
		 */
		final private void _doRequestUpdate()
		{
			try
			{
				logger.debug("Request update");
				this.requestUpdateSignal.dispatch();
			}
			catch (SignalException e)
			{
				logger.debug("Dispatch requestUpdateSignal: " + e.toString());
			}
		}
		
		/**
		 * 
		 * @param canvas
		 * @param newImageWidth
		 * @param canvasHeight
		 * @param marginWidth
		 */
		final private void _drawBrightnessHistogram(Canvas canvas, int newImageWidth, int canvasHeight, int marginWidth) {
			float barWidth = ((float)newImageWidth) / 256;
			//float barMarginHeight = 2;
			RectF barRect = new RectF();
			barRect.bottom = canvasHeight;
			barRect.top = barRect.bottom - 100;
			barRect.left = marginWidth;
			barRect.right = barRect.left + barWidth;
			for (int bin = 0; bin < 256; bin++)
			{
				if (brightnessHistory[bin] != 0)
				{
					mPaintBrightness.setARGB(255, (int)brightnessHistory[bin], (int)brightnessHistory[bin], (int)brightnessHistory[bin]);
					canvas.drawRect(barRect, mPaintBrightness);
					barRect.left  += barWidth;
					barRect.right += barWidth;
				}
			} // bin
			
		}

		/**
		 * 
		 * @param canvas
		 * @param averageBrightness
		 * @param marginWidth
		 */
		final private void _drawBrightnessText(Canvas canvas, double averageBrightness, int marginWidth) {
			String TimeBrightness = "Time: "+ System.currentTimeMillis() + " Brightness: " + String.format("%.4g", averageBrightness);
			canvas.drawText(TimeBrightness, marginWidth + 10-1, 60-1, mPaintBlack);
			canvas.drawText(TimeBrightness, marginWidth + 10+1, 60-1, mPaintBlack);
			canvas.drawText(TimeBrightness, marginWidth + 10+1, 60+1, mPaintBlack);
			canvas.drawText(TimeBrightness, marginWidth + 10-1, 60+1, mPaintBlack);
			canvas.drawText(TimeBrightness, marginWidth + 10,   60,   mPaintYellow);
		}
		
		/**
		 * 
		 * @param canvas
		 * @param longitude
		 * @param latitude
		 * @param marginWidth
		 */
		final private void _drawLocationText(Canvas canvas, double longitude, double latitude, int marginWidth)
		{
			String LocationValues = "Latitude: " + String.format("%.6g", latitude) + " Longitude: " + String.format("%.6g", longitude);
			canvas.drawText(LocationValues, marginWidth + 10-1, 30-1, mPaintBlack);
			canvas.drawText(LocationValues, marginWidth + 10+1, 30-1, mPaintBlack);
			canvas.drawText(LocationValues, marginWidth + 10+1, 30+1, mPaintBlack);
			canvas.drawText(LocationValues, marginWidth + 10-1, 30+1, mPaintBlack);
			canvas.drawText(LocationValues, marginWidth + 10,   30,   mPaintYellow);
		}
		
		/**
		 * 
		 * @param rgb
		 * @param yuv420sp
		 * @param width
		 * @param height
		 */
		final public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
			final int frameSize = width * height;

			for (int j = 0, yp = 0; j < height; j++) {
				int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
				for (int i = 0; i < width; i++, yp++) {
					int y = (0xff & ((int) yuv420sp[yp])) - 16;
					if (y < 0) y = 0;
					if ((i & 1) == 0) {
						v = (0xff & yuv420sp[uvp++]) - 128;
						u = (0xff & yuv420sp[uvp++]) - 128;
					}

					int y1192 = 1192 * y;
					int r = (y1192 + 1634 * v);
					int g = (y1192 - 833 * v - 400 * u);
					int b = (y1192 + 2066 * u);

					if (r < 0) r = 0; else if (r > 262143) r = 262143;
					if (g < 0) g = 0; else if (g > 262143) g = 262143;
					if (b < 0) b = 0; else if (b > 262143) b = 262143;

					rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
				}
			}
		}
		
		/**
		 * 
		 * @param rgb
		 * @param yuv420sp
		 * @param width
		 * @param height
		 */
		final public void decodeYUV420SPGrayscale(int[] rgb, byte[] yuv420sp, int width, int height)
		{
			final int frameSize = width * height;

			for (int pix = 0; pix < frameSize; pix++)
			{
				int pixVal = (0xff & ((int) yuv420sp[pix])) - 16;
				if (pixVal < 0) pixVal = 0;
				if (pixVal > 255) pixVal = 255;
				rgb[pix] = 0xff000000 | (pixVal << 16) | (pixVal << 8) | pixVal;
			} // pix
		}
		
		/**
		 * 
		 * @param rgb
		 * @param histogram
		 * @param width
		 * @param height
		 * @param component
		 */
		final public void calculateIntensityHistogram(int[] rgb, int[] histogram, int width, int height, int component)
		{
			for (int bin = 0; bin < 256; bin++)
			{
				histogram[bin] = 0;
			} // bin
			if (component == 0) // red
			{
				for (int pix = 0; pix < width*height; pix += 3)
				{
					int pixVal = (rgb[pix] >> 16) & 0xff;
					histogram[ pixVal ]++;
				} // pix
			}
			else if (component == 1) // green
			{
				for (int pix = 0; pix < width*height; pix += 3)
				{
					int pixVal = (rgb[pix] >> 8) & 0xff;
					histogram[ pixVal ]++;
				} // pix
			}
			else // blue
			{
				for (int pix = 0; pix < width*height; pix += 3)
				{
					int pixVal = rgb[pix] & 0xff;
					histogram[ pixVal ]++;
				} // pix
			}
		}
	} 

}