// Representa o Canhão e dispara a Bola de Canhão
package com.example.cannongame;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class Cannon {
    private int baseRadius; // Raio da base do canhão
    private int barrelLength;  // Comprimento do cano do canhão
    private Point barrelEnd = new Point(); // Ponto final do cano do Canhão
    private double barrelAngle; // ângulo do cano do Canhão
    private Cannonball cannonball; // a Bola de Canhão do Canhão
    private Paint paint = new Paint(); // Tinta usada para desenhar o canhão
    private CannonView view;  // visualização contendo o Canhão

    public Cannon(CannonView view, int baseRadius, int barrelLength, int barrelWidth) {
        this.view = view;
        this.baseRadius = baseRadius;
        this.barrelLength = barrelLength;
        paint.setStrokeWidth(barrelWidth); // definir largura do cano
        paint.setColor(Color.BLACK); // A cor do canhão é preta
        align(Math.PI / 2); // Cano de canhão voltado para a direita
    }

    public void align(double barrelAngle) { // alinha o cano do Canhão ao ângulo dado
        this.barrelAngle = barrelAngle;

        barrelEnd.x = (int) (barrelLength * Math.sin(barrelAngle));
        barrelEnd.y = (int) (-barrelLength * Math.cos(barrelAngle)) + view.getScreenHeight() / 2;
    }

    // cria e dispara Cannonball na direção em que Cannon aponta
    public void fireCannonball() {

        // calcule o componente x da velocidade da bala de canhão
        int velocityX = (int) (CannonView.CANNONBALL_SPEED_PERCENT *
                view.getScreenWidth() * Math.sin(barrelAngle));

        // calcule o componente y da velocidade da bala de canhão
        int velocityY = (int) (CannonView.CANNONBALL_SPEED_PERCENT *
                view.getScreenWidth() * -Math.cos(barrelAngle));

        // calcule o raio da bala de canhão
        int radius = (int) (view.getScreenHeight() * CannonView.CANNONBALL_RADIUS_PERCENT);

        // construa Cannonball e posicione-o no Cannon
        cannonball = new Cannonball(view, Color.BLACK,
                CannonView.CANNON_SOUND_ID, -radius,
                view.getScreenHeight() / 2 - radius, radius, velocityX, velocityY);

        // reproduzir som de bola de canhão de fogo
        cannonball.playSound();
    }

    // desenha o canhão na tela
    public void draw(Canvas canvas) {

        // desenhar cano de canhão
        canvas.drawLine(0, view.getScreenHeight() / 2, barrelEnd.x, barrelEnd.y, paint);

        // desenhar base do canhão
        canvas.drawCircle(0, (int) view.getScreenHeight() / 2, (int) baseRadius, paint);
    }

    // retorna a Cannonball que este Cannon disparou
    public Cannonball getCannonball() {
        return cannonball;
    }

    // remove a Cannonball do jogo
    public void removeCannonball() {
        cannonball = null;
    }
}