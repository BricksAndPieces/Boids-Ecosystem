package internal;

import internal.Boid;

import java.awt.*;

@SuppressWarnings("All")
public class Food extends Boid {

    public Food(double x, double y) {
        super(x, y, 0, null);
        stopMovement();
    }

    @Override
    public void run(Graphics2D g, int w, int h) {
        g.setColor(Color.green);
        g.fillOval((int)getPosition().x, (int)getPosition().y, 2, 2);
    }
}