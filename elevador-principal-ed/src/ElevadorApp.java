import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;
import java.util.List;
import javax.swing.border.TitledBorder;
import java.text.DecimalFormat;

public class ElevadorApp {
    private JFrame frame;
    private JPanel buildingPanel;
    private JPanel controlPanel;
    private JPanel statsPanel;
    private Simulador simulador;
    private Timer uiUpdateTimer;
    private int numAndares = 5;
    private int numElevadores = 2;
    private int capacidadeElevador = 8;
    private int velocidadeSimulacao = 1000; // 1 segundo por minuto simulado
    private Random random = new Random();
    private DecimalFormat df = new DecimalFormat("#.##");
    private HeuristicaElevador heuristicaAtual = HeuristicaElevador.SEM_HEURISTICA;

    public ElevadorApp() {
        // Inicializar o simulador
        simulador = new Simulador(numAndares, numElevadores, velocidadeSimulacao, capacidadeElevador);
        simulador.setHeuristicaAtual(heuristicaAtual);
        
        // Configurar a interface gráfica
        inicializarInterface();
        
        // Iniciar o timer para atualizar a UI
        iniciarUITimer();
    }

    private void inicializarInterface() {
        // Configurar o frame principal
        frame = new JFrame("Simulador de Elevadores");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 700);
        frame.setLayout(new BorderLayout());

        // Painel de configuração no topo
        JPanel configPanel = criarPainelConfiguracao();
        frame.add(configPanel, BorderLayout.NORTH);

        // Painel principal dividido entre visualização do prédio e estatísticas
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Painel do prédio à esquerda
        buildingPanel = new BuildingPanel(simulador);
        JScrollPane scrollPane = new JScrollPane(buildingPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Painel de estatísticas à direita
        statsPanel = criarPainelEstatisticas();
        mainPanel.add(statsPanel, BorderLayout.EAST);
        
        frame.add(mainPanel, BorderLayout.CENTER);

        // Painel de controle na parte inferior
        controlPanel = criarPainelControle();
        frame.add(controlPanel, BorderLayout.SOUTH);

        // Exibir a janela
        frame.setVisible(true);
    }

    private JPanel criarPainelConfiguracao() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Configuração"));

        // Configuração do número de andares - sem limite superior
        panel.add(new JLabel("Andares:"));
        JSpinner andaresSpinner = new JSpinner(new SpinnerNumberModel(numAndares, 2, Integer.MAX_VALUE, 1));
        andaresSpinner.addChangeListener(e -> {
            numAndares = (int) andaresSpinner.getValue();
        });
        // Permitir edição direta do valor
        JSpinner.NumberEditor andaresEditor = new JSpinner.NumberEditor(andaresSpinner);
        andaresSpinner.setEditor(andaresEditor);
        panel.add(andaresSpinner);

        // Configuração do número de elevadores - sem limite superior
        panel.add(new JLabel("Elevadores:"));
        JSpinner elevadoresSpinner = new JSpinner(new SpinnerNumberModel(numElevadores, 1, Integer.MAX_VALUE, 1));
        elevadoresSpinner.addChangeListener(e -> {
            numElevadores = (int) elevadoresSpinner.getValue();
        });
        // Permitir edição direta do valor
        JSpinner.NumberEditor elevadoresEditor = new JSpinner.NumberEditor(elevadoresSpinner);
        elevadoresSpinner.setEditor(elevadoresEditor);
        panel.add(elevadoresSpinner);

        // Configuração da capacidade do elevador - sem limite superior
        panel.add(new JLabel("Capacidade:"));
        JSpinner capacidadeSpinner = new JSpinner(new SpinnerNumberModel(capacidadeElevador, 1, Integer.MAX_VALUE, 1));
        capacidadeSpinner.addChangeListener(e -> {
            capacidadeElevador = (int) capacidadeSpinner.getValue();
        });
        // Permitir edição direta do valor
        JSpinner.NumberEditor capacidadeEditor = new JSpinner.NumberEditor(capacidadeSpinner);
        capacidadeSpinner.setEditor(capacidadeEditor);
        panel.add(capacidadeSpinner);

        // Configuração da velocidade de simulação
        panel.add(new JLabel("Velocidade (ms):"));
        JSpinner velocidadeSpinner = new JSpinner(new SpinnerNumberModel(velocidadeSimulacao, 10, 10000, 10));
        velocidadeSpinner.addChangeListener(e -> {
            velocidadeSimulacao = (int) velocidadeSpinner.getValue();
            if (simulador != null) {
                simulador.setVelocidadeEmMs(velocidadeSimulacao);
            }
        });
        panel.add(velocidadeSpinner);
        
        // Seleção da heurística
        panel.add(new JLabel("Heurística:"));
        JComboBox<HeuristicaElevador> heuristicaComboBox = new JComboBox<>(HeuristicaElevador.values());
        heuristicaComboBox.setSelectedItem(heuristicaAtual);
        heuristicaComboBox.addActionListener(e -> {
            heuristicaAtual = (HeuristicaElevador) heuristicaComboBox.getSelectedItem();
            if (simulador != null) {
                simulador.setHeuristicaAtual(heuristicaAtual);
            }
        });
        panel.add(heuristicaComboBox);

        // Botão para criar nova simulação
        JButton novaSimulacaoBtn = new JButton("Nova Simulação");
        novaSimulacaoBtn.addActionListener(e -> {
            if (simulador != null) {
                simulador.encerrar();
            }
            simulador = new Simulador(numAndares, numElevadores, velocidadeSimulacao, capacidadeElevador);
            simulador.setHeuristicaAtual(heuristicaAtual);
            buildingPanel = new BuildingPanel(simulador);
            
            // Atualizar o painel do prédio
            JScrollPane scrollPane = (JScrollPane) ((JPanel)frame.getContentPane().getComponent(1)).getComponent(0);
            scrollPane.setViewportView(buildingPanel);
            
            frame.revalidate();
            frame.repaint();
        });
        panel.add(novaSimulacaoBtn);

        return panel;
    }

    private JPanel criarPainelEstatisticas() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Estatísticas"));
        panel.setPreferredSize(new Dimension(300, 500));
        
        // Estatísticas gerais
        JPanel generalStatsPanel = new JPanel();
        generalStatsPanel.setLayout(new BoxLayout(generalStatsPanel, BoxLayout.Y_AXIS));
        generalStatsPanel.setBorder(BorderFactory.createTitledBorder("Geral"));
        
        JLabel minutoLabel = new JLabel("Minuto simulado: 0");
        JLabel heuristicaLabel = new JLabel("Heurística: " + heuristicaAtual.toString());
        JLabel pessoasGeradasLabel = new JLabel("Pessoas geradas: 0");
        JLabel pessoasTransportadasLabel = new JLabel("Pessoas transportadas: 0");
        JLabel tempoMedioEsperaLabel = new JLabel("Tempo médio de espera: 0.00 min");
        JLabel tempoMedioViagemLabel = new JLabel("Tempo médio de viagem: 0.00 min");
        JLabel tempoMedioTotalLabel = new JLabel("Tempo médio total: 0.00 min");
        JLabel horarioPicoLabel = new JLabel("Horário de pico: Não");
        
        generalStatsPanel.add(minutoLabel);
        generalStatsPanel.add(Box.createVerticalStrut(5));
        generalStatsPanel.add(heuristicaLabel);
        generalStatsPanel.add(Box.createVerticalStrut(5));
        generalStatsPanel.add(pessoasGeradasLabel);
        generalStatsPanel.add(Box.createVerticalStrut(5));
        generalStatsPanel.add(pessoasTransportadasLabel);
        generalStatsPanel.add(Box.createVerticalStrut(5));
        generalStatsPanel.add(tempoMedioEsperaLabel);
        generalStatsPanel.add(Box.createVerticalStrut(5));
        generalStatsPanel.add(tempoMedioViagemLabel);
        generalStatsPanel.add(Box.createVerticalStrut(5));
        generalStatsPanel.add(tempoMedioTotalLabel);
        generalStatsPanel.add(Box.createVerticalStrut(5));
        generalStatsPanel.add(horarioPicoLabel);
        
        panel.add(generalStatsPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // Estatísticas dos elevadores
        JPanel elevatorsStatsPanel = new JPanel();
        elevatorsStatsPanel.setLayout(new BoxLayout(elevatorsStatsPanel, BoxLayout.Y_AXIS));
        elevatorsStatsPanel.setBorder(BorderFactory.createTitledBorder("Elevadores"));
        
        // Será preenchido dinamicamente
        JScrollPane elevatorScrollPane = new JScrollPane(elevatorsStatsPanel);
        elevatorScrollPane.setPreferredSize(new Dimension(280, 200));
        panel.add(elevatorScrollPane);
        panel.add(Box.createVerticalStrut(10));
        
        // Descrição das heurísticas
        JPanel heuristicsPanel = new JPanel();
        heuristicsPanel.setLayout(new BoxLayout(heuristicsPanel, BoxLayout.Y_AXIS));
        heuristicsPanel.setBorder(BorderFactory.createTitledBorder("Descrição das Heurísticas"));
        
        JTextArea heuristicsText = new JTextArea();
        heuristicsText.setEditable(false);
        heuristicsText.setLineWrap(true);
        heuristicsText.setWrapStyleWord(true);
        heuristicsText.setText(
            "Modelo 1: Sem heurística (FCFS)\n" +
            "- Atendimento na ordem de chegada\n" +
            "- Primeiro elevador disponível atende a primeira chamada\n\n" +
            
            "Modelo 2: Otimização do tempo de espera\n" +
            "- Prioriza minimizar o tempo de espera das pessoas\n" +
            "- Considera distância, direção e ocupação do elevador\n" +
            "- Estima o tempo de chegada de cada elevador\n\n" +
            
            "Modelo 3: Otimização do consumo de energia\n" +
            "- Minimiza deslocamentos desnecessários\n" +
            "- Considera horários de pico e baixo movimento\n" +
            "- Prioriza elevadores já em movimento na direção correta\n" +
            "- Agrupa chamadas para reduzir viagens"
        );
        
        JScrollPane heuristicsScroll = new JScrollPane(heuristicsText);
        heuristicsScroll.setPreferredSize(new Dimension(280, 150));
        heuristicsPanel.add(heuristicsScroll);
        
        panel.add(heuristicsPanel);
        
        // Timer para atualizar as estatísticas
        Timer statsTimer = new Timer(500, e -> {
            // Atualizar estatísticas gerais
            minutoLabel.setText("Minuto simulado: " + simulador.getMinutoSimulado());
            heuristicaLabel.setText("Heurística: " + simulador.getHeuristicaAtual().toString());
            pessoasGeradasLabel.setText("Pessoas geradas: " + simulador.getPessoasGeradas());
            pessoasTransportadasLabel.setText("Pessoas transportadas: " + simulador.getPessoasTransportadas());
            tempoMedioEsperaLabel.setText("Tempo médio de espera: " + df.format(simulador.getTempoMedioEspera()) + " seg");
            tempoMedioViagemLabel.setText("Tempo médio de viagem: " + df.format(simulador.getTempoMedioViagem()) + " seg");
            tempoMedioTotalLabel.setText("Tempo médio total: " + df.format(simulador.getTempoMedioTotal()) + " seg");
            horarioPicoLabel.setText("Horário de pico: " + (simulador.getPredio().isHorarioPico() ? "Sim" : "Não"));
            
            // Atualizar estatísticas dos elevadores
            elevatorsStatsPanel.removeAll();
            
            Lista elevadores = simulador.getPredio().getCentral().getElevadores();
            Ponteiro p = elevadores.getInicio();
            while (p != null) {
                Elevador elevador = (Elevador) p.getElemento();
                
                JPanel elevadorPanel = new JPanel();
                elevadorPanel.setLayout(new BoxLayout(elevadorPanel, BoxLayout.Y_AXIS));
                elevadorPanel.setBorder(BorderFactory.createTitledBorder("Elevador " + elevador.getId()));
                
                JLabel posicaoLabel = new JLabel("Posição: Andar " + elevador.getAndarAtual());
                JLabel estadoLabel = new JLabel("Estado: " + (elevador.estaEmMovimento() ? 
                                              (elevador.estaSubindo() ? "Subindo" : "Descendo") : "Parado"));
                JLabel ocupacaoLabel = new JLabel("Ocupação: " + elevador.getPessoasDentro().getTamanho() + 
                                               "/" + elevador.getCapacidadeMaxima());
                
                // Destinos pendentes
                StringBuilder destinosSb = new StringBuilder("Destinos: ");
                List<Integer> destinos = elevador.getAndaresParaAtender();
                if (destinos.isEmpty()) {
                    destinosSb.append("Nenhum");
                } else {
                    for (int i = 0; i < Math.min(10, destinos.size()); i++) {
                        destinosSb.append(destinos.get(i));
                        if (i < Math.min(10, destinos.size()) - 1) {
                            destinosSb.append(", ");
                        }
                    }
                    if (destinos.size() > 10) {
                        destinosSb.append("... (+" + (destinos.size() - 10) + " mais)");
                    }
                }
                JLabel destinosLabel = new JLabel(destinosSb.toString());
                
                elevadorPanel.add(posicaoLabel);
                elevadorPanel.add(estadoLabel);
                elevadorPanel.add(ocupacaoLabel);
                elevadorPanel.add(destinosLabel);
                
                elevatorsStatsPanel.add(elevadorPanel);
                elevatorsStatsPanel.add(Box.createVerticalStrut(5));
                
                p = p.getProximo();
            }
            
            elevatorsStatsPanel.revalidate();
            elevatorsStatsPanel.repaint();
        });
        statsTimer.start();
        
        return panel;
    }

    private JPanel criarPainelControle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Controle da Simulação"));

        // Painel para os botões de controle
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Botão Iniciar
        JButton iniciarBtn = new JButton("Iniciar");
        iniciarBtn.addActionListener(e -> simulador.iniciar());
        botoesPanel.add(iniciarBtn);

        // Botão Pausar
        JButton pausarBtn = new JButton("Pausar");
        pausarBtn.addActionListener(e -> simulador.pausar());
        botoesPanel.add(pausarBtn);

        // Botão Continuar
        JButton continuarBtn = new JButton("Continuar");
        continuarBtn.addActionListener(e -> simulador.continuar());
        botoesPanel.add(continuarBtn);

        // Botão Encerrar
        JButton encerrarBtn = new JButton("Encerrar");
        encerrarBtn.addActionListener(e -> simulador.encerrar());
        botoesPanel.add(encerrarBtn);

        // Botão Salvar
        JButton salvarBtn = new JButton("Salvar");
        salvarBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                simulador.gravar(file.getAbsolutePath());
            }
        });
        botoesPanel.add(salvarBtn);

        // Botão Carregar
        JButton carregarBtn = new JButton("Carregar");
        carregarBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                simulador = Simulador.carregar(file.getAbsolutePath());
                buildingPanel = new BuildingPanel(simulador);
                
                // Atualizar o painel do prédio
                JScrollPane scrollPane = (JScrollPane) ((JPanel)frame.getContentPane().getComponent(1)).getComponent(0);
                scrollPane.setViewportView(buildingPanel);
                
                frame.revalidate();
                frame.repaint();
            }
        });
        botoesPanel.add(carregarBtn);

        panel.add(botoesPanel, BorderLayout.NORTH);

        // Painel para adicionar pessoas aleatoriamente
        JPanel pessoaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pessoaPanel.setBorder(BorderFactory.createTitledBorder("Adicionar Pessoas Aleatoriamente"));
        
        pessoaPanel.add(new JLabel("Quantidade:"));
        // Sem limite superior para a quantidade de pessoas
        JSpinner quantidadeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, Integer.MAX_VALUE, 1));
        // Permitir edição direta do valor
        JSpinner.NumberEditor quantidadeEditor = new JSpinner.NumberEditor(quantidadeSpinner);
        quantidadeSpinner.setEditor(quantidadeEditor);
        pessoaPanel.add(quantidadeSpinner);
        
        JButton adicionarPessoasBtn = new JButton("Adicionar Pessoas Aleatórias");
        adicionarPessoasBtn.addActionListener(e -> {
            int quantidade = (int) quantidadeSpinner.getValue();
            adicionarPessoasAleatorias(quantidade);
        });
        pessoaPanel.add(adicionarPessoasBtn);
        
        panel.add(pessoaPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void adicionarPessoasAleatorias(int quantidade) {
        // Obter o prédio do simulador
        Predio predio = simulador.getPredio();
        
        // Obter a lista de andares
        Lista andares = predio.getAndares();
        int totalAndares = 0;
        
        // Contar o número total de andares
        Ponteiro p = andares.getInicio();
        while (p != null) {
            totalAndares++;
            p = p.getProximo();
        }
        
        if (totalAndares < 2) {
            JOptionPane.showMessageDialog(frame, "É necessário ter pelo menos 2 andares para adicionar pessoas!", 
                                         "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Adicionar a quantidade especificada de pessoas
        for (int i = 0; i < quantidade; i++) {
            // Gerar origem e destino aleatórios (diferentes entre si)
            int origem = random.nextInt(totalAndares);
            int destino;
            do {
                destino = random.nextInt(totalAndares);
            } while (destino == origem);
            
            // Encontrar o andar de origem
            p = andares.getInicio();
            Andar andarOrigem = null;
            
            for (int j = 0; j < origem && p != null; j++) {
                p = p.getProximo();
            }
            
            if (p != null) {
                andarOrigem = (Andar) p.getElemento();
                
                // Criar uma nova pessoa
                Pessoa pessoa = new Pessoa(simulador.getProximoIdPessoa(), origem, destino, simulador.getMinutoSimulado());
                
                // Adicionar a pessoa à fila de espera do andar
                andarOrigem.getPessoasAguardando().enfileirar(pessoa);
                
                // Ativar o botão apropriado no painel do elevador
                if (destino > origem) {
                    andarOrigem.getPainel().pressionarSubir();
                } else {
                    andarOrigem.getPainel().pressionarDescer();
                }
            }
        }
        
        // Incrementar o contador de pessoas geradas
        simulador.incrementarPessoasGeradas(quantidade);
        
        // Atualizar a interface
        buildingPanel.repaint();
        
        JOptionPane.showMessageDialog(frame, quantidade + " pessoas foram adicionadas aleatoriamente.", 
                                     "Pessoas Adicionadas", JOptionPane.INFORMATION_MESSAGE);
    }

    private void iniciarUITimer() {
        uiUpdateTimer = new Timer(100, e -> {
            buildingPanel.repaint();
        });
        uiUpdateTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ElevadorApp();
        });
    }

    // Classe interna para representar o painel do prédio
    class BuildingPanel extends JPanel {
        private Simulador simulador;
        private final int ANDAR_HEIGHT = 80;
        private final int ELEVADOR_WIDTH = 60;
        private final int PESSOA_SIZE = 20;
        private final int MARGIN = 20;

        public BuildingPanel(Simulador simulador) {
            this.simulador = simulador;
            
            // Calcular a altura necessária com base no número de andares
            int numAndares = 0;
            Lista andares = simulador.getPredio().getAndares();
            Ponteiro p = andares.getInicio();
            while (p != null) {
                numAndares++;
                p = p.getProximo();
            }
            
            setPreferredSize(new Dimension(800, numAndares * ANDAR_HEIGHT + MARGIN * 2));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Desenhar o prédio
            Predio predio = simulador.getPredio();
            Lista andares = predio.getAndares();
            CentralDeControle central = predio.getCentral();
            Lista elevadores = central.getElevadores();

            // Desenhar os andares
            Ponteiro pAndar = andares.getInicio();
            int andarY = getHeight() - MARGIN;
            
            while (pAndar != null) {
                Andar andar = (Andar) pAndar.getElemento();
                
                // Desenhar o andar
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(MARGIN, andarY - ANDAR_HEIGHT, getWidth() - MARGIN * 2, 2);
                
                // Número do andar
                g2d.setColor(Color.BLACK);
                g2d.drawString("Andar " + andar.getNumero(), MARGIN, andarY - ANDAR_HEIGHT / 2);
                
                // Desenhar pessoas esperando
                Fila pessoasAguardando = andar.getPessoasAguardando();
                if (!pessoasAguardando.estaVazia()) {
                    Ponteiro pPessoa = pessoasAguardando.getLista().getInicio();
                    int pessoaX = MARGIN + 100;
                    int pessoaY = andarY - ANDAR_HEIGHT / 2;
                    int pessoasPorLinha = 0;
                    
                    while (pPessoa != null) {
                        Pessoa pessoa = (Pessoa) pPessoa.getElemento();
                        desenharPessoa(g2d, pessoaX, pessoaY, pessoa);
                        
                        pessoaX += PESSOA_SIZE + 5;
                        pessoasPorLinha++;
                        
                        // Se atingir o limite de pessoas por linha, pule para a próxima linha
                        if (pessoasPorLinha >= 10) {
                            pessoasPorLinha = 0;
                            pessoaX = MARGIN + 100;
                            pessoaY += PESSOA_SIZE + 5;
                        }
                        
                        pPessoa = pPessoa.getProximo();
                    }
                }
                
                // Desenhar botões do painel
                PainelElevador painel = andar.getPainel();
                int painelX = MARGIN + 200;
                int painelY = andarY - ANDAR_HEIGHT / 2;
                
                // Botão subir
                g2d.setColor(painel.isBotaoSubirAtivado() ? Color.RED : Color.GRAY);
                g2d.fillPolygon(
                    new int[]{painelX, painelX + 15, painelX + 30},
                    new int[]{painelY, painelY - 15, painelY},
                    3
                );
                
                // Botão descer
                g2d.setColor(painel.isBotaoDescerAtivado() ? Color.RED : Color.GRAY);
                g2d.fillPolygon(
                    new int[]{painelX, painelX + 15, painelX + 30},
                    new int[]{painelY + 20, painelY + 35, painelY + 20},
                    3
                );
                
                andarY -= ANDAR_HEIGHT;
                pAndar = pAndar.getProximo();
            }
            
            // Desenhar os elevadores
            Ponteiro pElevador = elevadores.getInicio();
            int elevadorX = getWidth() - MARGIN - ELEVADOR_WIDTH;
            int elevadorIndex = 0;
            
            while (pElevador != null) {
                Elevador elevador = (Elevador) pElevador.getElemento();
                
                // Posição do elevador
                int elevadorY = getHeight() - MARGIN - (elevador.getAndarAtual() * ANDAR_HEIGHT) - ANDAR_HEIGHT;
                
                // Desenhar o elevador
                g2d.setColor(elevador.estaEmMovimento() ? Color.GREEN : Color.BLUE);
                g2d.fillRect(elevadorX, elevadorY, ELEVADOR_WIDTH, ANDAR_HEIGHT - 10);
                
                // ID do elevador e capacidade
                g2d.setColor(Color.WHITE);
                g2d.drawString("E" + elevador.getId(), elevadorX + 5, elevadorY + 15);
                g2d.drawString(elevador.getPessoasDentro().getTamanho() + "/" + elevador.getCapacidadeMaxima(), 
                              elevadorX + 5, elevadorY + 30);
                
                // Desenhar pessoas dentro do elevador
                Lista pessoasDentro = elevador.getPessoasDentro();
                if (!pessoasDentro.estaVazia()) {
                    Ponteiro pPessoa = pessoasDentro.getInicio();
                    int pessoaX = elevadorX + 5;
                    int pessoaY = elevadorY + 40;
                    int pessoasPorLinha = 0;
                    
                    while (pPessoa != null) {
                        Pessoa pessoa = (Pessoa) pPessoa.getElemento();
                        desenharPessoa(g2d, pessoaX, pessoaY, pessoa);
                        
                        pessoaX += PESSOA_SIZE + 2;
                        pessoasPorLinha++;
                        
                        // Se atingir o limite de pessoas por linha, pule para a próxima linha
                        if (pessoasPorLinha >= (ELEVADOR_WIDTH / (PESSOA_SIZE + 2))) {
                            pessoasPorLinha = 0;
                            pessoaX = elevadorX + 5;
                            pessoaY += PESSOA_SIZE + 2;
                        }
                        
                        pPessoa = pPessoa.getProximo();
                    }
                }
                
                // Desenhar destinos pendentes
                List<Integer> destinos = elevador.getAndaresParaAtender();
                if (!destinos.isEmpty()) {
                    g2d.setColor(Color.WHITE);
                    StringBuilder sb = new StringBuilder("→ ");
                    for (int i = 0; i < Math.min(3, destinos.size()); i++) {
                        sb.append(destinos.get(i));
                        if (i < Math.min(3, destinos.size()) - 1) {
                            sb.append(",");
                        }
                    }
                    if (destinos.size() > 3) {
                        sb.append("...");
                    }
                    g2d.drawString(sb.toString(), elevadorX + 5, elevadorY + ANDAR_HEIGHT - 15);
                }
                
                elevadorX -= ELEVADOR_WIDTH + 10;
                elevadorIndex++;
                pElevador = pElevador.getProximo();
            }
        }
        
        private void desenharPessoa(Graphics2D g2d, int x, int y, Pessoa pessoa) {
            // Desenhar círculo para a cabeça
            g2d.setColor(Color.ORANGE);
            g2d.fillOval(x, y - PESSOA_SIZE/2, PESSOA_SIZE, PESSOA_SIZE);
            
            // Desenhar ID da pessoa
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(pessoa.getId()), x + 5, y + 5);
            
            // Desenhar uma pequena indicação do destino
            g2d.setColor(Color.RED);
            g2d.drawString("→" + pessoa.getAndarDestino(), x, y + PESSOA_SIZE);
        }
    }
}
