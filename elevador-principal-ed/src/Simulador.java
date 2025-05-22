import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public class Simulador implements Serializable {
    private int minutoSimulado;
    private int velocidadeEmMs;
    private transient Timer timer;
    private boolean emExecucao;
    private Predio predio;
    private int proximoIdPessoa;
    private HeuristicaElevador heuristicaAtual;
    
    // Estatísticas
    private int pessoasGeradas;
    private int pessoasTransportadas;

    public Simulador(int andares, int elevadores, int velocidadeEmMs, int capacidadeElevador) {
        this.minutoSimulado = 0;
        this.velocidadeEmMs = velocidadeEmMs;
        this.predio = new Predio(andares, elevadores, capacidadeElevador);
        this.proximoIdPessoa = 1;
        this.pessoasGeradas = 0;
        this.pessoasTransportadas = 0;
        this.heuristicaAtual = HeuristicaElevador.SEM_HEURISTICA;
        this.predio.setHeuristicaAtual(heuristicaAtual);
    }

    public void iniciar() {
        if (emExecucao) return;
        emExecucao = true;
        iniciarTimer();
        System.out.println("Simulação iniciada.");
    }

    public void pausar() {
        if (timer != null) {
            timer.cancel();
            emExecucao = false;
            System.out.println("Simulação pausada.");
        }
    }

    public void continuar() {
        if (!emExecucao) {
            iniciarTimer();
            emExecucao = true;
            System.out.println("Simulação retomada.");
        }
    }

    public void encerrar() {
        if (timer != null) timer.cancel();
        emExecucao = false;
        System.out.println("Simulação encerrada.");
    }

    private void iniciarTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                predio.atualizar(minutoSimulado++);
            }
        }, 0, velocidadeEmMs);
    }

    public void gravar(String nomeArquivo) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(nomeArquivo))) {
            out.writeObject(this);
            System.out.println("Simulação gravada em: " + nomeArquivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Simulador carregar(String nomeArquivo) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(nomeArquivo))) {
            Simulador sim = (Simulador) in.readObject();
            return sim;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getMinutoSimulado() {
        return minutoSimulado;
    }

    public Predio getPredio() {
        return predio;
    }

    public int getProximoIdPessoa() {
        int id = proximoIdPessoa;
        proximoIdPessoa++;
        pessoasGeradas++;
        return id;
    }

    public void setVelocidadeEmMs(int velocidadeEmMs) {
        this.velocidadeEmMs = velocidadeEmMs;
        // Se a simulação estiver em execução, reinicie o timer com a nova velocidade
        if (emExecucao) {
            pausar();
            continuar();
        }
    }

    public int getVelocidadeEmMs() {
        return velocidadeEmMs;
    }
    
    public int getPessoasGeradas() {
        return pessoasGeradas;
    }
    
    public void incrementarPessoasGeradas(int quantidade) {
        pessoasGeradas += quantidade;
        predio.incrementarPessoasGeradas(quantidade);
    }
    
    // Métodos para estatísticas
    public double getTempoMedioEspera() {
        return predio.getTempoMedioEspera();
    }
    
    public double getTempoMedioViagem() {
        return predio.getTempoMedioViagem();
    }
    
    public double getTempoMedioTotal() {
        return predio.getTempoMedioTotal();
    }
    
    public int getPessoasTransportadas() {
        return predio.getPessoasTransportadas();
    }
    
    public HeuristicaElevador getHeuristicaAtual() {
        return heuristicaAtual;
    }
    
    public void setHeuristicaAtual(HeuristicaElevador heuristica) {
        this.heuristicaAtual = heuristica;
        this.predio.setHeuristicaAtual(heuristica);
    }
}
