import java.io.Serializable;

public class Pessoa implements Serializable {
    private int id;
    private int andarOrigem;
    private int andarDestino;
    private boolean dentroElevador;
    private int tempoEspera;
    private int tempoViagem;
    private int tempoTotal;
    private int minutoChegada;

    public Pessoa(int id, int origem, int destino, int minutoAtual) {
        this.id = id;
        this.andarOrigem = origem;
        this.andarDestino = destino;
        this.dentroElevador = false;
        this.tempoEspera = 0;
        this.tempoViagem = 0;
        this.tempoTotal = 0;
        this.minutoChegada = minutoAtual;
    }

    public int getId() {
        return id;
    }

    public int getAndarOrigem() {
        return andarOrigem;
    }

    public int getAndarDestino() {
        return andarDestino;
    }

    public boolean estaDentroDoElevador() {
        return dentroElevador;
    }

    public void entrarElevador() {
        this.dentroElevador = true;
    }

    public void sairElevador() {
        this.dentroElevador = false;
        this.tempoTotal = this.tempoEspera + this.tempoViagem;
    }
    
    public void incrementarTempoEspera() {
        if (!dentroElevador) {
            tempoEspera++;
        }
    }
    
    public void incrementarTempoViagem() {
        if (dentroElevador) {
            tempoViagem++;
        }
    }
    
    public int getTempoEspera() {
        return tempoEspera;
    }
    
    public void setTempoEspera(int tempoEspera) {
        this.tempoEspera = tempoEspera;
    }
    
    public int getTempoViagem() {
        return tempoViagem;
    }
    
    public int getTempoTotal() {
        return tempoTotal;
    }
    
    public int getMinutoChegada() {
        return minutoChegada;
    }
}
