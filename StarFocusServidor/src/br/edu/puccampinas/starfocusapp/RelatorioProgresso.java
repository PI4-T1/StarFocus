package br.edu.puccampinas.starfocusapp;

public class RelatorioProgresso {
	
	// Variáveis para armazenar as métricas
    private int tarefasPendentes;
    private int tarefasConcluidas;
    private int tarefasEnviadas;
    private int totalTarefas;
    
    // Variáveis para armazenar os cálculos de porcentagem
    private double porcentagemPendentes;
    private double porcentagemConcluidas;
    private double porcentagemEnviadas;
    
    // Getters para acessar as variáveis, se necessário
    public int getTarefasPendentes() {
        return tarefasPendentes;
    }

    public int getTarefasConcluidas() {
        return tarefasConcluidas;
    }

    public int getTarefasEnviadas() {
        return tarefasEnviadas;
    }

    public int getTotalTarefas() {
        return totalTarefas;
    }
    
    // Método que processa as métricas
    public String calcularMetricas(String metricaString) {
    	
        // Verifica se a string tem pelo menos 12 caracteres
        if (metricaString == null || metricaString.length() != 12) {
            throw new IllegalArgumentException("A string de métricas deve ter 12 caracteres.");
        }

        try {
            // Extrair e converter as partes da string para inteiros
            tarefasPendentes = Integer.parseInt(metricaString.substring(0, 3));
            tarefasConcluidas = Integer.parseInt(metricaString.substring(3, 6));
            tarefasEnviadas = Integer.parseInt(metricaString.substring(6, 9));
            totalTarefas = Integer.parseInt(metricaString.substring(9, 12));

            // Calcular as porcentagens
            porcentagemPendentes = ((double) tarefasPendentes / totalTarefas) * 100;
            porcentagemConcluidas = ((double) tarefasConcluidas / totalTarefas) * 100;
            porcentagemEnviadas = ((double) tarefasEnviadas / totalTarefas) * 100;

            // Convertendo as porcentagens para inteiros e formatando com três dígitos
            String pendentesStr = String.format("%03d", (int) porcentagemPendentes);
            String concluidasStr = String.format("%03d", (int) porcentagemConcluidas);
            String enviadasStr = String.format("%03d", (int) porcentagemEnviadas);

            // Montando a string final de 9 dígitos
            String resultado = pendentesStr + concluidasStr + enviadasStr;

            // Exibir a string para depuração
            System.out.println("String de métricas calculada: " + resultado);

            return resultado;
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("A string de métricas deve conter apenas números.", e);
        }
    }
}
