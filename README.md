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
