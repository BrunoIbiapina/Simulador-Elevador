import java.io.Serializable;

/**
  Enum que representa os diferentes modelos de heurística para o controle de elevadores.
 */
public enum HeuristicaElevador implements Serializable {
    /**
     * Modelo 1: Sem heurística (atendimento na ordem de chegada).
     */
    SEM_HEURISTICA,
    
    /**
     * Modelo 2: Otimização do tempo de espera, ajustando dinamicamente os ciclos dos elevadores
     * conforme a fila de espera.
     */
    OTIMIZACAO_TEMPO_ESPERA,
    
    /**
     * Modelo 3: Otimização do consumo de energia, minimizando deslocamentos desnecessários e
     * ajustando os ciclos de operação conforme os horários de pico e de menor movimentação.
     */
    OTIMIZACAO_ENERGIA;
    
    @Override
    public String toString() {
        switch (this) {
            case SEM_HEURISTICA:
                return "Modelo 1: Sem heurística (FiFo)";
            case OTIMIZACAO_TEMPO_ESPERA:
                return "Modelo 2: Otimização do tempo de espera";
            case OTIMIZACAO_ENERGIA:
                return "Modelo 3: Otimização do consumo de energia";
            default:
                return "Desconhecido";
        }
    }
}
