import org.jgrapht.Graph;
import org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching;
import org.jgrapht.generate.GnmRandomBipartiteGraphGenerator;
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

        int n = 1000;
        double p = 0.7;

        Graph<Integer, DefaultEdge> gnpRandomGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        GnpRandomGraphGenerator<Integer, DefaultEdge> gnpRandomGraphGenerator =
                new GnpRandomGraphGenerator<>(n, p);

        gnpRandomGraphGenerator.generateGraph(gnpRandomGraph);

        Set<DefaultEdge> edgeSet = gnpRandomGraph.edgeSet();
        List<DefaultEdge> stream = new ArrayList<>(edgeSet);
        McGregor mcGregor = new McGregor(stream);
        double eps = 0.25;
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

}