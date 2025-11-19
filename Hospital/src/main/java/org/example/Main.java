package org.example;


import org.example.view.TelaConfiguracaoGeral;
import org.example.view.TelaCriacaoPaciente;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TelaConfiguracaoGeral().setVisible(true);
            // new TelaCriacaoPaciente().setVisible(true);
        });
    }
}