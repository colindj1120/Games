package com.games.jezzball.games.files2;

/**
 * Represents an Axis-Aligned Bounding Box for spatial objects.
 * This record is immutable and primarily intended for containing data.
 *
 * @version 1.0
 * @author Colin Jokisch
 */
public record AABB(double minX, double minY, double maxX, double maxY) {

    /**
     * Constructs an AABB object with the given coordinates.
     *
     * @param minX The minimum x-coordinate of the bounding box.
     * @param minY The minimum y-coordinate of the bounding box.
     * @param maxX The maximum x-coordinate of the bounding box.
     * @param maxY The maximum y-coordinate of the bounding box.
     */
    public AABB {
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Minimum coordinates should be less than or equal to maximum coordinates");
        }
    }
}

