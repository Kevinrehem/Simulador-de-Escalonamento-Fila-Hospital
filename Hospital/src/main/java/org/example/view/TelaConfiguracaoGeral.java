package org.example.view;

import org.example.model.Medico;
import org.example.model.Paciente;
import org.example.model.enums.AlgoritmoEscalonamento;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TelaConfiguracaoGeral extends JFrame {

    // Componente de Cenário
    private JComboBox<String> cbCenario;

    // Componentes de Geração
    private JSpinner spQtdPacientes;
    private JSpinner spArrivalMin, spArrivalMax;
    private JSpinner spBurstMin, spBurstMax;
    private JSpinner spPrioridadeMin, spPrioridadeMax;
    private JSpinner spQuantumMin, spQuantumMax;

    // Componentes de Simulação
    private JComboBox<Integer> cbQtdMedicos;
    private JComboBox<AlgoritmoEscalonamento> cbAlgoritmo;

    // Tabela e Dados
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Paciente> listaPacientes = new ArrayList<>();

    public TelaConfiguracaoGeral() {
        super("Configurador do Hospital - Simulador de Escalonamento");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- PAINEL ESQUERDO (Controles) ---
        JPanel pnlControles = new JPanel();
        pnlControles.setLayout(new BoxLayout(pnlControles, BoxLayout.Y_AXIS));
        pnlControles.setPreferredSize(new Dimension(340, 0)); // Aumentei um pouco a largura
        pnlControles.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Seção 1: Gerador de Carga
        JPanel pnlGerador = criarPainelGeracao();

        // Seção 2: Configuração do Sistema
        JPanel pnlConfig = criarPainelConfiguracao();

        // Botão Gerar
        JButton btnGerar = new JButton("Gerar / Atualizar Cenário");
        estilizarBotao(btnGerar, new Color(70, 130, 180), Color.WHITE);
        btnGerar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGerar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnGerar.addActionListener(e -> gerarPacientes());

        pnlControles.add(pnlGerador);
        pnlControles.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlControles.add(btnGerar);
        pnlControles.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlControles.add(pnlConfig);
        pnlControles.add(Box.createVerticalGlue());

        add(pnlControles, BorderLayout.WEST);

        // --- PAINEL CENTRAL (Tabela) ---
        JPanel pnlTabela = new JPanel(new BorderLayout());
        pnlTabela.setBorder(new EmptyBorder(10, 0, 10, 10));

        String[] colunas = {"#", "Chegada (s)", "Burst (s)", "Prioridade", "Quantum (s)"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel lblTabela = new JLabel("Visualização da Fila de Processos (Pacientes)");
        lblTabela.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTabela.setBorder(new EmptyBorder(0, 0, 10, 0));

        pnlTabela.add(lblTabela, BorderLayout.NORTH);
        pnlTabela.add(scrollPane, BorderLayout.CENTER);

        add(pnlTabela, BorderLayout.CENTER);

        // --- RODAPÉ (Ação Final) ---
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setBackground(new Color(245, 245, 245));
        pnlFooter.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton btnIniciar = new JButton("INICIAR SIMULAÇÃO");
        estilizarBotao(btnIniciar, new Color(34, 139, 34), Color.WHITE);
        btnIniciar.setPreferredSize(new Dimension(200, 45));
        btnIniciar.setFont(new Font("Segoe UI", Font.BOLD, 14));

        btnIniciar.addActionListener(e -> iniciarSimulacao());

        pnlFooter.add(btnIniciar);
        add(pnlFooter, BorderLayout.SOUTH);

        // Inicializa com o Cenário 1 selecionado por padrão
        cbCenario.setSelectedIndex(0);
    }

    // --- MÉTODOS DE MONTAGEM DE UI ---

    private JPanel criarPainelGeracao() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(createTitledBorder("1. Definição do Cenário e Pacientes"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);
        gc.weightx = 1;
        gc.gridx = 0;
        gc.gridy = 0;

        // --- NOVO DROPDOWN DE CENÁRIOS ---
        p.add(new JLabel("Selecionar Cenário:"), gc);
        String[] cenarios = {
                "Cenário 1: Emergência Crítica",
                "Cenário 2: Plantão Lotado",
                "Cenário 3: Hospital Moderno",
                "Personalizado"
        };
        cbCenario = new JComboBox<>(cenarios);
        gc.gridx = 1;
        p.add(cbCenario, gc);

        // Listener para preencher dados automaticamente
        cbCenario.addActionListener(e -> aplicarCenarioSelecionado());

        // --- SPINNERS (Abaixo do cenário) ---

        // Qtd
        gc.gridy++; gc.gridx = 0;
        p.add(new JLabel("Quantidade:"), gc);
        spQtdPacientes = new JSpinner(new SpinnerNumberModel(10, 1, 10000, 1));
        gc.gridx = 1;
        p.add(spQtdPacientes, gc);

        // Arrival
        gc.gridy++; gc.gridx = 0;
        p.add(new JLabel("Chegada (s):"), gc);
        gc.gridx = 1;
        spArrivalMin = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        spArrivalMax = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
        p.add(createRangePanel(spArrivalMin, spArrivalMax), gc);

        // Burst
        gc.gridy++; gc.gridx = 0;
        p.add(new JLabel("Duração (s):"), gc);
        gc.gridx = 1;
        spBurstMin = new JSpinner(new SpinnerNumberModel(2, 1, 1000, 1));
        spBurstMax = new JSpinner(new SpinnerNumberModel(8, 1, 1000, 1));
        p.add(createRangePanel(spBurstMin, spBurstMax), gc);

        // Prioridade
        gc.gridy++; gc.gridx = 0;
        p.add(new JLabel("Prioridade (1-5):"), gc);
        gc.gridx = 1;
        spPrioridadeMin = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        spPrioridadeMax = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        p.add(createRangePanel(spPrioridadeMin, spPrioridadeMax), gc);

        // Quantum
        gc.gridy++; gc.gridx = 0;
        p.add(new JLabel("Quantum (s):"), gc);
        gc.gridx = 1;
        spQuantumMin = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        spQuantumMax = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        p.add(createRangePanel(spQuantumMin, spQuantumMax), gc);

        return p;
    }

    private JPanel criarPainelConfiguracao() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(createTitledBorder("2. Recursos do Hospital"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 5, 10, 5);
        gc.weightx = 1;
        gc.gridx = 0;
        gc.gridy = 0;

        p.add(new JLabel("Número de Médicos (Cores):"), gc);
        gc.gridy++;
        cbQtdMedicos = new JComboBox<>(new Integer[]{1, 2, 4});
        p.add(cbQtdMedicos, gc);

        gc.gridy++;
        p.add(new JLabel("Algoritmo de Escalonamento:"), gc);
        gc.gridy++;
        cbAlgoritmo = new JComboBox<>(AlgoritmoEscalonamento.values());
        p.add(cbAlgoritmo, gc);

        return p;
    }

    // --- LÓGICA DOS CENÁRIOS ---

    private void aplicarCenarioSelecionado() {
        int index = cbCenario.getSelectedIndex();

        // Lógica baseada na descrição do seu trabalho
        switch (index) {
            case 0: // Cenário 1 – Emergência Crítica
                // Configuração: 1 Médico. Poucos pacientes. Prioridades muito diferentes.
                cbQtdMedicos.setSelectedItem(1);
                spQtdPacientes.setValue(5);

                // Chegam quase juntos
                spArrivalMin.setValue(0); spArrivalMax.setValue(2);
                // Tempos variados
                spBurstMin.setValue(3); spBurstMax.setValue(8);
                // Prioridade Crítica (1) até Baixa (5)
                spPrioridadeMin.setValue(1); spPrioridadeMax.setValue(5);
                // Quantum padrão
                spQuantumMin.setValue(2); spQuantumMax.setValue(2);
                break;

            case 1: // Cenário 2 – Plantão Lotado
                // Configuração: 2 Médicos. Muitos pacientes. Chegando continuamente.
                cbQtdMedicos.setSelectedItem(2);
                spQtdPacientes.setValue(25); // Carga alta

                // Chegada contínua (espalhada ao longo do tempo)
                spArrivalMin.setValue(0); spArrivalMax.setValue(20);
                // Tempos variados
                spBurstMin.setValue(2); spBurstMax.setValue(6);
                // Prioridade variada
                spPrioridadeMin.setValue(1); spPrioridadeMax.setValue(5);
                spQuantumMin.setValue(2); spQuantumMax.setValue(2);
                break;

            case 2: // Cenário 3 – Hospital Moderno
                // Configuração: 4 Médicos. Random. Burst e Prioridades diferentes.
                cbQtdMedicos.setSelectedItem(4);
                spQtdPacientes.setValue(40); // Bastante gente pra testar 4 cores

                // Chegada aleatória
                spArrivalMin.setValue(0); spArrivalMax.setValue(15);
                // Burst Times bem variados (rápidos e demorados)
                spBurstMin.setValue(1); spBurstMax.setValue(12);
                spPrioridadeMin.setValue(1); spPrioridadeMax.setValue(5);
                spQuantumMin.setValue(3); spQuantumMax.setValue(3);
                break;

            case 3: // Personalizado
                break;
        }

        // Se não for personalizado, já gera os dados para o usuário ver o resultado
        if (index != 3) {
            gerarPacientes();
        }
    }

    private JPanel createRangePanel(JSpinner min, JSpinner max) {
        JPanel p = new JPanel(new GridLayout(1, 2, 5, 0));
        p.add(min);
        p.add(max);
        return p;
    }

    private TitledBorder createTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), title);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        return border;
    }

    private void gerarPacientes() {
        // 1. Obter Valores
        int qtd = (Integer) spQtdPacientes.getValue();
        int aMin = (Integer) spArrivalMin.getValue();
        int aMax = (Integer) spArrivalMax.getValue();
        int bMin = (Integer) spBurstMin.getValue();
        int bMax = (Integer) spBurstMax.getValue();
        int pMin = (Integer) spPrioridadeMin.getValue();
        int pMax = (Integer) spPrioridadeMax.getValue();
        int qMin = (Integer) spQuantumMin.getValue();
        int qMax = (Integer) spQuantumMax.getValue();

        // Validação
        if (aMin > aMax || bMin > bMax || pMin > pMax || qMin > qMax) {
            JOptionPane.showMessageDialog(this, "Valores mínimos não podem ser maiores que os máximos.", "Erro na Geração", JOptionPane.ERROR_MESSAGE);
            return;
        }

        listaPacientes.clear();
        Random random = new Random();

        for (int i = 0; i < qtd; i++) {
            int arrival = (random.nextInt(aMax - aMin + 1) + aMin) * 1000;
            int burst = (random.nextInt(bMax - bMin + 1) + bMin) * 1000;
            int priority = random.nextInt(pMax - pMin + 1) + pMin;
            int quantum = (random.nextInt(qMax - qMin + 1) + qMin) * 1000;

            listaPacientes.add(new Paciente("P" + (i + 1), arrival, burst, priority, quantum));
        }

        // Ordena por tempo de chegada (para ficar bonitinho na tabela)
        Collections.sort(listaPacientes);

        // Renomeia para ficar em ordem P1, P2 na tabela visual (opcional)
        for(int i=0; i<listaPacientes.size(); i++) {
            listaPacientes.get(i).setName("P" + (i+1));
        }

        atualizarTabela();
    }

    private void atualizarTabela() {
        tableModel.setRowCount(0);
        for (Paciente p : listaPacientes) {
            tableModel.addRow(new Object[]{
                    p.getName(),
                    String.format("%.1f", p.getArrivalTime() / 1000.0),
                    String.format("%.1f", p.getBurstTime() / 1000.0),
                    p.getPriority(),
                    String.format("%.1f", p.getQuantum() / 1000.0)
            });
        }
    }

    private void iniciarSimulacao() {
        if (listaPacientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gere os pacientes primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qtdMedicos = (Integer) cbQtdMedicos.getSelectedItem();
        AlgoritmoEscalonamento algoritmo = (AlgoritmoEscalonamento) cbAlgoritmo.getSelectedItem();

        TelaSimulacao telaSimulacao = new TelaSimulacao(listaPacientes, qtdMedicos);
        telaSimulacao.setVisible(true);

        Thread[] medicos = new Thread[qtdMedicos];
        for (int i = 0; i < qtdMedicos; i++) {
            medicos[i] = new Thread(new Medico(listaPacientes, algoritmo, telaSimulacao));
            medicos[i].start();
        }

        dispose();
    }

    private void estilizarBotao(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                new EmptyBorder(5, 15, 5, 15)
        ));
    }
}