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

public class LineDrawingView extends View {

	private static final String TAG = "LineDrawingView";
	private Rect mMeasuredRect;
	Point point1, point2;
	public static double length;

	private ArrayList<ColorBall> colorballs = new ArrayList<ColorBall>();
	// array that holds the balls
	private int balID = 0;
	// variable to know what ball is being dragged


	/** Stores data about single circle */
	private static class Line {
		int x1, x2, y1, y2;
		
		Line(int x1, int y1, int x2, int y2) {
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
		}

		@Override
		public String toString() {
			return "Line:" + "p1(" + x1 + ", " +y1 + ") " +
					"p2("+ x2 + "," + y2 + ")";
		}

		public double calcLength() {
			return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
		}
	}

	/** Paint to draw circles */
	private Paint mLinePaint;
	private Paint mBallPaint;
	private Line line;


	/**
	 * Default constructor
	 *
	 * @param ct {@link android.content.Context}
	 */
	public LineDrawingView(final Context ct) {
		super(ct);
		init(ct);
		setFocusable(true); // necessary for getting the touch events

	}

	public LineDrawingView(final Context ct, final AttributeSet attrs) {
		super(ct, attrs);
		init(ct);
		setFocusable(true); // necessary for getting the touch events
	}

	public LineDrawingView(final Context ct, final AttributeSet attrs, final int defStyle) {
		super(ct, attrs, defStyle);
		init(ct);
		setFocusable(true);
	}

	private void init(final Context ct) {
		mLinePaint = new Paint();
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(Color.parseColor("#55000000"));
		mLinePaint.setStyle(Paint.Style.FILL);
		mLinePaint.setStrokeJoin(Paint.Join.ROUND);
		mLinePaint.setStrokeWidth(5);

		mBallPaint = new Paint();
		mBallPaint.setAntiAlias(true);
		mBallPaint.setColor(Color.GRAY);
		mBallPaint.setStrokeWidth(20);
		mBallPaint.setStyle(Paint.Style.FILL);

		point1 = new Point();
		point1.x = 100;
		point1.y = 400;

		point2 = new Point();
		point2.x = 250;
		point2.y = 400;
		
		line = new Line(point1.x, point1.y, point2.x, point2.y);
		
		// declare each ball with the ColorBall class
		ColorBall.count = 0; //reset count for BallID after creating CirclesDrawingView
		colorballs.add(new ColorBall(ct, point1,(int) line.calcLength()/20 ));
		colorballs.add(new ColorBall(ct, point2, (int) line.calcLength()/20));

	}

	@Override
	public void onDraw(final Canvas canv) {
		//draw the line
		canv.drawLine(line.x1, line.y1, line.x2, line.y2, mLinePaint);


		// draw the balls on the canvas
		for (ColorBall ball : colorballs) {
			canv.drawCircle(ball.getX(), ball.getY(), ball.getRadius(),mBallPaint);
		}
		
		length = line.calcLength();
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {


		int xTouch = (int) event.getX();
		int yTouch = (int) event.getY();

		// get touch event coordinates and make transparent circle from it
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			Log.v(TAG,"Moving line : " + xTouch + ";" + yTouch);
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
					invalidate();
					break;
				}
			}

			if(balID > 0){
				break;
			}
			
			mBallPaint.setColor(Color.GRAY);
			int x1 = colorballs.get(0).getX(); 
			int x2 = colorballs.get(1).getX();
			int y1 = colorballs.get(0).getY();
			int y2 = colorballs.get(1).getY();
			int dist_x1_x2 = x2 - x1;
			int dist_y1_y2 = y2 - y1;
			colorballs.get(0).setX(xTouch);
			colorballs.get(0).setY(yTouch);
			colorballs.get(1).setX(xTouch + dist_x1_x2);
			colorballs.get(1).setY(yTouch + dist_y1_y2);
			line = new Line(colorballs.get(0).getX(), colorballs.get(0).getY(), 
					colorballs.get(1).getX(), colorballs.get(1).getY());			
			invalidate();
			break;


		case MotionEvent.ACTION_MOVE: // touch drag ball with same finger used to select
			Log.v(TAG,"Moving Ball : " + balID);
			if (balID > -1) {
				int ball_index=balID-1;
				colorballs.get(ball_index).setX(xTouch);
				colorballs.get(ball_index).setY(yTouch);
				//dynamically reshape line
				line = new Line(colorballs.get(0).getX(), colorballs.get(0).getY(), 
						colorballs.get(1).getX(), colorballs.get(1).getY());			
				invalidate();
			}
			invalidate();
			break;

		case MotionEvent.ACTION_UP:
			Log.v(TAG,"done");
			balID = -1;
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


