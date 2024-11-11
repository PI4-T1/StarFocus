package br.edu.puccampinas.starfocusapp;

import android.os.AsyncTask;
import android.util.Log;

public class ClienteAndroid {
    private final ProgressListener listener;
    private final Parceiro parceiro;

    // Construtor
    public ClienteAndroid(ProgressListener listener, Parceiro parceiro) {
        this.listener = listener;
        this.parceiro = parceiro;
    }

    // Função para enviar progresso em segundo plano
    public void sendProgress(final int totalTarefas, final int tarefasConcluidas) {
        new SendProgressTask(listener, parceiro).execute(totalTarefas, tarefasConcluidas);
    }

    // AsyncTask deve ser estática para evitar leaks de memória
    private static class SendProgressTask extends AsyncTask<Integer, Void, Integer> {
        private final ProgressListener listener;
        private final Parceiro parceiro;

        // Construtor para passar listener e parceiro para a AsyncTask
        public SendProgressTask(ProgressListener listener, Parceiro parceiro) {
            this.listener = listener;
            this.parceiro = parceiro;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            int totalTarefas = params[0];
            int tarefasConcluidas = params[1];

            try {
                if (parceiro != null) {
                    // Enviar dois inteiros diretamente
                    parceiro.getDataOutputStream().writeInt(totalTarefas);
                    parceiro.getDataOutputStream().writeInt(tarefasConcluidas);

                    // Receber a resposta com a porcentagem
                    return parceiro.getDataInputStream().readInt();
                } else {
                    Log.e("ClienteAndroid", "Parceiro está nulo");
                    return -1; // Indicando erro
                }
            } catch (Exception e) {
                Log.e("ClienteAndroid", "Erro ao enviar progresso", e);
                return -1; // Indicando erro
            }
        }

        @Override
        protected void onPostExecute(Integer porcentagem) {
            if (porcentagem != -1 && listener != null) {
                listener.onProgressUpdate(porcentagem);  // Atualiza o progresso
                Log.i("ClienteAndroid", "Porcentagem concluída recebida do servidor: " + porcentagem + "%");
            } else {
                Log.e("ClienteAndroid", "Erro ao obter porcentagem ou listener é nulo");
            }
        }
    }
}
