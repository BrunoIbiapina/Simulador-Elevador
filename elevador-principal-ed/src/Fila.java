import java.io.Serializable;

public class Fila implements Serializable {
    private Lista lista;

    public Fila() {
        this.lista = new Lista();
    }

    public void enfileirar(Object elemento) {
        lista.inserirFim(elemento);
    }

    public Object desenfileirar() {
        return lista.removerInicio();
    }

    public boolean estaVazia() {
        return lista.estaVazia();
    }

    public int getTamanho() {
        return lista.getTamanho();
    }
    
    public Lista getLista() {
        return lista;
    }
}
