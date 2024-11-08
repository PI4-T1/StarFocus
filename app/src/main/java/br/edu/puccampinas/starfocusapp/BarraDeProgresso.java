package br.edu.puccampinas.starfocusapp;

import java.io.Serializable;

public class BarraDeProgresso extends Comunicado implements Serializable {
    private final int totalTarefas;
    private final int tarefasConcluidas;
    private final double porcentagemConcluida;

    public BarraDeProgresso(int totalTarefas, int tarefasConcluidas) {
        if (totalTarefas <= 0)
            throw new IllegalArgumentException("O número total de tarefas deve ser maior que zero.");
        if (tarefasConcluidas < 0 || tarefasConcluidas > totalTarefas)
            throw new IllegalArgumentException("Número de tarefas concluídas inválido.");

        this.totalTarefas = totalTarefas;
        this.tarefasConcluidas = tarefasConcluidas;
        this.porcentagemConcluida = calcularPorcentagem();
    }

    private double calcularPorcentagem() {
        return (double) tarefasConcluidas / totalTarefas * 100;
        //fazer if no codigo para saber se recebeu recompensa.
    }

    public int getTotalTarefas() {
        return totalTarefas;
    }

    public int getTarefasConcluidas() {
        return tarefasConcluidas;
    }

    public double getPorcentagemConcluida() {
        return porcentagemConcluida;
    }

    @Override
    public String toString() {
        return "Progresso: " + String.format("%.2f", porcentagemConcluida) + "% concluído (" +
                tarefasConcluidas + " de " + totalTarefas + " tarefas concluídas)";
    }
}
