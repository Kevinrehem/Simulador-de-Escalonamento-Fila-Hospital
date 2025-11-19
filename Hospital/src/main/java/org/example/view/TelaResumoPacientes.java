package org.example.view;

import org.example.model.Paciente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaResumoPacientes extends JFrame {

    private JTable table;
    // Adicionado para persistir a lista e passar para a próxima tela
    private List<Paciente> listaPacientes;

    public TelaResumoPacientes(List<Paciente> pacientes) {
        super("Resumo de Pacientes");
        this.listaPacientes = pacientes; // Guarda a referência

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

        // LÓGICA ALTERADA AQUI:
        btnContinuar.addActionListener(e -> {
            // Abre o dialog de configuração passando esta janela como pai e a lista de pacientes
            DialogConfigsMedico dialog = new DialogConfigsMedico(this, this.listaPacientes);
            dialog.setVisible(true);

            // Opcional: fechar a tela de resumo após abrir a próxima
            dispose();
        });

        footer.add(btnCancelar);
        footer.add(btnContinuar);
        add(footer, BorderLayout.SOUTH);
    }
}