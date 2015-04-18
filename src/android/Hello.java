package com.example.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.*;
import android.content.*;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.*;
import android.util.Log;

public class Hello extends CordovaPlugin {

    Camera cam = null;
    Context me = null;
    
        private PictureCallback mPicture = new PictureCallback() {
		    @Override
		    public void onPictureTaken(byte[] data, Camera camera) {
	    		Log.i("CasaDeBalloon","PhotoTimerTask onPictureTaken");
		        File pictureFile = getOutputMediaFile();
		        if (pictureFile == null){
		            Log.i("CasaDeBalloon", "Error creating media file, check storage permissions: ");
		            return;
		        }

		        try {
		            FileOutputStream fos = new FileOutputStream(pictureFile);
		            fos.write(data);
		            fos.close();	    		
		        } catch (FileNotFoundException e) {
		            Log.i("CasaDeBalloon", "File not found: " + e.getMessage());
		        } catch (IOException e) {
		            Log.i("CasaDeBalloon", "Error accessing file: " + e.getMessage());
		        }
		        cam.stopPreview();
				me.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(pictureFile)));
				Log.i("CasaDeBalloon", "Picture Taken");

		    }
		};
		
		private File getOutputMediaFile(){
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
                );
                return image;
            }
            catch(Throwable ex) {
                return null;
            }
		}

    
    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        me = this.cordova.getActivity().getApplicationContext();
    
        if (action.equals("greet")) {
            cam = Camera.open();
            
            Camera.Parameters params = cam.getParameters();
    		params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
    		params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            Camera.Size maxSize = params.getPictureSize();
    		for(Camera.Size size : params.getSupportedPictureSizes())
    		{
    			if ((size.width * size.height) > (maxSize.width * maxSize.height))
    			{
    				maxSize = size;
    			}
    		}
    		params.setPictureSize(maxSize.width, maxSize.height);
            params.setJpegQuality(100);
            cam.setParameters(params); 
            cam.takePicture(null, null, mPicture);    	
            cam.startPreview();
            try {
    			Thread.sleep(2000);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
            
            cam.release();
        
            
            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;

        } else {
            
            return false;

        }
    }
}
