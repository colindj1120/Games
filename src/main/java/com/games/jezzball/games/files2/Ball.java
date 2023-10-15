package com.games.jezzball.games.files2;

import com.google.common.util.concurrent.AtomicDouble;
import javafx.scene.shape.Rectangle;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Represents a Ball in the JezzBall game, containing information about its position, velocity, radius, and mass.
 * <p>
 * A ball in the game is modeled as a circle in a 2D coordinate system. The ball has properties like position, speed, direction, radius, and mass.
 * </p>
 *
 * @author Colin Jokisch
 * @version 1.3
 */
public class Ball implements SpatialObject {
    private final AtomicReference<Coordinate> position;   // Position of the ball
    private final AtomicDouble                direction;  // Direction of movement in radians (0 to 2π)

    private final double speed;    // Speed of the ball
    private final double radius;   // Radius of the ball
    private final double mass;     // Mass of the ball

    /**
     * Constructs a new ball with specified properties.
     *
     * @param position
     *         The balls initial position
     * @param speed
     *         Initial speed
     * @param direction
     *         Initial direction in radians
     * @param radius
     *         Radius of the ball
     * @param mass
     *         Mass of the ball
     */
    public Ball(Coordinate position, double speed, double direction, double radius, double mass) {
        this.position  = new AtomicReference<>(position);
        this.direction = new AtomicDouble(direction);
        this.speed     = speed;
        this.radius    = radius;
        this.mass      = mass;
    }

    // Getters and Setters
    @Override
    public Coordinate getPosition() {
        return position.get();
    }

    @Override
    public void setPosition(Coordinate position) {
        this.position.set(position);
    }

    public double[] getBoundingCoordinates() {
        return new double[]{position.get().x(), position.get().y(), radius};
    }

    @Override
    public boolean rectangleIntersection(Rectangle range) {
        synchronized (position) {
            Coordinate pos      = position.get();
            double     closestX = Math.max(range.getX(), Math.min(pos.x(), range.getX() + range.getWidth()));
            double     closestY = Math.max(range.getY(), Math.min(pos.y(), range.getY() + range.getHeight()));

            double distanceX = pos.x() - closestX;
            double distanceY = pos.y() - closestY;

            double distanceSquared = distanceX * distanceX + distanceY * distanceY;

            return distanceSquared < (radius * radius);
        }
    }

    @Override
    public AABB getAABB() {
        synchronized (position) {
            Coordinate pos = position.get();
            return new AABB(pos.x() - radius, pos.y() - radius, pos.x() + radius, pos.y() + radius);
        }
    }

    public double getSpeed() {
        return speed;
    }

    public double getDirection() {
        return direction.get();
    }

    public void setDirection(double direction) {
        this.direction.set(direction);
    }

    public double getRadius() {
        return radius;
    }

    public double getMass() {
        return mass;
    }

    /**
     * Determines if this Ball is equal to another object.
     * <p>
     * A Ball is considered equal to another Ball if their positions, directions, speeds, radii, and masses are identical.
     * </p>
     *
     * @param o
     *         Object to compare
     *
     * @return true if the object is a Ball and has the same properties, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        synchronized (position) {
            if (this == o) {return true;}
            if (o == null || getClass() != o.getClass()) {return false;}

            Ball ball = (Ball) o;

            if (Double.compare(ball.speed, speed) != 0) {return false;}
            if (Double.compare(ball.radius, radius) != 0) {return false;}
            if (Double.compare(ball.mass, mass) != 0) {return false;}
            if (!position.get()
                         .equals(ball.position.get())) {return false;}
            return Double.compare(ball.direction.get(), direction.get()) == 0;
        }
    }

    /**
     * Generates a hash code for the Ball.
     * <p>
     * The hash code is computed based on the Ball's position, direction, speed, radius, and mass.
     * </p>
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        synchronized (position) {
            return Objects.hash(position.get(), direction.get(), radius, speed, mass);
        }
    }

    /**
     * Calculates whether this ball will collide with another ball.
     *
     * @param other The other ball to check for collision with.
     * @return An Optional containing CollisionDetail if they will collide, otherwise Optional.empty().
     */
    public Optional<CollisionDetail<Ball, Ball>> willCollideWith(Ball other) {
        synchronized (position) {
            // Step 1: Initialize variables
            // -----------------------------
            // Get the current positions of both balls
            Coordinate p1 = this.getPosition();
            Coordinate p2 = other.getPosition();

            // Calculate the differences in the x and y coordinates of the two balls
            double dx = p2.x() - p1.x();
            double dy = p2.y() - p1.y();

            // Get the radius of both balls
            double r1 = this.getRadius();
            double r2 = other.getRadius();

            // Calculate the current distance between the two balls
            double d = Math.sqrt(dx * dx + dy * dy);

            // Step 2: Check for existing overlap
            // -----------------------------------
            // If the balls are already overlapping, then they can't collide in the future.
            if (d < r1 + r2) {
                return Optional.empty();
            }

            // Step 3: Compute velocity components for each ball
            // ------------------------------------------------
            // Calculate the x and y components of velocity for the first ball
            double v1x = this.getSpeed() * Math.cos(this.getDirection());
            double v1y = this.getSpeed() * Math.sin(this.getDirection());

            // Calculate the x and y components of velocity for the second ball
            double v2x = other.getSpeed() * Math.cos(other.getDirection());
            double v2y = other.getSpeed() * Math.sin(other.getDirection());

            // Compute the difference in velocity components between the two balls
            double dvx = v1x - v2x;
            double dvy = v1y - v2y;

            // Step 4: Set up the quadratic equation to solve for time t
            // ---------------------------------------------------------
            // Coefficients for the quadratic equation at^2 + bt + c = 0
            double a = dvx * dvx + dvy * dvy;
            double b = 2 * (dx * dvx + dy * dvy);
            double c = dx * dx + dy * dy - (r1 + r2) * (r1 + r2);

            // Step 5: Solve quadratic equation for t
            // ---------------------------------------
            // Discriminant for the quadratic equation
            double discriminant = b * b - 4 * a * c;

            // If discriminant is negative, then no real roots exist, i.e., no collision
            if (discriminant < 0) {
                return Optional.empty();
            }

            // Calculate the two possible times of collision
            double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
            double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);

            // Take the minimum positive time
            double t = Math.min(t1, t2);

            // Negative time means collision happened in the past
            if (t < 0) {
                return Optional.empty();
            }

            // Step 6: Calculate the collision coordinates
            // -------------------------------------------
            // Using one ball's initial coordinates and velocity components to find collision point
            double collisionX = p1.x() + v1x * t;
            double collisionY = p1.y() + v1y * t;

            // Step 7: Return the collision details
            // ------------------------------------
            return Optional.of(new CollisionDetail<>(t, collisionX, collisionY, this, other));
        }
    }

    /**
     * Updates the directions of this ball and another ball post-collision.
     * Assumes that the balls are just at the point of collision and that the
     * speed of each ball is constant.
     *
     * @param other The other ball involved in the collision.
     */
    public void resolveCollision(Ball other) {
        synchronized (position) {
            // Step 1: Get initial properties of both balls
            // --------------------------------------------
            Coordinate p1 = this.getPosition();
            Coordinate p2 = other.getPosition();

            // Get initial velocity components for both balls
            double v1x = this.getSpeed() * Math.cos(this.getDirection());
            double v1y = this.getSpeed() * Math.sin(this.getDirection());
            double v2x = other.getSpeed() * Math.cos(other.getDirection());
            double v2y = other.getSpeed() * Math.sin(other.getDirection());

            // Step 2: Calculate the new directions
            // -----------------------------------
            // Calculate the difference in positions between the two balls
            double dx = p2.x() - p1.x();
            double dy = p2.y() - p1.y();

            // Calculate unit normal vector
            double d = Math.sqrt(dx * dx + dy * dy);
            double nx = dx / d;
            double ny = dy / d;

            // Calculate the dot product between the velocity vectors and the normal vector
            double dot1 = v1x * nx + v1y * ny;
            double dot2 = v2x * nx + v2y * ny;

            // Calculate the reflected velocity vectors, keeping speed constant
            double v1xNew = v1x - 2 * dot1 * nx;
            double v1yNew = v1y - 2 * dot1 * ny;
            double v2xNew = v2x - 2 * dot2 * nx;
            double v2yNew = v2y - 2 * dot2 * ny;

            // Step 3: Update directions of the balls
            // --------------------------------------
            // Update directions based on new velocity components, keeping speed constant
            double newDirection1 = Math.atan2(v1yNew, v1xNew);
            double newDirection2 = Math.atan2(v2yNew, v2xNew);

            this.setDirection(newDirection1);
            other.setDirection(newDirection2);
        }
    }

    /**
     * Bounces the ball off a wall by updating its direction.
     *
     * @param wall The wall the ball is bouncing off of.
     */
    public void bounceOffWall(Wall wall) {
        synchronized (position) {
            // Get the current direction of the ball
            double currentDirection = this.getDirection();

            // Determine the orientation of the wall and calculate the new direction accordingly
            if (wall.getOrientation() == Wall.Orientation.HORIZONTAL) {
                // For horizontal walls, reflect the angle over the x-axis
                this.setDirection(2 * Math.PI - currentDirection);
            } else {
                // For vertical walls, reflect the angle over the y-axis
                this.setDirection(Math.PI - currentDirection);
            }

            // Make sure the new direction is within [0, 2π]
            double newDirection = this.getDirection() % (2 * Math.PI);
            this.setDirection(newDirection);
        }
    }

    /**
     * Calculates whether this ball will collide with a wall, considering the wall's potential growth.
     *
     * @param wall The wall to check for collision with.
     * @return An Optional containing CollisionDetail if they will collide, otherwise Optional.empty().
     */
    public Optional<CollisionDetail<Ball, Wall>> willCollideWithWall(Wall wall) {
        synchronized (position) {
            // Initialize common variables
            Coordinate p1 = this.getPosition();
            double v1x = this.getSpeed() * Math.cos(this.getDirection());
            double v1y = this.getSpeed() * Math.sin(this.getDirection());
            double wallSize = wall.getSize();
            double ballRadius = this.radius;

            // Handle stationary wall
            if (!wall.isGrowing()) {
                if (wall.getOrientation() == Wall.Orientation.HORIZONTAL) {
                    double adjustedWallY = wall.getPosition().y() - wallSize / 2.0;
                    double adjustedWallMaxY = wall.getPosition().y() + wallSize / 2.0;
                    double timeToCollision = calculateTimeToCollision(adjustedWallY, adjustedWallMaxY, p1.y(), v1y, ballRadius);
                    if (timeToCollision >= 0) {
                        return Optional.of(new CollisionDetail<>(timeToCollision, p1.x() + v1x * timeToCollision, adjustedWallY, this, wall));
                    }
                } else {
                    double adjustedWallX = wall.getPosition().x() - wallSize / 2.0;
                    double adjustedWallMaxX = wall.getPosition().x() + wallSize / 2.0;
                    double timeToCollision = calculateTimeToCollision(adjustedWallX, adjustedWallMaxX, p1.x(), v1x, ballRadius);
                    if (timeToCollision >= 0) {
                        return Optional.of(new CollisionDetail<>(timeToCollision, adjustedWallX, p1.y() + v1y * timeToCollision, this, wall));
                    }
                }
            } else { // Handle growing wall
                // Calculation logic
                double growthRate = wall.getGrowthRate(); // In coordinate units per second
                Coordinate currentEnd1 = wall.getCurrentEnd1();

                if (wall.getOrientation() == Wall.Orientation.HORIZONTAL) {
                    double y = currentEnd1.y();
                    while (y <= wall.getTarget1().y()) {
                        double adjustedWallY = y - wallSize / 2.0;
                        double adjustedWallMaxY = y + wallSize / 2.0;
                        double timeForWallToReachY = (y - currentEnd1.y()) / growthRate;
                        double timeForBallToReachY = calculateTimeToCollision(adjustedWallY, adjustedWallMaxY, p1.y(), v1y, ballRadius);

                        if (timeForBallToReachY <= timeForWallToReachY) {
                            return Optional.of(new CollisionDetail<>(timeForBallToReachY, p1.x() + v1x * timeForBallToReachY, adjustedWallY, this, wall));
                        }
                        y += growthRate;
                    }
                } else { // VERTICAL
                    double x = currentEnd1.x();
                    while (x <= wall.getTarget1().x()) {
                        double adjustedWallX = x - wallSize / 2.0;
                        double adjustedWallMaxX = x + wallSize / 2.0;
                        double timeForWallToReachX = (x - currentEnd1.x()) / growthRate;
                        double timeForBallToReachX = calculateTimeToCollision(adjustedWallX, adjustedWallMaxX, p1.x(), v1x, ballRadius);

                        if (timeForBallToReachX <= timeForWallToReachX) {
                            return Optional.of(new CollisionDetail<>(timeForBallToReachX, adjustedWallX, p1.y() + v1y * timeForBallToReachX, this, wall));
                        }
                        x += growthRate;
                    }
                }
            }

            return Optional.empty();
        }
    }

    /**
     * Calculate time to collision based on the adjusted coordinates of the wall and ball.
     *
     * @param adjustedWallMin The minimum coordinate of the wall taking its size into account.
     * @param adjustedWallMax The maximum coordinate of the wall taking its size into account.
     * @param ballCoordinate The current coordinate of the ball along the axis of interest.
     * @param ballVelocity The velocity of the ball along the axis of interest.
     * @param ballRadius The radius of the ball.
     * @return The time to collision, or -1 if no collision is predicted.
     */
    private double calculateTimeToCollision(double adjustedWallMin, double adjustedWallMax, double ballCoordinate, double ballVelocity, double ballRadius) {
        double minTime = Math.abs((adjustedWallMin - ballRadius - ballCoordinate) / ballVelocity);
        double maxTime = Math.abs((adjustedWallMax + ballRadius - ballCoordinate) / ballVelocity);

        if (minTime <= maxTime && minTime >= 0) {
            return minTime;
        }
        return -1.0;
    }

}
