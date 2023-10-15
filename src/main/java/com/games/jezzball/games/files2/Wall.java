package com.games.jezzball.games.files2;

import com.google.common.util.concurrent.AtomicDouble;
import javafx.scene.shape.Rectangle;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;

/**
 * Represents a Wall in the JezzBall game, which can be either stationary or growing. This refactored version calculates the grow velocity of the wall based on its start and end coordinates and grow
 * speed. It also includes a size property to be used in collision detection, which can represent either width or height.
 * <p>
 * Wall objects are responsible for tracking their position, determining if they intersect with a given rectangle, and calculating their Axis-Aligned Bounding Box (AABB).
 *
 * @author Colin Jokisch
 * @version 1.3
 */
public class Wall implements SpatialObject {
    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    private final AtomicReference<Coordinate> start;      // Starting coordinate of the wall
    private final AtomicReference<Coordinate> currentEnd1;       // Coordinate for the first end of the wall
    private final AtomicReference<Coordinate> currentEnd2;       // Coordinate for the second end of the wall
    private final AtomicBoolean               isGrowing;  // Boolean to check if the wall is currently growing
    private final AtomicDouble                growthRate; // The rate at which the wall is growing
    private final Wall end1CollideInto;
    private final Wall end2CollideInto;

    private final Coordinate target1; // Target coordinate for the first end of the wall
    private final Coordinate target2; // Target coordinate for the second end of the wall
    private final double     size;    // Represents the width if vertical and the height if horizontal

    private       Optional<double[]> cachedBoundingCoordinates = Optional.empty();
    private final Object             boundingCoordinatesLock   = new Object();

    private       Optional<Orientation> cachedOrientation = Optional.empty();
    private final Object                orientationLock   = new Object();

    public Wall(Coordinate start, double size, int growthRate, boolean isGrowing, Coordinate target1, Coordinate target2, Wall end1CollideInto, Wall end2CollideInto) {
        this.start = new AtomicReference<>(start);
        this.size = size;
        this.growthRate = new AtomicDouble(growthRate);
        this.isGrowing = new AtomicBoolean(isGrowing);
        this.target1 = target1;
        this.target2 = target2;
        this.end1CollideInto = end1CollideInto;
        this.end2CollideInto = end2CollideInto;
        this.currentEnd1 = new AtomicReference<>(start);
        this.currentEnd2 = new AtomicReference<>(start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Coordinate getPosition() {
        return start.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(Coordinate position) {
        this.start.set(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rectangleIntersection(Rectangle range) {
        synchronized (boundingCoordinatesLock) {
            double[] boundingCoords = getCachedOrComputeBoundingCoordinates();
            return range.intersects(boundingCoords[0], boundingCoords[1], boundingCoords[2] - boundingCoords[0], boundingCoords[3] - boundingCoords[1]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AABB getAABB() {
        synchronized (boundingCoordinatesLock) {
            double[] coords = getCachedOrComputeBoundingCoordinates();
            return new AABB(coords[0], coords[1], coords[2], coords[3]);
        }
    }

    public Coordinate getCurrentEnd1() {
        return currentEnd1.get();
    }

    public Coordinate getCurrentEnd2() {
        return currentEnd2.get();
    }

    public Coordinate getTarget1() {
        return target1;
    }

    public Coordinate getTarget2() {
        return target2;
    }

    public double getSize() {
        return size;
    }

    public boolean isGrowing() {
        return isGrowing.get();
    }

    public double getGrowthRate() {
        return growthRate.get();
    }

    public void stopGrowing() {
        isGrowing.set(false);
        growthRate.set(0);
    }

    /**
     * Checks if the wall is horizontal or vertical.
     *
     * @return the orientation of the wall.
     */
    public Orientation getOrientation() {
        synchronized (orientationLock) {
            return cachedOrientation.orElseGet(() -> {
                if (checkAlignment(start.get(), currentEnd1.get(), currentEnd2.get(), Coordinate::yEquals)) {
                    return setCachedOrientation(Orientation.HORIZONTAL);
                } else if (checkAlignment(start.get(), currentEnd1.get(), currentEnd2.get(), Coordinate::xEquals)) {
                    return setCachedOrientation(Orientation.VERTICAL);
                } else {
                    throw new IllegalStateException("Undefined orientation for wall");
                }
            });
        }
    }

    /**
     * A generic helper method that tests if the coordinates are aligned according to the given predicate.
     *
     * @param startCoord
     *         the start coordinate
     * @param end1
     *         the first end coordinate
     * @param end2
     *         the second end coordinate
     * @param alignmentCheck
     *         the alignment check as a BiPredicate
     *
     * @return true if the coordinates are aligned according to the predicate, otherwise false
     */
    private boolean checkAlignment(Coordinate startCoord, Coordinate end1, Coordinate end2, BiPredicate<Coordinate, Coordinate> alignmentCheck) {
        return alignmentCheck.test(startCoord, end1) && alignmentCheck.test(startCoord, end2);
    }

    /**
     * Determines if the first end of the wall has reached its target.
     *
     * @return True if the first end has reached its target, otherwise false.
     */
    public boolean hasReachedEnd1() {
        return this.currentEnd1.get()
                               .equals(this.target1);
    }

    /**
     * Determines if the second end of the wall has reached its target.
     *
     * @return True if the second end has reached its target, otherwise false.
     */
    public boolean hasReachedEnd2() {
        return this.currentEnd2.get()
                               .equals(this.target2);
    }

    /**
     * Calculates the time for the wall to become stationary.
     *
     * @return The time in some unit for the wall to become stationary.
     */
    public double timeToBecomeStationary() {
        synchronized (boundingCoordinatesLock) {  // Assuming you are using the same lock
            if (hasReachedEnd1() && hasReachedEnd2()) {
                return 0;
            }

            double distanceToEnd1 = 0;
            double distanceToEnd2 = 0;

            if (!hasReachedEnd1()) {
                distanceToEnd1 = calculateEuclideanDistance(currentEnd1.get(), target1);
            }
            if (!hasReachedEnd2()) {
                distanceToEnd2 = calculateEuclideanDistance(currentEnd2.get(), target2);
            }

            double timeToEnd1 = distanceToEnd1 / growthRate.get();
            double timeToEnd2 = distanceToEnd2 / growthRate.get();

            return Math.max(timeToEnd1, timeToEnd2);
        }
    }

    /**
     * Calculates the Euclidean distance between two given coordinates.
     * <p>
     * The Euclidean distance is calculated using the formula: \[ \sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2} \] where \( (x_1, y_1) \) and \( (x_2, y_2) \) are the coordinates of the first and second points
     * respectively.
     * </p>
     *
     * @param coord1
     *         The first coordinate.
     * @param coord2
     *         The second coordinate.
     *
     * @return The Euclidean distance between coord1 and coord2.
     */
    private double calculateEuclideanDistance(Coordinate coord1, Coordinate coord2) {
        return Math.sqrt(Math.pow(coord2.x() - coord1.x(), 2) + Math.pow(coord2.y() - coord1.y(), 2));
    }

    /**
     * Retrieves the bounding coordinates for the wall based on its current ends and orientation.
     * <p>
     * This method is synchronized to ensure thread safety, especially since it caches the computed bounding coordinates for faster future access. The method first checks if the bounding coordinates
     * are already cached. If so, it returns the cached coordinates. Otherwise, it computes the new bounding coordinates based on the wall's current ends and orientation.
     * </p>
     * <p>
     * The bounding coordinates are represented as an array of four double values: [coord1Min, coord2Min, coord1Max, coord2Max], where coord1Min and coord1Max are either the minimum and maximum x or y
     * coordinates, depending on the orientation. coord2Min and coord2Max are calculated using the 'size' property of the wall, and are based on the other coordinate (y if orientation is horizontal
     * and x if orientation is vertical).
     * </p>
     *
     * @return An array of four double values representing the bounding coordinates.
     *
     * @throws IllegalStateException
     *         if the wall has an undefined orientation.
     */
    private double[] getBoundingCoordinates() {
        synchronized (boundingCoordinatesLock) {
            if (cachedBoundingCoordinates.isPresent()) {
                return cachedBoundingCoordinates.get();
            }

            double[] newCachedBoundingCoordinates = new double[4];
            double   coord1Min, coord1Max, coord2;

            switch (getOrientation()) {
                case HORIZONTAL -> {
                    coord1Min = Coordinate.minX(currentEnd1.get(), currentEnd2.get());
                    coord1Max = Coordinate.maxX(currentEnd1.get(), currentEnd2.get());
                    coord2    = start.get()
                                     .y();
                }
                case VERTICAL -> {
                    coord1Min = Coordinate.minY(currentEnd1.get(), currentEnd2.get());
                    coord1Max = Coordinate.maxY(currentEnd1.get(), currentEnd2.get());
                    coord2    = start.get()
                                     .x();
                }
                default -> throw new IllegalStateException("Undefined orientation for wall");
            }

            newCachedBoundingCoordinates[0] = coord1Min;
            newCachedBoundingCoordinates[1] = coord2 - size / 2.0;
            newCachedBoundingCoordinates[2] = coord1Max;
            newCachedBoundingCoordinates[3] = coord2 + size / 2.0;

            cachedBoundingCoordinates = Optional.of(newCachedBoundingCoordinates);
            return cachedBoundingCoordinates.get();
        }
    }

    /**
     * Private method to check and return cached bounding coordinates.
     *
     * @return Cached or newly computed AtomicDoubleArray of bounding coordinates.
     */
    private double[] getCachedOrComputeBoundingCoordinates() {
        return cachedBoundingCoordinates.orElseGet(this::getBoundingCoordinates);
    }

    /**
     * Sets the cached orientation value and returns it.
     * <p>
     * This method stores the provided orientation value in an Optional cache for quicker future access. The same orientation value is then returned. This is intended to reduce computational load if
     * the orientation is frequently accessed but seldom changed.
     * </p>
     *
     * @param orientation
     *         The orientation value to be cached.
     *
     * @return The cached orientation value.
     */
    private Orientation setCachedOrientation(Orientation orientation) {
        cachedOrientation = Optional.of(orientation);
        return orientation;
    }

    /**
     * Compares this Wall object to the specified object for equality.
     * Two Wall objects are considered equal if all their properties are equal.
     *
     * @param o the object to be compared for equality with this Wall object
     * @return true if the specified object is equal to this Wall object
     */
    public synchronized boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wall wall = (Wall) o;

        return size == wall.size &&
               growthRate.get() == wall.growthRate.get() &&
               isGrowing.get() == wall.isGrowing.get() &&
               start.get().equals(wall.start.get()) &&
               currentEnd1.get().equals(wall.currentEnd1.get()) &&
               currentEnd2.get().equals(wall.currentEnd2.get()) &&
               target1.equals(wall.target1) &&
               target2.equals(wall.target2);
    }

    /**
     * Returns the hash code for this Wall object.
     *
     * @return the hash code for this Wall object
     */
    public synchronized int hashCode() {
        return Objects.hash(
                start.get(),
                currentEnd1.get(),
                currentEnd2.get(),
                isGrowing.get(),
                growthRate.get(),
                target1,
                target2,
                size
        );
    }

    /**
     * Updates the wall's end coordinates based on its growth rate and direction.
     * This method should be called periodically to make the wall grow.
     */
    public void update() {
        synchronized (isGrowing) {
            if (isGrowing.get()) {
                double growthAmount = growthRate.get();
                synchronized (currentEnd1) {
                    synchronized (currentEnd2) {
                        switch (getOrientation()) {
                            case HORIZONTAL:
                                // Update the horizontal wall ends
                                updateHorizontalEnds(growthAmount);
                                break;
                            case VERTICAL:
                                // Update the vertical wall ends
                                updateVerticalEnds(growthAmount);
                                break;
                        }
                    }
                }
            }
        }
    }

    private void updateHorizontalEnds(double growthAmount) {
        // Check if the wall has reached its target ends
        if (!hasReachedEnd1()) {
            // Update the first end horizontally
            Coordinate newEnd1 = new Coordinate(currentEnd1.get().x() + growthAmount, currentEnd1.get().y());
            setCurrentEnd1(newEnd1);
        }
        if (!hasReachedEnd2()) {
            // Update the second end horizontally
            Coordinate newEnd2 = new Coordinate(currentEnd2.get().x() - growthAmount, currentEnd2.get().y());
            setCurrentEnd2(newEnd2);
        }
    }

    private void updateVerticalEnds(double growthAmount) {
        // Check if the wall has reached its target ends
        if (!hasReachedEnd1()) {
            // Update the first end vertically
            Coordinate newEnd1 = new Coordinate(currentEnd1.get().x(), currentEnd1.get().y() + growthAmount);
            setCurrentEnd1(newEnd1);
        }
        if (!hasReachedEnd2()) {
            // Update the second end vertically
            Coordinate newEnd2 = new Coordinate(currentEnd2.get().x(), currentEnd2.get().y() - growthAmount);
            setCurrentEnd2(newEnd2);
        }
    }

    private void setCurrentEnd1(Coordinate currentEnd1) {
        cachedBoundingCoordinates = Optional.empty();
        this.currentEnd1.set(currentEnd1);
    }

    private void setCurrentEnd2(Coordinate currentEnd2) {
        cachedBoundingCoordinates = Optional.empty();
        this.currentEnd2.set(currentEnd2);
    }

    public CollisionDetail<Wall, Wall> willCollideWithWallEnd1(Wall otherWall) {
        double timeToCollision = calculateTimeToCollision(otherWall, true);
        return createCollisionDetail(otherWall, timeToCollision, currentEnd1, currentEnd2);
    }

    public CollisionDetail<Wall, Wall> willCollideWithWallEnd2(Wall otherWall) {
        double timeToCollision = calculateTimeToCollision(otherWall, false);
        return createCollisionDetail(otherWall, timeToCollision, currentEnd2, currentEnd1);
    }

    private CollisionDetail<Wall, Wall> createCollisionDetail(Wall otherWall, double timeToCollision, AtomicReference<Coordinate> currentEnd1, AtomicReference<Coordinate> currentEnd2) {
        if (timeToCollision >= 0) {
            double collisionX = currentEnd1.get().x() + (currentEnd2.get().x() - currentEnd1.get().x()) * timeToCollision;
            double collisionY = currentEnd1.get().y() + (currentEnd2.get().y() - currentEnd1.get().y()) * timeToCollision;
            return new CollisionDetail<>(timeToCollision, collisionX, collisionY, this, otherWall);
        } else {
            return null; // No collision
        }
    }

    private double calculateTimeToCollision(Wall otherWall, boolean isEnd1) {
        // Calculate relative velocity (velocity of the other wall with respect to this wall)
        double relativeVelocityX = otherWall.getGrowthRate() - this.getGrowthRate();

        // Check if the walls are moving away from each other (no collision possible)
        if (relativeVelocityX >= 0) {
            return -1; // No collision
        }

        // Calculate relative position (position of the other wall with respect to this wall)
        double relativePositionX;
        if (isEnd1) {
            relativePositionX = otherWall.getCurrentEnd1().x() - this.getCurrentEnd1().x();
        } else {
            relativePositionX = otherWall.getCurrentEnd2().x() - this.getCurrentEnd2().x();
        }

        // Calculate the time to collision using relative position and relative velocity
        double timeToCollision = relativePositionX / relativeVelocityX;

        // Check if the collision will occur in the future (time to collision is positive)
        if (timeToCollision >= 0) {
            return timeToCollision;
        } else {
            return -1; // No collision
        }
    }

}
