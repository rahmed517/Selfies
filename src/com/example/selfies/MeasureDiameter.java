package com.example.selfies;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Build;

public class MeasureDiameter extends ActionBarActivity{
	private static final String TAG = "CircleDraw";
	private ImageView imgPreview;
	private LineDrawingView lineView;
	private CirclesDrawingView circleView;
	private TextView instr_measure_penny;
	private TextView instr_measure_mole;
	private Button next;
	private Button finish;
	public Uri fileUriPenny;
	private double scale;
	private double length;
	private double [] scores = new double [4];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b = getIntent().getExtras();
		if(b!=null){
			fileUriPenny =(Uri) b.get("penny_pic");
			Log.v(TAG, "penny: "+ fileUriPenny);
		}

		setContentView(R.layout.activity_measure_diameter);
		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		lineView = (LineDrawingView) findViewById(R.id.lineView);
		circleView = (CirclesDrawingView) findViewById(R.id.circleView);
		instr_measure_penny = (TextView) findViewById(R.id.instructions_measure_penny);
		instr_measure_mole = (TextView) findViewById(R.id.instructions_measure_mole);
		next = (Button) findViewById(R.id.button_next);
		finish = (Button) findViewById(R.id.button_finish);


		imgPreview.setVisibility(View.VISIBLE);

		// bitmap factory
		BitmapFactory.Options options = new BitmapFactory.Options();

		// downsizing image as it throws OutOfMemory Exception for larger
		// images
		options.inSampleSize = 8;

		Bitmap bitmap = BitmapFactory.decodeFile(fileUriPenny.getPath(),
				options);
		imgPreview.setImageBitmap(bitmap);
		imgPreview.setFocusable(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.circle_draw, menu);
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


	public void beginMeasureMole(View view){
		scale = CirclesDrawingView.scale_mm2pix;
		circleView.setVisibility(View.GONE);
		instr_measure_penny.setVisibility(View.GONE);
		next.setVisibility(View.GONE);
		lineView.setVisibility(View.VISIBLE);
		instr_measure_mole.setVisibility(View.VISIBLE);	
		finish.setVisibility(View.VISIBLE);
		length = LineDrawingView.length;
		Log.v(TAG, "scale: " + scale);
		Log.v(TAG, "length: " + length);

	}

	public void calcDiam(View view){
		length = LineDrawingView.length;
		double diam = length*scale;
		scores[3] = diam;
		Log.v(TAG, "scale: " + scale);
		Log.v(TAG, "length: " + length);
		Log.v(TAG, "diam: " + diam);

	}




}
