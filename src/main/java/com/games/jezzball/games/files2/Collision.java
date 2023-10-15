package com.games.jezzball.games.files2;

import javafx.scene.shape.Rectangle;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Collision {
    private final QuadTree collisionTree;
    private final PriorityQueue<CollisionDetail<?, ?>> collisionQueue;
    private       double currentTime;  // Current time
    private final double targetTime;   // Target time for continuous collision detection
    private final double subStepSize;  // Substep size for continuous collision detection

    public Collision(double currentTime, double targetTime, double subStepSize) {
        this.currentTime    = currentTime;
        this.targetTime     = targetTime;
        this.subStepSize    = subStepSize;
        this.collisionTree  = new QuadTree(0, new Rectangle(0, 0, 1000, 1000));
        this.collisionQueue = new PriorityQueue<>(Comparator.comparingDouble(CollisionDetail::timeToCollision));
    }

    public void addBall(Ball ball) {
        collisionTree.insert(ball);
    }

    public void addWall(Wall wall) {
        collisionTree.insert(wall);
    }

    public void update(double deltaTime) {
        currentTime += deltaTime;

        // Detect and resolve collisions based on the elapsed time (deltaTime)...

        // 1. Detect Collisions
        detectCollisions();

        // 2. Sort Collisions by Time to Collision
        sortCollisionsByTime();

        // 3. Resolve Collisions
        resolveCollisions(deltaTime);
    }

    private void detectCollisions() {
        // Detect different types of collisions here and add them to collisionQueue
        detectBallToBallCollisions();
        detectBallToWallCollisions();
        detectWallToWallCollisions();
    }

    private void sortCollisionsByTime() {
        // Sort collisionQueue by time to collision
    }

    private void resolveCollisions(double deltaTime) {
        // Resolve collisions based on their types and times
        while (!collisionQueue.isEmpty()) {
            CollisionDetail<?, ?> collision = collisionQueue.poll();
            double timeToCollision = collision.timeToCollision() - currentTime;

            // Handle the collision based on its type and time
            if (timeToCollision <= deltaTime) {
                // Apply the collision to the objects involved
                collision.resolve();
            } else {
                // Apply a partial update to the objects
                collision.partialUpdate(deltaTime);
                break;  // Stop resolving collisions beyond the current time step
            }
        }
    }

    private void detectBallToBallCollisions() {
        // Detect ball-to-ball collisions and add them to collisionQueue
    }

    private void detectBallToWallCollisions() {
        // Detect ball-to-wall collisions and add them to collisionQueue
    }

    private void detectWallToWallCollisions() {
        // Detect wall-to-wall collisions and add them to collisionQueue
    }

    // Other utility methods and getters...
}
