package org.example.model;

import org.example.model.enums.AlgoritmoEscalonamento;
import org.example.view.SimulacaoObserver;

import java.util.List;

public class Medico implements Runnable {

    private List<Paciente> pacientes;
    private AlgoritmoEscalonamento algoritmoEscalonamento;

    // Adicionado para comunicação com a UI
    private SimulacaoObserver observer;

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
        // Loop principal: Enquanto houver pacientes na lista
        while (true) {
            // Verifica se ainda há pacientes E se alguém não terminou
            // (simplificação: se lista vazia, thread encerra)
            synchronized (pacientes) {
                if (pacientes.isEmpty()) break;
            }

            switch (algoritmoEscalonamento) {
                case ROUND_ROBIN:
                    boolean trabalhou = roundRobin();
                    // Se não trabalhou (ex: todos esperando arrival time), evita loop infinito consumindo CPU
                    if (!trabalhou) {
                        try { Thread.sleep(100); } catch (InterruptedException e) {}
                    }
                    break;
                case SHORTEST_JOB_FIRST:
                    trabalhou = shortestJobFirst();
                    break;
                default:
                    break;
            }
        }
    }

    public boolean roundRobin(){
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

            if(atual.isFirstRodeo()) {
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

    public boolean shortestJobFirst(){
        Paciente atual = null;

        synchronized (pacientes) {
            if (pacientes.isEmpty()) return false;

            // Procura quem tem o MENOR burstTime na lista
            Paciente menorJob = null;

            for (Paciente p : pacientes) {
                if (menorJob == null || p.getBurstTime() < menorJob.getBurstTime()) {
                    menorJob = p;
                }
            }

            // Se achamos alguém, removemos ele da lista e assumimos o trabalho
            if (menorJob != null) {
                atual = menorJob;
                pacientes.remove(atual);
            }
        }

        if (atual == null) return false;

        try {
            // Delay visual para primeira execução (troca de contexto visual)
            if(atual.isFirstRodeo()) {
                Thread.sleep(50);
                atual.setFirstRodeo(false);
            }

            // NOTIFICA INICIO (Fica Verde na tela)
            if (observer != null) observer.notificarInicioExecucao(atual);

            // Executa TUDO o que falta, pois Não tem Quantum.
            int tempoParaExecutar = atual.getBurstTime();

            // Simula processamento (Thread dorme pelo tempo total)
            Thread.sleep(tempoParaExecutar);

            // Zera o tempo restante (pois executou tudo)
            atual.setBurstTime(0);

            // NOTIFICA FIM (Salva bloco azul no histórico)
            if (observer != null) observer.notificarFimExecucao(atual);

            // Como é não-preemptivo, se ele rodou completamente, ele acabou
            if (observer != null) observer.notificarConclusao(atual);

            return true;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void shortestRemainingTimeFirst(){}
    public void priorityNonPreemptive(){}
}