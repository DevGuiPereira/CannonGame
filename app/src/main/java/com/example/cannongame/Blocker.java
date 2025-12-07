package com.example.cannongame;
// Blocker.java

public class Blocker extends GameElement {
    private int missPenalty; // the miss penalty for this Blocker

    // constructor
    public Blocker(CannonView view, int color, int missPenalty, int x,
                   int y, int width, int length, float velocityY) {

        // ESTA É A ÚNICA CHAMADA SUPER PERMITIDA
        super(view, color, CannonView.BLOCKER_SOUND_ID, x, y, width, length,
                velocityY); //

        // then initializes missPenalty
        this.missPenalty = missPenalty; //
    }

    // returns the miss penalty for this Blocker
    public int getMissPenalty() {
        return missPenalty; //
    }
}
