package org.example.view;

import org.example.model.Medico;
import org.example.model.Paciente;
import org.example.model.enums.AlgoritmoEscalonamento;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DialogConfigsMedico extends JDialog {

    private JComboBox<Integer> cbQtdMedicos;
    private JComboBox<AlgoritmoEscalonamento> cbAlgoritmo;
    private List<Paciente> pacientes;
    private Frame owner;

    public DialogConfigsMedico(Frame owner, List<Paciente> pacientes) {
        super(owner, "Configuração dos Médicos", true);
        this.owner = owner;
        this.pacientes = pacientes;

        setSize(400, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0;
        content.add(new JLabel("Quantidade de Médicos:"), gc);
        gc.gridx = 1;
        cbQtdMedicos = new JComboBox<>(new Integer[]{1, 2, 4});
        content.add(cbQtdMedicos, gc);

        gc.gridx = 0; gc.gridy = 1;
        content.add(new JLabel("Algoritmo de Escalonamento:"), gc);
        gc.gridx = 1;
        cbAlgoritmo = new JComboBox<>(AlgoritmoEscalonamento.values());
        content.add(cbAlgoritmo, gc);

        add(content, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnIniciar = new JButton("Iniciar Simulação");

        btnCancelar.addActionListener(e -> dispose());
        btnIniciar.addActionListener(e -> iniciarSimulacao());

        footer.add(btnCancelar);
        footer.add(btnIniciar);
        add(footer, BorderLayout.SOUTH);
    }

    private void iniciarSimulacao() {
        int qtdMedicos = (Integer) cbQtdMedicos.getSelectedItem();
        AlgoritmoEscalonamento algoritmo = (AlgoritmoEscalonamento) cbAlgoritmo.getSelectedItem();

        TelaSimulacao telaSimulacao = new TelaSimulacao(pacientes, qtdMedicos);
        telaSimulacao.setVisible(true);

        Thread medicos[] = new Thread[qtdMedicos];
        for(int i=0; i<qtdMedicos; i++){
            medicos[i] = new Thread(new Medico(pacientes, algoritmo, telaSimulacao));
            medicos[i].start();
        }

        dispose();
    }
}