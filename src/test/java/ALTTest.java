import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching;
import org.jgrapht.generate.CompleteBipartiteGraphGenerator;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GnpRandomBipartiteGraphGenerator;
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
    void testFindApproximateMaxMatching() {
        int partitionSize = 1000;
        double p = 0.0006 / 10;
        long seed = 42;
        Pair<Graph<Integer, DefaultEdge>,GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge>> randomGraphPair = GraphUtils.generateRandomBipartiteGraph(partitionSize, p, seed);
        Graph<Integer, DefaultEdge> gnpRandomBipartiteGraph = randomGraphPair.getValue0();
        GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> gnpRandomBipartiteGraphGenerator = randomGraphPair.getValue1();
        Set<Integer> firstPartition = gnpRandomBipartiteGraphGenerator.getFirstPartition();
        Set<Integer> secondPartition = gnpRandomBipartiteGraphGenerator.getSecondPartition();
        Pair<Graph<Integer, DefaultEdge>,CompleteBipartiteGraphGenerator<Integer, DefaultEdge>> completeGraphPair = GraphUtils.generateCompleteBipartiteGraph(firstPartition, secondPartition);
        Graph<Integer, DefaultEdge> completeBipartiteGraph = completeGraphPair.getValue0();
        for (Graph<Integer, DefaultEdge> graph : Arrays.asList(gnpRandomBipartiteGraph, completeBipartiteGraph)) {
            Set<DefaultEdge> originalEdgeSet = graph.edgeSet();
            List<DefaultEdge> stream = new ArrayList<>(originalEdgeSet);
            // Compute the actual MCM using the Hopcroft-Karp algorithm
            HopcroftKarpMaximumCardinalityBipartiteMatching<Integer, DefaultEdge> actualMaxMatching
                    = new HopcroftKarpMaximumCardinalityBipartiteMatching<>(graph, firstPartition, secondPartition);
            int actualMaxMatchingSize = actualMaxMatching.getMatching().getEdges().size();
            ALT alt = new ALT(stream);
            List<Double> epsValues = Arrays.asList(1.0/2, 1.0/3, 1.0/5, 1.0/7); // values of eps to test for correctness
            for (double eps : epsValues) {
                Set<DefaultEdge> ALTMatching = alt.findApproximateMaxMatching(eps);
                GraphUtils.isMatching(ALTMatching, originalEdgeSet); // verify that the returned set of edges is a matching
                assertTrue(ALTMatching.size() >= (1 - eps) * actualMaxMatchingSize); // verify that the returned matching is a (1 - eps)-approximation to the MCM
            }
        }

    }
}


