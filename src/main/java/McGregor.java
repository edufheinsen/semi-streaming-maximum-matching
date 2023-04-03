import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.color.GreedyColoring;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class McGregor {
    private final List<DefaultEdge> stream;
    private int k;
    private int r;

    public McGregor(List<DefaultEdge> stream) {
        this.stream = stream;
    }

    public Set<DefaultEdge> findApproximateMaxMatching(double eps) {
        if (eps <= 0 || eps >= 1) {
            throw new IllegalArgumentException("Argument to findApproximateMatching must be a real number strictly between 0 and 1");
        }
        Graph<Integer, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Set<DefaultEdge> M = findMaximalMatching(this.stream, g);
        this.k = (int) Math.ceil(1.0/eps + 1);
        this.r = 4 * (k * k) * (8*k + 10) * (k-1) * ((int) Math.pow(2*k, k) );

        for (int j = 1; j <= r; j++) {
            List<Set<DefaultEdge>> augmentingPaths = new ArrayList<>(); // TODO: make more efficient! can just keep maximum
            for (int i = 1; i <= k; i++) {
                Set<DefaultEdge> augmentingPath = findAugPaths(this.stream, M, i);
                augmentingPaths.add(augmentingPath);
            }
            M = maxCardinalityAugmentingPath(augmentingPaths);
        }

        return M;
    }

    private Set<DefaultEdge> maxCardinalityAugmentingPath(List<Set<DefaultEdge>> augmentingPaths) {
        int maxCard = -1;
        Set<DefaultEdge> longestAugmentingPath = new HashSet<>();
        for (Set<DefaultEdge> augmentingPath : augmentingPaths) {
            int cardinality = augmentingPath.size();
            if (cardinality > maxCard) {
                maxCard = cardinality;
                longestAugmentingPath = augmentingPath;
            }
        }
        return longestAugmentingPath;
    }

    private Set<DefaultEdge> findMaximalMatching(List<DefaultEdge> stream, Graph<Integer, DefaultEdge> g) {
        Set<DefaultEdge> matching = new HashSet<>();
        Set<Integer> verticesCoveredByMatching = new HashSet<>();

        for (DefaultEdge edge : stream) {
            int s = g.getEdgeSource(edge);
            int t = g.getEdgeTarget(edge);
            if (!verticesCoveredByMatching.contains(s) && !verticesCoveredByMatching.contains(t)) {
                matching.add(edge);
                verticesCoveredByMatching.add(s);
                verticesCoveredByMatching.add(t);
            }
        }
        return matching;
    }

    private Set<DefaultEdge> findAugPaths(List<DefaultEdge> stream, Set<DefaultEdge> M, int i) {
        Triplet<Graph<Integer, DefaultEdge>, List<Set<Integer>>, List<Set<DefaultEdge>>> GPrime = createLayerGraph(stream, M, i);
        Set<DefaultEdge> P = findLayerPaths(GPrime.getValue0(), GPrime.getValue1().get(i+1), 1.0 / (r * (2*k + 2)), i+1);
        return setDifference(M, P);
    }

    private Triplet<Graph<Integer, DefaultEdge>, List<Set<Integer>>, List<Set<DefaultEdge>>> createLayerGraph(List<DefaultEdge> stream, Set<DefaultEdge> M, int i) {
        Graph<Integer, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);

        Set<Integer> freeVertices = getFreeVertices(stream, M);

        Map<Integer, Pair<Integer, String>> vertexL = new HashMap<>();
        Map<Integer, Set<Integer>> vertexLInv = new HashMap<>();
        Map<DefaultEdge, Pair<Integer, String>> edgeL = new HashMap<>();
        Map<Integer, Set<DefaultEdge>> edgeLInv = new HashMap<>();


        for (int v : freeVertices) {
            int randLayer = ThreadLocalRandom.current().nextBoolean() ? 0 : (i+1);
            vertexL.put(v, Pair.with(randLayer, ""));
            if (!vertexLInv.containsKey(randLayer)) {
                vertexLInv.put(randLayer, new HashSet<>());
            }
            Set<Integer> vertexSet = vertexLInv.get(randLayer);
            vertexSet.add(v);
        }

        for (DefaultEdge edge : M) {
            int u = g.getEdgeSource(edge);
            int v = g.getEdgeTarget(edge);
            int j = ThreadLocalRandom.current().nextInt(1, i + 1);
            edgeL.put(edge, Pair.with(j, ""));
            if (!edgeLInv.containsKey(j)) {
                edgeLInv.put(j, new HashSet<>());
            }
            Set<DefaultEdge> edgeSet = edgeLInv.get(j);
            edgeSet.add(edge);

            vertexL.put(u, Pair.with(j, "a"));
            vertexL.put(v, Pair.with(j, "b"));

            if (!vertexLInv.containsKey(j)) {
                vertexLInv.put(j, new HashSet<>());
            }
            Set<Integer> vertexSet = vertexLInv.get(j);
            vertexSet.add(u);
            vertexSet.add(v);

        }

        Map<Integer, Set<DefaultEdge>> E = new HashMap<>();
        Map<Integer, Set<Integer>> L = new HashMap<>();

        E.put(i, new HashSet<>());
        E.put(0, new HashSet<>());

        for (DefaultEdge edge : stream) {
            int u = g.getEdgeSource(edge);
            int v = g.getEdgeTarget(edge);
            Pair<Integer, String> uLayer = vertexL.get(u);
            Pair<Integer, String> vLayer = vertexL.get(v);
            if (uLayer.getValue0() == (i+1) && (vLayer.getValue0() == i && vLayer.getValue1().equals("a"))) {
                E.get(i).add(edge);
            }
            if ((uLayer.getValue0() == 1 && uLayer.getValue1() == "b") && vLayer.getValue0() == 0) {
                E.get(0).add(edge);
            }
        }

        // for j = 0 to i + 1
        //5. do Lj ← l−1(j)
        for (int j = 0; j <= i + 1; j++) {
            L.put()
        }

        for (int j = 1; j <= i - 1; j++) {
            Set<DefaultEdge> edgeSetJ = new HashSet<>();
            for (DefaultEdge edge : stream) {
                int u = g.getEdgeSource(edge);
                int v = g.getEdgeTarget(edge);
                if (vertexL.get(u).getValue0() == (j+1) && vertexL.get(u).getValue1().equals("b")
                        && vertexL.get(v).getValue0() == j && vertexL.get(v).getValue1().equals("a")) {
                    edgeSetJ.add(edge);
                }
            }
            E.put(j, edgeSetJ);
        }

        // TODO: Implement!
        return Triplet.with(g, L, E);
    }

    private Set<Integer> getFreeVertices(List<DefaultEdge> stream, Set<DefaultEdge> M) {
        return new HashSet<>();
    }



    private Set<DefaultEdge> findLayerPaths(Graph<Integer, DefaultEdge> GPrime, Set<Integer> S, double delta, int j) {
        // TODO: Implement!
        return new HashSet<>();
    }

    private Set<DefaultEdge> setDifference(Set<DefaultEdge> setA, Set<DefaultEdge> setB) {
        Set<DefaultEdge> symmetricDiff = new HashSet<>();
        for (DefaultEdge edge : setA) {
            if (!setB.contains(edge)) {
                symmetricDiff.add(edge);
            }
        }
        for (DefaultEdge edge : setB) {
            if (!setA.contains(edge)) {
                symmetricDiff.add(edge);
            }
        }
        return symmetricDiff;
    }
}
