package br.edu.puccampinas.starfocusapp;

import android.util.Log;

public class ClienteAndroid {
    private final ProgressListener listener;
    private final Parceiro parceiro;

    public ClienteAndroid(ProgressListener listener, Parceiro parceiro) {
        this.listener = listener;
        this.parceiro = parceiro;
    }

    // Método para enviar o progresso
    public void sendProgress(int totalTarefas, int tarefasConcluidas) {
        try {
            BarraDeProgresso barraProgresso = new BarraDeProgresso(totalTarefas, tarefasConcluidas);
            if (parceiro != null) {
                parceiro.receba(barraProgresso);  // Envia o progresso para o parceiro
            } else {
                Log.e("ClienteAndroid", "Objeto parceiro está nulo");
            }

            // Recebe a resposta com a porcentagem
            if (parceiro != null) {
                Comunicado resposta = parceiro.envie();  // Recebe a resposta do servidor
                if (resposta instanceof RespostaProgresso) {
                    int porcentagem = ((RespostaProgresso) resposta).getPorcentagemConcluida();
                    if (listener != null) {
                        listener.onProgressUpdate(porcentagem);  // Atualiza o progresso
                    } else {
                        Log.e("ClienteAndroid", "Listener (Parceiro) is null");
                    }
                    Log.i("ClienteAndroid", "Porcentagem concluída recebida do servidor: " + porcentagem + "%");
                }
            } else {
                Log.e("ClienteAndroid", "Parceiro está nulo");
            }

        } catch (Exception e) {
            Log.e("ClienteAndroid", "Erro ao enviar progresso", e);
        }
    }
}
