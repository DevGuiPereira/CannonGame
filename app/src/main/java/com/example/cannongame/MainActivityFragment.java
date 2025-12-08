package com.example.cannongame; // <--- Mantenha o SEU pacote

import android.media.AudioManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment; // Import moderno (diferente do livro)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainActivityFragment extends Fragment {
    private CannonView cannonView; // Referência para a View do jogo

    // Chamado quando o Fragmento cria sua visualização
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Infla o layout que tem o CannonView
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Pega a referência do CannonView pelo ID
        cannonView = (CannonView) view.findViewById(R.id.cannonView);

        final View startScreen = view.findViewById(R.id.startScreen);
        Button btnStart = (Button) view.findViewById(R.id.btn_start);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Esconde a tela de início
                startScreen.setVisibility(View.GONE);

                // Inicia o jogo
                cannonView.newGame();
            }
        });

        final View endScreen = view.findViewById(R.id.endScreen);
        TextView tvResult = (TextView) view.findViewById(R.id.tv_game_result);
        TextView tvScore = (TextView) view.findViewById(R.id.tv_final_score);
        Button btnRestart = (Button) view.findViewById(R.id.btn_restart);

        // Passa as referências para a CannonView poder controlá-las
        cannonView.setEndGameScreen(endScreen, tvResult, tvScore);

        // Ação do Botão Restart
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Esconde a tela de fim de jogo
                endScreen.setVisibility(View.GONE);
                // Inicia um novo jogo
                cannonView.newGame();
            }
        });

        return view;
    }

    // Configura o volume quando a atividade é criada
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Permite que os botões de volume controlem o som do jogo
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    // Para o jogo quando o app vai para o fundo (pausa)
    @Override
    public void onPause() {
        super.onPause();
        cannonView.stopGame();
    }

    // Libera recursos (sons) quando o app fecha
    @Override
    public void onDestroy() {
        super.onDestroy();
        cannonView.releaseResources();
    }
}