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
import android.media.AudioManager;
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

import java.util.ArrayList;
import java.util.Random;

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CannonView"; // para registrar erros no LogCat

    // --- CONSTANTES DE JOGO (Init) ---
    // Necessárias para as classes Cannon, Cannonball, etc. funcionarem
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

    // Variáveis de Controle da Thread (Thread)
    private CannonThread cannonThread; // controla o loop do jogo
    private boolean dialogIsDisplayed = false;

    // Objetos do Jogo (Init)
    private Activity activity; // referência para a Activity
    private Cannon cannon;
    private Blocker blocker;
    private ArrayList<Target> targets;

    // Variáveis de Dimensão (Init)
    private int screenWidth;
    private int screenHeight;

    // Variáveis de Estado do Jogo
    private boolean gameOver; // o jogo acabou?
    private double timeLeft; // tempo restante
    private int shotsFired; // tiros disparados
    private double totalElapsedTime; // tempo total decorrido

    // Variáveis de Som e Desenho (Init / Draw)
    private SoundPool soundPool;
    private SparseIntArray soundMap;
    private Paint textPaint;
    private Paint backgroundPaint;

    // ----------------------------------------------------------
    // 1. INICIALIZAÇÃO (INIT) - Construtor e Configuração
    // ----------------------------------------------------------
    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;

        // Registra o callback do SurfaceHolder
        getHolder().addCallback(this);

        // Configura atributos de áudio para jogos
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

        // Inicializa SoundPool para tocar efeitos sonoros
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();

        // Carrega sons no mapa de sons
        soundMap = new SparseIntArray(3);
        soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
        soundMap.put(CANNON_SOUND_ID, soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));

        // Configura Paints (Canetas) para desenho
        textPaint = new Paint();
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE); // Fundo branco
    }

    // Chamado quando o tamanho da view muda (ex: rotação ou criação)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        // Configura o tamanho do texto baseado na altura da tela
        textPaint.setTextSize((int) (TEXT_SIZE_PERCENT * screenHeight));
        textPaint.setAntiAlias(true);
    }

    // Retorna a largura da tela (usado pelas classes Cannon/Cannonball)
    public int getScreenWidth() {
        return screenWidth;
    }

    // Retorna a altura da tela (usado pelas classes Cannon/Cannonball)
    public int getScreenHeight() {
        return screenHeight;
    }

    // Toca um som específico
    public void playSound(int soundId) {
        if (soundPool != null) {
            soundPool.play(soundMap.get(soundId), 1, 1, 1, 0, 1f);
        }
    }

    // Reinicia os elementos e inicia um novo jogo
    public void newGame() {
        // Cria o Canhão
        cannon = new Cannon(this,
                (int) (CANNON_BASE_RADIUS_PERCENT * screenHeight),
                (int) (CANNON_BARREL_LENGTH_PERCENT * screenWidth),
                (int) (CANNON_BARREL_WIDTH_PERCENT * screenHeight));

        Random random = new Random();
        targets = new ArrayList<>();

        // Inicializa posição X do primeiro alvo
        int targetX = (int) (TARGET_FIRST_X_PERCENT * screenWidth);
        // Calcula coordenada Y dos alvos
        int targetY = (int) ((0.5 - TARGET_LENGTH_PERCENT / 2) * screenHeight);

        // Cria e adiciona TARGET_PIECES (9) alvos à lista
        for (int n = 0; n < TARGET_PIECES; n++) {
            // Velocidade aleatória
            double velocity = screenHeight * (random.nextDouble() *
                    (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) + TARGET_MIN_SPEED_PERCENT);

            // Alterna cores (Escuro/Claro) - Requer R.color.dark e R.color.light em colors.xml
            int color = (n % 2 == 0) ?
                    getResources().getColor(R.color.dark, getContext().getTheme()) :
                    getResources().getColor(R.color.light, getContext().getTheme());

            velocity *= -1; // inverte velocidade para o próximo alvo

            // Cria o alvo com os parâmetros exigidos pela classe Target
            targets.add(new Target(this, color, HIT_REWARD, targetX, targetY,
                    (int) (TARGET_WIDTH_PERCENT * screenWidth),
                    (int) (TARGET_LENGTH_PERCENT * screenHeight),
                    (float) velocity));

            // Aumenta x para o próximo alvo
            targetX += (TARGET_WIDTH_PERCENT + TARGET_SPACING_PERCENT) * screenWidth;
        }

        // Cria um novo Bloqueador
        blocker = new Blocker(this, Color.BLACK, MISS_PENALTY,
                (int) (BLOCKER_X_PERCENT * screenWidth),
                (int) ((0.5 - BLOCKER_LENGTH_PERCENT / 2) * screenHeight),
                (int) (BLOCKER_WIDTH_PERCENT * screenWidth),
                (int) (BLOCKER_LENGTH_PERCENT * screenHeight),
                (float) (BLOCKER_SPEED_PERCENT * screenHeight));

        timeLeft = 10; // Contagem regressiva de 10 segundos
        shotsFired = 0; // Zera tiros
        totalElapsedTime = 0.0; // Zera tempo total

        if (gameOver) {
            gameOver = false; // O jogo não acabou mais
            cannonThread = new CannonThread(getHolder()); // Cria nova thread
            cannonThread.start(); // Inicia o loop
        }

        hideSystemBars(); // Oculta barras do sistema (Modo imersivo)
    }

    // ----------------------------------------------------------
    // 2. THREAD (GAME LOOP) - Gerenciamento da Thread
    // ----------------------------------------------------------

    // Classe interna para controlar o loop do jogo
    private class CannonThread extends Thread {
        private SurfaceHolder surfaceHolder; // para manipular o canvas
        private boolean threadIsRunning = true; // flag de controle

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
                    // Bloqueia o canvas para desenho exclusivo desta thread
                    canvas = surfaceHolder.lockCanvas(null);

                    // Sincroniza surfaceHolder para evitar conflitos
                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        totalElapsedTime += elapsedTimeMS / 1000.0;

                        // Atualiza as posições do jogo (Lógica de física)
                        updatePositions(elapsedTimeMS);

                        // Desenha os elementos do jogo (Visual)
                        drawGameElements(canvas);

                        previousFrameTime = currentTime; // Atualiza tempo
                    }
                } finally {
                    // Exibe o canvas na tela e libera o bloqueio
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    // Chamado quando a superfície é criada (inicia o jogo)
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            newGame();
            cannonThread = new CannonThread(holder);
            cannonThread.setRunning(true);
            cannonThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    // Chamado quando a superfície é destruída (para a thread)
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        cannonThread.setRunning(false); // Encerra loop
        while (retry) {
            try {
                cannonThread.join(); // Aguarda thread terminar
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    }

    public void stopGame() {
        if (cannonThread != null)
            cannonThread.setRunning(false);
    }

    // ----------------------------------------------------------
    // 3. DRAW (DESENHO) - Renderização
    // ----------------------------------------------------------
    public void drawGameElements(Canvas canvas) {
        // Limpa o fundo com branco
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

        // Exibe o tempo restante
        canvas.drawText(getResources().getString(R.string.time_remaining_format, timeLeft),
                50, 100, textPaint);

        // Desenha o Canhão
        if (cannon != null)
            cannon.draw(canvas);

        // Desenha o Bloqueador e Bola (se estiverem ativos)
        if (cannon.getCannonball() != null && cannon.getCannonball().isOnScreen())
            cannon.getCannonball().draw(canvas);

        if (blocker != null)
            blocker.draw(canvas);

        // Desenha todos os Alvos
        if (targets != null) {
            for (Target target : targets)
                target.draw(canvas);
        }
    }

    // ----------------------------------------------------------
    // MÉTODOS DE LÓGICA E INPUT
    // (Incluídos apenas para evitar erros de compilação, a lógica real
    // deve ser integrada com o restante do grupo)
    // ----------------------------------------------------------

    public void releaseResources() {
        soundPool.release(); // libera recursos de áudio
        soundPool = null;
    }

    // Stub para atualização de posições (Responsabilidade de outro membro ou Seção 6.13.7)
    private void updatePositions(double elapsedTimeMS) {
        double interval = elapsedTimeMS / 1000.0;

        // Atualiza bola, bloqueador e alvos (Lógica simplificada para a Thread funcionar)
        if (cannon.getCannonball() != null)
            cannon.getCannonball().update(interval);
        blocker.update(interval);
        for (Target target : targets) target.update(interval);

        timeLeft -= interval; // Reduz tempo

        // Se o tempo acabar... (Lógica de Game Over iria aqui)
        if (timeLeft <= 0) {
            timeLeft = 0.0;
            gameOver = true;
            cannonThread.setRunning(false);
            showGameOverDialog(R.string.lose); // Requer string 'lose' em strings.xml
        }
    }

    // Tratamento de Toque (Seção 6.13.14)
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            alignAndFireCannonball(e); // Dispara ao tocar
        }
        return true;
    }

    private void alignAndFireCannonball(MotionEvent event) {
        // Lógica de mira e tiro
        Point touchPoint = new Point((int) event.getX(), (int) event.getY());
        double centerMinusY = (screenHeight / 2 - touchPoint.y);
        double angle = 0;
        angle = Math.atan2(touchPoint.x, centerMinusY);
        cannon.align(angle);
        if (cannon.getCannonball() == null || !cannon.getCannonball().isOnScreen()) {
            cannon.fireCannonball();
            shotsFired++;
        }
    }

    // Stub para exibir diálogo de fim de jogo
    private void showGameOverDialog(final int messageId) {
        // Lógica para exibir DialogFragment (via runOnUiThread)
        // Isso requer integração com a Activity principal
    }

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
}