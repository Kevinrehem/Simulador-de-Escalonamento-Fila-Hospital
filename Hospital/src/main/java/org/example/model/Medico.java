package org.example.model;

import org.example.model.enums.AlgoritmoEscalonamento;

import java.util.List;

public class Medico implements Runnable {

    private static List<Paciente> pacientes;
    private static AlgoritmoEscalonamento algoritmoEscalonamento;

    public Medico(List<Paciente> pacientes, AlgoritmoEscalonamento algoritmoEscalonamento) {
        this.pacientes = pacientes;
        this.algoritmoEscalonamento = algoritmoEscalonamento;
    }

    public void run() {
        switch (algoritmoEscalonamento) {
            case AlgoritmoEscalonamento.ROUND_ROBIN:
                roundRobin();
                break;
            case SHORTEST_JOB_FIRST:
                break;
            case SHORTEST_REMAINING_TIME_FIRST:
                break;
            case PRIORITY_NON_PREEMPTIVE:
                break;
        }
    }

    public void roundRobin(){
        //TODO... KEVIN
    }

    public void shortestJobFirst(){
        //TODO... RHUAN
    }
    public void shortestRemainingTimeFirst(){
        //TODO... HENRIQUE
    }

    public void priorityNonPreemptive(){
        //TODO... TONHÃO
    }

}
