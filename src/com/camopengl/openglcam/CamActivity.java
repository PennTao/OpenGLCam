package com.camopengl.openglcam;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;

import android.opengl.GLSurfaceView;


public class CamActivity extends Activity 
{
	/** Hold a reference to our GLSurfaceView */
	private GLSurfaceView mGLSurfaceView;

	private MyGLRenderer mRender;
	private String fileName;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		Log.d("setContentView","start");
		setContentView(R.layout.activity_cam);
		Log.d("set View","done");
		mGLSurfaceView = (GLSurfaceView) findViewById(R.id.GLDraw);
		
		Log.d("GLSurface","view created");
		// Check if the system supports OpenGL ES 2.0.
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
	
		Intent intent = getIntent();

        fileName = (String) intent.getExtras().getBundle("bd").get("photoPath");
		mRender = new MyGLRenderer(this,fileName);
		if (supportsEs2) 
		{
			// Request an OpenGL ES 2.0 compatible context.
		//	Log.d("setEGLContextClientVersion","start");
			mGLSurfaceView.setEGLContextClientVersion(2);
		//	Log.d("setEGLContextClientVersion","done");
		//	Log.d("set Rder","start");
			// Set the renderer to our demo renderer, defined below.
		//	Log.d("file",fileName);
			mGLSurfaceView.setRenderer(mRender);
			mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		//	Log.d("set Rder","done");
		} 
		else 
		{
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}


	}

	@Override
	protected void onResume() 
	{
		// The activity must call the GL surface view's onResume() on activity onResume().
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() 
	{
		// The activity must call the GL surface view's onPause() on activity onPause().
		super.onPause();
		mGLSurfaceView.onPause();
	}
	
	
	public void setNormal(View view){
		mRender.setPorgID(1);
		mGLSurfaceView.requestRender();
	}
	
	public void setEmboss(View view){
		mRender.setPorgID(2);
		mGLSurfaceView.requestRender();
		mGLSurfaceView.requestRender();// request once will not take effect, so request a second time..
		
	}
	public void setBlur(View view){
		mRender.setPorgID(3);
		mGLSurfaceView.requestRender();
		mGLSurfaceView.requestRender();// request once will not take effect, so request a second time..
	}
	public void setEdge(View view){
		mRender.setPorgID(4);
		mGLSurfaceView.requestRender();
		mGLSurfaceView.requestRender();// request once will not take effect, so request a second time..
	}
	public void setGS(View view){
		mRender.setPorgID(5);
		mGLSurfaceView.requestRender();
	}
	public void setNegative(View view){
		mRender.setPorgID(6);
		mGLSurfaceView.requestRender();
	}
	public void setToonshading(View view){
		mRender.setPorgID(7);
		mGLSurfaceView.requestRender();
		mGLSurfaceView.requestRender();// request once will not take effect, so request a second time..
	}
	
	
	 public boolean onOptionsItemSelected(MenuItem item) {
		 switch (item.getItemId()) {
	     case android.R.id.home:
	    	 NavUtils.navigateUpFromSameTask(this);
	         return true;
		 
		 
		 case R.id.action_save:
			 mRender.saveImage();
			 mGLSurfaceView.requestRender();
			 return true;
		 }	 
		 return super.onOptionsItemSelected(item);
	}
	 
	 
	 public boolean onCreateOptionsMenu(Menu menu) {
		    // Inflate the menu items for use in the action bar
		    MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.save, menu);
		    return super.onCreateOptionsMenu(menu);
	}
	 public void onBackPressed(){
		 NavUtils.navigateUpFromSameTask(this);  
	}
}
