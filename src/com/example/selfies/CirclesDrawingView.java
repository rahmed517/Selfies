package com.example.selfies;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CirclesDrawingView extends View {

	private static final String TAG = "CirclesDrawingView";
	private Rect mMeasuredRect;
	Point point1, point3;
	Point point2, point4;
	public static double scale_mm2pix; 
	private static final double penny_rad = 19.05/2; //mm 
	
	// point1 and point 3 (corresponding to BalID 0 & 2 respectively) are grouped with GroupID 1
	// point 2 and point 4 (corresponding to BalID 1 & 3 respectively) are grouped with GroupID 2
	int groupId = -1;
	private ArrayList<ColorBall> colorballs = new ArrayList<ColorBall>();
	// array that holds the balls
	private int balID = -1;
	// variable to know what ball is being dragged


	/** Stores data about single circle */
	private static class CircleArea {
		int radius;
		int centerX;
		int centerY;

		CircleArea(int centerX, int centerY, int radius) {
			this.radius = radius;
			this.centerX = centerX;
			this.centerY = centerY;
		}

		@Override
		public String toString() {
			return "Circle[" + centerX + ", " + centerY + ", " + radius + "]";
		}
	}

	/** Paint to draw circles */
	private Paint mCirclePaint;
	private Paint mRectPaint;
	private Paint mBallPaint;
	private CircleArea circle;


	/**
	 * Default constructor
	 *
	 * @param ct {@link android.content.Context}
	 */
	public CirclesDrawingView(final Context ct) {
		super(ct);
		init(ct);
		setFocusable(true); // necessary for getting the touch events

	}

	public CirclesDrawingView(final Context ct, final AttributeSet attrs) {
		super(ct, attrs);
		init(ct);
		setFocusable(true); // necessary for getting the touch events
	}

	public CirclesDrawingView(final Context ct, final AttributeSet attrs, final int defStyle) {
		super(ct, attrs, defStyle);
		init(ct);
		setFocusable(true);
	}

	private void init(final Context ct) {

		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setColor(Color.BLUE);
		mCirclePaint.setStrokeWidth(5);
		mCirclePaint.setStyle(Paint.Style.STROKE);

		mRectPaint = new Paint();
		mRectPaint.setAntiAlias(true);
		mRectPaint.setColor(Color.parseColor("#55000000"));
		mRectPaint.setStyle(Paint.Style.FILL);
		mRectPaint.setStrokeJoin(Paint.Join.ROUND);
		mRectPaint.setStrokeWidth(5);

		mBallPaint = new Paint();
		mBallPaint.setAntiAlias(true);
		mBallPaint.setColor(Color.GRAY);
		mBallPaint.setStrokeWidth(20);
		mBallPaint.setStyle(Paint.Style.FILL);
		
//
//		int viewWidth = this.getWidth();
//		int viewHeight = this.getHeight();
//		Log.v(TAG, "width: " + viewWidth);
//		Log.v(TAG, "height: " + viewHeight);
//		circle = new CircleArea(viewWidth/2,viewHeight/2,viewHeight/2);
		
		circle = new CircleArea(80,80,50);
		
		point1 = new Point();
		point1.x = circle.centerX-circle.radius;
		point1.y = circle.centerY-circle.radius;

		point2 = new Point();
		point2.x = circle.centerX+circle.radius;
		point2.y = circle.centerY-circle.radius;

		point3 = new Point();
		point3.x = circle.centerX+circle.radius;
		point3.y = circle.centerY+circle.radius;

		point4 = new Point();
		point4.x = circle.centerX-circle.radius;
		point4.y = circle.centerY+circle.radius;

		// declare each ball with the ColorBall class, balID assigned automatically by ColorBall class 
		// depending on order ball added to colorball array
		colorballs.add(new ColorBall(ct, point1, 10));
		colorballs.add(new ColorBall(ct, point2, 10));
		colorballs.add(new ColorBall(ct, point3, 10));
		colorballs.add(new ColorBall(ct, point4, 10));

	}

	@Override
	public void onDraw(final Canvas canv) {
		//draw the circle
		canv.drawCircle(circle.centerX, circle.centerY, circle.radius, mCirclePaint);

		//draw the rectangle; note colorballs is zero-indexed 
		if (groupId == 1) {
			canv.drawRect(colorballs.get(0).getX(), colorballs.get(0).getY(), 
					colorballs.get(2).getX(), colorballs.get(2).getY(),mRectPaint); 
		} else {
			canv.drawRect(colorballs.get(3).getX(), colorballs.get(1).getY(), 
					colorballs.get(1).getX(), colorballs.get(3).getY(),mRectPaint); 
		}


		// draw the balls on the canvas
		for (ColorBall ball : colorballs) {
			canv.drawCircle(ball.getX(), ball.getY(), ball.getRadius(),mBallPaint);
		}
		scale_mm2pix = penny_rad/circle.radius; //mm/pix 
		Log.v(TAG,"scale: " + scale_mm2pix);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {


		int xTouch = (int) event.getX();
		int yTouch = (int) event.getY();

		// get touch event coordinates and make transparent circle from it
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			Log.v(TAG,"Moving figure : " + xTouch + ";" + yTouch);
			// check if inside the bounds of the ball 
			for (ColorBall ball : colorballs) {
				// get the center for the ball
				int centerX = ball.getX();
				int centerY = ball.getY();
				// calculate the distance from the touch to the center of the ball
				double dist = Math
						.sqrt((double) (((centerX - xTouch) * (centerX - xTouch)) + (centerY - yTouch)
								* (centerY - yTouch)));

				if (dist < ball.getRadius()) {
					mBallPaint.setColor(Color.RED);
					balID = ball.getID();
					Log.v(TAG,"Ball selected: " + balID);
					if (balID == 1 || balID == 3) {
						groupId = 2;
					} else if(balID == 0 || balID == 2) {
						groupId = 1;
					}
					invalidate();
					break;
				}
			}

			if(groupId > -1){
				break;
			}
			mBallPaint.setColor(Color.GRAY);
			circle.centerX = xTouch;
			circle.centerY = yTouch;

			colorballs.get(0).setX(circle.centerX-circle.radius);
			colorballs.get(0).setY(circle.centerY-circle.radius);
			colorballs.get(1).setX(circle.centerX+circle.radius);
			colorballs.get(1).setY(circle.centerY-circle.radius);
			colorballs.get(2).setX(circle.centerX+circle.radius);
			colorballs.get(2).setY(circle.centerY+circle.radius);
			colorballs.get(3).setX(circle.centerX-circle.radius);
			colorballs.get(3).setY(circle.centerY+circle.radius);

			invalidate();
			break;


		case MotionEvent.ACTION_MOVE: // touch drag ball with same finger used to select
			Log.v(TAG,"Moving Ball : " + balID);
			if (balID > -1) {
//				int ball_index=balID-1;
				colorballs.get(balID).setX(xTouch);
				colorballs.get(balID).setY(yTouch);
				

				//dynamically change other balls positions to reshape square
				if (groupId == 1) {
					double diagLength = Math
							.sqrt((double) (((colorballs.get(0).getX() -colorballs.get(2).getX()) * 
									(colorballs.get(0).getX() -colorballs.get(2).getX())
									+ (colorballs.get(0).getY() -colorballs.get(2).getY()) * 
									(colorballs.get(0).getY() -colorballs.get(2).getY()))));
					int width = (int) (diagLength/Math.sqrt(2));
					int height = (int) (diagLength/Math.sqrt(2));
					colorballs.get(1).setX(colorballs.get(0).getX() + width);
					colorballs.get(1).setY(colorballs.get(0).getY());
					colorballs.get(2).setX(colorballs.get(0).getX() + width);
					colorballs.get(2).setY(colorballs.get(0).getY() + height);
					colorballs.get(3).setX(colorballs.get(0).getX());
					colorballs.get(3).setY(colorballs.get(0).getY() + height);
				} else {
					double diagLength = Math
							.sqrt((double) (((colorballs.get(1).getX() -colorballs.get(3).getX()) * 
									(colorballs.get(1).getX() -colorballs.get(3).getX())
									+ (colorballs.get(1).getY() -colorballs.get(3).getY()) * 
									(colorballs.get(1).getY() -colorballs.get(3).getY()))));
					int width = (int) (diagLength/Math.sqrt(2));
					int height = (int) (diagLength/Math.sqrt(2));
					colorballs.get(0).setX(colorballs.get(1).getX() - width);
					colorballs.get(0).setY(colorballs.get(1).getY());
					colorballs.get(2).setX(colorballs.get(1).getX());
					colorballs.get(2).setY(colorballs.get(1).getY() + height);
					colorballs.get(3).setX(colorballs.get(1).getX() - width);
					colorballs.get(3).setY(colorballs.get(1).getY() + height);
				}

				circle.radius = (colorballs.get(1).getX() - colorballs.get(0).getX())/2;
				circle.centerX = colorballs.get(0).getX() + circle.radius;
				circle.centerY = colorballs.get(0).getY() + circle.radius;

				invalidate();
			}
			invalidate();
			break;

		case MotionEvent.ACTION_UP:
			Log.v(TAG,"done");
			balID = -1;
			groupId = -1;

			break;

		default:
			invalidate();
			break;


		}
		return true;

	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		mMeasuredRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
	}
	
}