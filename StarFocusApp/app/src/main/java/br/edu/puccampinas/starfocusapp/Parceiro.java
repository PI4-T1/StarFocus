package br.edu.puccampinas.starfocusapp;

import java.io.*;
import java.net.*;

/**
 * Definição da classe Parceiro, que representa um parceiro
 * de comunicação em uma rede, utilizando sockets.
 * @author Laís
 */
public class Parceiro {
    // Declaração dos objetos
    private final Socket conexao;
    private final DataInputStream receptor;
    private final DataOutputStream transmissor;

    // / Construtor da classe Parceiro, que inicializa os atributos com os parâmetros fornecidos.
    public Parceiro(Socket conexao, DataInputStream receptor, DataOutputStream transmissor) throws Exception {
        //Verifica se a conexão (Socket) não é nula. Caso seja, lança uma exceção.
        if (conexao == null)
            throw new Exception("Conexao ausente");

        // Verifica se o DataInputStream não é nulo. Caso seja, lança uma exceção.
        if (receptor == null)
            throw new Exception("Receptor ausente");

        // Verifica se o DataOutputStream não é nulo. Caso seja, lança uma exceção.
        if (transmissor == null)
            throw new Exception("Transmissor ausente");

        // Inicializa os atributos com os valores fornecidos.
        this.conexao = conexao;
        this.receptor = receptor;
        this.transmissor = transmissor;
    }

    // Metodo para acessar o DataInputStream
    public DataInputStream getDataInputStream() {
        return receptor;
    }

    // Metodo para acessar o DataOutputStream
    public DataOutputStream getDataOutputStream() {
        return transmissor;
    }

}
