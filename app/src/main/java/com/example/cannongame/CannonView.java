package com.example.cannongame;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class CannonView extends SurfaceView {

    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public int getScreenHeight() { return 0; }
    public void playSound(int id) {}
    public void stopGame() {}
    public void releaseResources() {}
}