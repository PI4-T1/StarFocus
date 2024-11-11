package br.edu.puccampinas.starfocusapp

data class MemoryCard(
    val identifier: Int,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)
