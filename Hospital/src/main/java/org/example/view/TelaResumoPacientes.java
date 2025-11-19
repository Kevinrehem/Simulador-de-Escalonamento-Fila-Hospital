package org.example.view;

import org.example.model.Paciente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaResumoPacientes extends JFrame {

    private JTable table;

    public TelaResumoPacientes(List<Paciente> pacientes) {
        super("Resumo de Pacientes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(720, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Modelo de tabela com os nomes das colunas solicitados
        String[] colunas = new String[] {
                "#",
                "Tempo de Chegada (s)",
                "Tempo de Execução (s)",
                "Quantum (s)",
                "Prioridade (1=Maior, 5=Menor)"
        };

        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // somente leitura
            }
        };

        // Preenche as linhas
        int idx = 1;
        for (Paciente p : pacientes) {
            model.addRow(new Object[] {
                    idx++,
                    String.format("%.3f", p.getArrivalTime() / 1000.0),
                    String.format("%.3f", p.getBurstTime() / 1000.0),
                    String.format("%.3f", p.getQuantum() / 1000.0),
                    p.getPriority()
            });
        }

        table = new JTable(model);
        table.setFillsViewportHeight(true);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Rodapé com botões Continuar e Cancelar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnContinuar = new JButton("Continuar");

        btnCancelar.addActionListener(e -> dispose());
        btnContinuar.addActionListener(e -> {
            // Aqui você pode acionar a próxima etapa do fluxo (ex: iniciar simulação)
            // Por ora, apenas fecha a janela
            dispose();
        });

        footer.add(btnCancelar);
        footer.add(btnContinuar);
        add(footer, BorderLayout.SOUTH);
    }
}
