package com.example.selfies;

import java.text.DecimalFormat;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Build;

public class DisplayResults extends ActionBarActivity {
	private static final String TAG = "DisplayResults";
	private ImageView imgView;
	private TextView scoreText;
	private TextView diag_Text;
	public double [] scores;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_results);
		Bundle b = getIntent().getExtras();
		if(b!=null){
			scores = (double []) b.get("scores");
		}
		
		DecimalFormat df = new DecimalFormat("#.0");
		
		//display asymmetry results
		if(scores[0]<=1){
			imgView = (ImageView)findViewById(R.id.A_check);
		}else{
			imgView = (ImageView)findViewById(R.id.A_cross);
		}

		imgView.setVisibility(View.VISIBLE);
		scoreText = (TextView)findViewById(R.id.A_score);
		scoreText.setText(df.format(scores[0]));

		//display border results
		if(scores[1]<=4){
			imgView = (ImageView)findViewById(R.id.B_check);
		}else{
			imgView = (ImageView)findViewById(R.id.B_cross);
		}

		imgView.setVisibility(View.VISIBLE);
		scoreText = (TextView)findViewById(R.id.B_score);
		scoreText.setText(df.format(scores[1]));

		//display color results
		if(scores[2]<=3){
			imgView = (ImageView)findViewById(R.id.C_check);
		}else{
			imgView = (ImageView)findViewById(R.id.C_cross);
		}

		imgView.setVisibility(View.VISIBLE);
		scoreText = (TextView)findViewById(R.id.C_score);
		scoreText.setText(df.format(scores[2]));

		//display diameter results
		if(scores[3]<=6){
			imgView = (ImageView)findViewById(R.id.D_check);
		}else{
			imgView = (ImageView)findViewById(R.id.D_cross);
		}

		imgView.setVisibility(View.VISIBLE);
		scoreText = (TextView)findViewById(R.id.D_score);
		double diam_score = Math.min(scores[3]*2.5/6, 5);
		scoreText.setText(df.format(diam_score));

		//display overall results. tot_score weight factors determined by Stolz Method
		diag_Text = (TextView)findViewById(R.id.instr);
		double tot_score = 1.3*scores[0] + 0.1*scores[1] + 0.5*scores[2] + 0.5*diam_score;
		if(tot_score<4.76){
			imgView = (ImageView)findViewById(R.id.Tot_check);
			diag_Text.setText("The mole is benign");
		}else if(tot_score>=4.76 && tot_score < 5.45){
			imgView = (ImageView)findViewById(R.id.Tot_cross);
			diag_Text.setText("The mole is suspicious. Please consult a professional");
		}else{
			imgView = (ImageView)findViewById(R.id.Tot_cross);
			diag_Text.setText("The mole may be be malignant. Please consult a professional");
		}

		imgView.setVisibility(View.VISIBLE);
		scoreText = (TextView)findViewById(R.id.Tot_score);
		scoreText.setText(df.format(tot_score));
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_results, menu);
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
