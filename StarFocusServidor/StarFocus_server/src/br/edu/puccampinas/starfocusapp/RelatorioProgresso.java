package br.edu.puccampinas.starfocusapp;

import java.io.Serializable;
import java.util.List;

public class RelatorioProgresso implements Serializable {
    private final String idUsuario;
    private int totalTarefasMes;
    private int tarefasConcluidasMes;
    private int totalRecompensasMes;
    private int diasComTarefas;
    private int diaMaisProdutivo;
    private int tarefasDiaMaisProdutivo;

    public RelatorioProgresso(String idUsuario, List<List<Tarefa>> tarefasDoMes) {
        this.idUsuario = idUsuario;
        calcularMetricasMensais(tarefasDoMes);
    }

    private void calcularMetricasMensais(List<List<Tarefa>> tarefasDoMes) {
        this.totalTarefasMes = 0;
        this.tarefasConcluidasMes = 0;
        this.totalRecompensasMes = 0;
        this.diasComTarefas = 0;
        this.diaMaisProdutivo = -1;  // -1 indica que ainda não foi encontrado
        this.tarefasDiaMaisProdutivo = 0;

        // Itera sobre cada lista de tarefas diárias
        for (int i = 0; i < tarefasDoMes.size(); i++) {
            List<Tarefa> tarefasDoDia = tarefasDoMes.get(i);
            int totalTarefasDia = tarefasDoDia.size();
            int tarefasConcluidasDia = 0;

            for (Tarefa tarefa : tarefasDoDia) {
                if (tarefa.isConcluida()) {
                    tarefasConcluidasDia++;
                }
            }

            totalTarefasMes += totalTarefasDia;
            tarefasConcluidasMes += tarefasConcluidasDia;

            // Calcule recompensas para o dia com base na porcentagem concluída
            double porcentagemConcluidaDia = (double) tarefasConcluidasDia / totalTarefasDia;
            int recompensasDia = 0;

            if (porcentagemConcluidaDia >= 1.0) {
                recompensasDia = 3;
            } else if (porcentagemConcluidaDia >= 0.66) {
                recompensasDia = 2;
            } else if (porcentagemConcluidaDia >= 0.33) {
                recompensasDia = 1;
            }

            totalRecompensasMes += recompensasDia;

            // Verifica se o dia atual é o mais produtivo
            if (tarefasConcluidasDia > tarefasDiaMaisProdutivo) {
                tarefasDiaMaisProdutivo = tarefasConcluidasDia;
                diaMaisProdutivo = i + 1;  // armazena o dia (1-indexed)
            }
        }
    }

    public String gerarRelatorio() {
        return "Relatório Mensal do Usuário " + idUsuario + ":\n" +
                "Total de Tarefas no Mês: " + totalTarefasMes + "\n" +
                "Tarefas Concluídas: " + tarefasConcluidasMes + "\n" +
                "Tarefas Não Concluídas: " + (totalTarefasMes - tarefasConcluidasMes) + "\n" +
                "Recompensas Totais Obtidas no Mês: " + totalRecompensasMes + " de " + (diasComTarefas * 3) + " possíveis\n" +
                "Dia Mais Produtivo: Dia " + diaMaisProdutivo + " (com " + tarefasDiaMaisProdutivo + " tarefas concluídas)\n";
    }
}
