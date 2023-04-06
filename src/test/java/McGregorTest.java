import org.jgrapht.Graph;
import org.jgrapht.alg.matching.DenseEdmondsMaximumCardinalityMatching;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class McGregorTest {
    @Test
    void testFindApproximateMaxMatchingReturnsMatching() {
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        int n = 20;
        double p = 0.7;

        Graph<Integer, DefaultEdge> gnpRandomGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        GnpRandomGraphGenerator<Integer, DefaultEdge> gnpRandomGraphGenerator =
                new GnpRandomGraphGenerator<>(n, p);

        gnpRandomGraphGenerator.generateGraph(gnpRandomGraph);

        Set<DefaultEdge> edgeSet = gnpRandomGraph.edgeSet();
        List<DefaultEdge> stream = new ArrayList<>(edgeSet);
        McGregor mcGregor = new McGregor(stream);
        // can't set eps too small due to number of passes exponential in (1/eps)
        double eps = 0.95;
        Set<DefaultEdge> matching = mcGregor.findApproximateMaxMatching(eps);
        Set<Integer> verticesCoveredByReturnedMatching = new HashSet<>();
        Set<Integer> originalVertexSet = gnpRandomGraph.vertexSet();
        for (DefaultEdge edge : matching) {
            int s = gnpRandomGraph.getEdgeSource(edge);
            int t = gnpRandomGraph.getEdgeTarget(edge);
            assertAll(() -> assertTrue(originalVertexSet.contains(s)),
                    () -> assertTrue(originalVertexSet.contains(t)),
                    () -> assertTrue(gnpRandomGraph.containsEdge(s, t)),
                    () -> assertFalse(verticesCoveredByReturnedMatching.contains(s)),
                    () -> assertFalse(verticesCoveredByReturnedMatching.contains(t)));
            verticesCoveredByReturnedMatching.add(s);
            verticesCoveredByReturnedMatching.add(t);
        }
    }

    @Test
    void testFindApproximateMaxMatchingReturnsGoodApproximationOfMaxMatching() {
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        int n = 20;
        double p = 0.7;

        Graph<Integer, DefaultEdge> gnpRandomGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        GnpRandomGraphGenerator<Integer, DefaultEdge> gnpRandomGraphGenerator =
                new GnpRandomGraphGenerator<>(n, p);

        gnpRandomGraphGenerator.generateGraph(gnpRandomGraph);

        Set<DefaultEdge> edgeSet = gnpRandomGraph.edgeSet();
        List<DefaultEdge> stream = new ArrayList<>(edgeSet);
        McGregor mcGregor = new McGregor(stream);
        // can't set eps too small due to number of passes exponential in (1/eps)
        double eps = 0.95;
        Set<DefaultEdge> matching = mcGregor.findApproximateMaxMatching(eps);
        System.out.println("The matching found by McGregor is");
        System.out.println(matching);
        System.out.println("with size " + matching.size());
        System.out.println("The true cardinality of the maximum matching is");
        DenseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> actualMaxMatching
                = new DenseEdmondsMaximumCardinalityMatching<>(gnpRandomGraph);
        int actualMaxMatchingSize = actualMaxMatching.getMatching().getEdges().size();
        System.out.println(actualMaxMatchingSize);

        // TODO: Turn this into a real test - over a large number of trials, the algorithm should return a good
        // TODO: approximation of the maximum matching with a certain (high) probability
    }

}