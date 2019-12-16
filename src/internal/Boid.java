package internal;

import internal.quadtree.Vector;
import simulation.Environment;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.PI;
import static java.lang.Math.random;

@SuppressWarnings("All")
public class Boid {

    private static final double maxVelocity = 10;
    private static final double maxForce = 0.5;
    private static final double viewRange = 50;
    private static final double viewAngle = PI*0.85;
    private static final int startingLife = 100;

    private static final int desiredSeparation = 25;
    private static final int desiredAlignment = 50;
    private static final int desiredCohesion = 50;
    private static final int desiredAvoidance = 50;
    private static final int desiredAttraction = 50;

    public static double separationWeight = 2.5;
    public static double alignmentWeight = 1.5;
    public static double cohesionWeight = 1.3;
    public static double avoidanceWeight = 3;
    public static double attractionWeight = 2;
    public static double noiseWeight = 0.5;

    public static boolean flockingMode = true;

    private static final Random rng = ThreadLocalRandom.current();
    private static Map<Integer, Shape> shapes = new HashMap<>();

    private int size;
    private boolean visible = true;
    private boolean dead = false;
    private float hue;
    private int life;

    private List<Boid> prey = null;
    private List<Boid> flock = null;
    private List<Boid> predators = null;

    private Vector position;
    private final Vector velocity = new Vector(rng.nextInt(5) - 2.5, rng.nextInt(5) - 2.5);
    private final Environment environment;

    public Boid(double x, double y, int size, Environment e) {
        this.position = new Vector(x, y);
        this.size = size;
        this.life = startingLife;
        this.environment = e;

        this.hue = flockingMode ? (float)Math.random() : size / 5f;
    }

    public void run(Graphics2D g, int w, int h) {
        calculateView();
        update();
        wrapAround(w, h);
        draw(g);
    }

    private void update() {
        velocity.add(separation(flock));
        velocity.add(alignment(flock));
        velocity.add(cohesion(flock));
        velocity.add(avoidance(predators));
        velocity.add(attraction(prey));
        velocity.add(noise());

        velocity.limit(maxVelocity);
        position.add(velocity);

        if(life-- == 0 && !flockingMode)
            dead = true;
    }

    private Vector separation(List<Boid> boids) {
        if(boids == null)
            return new Vector();

        Vector steer = new Vector();
        int count = 0;

        for(Boid b : boids) {
            if(!b.visible)
                continue;

            double d = Vector.dist(position, b.position);
            if(d < desiredSeparation) {
                Vector diff = Vector.sub(position, b.position);
                diff.normalize();
                diff.div(d);
                steer.add(diff);
                count++;
            }
        }

        if(count > 0)
            steer.div(count);

        if(steer.mag() > 0) {
            steer.normalize();
            steer.mult(maxVelocity);
            steer.sub(velocity);
            steer.limit(maxForce);
        }

        steer.mult(separationWeight);
        return steer;
    }

    private Vector alignment(List<Boid> boids) {
        if(boids == null)
            return new Vector();

        Vector steer = new Vector();
        int count = 0;

        for(Boid b : boids) {
            if(!b.visible)
                continue;

            double d = Vector.dist(position, b.position);
            if(d < desiredAlignment) {
                steer.add(b.velocity);
                count++;
            }
        }

        if(count > 0) {
            steer.div(count);
            steer.normalize();
            steer.mult(maxVelocity);
            steer.sub(velocity);
            steer.limit(maxForce);
        }

        steer.mult(alignmentWeight);
        return steer;
    }

    private Vector cohesion(List<Boid> boids) {
        if(boids == null)
            return new Vector();

        Vector target = new Vector();
        int count = 0;

        for(Boid b : boids) {
            if(!b.visible)
                continue;

            double d = Vector.dist(position, b.position);
            if ((d > 0) && (d < desiredCohesion)) {
                target.add(b.position);
                count++;
            }
        }

        if(count == 0) return target;
        else target.div(count);

        Vector steer = Vector.sub(target, position);
        steer.normalize();
        steer.mult(maxVelocity);
        steer.sub(velocity);
        steer.limit(maxForce);

        steer.mult(cohesionWeight);
        return steer;
    }

    private Vector attraction(List<Boid> boids) {
        if(boids == null)
            return new Vector();

        Vector target = new Vector();
        int count = 0;

        for(Boid b : boids) {
            if(!b.visible)
                continue;

            double d = Vector.dist(position, b.position);
            if ((d > 0) && (d < desiredAttraction)) {
                if(d < 20 && !b.dead && !flockingMode) {
                    b.dead = true;
                    incrementLife(b);
                }

                target.add(b.position);
                count++;
            }
        }

        if(count == 0) return target;
        else target.div(count);

        Vector steer = Vector.sub(target, position);
        steer.normalize();
        steer.mult(maxVelocity);
        steer.sub(velocity);
        steer.limit(maxForce);

        steer.mult(attractionWeight);
        return steer;
    }

    private Vector avoidance(List<Boid> boids) {
        if(boids == null)
            return new Vector();

        Vector steer = new Vector();
        int count = 0;

        for(Boid b : boids) {
            if(!b.visible)
                continue;

            double d = Vector.dist(position, b.position);
            if(d < desiredAvoidance) {
                Vector diff = Vector.sub(position, b.position);
                diff.normalize();
                diff.div(d);
                steer.add(diff);
                count++;
            }
        }

        if(count > 0)
            steer.div(count);

        if(steer.mag() > 0) {
            steer.normalize();
            steer.mult(maxVelocity);
            steer.sub(velocity);
            steer.limit(maxForce);
        }

        steer.mult(avoidanceWeight);
        return steer;
    }

    private Vector noise() {
        Vector steer = new Vector(random() * 2 - 1, random() * 2 - 1);
        steer.mult(noiseWeight);
        return steer;
    }

    private void incrementLife(Boid prey) {
        life += Math.max(prey.life, startingLife);
        if(life > startingLife * size) {
            environment.addBoid(new Boid(position.x, position.y, size, environment));
            life -= startingLife/2 * size;
        }
    }

    private void calculateView() {
        calculateView(prey);
        calculateView(flock);
        calculateView(predators);
    }

    private void calculateView(List<Boid> boids) {
        if(boids == null)
            return;

        for(Boid b : boids) {
            b.visible = false;
            if(b.equals(this))
                continue;

            // Too far away to be seen
            double d = Vector.dist(position, b.position);
            if(d > viewRange) continue;

            // Boid in view angle?
            Vector lineOfSight = Vector.sub(b.position, position);
            double angleOfSight = Vector.angleBetween(lineOfSight, velocity);
            if(angleOfSight < viewAngle)
                b.visible = true;
        }
    }

    private void wrapAround(int width, int height) {
        if(position.x > width) position.x = 0;
        else if(position.x < 0) position.x = width-1;

        if(position.y > height) position.y = 0;
        else if(position.y < 0) position.y = height-1;
    }

    private void draw(Graphics2D g) {
        AffineTransform save = g.getTransform();
        g.translate(position.x, position.y);
        g.rotate(velocity.heading() + Math.PI / 2);
        g.setColor(Color.getHSBColor(flockingMode ? avgHue() : hue, 0.6f, 0.7f));
        g.fill(getShape(size));

        g.setTransform(save);
    }

    private Shape getShape(int x) {
        if(shapes.containsKey(x))
            return shapes.get(x);

        Path2D shape = new Path2D.Double();
        shape.moveTo(0, -size*2);
        shape.lineTo(-size, size*2);
        shape.lineTo(size, size*2);
        shape.closePath();

        shapes.put(x, shape);
        return shape;
    }

    private float avgHue() {
        int visible = 0;
        float hueSum = 0;

        for(Boid b : flock) {
            if(b.visible) {
                visible++;
                hueSum += b.hue;
            }
        }

        return hueSum / visible;
    }

    public void setPrey(List<Boid> prey) {
        this.prey = prey;
    }

    public void setFlock(List<Boid> flock) {
        this.flock = flock;
    }

    public void setPredators(List<Boid> predators) {
        this.predators = predators;
    }

    public void stopMovement() {
        velocity.mult(0);
    }

    public int getSize() {
        return size;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isDead() {
        return dead;
    }

    public int getLife() {
        return life;
    }

    public float getHue() {
        return hue;
    }

    public Vector getPosition() {
        return position;
    }

    public Vector getVelocity() {
        return velocity;
    }
}