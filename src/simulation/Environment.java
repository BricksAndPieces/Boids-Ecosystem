package simulation;

import internal.Boid;
import internal.Food;
import internal.quadtree.QuadTree;
import internal.quadtree.Vector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("All")
public class Environment extends JPanel {

    private static final int smallestBoid = 3;

    private int species;
    private boolean spawn = false;
    private boolean clear = false;
    private int spawnSize = smallestBoid;

    private String mode = "Flocking";
    private final Options options = new Options();

    private final Timer timer = new Timer(50, x -> repaint());
    private final List<List<Boid>> boids = new ArrayList<>();
    private final List<Boid> boidQueue = new ArrayList<>();

    private List<QuadTree> quadTrees = new ArrayList<>();
    private boolean showQuads = false;
    private boolean showInfo = true;

    public Environment(int species) {
        this.species = species;
        setBackground(Color.black);
        setFocusable(true);
        add(options);

        for(int i = 0; i < species+1; i++)
            boids.add(new ArrayList<>());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                spawn = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                requestFocus();
                spawn = false;
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    options.flip();
                    boids.forEach(List::clear);
                    spawnSize = smallestBoid;
                    mode = options.inFlockingMode() ? "Flocking" : "Ecosystem";
                }

                if(e.getKeyCode() == KeyEvent.VK_Q) {
                    showQuads ^= true;
                }

                if(e.getKeyCode() == KeyEvent.VK_I) {
                    showInfo ^= true;
                }

                if(Boid.flockingMode) return;

                int num = getNum(e.getKeyCode());
                if(num == -1 || num >= boids.size()) return;
                spawnSize = num + smallestBoid-1;
            }
        });
    }

    public void display() {
        JFrame frame = new JFrame("FoodChain");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);

        frame.setBackground(Color.black);
        frame.add(this, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D)graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        updateLists();
        updateBoidLists();
        simulateBoids(g);
        spawnBoids();

        if(showQuads) {
            g.setColor(new Color(255, 255, 255, 50));
            quadTrees.forEach(z -> z.draw(g));
        }

        if(showInfo) {
            Font font = new Font("TimesRoman", Font.BOLD, 20);
            g.setColor(Color.white);
            g.setFont(font);

            int height = (int) (g.getFontMetrics().getHeight() * 1.1);
            int count = boids.stream().mapToInt(species -> species.size()).sum();
            g.drawString("Selected: " + (spawnSize-smallestBoid+1), 10, getHeight() - height*3);
            g.drawString("Entities: " + count, 10, getHeight() - height * 2);
            g.drawString("Mode: " + mode, 10, getHeight() - height);
        }
    }

    private void simulateBoids(Graphics2D g) {
        boids.forEach(species -> species.forEach(boid ->
            boid.run(g, getWidth(), getHeight())
        ));
    }

    private void updateBoidLists() {
        List<QuadTree> quadTrees = new ArrayList<>();
        for(List<Boid> species : boids) {
            QuadTree qt = new QuadTree(0, 0, getWidth()-1, getHeight()-1, 1);
            for(Boid b : species) qt.insert(b);
            quadTrees.add(qt);
        }

        for(int i = 1; i < boids.size() - 1; i++) {
            for(Boid b : boids.get(i)) {
                Vector p1 = new Vector(b.getPosition().x - 50, b.getPosition().y - 50);
                Vector p2 = new Vector(b.getPosition().x + 50, b.getPosition().y + 50);
                b.setPrey(quadTrees.get(i-1).query(p1, p2));
                b.setFlock(quadTrees.get(i).query(p1, p2));
                b.setPredators(quadTrees.get(i+1).query(p1, p2));
            }
        }

        for(Boid b : boids.get(boids.size()-1)) {
            Vector p1 = new Vector(b.getPosition().x - 50, b.getPosition().y - 50);
            Vector p2 = new Vector(b.getPosition().x + 50, b.getPosition().y + 50);
            b.setPrey(quadTrees.get(boids.size()-2).query(p1, p2));
            b.setFlock(quadTrees.get(boids.size()-1).query(p1, p2));
        }

        this.quadTrees = quadTrees;
    }

    private void updateLists() {
        if(options.inFlockingMode())
            return;

        boids.forEach(species -> species.removeIf(Boid::isDead));
        boidQueue.forEach(boid -> boids.get(boid.getSize()-smallestBoid+1).add(boid));
        boidQueue.clear();

        for(int i = 0; i < species+3 && boids.get(0).size() < 500; i++)
            boids.get(0).add(new Food(Math.random() * getWidth(), Math.random() * getHeight()));
    }

    private void spawnBoids() {
        if(spawn) try {
            boids.get(spawnSize - smallestBoid+1).add(createBoid(getMousePosition().x, getMousePosition().y, spawnSize));
        }catch(NullPointerException e) { };
    }

    private Boid createBoid(double x, double y, int size) {
        Boid b = new Boid(x, y, size, this);
//        b.setPrey(getList(size - smallestBoid));
//        b.setFlock(getList(size - smallestBoid + 1));
//        b.setPredators(getList(size - smallestBoid + 2));
        return b;
    }

    private List<Boid> getList(int index) {
        if(index < 0 || index >= boids.size())
            return null;

        return boids.get(index);
    }

    private int getNum(int code) {
        switch(code) {
            case KeyEvent.VK_1 : return 1;
            case KeyEvent.VK_2 : return 2;
            case KeyEvent.VK_3 : return 3;
            case KeyEvent.VK_4 : return 4;
            case KeyEvent.VK_5 : return 5;
            case KeyEvent.VK_6 : return 6;
            case KeyEvent.VK_7 : return 7;
            case KeyEvent.VK_8 : return 8;
            case KeyEvent.VK_9 : return 9;
        }

        return -1;
    }

    public void addBoid(Boid boid) {
        boidQueue.add(boid);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}