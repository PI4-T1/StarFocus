package br.edu.puccampinas.starfocusapp;

import java.net.*;
import java.io.*;

public class ClienteAndroid {
    private static final String HOST_PADRAO = "10.0.2.2";
    private static final int PORTA_PADRAO = 3000;

    private Parceiro servidor;

    // Construtor para inicializar a conexão com o servidor
    public ClienteAndroid() {
        try {
            Socket conexao = new Socket(HOST_PADRAO, PORTA_PADRAO);
            ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
            ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
            servidor = new Parceiro(conexao, receptor, transmissor);
            System.out.println("Conexão com o servidor estabelecida com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao conectar com o servidor: " + e.getMessage());
        }
    }

    // Método chamado para enviar o progresso das tarefas ao servidor
    public void sendProgress(int tarefasPlanejadas, int tarefasConcluidas) {
        try {
            BarraDeProgresso barraDeProgresso = new BarraDeProgresso(tarefasPlanejadas, tarefasConcluidas);
            servidor.receba(barraDeProgresso);  // Envia o pedido de progresso ao servidor
            System.out.println("Progresso enviado ao servidor.");
        } catch (Exception e) {
            System.err.println("Erro ao enviar progresso: " + e.getMessage());
        }
    }

    /*// Método para solicitar um relatório de progresso mensal para o usuário
    public void solicitarRelatorio(String idUsuario, int mes, int ano) {
        try {
            // Cria o pedido de relatório com os parâmetros fornecidos
            PedidoDeRelatorio pedidoRelatorio = new PedidoDeRelatorio(idUsuario, mes, ano);
            servidor.receba(pedidoRelatorio);  // Envia o pedido ao servidor

            Comunicado comunicado;
            do {
                comunicado = servidor.espie();  // Obtém o próximo comunicado
            } while (!(comunicado instanceof RelatorioProgresso)); // Continua até que um relatório de progresso seja recebido

            // Agora, o cast deve ser seguro, pois já verificamos se é uma instância de RelatorioProgresso
            if (comunicado instanceof RelatorioProgresso) {
                RelatorioProgresso relatorio = (RelatorioProgresso) comunicado;  // Faz o cast com segurança
                System.out.println("Relatório recebido:\n" + relatorio.gerarRelatorio());  // Exibe o relatório
            } else {
                System.err.println("O comunicado recebido não é do tipo RelatorioProgresso.");
            }

        } catch (Exception e) {
            System.err.println("Erro ao solicitar relatório: " + e.getMessage());
        }
    }*/
}
