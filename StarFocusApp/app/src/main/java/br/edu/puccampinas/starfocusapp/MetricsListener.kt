package br.edu.puccampinas.starfocusapp



/**
 * Função a ser implementada para receber atualizações de métricas.
 * A função recebe uma string que contém as métricas atualizadas.
 * @author Laís
 */
interface MetricsListener {
    fun onMetricsUpdate(metrics: String)
}
