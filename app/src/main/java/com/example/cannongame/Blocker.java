package com.example.cannongame;
public class Blocker extends GameElement {

    public Blocker(CannonView view, int color, int missPenalty, int x, int y, int width, int length, float velocityY) {
        super(view, color, 0, x, y, width, length, velocityY);
    }
}