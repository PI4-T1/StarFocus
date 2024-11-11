package br.edu.puccampinas.starfocusapp;

import java.io.Serializable;

public class RespostaProgresso extends Comunicado implements Serializable {
    private final int porcentagemConcluida;

    public RespostaProgresso(double porcentagemConcluida) {
        this.porcentagemConcluida = (int) Math.round(porcentagemConcluida); // Arredonda para int
    }

    public int getPorcentagemConcluida() {
        return porcentagemConcluida;
    }

    @Override
    public String toString() {
        return "Progresso atualizado: " + porcentagemConcluida + "%";
    }
}
