package org.example.model;

import java.util.List;

public class Medico implements Runnable {

    private static List<Paciente> pacientes;
    private static int mode;

    public void run() {
        switch (mode) {
            case 1:
                roundRobin();
                break;
            default:
                break;
        }
    }

    public void roundRobin(){

    }



}
