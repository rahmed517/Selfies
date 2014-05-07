package com.example.selfies;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.example.selfies.ProcessMoleImg;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Build;
import android.provider.MediaStore;

public class CaptureMoleImg extends Activity {
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final String IMAGE_DIRECTORY_NAME = "Selfies";
	public Uri fileUriMole;
	public Uri fileUriSafe;
	private Button btnCapturePicture;
	private static final String TAG = "CaptureMoleImg";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				Bundle b = getIntent().getExtras();
				if(b!=null){
					fileUriSafe = (Uri)b.get("safe_pic");
				}
		
		
		setContentView(R.layout.activity_capture_mole_img);
		btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
		
		btnCapturePicture.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// capture picture
				captureImage();
			}
		});
		
	}
	private void captureImage() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		fileUriMole = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
		
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUriMole);
		
		// start the image capture Intent
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if the result is capturing Image
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// successfully captured the image
				// display it in image view
				beginProcessMoleImg();
			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled Image capture
				Toast.makeText(getApplicationContext(),
							   "User cancelled image capture", Toast.LENGTH_SHORT)
				.show();
			} else {
				// failed to capture image
				Toast.makeText(getApplicationContext(),
							   "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
				.show();
			}
		}
	}
	
	public void beginProcessMoleImg(){
		Intent intent = new Intent(this,ProcessMoleImg.class);
		intent.putExtra("safe_pic", fileUriSafe);
		intent.putExtra("mole_pic",fileUriMole);
		startActivity(intent);
	}
	
	private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.e("TEST", "Success");
					btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
					
					btnCapturePicture.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// capture picture
							captureImage();
						}
					});  
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
		}
	};
	
	
	
	/**
	 * Creating file uri to store image/video
	 */
	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}
	
	/*
	 * returning image / video
	 */
	private static File getOutputMediaFile(int type) {
		
		// External sdcard location
		File mediaStorageDir = new File(
										Environment
										.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
										IMAGE_DIRECTORY_NAME);
		
		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
					  + IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}
		
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
												Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
								 + "mole_" + timeStamp + ".jpg");
		} else {
			return null;
		}
		
		return mediaFile;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.capture_safe_img, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}

