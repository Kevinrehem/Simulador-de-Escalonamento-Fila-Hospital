package org.example.view;

import org.example.model.Paciente;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TelaSimulacao extends JFrame implements SimulacaoObserver {

    // Dados para o Gráfico
    private final List<Paciente> todosPacientes;
    private Map<Paciente, List<Intervalo>> historicoExecucao = new ConcurrentHashMap<>();
    private Map<Paciente, Long> inicioAtual = new ConcurrentHashMap<>();

    // Controle de Estado da Simulação
    private int qtdMedicos;
    private long tempoInicioSimulacao;
    private AtomicInteger totalTrocasContexto = new AtomicInteger(0);
    private AtomicInteger pacientesConcluidos = new AtomicInteger(0); // Contador de conclusões
    private volatile boolean isRunning = true; // Flag para saber se a simulação ainda roda
    private long tempoFinalSimulacao = 0;      // Guarda o timestamp de quando acabou

    // Componentes Visuais
    private GanttPanel ganttPanel;
    private JScrollPane scrollPane;
    private JCheckBox chkAutoScroll;
    private JLabel lblWaitTime, lblTurnaround, lblContextSwitch, lblCpuUtil, lblStatus;

    private javax.swing.Timer timerRepintura;

    public TelaSimulacao(List<Paciente> pacientesOriginais, int qtdMedicos) {
        super("Simulação em Tempo Real - Monitoramento");
        this.qtdMedicos = qtdMedicos;

        this.todosPacientes = new ArrayList<>(pacientesOriginais);

        for (Paciente p : todosPacientes) {
            historicoExecucao.put(p, Collections.synchronizedList(new ArrayList<>()));
        }

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Painel de Métricas (Topo) ---
        JPanel pnlTop = new JPanel(new BorderLayout());

        // Status Bar
        lblStatus = new JLabel(" Status: SIMULANDO ", SwingConstants.CENTER);
        lblStatus.setOpaque(true);
        lblStatus.setBackground(new Color(255, 255, 224)); // Amarelo claro
        lblStatus.setFont(new Font("Arial", Font.BOLD, 12));
        pnlTop.add(lblStatus, BorderLayout.NORTH);

        JPanel pnlMetrics = new JPanel(new GridLayout(1, 4, 10, 10));
        pnlMetrics.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlMetrics.setBackground(new Color(240, 240, 240));

        lblWaitTime = createMetricLabel("Avg Wait Time: 0ms");
        lblTurnaround = createMetricLabel("Avg Turnaround: 0ms");
        lblContextSwitch = createMetricLabel("Context Switches: 0");
        lblCpuUtil = createMetricLabel("CPU Util: 0%");

        pnlMetrics.add(lblWaitTime);
        pnlMetrics.add(lblTurnaround);
        pnlMetrics.add(lblContextSwitch);
        pnlMetrics.add(lblCpuUtil);

        pnlTop.add(pnlMetrics, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        // --- Painel Gantt (Centro) ---
        ganttPanel = new GanttPanel();
        scrollPane = new JScrollPane(ganttPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // --- Rodapé ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chkAutoScroll = new JCheckBox("Acompanhar tempo real (Auto-scroll)", true);
        bottomPanel.add(chkAutoScroll);
        add(bottomPanel, BorderLayout.SOUTH);

        this.tempoInicioSimulacao = System.currentTimeMillis();

        // Timer de Atualização
        timerRepintura = new javax.swing.Timer(30, e -> atualizarInterface());
        timerRepintura.start();
    }

    private JLabel createMetricLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        l.setOpaque(true);
        l.setBackground(Color.WHITE);
        l.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return l;
    }

    // Método auxiliar para pegar o tempo atual (Congelado ou Real)
    private long getTempoReferencia() {
        return isRunning ? System.currentTimeMillis() : tempoFinalSimulacao;
    }

    private void atualizarInterface() {
        long agora = getTempoReferencia();
        long tempoDecorrido = agora - tempoInicioSimulacao;

        // 1. Calcular Métricas
        calcularMetricas(agora, tempoDecorrido);

        // 2. Redimensionar Painel (Para de crescer se isRunning = false)
        int larguraNecessaria = 100 + (int)(tempoDecorrido * ganttPanel.PIXELS_PER_MS) + 400;
        int alturaNecessaria = todosPacientes.size() * ganttPanel.ROW_HEIGHT + 50;

        // Só redimensiona se necessário (evita flicker quando parado)
        if (ganttPanel.getPreferredSize().width != larguraNecessaria) {
            ganttPanel.setPreferredSize(new Dimension(larguraNecessaria, alturaNecessaria));
            ganttPanel.revalidate();
        }

        // 3. Auto-Scroll (Só rola se estiver rodando)
        if (chkAutoScroll.isSelected() && isRunning) {
            JScrollBar hBar = scrollPane.getHorizontalScrollBar();
            int xCursor = 100 + (int)(tempoDecorrido * ganttPanel.PIXELS_PER_MS);
            int viewWidth = scrollPane.getViewport().getWidth();
            int currentPos = hBar.getValue();

            if (xCursor > currentPos + viewWidth - 50) {
                hBar.setValue(xCursor - viewWidth + 150);
            }
        }
        ganttPanel.repaint();
    }

    private void calcularMetricas(long agora, long tempoDecorrido) {
        double totalWait = 0;
        double totalTurnaround = 0;
        long totalExecutionTimeAllCores = 0;
        int arrivedCount = 0;

        for (Paciente p : todosPacientes) {
            if (p.getArrivalTime() > tempoDecorrido) continue;
            arrivedCount++;

            long executadoPeloPaciente = 0;
            List<Intervalo> intervalos = historicoExecucao.get(p);
            synchronized (intervalos) {
                for (Intervalo i : intervalos) {
                    executadoPeloPaciente += (i.end - i.start);
                }
            }
            if (inicioAtual.containsKey(p)) {
                executadoPeloPaciente += (agora - inicioAtual.get(p));
            }

            totalExecutionTimeAllCores += executadoPeloPaciente;

            // Turnaround
            long turnaround;
            // Se já acabou (não está rodando e burst zerou), o turnaround trava no último fim
            boolean finalizado = !inicioAtual.containsKey(p) && p.getBurstTime() <= 0;

            if (finalizado && !intervalos.isEmpty()) {
                long ultimoFim = intervalos.get(intervalos.size()-1).end;
                turnaround = (ultimoFim - tempoInicioSimulacao) - p.getArrivalTime();
            } else {
                turnaround = tempoDecorrido - p.getArrivalTime();
            }

            long wait = turnaround - executadoPeloPaciente;
            if (wait < 0) wait = 0;

            totalWait += wait;
            totalTurnaround += turnaround;
        }

        if (arrivedCount > 0) {
            lblWaitTime.setText(String.format("Avg Wait: %.0f ms", totalWait / arrivedCount));
            lblTurnaround.setText(String.format("Avg Turnaround: %.0f ms", totalTurnaround / arrivedCount));
        }

        lblContextSwitch.setText("Context Switches: " + totalTrocasContexto.get());

        if (tempoDecorrido > 0 && qtdMedicos > 0) {
            double util = (double) totalExecutionTimeAllCores / (tempoDecorrido * qtdMedicos) * 100.0;
            if (util > 100.0) util = 100.0;
            lblCpuUtil.setText(String.format("CPU Util: %.1f%%", util));
        }
    }

    // --- Observer Methods ---
    @Override
    public void notificarInicioExecucao(Paciente p) {
        if (!isRunning) return;
        inicioAtual.put(p, System.currentTimeMillis());
        totalTrocasContexto.incrementAndGet();
    }

    @Override
    public void notificarFimExecucao(Paciente p) {
        Long inicio = inicioAtual.remove(p);
        if (inicio != null) {
            long fim = System.currentTimeMillis();
            historicoExecucao.get(p).add(new Intervalo(inicio, fim));
        }
    }

    @Override
    public void notificarConclusao(Paciente p) {
        // Incrementa contador de concluídos de forma thread-safe
        int concluidos = pacientesConcluidos.incrementAndGet();

        // Verifica se TODOS terminaram
        if (concluidos >= todosPacientes.size()) {
            finalizarSimulacao();
        }
    }

    private void finalizarSimulacao() {
        // Marca timestamp final e flag
        tempoFinalSimulacao = System.currentTimeMillis();
        isRunning = false;

        // Atualiza visual na Thread da UI
        SwingUtilities.invokeLater(() -> {
            timerRepintura.stop(); // Para o loop
            atualizarInterface();  // Desenha o estado final travado
            lblStatus.setText(" Status: FINALIZADO ");
            lblStatus.setBackground(new Color(144, 238, 144)); // Verde claro
            JOptionPane.showMessageDialog(this, "Simulação Concluída com Sucesso!");
        });
    }

    // --- Classes Auxiliares ---
    private static class Intervalo {
        long start;
        long end;
        public Intervalo(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    private class GanttPanel extends JPanel {
        final int ROW_HEIGHT = 40;
        final int HEADER_WIDTH = 100;
        final double PIXELS_PER_MS = 0.1;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Usa o tempo congelado se acabou, ou tempo real se rodando
            long agora = getTempoReferencia();
            long tempoDecorrido = agora - tempoInicioSimulacao;

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            for (int i = 0; i < todosPacientes.size(); i++) {
                Paciente p = todosPacientes.get(i);
                int y = i * ROW_HEIGHT + 10;

                // Linhas
                g2.setColor(new Color(245, 245, 245));
                g2.fillRect(0, y, getWidth(), ROW_HEIGHT);
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(0, y + ROW_HEIGHT, getWidth(), y + ROW_HEIGHT);

                // Texto
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.drawString(p.getName(), 10, y + 25);

                g2.drawLine(HEADER_WIDTH, 0, HEADER_WIDTH, getHeight());

                long arrivalTimeMs = p.getArrivalTime();
                int xArrival = HEADER_WIDTH + (int)(arrivalTimeMs * PIXELS_PER_MS);

                // Retângulo de Espera
                if (tempoDecorrido >= arrivalTimeMs) {
                    int xAtual = HEADER_WIDTH + (int)(tempoDecorrido * PIXELS_PER_MS);

                    // Se finalizado, espera trava no fim do ultimo intervalo
                    long fimWait = tempoDecorrido;
                    List<Intervalo> intervalos = historicoExecucao.get(p);

                    // Lógica visual para parar a barra cinza quando o processo termina
                    boolean pFinalizado = !inicioAtual.containsKey(p) && p.getBurstTime() <= 0;
                    if (pFinalizado && !intervalos.isEmpty()) {
                        long lastEnd = intervalos.get(intervalos.size()-1).end - tempoInicioSimulacao;
                        if (lastEnd < fimWait) fimWait = lastEnd;
                    }

                    int xEndWait = HEADER_WIDTH + (int)(fimWait * PIXELS_PER_MS);
                    int widthWait = xEndWait - xArrival;

                    if (widthWait > 0) {
                        g2.setColor(new Color(220, 220, 220));
                        g2.fillRect(xArrival, y + 5, widthWait, ROW_HEIGHT - 10);
                    }
                }

                // Histórico (Azul)
                List<Intervalo> intervalos = historicoExecucao.get(p);
                synchronized (intervalos) {
                    for (Intervalo interv : intervalos) {
                        long tStart = interv.start - tempoInicioSimulacao;
                        long tEnd = interv.end - tempoInicioSimulacao;

                        int xStart = HEADER_WIDTH + (int) (tStart * PIXELS_PER_MS);
                        int width = (int) ((tEnd - tStart) * PIXELS_PER_MS);

                        g2.setColor(new Color(70, 130, 180));
                        g2.fillRect(xStart, y + 5, width, ROW_HEIGHT - 10);
                    }
                }

                // Execução Atual (Verde)
                if (inicioAtual.containsKey(p)) {
                    long tStart = inicioAtual.get(p) - tempoInicioSimulacao;
                    long tEnd = agora - tempoInicioSimulacao;

                    int xStart = HEADER_WIDTH + (int) (tStart * PIXELS_PER_MS);
                    int width = (int) ((tEnd - tStart) * PIXELS_PER_MS);

                    g2.setColor(new Color(50, 205, 50));
                    g2.fillRect(xStart, y + 5, width, ROW_HEIGHT - 10);
                }
            }

            // Cursor Vermelho
            int xCursor = HEADER_WIDTH + (int)(tempoDecorrido * PIXELS_PER_MS);
            g2.setColor(isRunning ? Color.RED : Color.DARK_GRAY); // Fica cinza escuro quando termina
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(xCursor, 0, xCursor, getHeight());
        }
    }
}