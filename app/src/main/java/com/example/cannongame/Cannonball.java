package com.example.cannongame;
public class Cannonball extends GameElement {

    public Cannonball(CannonView view, int color, int soundId, int x, int y, int radius, float velocityX, float velocityY) {
        super(view, color, soundId, x, y, radius * 2, radius * 2, velocityY);
    }
}