package org.example.model;

import org.example.model.enums.AlgoritmoEscalonamento;
import org.example.view.SimulacaoObserver;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Medico implements Runnable {

    private static List<Paciente> pacientes;
    private AlgoritmoEscalonamento algoritmoEscalonamento;

    // Adicionado para comunicação com a UI
    private SimulacaoObserver observer;

    // Controle de tempo relativo da simulação
    private long startTime = 0;
    private boolean iniciado = false;

    public Medico(List<Paciente> pacientes, AlgoritmoEscalonamento algoritmoEscalonamento, SimulacaoObserver observer) {
        this.pacientes = pacientes;
        this.algoritmoEscalonamento = algoritmoEscalonamento;
        this.observer = observer;
    }

    // Sobrecarga para manter compatibilidade se necessário, mas idealmente usamos o construtor acima
    public Medico(List<Paciente> pacientes, AlgoritmoEscalonamento algoritmoEscalonamento) {
        this(pacientes, algoritmoEscalonamento, null);
    }

    public void run() {
        if (!iniciado) {
            this.startTime = System.currentTimeMillis();
            this.iniciado = true;
        }
        // Loop principal: Enquanto houver pacientes na lista
        while (true) {
            // Verifica se ainda há pacientes E se alguém não terminou
            // (simplificação: se lista vazia, thread encerra)
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
                case PRIORITY_NON_PREEMPTIVE:
                    trabalhou = priorityNonPreemptive();
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

    public boolean shortestJobFirst() {
        Paciente atual = null;

        // Tempo que já passou na simulação
        long tempoDecorrido = System.currentTimeMillis() - this.startTime;

        synchronized (pacientes) {
            if (pacientes.isEmpty()) return false;

            Paciente menorJob = null;

            // Procura o menor burst ENTRE os pacientes que já chegaram
            for (Paciente p : pacientes) {
                if (p.getArrivalTime() <= tempoDecorrido) {
                    if (menorJob == null || p.getBurstTime() < menorJob.getBurstTime()) {
                        menorJob = p;
                    }
                }
            }

            // Se ninguém chegou ainda, não há o que fazer neste ciclo
            if (menorJob == null) {
                return false;
            }

            atual = menorJob;
            pacientes.remove(atual);
        }

        try {
            if (atual.isFirstRodeo()) {
                Thread.sleep(50);
                atual.setFirstRodeo(false);
            }

            if (observer != null) observer.notificarInicioExecucao(atual);

            // Executa todo o burst
            int tempoParaExecutar = atual.getBurstTime();
            Thread.sleep(tempoParaExecutar);

            atual.setBurstTime(0);

            if (observer != null) observer.notificarFimExecucao(atual);
            if (observer != null) observer.notificarConclusao(atual);

            return true;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean shortestRemainingTimeFirst() {
        Paciente atual = null;
        long tempoDecorrido = System.currentTimeMillis() - this.startTime;

        synchronized (pacientes) {
            if (pacientes.isEmpty()) return false;

            // 1. SELEÇÃO DO PACIENTE (Igual ao seu código)
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

                    // Verifica se ALGUÉM na fila é melhor que o atual
                    precisaPreemptar = pacientes.stream()
                            .anyMatch(p -> p.getArrivalTime() <= finalTempoDecorrido
                                    && p.getBurstTime() < finalAtual.getBurstTime());

                    if (precisaPreemptar) {
                        // SÓ AQUI ele volta para a fila
                        if (observer != null) observer.notificarFimExecucao(atual);
                        pacientes.add(atual);
                        break;
                    }
                }
                // SE NÃO PRECISAR PREEMPTAR:
                // O código simplesmente ignora o if acima, atinge o fim do while
                // e volta para o topo para processar mais 100ms DO MESMO PACIENTE.
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean priorityNonPreemptive() {
        Paciente atual = null;
        long tempoDecorrido = System.currentTimeMillis() - this.startTime;

        synchronized (pacientes) {
            if (pacientes.isEmpty()) return false;

            Paciente candidatoPrioritario = null;

            // Itera sobre a lista para encontrar o paciente com maior prioridade (menor valor)
            // que JÁ chegou no hospital (arrivalTime <= tempoDecorrido)
            for (Paciente p : pacientes) {
                // Regra 1: O paciente precisa ter chegado
                if (p.getArrivalTime() <= tempoDecorrido) {

                    // Se ainda não escolhemos ninguém, pega o primeiro que chegou
                    if (candidatoPrioritario == null) {
                        candidatoPrioritario = p;
                    } else {
                        // Regra 2: Menor valor de prioridade ganha (1 > 5)
                        if (p.getPriority() < candidatoPrioritario.getPriority()) {
                            candidatoPrioritario = p;
                        }
                        // Regra 3: Desempate FIFO (Se prioridades iguais, ganha quem chegou antes)
                        // Como a lista geralmente está ordenada por chegada ou inserção,
                        // o 'if' estrito (<) acima já preserva o FIFO natural da lista.
                        // Mas podemos garantir explicitamente comparando ArrivalTime:
                        else if (p.getPriority() == candidatoPrioritario.getPriority()) {
                            if (p.getArrivalTime() < candidatoPrioritario.getArrivalTime()) {
                                candidatoPrioritario = p;
                            }
                        }
                    }
                }
            }

            // Se encontramos alguém apto, removemos da fila global e assumimos o atendimento
            if (candidatoPrioritario != null) {
                atual = candidatoPrioritario;
                pacientes.remove(atual);
            }
        }

        // Se ninguém chegou ainda ou a lista está vazia
        if (atual == null) return false;

        try {
            // Delay visual para troca de contexto (apenas se for a primeira vez)
            if (atual.isFirstRodeo()) {
                Thread.sleep(50);
                atual.setFirstRodeo(false);
            }

            // --- INÍCIO DO ATENDIMENTO ---
            if (observer != null) observer.notificarInicioExecucao(atual);

            // Como é NÃO-PREEMPTIVO, executamos o Burst Time inteiro de uma vez
            int tempoExecucao = atual.getBurstTime();

            // Simula o processamento travando a thread pelo tempo total
            Thread.sleep(tempoExecucao);

            // O processo terminou
            atual.setBurstTime(0);

            // --- FIM DO ATENDIMENTO ---
            if (observer != null) observer.notificarFimExecucao(atual);

            // Notifica que o processo foi concluído (saiu do sistema)
            if (observer != null) observer.notificarConclusao(atual);

            return true;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Método auxiliar para limpar o código do run (opcional)
    private void esperar() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
    }
}