package org.example.view;

import org.example.model.Paciente;

public interface SimulacaoObserver {
    void notificarInicioExecucao(Paciente p);
    void notificarFimExecucao(Paciente p);
    void notificarConclusao(Paciente p);
}