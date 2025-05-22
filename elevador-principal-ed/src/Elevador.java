import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Elevador extends EntidadeSimulavel implements Serializable {
    private int id;
    private int andarAtual;
    private int andarDestino;
    private boolean emMovimento;
    private boolean subindo;
    private Lista pessoasDentro;
    private int capacidadeMaxima;
    private List<Integer> andaresParaAtender;
    private int tempoParado;
    private static final int TEMPO_PARADO_MAXIMO = 3; // Tempo que o elevador fica parado em um andar
    
    // Estatísticas
    private int pessoasTransportadas;
    private int andaresPercorridos;
    private int tempoTotalViagem;
    private int tempoOcioso;

    public Elevador(int id, int capacidadeMaxima) {
        this.id = id;
        this.andarAtual = 0;
        this.andarDestino = 0;
        this.emMovimento = false;
        this.subindo = true;
        this.pessoasDentro = new Lista();
        this.capacidadeMaxima = capacidadeMaxima;
        this.andaresParaAtender = new ArrayList<>();
        this.tempoParado = 0;
        
        // Inicializar estatísticas
        this.pessoasTransportadas = 0;
        this.andaresPercorridos = 0;
        this.tempoTotalViagem = 0;
        this.tempoOcioso = 0;
    }

    @Override
    public void atualizar(int minutoSimulado) {
        if (emMovimento) {
            // Mover o elevador
            if (andarAtual < andarDestino) {
                andarAtual++;
                subindo = true;
                andaresPercorridos++;
            } else if (andarAtual > andarDestino) {
                andarAtual--;
                subindo = false;
                andaresPercorridos++;
            } else {
                emMovimento = false;
                tempoParado = 0;
                // Chegou ao destino, desembarcar pessoas
                desembarcarPessoas();
            }
            
            // Verificar se há paradas pelo meio
            if (andaresParaAtender.contains(andarAtual)) {
                emMovimento = false;
                tempoParado = 0;
                andaresParaAtender.remove(Integer.valueOf(andarAtual));
                desembarcarPessoas();
            }
            
            // Atualizar tempo de viagem para pessoas dentro do elevador
            atualizarTempoViagem();
        } else {
            // Elevador está parado em um andar
            tempoParado++;
            
            // Se ficou parado por tempo suficiente, pode continuar a viagem
            if (tempoParado >= TEMPO_PARADO_MAXIMO) {
                // Se ainda tiver andares para atender, continue a viagem
                if (!andaresParaAtender.isEmpty()) {
                    // Determinar próximo andar a atender baseado na direção atual
                    determinarProximoAndar();
                } else {
                    // Se não tiver mais andares para atender, está ocioso
                    tempoOcioso++;
                }
            }
        }
        
        System.out.println("Elevador " + id + " no andar " + andarAtual + 
                          (emMovimento ? (subindo ? " subindo" : " descendo") : " parado") + 
                          " - Minuto " + minutoSimulado);
    }
    
    private void determinarProximoAndar() {
        if (andaresParaAtender.isEmpty()) return;
        
        // Implementação da heurística 
        int proximoAndar = -1;
        
        if (subindo) {
            // Procurar o próximo andar acima
            int menorDistanciaAcima = Integer.MAX_VALUE;
            for (int andar : andaresParaAtender) {
                if (andar > andarAtual && andar - andarAtual < menorDistanciaAcima) {
                    menorDistanciaAcima = andar - andarAtual;
                    proximoAndar = andar;
                }
            }
            
            // Se não encontrou nenhum andar acima, inverte a direção
            if (proximoAndar == -1) {
                subindo = false;
                determinarProximoAndar();
                return;
            }
        } else {
            // Procurar o próximo andar abaixo
            int menorDistanciaAbaixo = Integer.MAX_VALUE;
            for (int andar : andaresParaAtender) {
                if (andar < andarAtual && andarAtual - andar < menorDistanciaAbaixo) {
                    menorDistanciaAbaixo = andarAtual - andar;
                    proximoAndar = andar;
                }
            }
            
            // Se não encontrou nenhum andar abaixo, inverte a direção
            if (proximoAndar == -1) {
                subindo = true;
                determinarProximoAndar();
                return;
            }
        }
        
        // Definir o próximo destino
        if (proximoAndar != -1) {
            andarDestino = proximoAndar;
            emMovimento = true;
        }
    }

    public void chamarPara(int andar) {
        if (andarAtual != andar && !andaresParaAtender.contains(andar)) {
            andaresParaAtender.add(andar);
            
            // Se o elevador estiver parado, definir o destino imediatamente
            if (!emMovimento && tempoParado >= TEMPO_PARADO_MAXIMO) {
                determinarProximoAndar();
            }
        }
    }

    public void embarcarPessoa(Pessoa pessoa) {
        if (pessoasDentro.getTamanho() < capacidadeMaxima) {
            pessoasDentro.inserirFim(pessoa);
            pessoa.entrarElevador();
            pessoa.setTempoEspera(pessoa.getTempoEspera()); // Registrar tempo de espera
            
            // Adicionar o destino da pessoa aos andares para atender
            if (!andaresParaAtender.contains(pessoa.getAndarDestino())) {
                andaresParaAtender.add(pessoa.getAndarDestino());
            }
            
            // Se o elevador estiver parado, definir o destino imediatamente
            if (!emMovimento && tempoParado >= TEMPO_PARADO_MAXIMO) {
                determinarProximoAndar();
            }
        }
    }

    private void desembarcarPessoas() {
        Ponteiro p = pessoasDentro.getInicio();
        Ponteiro anterior = null;
        
        while (p != null) {
            Pessoa pessoa = (Pessoa) p.getElemento();
            
            if (pessoa.getAndarDestino() == andarAtual) {
                // Remover a pessoa da lista
                if (anterior == null) {
                    pessoasDentro.removerInicio();
                    p = pessoasDentro.getInicio();
                } else {
                    anterior.setProximo(p.getProximo());
                    p = p.getProximo();
                }
                
                pessoa.sairElevador();
                pessoasTransportadas++;
                tempoTotalViagem += pessoa.getTempoViagem();
            } else {
                anterior = p;
                p = p.getProximo();
            }
        }
    }
    
    private void atualizarTempoViagem() {
        Ponteiro p = pessoasDentro.getInicio();
        while (p != null) {
            Pessoa pessoa = (Pessoa) p.getElemento();
            pessoa.incrementarTempoViagem();
            p = p.getProximo();
        }
    }

    public int getId() {
        return id;
    }

    public int getAndarAtual() {
        return andarAtual;
    }

    public boolean estaEmMovimento() {
        return emMovimento;
    }

    public boolean estaSubindo() {
        return subindo;
    }

    public Lista getPessoasDentro() {
        return pessoasDentro;
    }

    public boolean temEspacoDisponivel() {
        return pessoasDentro.getTamanho() < capacidadeMaxima;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
    
    public List<Integer> getAndaresParaAtender() {
        return andaresParaAtender;
    }
    
    // Métodos para estatísticas
    public int getPessoasTransportadas() {
        return pessoasTransportadas;
    }
    
    public int getAndaresPercorridos() {
        return andaresPercorridos;
    }
    
    public int getTempoTotalViagem() {
        return tempoTotalViagem;
    }
    
    public double getTempoMedioViagem() {
        return pessoasTransportadas > 0 ? (double) tempoTotalViagem / pessoasTransportadas : 0;
    }
    
    public int getTempoOcioso() {
        return tempoOcioso;
    }
    
    public void resetarEstatisticas() {
        pessoasTransportadas = 0;
        andaresPercorridos = 0;
        tempoTotalViagem = 0;
        tempoOcioso = 0;
    }
}
