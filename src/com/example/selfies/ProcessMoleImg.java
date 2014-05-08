package com.example.selfies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.os.Build;


public class ProcessMoleImg extends Activity implements OnTouchListener{
	private static final String TAG = "ProcessMoleImg";
	private ImageView imgPreview;
	public Uri fileUriSafe;
	public Uri fileUriMole;

	private Mat  mIntermediateMat = new Mat();
	Mat mHierarchy = new Mat();
	private static double mMinContourArea = .1;
	private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
	private Scalar CONTOUR_COLOR = new Scalar(255,0,0, 255);
	public List<Point>border = new ArrayList<Point>();

	private Bitmap bitmap_ref;
	private Bitmap bitmap_sample;
	public double [] scores = new double [4];


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the images from the intent
		Bundle b = getIntent().getExtras();
		if(b!=null){
			fileUriSafe =(Uri) b.get("safe_pic");
			fileUriMole = (Uri)b.get("mole_pic");
			Log.v(TAG, "safe: "+ fileUriSafe);
			Log.v(TAG, "mole: " + fileUriMole);
		}

		setContentView(R.layout.activity_process_mole_img);
		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		imgPreview.setFocusable(true);
		imgPreview.setOnTouchListener(ProcessMoleImg.this);

		scores[0] = processImg();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.process_mole_img, menu);
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

	public void setMinContourArea(double area){
		mMinContourArea = area;	
	}

	//the sample image is processed to calc border irregularity
	private double processImg() {

		try {
			imgPreview.setVisibility(View.VISIBLE);

			// bitmap factory
			BitmapFactory.Options options = new BitmapFactory.Options();

			// downsizing image as it throws OutOfMemory Exception for larger
			// images
			options.inSampleSize = 8;

			bitmap_ref = BitmapFactory.decodeFile(fileUriSafe.getPath(),
					options);

			bitmap_sample = BitmapFactory.decodeFile(fileUriMole.getPath(),
					options);

			//convert from bitmap to Mat
			Mat ref = new Mat(bitmap_ref.getHeight(), bitmap_ref.getWidth(), CvType.CV_8UC4);
			Mat sample = new Mat(bitmap_sample.getHeight(), bitmap_sample.getWidth(), CvType.CV_8UC4);
			Mat sample2calcgrad = new Mat(bitmap_sample.getHeight(), bitmap_sample.getWidth(), CvType.CV_8UC4);

			Utils.bitmapToMat(bitmap_ref, ref);
			Utils.bitmapToMat(bitmap_sample, sample);
			Utils.bitmapToMat(bitmap_sample,sample2calcgrad);

			//normalize image based on reference			
			//sample = normalizeImg(sample, ref);

			//Using Sobel filter to calculate gradient
			Mat grad_x = new Mat();
			Mat grad_y = new Mat();
			Mat abs_grad_x = new Mat();
			Mat abs_grad_y = new Mat();
			Mat gradVals = new Mat();
			Mat sample_gray = new Mat(bitmap_sample.getHeight(),bitmap_sample.getWidth(),CvType.CV_8UC1);
			Imgproc.cvtColor(sample2calcgrad, sample_gray, Imgproc.COLOR_BGRA2GRAY);
			Imgproc.GaussianBlur(sample_gray, sample_gray, new Size(5,5), 0);

			//Gradient X
			Imgproc.Sobel(sample_gray, grad_x, CvType.CV_8UC1, 1, 0);
			Core.convertScaleAbs(grad_x, abs_grad_x,10,0);

			//Gradient Y
			Imgproc.Sobel(sample_gray, grad_y, CvType.CV_8UC1, 0, 1);
			Core.convertScaleAbs(grad_y,abs_grad_y, 10, 0);

			//combine with grad = sqrt(gx^2 + gy^2)
			Core.addWeighted(abs_grad_x, .5, abs_grad_y, .5, 0, gradVals);


			//Using CANNY to further smooth Gaussian blurred image; extract contours
			Imgproc.Canny(sample_gray, mIntermediateMat, 80, 90);

			//find contours of filtered image
			List <MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(mIntermediateMat, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

			// Find max contour area
			double maxArea = 0;
			Iterator<MatOfPoint> each = contours.iterator();
			while (each.hasNext()) {
				MatOfPoint wrapper = each.next();
				double area = Imgproc.contourArea(wrapper);
				if (area > maxArea)
					maxArea = area;
			}

			// Filter contours by area and only keep those above thresh value
			mContours.clear();
			each = contours.iterator();
			while (each.hasNext()) {
				MatOfPoint contour = each.next();
				if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
					mContours.add(contour);
					border.addAll(contour.toList());
				}
			}

			//segment border into 8 parts 
			//calc gradient along contour segment & normalize based on number of points in border
			//upto 7 points at end of border list ignored
			double [] seg_val = new double [8];
			int seg_len = border.size()/8;
			for(int i = 0; i<8; i++){
				double contourGradientSum = 0;
				for(int j=i*seg_len; j<(i+1)*seg_len;j++){
					Point pt = border.get(j);
					int x = (int) pt.x;
					int y = (int) pt.y;			
					contourGradientSum += Core.mean(gradVals.submat(y-1, y+1, x-1, x+1)).val[0];
				}
				seg_val[i]=contourGradientSum/seg_len;
			}


			Log.v(TAG, "grad vals: [" + seg_val[0] + "," + seg_val[1] + "," 
					+ seg_val[2] + ","+ seg_val[3] + ","
					+ seg_val[4] + ","+ seg_val[5] + ","
					+ seg_val[6] + ","+ seg_val[7] + "]");
			
			double thresh = 140;
			double score = 0;

			for(double val:seg_val){
				if (val<=thresh){
					score++;
				}
			}
			
			if(score<8){
				score += mContours.size()/7;
				score = Math.min(8,score);
			}



			Log.v(TAG, "score: " +score);
			Log.v(TAG, "Contours count: " + mContours.size());
			Log.v(TAG, "Border size: " + border.size());
			Imgproc.drawContours(sample, mContours, -1, CONTOUR_COLOR);

			//display image with contours
			Utils.matToBitmap(sample, bitmap_sample);
			imgPreview.setImageBitmap(bitmap_sample);
			imgPreview.setFocusable(true);

			return score;

			//			
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return -1;
	}

	public Mat normalizeImg(Mat sample, Mat ref){
		Log.v(TAG, "bitmap to matrix");

		//calculate mean and stddev for each color channel for both sample and ref
		//stored in matrix mean and stddev

		MatOfDouble mean_ref = new MatOfDouble(); 
		MatOfDouble std_ref = new MatOfDouble();
		Core.meanStdDev(ref, mean_ref, std_ref);
		Log.v(TAG, "mean_ref" + mean_ref.toString());
		Log.v(TAG, "std_ref" + std_ref.toString());


		MatOfDouble mean_sample = new MatOfDouble();
		MatOfDouble std_sample = new MatOfDouble();
		Core.meanStdDev(sample, mean_sample, std_sample);
		Log.v(TAG, "mean_sample" + mean_sample.toString());
		Log.v(TAG, "std_sample" + std_sample.toString());

		Scalar mean_ref_scalar = new Scalar(mean_ref.toArray());
		Scalar std_ref_scalar = new Scalar(std_ref.toArray());
		Scalar mean_samp_scalar = new Scalar (mean_sample.toArray());
		Scalar std_samp_scalar = new Scalar(std_sample.toArray());

		List<Mat> lbgr = new ArrayList <Mat>(4);
		Core.split(sample,lbgr);
		Mat mB_sample = new Mat (sample.height(),sample.width(), CvType.CV_8UC1);
		Mat mG_sample = new Mat (sample.height(),sample.width(), CvType.CV_8UC1);
		Mat mR_sample = new Mat (sample.height(),sample.width(), CvType.CV_8UC1);
		Mat temp = new Mat (sample.height(),sample.width(), CvType.CV_8UC1);

		Log.v(TAG, "begin transform");
		Core.subtract(lbgr.get(0), new Scalar(mean_samp_scalar.val[0]), mB_sample);
		Core.multiply(mB_sample, new Scalar(std_ref_scalar.val[0]/std_samp_scalar.val[0]), temp);
		Core.add(temp, new Scalar(mean_ref_scalar.val[0]), mB_sample);

		Core.subtract(lbgr.get(1), new Scalar(mean_samp_scalar.val[1]), mG_sample);
		Core.multiply(mG_sample, new Scalar(std_ref_scalar.val[1]/std_samp_scalar.val[1]), temp);
		Core.add(temp, new Scalar(mean_ref_scalar.val[1]), mG_sample);

		Core.subtract(lbgr.get(2), new Scalar(mean_samp_scalar.val[2]), mR_sample);
		Core.multiply(mR_sample, new Scalar(std_ref_scalar.val[2]/std_samp_scalar.val[2]), temp);
		Core.add(temp, new Scalar(mean_ref_scalar.val[2]), mR_sample);

		Mat mB_sample_temp = new Mat(sample.height(),sample.width(), CvType.CV_8UC1);
		Mat mG_sample_temp = new Mat(sample.height(),sample.width(), CvType.CV_8UC1);
		Mat mR_sample_temp = new Mat(sample.height(),sample.width(), CvType.CV_8UC1);
		Core.inRange(mB_sample,new Scalar(0), new Scalar(255), mB_sample_temp);
		Core.inRange(mG_sample,new Scalar(0), new Scalar(255), mG_sample_temp);
		Core.inRange(mR_sample,new Scalar(0), new Scalar(255), mR_sample_temp);

		Log.v(TAG, "begin merge");
		List<Mat> lbgr_trans = new ArrayList<Mat>(4);
		lbgr_trans.add(mB_sample_temp);
		lbgr_trans.add(mG_sample_temp);
		lbgr_trans.add(mR_sample_temp);
		lbgr_trans.add(lbgr.get(3));

		Mat sample_trans = new Mat();
		Core.merge(lbgr_trans, sample_trans);
		Bitmap samp_bitmap = Bitmap.createBitmap(sample.cols(), sample.rows(),Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(sample_trans, samp_bitmap);
		imgPreview.setImageBitmap(samp_bitmap);


		return sample_trans;
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {


		return false; // don't need subsequent touch events
	}

	public void takePennyImg(View view){
		Intent intent = new Intent(view.getContext(), CapturePennyImg.class);
		intent.putExtra("scores", scores);
		startActivity(intent);
		//add intent extra for result
	}

}




