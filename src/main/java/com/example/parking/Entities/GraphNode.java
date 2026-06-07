package com.example.parking.Entities;

import java.util.Objects;

public class GraphNode {
    private final String id; // למשל "1-A-1" או "Lane_A_1"
    private final boolean isLane; // true = נתיב, false = חניה

    public GraphNode(String id, boolean isLane) {
        this.id = id;
        this.isLane = isLane;
    }

    public String getId() { return id; }
    public boolean isLane() { return isLane; }

    // חשוב מאוד בשביל ה-HashMap וה-HashSet שמשתמשים בהם ב-BFS
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphNode node = (GraphNode) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}