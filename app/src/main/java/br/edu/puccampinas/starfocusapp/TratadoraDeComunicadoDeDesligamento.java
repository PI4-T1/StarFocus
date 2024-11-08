package br.edu.puccampinas.starfocusapp;

public class TratadoraDeComunicadoDeDesligamento extends Thread
{
    private final Parceiro servidor;

    public TratadoraDeComunicadoDeDesligamento (Parceiro servidor) throws Exception
    {
        if (servidor==null)
            throw new Exception ("Porta invalida");

        this.servidor = servidor;
    }

    public void run ()
    {
        for(;;)
        {
            try
            {
                if (this.servidor.espie() instanceof ComunicadoDeDesligamento)
                {
                    System.out.println ("\nO servidor vai ser desligado agora;");
                    System.err.println ("volte mais tarde!\n");
                    System.exit(0);
                }
            }
            catch (Exception erro)
            {}
        }
    }
}
