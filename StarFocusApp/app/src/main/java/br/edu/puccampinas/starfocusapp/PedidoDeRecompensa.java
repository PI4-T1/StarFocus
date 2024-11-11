package br.edu.puccampinas.starfocusapp;

import androidx.annotation.NonNull;

public class PedidoDeRecompensa extends Comunicado {

    private final int totalTarefas;
    private final int tarefasConcluidas;

    public PedidoDeRecompensa(int totalTarefas, int tarefasConcluidas) {
        if (totalTarefas <= 0)
            throw new IllegalArgumentException("Número total de tarefas deve ser positivo.");
        if (tarefasConcluidas < 0 || tarefasConcluidas > totalTarefas)
            throw new IllegalArgumentException("Número de tarefas concluídas inválido.");

        this.totalTarefas = totalTarefas;
        this.tarefasConcluidas = tarefasConcluidas;
    }

    public int getTotalTarefas() {
        return this.totalTarefas;
    }

    public int getTarefasConcluidas() {
        return this.tarefasConcluidas;
    }

    @NonNull
    @Override
    public String toString() {
        return "Pedido de Recompensa - Total de Tarefas: " + totalTarefas +
                ", Tarefas Concluídas: " + tarefasConcluidas;
    }
}
