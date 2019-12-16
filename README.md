# Boids-Ecosystem
This project is a combination of the Boids flocking algorithm with an ecosystem simulation. Boids eat each other based on size.

## Instructions
After cloning the repository and running the project, the controls are as follows:
- Space  -> Changes between flocking and ecosystem modes
- Q -> Displays the underlying quadtrees (which looks cool)
- Clicking -> Spawns boids
- 1-5 -> Sets the size of the boid to be spawned during ecosystem mode

## Boids
The Boids algorithm mimics the flocking nature of animals such as birds or fish. Entities, called boids, are used to demonstrate this. The algorithm work using three main principles.
More information can be found at https://en.wikipedia.org/wiki/Boids

#### Separation
In the separation stage, boids attempt to dodge each other and maintatin an equal distance between other boids. This usually involves a distance check.
```java
private Vector separation(List<Boid> boids) {
    // The direction to avoid other boids
    Vector steer = new Vector();
    
    // Check all other boids to determine direction
    for(Boid boid : boids) {
        // Perform calculations
    }
    
    return steer;
}
```

#### Alignment
In the alignment stage, boids attempt to set their velocity to the same as the boids around them. This makes all boids in an area go in the same direction.
```java
private Vector alignment(List<Boid> boids) {
    // The direction to align with other boids
    Vector steer = new Vector();
    
    // Check all other boids to determine direction
    for(Boid boid : boids) {
        // Perform calculations
    }
    
    return steer;
}
```

#### Cohesion
In the cohesion stage, boids attempt to go to the center of mass of their flock. This allows the boids to stick together and creates a swirling effect on its own.
```java
private Vector cohesion(List<Boid> boids) {
    // The direction to the center of mass
    Vector steer = new Vector();
    
    // Check all other boids to determine direction
    for(Boid boid : boids) {
        // Perform calculations
    }
    
    return steer;
}
```

These are the three main principles that define the boids algorithm. I have edited it to add more functionality

#### Noise
I added some noise to the simulation which allows the boids to randomly change direction and makes the whole simulation seem more organic. (Note: this should have a small weight compared to the three primary influences)
```java
private Vector noise() {
    // Generate a random direction
    Vector random = randomDirection;
    return random;
}
```

#### Attraction
This is basically the same as cohesion except it allows the boid to target the center of mass of prey. This is used in the ecosystem simulation.
```java
private Vector attraction(List<Boid> prey) {
    // The direction to the center of mass
    Vector steer = new Vector();
    
    // Check all other boids to determine direction
    for(Boid boid : prey) {
        // Perform calculations
    }
    
    return steer;
}
```

#### Avoidance
This is basically the same as separation except it allows the boids to flee from predators. This is the highest weighted influence in the ecosystem simulation.
```java
private Vector avoidance(List<Boid> predators) {
    // The direction to avoid other boids
    Vector steer = new Vector();
    
    // Check all other boids to determine direction
    for(Boid boid : predators) {
        // Perform calculations
    }
    
    return steer;
}
```

When all six of these factors are combined, it creates a very cool and ond organic simulation. Its a simple ecosystem where boids can only eat the boids one size smaller than themselves. There is also a simple health system that controls lifetime and reproduction.

## Quadtree
This boids simulation uses a quadtree to help speed up the process of calculating the direction of each boid. In a normal boids system. Each boid has to check every other boid which leads to a calculation time of n^2 where n is the number of boids in the simulation. In the quadtree system. The environment is split into subchunks and the distances of boids are calculated based off these subchunks. This leads to a calculation speed that is nlog(n) where b is the number of boids. A logarithmic function is a lot faster than a exponential function when the number of boids get big. 
More information can be found at https://en.wikipedia.org/wiki/Quadtree

#### Insert Function
This is how boids are sorted among subchunks.
```java
public void insert(Boid boid) {
    // Add boid to quadtree if in capacity
    if(index < boids.length) {
        boids[index++] = boid;
        return;
    }
    
    if(children[0] == null) // Divide tree if not already divided
        subDivide(position.x, position.y, size.x, size.y, boids.length);
    
    // Recursively place boid in children
    for(QuadTree child : children) {
        if(pointInBounds(boid.getPosition(), child.position, child.size)) {
            child.insert(boid);
            break;
        }
    }
}
```

#### Query Function
This is how boids access a list of boids around them
```java
public List<Boid> query(Vector pos, Vector size) {
    // If the quadtree is not in specified area
    if(!intersects(pos, size, this.position, this.size))
        return Collections.emptyList();
    
    // Get boids in specifed area
    List<Boid> query = new ArrayList<>();
    for(Boid b : boids) {
        if(b != null && pointInBounds(b.getPosition(), pos, size))
            query.add(b);
    }
    
    // Recursively get boids in area in children
    if(children[0] != null) { // If divided add children
        for(QuadTree child : children)
            query.addAll(child.query(pos, size));
    }
        
    return query;
}
```