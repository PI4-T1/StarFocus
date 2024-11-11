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
            // Receber os inteiros enviados pelo cliente
            int totalTarefas = inputStream.readInt();
            int tarefasConcluidas = inputStream.readInt();

            // Calcular a porcentagem
            double porcentagem = (double) tarefasConcluidas / totalTarefas * 100;

            // Enviar a porcentagem de volta para o cliente
            outputStream.writeInt((int) porcentagem);

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
