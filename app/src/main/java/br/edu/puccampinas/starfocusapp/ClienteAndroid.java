package br.edu.puccampinas.starfocusapp;

import java.io.*;
import java.net.Socket;
import kotlinx.coroutines.*;

public class ClienteAndroid {

    private static final String SERVER_IP = "10.0.2.2"; // IP do servidor (não use "localhost" no Android)
    private static final int SERVER_PORT = 3000; // Porta do servidor

    public static void conectarAoServidor() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Estabelece a conexão com o servidor
                val socket = Socket("IP_DO_SERVIDOR", 3000)
                val outputStream = ObjectOutputStream(socket.getOutputStream())
                val inputStream = ObjectInputStream(socket.getInputStream())

                // Envia o comunicado ao servidor
                val comunicado = Comunicado("Solicitar barra de progresso")
                outputStream.writeObject(comunicado)
                outputStream.flush()

                // Espera pela resposta com a BarraDeProgresso
                val progresso = inputStream.readObject() as BarraDeProgresso

                // Exibe a barra de progresso recebida
                withContext(Dispatchers.Main) {
                    println("Progresso recebido: ${progresso.toString()}")
                }

                // Fecha os streams e o socket
                inputStream.close()
                outputStream.close()
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    println("Erro: ${e.message}")
                }
            }
        }
}
