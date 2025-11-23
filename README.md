# SIMULADOR DE ESCALONAMENTO DE PROCESSOS

## INTEGRANTES
- KEVIN ÁVILA REHEM
- HENRIQUE PAIM
- ANTÔNIO CARLOS
- RHUAN AZEVEDO

## OBJETIVO
O projeto tem por objetivo por em prática conceitos de escalonamento de processos para melhor entendimento da matéria de Sistemas Operacionais
 
## RESUMO

O projeto "Simulador de Escalonamento de Processos" aplica conceitos de escalonamento de sistemas operacionais a um cenário de filas de atendimento hospitalar. O objetivo é permitir experimentar e comparar diferentes políticas de escalonamento (Round-Robin, Shortest Job First, Shortest Remaining Time First e Prioridade Não-Preemptivo) em um ambiente controlado, observando métricas como tempo de espera, tempo de atendimento e utilização dos profissionais.

- Principais componentes:
    - Modelo: as classes `Paciente` e `Medico` representam respectivamente os processos e os núcleos do processador, junto com enums para os algoritmos de escalonamento.
    - Visualização: telas e pop-ups (por exemplo, `TelaConfiguracaoGeral`, `TelaCriacaoPaciente`, `TelaSimulacao`, `TelaResumoPacientes`, `DialogConfigsMedico`) que permitem configurar a simulação, inserir pacientes e visualizar resultados.
    - Observers: implementação de observadores (por exemplo, `SimulacaoObserver`) para atualizar a interface conforme a simulação avança.

- Tecnologia:
    - Linguagem: Java
    - Build: Maven (`pom.xml` está no diretório `Hospital/`)

## PACIENTE (PROCESSO)
Classe que representa os processos criados pelos programas. Armazena diversos parâmetros de execução.

### ATRIBUTOS
- `private String name` ==> Tag representativa do processo(P1, P2...)
- `private int arrivalTime` ==> Momento em que o processochega à fila
- `private int burstTime` ==> Tempo total que cada Paciente(processo) será atendido (Executado) pelos Médicos (Cores).
- `private int priority` ==> Prioridade de atendimento doPaciente (Processo). Sendo 1 a maior e 5 a menor
- `private int quantum` ==> Quantia de tempo que o Pacienteocupará o Médico a cada vez que for atendido **(ApenasRound-Robin)**
- `private boolean firstRodeo` ==> Indica se é a primeira entrada do Paciente no consultório 

## MÉDICO (CORE)
Classe que representa os núcleos do processador, fará o trabalho de consumir os Pacientes a partir de uma lista compartilhada entre todos os Médicos. Detentor dos algoritmos de escalonamento.

### AlgoritmosEscalonamento (Enum)
```java
package org.example.model.enums;

public enum AlgoritmoEscalonamento {
    ROUND_ROBIN,
    SHORTEST_JOB_FIRST,
    SHORTEST_REMAINING_TIME_FIRST,
    PRIORITY_NON_PREEMPTIVE
}

``` 

### ATRIBUTOS
- #### VARIÁVEIS COMPARTILHADAS
    - private static List<Paciente> pacientes;  
    - private AlgoritmoEscalonamento algoritmoEscalonamento;

- #### COMUNICAÇÃO COM A UI
    - private SimulacaoObserver observer;

- #### CONTROLE DE TEMPO RELATIVO DA EXECUÇÃO
    - private long startTime = 0;
    - private boolean iniciado = false;

### IMPLEMENTAÇÕES
#### `public class Medico implements Runnable`
```java
public void run() {
        if (!iniciado) {
            this.startTime = System.currentTimeMillis();
            this.iniciado = true;
        }
        // Loop principal: Enquanto houver pacientes na lista
        while (true) {
            // Verifica se ainda há pacientes E se alguém não terminou
            // (se lista vazia, thread encerra)
            synchronized (pacientes) {
                if (pacientes.isEmpty()) break;
            }

            boolean trabalhou = false;
            switch (algoritmoEscalonamento) {
                case ROUND_ROBIN:
                    trabalhou = roundRobin();
                    // Se não trabalhou (ex: todos esperando arrival time), evita loop infinito consumindo CPU
                    if (!trabalhou) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                    break;
                case SHORTEST_JOB_FIRST:
                    trabalhou = shortestJobFirst();
                    break;
                case SHORTEST_REMAINING_TIME_FIRST:
                    trabalhou = shortestRemainingTimeFirst();
                    break;
                default:
                    break;
            }
            if (!trabalhou) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
    }
```

Método run inicia a simulação, capturando o tempo de início para registrar as métricas de execução. O algoritmo de escalonamento que será implementado será escolhido ao dar um set na variável global algoritmoEscalonamento

---

### Round-Robin
O algoritmo Round-Robin, ao executar, remove o primeiro processo da fila de execução (FIFO), executa ele pelo *`quantum`* de tempo definido na criação do Paciente. Em seguida, se ainda houver tempo de execução restante no Paciente, ele retorna ao final da fila estática de Pacientes, de forma sincronizada - a fim de evitar situações de disputa - e é executado novamente quando chega sua vez, novamente pelo mesmo *`quantum`* de tempo, até que não haja *`burstTime`* restante para o Paciente.

```java
public boolean roundRobin() {
        Paciente atual = null;

        synchronized (pacientes) {
            if (pacientes.isEmpty()) return false;

            // Vamos varrer a lista para achar alguém que já chegou.
            long tempoAtualSimulacao = System.currentTimeMillis();

            // Pega o primeiro (Fila FIFO do RR)
            atual = pacientes.remove(0);
        }

        if (atual == null) return false;

        try {
            if (atual.isFirstRodeo()) {
                Thread.sleep(50); // Pequeno delay visual para troca de contexto
                atual.setFirstRodeo(false);
            }

            // NOTIFICA INICIO (UI desenha verde)
            if (observer != null) observer.notificarInicioExecucao(atual);

            int tempoExecucao = Integer.min(atual.getQuantum(), atual.getBurstTime());

            // Simula processamento
            Thread.sleep(tempoExecucao);

            // Atualiza tempo restante
            atual.setBurstTime(atual.getBurstTime() - tempoExecucao);

            // NOTIFICA FIM (UI salva bloco azul)
            if (observer != null) observer.notificarFimExecucao(atual);

            // Se ainda tem burst time, volta pro fim da fila
            if (atual.getBurstTime() > 0) {
                synchronized (pacientes) {
                    pacientes.add(atual);
                }
            } else {
                if (observer != null) observer.notificarConclusao(atual);
            }
            return true;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
```

---

### Shortest Job First


---

### Shortest Remaining Time First

O algoritmo Shortest Remaining Time First (SRTF) é a versão preemptiva do Shortest Job First. A cada ciclo, ele seleciona o paciente que possui o menor tempo de execução restante (`burstTime`) entre aqueles que já chegaram (`arrivalTime <= tempoDecorrido`).

Diferente das outras implementações, este método mantém um loop interno de execução. A cada pequena fatia de tempo processada (simulada como 100ms), o sistema verifica se houve a chegada de um novo paciente com tempo de execução menor do que o tempo restante do paciente atual. Se essa condição for verdadeira, ocorre a **preempção**: o paciente atual é interrompido, devolvido à fila de espera, e o médico fica livre para selecionar o novo melhor candidato na próxima iteração principal.

```java
public boolean shortestRemainingTimeFirst() {
    Paciente atual = null;
    long tempoDecorrido = System.currentTimeMillis() - this.startTime;

    synchronized (pacientes) {
        if (pacientes.isEmpty()) return false;

        // 1. SELEÇÃO DO PACIENTE (Menor tempo restante entre os que já chegaram)
        long finalTempoDecorrido = tempoDecorrido;
        Optional<Paciente> candidato = pacientes.stream()
                .filter(p -> p.getArrivalTime() <= finalTempoDecorrido)
                .min(Comparator.comparingInt(Paciente::getBurstTime));

        if (candidato.isPresent()) {
            atual = candidato.get();
            pacientes.remove(atual); // Remove da lista global para atender com exclusividade
        }
    }

    if (atual == null) return false;

    try {
        if (observer != null) observer.notificarInicioExecucao(atual);

        // LOOP DE EXECUÇÃO (Mantém o paciente na CPU enquanto for o melhor)
        while (true) {
            // 2. EXECUÇÃO DA FATIA DE TEMPO
            int timeStep = 100;
            int tempoExecucao = Integer.min(timeStep, atual.getBurstTime());

            Thread.sleep(tempoExecucao);
            atual.setBurstTime(atual.getBurstTime() - tempoExecucao);


            // 3. VERIFICAÇÃO DE CONCLUSÃO
            if (atual.getBurstTime() == 0) {
                if (observer != null) observer.notificarFimExecucao(atual);
                if (observer != null) observer.notificarConclusao(atual);
                System.out.printf(atual.getBurstTime()+"");
                Thread.sleep(50);
                break;
            }

            // 4. VERIFICAÇÃO DE PREEMPÇÃO
            // Recalcula o tempo atual pois o sleep passou
            tempoDecorrido = System.currentTimeMillis() - this.startTime;

            boolean precisaPreemptar = false;

            synchronized (pacientes) {
                long finalTempoDecorrido = tempoDecorrido;
                Paciente finalAtual = atual;

                // Verifica se ALGUÉM na fila é melhor que o atual (SRTF)
                precisaPreemptar = pacientes.stream()
                        .anyMatch(p -> p.getArrivalTime() <= finalTempoDecorrido
                                && p.getBurstTime() < finalAtual.getBurstTime());

                if (precisaPreemptar) {
                    // SÓ AQUI ele volta para a fila (Troca de Contexto)
                    if (observer != null) observer.notificarFimExecucao(atual);
                    pacientes.add(atual);
                    break;
                }
            }
            // SE NÃO PRECISAR PREEMPTAR:
            // O código continua no loop while, processando mais uma fatia do mesmo paciente.
        }

    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return true;
}
```

---

### Prioridade Não-Preemptivo
