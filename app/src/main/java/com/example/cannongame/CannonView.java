package com.example.cannongame;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class CannonView extends SurfaceView {
    // Construtor necessário
    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Métodos stub para o GameElement não dar erro
    public int getScreenHeight() { return 0; }
    public void playSound(int id) {}
}