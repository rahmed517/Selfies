package com.example.selfies;

import android.content.Context;
import android.graphics.Point;

public class ColorBall {

    Context mContext;
    Point point;
    int id;
    static int count = 0;
    int radius;

    public ColorBall(Context context, Point point, int radius) {
        this.id = ++count;
      
        mContext = context;
        this.point = point;
        this.radius = radius;
    }

    public int getX() {
        return point.x;
    }

    public int getY() {
        return point.y;
    }

    public int getID() {
        return id;
    }
    
    public int getRadius() {
    	return radius;
    }

    public void setX(int x) {
        point.x = x;
    }

    public void setY(int y) {
        point.y = y;
    }
    
    public void setID(int id){
    	this.id = id;
    }
    
}
