import org.jgrapht.Graph;
import org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching;
import org.jgrapht.generate.CompleteBipartiteGraphGenerator;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GnmRandomBipartiteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ALTTest {



    @Test
    void testConstructorThrowsExceptionForNonBipartiteGraph() {

        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        int size = 10;

        Graph<Integer, DefaultEdge> completeGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        CompleteGraphGenerator<Integer, DefaultEdge> completeGenerator =
                new CompleteGraphGenerator<>(size);

        completeGenerator.generateGraph(completeGraph);

        Set<DefaultEdge> edgeSet = completeGraph.edgeSet();
        List<DefaultEdge> stream = new ArrayList<>(edgeSet);
        assertThrows(IllegalArgumentException.class, () -> new ALT(stream));
    }

    @Test
    void testConstructorWorksForBipartiteGraph() {
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        int partitionSize = 100;

        Graph<Integer, DefaultEdge> completeBipartiteGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        CompleteBipartiteGraphGenerator<Integer, DefaultEdge> completeBipartiteGraphGenerator =
                new CompleteBipartiteGraphGenerator<>(partitionSize, partitionSize);

        completeBipartiteGraphGenerator.generateGraph(completeBipartiteGraph);

        Set<DefaultEdge> edgeSet = completeBipartiteGraph.edgeSet();
        List<DefaultEdge> stream = new ArrayList<>(edgeSet);
        ALT alt = new ALT(stream);
    }

    @Test
    void testFindApproximateMaxMatchingReturnsMatching() {
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        int partitionSize = 100;
        int numEdges = 70;

        Graph<Integer, DefaultEdge> gnmBipartiteGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> gnmRandomBipartiteGraphGenerator =
                new GnmRandomBipartiteGraphGenerator<>(partitionSize, partitionSize, numEdges);

        gnmRandomBipartiteGraphGenerator.generateGraph(gnmBipartiteGraph);

        Set<DefaultEdge> edgeSet = gnmBipartiteGraph.edgeSet();
        List<DefaultEdge> stream = new ArrayList<>(edgeSet);
        ALT alt = new ALT(stream);
        double eps = 0.25;
        Set<DefaultEdge> matching = alt.findApproximateMaxMatching(eps);
        Set<Integer> verticesCoveredByReturnedMatching = new HashSet<>();
        Set<Integer> originalVertexSet = gnmBipartiteGraph.vertexSet();
        for (DefaultEdge edge : matching) {
            int s = gnmBipartiteGraph.getEdgeSource(edge);
            int t = gnmBipartiteGraph.getEdgeTarget(edge);
            assertAll(() -> assertTrue(originalVertexSet.contains(s)),
                    () -> assertTrue(originalVertexSet.contains(t)),
                    () -> assertTrue(gnmBipartiteGraph.containsEdge(s, t)),
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

        int partitionSize = 100;
        int numEdges = 70;

        Graph<Integer, DefaultEdge> gnmBipartiteGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> gnmRandomBipartiteGraphGenerator =
                new GnmRandomBipartiteGraphGenerator<>(partitionSize, partitionSize, numEdges);

        gnmRandomBipartiteGraphGenerator.generateGraph(gnmBipartiteGraph);

        Set<DefaultEdge> edgeSet = gnmBipartiteGraph.edgeSet();
        List<DefaultEdge> stream = new ArrayList<>(edgeSet);
        ALT alt = new ALT(stream);
        double eps = 0.25;
        Set<DefaultEdge> matching = alt.findApproximateMaxMatching(eps);

       int returnedMatchingSize = matching.size();
       HopcroftKarpMaximumCardinalityBipartiteMatching<Integer, DefaultEdge> actualMaxMatching
                = new HopcroftKarpMaximumCardinalityBipartiteMatching<>(gnmBipartiteGraph, gnmRandomBipartiteGraphGenerator.getFirstPartition(), gnmRandomBipartiteGraphGenerator.getSecondPartition());
       int actualMaxMatchingSize = actualMaxMatching.getMatching().getEdges().size();
       double correctedEps = eps + Math.pow(10, -10);
       assertTrue(returnedMatchingSize >= (1 - correctedEps) * actualMaxMatchingSize);
    }
}