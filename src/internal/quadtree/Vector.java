package internal.quadtree;

import static java.lang.Math.*;

@SuppressWarnings("All")
public class Vector {

    public double x;
    public double y;

    public Vector() {
        this(0, 0);
    }

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(double o) {
        x += o;
        y += o;
    }

    public void add(Vector o) {
        x += o.x;
        y += o.y;
    }

    public void sub(double o) {
        x -= o;
        y -= o;
    }

    public void sub(Vector o) {
        x -= o.x;
        y -= o.y;
    }

    public void mult(double o) {
        x *= o;
        y *= o;
    }

    public void mult(Vector o) {
        x *= o.x;
        y *= o.y;
    }

    public void div(double o) {
        x /= o;
        y /= o;
    }

    public void div(Vector o) {
        x /= o.x;
        y /= o.y;
    }

    public double mag() {
        return sqrt(pow(x, 2) + pow(y, 2));
    }

    public double dot(Vector o) {
        return x * o.x + y * o.y;
    }

    public void normalize() {
        double mag = mag();
        if (mag != 0) {
            x /= mag;
            y /= mag;
        }
    }

    public void limit(double lim) {
        double mag = mag();
        if (mag != 0 && mag > lim) {
            x *= lim / mag;
            y *= lim / mag;
        }
    }

    public double heading() {
        return atan2(y, x);
    }

    public static Vector sub(Vector v1, Vector v2) {
        return new Vector(v1.x - v2.x, v1.y - v2.y);
    }

    public static double dist(Vector v1, Vector v2) {
        return sqrt(pow(v1.x - v2.x, 2) + pow(v1.y - v2.y, 2));
    }

    public static double angleBetween(Vector v1, Vector v2) {
        return acos(v1.dot(v2) / (v1.mag() * v2.mag()));
    }
}