import simulation.Environment;

import javax.swing.*;

public class Boids {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Environment e = new Environment(5);
            e.display();
            e.start();
        });
    }
}