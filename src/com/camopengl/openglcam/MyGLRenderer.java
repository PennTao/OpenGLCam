package com.camopengl.openglcam;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;



public class MyGLRenderer implements Renderer {
	private final Context mActivityContext;
	private String mFilepath;

	private final FloatBuffer mTexcoordPositions;
	private final FloatBuffer mVerticesPositions;
	
	private int mImageUniformHandle;
	private int mStepUniformHandle;
	
	private int mProgramHandle;
	private int mPositionHandle;
	private int mTexcoordHandle;

	
	private int mEmbossProgram;
	private int mBlurProgram;
	private int mEdgeProgram;
	private int mGrayScaleProgram;
	private int mNegProgram;
	private int mToonshadingProgram;
	private int mNormalProgram;
	private int height = 1600;
	private int width = 900;

	private int mProgramID = 1;
	
	private int mTextureDataHandle;
	private final int mBytesPerFloat = 4;

	private final int mPositionDataSize = 2;
	private final int mTexcoordDataSize = 2;

	private boolean mScreenshot;
	private final float[] vertices =	{ 
			1.0f, 1.0f, 				
			-1.0f, 1.0f, 
			1.0f, -1.0f, 
			-1.0f, 1.0f,				
			-1.0f, -1.0f,
			1.0f, -1.0f
	    };
	private final float[] texcoords =   { 
			0.0f, 0.0f,
	        0.0f, 1.0f,
	        1.0f, 0.0f,
	        0.0f, 1.0f,
	        1.0f, 1.0f,
	        1.0f, 0.0f,
	    };
	@Override
	public void onDrawFrame(GL10 arg0) {
		// TODO Auto-generated method stub

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		

		mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "Position");
		mTexcoordHandle = GLES20.glGetAttribLocation(mProgramHandle, "Texcoords");
		mStepUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_step");
		mImageUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_image");
		
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        if(mImageUniformHandle != -1)
        	GLES20.glUniform1i(mImageUniformHandle, 0);    
        if(mStepUniformHandle != -1)
        	GLES20.glUniform2f(mStepUniformHandle, 1.0f / (float)width, 1.0f / (float)height);
        Draw();
        if(mScreenshot == true)
        	Log.d("mScreenshot", "true");
        if(mScreenshot){  
        	Log.d("screenshot","start");
            int screenshotSize = width * height;
            ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
            bb.order(ByteOrder.nativeOrder());
            GLES20.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
            int pixelsBuffer[] = new int[screenshotSize];
            bb.asIntBuffer().get(pixelsBuffer);
            bb = null;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);
            pixelsBuffer = null;

            short sBuffer[] = new short[screenshotSize];
            ShortBuffer sb = ShortBuffer.wrap(sBuffer);
            bitmap.copyPixelsToBuffer(sb);

            //Making created bitmap (from OpenGL points) compatible with Android bitmap
            for (int i = 0; i < screenshotSize; ++i) {                  
                short v = sBuffer[i];
                sBuffer[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
            }
            sb.rewind();
            bitmap.copyPixelsFromBuffer(sb);
            try {
            	StringBuffer sbFilepath = new StringBuffer(mFilepath);
            	String sProgType = getProgramType();
            	int insertIdx = sbFilepath.length() - 4;
            	System.out.println(sbFilepath.insert(insertIdx, sProgType).toString());
                FileOutputStream fos = new FileOutputStream(sbFilepath.insert(insertIdx, sProgType).toString());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                // handle
            }

            mScreenshot = false;
        }
	}

	@Override
	public void onSurfaceChanged(GL10 arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		// TODO Auto-generated method stub
		Log.d("onSurfaceCreated","start");
		GLES20.glClearColor(0.3f, 0.0f, 0.0f, 0.0f);
		
		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		String[] attr = new String[] {"Position",  "Texcoords"};
		String vertexShader = getVertexShader();   		
		String fragmentShader = getFragmentShader(R.raw.passthroughfs);	
		final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);		
		int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);		
		mNormalProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, attr);								                                							       
	
		fragmentShader = getFragmentShader(R.raw.embossfs);
		fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);	
		mEmbossProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,attr);
		
		fragmentShader = getFragmentShader(R.raw.boxblurfs);
		fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);	
		mBlurProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,attr);
		
		fragmentShader = getFragmentShader(R.raw.edgefs);
		fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);	
		mEdgeProgram =  ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,attr);
		
		fragmentShader = getFragmentShader(R.raw.grayscalefs);
		fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);	
		mGrayScaleProgram =  ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,attr);
		
		fragmentShader = getFragmentShader(R.raw.negfs);
		fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);	
		mNegProgram =  ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,attr);
		
		fragmentShader = getFragmentShader(R.raw.toonshadingfs);
		fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);	
		mToonshadingProgram =  ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,attr);
		
	//	mProgramHandle = mToonshadingProgram;
	/*	switch (mProgramID){
		
		
		case 1:
			mProgramHandle = mNormalProgram;
		case 2:
			mProgramHandle = mEmbossProgram;
		case 3:
			mProgramHandle =  mBlurProgram;
		case 4:
			mProgramHandle = mEdgeProgram;
		case 5:
			mProgramHandle = mGrayScaleProgram;
		case 6:
			mProgramHandle = mNegProgram;
		default:
			mProgramHandle = mNormalProgram;

		}*/
		
        // Load the texture
		setEffect(mProgramID);
		GLES20.glUseProgram(mProgramHandle);
        mTextureDataHandle = TextureHelper.loadTexture(mActivityContext,mFilepath );
		Log.d("onSurfaceCreated","done");

	}
	public MyGLRenderer(final Context activityContext, String file){
		mActivityContext = activityContext;
		mFilepath = file;
	
		System.out.println("constructor");
		Log.d("MyGLRenderer constructor", "start construct");
		
		mVerticesPositions = ByteBuffer.allocateDirect(vertices.length * mBytesPerFloat)
		        .order(ByteOrder.nativeOrder()).asFloatBuffer();	
		mVerticesPositions.put(vertices).position(0);
		mTexcoordPositions = ByteBuffer.allocateDirect(texcoords.length * mBytesPerFloat)
		        .order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTexcoordPositions.put(texcoords).position(0);
		Log.d("MyGLRenderer constructor", "done construct");
		mScreenshot = false;

	}
	
	protected String getVertexShader(){
		return RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.passthroughvs);
	}
	protected String getFragmentShader(final int resourceId){
		return RawResourceReader.readTextFileFromRawResource(mActivityContext, resourceId);
	}
	
	private void Draw(){
		setEffect(mProgramID);
		GLES20.glUseProgram(mProgramHandle);
		mVerticesPositions.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		0, mVerticesPositions);    
		GLES20.glEnableVertexAttribArray(mPositionHandle); 
		
		mTexcoordPositions.position(0);
		GLES20.glVertexAttribPointer(mTexcoordHandle, mTexcoordDataSize, GLES20.GL_FLOAT, false,
        		0, mTexcoordPositions);    
		GLES20.glEnableVertexAttribArray(mTexcoordHandle);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 );  
	}

	
	public void setEffect(int programID){
		switch (programID){
		
		
		case 1:
			mProgramHandle = mNormalProgram;
			break;
		case 2:
			mProgramHandle = mEmbossProgram;
			break;
		case 3:
			mProgramHandle =  mBlurProgram;
			break;
		case 4:
			mProgramHandle = mEdgeProgram;
			break;
		case 5:
			mProgramHandle = mGrayScaleProgram;
			break;
		case 6:
			mProgramHandle = mNegProgram;
			break;
		case 7:
			mProgramHandle = mToonshadingProgram;
			break;
		default:
			mProgramHandle = mToonshadingProgram;
			break;
		}
		GLES20.glUseProgram(mProgramHandle);
		
	}
	
	public void setPorgID(int id){
		mProgramID = id;
	
	}
	
	public String getProgramType(){
		switch(mProgramID){
		case 1:
			return "normal";
		case 2:
			return "emobss";
		case 3:
			return "blur";
		case 4:
			return "edge";
		case 5:
			return "grayscale";
		case 6:
			return "negative";
		case 7:
			return "toonshading";
		default:
			return "";
		}	
	}
	public void saveImage(){
		
		mScreenshot = true;
	}
	
	
	

}
