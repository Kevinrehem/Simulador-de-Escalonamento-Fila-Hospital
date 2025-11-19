package org.example.model;

import org.example.model.enums.AlgoritmoEscalonamento;
import org.example.view.SimulacaoObserver;

import java.util.List;

public class Medico implements Runnable {

    private static List<Paciente> pacientes;
    private static AlgoritmoEscalonamento algoritmoEscalonamento;

    // Adicionado para comunicação com a UI
    private static SimulacaoObserver observer;

    public Medico(List<Paciente> pacientes, AlgoritmoEscalonamento algoritmoEscalonamento, SimulacaoObserver observer) {
        Medico.pacientes = pacientes;
        Medico.algoritmoEscalonamento = algoritmoEscalonamento;
        Medico.observer = observer;
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
                // Outros casos...
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

    public void shortestJobFirst(){}
    public void shortestRemainingTimeFirst(){}
    public void priorityNonPreemptive(){}
}