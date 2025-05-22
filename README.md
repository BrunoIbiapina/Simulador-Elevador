# 🛗 Simulador de Elevador Inteligente

Projeto desenvolvido para a disciplina de **Estrutura de Dados**, com o objetivo de simular o funcionamento de elevadores inteligentes em um prédio, utilizando heurísticas para otimização das paradas, redução do tempo de espera e eficiência energética.

---

## 🎯 Objetivo

Este simulador representa a lógica de controle de elevadores em diferentes andares, com múltiplos passageiros, implementando estratégias para:
- Agrupamento de chamadas por sentido (subida/descida)
- Atendimento prioritário para pessoas com mobilidade reduzida
- Minimização do número de viagens
- Redução do tempo de espera

---

## 🧠 Heurísticas Aplicadas

O comportamento do elevador é controlado por lógicas implementadas na classe `HeuristicaElevador.java`. As principais heurísticas utilizadas incluem:
- **Sentido atual prioritário**: o elevador completa todas as chamadas em um sentido antes de inverter.
- **Fila de espera organizada**: passageiros são alocados em uma fila por ordem e prioridade.
- **Priorização de usuários especiais**: idosos e cadeirantes são atendidos antes em caso de conflito de chamadas.

---

## 🗂️ Estrutura do Projeto

  📁 Simulador-Elevador
├── 📄 *.java            → Código-fonte Java
├── 📄 *.class           → Arquivos compilados (gerados após compilação)
├── 📁 Slides/           → Documentação e apresentação
│   ├── 📄 relatorio.pdf
│   ├── 📄 slides-apresentacao.pptx
│   └── 📄 diagramas.png (se houver)
├── 📄 README.md         → Explicação geral do projeto

---

## ▶️ Como Executar o Projeto

1. **Abrir o terminal na pasta do projeto**
2. **Compilar todos os arquivos `.java`**:

```bash
javac *.java
Executar a classe principal:
java ElevadorApp
Se necessário, tente java Simulador se a classe principal for essa.
📑 Documentação

A documentação do projeto está disponível na pasta Slides/, incluindo:

relatorio.pdf — Relatório técnico completo
slides-apresentacao.pptx — Slides utilizados na apresentação
Diagramas e explicações complementares
