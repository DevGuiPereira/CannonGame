package com.example.cannongame;
public class Target extends GameElement {

    public Target(CannonView view, int color, int hitReward, int x, int y, int width, int length, float velocityY) {
        super(view, color, 0, x, y, width, length, velocityY);
    }
}