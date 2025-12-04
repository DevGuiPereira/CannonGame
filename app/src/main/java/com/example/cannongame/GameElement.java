package com.example.cannongame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class GameElement {
    protected CannonView view; // a view onde o elemento está
    protected Paint paint = new Paint(); // para desenhar
    protected Rect shape; // limites retangulares do elemento
    private float velocityY; // velocidade vertical
    private int soundId; // som associado

    // Construtor
    public GameElement(CannonView view, int color, int soundId, int x,
                       int y, int width, int length, float velocityY) {
        this.view = view;
        paint.setColor(color);
        shape = new Rect(x, y, x + width, y + length); // define limites
        this.soundId = soundId;
        this.velocityY = velocityY;
    }

    // Atualiza posição e verifica colisão com as paredes
    public void update(double interval) {
        // atualiza posição vertical
        shape.offset(0, (int) (velocityY * interval));

        // se bater no topo ou fundo da tela, inverte a velocidade
        if (shape.top < 0 && velocityY < 0 ||
                shape.bottom > view.getScreenHeight() && velocityY > 0) {
            velocityY *= -1; // inverte direção
        }
    }

    // Desenha o elemento na tela
    public void draw(Canvas canvas) {
        canvas.drawRect(shape, paint);
    }

    // Toca o som associado
    public void playSound() {
        view.playSound(soundId);
    }
}