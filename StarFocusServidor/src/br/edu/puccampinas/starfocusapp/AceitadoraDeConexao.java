package br.edu.puccampinas.starfocusapp;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class AceitadoraDeConexao {

    private int serverPort; // Porta do servidor
    private ServerSocket serverSocket; // O servidor que ficará ouvindo a porta
    private ExecutorService executorService; // Executor para gerenciar múltiplas conexões

    // Construtor que recebe a porta do servidor
    public AceitadoraDeConexao(int serverPort) {
        this.serverPort = serverPort;
    }

    // Método para iniciar a aceitação de conexões
    public void iniciarAceitacaoConexoes() throws IOException {
        // Inicializa o ExecutorService com um número fixo de threads (por exemplo, 10)
        executorService = Executors.newFixedThreadPool(10);

        // Cria o ServerSocket para ouvir a porta fornecida
        serverSocket = new ServerSocket(serverPort);
        System.out.println("Servidor aceitando conexões na porta " + serverPort);

        while (true) {
            // Aceita uma nova conexão do cliente
            Socket clienteSocket = serverSocket.accept();
            System.out.println("Novo cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());

            // Submete uma nova tarefa para a ThreadPool para lidar com a conexão
            executorService.submit(new GerenciadorDeConexao(clienteSocket));
        }
    }

    // Classe interna para gerenciar a conexão com o cliente
    private static class GerenciadorDeConexao implements Runnable {

        private Socket clienteSocket;

        public GerenciadorDeConexao(Socket clienteSocket) {
            this.clienteSocket = clienteSocket;
        }

        @Override
        public void run() {
            try {
                // Cria fluxos de entrada e saída para se comunicar com o cliente
                ObjectInputStream inputStream = new ObjectInputStream(clienteSocket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(clienteSocket.getOutputStream());

                // Recebe a mensagem do cliente
                String mensagemRecebida = (String) inputStream.readObject();
                System.out.println("Mensagem recebida do cliente: " + mensagemRecebida);

                // Envia uma resposta ao cliente
                String resposta = "Olá, cliente!";
                outputStream.writeObject(resposta);
                outputStream.flush();

                // Fecha os fluxos e a conexão com o cliente
                inputStream.close();
                outputStream.close();
                clienteSocket.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
