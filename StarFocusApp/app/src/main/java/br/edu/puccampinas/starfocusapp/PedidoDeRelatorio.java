package br.edu.puccampinas.starfocusapp;

public class PedidoDeRelatorio extends Comunicado {
    private final String idUsuario;
    private final int mes;
    private final int ano;

    public PedidoDeRelatorio(String idUsuario, int mes, int ano) {
        if (idUsuario == null || idUsuario.isEmpty())
            throw new IllegalArgumentException("ID de usuário inválido.");
        if (mes < 1 || mes > 12)
            throw new IllegalArgumentException("Mês inválido.");
        if (ano < 2000)
            throw new IllegalArgumentException("Ano inválido.");

        this.idUsuario = idUsuario;
        this.mes = mes;
        this.ano = ano;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public int getMes() {
        return mes;
    }

    public int getAno() {
        return ano;
    }
}

