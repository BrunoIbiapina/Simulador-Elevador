import java.io.Serializable;

public class Lista implements Serializable {
    private Ponteiro inicio;
    private Ponteiro fim;
    private int tamanho;

    public Lista() {
        this.inicio = null;
        this.fim = null;
        this.tamanho = 0;
    }

    public void inserirInicio(Object elemento) {
        Ponteiro novoPonteiro = new Ponteiro(elemento);
        if (inicio == null) {
            inicio = novoPonteiro;
            fim = novoPonteiro;
        } else {
            novoPonteiro.setProximo(inicio);
            inicio = novoPonteiro;
        }
        tamanho++;
    }

    public void inserirFim(Object elemento) {
        Ponteiro novoPonteiro = new Ponteiro(elemento);
        if (fim == null) {
            inicio = novoPonteiro;
            fim = novoPonteiro;
        } else {
            fim.setProximo(novoPonteiro);
            fim = novoPonteiro;
        }
        tamanho++;
    }

    public Object removerInicio() {
        if (inicio == null) {
            return null;
        }
        
        Object elemento = inicio.getElemento();
        inicio = inicio.getProximo();
        
        if (inicio == null) {
            fim = null;
        }
        
        tamanho--;
        return elemento;
    }

    public boolean estaVazia() {
        return inicio == null;
    }

    public int getTamanho() {
        return tamanho;
    }

    public Ponteiro getInicio() {
        return inicio;
    }

    public Ponteiro getFim() {
        return fim;
    }
}
