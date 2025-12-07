package com.example.cannongame; // (Confira o nome do seu pacote)

// Target.java - Seção 6.10
public class Target extends GameElement {
    private int hitReward; // the hit reward for this target

    // constructor
    public Target(CannonView view, int color, int hitReward, int x,
                  int y, int width, int length, float velocityY) {

        // Chama o construtor da superclasse (linha 11 do livro)
        super(view, color, CannonView.TARGET_SOUND_ID, x, y, width, length,
                velocityY);

        // initializes hitReward (linha 14 do livro)
        this.hitReward = hitReward;
    }

    // returns the hit reward for this Target (linha 17-19 do livro)
    public int getHitReward() {
        return hitReward;
    }
}