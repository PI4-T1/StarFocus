package br.edu.puccampinas.starfocusapp;

public class Tarefa {
    private String nome;
    private boolean concluida;

    public Tarefa(String nome, boolean concluida) {
        this.nome = nome;
        this.concluida = concluida;
    }

    public String getNome() {
        return nome;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }
}
