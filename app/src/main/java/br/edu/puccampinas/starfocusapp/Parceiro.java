package br.edu.puccampinas.starfocusapp;

import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;
import android.util.Log;  // Para adicionar logs de depuração

public class Parceiro
{
    private final Socket             conexao;
    private final ObjectInputStream  receptor;
    private final ObjectOutputStream transmissor;

    private Comunicado proximoComunicado = null;

    private final Semaphore mutEx = new Semaphore(1, true);

    // Construtor do Parceiro
    public Parceiro(Socket conexao, ObjectInputStream receptor, ObjectOutputStream transmissor)
            throws Exception {
        if (conexao == null)
            throw new Exception("Conexao ausente");

        if (receptor == null)
            throw new Exception("Receptor ausente");

        if (transmissor == null)
            throw new Exception("Transmissor ausente");

        this.conexao = conexao;
        this.receptor = receptor;
        this.transmissor = transmissor;

        Log.d("Parceiro", "Parceiro instanciado com sucesso");
    }

    // Método para enviar um comunicado
    public void receba(Comunicado x) throws Exception {
        if (x == null) {
            Log.e("Erro", "Comunicado nulo não pode ser enviado");
            throw new Exception("Comunicado não pode ser nulo");
        }

        try {
            this.transmissor.writeObject(x);
            this.transmissor.flush();
            Log.d("Parceiro", "Comunicado enviado: " + x);
        } catch (IOException erro) {
            Log.e("Erro de transmissão", erro.getMessage(), erro);
            throw new Exception("Erro de transmissão");
        }
    }

    // Método para espiar o próximo comunicado
    public Comunicado espie() throws Exception {
        try {
            this.mutEx.acquireUninterruptibly();
            if (this.proximoComunicado == null)
                this.proximoComunicado = (Comunicado) this.receptor.readObject();
            this.mutEx.release();

            Log.d("Parceiro", "Comunicado espiado: " + this.proximoComunicado);
            return this.proximoComunicado;
        } catch (Exception erro) {
            Log.e("Erro de recepção", erro.getMessage(), erro);
            throw new Exception("Erro de recepção");
        }
    }

    // Método para enviar o próximo comunicado
    public Comunicado envie() throws Exception {
        try {
            if (this.proximoComunicado == null)
                this.proximoComunicado = (Comunicado) this.receptor.readObject();

            Comunicado ret = this.proximoComunicado;
            this.proximoComunicado = null;

            Log.d("Parceiro", "Comunicado enviado: " + ret);
            return ret;
        } catch (Exception erro) {
            Log.e("Erro de recepção", erro.getMessage(), erro);
            throw new Exception("Erro de recepção");
        }
    }

    // Método para fechar a conexão e os fluxos
    public void adeus() throws Exception {
        try {
            this.transmissor.close();
            this.receptor.close();
            this.conexao.close();

            Log.d("Parceiro", "Conexão encerrada com sucesso");
        } catch (Exception erro) {
            Log.e("Erro de desconexão", erro.getMessage(), erro);
            throw new Exception("Erro de desconexão");
        }
    }
}
