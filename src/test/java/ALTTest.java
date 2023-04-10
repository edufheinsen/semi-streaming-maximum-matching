import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching;
import org.jgrapht.generate.CompleteBipartiteGraphGenerator;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GnmRandomBipartiteGraphGenerator;
import org.jgrapht.generate.GnpRandomBipartiteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
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

    private Pair<Graph<Integer, DefaultEdge>, GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge>>  generateRandomBipartiteGraph(int partitionSize, double p) {
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
                new GnpRandomBipartiteGraphGenerator<>(partitionSize, partitionSize, p);
        gnpRandomBipartiteGraphGenerator.generateGraph(gnpBipartiteGraph);
        return Pair.with(gnpBipartiteGraph,gnpRandomBipartiteGraphGenerator);
    }

    private void isMatching(Set<DefaultEdge> matching, Set<DefaultEdge> edgeSet) {
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

    @Test
    void testFindApproximateMaxMatching() {
        int partitionSize = 1000;
        double p = 0.001;
        Pair<Graph<Integer, DefaultEdge>,GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge>> pair = generateRandomBipartiteGraph(partitionSize, p);
        Graph<Integer, DefaultEdge> g = pair.getValue0();
        GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator = pair.getValue1();
        Set<DefaultEdge> originalEdgeSet = g.edgeSet();
        List<DefaultEdge> stream = new ArrayList<>(originalEdgeSet);
        ALT alt = new ALT(stream);
        HopcroftKarpMaximumCardinalityBipartiteMatching<Integer, DefaultEdge> actualMaxMatching
                = new HopcroftKarpMaximumCardinalityBipartiteMatching<>(g, generator.getFirstPartition(), generator.getSecondPartition());

        int actualMaxMatchingSize = actualMaxMatching.getMatching().getEdges().size();
        // System.out.println("The actual max matching has size " + actualMaxMatchingSize);
        // System.out.println("The original edge set is " + originalEdgeSet);

        List<Double> epsValues = Arrays.asList(1.0/2, 1.0/3, 1.0/5); // values of eps to test
        // TODO - test on multiple deterministically constructed graphs
        for (double eps : epsValues) {
            // System.out.println("Now testing eps value " + eps);
            Set<DefaultEdge> ALTMatching = alt.findApproximateMaxMatching(eps);
            // System.out.println("The ALT matching has size " + ALTMatching.size());
            // System.out.println("The ALT matching is " + ALTMatching);
            isMatching(ALTMatching, originalEdgeSet);

            assertTrue(ALTMatching.size() >= (1 - eps) * actualMaxMatchingSize);
        }
    }
}