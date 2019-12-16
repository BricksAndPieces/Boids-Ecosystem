package internal.quadtree;

import internal.Boid;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("All")
public class QuadTree {

    private final Vector position;
    private final Vector size;

    private int index = 0;
    private final Boid[] boids;
    private final QuadTree[] children = new QuadTree[4];

    public QuadTree(double x, double y, double w, double h, int capacity) {
        this.position = new Vector(x, y);
        this.size = new Vector(w, h);
        this.boids = new Boid[capacity];
    }

    public void insert(Boid boid) {
        if(index < boids.length) {
            boids[index++] = boid;
            return;
        }

        if(children[0] == null) // Divide tree if not already divided
            subDivide(position.x, position.y, size.x, size.y, boids.length);

        for(QuadTree child : children) {
            if(pointInBounds(boid.getPosition(), child.position, child.size)) {
                child.insert(boid);
                break;
            }
        }
    }

    public List<Boid> query(Vector pos, Vector size) {
        if(!intersects(pos, size, this.position, this.size))
            return Collections.emptyList();

        List<Boid> query = new ArrayList<>();
        for(Boid b : boids) {
            if(b != null && pointInBounds(b.getPosition(), pos, size))
                query.add(b);
        }

        if(children[0] != null) { // If divided add children
            for(QuadTree child : children)
                query.addAll(child.query(pos, size));
        }

        return query;
    }

    public void clear() {
        index = 0;
        for(int i = 0; i < boids.length; i++)
            boids[i] = null;

        for(int i = 0; i < children.length; i++)
            children[i] = null;
    }

    private void subDivide(double x, double y, double w, double h, int cap) {
        children[0] = new QuadTree(x, y, w/2, h/2, cap);
        children[1] = new QuadTree(x+w/2, y, w/2, h/2, cap);
        children[2] = new QuadTree(x, y+h/2, w/2, h/2, cap);
        children[3] = new QuadTree(x+w/2, y+h/2, w/2, h/2, cap);
    }

    private boolean pointInBounds(Vector p, Vector pos, Vector s) {
        return p.x > pos.x && p.x <= pos.x+s.x && p.y > pos.y && p.y <= pos.y+s.y;
    }

    private boolean intersects(Vector p1, Vector s1, Vector p2, Vector s2) {
        return Math.min(p1.x + s1.x, p2.x + s2.x) > Math.max(p1.x, p2.x)
               && Math.min(p1.y + s1.y, p2.y + s2.y) > Math.max(p1.y, p2.y);
    }

    public void draw(Graphics2D g) {
        g.drawRect((int)position.x, (int)position.y, (int)size.x, (int)size.y);
        if(children[0] != null) for(QuadTree child : children) child.draw(g);
    }
}