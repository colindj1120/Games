package com.games.jezzball.games.files2;

public record Coordinate(double x, double y) {
    private static final double TOLERANCE = 0.0001;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return Math.abs(x - that.x) <= TOLERANCE && Math.abs(y - that.y) <= TOLERANCE;
    }

    public boolean yEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return Math.abs(y - that.y) <= TOLERANCE;
    }

    public boolean xEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return Math.abs(x - that.x) <= TOLERANCE;
    }

    public static double minX(Coordinate first, Coordinate second) {
        return Math.min(first.x(), second.x());
    }

    public static double maxX(Coordinate first, Coordinate second) {
        return Math.max(first.x(), second.x());
    }

    public static double minY(Coordinate first, Coordinate second) {
        return Math.min(first.y(), second.y());
    }

    public static double maxY(Coordinate first, Coordinate second) {
        return Math.max(first.y(), second.y());
    }
}
