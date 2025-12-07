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

    public static final int TARGET_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;
    public static final int CANNON_SOUND_ID = 3;
    public static final int MISS_SOUND_ID = 4;

    public static final double CANNONBALL_RADIUS_PERCENT = 0.0075;
    public static final double CANNONBALL_SPEED_PERCENT = 2.0;

    // Método getScreenWidth()
    public int getScreenWidth() {
        // Isso retorna a largura da SurfaceView (a tela do jogo)
        return getWidth();
    }



}