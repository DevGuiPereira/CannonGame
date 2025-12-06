// Representa a Bola de Canhão que o Canhão dispara
package com.example.cannongame;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Cannonball extends GameElement {
    private float velocityX;
    private boolean onScreen;

    // construtor
    public Cannonball(CannonView view, int color, int soundId, int x,
                      int y, int radius, float velocityX, float velocityY) {
        super(view, color, soundId, x, y, 2 * radius, 2 * radius, velocityY);
        this.velocityX = velocityX;
        onScreen = true;
    }

    // obtem o raio de Cannonball
    private int getRadius() {
        return (shape.right - shape.left) / 2;
    }

    // teste se Cannonball colide com o GameElement fornecido
    public boolean collidesWith(GameElement element) {
        return (Rect.intersects(shape, element.shape) && velocityX > 0);
    }

    // retorna verdadeiro se esta Cannonball estiver na tela
    public boolean isOnScreen() {
        return onScreen;
    }

    // inverte a velocidade horizontal do Cannonball
    public void reverseVelocityX() {
        velocityX *= -1;
    }

    // atualiza a posição do Cannonball
    @Override
    public void update(double interval) {
        super.update(interval);  // atualiza a posição vertical do Cannonball

        // atualizar posição horizontal
        shape.offset((int) (velocityX * interval), 0);

        // se Cannonball sair da tela
        if (shape.top < 0 || shape.left < 0 ||
                shape.bottom > view.getScreenHeight() ||
                shape.right > view.getScreenWidth()) {
            onScreen = false; // defina-o para ser removido
        }
    }

    // desenha a Cannonball na tela fornecida
    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(shape.left + getRadius(),
                shape.top + getRadius(), getRadius(), paint);
    }
}