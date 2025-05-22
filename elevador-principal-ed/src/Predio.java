import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
//import java.util.Comparator;
import java.util.List;
/* */
public class Predio extends EntidadeSimulavel implements Serializable {
    private CentralDeControle central;
    private Lista andares;
    private HeuristicaElevador heuristicaAtual;
    private List<Integer> chamadas; // Lista de chamadas pendentes para o modelo FCFS
    
    // Parâmetros para o modelo de otimização de energia
    private boolean horarioPico;
    private int inicioHorarioPico = 420; // 7:00 (em minutos)
    private int fimHorarioPico = 600; // 10:00 (em minutos)
    private int inicioHorarioPicoTarde = 960; // 16:00 (em minutos)
    private int fimHorarioPicoTarde = 1140; // 19:00 (em minutos)
    
    // Estatísticas
    private int pessoasGeradas;
    private int pessoasTransportadas;
    private int tempoTotalEspera;
    private int tempoTotalViagem;
    private int tempoTotalTransporte;
    private List<Integer> temposEspera;
    private List<Integer> temposViagem;
    private List<Integer> temposTotal;

    public Predio(int quantidadeAndares, int quantidadeElevadores, int capacidadeElevador) {
        central = new CentralDeControle(quantidadeElevadores, capacidadeElevador);
        andares = new Lista();
        for (int i = 0; i < quantidadeAndares; i++) {
            andares.inserirFim(new Andar(i));
        }
        
        // Inicializar com a heurística padrão
        heuristicaAtual = HeuristicaElevador.SEM_HEURISTICA;
        chamadas = new ArrayList<>();
        
        // Inicializar estatísticas
        pessoasGeradas = 0;
        pessoasTransportadas = 0;
        tempoTotalEspera = 0;
        tempoTotalViagem = 0;
        tempoTotalTransporte = 0;
        temposEspera = new ArrayList<>();
        temposViagem = new ArrayList<>();
        temposTotal = new ArrayList<>();
    }

    @Override
    public void atualizar(int minutoSimulado) {
        // Verificar se é horário de pico (para o modelo de otimização de energia)
        atualizarHorarioPico(minutoSimulado);
        
        // Atualizar a central de controle
        central.atualizar(minutoSimulado);
        
        // Atualizar o tempo de espera das pessoas em cada andar
        atualizarTempoEsperaPessoas();
        
        // Verificar se há pessoas aguardando em cada andar
        Ponteiro pAndar = andares.getInicio();
        while (pAndar != null) {
            Andar andar = (Andar) pAndar.getElemento();
            verificarChamadasElevador(andar, minutoSimulado);
            pAndar = pAndar.getProximo();
        }
        
        // Atualizar estatísticas dos elevadores
        atualizarEstatisticasElevadores();
    }
    
    private void atualizarHorarioPico(int minutoSimulado) {
        // Converter minuto simulado para hora do dia (assumindo que começa à meia-noite)
        int minutosDoDia = minutoSimulado % 1440; // 1440 = 24 horas * 60 minutos
        
        // Verificar se é horário de pico (manhã ou tarde)
        horarioPico = (minutosDoDia >= inicioHorarioPico && minutosDoDia <= fimHorarioPico) ||
                      (minutosDoDia >= inicioHorarioPicoTarde && minutosDoDia <= fimHorarioPicoTarde);
    }
    
    private void atualizarTempoEsperaPessoas() {
        Ponteiro pAndar = andares.getInicio();
        while (pAndar != null) {
            Andar andar = (Andar) pAndar.getElemento();
            Fila pessoasAguardando = andar.getPessoasAguardando();
            
            if (!pessoasAguardando.estaVazia()) {
                Ponteiro pPessoa = pessoasAguardando.getLista().getInicio();
                while (pPessoa != null) {
                    Pessoa pessoa = (Pessoa) pPessoa.getElemento();
                    pessoa.incrementarTempoEspera();
                    pPessoa = pPessoa.getProximo();
                }
            }
            
            pAndar = pAndar.getProximo();
        }
    }

    private void verificarChamadasElevador(Andar andar, int minutoSimulado) {
        PainelElevador painel = andar.getPainel();
        
        // Se houver botões ativados, notificar a central de controle
        if (painel.isBotaoSubirAtivado() || painel.isBotaoDescerAtivado()) {
            // Verificar se há elevadores no andar para embarcar pessoas
            embarcarPessoasEmElevadoresNoAndar(andar, minutoSimulado);
            
            // Se ainda houver pessoas aguardando, chamar elevador
            if (!andar.getPessoasAguardando().estaVazia()) {
                // Adicionar chamada à lista de chamadas pendentes (para o modelo FCFS)
                if (heuristicaAtual == HeuristicaElevador.SEM_HEURISTICA && !chamadas.contains(andar.getNumero())) {
                    chamadas.add(andar.getNumero());
                }
                
                // Chamar elevador de acordo com a heurística selecionada
                switch (heuristicaAtual) {
                    case SEM_HEURISTICA:
                        chamarElevadorFCFS();
                        break;
                    case OTIMIZACAO_TEMPO_ESPERA:
                        chamarElevadorOtimizacaoTempoEspera(andar, painel.isBotaoSubirAtivado(), painel.isBotaoDescerAtivado());
                        break;
                    case OTIMIZACAO_ENERGIA:
                        chamarElevadorOtimizacaoEnergia(andar, painel.isBotaoSubirAtivado(), painel.isBotaoDescerAtivado(), minutoSimulado);
                        break;
                }
            } else {
                // Se não houver mais pessoas aguardando, resetar os botões
                painel.resetar();
                // Remover da lista de chamadas pendentes
                chamadas.remove(Integer.valueOf(andar.getNumero()));
            }
        }
    }
    
    private void embarcarPessoasEmElevadoresNoAndar(Andar andar, int minutoSimulado) {
        Lista elevadores = central.getElevadores();
        Ponteiro pElevador = elevadores.getInicio();
        
        while (pElevador != null) {
            Elevador elevador = (Elevador) pElevador.getElemento();
            
            // Se o elevador estiver neste andar e não estiver em movimento
            if (elevador.getAndarAtual() == andar.getNumero() && !elevador.estaEmMovimento()) {
                // Embarcar pessoas que vão na mesma direção do elevador
                embarcarPessoasNoElevador(andar, elevador, minutoSimulado);
            }
            
            pElevador = pElevador.getProximo();
        }
    }
    
    private void embarcarPessoasNoElevador(Andar andar, Elevador elevador, int minutoSimulado) {
        Fila pessoasAguardando = andar.getPessoasAguardando();
        PainelElevador painel = andar.getPainel();
        
        if (pessoasAguardando.estaVazia()) return;
        
        // Criar uma lista temporária para as pessoas que não embarcarem
        Lista pessoasRestantes = new Lista();
        
        while (!pessoasAguardando.estaVazia() && elevador.temEspacoDisponivel()) {
            Pessoa pessoa = (Pessoa) pessoasAguardando.desenfileirar();
            
            // Verificar se a pessoa vai na mesma direção do elevador ou se o elevador está parado
            boolean mesmaDir = (pessoa.getAndarDestino() > andar.getNumero() && elevador.estaSubindo()) ||
                              (pessoa.getAndarDestino() < andar.getNumero() && !elevador.estaSubindo());
            
            if (mesmaDir || !elevador.estaEmMovimento()) {
                // Embarcar a pessoa
                elevador.embarcarPessoa(pessoa);
                
                // Atualizar estatísticas
                tempoTotalEspera += pessoa.getTempoEspera();
                temposEspera.add(pessoa.getTempoEspera());
            } else {
                // A pessoa não vai na mesma direção, colocar de volta na fila
                pessoasRestantes.inserirFim(pessoa);
            }
        }
        
        // Colocar as pessoas restantes de volta na fila
        Ponteiro p = pessoasRestantes.getInicio();
        while (p != null) {
            pessoasAguardando.enfileirar(p.getElemento());
            p = p.getProximo();
        }
        
        // Atualizar os botões do painel
        atualizarBotoesPainel(andar);
    }
    
    private void atualizarBotoesPainel(Andar andar) {
        PainelElevador painel = andar.getPainel();
        painel.resetar();
        
        // Verificar se ainda há pessoas aguardando e atualizar os botões
        Fila pessoasAguardando = andar.getPessoasAguardando();
        if (!pessoasAguardando.estaVazia()) {
            Ponteiro p = pessoasAguardando.getLista().getInicio();
            while (p != null) {
                Pessoa pessoa = (Pessoa) p.getElemento();
                if (pessoa.getAndarDestino() > andar.getNumero()) {
                    painel.pressionarSubir();
                } else if (pessoa.getAndarDestino() < andar.getNumero()) {
                    painel.pressionarDescer();
                }
                p = p.getProximo();
            }
        }
    }

    // Modelo 1: Sem heurística (FCFS)
    private void chamarElevadorFCFS() {
        if (chamadas.isEmpty()) return;
        
        // Pegar o primeiro andar da lista de chamadas
        int andarChamada = chamadas.get(0);
        
        // Encontrar o primeiro elevador disponível
        Lista elevadores = central.getElevadores();
        Ponteiro p = elevadores.getInicio();
        
        while (p != null) {
            Elevador elevador = (Elevador) p.getElemento();
            
            // Se o elevador não estiver em movimento ou já estiver indo para este andar, usá-lo
            if (!elevador.estaEmMovimento() || elevador.getAndaresParaAtender().contains(andarChamada)) {
                elevador.chamarPara(andarChamada);
                break;
            }
            
            p = p.getProximo();
        }
        
        // Se nenhum elevador estiver disponível, manter a chamada na fila
    }
    
    // Modelo 2: Otimização do tempo de espera
    private void chamarElevadorOtimizacaoTempoEspera(Andar andar, boolean subindo, boolean descendo) {
        Lista elevadores = central.getElevadores();
        
        // Implementação da heurística de otimização de tempo de espera
        Elevador melhorElevador = null;
        int menorTempoEstimado = Integer.MAX_VALUE;
        
        Ponteiro p = elevadores.getInicio();
        while (p != null) {
            Elevador elevador = (Elevador) p.getElemento();
            
            // Calcular o tempo estimado de chegada para este elevador
            int tempoEstimado = calcularTempoEstimadoChegada(elevador, andar, subindo, descendo);
            
            if (tempoEstimado < menorTempoEstimado) {
                menorTempoEstimado = tempoEstimado;
                melhorElevador = elevador;
            }
            
            p = p.getProximo();
        }
        
        // Chamar o elevador com menor tempo estimado
        if (melhorElevador != null) {
            melhorElevador.chamarPara(andar.getNumero());
        }
    }
    
    private int calcularTempoEstimadoChegada(Elevador elevador, Andar andar, boolean subindo, boolean descendo) {
        int tempoBase = Math.abs(elevador.getAndarAtual() - andar.getNumero());
        
        // Se o elevador estiver em movimento, considerar suas paradas intermediárias
        if (elevador.estaEmMovimento()) {
            // Adicionar tempo para cada parada intermediária
            for (int andarParada : elevador.getAndaresParaAtender()) {
                if ((elevador.getAndarAtual() < andarParada && andarParada < andar.getNumero()) ||
                    (elevador.getAndarAtual() > andarParada && andarParada > andar.getNumero())) {
                    tempoBase += 2; // Tempo para parar e embarcar/desembarcar
                }
            }
            
            // Se o elevador estiver indo na direção oposta, adicionar tempo para inverter
            if ((elevador.estaSubindo() && andar.getNumero() < elevador.getAndarAtual()) ||
                (!elevador.estaSubindo() && andar.getNumero() > elevador.getAndarAtual())) {
                // Adicionar tempo para chegar ao extremo e voltar
                int extremo = elevador.estaSubindo() ? 
                              Collections.max(elevador.getAndaresParaAtender()) : 
                              Collections.min(elevador.getAndaresParaAtender());
                tempoBase += 2 * Math.abs(extremo - elevador.getAndarAtual());
            }
        }
        
        // Considerar a ocupação do elevador (elevadores mais cheios demoram mais)
        tempoBase += elevador.getPessoasDentro().getTamanho();
        
        // Considerar a direção desejada
        if ((subindo && !elevador.estaSubindo()) || (descendo && elevador.estaSubindo())) {
            tempoBase += 5; // Penalidade por estar na direção oposta
        }
        
        return tempoBase;
    }
    
    // Modelo 3: Otimização do consumo de energia
    private void chamarElevadorOtimizacaoEnergia(Andar andar, boolean subindo, boolean descendo, int minutoSimulado) {
        Lista elevadores = central.getElevadores();
        
        // Implementação da heurística de otimização de energia
        Elevador melhorElevador = null;
        int menorConsumoEstimado = Integer.MAX_VALUE;
        
        Ponteiro p = elevadores.getInicio();
        while (p != null) {
            Elevador elevador = (Elevador) p.getElemento();
            
            // Calcular o consumo estimado para este elevador
            int consumoEstimado = calcularConsumoEstimadoEnergia(elevador, andar, horarioPico);
            
            if (consumoEstimado < menorConsumoEstimado) {
                menorConsumoEstimado = consumoEstimado;
                melhorElevador = elevador;
            }
            
            p = p.getProximo();
        }
        
        // Chamar o elevador com menor consumo estimado
        if (melhorElevador != null) {
            melhorElevador.chamarPara(andar.getNumero());
        }
    }
    
    private int calcularConsumoEstimadoEnergia(Elevador elevador, Andar andar, boolean horarioPico) {
        int consumoBase = Math.abs(elevador.getAndarAtual() - andar.getNumero());
        
        // Em horários de pico, priorizar o atendimento mesmo com maior consumo
        if (horarioPico) {
            return consumoBase;
        }
        
        // Fora do horário de pico, considerar fortemente o consumo de energia
        
        // Se o elevador já estiver em movimento, é mais eficiente usá-lo
        if (elevador.estaEmMovimento()) {
            // Se o andar estiver no caminho do elevador, é muito eficiente
            boolean noMesmoCaminho = (elevador.estaSubindo() && andar.getNumero() > elevador.getAndarAtual()) ||
                                    (!elevador.estaSubindo() && andar.getNumero() < elevador.getAndarAtual());
            
            if (noMesmoCaminho) {
                consumoBase /= 2; // Reduzir o consumo estimado pela metade
            } else {
                consumoBase *= 2; // Dobrar o consumo estimado
            }
        }
        
        // Considerar a ocupação do elevador (elevadores mais cheios consomem mais energia)
        consumoBase += elevador.getPessoasDentro().getTamanho();
        
        // Considerar a distância total que o elevador terá que percorrer
        int distanciaTotal = consumoBase;
        for (int andarDestino : elevador.getAndaresParaAtender()) {
            distanciaTotal += Math.abs(andarDestino - andar.getNumero());
        }
        
        return distanciaTotal;
    }
    
    private void atualizarEstatisticasElevadores() {
        Lista elevadores = central.getElevadores();
        Ponteiro p = elevadores.getInicio();
        
        while (p != null) {
            Elevador elevador = (Elevador) p.getElemento();
            
            // Atualizar estatísticas globais com base nas estatísticas do elevador
            pessoasTransportadas += elevador.getPessoasTransportadas();
            tempoTotalViagem += elevador.getTempoTotalViagem();
            
            // Resetar estatísticas do elevador para evitar contagem duplicada
            elevador.resetarEstatisticas();
            
            p = p.getProximo();
        }
    }

    public CentralDeControle getCentral() {
        return central;
    }

    public Lista getAndares() {
        return andares;
    }
    
    public void adicionarPessoa(Pessoa pessoa) {
        pessoasGeradas++;
    }
    
    public void registrarPessoaTransportada(Pessoa pessoa) {
        pessoasTransportadas++;
        tempoTotalEspera += pessoa.getTempoEspera();
        tempoTotalViagem += pessoa.getTempoViagem();
        tempoTotalTransporte += pessoa.getTempoTotal();
        
        temposEspera.add(pessoa.getTempoEspera());
        temposViagem.add(pessoa.getTempoViagem());
        temposTotal.add(pessoa.getTempoTotal());
    }
    
    // Métodos para estatísticas
    public int getPessoasGeradas() {
        return pessoasGeradas;
    }
    
    public int getPessoasTransportadas() {
        return pessoasTransportadas;
    }
    
    public double getTempoMedioEspera() {
        return pessoasTransportadas > 0 ? (double) tempoTotalEspera / pessoasTransportadas : 0;
    }
    
    public double getTempoMedioViagem() {
        return pessoasTransportadas > 0 ? (double) tempoTotalViagem / pessoasTransportadas : 0;
    }
    
    public double getTempoMedioTotal() {
        return pessoasTransportadas > 0 ? (double) tempoTotalTransporte / pessoasTransportadas : 0;
    }
    
    public List<Integer> getTemposEspera() {
        return temposEspera;
    }
    
    public List<Integer> getTemposViagem() {
        return temposViagem;
    }
    
    public List<Integer> getTemposTotal() {
        return temposTotal;
    }
    
    public void incrementarPessoasGeradas(int quantidade) {
        pessoasGeradas += quantidade;
    }
    
    public HeuristicaElevador getHeuristicaAtual() {
        return heuristicaAtual;
    }
    
    public void setHeuristicaAtual(HeuristicaElevador heuristica) {
        this.heuristicaAtual = heuristica;
    }
    
    public boolean isHorarioPico() {
        return horarioPico;
    }
}
