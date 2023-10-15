/**
 * The SpatialObject interface represents an object that exists in a two-dimensional Cartesian space.
 * <p>
 * It is designed to be used for game elements that have a position and dimensions. The interface provides
 * methods for retrieving and setting the position of the object, as well as for collision detection through
 * rectangular intersection and obtaining an Axis-Aligned Bounding Box (AABB).
 * </p>
 * <p>
 * This interface is typically implemented by game elements like walls, balls, or other objects that interact
 * spatially within the game arena.
 * </p>
 *
 * @author Colin Jokisch
 * @version 1.0
 */
package com.games.jezzball.games.files2;

import javafx.scene.shape.Rectangle;

public interface SpatialObject {

    /**
     * Retrieves the current position of the SpatialObject.
     *
     * @return A Coordinate object representing the current position.
     */
    Coordinate getPosition();

    /**
     * Sets the position of the SpatialObject.
     * <p>
     * This method is used to update the position of the SpatialObject, typically as part of a game loop
     * or in response to some event.
     * </p>
     *
     * @param position A Coordinate object representing the new position.
     */
    void setPosition(Coordinate position);

    /**
     * Determines if the SpatialObject intersects with a given Rectangle.
     * <p>
     * This method is usually used for collision detection between the SpatialObject and other objects in
     * the game arena.
     * </p>
     *
     * @param range The Rectangle with which to check for intersection.
     * @return True if the SpatialObject intersects with the Rectangle, otherwise false.
     */
    boolean rectangleIntersection(Rectangle range);

    /**
     * Retrieves the Axis-Aligned Bounding Box (AABB) of the SpatialObject.
     * <p>
     * The AABB is a rectangle aligned with the coordinate axes that tightly encloses the SpatialObject.
     * It is commonly used for efficient collision detection.
     * </p>
     *
     * @return An AABB object representing the bounding box of the SpatialObject.
     */
    AABB getAABB();
}
