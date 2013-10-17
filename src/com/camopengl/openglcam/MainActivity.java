/*This app is a free software developed by Tao Lei
 * The source code contains several pieces from android tutorials and android OpenGL tutorials
 * This app can use the camera of your android device to capture a picture and apply several filters to the picture taken*/

package com.camopengl.openglcam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;



import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;



public class MainActivity extends Activity {

	public Intent intent;

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	protected static final String TAG = null;

	protected static final String EXTRA_IMG = "IMG";
	private Uri fileUri;

	private Camera mCamera;
    private CameraPreview mPreview;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("Main", "onCreate");
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    if(checkCameraHardware(this)){
	    	mCamera = getCameraInstance();
	    }

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        Log.d("campreview", "done");
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
 
        preview.addView(mPreview);
	    
	}
	protected void onStart(){
		Log.d("Main", "onStart");
		super.onStart();
		
		if(mCamera == null){
			mCamera = getCameraInstance();

	        // Create our Preview view and set it as the content of our activity.
	        mPreview = new CameraPreview(this, mCamera);
	        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	 
	        preview.addView(mPreview);
		}
			

	}
	protected void onResume(){
		super.onResume();
		if(mCamera == null){
			mCamera = getCameraInstance();

	        // Create our Preview view and set it as the content of our activity.

	        mPreview = new CameraPreview(this, mCamera);

	        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	 
	        preview.addView(mPreview);
		}
		Log.d(TAG,"onResume");
	}
	protected void onPause() {
		Log.d(TAG,"onPause");
        super.onPause();
               // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }
	protected void onStop(){
		super.onStop();
		Log.d(TAG,"onStop");
		releaseCamera(); 
	}
	protected void onDestry(){
		super.onDestroy();
		releaseCamera(); 
		Log.d(TAG,"onDestroy");
	}



    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
	
	

	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
	public static Camera getCameraInstance(){
		
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {


	        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	        if (pictureFile == null){
	            Log.d(TAG, "Error creating media file, check storage permissions");
	            return;
	        }

	        try {

	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            fos.write(data);	 
	            fos.close();

	        } catch (FileNotFoundException e) {
	            Log.d(TAG, "File not found: " + e.getMessage());
	        } catch (IOException e) {
	            Log.d(TAG, "Error accessing file: " + e.getMessage());
	        }
	        System.out.println("flag");
	        Log.d(TAG,"aaaaa");
        	Intent intent = new Intent(getBaseContext(), CamActivity.class);

        	Bundle bundle = new Bundle();
        	bundle.putString("photoPath", pictureFile.toString());
        	
        	intent.putExtra("bd", bundle);
 
        	startActivity(intent);
	    }
	    
	};
	
	

	public void capturePicture(View view){
		intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

	    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

	    // start the image capture Intent
	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	    
	}
	
	public void takePicture(View view){
		Log.d(TAG,"Take pic");
		mCamera.takePicture(null, null, mPicture);

	}
	public void captureVideo(View view){
		intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

	    fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);  // create a file to save the video
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name

	    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high

	    // start the Video Capture Intent
	    startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);	
	}
	
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}
	@SuppressLint("SimpleDateFormat")
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
	    Log.d("lb",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString());
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}

}
