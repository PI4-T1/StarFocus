package br.edu.puccampinas.starfocusapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private static final int SERVER_PORT = 3000; // Porta para o servidor ouvir
    private static ServerSocket serverSocket;
    private static volatile boolean servidorAtivo = true; // Variável para controlar se o servidor está ativo

    public static void main(String[] args) throws Exception {
        try {
            // Inicializa o ServerSocket
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Servidor iniciado, aguardando conexões...");

            // Inicia a thread de monitoramento do comando de desativação
            Thread monitoramento = new Thread(() -> {
                while (servidorAtivo) {
                    System.out.println("Digite 'desativar' para encerrar o servidor.");
                    String comando = Teclado.getUmString();
                    if ("desativar".equalsIgnoreCase(comando)) {
                        System.out.println("Comando de desativação recebido. Encerrando servidor...");
                        pararServidor();
                    }
                }
            });
            monitoramento.start();

            // Loop para aceitar conexões de clientes enquanto o servidor estiver ativo
            while (servidorAtivo) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());

                // Trata a conexão com o cliente em uma função separada
                tratarConexaoCliente(clienteSocket);
            }

        } catch (IOException e) {
            if (servidorAtivo) {
                e.printStackTrace();
            }
        }
    }

    private static void tratarConexaoCliente(Socket clienteSocket) throws Exception {
        try (
            DataInputStream inputStream = new DataInputStream(clienteSocket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(clienteSocket.getOutputStream())
        ) {
            // Ler o "comando" enviado pelo cliente para determinar o tipo de solicitação
            int comando = inputStream.readInt();

            if (comando == 1) {  // 1 = Progresso
                // Ler inteiros do cliente (progresso)
                int totalTarefas = inputStream.readInt();
                int tarefasConcluidas = inputStream.readInt();

                // Criar instância de BarraDeProgresso para calcular a porcentagem
                BarraDeProgresso barraDeProgresso = new BarraDeProgresso(totalTarefas, tarefasConcluidas);

                // Obter a porcentagem calculada pela BarraDeProgresso
                double porcentagem = barraDeProgresso.getPorcentagemConcluida();

                // Enviar a porcentagem de volta para o cliente
                outputStream.writeInt((int) porcentagem);  // Enviar como inteiro (porcentagem)
                
            } else if (comando == 2) {  // 2 = Métricas
                // Ler string de métricas
                String metricaString = inputStream.readUTF();

                // Criar instância de RelatorioProgresso para processar as métricas
                RelatorioProgresso relatorioProgresso = new RelatorioProgresso();

                // Calcular ou processar as métricas utilizando o método calcularMetricas
                String resultadoMetrica = relatorioProgresso.calcularMetricas(metricaString);

                // Enviar o resultado das métricas de volta para o cliente
                outputStream.writeUTF(resultadoMetrica);
                
            } else {
                System.out.println("Comando desconhecido recebido.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clienteSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para parar o servidor com segurança
    private static void pararServidor() {
        try {
            servidorAtivo = false; // Define o servidor como inativo
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Fecha o ServerSocket
                System.out.println("Servidor foi encerrado com sucesso.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
