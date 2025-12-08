package com.example.cannongame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CannonView";

    // --- CONSTANTES DE JOGO ---
    private boolean isGameRunning = false;
    public static final int MISS_PENALTY = 2; // segundos deduzidos ao errar
    public static final int HIT_REWARD = 3; // segundos adicionados ao acertar

    // Constantes de Som
    public static final int TARGET_SOUND_ID = 0;
    public static final int CANNON_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;

    // Constantes de Dimensão (Percentuais da tela)
    public static final double CANNON_BASE_RADIUS_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_WIDTH_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_LENGTH_PERCENT = 1.0 / 10;
    public static final double CANNONBALL_RADIUS_PERCENT = 3.0 / 80;
    public static final double CANNONBALL_SPEED_PERCENT = 3.0 / 2;
    public static final double TARGET_WIDTH_PERCENT = 1.0 / 40;
    public static final double TARGET_LENGTH_PERCENT = 3.0 / 20;
    public static final double TARGET_FIRST_X_PERCENT = 3.0 / 5;
    public static final double TARGET_SPACING_PERCENT = 1.0 / 60;
    public static final double TARGET_PIECES = 9;
    public static final double TARGET_MIN_SPEED_PERCENT = 3.0 / 4;
    public static final double TARGET_MAX_SPEED_PERCENT = 6.0 / 4;
    public static final double BLOCKER_WIDTH_PERCENT = 1.0 / 40;
    public static final double BLOCKER_LENGTH_PERCENT = 1.0 / 4;
    public static final double BLOCKER_X_PERCENT = 1.0 / 2;
    public static final double BLOCKER_SPEED_PERCENT = 1.0;
    public static final double TEXT_SIZE_PERCENT = 1.0 / 18;

    // Variáveis de Controle da Thread
    private CannonThread cannonThread; // controla o loop do jogo
    private boolean dialogIsDisplayed = false;

    // Variáveis da interface de Fim de Jogo
    private View endScreen;
    private TextView tvResult;
    private TextView tvScore;

    // Objetos do Jogo
    private Activity activity; // referência para a Activity
    private Cannon cannon;
    private Blocker blocker;
    private ArrayList<Target> targets;

    // Variáveis de Dimensão
    private int screenWidth;
    private int screenHeight;

    // Variáveis de Estado do Jogo
    private boolean gameOver; // o jogo acabou?
    private double timeLeft; // tempo restante
    private int shotsFired; // tiros disparados
    private double totalElapsedTime; // tempo total decorrido

    private int score = 0;

    // Variáveis de Som e Desenho
    private SoundPool soundPool;
    private SparseIntArray soundMap;
    private Paint textPaint;
    private Paint backgroundPaint;

    // ----------------------------------------------------------
    // 1. INICIALIZAÇÃO
    // ----------------------------------------------------------
    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;

        // Registra o callback do SurfaceHolder
        getHolder().addCallback(this);

        // Configura atributos de áudio para jogos
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

        // Inicializa SoundPool
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();

        // Carrega sons
        soundMap = new SparseIntArray(3);
        soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
        soundMap.put(CANNON_SOUND_ID, soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));

        // Configura Paints
        textPaint = new Paint();
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        textPaint.setTextSize((int) (TEXT_SIZE_PERCENT * screenHeight));
        textPaint.setAntiAlias(true);
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void playSound(int soundId) {
        if (soundPool != null) {
            soundPool.play(soundMap.get(soundId), 1, 1, 1, 0, 1f);
        }
    }

    public void newGame() {
        // Cria o Canhão
        cannon = new Cannon(this,
                (int) (CANNON_BASE_RADIUS_PERCENT * screenHeight),
                (int) (CANNON_BARREL_LENGTH_PERCENT * screenWidth),
                (int) (CANNON_BARREL_WIDTH_PERCENT * screenHeight));

        Random random = new Random();
        targets = new ArrayList<>();

        int targetX = (int) (TARGET_FIRST_X_PERCENT * screenWidth);
        int targetY = (int) ((0.5 - TARGET_LENGTH_PERCENT / 2) * screenHeight);

        for (int n = 0; n < TARGET_PIECES; n++) {
            double velocity = screenHeight * (random.nextDouble() *
                    (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) + TARGET_MIN_SPEED_PERCENT);

            int color = (n % 2 == 0) ?
                    getResources().getColor(R.color.dark, getContext().getTheme()) :
                    getResources().getColor(R.color.light, getContext().getTheme());

            velocity *= -1;

            targets.add(new Target(this, color, HIT_REWARD, targetX, targetY,
                    (int) (TARGET_WIDTH_PERCENT * screenWidth),
                    (int) (TARGET_LENGTH_PERCENT * screenHeight),
                    (float) velocity));

            targetX += (TARGET_WIDTH_PERCENT + TARGET_SPACING_PERCENT) * screenWidth;
        }

        blocker = new Blocker(this, Color.BLACK, MISS_PENALTY,
                (int) (BLOCKER_X_PERCENT * screenWidth),
                (int) ((0.5 - BLOCKER_LENGTH_PERCENT / 2) * screenHeight),
                (int) (BLOCKER_WIDTH_PERCENT * screenWidth),
                (int) (BLOCKER_LENGTH_PERCENT * screenHeight),
                (float) (BLOCKER_SPEED_PERCENT * screenHeight));

        timeLeft = 10;
        shotsFired = 0;
        score = 0;
        totalElapsedTime = 0.0;

        isGameRunning = true;

        if (gameOver) {
            gameOver = false;
            cannonThread = new CannonThread(getHolder());
            cannonThread.start();
        }

        hideSystemBars();
    }

    // ----------------------------------------------------------
    // 2. LÓGICA DO JOGO (UPDATE & COLISÃO)
    // ----------------------------------------------------------

    // Chamado repetidamente pela CannonThread para atualizar elementos do jogo
    private void updatePositions(double elapsedTimeMS) {
        if (!isGameRunning) {
            return;
        }
        double interval = elapsedTimeMS / 1000.0; // converte para segundos

        // Atualiza posição da bola se ela estiver na tela
        if (cannon.getCannonball() != null)
            cannon.getCannonball().update(interval);

        blocker.update(interval); // atualiza posição do bloqueador

        for (Target target : targets)
            target.update(interval); // atualiza posição dos alvos

        timeLeft -= interval; // subtrai do tempo restante

        // Se o tempo acabou
        if (timeLeft <= 0) {
            timeLeft = 0.0;
            gameOver = true;
            cannonThread.setRunning(false); // termina a thread
            showGameOverDialog(R.string.lose); // mostra diálogo de derrota
        }

        // Se todos os alvos foram atingidos
        if (targets.isEmpty()) {
            cannonThread.setRunning(false); // termina a thread
            showGameOverDialog(R.string.win); // mostra diálogo de vitória
            gameOver = true;
        }
    }

    // Verifica se a bola colide com o Bloqueador ou Alvos
    public void testForCollisions() {
        // Remove qualquer alvo que a bola colidir
        if (cannon.getCannonball() != null && cannon.getCannonball().isOnScreen()) {
            for (int n = 0; n < targets.size(); n++) {
                if (cannon.getCannonball().collidesWith(targets.get(n))) {
                    targets.get(n).playSound(); // som de acerto
                    timeLeft += targets.get(n).getHitReward(); // adiciona tempo extra

                    score += 100;

                    cannon.removeCannonball(); // remove a bola
                    targets.remove(n); // remove o alvo atingido
                    --n; // garante que não pulamos o teste do próximo alvo
                    break;
                }
            }
        } else {
            // Remove a bola se ela não deve estar na tela
            cannon.removeCannonball();
        }

        // Verifica colisão com o bloqueador
        if (cannon.getCannonball() != null &&
                cannon.getCannonball().collidesWith(blocker)) {
            blocker.playSound(); // som de bloqueio

            // Inverte a direção da bola
            cannon.getCannonball().reverseVelocityX();

            // Deduz a penalidade do tempo restante
            timeLeft -= blocker.getMissPenalty();
        }
    }

    // Alinha o cano e dispara se não houver bola na tela
    public void alignAndFireCannonball(MotionEvent event) {
        // pega a localização do toque nesta view
        Point touchPoint = new Point((int) event.getX(), (int) event.getY());

        // calcula a distância do toque a partir do centro da tela no eixo Y
        double centerMinusY = (screenHeight / 2 - touchPoint.y);

        double angle = 0; // inicializa ângulo

        // calcula o ângulo que o cano faz com a horizontal
        angle = Math.atan2(touchPoint.x, centerMinusY);

        // aponta o cano para onde a tela foi tocada
        cannon.align(angle);

        // dispara a bola se não houver uma na tela
        if (cannon.getCannonball() == null || !cannon.getCannonball().isOnScreen()) {
            cannon.fireCannonball();
            shotsFired++;
        }
    }

    // Exibe um AlertDialog quando o jogo termina
    private void showGameOverDialog(final int messageId) {
        // Usa a thread de UI para mexer na tela (obrigatório)
        activity.runOnUiThread(new Runnable() {
            public void run() {
                // 1. Configura o Texto de Vitória ou Derrota
                if (messageId == R.string.win) {
                    tvResult.setText("VOCÊ VENCEU!");
                    tvResult.setTextColor(Color.GREEN);
                } else {
                    tvResult.setText("GAME OVER");
                    tvResult.setTextColor(Color.RED);
                }

                // 2. Formata as estatísticas
                // %.2f significa: número decimal com 2 casas após a vírgula
                String timeFormatted = String.format("%.2f", totalElapsedTime);

                // 3. Monta o texto final com PULA LINHA (\n)
                String finalText = "Pontuação: " + score + "\n" +
                        "Tempo Total: " + timeFormatted + "s";

                tvScore.setText(finalText);

                // 4. Mostra a tela
                if (endScreen != null) {
                    endScreen.setVisibility(View.VISIBLE);
                }

                dialogIsDisplayed = true;
            }
        });
    }

    // ----------------------------------------------------------
    // 3. DESENHO (DRAW)
    // ----------------------------------------------------------
    public void drawGameElements(Canvas canvas) {
        // Limpa o fundo
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

        // Exibe o tempo restante
        canvas.drawText(getResources().getString(R.string.time_remaining_format, timeLeft),
                50, 100, textPaint);

        String scoreText = getResources().getString(R.string.score_hud_format, score);

        // Calculamos a largura do texto para alinhar direitinho à direita
        float textWidth = textPaint.measureText(scoreText);

        // Desenhamos: (Largura da Tela - Largura do Texto - Margem), Altura 100
        canvas.drawText(scoreText, screenWidth - textWidth - 50, 100, textPaint);

        if (cannon != null)
            cannon.draw(canvas);

        if (cannon.getCannonball() != null && cannon.getCannonball().isOnScreen())
            cannon.getCannonball().draw(canvas);

        if (blocker != null)
            blocker.draw(canvas);

        if (targets != null) {
            for (Target target : targets)
                target.draw(canvas);
        }
    }

    // ----------------------------------------------------------
    // 4. THREAD E CONTROLE DE SUPERFÍCIE
    // ----------------------------------------------------------
    private class CannonThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean threadIsRunning = true;

        public CannonThread(SurfaceHolder holder) {
            surfaceHolder = holder;
            setName("CannonThread");
        }

        public void setRunning(boolean running) {
            threadIsRunning = running;
        }

        @Override
        public void run() {
            Canvas canvas = null;
            long previousFrameTime = System.currentTimeMillis();

            while (threadIsRunning) {
                try {
                    canvas = surfaceHolder.lockCanvas(null);

                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        totalElapsedTime += elapsedTimeMS / 1000.0;

                        updatePositions(elapsedTimeMS); // atualiza estado do jogo
                        testForCollisions(); // testa colisões
                        drawGameElements(canvas); // desenha no canvas

                        previousFrameTime = currentTime;
                    }
                } finally {
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            newGame();

            isGameRunning = false;

            cannonThread = new CannonThread(holder);
            cannonThread.setRunning(true);
            cannonThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        cannonThread.setRunning(false);
        while (retry) {
            try {
                cannonThread.join();
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    }

    // ----------------------------------------------------------
    // 5. INPUT E RECURSOS
    // ----------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            alignAndFireCannonball(e);
        }
        return true;
    }

    public void stopGame() {
        if (cannonThread != null)
            cannonThread.setRunning(false);
    }

    public void releaseResources() {
        soundPool.release();
        soundPool = null;
    }

    // Métodos para esconder/mostrar barras do sistema (Modo Imersivo)
    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    // Método para conectar o XML com a lógica do jogo
    public void setEndGameScreen(View endScreen, TextView tvResult, TextView tvScore) {
        this.endScreen = endScreen;
        this.tvResult = tvResult;
        this.tvScore = tvScore;
    }
}