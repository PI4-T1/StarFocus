package br.edu.puccampinas.starfocusapp
/**
 * A data class MemoryCard representa uma carta no jogo da memória.
 * @author Luiz
 */
data class MemoryCard(
    // Identificador único para a carta, geralmente uma referência a um recurso de imagem.
    val identifier: Int,
    // Booleano que indica se a carta está virada para cima (verdadeira) ou para baixo (falsa).
    var isFaceUp: Boolean = false,
    // Booleano que indica se a carta foi combinada corretamente com outra carta.
    var isMatched: Boolean = false
)
