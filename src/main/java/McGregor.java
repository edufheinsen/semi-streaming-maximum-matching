import org.javatuples.Triplet;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.color.GreedyColoring;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.util.*;

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
        Set<DefaultEdge> M = findMaximalMatching(this.stream);
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

    private Set<DefaultEdge> findMaximalMatching(List<DefaultEdge> stream) {
        // TODO: Implement!
        return new HashSet<>();
    }

    private Set<DefaultEdge> findAugPaths(List<DefaultEdge> stream, Set<DefaultEdge> M, int i) {
        Triplet<Graph<Integer, DefaultEdge>, List<Set<Integer>>, List<Set<DefaultEdge>>> GPrime = createLayerGraph(stream, M, i);
        Set<DefaultEdge> P = findLayerPaths(GPrime.getValue0(), GPrime.getValue1().get(i+1), 1.0 / (r * (2*k + 2)), i+1);
        return setDifference(M, P);
    }

    private Triplet<Graph<Integer, DefaultEdge>, List<Set<Integer>>, List<Set<DefaultEdge>>> createLayerGraph(List<DefaultEdge> stream, Set<DefaultEdge> M, int i) {
        Graph<Integer, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Set<Integer>> L = new ArrayList<>();
        List<Set<DefaultEdge>> E = new ArrayList<>();
        // TODO: I  Implement!
        return Triplet.with(g, L, E);
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
