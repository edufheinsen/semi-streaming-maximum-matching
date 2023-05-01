import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.generate.CompleteBipartiteGraphGenerator;
import org.jgrapht.generate.GnpRandomBipartiteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GraphUtils {
    public static void isMatching(Set<DefaultEdge> matching, Set<DefaultEdge> edgeSet) {
        Set<Integer> verticesCoveredByReturnedMatching = new HashSet<>();
        Graph<Integer, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);
        for (DefaultEdge edge : matching) {
            int s = g.getEdgeSource(edge);
            int t = g.getEdgeTarget(edge);
            assertAll(() -> assertTrue(edgeSet.contains(edge)),
                    () -> assertFalse(verticesCoveredByReturnedMatching.contains(s)),
                    () -> assertFalse(verticesCoveredByReturnedMatching.contains(t)));
            verticesCoveredByReturnedMatching.add(s);
            verticesCoveredByReturnedMatching.add(t);
        }
    }

    public static Pair<Graph<Integer, DefaultEdge>, GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge>> generateRandomBipartiteGraph(int partitionSize, double p, long seed) {
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        Graph<Integer, DefaultEdge> gnpBipartiteGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> gnpRandomBipartiteGraphGenerator =
                new GnpRandomBipartiteGraphGenerator<>(partitionSize, partitionSize, p, seed);
        gnpRandomBipartiteGraphGenerator.generateGraph(gnpBipartiteGraph);
        return Pair.with(gnpBipartiteGraph, gnpRandomBipartiteGraphGenerator);
    }

    public static Pair<Graph<Integer, DefaultEdge>, CompleteBipartiteGraphGenerator<Integer, DefaultEdge>> generateCompleteBipartiteGraph(Set<Integer> firstPartition, Set<Integer> secondPartition) {
        Graph<Integer, DefaultEdge> completeBipartiteGraph =
                new SimpleGraph<>(DefaultEdge.class);
        for (int vertex : firstPartition) {
            completeBipartiteGraph.addVertex(vertex);
        }
        for (int vertex : secondPartition) {
            completeBipartiteGraph.addVertex(vertex);
        }
        CompleteBipartiteGraphGenerator<Integer, DefaultEdge> completeBipartiteGraphGenerator =
                new CompleteBipartiteGraphGenerator<>(firstPartition, secondPartition);
        completeBipartiteGraphGenerator.generateGraph(completeBipartiteGraph);
        return Pair.with(completeBipartiteGraph, completeBipartiteGraphGenerator);
    }

    // Find a maximal matching (1/2-approximation to the MCM) using the standard greedy algorithm
    public static Set<DefaultEdge> findMaximalMatching(List<DefaultEdge> stream, Graph<Integer, DefaultEdge> g) {
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
}
