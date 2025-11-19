package org.example.view;

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

    public DialogConfigsMedico(Frame owner, List<Paciente> pacientes) {
        super(owner, "Configuração dos Médicos", true); // 'true' torna o dialog Modal
        this.pacientes = pacientes;

        setSize(400, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // --- Seleção de Quantidade de Médicos ---
        gc.gridx = 0;
        gc.gridy = 0;
        content.add(new JLabel("Quantidade de Médicos:"), gc);

        gc.gridx = 1;
        // Dropdown com opções fixas: 1, 2 ou 4
        cbQtdMedicos = new JComboBox<>(new Integer[]{1, 2, 4});
        content.add(cbQtdMedicos, gc);

        // --- Seleção de Algoritmo ---
        gc.gridx = 0;
        gc.gridy = 1;
        content.add(new JLabel("Algoritmo de Escalonamento:"), gc);

        gc.gridx = 1;
        // Dropdown com os valores do Enum
        cbAlgoritmo = new JComboBox<>(AlgoritmoEscalonamento.values());
        content.add(cbAlgoritmo, gc);

        add(content, BorderLayout.CENTER);

        // --- Botões de Ação ---
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

        // TODO: AQUI VOCÊ CHAMA A LÓGICA DE EXECUÇÃO
        // Exemplo de log para teste:
        System.out.println("Iniciando simulação...");
        System.out.println("Pacientes carregados: " + pacientes.size());
        System.out.println("Médicos: " + qtdMedicos);
        System.out.println("Algoritmo: " + algoritmo);



        JOptionPane.showMessageDialog(this,
                "Simulação iniciada com " + qtdMedicos + " médico(s) \nAlgoritmo: " + algoritmo,
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }
}