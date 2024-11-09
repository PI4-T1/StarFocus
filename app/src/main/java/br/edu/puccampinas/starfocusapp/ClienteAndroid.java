package br.edu.puccampinas.starfocusapp;

import java.io.*;
import java.net.Socket;

public class ClienteAndroid {

    private static final String SERVER_IP = "10.0.2.2"; // IP do servidor (não use "localhost" no Android)
    private static final int SERVER_PORT = 3000; // Porta do servidor

    public static void conectarAoServidor() {
        try {
            // Conecta-se ao servidor na porta especificada
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            // Fluxo de entrada e saída
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            // Envia uma mensagem para o servidor
            String mensagem = "Olá, servidor!";
            outputStream.writeObject(mensagem);
            outputStream.flush();

            // Recebe a resposta do servidor
            String resposta = (String) inputStream.readObject();
            System.out.println("Resposta do servidor: " + resposta);

            // Fecha as conexões
            inputStream.close();
            outputStream.close();
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
