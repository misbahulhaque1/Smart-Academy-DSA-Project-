package dsas;

import java.util.*;

public class GraphDS {

    // simple edge
    public static class Edge {
        int targetCourseId;
        double weight;

        public Edge(int targetCourseId, double weight) {
            this.targetCourseId = targetCourseId;
            this.weight = weight;
        }
    }

    // adj list
    private Map<Integer, List<Edge>> adjacencyList;

    public GraphDS() {
        this.adjacencyList = new HashMap<>();
    }

    // add vertex
    public void addVertex(int courseId) {
        adjacencyList.putIfAbsent(courseId, new ArrayList<>());
    }

    // add edge
    public void addEdge(int fromCourseId, int toCourseId, double weight) {
        addVertex(fromCourseId);
        addVertex(toCourseId);
        adjacencyList.get(fromCourseId).add(new Edge(toCourseId, weight));
    }

    // neighbors
    public List<Edge> getNeighbors(int courseId) {
        return adjacencyList.getOrDefault(courseId, new ArrayList<>());
    }

    // recommendations (DFS + weights)
    public List<Integer> getRecommendations(int startCourseId, int maxDepth) {
        Map<Integer, Double> recommendations = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        dfsRecommendations(startCourseId, maxDepth, 0, 1.0, visited, recommendations);

        recommendations.remove(startCourseId); // skip self

        List<Map.Entry<Integer, Double>> sortedList = new ArrayList<>(recommendations.entrySet());
        sortedList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : sortedList) {
            result.add(entry.getKey());
        }
        return result;
    }

    // dfs helper
    private void dfsRecommendations(int courseId, int maxDepth, int currentDepth,
                                    double currentWeight, Set<Integer> visited,
                                    Map<Integer, Double> recommendations) {
        if (currentDepth > maxDepth) return;

        visited.add(courseId);

        // keep max weight
        recommendations.merge(courseId, currentWeight, Math::max);

        // go deeper
        for (Edge edge : getNeighbors(courseId)) {
            if (!visited.contains(edge.targetCourseId)) {
                double newWeight = currentWeight * edge.weight * 0.8; // decay
                dfsRecommendations(edge.targetCourseId, maxDepth, currentDepth + 1,
                        newWeight, visited, recommendations);
            }
        }

        visited.remove(courseId);
    }

    // first-level only
    public List<Integer> getDirectRecommendations(int courseId) {
        List<Edge> neighbors = getNeighbors(courseId);
        neighbors.sort((a, b) -> Double.compare(b.weight, a.weight));

        List<Integer> result = new ArrayList<>();
        for (Edge edge : neighbors) {
            result.add(edge.targetCourseId);
        }
        return result;
    }

    // has vertex?
    public boolean containsVertex(int courseId) {
        return adjacencyList.containsKey(courseId);
    }

    // vertex count
    public int getVertexCount() {
        return adjacencyList.size();
    }

    // clear all
    public void clear() {
        adjacencyList.clear();
    }
}

