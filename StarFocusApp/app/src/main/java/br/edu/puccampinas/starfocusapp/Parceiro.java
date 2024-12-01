package br.edu.puccampinas.starfocusapp;

import java.io.*;
import java.net.*;

public class Parceiro {
    private final Socket conexao;
    private final DataInputStream receptor;
    private final DataOutputStream transmissor;

    // Construtor do Parceiro
    public Parceiro(Socket conexao, DataInputStream receptor, DataOutputStream transmissor) throws Exception {
        if (conexao == null)
            throw new Exception("Conexao ausente");

        if (receptor == null)
            throw new Exception("Receptor ausente");

        if (transmissor == null)
            throw new Exception("Transmissor ausente");

        this.conexao = conexao;
        this.receptor = receptor;
        this.transmissor = transmissor;
    }

    // Método para acessar o DataInputStream
    public DataInputStream getDataInputStream() {
        return receptor;
    }

    // Método para acessar o DataOutputStream
    public DataOutputStream getDataOutputStream() {
        return transmissor;
    }

}
