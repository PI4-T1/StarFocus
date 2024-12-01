package br.edu.puccampinas.starfocusapp;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Classe ClienteAndroid responsável pela comunicação entre o aplicativo Android
 * e um servidor parceiro. Utiliza as interfaces ProgressListener e MetricsListener
 * para notificar sobre atualizações de progresso e métricas enviadas.
 * @author Lais
 */
public class ClienteAndroid {
    private final ProgressListener listener; // Listener para atualizações de progresso
    private final MetricsListener listener2; // Listener para atualizações de métricas
    private final Parceiro parceiro; // Objeto que representa o parceiro para troca de dados

    /**
     * Construtor da classe ClienteAndroid.
     *
     * @param listener Listener para atualizações de progresso.
     * @param listener2 Listener para atualizações de métricas.
     * @param parceiro Objeto Parceiro usado para comunicação com o servidor.
     */
    public ClienteAndroid(ProgressListener listener, MetricsListener listener2, Parceiro parceiro) {
        this.listener = listener;
        this.listener2 = listener2;
        this.parceiro = parceiro;
    }

    /**
     * Envia informações de progresso em segundo plano utilizando AsyncTask.
     *
     * @param totalTarefas Número total de tarefas.
     * @param tarefasConcluidas Número de tarefas concluídas.
     */
    public void sendProgress(final int totalTarefas, final int tarefasConcluidas) {
        new SendProgressTask(listener, parceiro).execute(totalTarefas, tarefasConcluidas);
    }

    /**
     * Envia métricas ao servidor em segundo plano utilizando AsyncTask.
     *
     * @param metricaString String que contém as métricas a serem enviadas.
     */
    public void sendMetrics(final String metricaString) {
        new SendMetricsTask(listener2, parceiro).execute(metricaString);
    }

    /**
     * AsyncTask para enviar informações de progresso (inteiros) ao servidor.
     */
    private static class SendProgressTask extends AsyncTask<Integer, Void, Integer> {
        private final ProgressListener listener;
        private final Parceiro parceiro;

        /**
         * Construtor da classe SendProgressTask.
         *
         * @param listener Listener para receber as atualizações de progresso.
         * @param parceiro Objeto Parceiro usado para envio de dados.
         */
        public SendProgressTask(ProgressListener listener, Parceiro parceiro) {
            this.listener = listener;
            this.parceiro = parceiro;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            // Obtém os parâmetros: total de tarefas e tarefas concluídas
            int totalTarefas = params[0];
            int tarefasConcluidas = params[1];
            try {
                if (parceiro != null) {
                    // Envia o "comando" 1 para indicar que está enviando progresso
                    parceiro.getDataOutputStream().writeInt(1); // 1 = comando para progresso
                    // Enviar os inteiros de progresso
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
                // Atualiza o progresso no listener
                listener.onProgressUpdate(porcentagem);
                Log.i("ClienteAndroid", "Porcentagem concluída recebida do servidor: " + porcentagem + "%");
            } else {
                Log.e("ClienteAndroid", "Erro ao obter porcentagem ou listener é nulo");
            }
        }
    }

    /**
     * AsyncTask para enviar métricas (string) ao servidor.
     */
    private static class SendMetricsTask extends AsyncTask<String, Void, String> {
        private final MetricsListener listener2;
        private final Parceiro parceiro;

        /**
         * Construtor da classe SendMetricsTask.
         *
         * @param listener2 Listener para receber as atualizações de métricas.
         * @param parceiro Objeto Parceiro usado para envio de dados.
         */
        public SendMetricsTask(MetricsListener listener2, Parceiro parceiro) {
            this.listener2 = listener2;
            this.parceiro = parceiro;
        }

        @Override
        protected String doInBackground(String... params) {
            // Obtém a string de métricas a ser enviada
            String metricaString = params[0];
            try {
                if (parceiro != null) {
                    // Envia o "comando" 2 para indicar que está enviando métricas
                    parceiro.getDataOutputStream().writeInt(2); // 2 = comando para métricas
                    // Enviar a string de métricas
                    parceiro.getDataOutputStream().writeUTF(metricaString);

                    // Recebe a resposta do servidor (string de métricas de volta)
                    return parceiro.getDataInputStream().readUTF();  // Retorna a string de métricas recebida do servidor
                } else {
                    Log.e("ClienteAndroid", "Parceiro está nulo");
                    return null; // Indicando erro
                }
            } catch (Exception e) {
                Log.e("ClienteAndroid", "Erro ao enviar métricas", e);
                return null; // Indicando erro
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.i("ClienteAndroid", "Métrica recebida com sucesso: " + result);
                // Notifica o listener sobre a atualização das métricas
                if (listener2 != null) {
                    listener2.onMetricsUpdate(result);  // Passa a string de métricas recebida
                }
            } else {
                Log.e("ClienteAndroid", "Erro ao receber métricas.");
            }
        }
    }
}
