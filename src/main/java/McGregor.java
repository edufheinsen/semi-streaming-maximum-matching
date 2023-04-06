import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.jgrapht.Graph;
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
            List<Set<DefaultEdge>> matchings = new ArrayList<>(); // TODO: make more efficient! can just keep maximum
            for (int i = 1; i <= k; i++) {
                Set<DefaultEdge> matching = findAugPaths(M, i);
                matchings.add(matching);
            }
            M = maxCardinalityMatching(matchings);
        }

        return M;
    }

    private Set<DefaultEdge> maxCardinalityMatching(List<Set<DefaultEdge>> matchings) {
        int maxCard = -1;
        Set<DefaultEdge> largestMatching = new HashSet<>();
        for (Set<DefaultEdge> matching : matchings) {
            int cardinality = matching.size();
            if (cardinality > maxCard) {
                maxCard = cardinality;
                largestMatching = matching;
            }
        }
        return largestMatching;
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

    private Set<DefaultEdge> findAugPaths(Set<DefaultEdge> M, int i) {
        Quartet<Map<Integer, Pair<Integer, String>>, Set<Integer>, Map<Integer, Integer>, Map<Integer, Integer>> quartet
                = createLayerGraph(M, i);
        Map<Integer, Pair<Integer, String>> L = quartet.getValue0();
        Set<Integer> firstLayer = quartet.getValue1();
        Map<Integer, Integer> matching = quartet.getValue2();
        Map<Integer, Integer> layerSizes = quartet.getValue3();
        Map<Integer, Integer> tags = new HashMap<>();
        for (int j = 0; j <= i + 1; j++) {
            if (layerSizes.get(j) == 0) {
                return M;
            }
        }
        findLayerPaths(L, firstLayer, 1.0 / (r * (2*k + 2)), i+1, tags, matching, layerSizes);
        Set<DefaultEdge> P = getPathsFromTags(tags, firstLayer);
        return getSymmetricDifference(M, P);
    }

    // just returns map from vertices to their layer (each layer is a Pair<Integer, String>)
    private Quartet<Map<Integer, Pair<Integer, String>>, Set<Integer>, Map<Integer, Integer>,
            Map<Integer, Integer>> createLayerGraph(Set<DefaultEdge> M, int i) {
        Graph<Integer, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);

        Set<Integer> freeVertices = getFreeVertices(M);

        Map<Integer, Pair<Integer, String>> vertexL = new HashMap<>();

        Map<Integer, Integer> matching = new HashMap<>();

        Map<Integer, Integer> layerSizes = new HashMap<>();

        for (int j = 0; j <= i + 1; j++) {
            layerSizes.put(j, 0);
        }

        Set<Integer> firstLayer = new HashSet<>();
        for (int v : freeVertices) {
            int randLayer = ThreadLocalRandom.current().nextBoolean() ? 0 : (i+1);
            vertexL.put(v, Pair.with(randLayer, "a")); // Suspicious - be careful if this nonempty tagging breaks something
            layerSizes.put(randLayer, layerSizes.getOrDefault(i+1,0) + 1);
            if (randLayer == (i+1)) {
                firstLayer.add(v);
            }
        }

        for (DefaultEdge edge : M) {
            int u = g.getEdgeSource(edge);
            int v = g.getEdgeTarget(edge);
            int j = ThreadLocalRandom.current().nextInt(1, i + 1);
            layerSizes.put(j, layerSizes.getOrDefault(j, 0) + 1);
            vertexL.put(u, Pair.with(j, "a"));
            vertexL.put(v, Pair.with(j, "b"));
            matching.put(u, v);
            matching.put(v, u);
        }

        return Quartet.with(vertexL, firstLayer, matching, layerSizes);
    }

    private Set<DefaultEdge> getPathsFromTags(Map<Integer, Integer> tags, Set<Integer> firstLayer) {
        Graph<Integer, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Set<Set<Integer>> edgeSet = new HashSet<>(); // TODO: Does this overuse space?? storing all of P????
        Set<DefaultEdge> P = new HashSet<>();
        if (tags == null || tags.size() == 0) {
            return P;
        }
        for (int v : firstLayer) {
            if (tags.get(v) == -1) {
                continue;
            }
            while (tags.get(v) != v) {
                int u = tags.get(v);
                Set<Integer> newEdge = new HashSet<>();
                newEdge.add(v);
                newEdge.add(u);
                edgeSet.add(newEdge);
                v = u;
            }
        }
        for (DefaultEdge edge : stream) {
            int v = g.getEdgeSource(edge);
            int u = g.getEdgeTarget(edge);
            Set<Integer> newEdge = new HashSet<>();
            newEdge.add(v);
            newEdge.add(u);
            if (edgeSet.contains(newEdge)) {
                P.add(edge);
            }
        }
        return P;
    }

    private Set<Integer> getFreeVertices(Set<DefaultEdge> M) {
        Graph<Integer, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Set<Integer> verticesCoveredByMatching = new HashSet<>();
        Set<Integer> freeVertices = new HashSet<>();
        for (DefaultEdge edge : M) {
            int s = g.getEdgeSource(edge);
            int t = g.getEdgeTarget(edge);
            assert !verticesCoveredByMatching.contains(s) && !verticesCoveredByMatching.contains(t);
            verticesCoveredByMatching.add(s);
            verticesCoveredByMatching.add(t);
        }
        for (DefaultEdge edge : stream) {
            int s = g.getEdgeSource(edge);
            int t = g.getEdgeTarget(edge);
            if (!verticesCoveredByMatching.contains(s)) {
                freeVertices.add(s);
            }
            if (!verticesCoveredByMatching.contains(t)) {
                freeVertices.add(t);
            }
        }
        return freeVertices;
    }



    private void findLayerPaths(Map<Integer, Pair<Integer, String>> L, Set<Integer> S,
                                double delta, int j, Map<Integer, Integer> tags, Map<Integer, Integer> matching,
                                Map<Integer, Integer> layerSizes) {
        System.out.println("Calling findLayerPaths");
        Graph<Integer, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Set<Integer> SPrime = new HashSet<>();
        Map<Integer, Integer> Gamma = new HashMap<>();

        Set<Integer> verticesCoveredByMatching = new HashSet<>();

        for (DefaultEdge edge : stream) {
            int s = g.getEdgeSource(edge);
            int t = g.getEdgeTarget(edge);
            Pair<Integer, String> sLayer = L.get(s);
            Pair<Integer, String> tLayer = L.get(t);

            if ((S.contains(s) && tLayer.equals(Pair.with(j - 1, "a")) && !tags.containsKey(t)) ||
                    (S.contains(t) && sLayer.equals(Pair.with(j - 1, "a")) && !tags.containsKey(s))) {
                if (!verticesCoveredByMatching.contains(s) && !verticesCoveredByMatching.contains(t)) {
                    Gamma.put(s, t);
                    Gamma.put(t, s);
                    verticesCoveredByMatching.add(s);
                    verticesCoveredByMatching.add(t);
                }
                if (L.get(s).getValue0() == (j - 1)) {
                    SPrime.add(matching.get(s));
                } else {
                    assert L.get(t).getValue0() == (j -1);
                    SPrime.add(matching.get(t));
                }
            }
        }

        if (j == 1) {
            for (int u : S) {
                if (Gamma.containsKey(u) && L.get(Gamma.get(u)).getValue0() == 0) {
                    tags.put(u, Gamma.get(u));
                    tags.put(matching.get(u), u);
                    tags.put(Gamma.get(u), Gamma.get(u));
                } else {
                    tags.put(u, -1);
                    tags.put(matching.get(u), -1);
                }
            }
            return;
        }
        System.out.println(layerSizes);
        while (SPrime.size() > delta * layerSizes.get(j-1)) {
            findLayerPaths(L, SPrime, delta * delta, j - 1, tags, matching, layerSizes);
            for (int v : SPrime) {
                if (!tags.containsKey(v) || tags.get(v) != -1) {
                    tags.put(Gamma.get(matching.get(v)), matching.get(v));
                }
            }

            SPrime = new HashSet<>();
            Gamma = new HashMap<>();

            verticesCoveredByMatching = new HashSet<>();
            for (DefaultEdge edge : stream) {
                int s = g.getEdgeSource(edge);
                int t = g.getEdgeTarget(edge);
                if ((S.contains(s) && !tags.containsKey(s) && L.get(t).equals(Pair.with(j - 1, "a")) && !tags.containsKey(t)) ||
                        (S.contains(t) && !tags.containsKey(t) && L.get(s).equals(Pair.with(j - 1, "a")) && !tags.containsKey(s))) {
                    if (!verticesCoveredByMatching.contains(s) && !verticesCoveredByMatching.contains(t)) {
                        Gamma.put(s, t);
                        Gamma.put(t, s);
                        verticesCoveredByMatching.add(s);
                        verticesCoveredByMatching.add(t);
                    }
                    if (L.get(s).getValue0() == (j - 1)) {
                        SPrime.add(matching.get(s));
                    } else {
                        assert L.get(t).getValue0() == (j - 1);
                        SPrime.add(matching.get(t));
                    }
                }
            }
        }

        for (int v : S) {
            if (!tags.containsKey(v)) {
                tags.put(v, -1);
                tags.put(matching.get(v),-1);
            }
        }
    }

    private Set<DefaultEdge> getSymmetricDifference(Set<DefaultEdge> setA, Set<DefaultEdge> setB) {
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
