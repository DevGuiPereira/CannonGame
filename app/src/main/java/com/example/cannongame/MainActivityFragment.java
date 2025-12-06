package com.example.cannongame; // <--- Mantenha o SEU pacote

import android.media.AudioManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment; // Import moderno (diferente do livro)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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