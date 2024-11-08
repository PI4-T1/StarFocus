package br.edu.puccampinas.starfocusapp;

import java.net.*;
import java.io.*;

public class ClienteAndroid {
    private static final String HOST_PADRAO = "localhost";
    private static final int PORTA_PADRAO = 3000;

    private Parceiro servidor;

    // Construtor para inicializar a conexão com o servidor
    public ClienteAndroid() {
        try {
            Socket conexao = new Socket(HOST_PADRAO, PORTA_PADRAO);
            ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
            ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
            servidor = new Parceiro(conexao, receptor, transmissor);
        } catch (Exception e) {
            System.err.println("Erro ao conectar com o servidor: " + e.getMessage());
        }
    }

    // Método chamado quando o botão 'sendProgress' é clicado
    public void sendProgress(int tarefasPlanejadas, int tarefasConcluidas) {
        try {
            PedidoDeProgresso pedidoDeProgresso = new PedidoDeProgresso(tarefasPlanejadas, tarefasConcluidas);
            servidor.receba(pedidoDeProgresso);
            System.out.println("Progresso enviado ao servidor.");
        } catch (Exception e) {
            System.err.println("Erro ao enviar progresso: " + e.getMessage());
        }
    }

    // Método chamado para solicitar relatório de progresso
    public void solicitarRelatorio(String tipo) {
        try {
            PedidoDeRelatorio pedidoRelatorio = new PedidoDeRelatorio(tipo); // "diario" ou "mensal"
            servidor.receba(pedidoRelatorio);

            Comunicado comunicado;
            do {
                comunicado = (Comunicado) servidor.espie();
            } while (!(comunicado instanceof RelatorioProgresso));

            RelatorioProgresso relatorio = (RelatorioProgresso) servidor.envie();
            System.out.println("Relatório recebido:\n" + relatorio);
            // Aqui você pode atualizar a interface gráfica com o relatório recebido.
        } catch (Exception e) {
            System.err.println("Erro ao solicitar relatório: " + e.getMessage());
        }
    }
}
