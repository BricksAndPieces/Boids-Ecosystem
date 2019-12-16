package simulation;

import internal.Boid;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

@SuppressWarnings("All")
public class Options extends JPanel {

    // Constants
    private static final double separationWeight = Boid.separationWeight;
    private static final double alignmentWeight = Boid.alignmentWeight;
    private static final double cohesionWeight = Boid.cohesionWeight;
    private static final double noiseWeight = Boid.noiseWeight;

    // Labels
    private final JLabel sLabel = new JLabel("Separation");
    private final JLabel aLabel = new JLabel("Alignment");
    private final JLabel cLabel = new JLabel("Cohesion");
    private final JLabel nLabel = new JLabel("Noise");

    // Sliders
    private final JSlider sSlider = new JSlider(0, 50, (int) (separationWeight * 10));
    private final JSlider aSlider = new JSlider(0, 50, (int) (alignmentWeight * 10));
    private final JSlider cSlider = new JSlider(0, 50, (int) (cohesionWeight * 10));
    private final JSlider nSlider = new JSlider(0, 50, (int) (noiseWeight * 10));

    // Current mode
    private boolean flocking = true;

    public Options() {
        setBackground(new Color(0,0,0,0));

        Font font = new Font("TimesRoman", Font.BOLD, 15);
        sLabel.setForeground(Color.white);
        sLabel.setFont(font);
        aLabel.setForeground(Color.white);
        aLabel.setFont(font);
        cLabel.setForeground(Color.white);
        cLabel.setFont(font);
        nLabel.setForeground(Color.white);
        nLabel.setFont(font);

        sSlider.setUI(new CustomSliderUI(sSlider));
        aSlider.setUI(new CustomSliderUI(aSlider));
        cSlider.setUI(new CustomSliderUI(cSlider));
        nSlider.setUI(new CustomSliderUI(nSlider));

        sSlider.addChangeListener(e -> Boid.separationWeight = sSlider.getValue()/10.0);
        aSlider.addChangeListener(e -> Boid.alignmentWeight = aSlider.getValue()/10.0);
        cSlider.addChangeListener(e -> Boid.cohesionWeight = cSlider.getValue()/10.0);
        nSlider.addChangeListener(e -> Boid.noiseWeight = nSlider.getValue()/10.0);

        add(sLabel);
        add(sSlider);
        add(aLabel);
        add(aSlider);
        add(cLabel);
        add(cSlider);
        add(nLabel);
        add(nSlider);

        flocking();
    }

    public void flip() {
        flocking ^= true;
        Boid.flockingMode = flocking;
        if(flocking) flocking();
        else ecosystem();
    }

    private void flocking() {
        sLabel.setVisible(true);
        sSlider.setVisible(true);
        aLabel.setVisible(true);
        aSlider.setVisible(true);
        cLabel.setVisible(true);
        cSlider.setVisible(true);
        nLabel.setVisible(true);
        nSlider.setVisible(true);
    }

    private void ecosystem() {
        Boid.separationWeight = separationWeight;
        Boid.alignmentWeight = alignmentWeight;
        Boid.cohesionWeight = cohesionWeight;
        Boid.noiseWeight = noiseWeight;

        sSlider.setValue((int)(separationWeight*10));
        aSlider.setValue((int)(alignmentWeight*10));
        cSlider.setValue((int)(cohesionWeight*10));
        nSlider.setValue((int)(noiseWeight*10));

        sLabel.setVisible(false);
        sSlider.setVisible(false);
        aLabel.setVisible(false);
        aSlider.setVisible(false);
        cLabel.setVisible(false);
        cSlider.setVisible(false);
        nLabel.setVisible(false);
        nSlider.setVisible(false);
    }

    public boolean inFlockingMode() {
        return flocking;
    }

    private class CustomSliderUI extends BasicSliderUI {

        private CustomSliderUI(JSlider b) {
            super(b);
            b.setBackground(new Color(0,0,0,0));
        }

        @Override
        public void paintTrack(Graphics g) {
            ((Graphics2D)g).setStroke(new BasicStroke(2));
            int y = trackRect.y+(trackRect.height/2);

            g.setColor(Color.white);
            g.drawLine(trackRect.x, y, trackRect.x+trackRect.width, y);
        }

        @Override
        public void paintThumb(Graphics g) {
            g.setColor(Color.lightGray);
            g.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
        }
    }
}