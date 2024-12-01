package br.edu.puccampinas.starfocusapp
/**
 * A interface ProgressListener é usada para notificar a atualização do progresso em uma tarefa ou ação.
 *  * Essa interface define um metodo chamado
 *  onProgressUpdate que é invocado quando há uma atualização no progresso.
 * @author Lais
 */
interface ProgressListener {

    /**
     * Este metodo é chamado para notificar uma atualização no progresso de uma tarefa ou ação.
     * @param progresso O valor do progresso (geralmente um número entre 0 e 100).
     */
    fun onProgressUpdate(progresso: Int)
}
