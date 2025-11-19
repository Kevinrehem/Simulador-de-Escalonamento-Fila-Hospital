package org.example.view;


import org.example.model.Paciente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TelaCriacaoPaciente extends JFrame {
    private JSpinner spQtdPacientes;
    private JSpinner spArrivalMin;
    private JSpinner spArrivalMax;
    private JSpinner spBurstMin;
    private JSpinner spBurstMax;
    private JSpinner spQuantumMin;
    private JSpinner spQuantumMax;
    private JSpinner spPrioridadeMin;
    private JSpinner spPrioridadeMax;

    public TelaCriacaoPaciente() {
        super("Criação de Pacientes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy = 0;

        // Quantidade de pacientes
        spQtdPacientes = new JSpinner(new SpinnerNumberModel(10, 1, 10000, 1));
        addRow(content, gc, "Quantidade de pacientes:", spQtdPacientes);

        // Arrival time range (segundos)
        spArrivalMin = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
        spArrivalMax = new JSpinner(new SpinnerNumberModel(10, 0, 1_000_000, 1));
        addRangeRow(content, gc, "Tempo de chegada (s):", spArrivalMin, spArrivalMax);

        // Burst time range (segundos)
        spBurstMin = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        spBurstMax = new JSpinner(new SpinnerNumberModel(5, 1, 1_000_000, 1));
        addRangeRow(content, gc, "Tempo de execução (s):", spBurstMin, spBurstMax);

        // Quantum range (segundos)
        spQuantumMin = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        spQuantumMax = new JSpinner(new SpinnerNumberModel(3, 1, 1_000_000, 1));
        addRangeRow(content, gc, "Quantum (s):", spQuantumMin, spQuantumMax);

        // Prioridade (1 a 5) - 1 maior, 5 menor
        spPrioridadeMin = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        spPrioridadeMax = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        addRangeRow(content, gc, "Prioridade (1-5):", spPrioridadeMin, spPrioridadeMax);

        add(content, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGerar = new JButton("Gerar");
        btnCancelar.addActionListener(this::onCancelar);
        btnGerar.addActionListener(this::onGerar);
        buttons.add(btnCancelar);
        buttons.add(btnGerar);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel parent, GridBagConstraints gc, String label, JComponent comp) {
        gc.gridx = 0;
        gc.weightx = 0;
        parent.add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        parent.add(comp, gc);
        gc.gridy++;
    }

    private void addRangeRow(JPanel parent, GridBagConstraints gc, String label, JSpinner min, JSpinner max) {
        gc.gridx = 0;
        gc.weightx = 0;
        parent.add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        JPanel rangePanel = new JPanel(new GridLayout(1, 3, 8, 0));
        rangePanel.add(labeled(min, "Mín"));
        rangePanel.add(new JLabel("até", SwingConstants.CENTER));
        rangePanel.add(labeled(max, "Máx"));
        parent.add(rangePanel, gc);
        gc.gridy++;
    }

    private JPanel labeled(JComponent comp, String text) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.add(new JLabel(text + ":"), BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void onCancelar(ActionEvent e) {
        dispose();
    }

    private List<Paciente> onGerar(ActionEvent e) {
        // Validações básicas de range
        int qtd = (Integer) spQtdPacientes.getValue();
        int aMin = (Integer) spArrivalMin.getValue();
        int aMax = (Integer) spArrivalMax.getValue();
        int bMin = (Integer) spBurstMin.getValue();
        int bMax = (Integer) spBurstMax.getValue();
        int qMin = (Integer) spQuantumMin.getValue();
        int qMax = (Integer) spQuantumMax.getValue();
        int pMin = (Integer) spPrioridadeMin.getValue();
        int pMax = (Integer) spPrioridadeMax.getValue();

        if (aMin > aMax || bMin > bMax || qMin > qMax || pMin > pMax) {
            JOptionPane.showMessageDialog(this, "Valores mínimos não podem ser maiores que os máximos.", "Validação", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (pMin < 1 || pMax > 5) {
            JOptionPane.showMessageDialog(this, "Prioridade deve estar entre 1 e 5.", "Validação", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Random random = new Random();
        List<Paciente> pacientes = new ArrayList<>();
        for (int i = 0; i < qtd; i++) {
            pacientes.add(new Paciente(
                    (random.nextInt(Math.max(1, (aMax - aMin + 1))) + aMin) * 1000,
                    (random.nextInt(Math.max(1, (bMax - bMin + 1))) + bMin) * 1000,
                    (random.nextInt(Math.max(1, (pMax - pMin + 1))) + pMin),
                    (random.nextInt(Math.max(1, (qMax - qMin + 1))) + qMin) * 1000
            ));
        }

        // Abre a tela de resumo imediatamente com a tabela
        TelaResumoPacientes resumo = new TelaResumoPacientes(pacientes);
        resumo.setVisible(true);
        return pacientes;
    }
}
