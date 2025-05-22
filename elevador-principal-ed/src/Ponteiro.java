import java.io.Serializable;

public class Ponteiro implements Serializable {
    private Object elemento;
    private Ponteiro proximo;

    public Ponteiro(Object elemento) {
        this.elemento = elemento;
        this.proximo = null;
    }

    public Object getElemento() {
        return elemento;
    }

    public void setElemento(Object elemento) {
        this.elemento = elemento;
    }

    public Ponteiro getProximo() {
        return proximo;
    }

    public void setProximo(Ponteiro proximo) {
        this.proximo = proximo;
    }
}
