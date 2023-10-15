package com.games.jezzball.games.files2;

import java.util.Optional;

/**
 * @param timeToCollision
 *         Time until the collision occurs
 * @param collisionX
 *         The X-coordinate of the collision
 * @param collisionY
 *         The Y-coordinate of the collision
 * @param object1
 *         The first object involved in the collision
 * @param object2
 *         The second object involved in the collision
 */
public record CollisionDetail<T1, T2>(double timeToCollision, double collisionX, double collisionY, T1 object1, T2 object2) {
    private static final double ELASTICITY = 1.0;  // Define the elasticity constant

    public void resolve() {
        if (object1 instanceof Ball && object2 instanceof Ball) {
            resolveBallToBall();
        } else if (object1 instanceof Ball && object2 instanceof Wall) {
            resolveBallToWall();
        } else if (object1 instanceof Wall && object2 instanceof Ball) {
            resolveBallToWall();
        } else if (object1 instanceof Wall && object2 instanceof Wall) {
            resolveWallToWall();
        }
    }

    private void resolveBallToBall() {

    }

    private void resolveBallToWall() {

    }

    private void resolveWallToWall() {

    }

    public Optional<CollisionDetail<T1, T2>> partialUpdate(double deltaTime) {
        if (object1 instanceof Ball && object2 instanceof Ball) {
            return handleBallBallCollision(deltaTime);
        }

        if (object1 instanceof Ball && object2 instanceof Wall) {
            return handleBallWallCollision(deltaTime);
        }

        if (object1 instanceof Wall && object2 instanceof Wall) {
            return handleWallWallCollision(deltaTime);
        }

        return Optional.empty(); // No more collisions in the remaining time
    }

    private Optional<CollisionDetail<T1, T2>> handleBallBallCollision(double deltaTime) {

        return Optional.empty();
    }

    private Optional<CollisionDetail<T1, T2>> handleBallWallCollision(double deltaTime) {

        return Optional.empty();
    }

    private Optional<CollisionDetail<T1, T2>> handleWallWallCollision(double deltaTime) {

        return Optional.empty();
    }

    public static double calculateTimeToBallBallCollision(Ball ball1, Ball ball2) {
        return 0.0;
    }

    private double calculateTimeToBallWallCollision(Ball ball, Wall wall) {
        return 0.0;
    }

}

