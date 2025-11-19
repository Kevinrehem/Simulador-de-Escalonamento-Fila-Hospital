package org.example;


import org.example.view.TelaCriacaoPaciente;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaCriacaoPaciente tela = new TelaCriacaoPaciente();
            tela.setVisible(true);
        });
    }
}