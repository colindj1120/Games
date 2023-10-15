package com.games.jezzball.games.files2;

import javafx.scene.shape.Rectangle;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements a QuadTree data structure to store SpatialObjects efficiently based on their positions. It recursively subdivides the space into quadrants to minimize the number of objects to be checked
 * for collision detection.
 *
 * @author Colin Jokisch
 * @version 1.1
 */
public class QuadTree {
    private static final int MAX_OBJECTS = 10;
    private static final int MAX_LEVELS  = 5;

    private enum Quadrant {
        NW,
        NE,
        SW,
        SE
    }

    private final int                 level;
    private final List<SpatialObject> objects;
    private final Rectangle           boundary;

    private final Map<Quadrant, QuadTree> childNodes = new EnumMap<>(Quadrant.class);

    public QuadTree(int level, Rectangle boundary) {
        this.level    = level;
        this.objects  = new LinkedList<>();
        this.boundary = boundary;
    }

    public void clear() {
        objects.clear();
        childNodes.clear();
    }

    private void split() {
        double subWidth  = boundary.getWidth() / 2;
        double subHeight = boundary.getHeight() / 2;
        double x         = boundary.getX();
        double y         = boundary.getY();

        Map<Quadrant, QuadTree> tempMap = Map.of(
                Quadrant.NW, new QuadTree(level + 1, new Rectangle(x, y, subWidth, subHeight)),
                Quadrant.NE, new QuadTree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight)),
                Quadrant.SW, new QuadTree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight)),
                Quadrant.SE, new QuadTree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight))
        );

        childNodes.putAll(tempMap);
    }

    /**
     * Inserts a SpatialObject into the appropriate node or child node.
     *
     * @param obj
     *         The object to insert.
     */
    public void insert(SpatialObject obj) {
        getIndex(obj).ifPresentOrElse(
                quadrant -> childNodes.get(quadrant)
                                      .insert(obj),
                () -> {
                    objects.add(obj);
                    if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
                        if (childNodes.isEmpty()) {
                            split();
                        }
                        partitionObjects();
                    }
                }
        );
    }

    /**
     * Moves objects from this node to the appropriate child nodes, if possible.
     */
    private void partitionObjects() {
        @SuppressWarnings("OptionalGetWithoutIsPresent") //You filter by isPresent so it must be present
        List<SpatialObject> toRemove = objects.stream()
                                              .filter(obj -> getIndex(obj).isPresent())
                                              .peek(obj -> childNodes.get(getIndex(obj).get())
                                                                     .insert(obj))
                                              .toList();
        objects.removeAll(toRemove);
    }

    private Optional<Quadrant> getIndex(SpatialObject obj) {
        double verticalMidpoint   = boundary.getX() + (boundary.getWidth() / 2);
        double horizontalMidpoint = boundary.getY() + (boundary.getHeight() / 2);

        AABB aabb = obj.getAABB();

        boolean topQuadrant    = (aabb.minY() > boundary.getY()) && (aabb.maxY() < horizontalMidpoint);
        boolean bottomQuadrant = (aabb.minY() > horizontalMidpoint) && (aabb.maxY() < boundary.getY() + boundary.getHeight());
        boolean leftQuadrant   = (aabb.minX() > boundary.getX()) && (aabb.maxX() < verticalMidpoint);
        boolean rightQuadrant  = (aabb.minX() > verticalMidpoint) && (aabb.maxX() < boundary.getX() + boundary.getWidth());

        if (leftQuadrant) {
            if (topQuadrant) {
                return Optional.of(Quadrant.NW);
            } else if (bottomQuadrant) {
                return Optional.of(Quadrant.SW);
            }
        } else if (rightQuadrant) {
            if (topQuadrant) {
                return Optional.of(Quadrant.NE);
            } else if (bottomQuadrant) {
                return Optional.of(Quadrant.SE);
            }
        }
        return Optional.empty();
    }

    /**
     * Queries the QuadTree to find all objects of a certain type using Java Streams.
     *
     * @param clazz
     *         The class type to look for.
     * @param <T>
     *         The type parameter, extending SpatialObject.
     *
     * @return A list of objects of the specified type.
     */
    public <T extends SpatialObject> List<T> queryType(Class<T> clazz) {
        return objects.stream()
                      .filter(clazz::isInstance)
                      .map(clazz::cast)
                      .collect(Collectors.toList());
    }

    /**
     * Queries the QuadTree for objects within a specified bounding box using Java Streams.
     *
     * @param x1
     *         The x-coordinate of the top-left corner of the bounding box.
     * @param y1
     *         The y-coordinate of the top-left corner of the bounding box.
     * @param x2
     *         The x-coordinate of the bottom-right corner of the bounding box.
     * @param y2
     *         The y-coordinate of the bottom-right corner of the bounding box.
     *
     * @return A list of objects meeting the criteria.
     */
    public <T extends SpatialObject> List<T> queryRange(double x1, double y1, double x2, double y2, Class<T> clazz) {
        Rectangle range = new Rectangle(x1, y1, x2 - x1, y2 - y1);

        return objects.stream()
                      .filter(clazz::isInstance)
                      .filter(obj -> obj.rectangleIntersection(range))
                      .map(clazz::cast)
                      .collect(Collectors.toList());
    }
}
