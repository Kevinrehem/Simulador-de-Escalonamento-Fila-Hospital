package org.example.model;

public class Paciente implements Comparable<Paciente> {
    private String name;
    private int arrivalTime;
    private int burstTime;
    private int priority;
    private int quantum;
    private boolean firstRodeo;

    public Paciente(String name, int arrivalTime, int burstTime, int priority, int quantum) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.quantum = quantum;
        this.firstRodeo = true;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getQuantum() {
        return quantum;
    }

    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public int compareTo(Paciente o) {
        return this.getArrivalTime()-o.getArrivalTime();
    }

    public boolean isFirstRodeo() {
        return firstRodeo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFirstRodeo(boolean firstRodeo) {
        this.firstRodeo = firstRodeo;
    }
}
