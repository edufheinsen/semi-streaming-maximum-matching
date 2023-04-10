import org.jgrapht.Graph;
import org.jgrapht.alg.matching.DenseEdmondsMaximumCardinalityMatching;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class McGregorTest {
    // TODO: Refactor this out so that McGregorTest and ALTTest can share
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
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        int n = 1000;
        double p = 0.008;

        Graph<Integer, DefaultEdge> gnpRandomGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        GnpRandomGraphGenerator<Integer, DefaultEdge> gnpRandomGraphGenerator =
                new GnpRandomGraphGenerator<>(n, p);

        gnpRandomGraphGenerator.generateGraph(gnpRandomGraph);

        Set<DefaultEdge> originalEdgeSet = gnpRandomGraph.edgeSet();

        DenseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> actualMaxMatching
                = new DenseEdmondsMaximumCardinalityMatching<>(gnpRandomGraph);
        int actualMaxMatchingSize = actualMaxMatching.getMatching().getEdges().size();
        System.out.println("The actual max matching size is " + actualMaxMatchingSize);

        List<DefaultEdge> stream = new ArrayList<>(originalEdgeSet);

        List<Double> epsValues = Arrays.asList(1.0/2); // values of eps to test
        List<Double> probabilities = Arrays.asList(0.5, 0.875);
        // TODO - test on multiple deterministically constructed graphs
        int trials = 1;
        double allowedError = 0.05;
        for (double eps : epsValues) {
            for (double prob : probabilities) {
                int numSuccesses = 0;
                int parallelRuns = (int) Math.ceil(Math.log(1 / (1 - p)));
                for (int i = 0; i < trials; i++) {
                    System.out.println("Now testing eps: " + eps + ", prob: " + prob + ", trial: " + i);
                    for (int j = 0; j < parallelRuns; j++) {
                        McGregor mcGregor = new McGregor(stream);
                        Set<DefaultEdge> matching = mcGregor.findApproximateMaxMatching(eps);
                        isMatching(matching, originalEdgeSet);
                        System.out.println("The size of the McGregor matching is " + matching.size());
                        if (matching.size() >= (1.0 / (1 + eps)) * actualMaxMatchingSize) {
                            numSuccesses++;
                            break;
                        }
                    }
                }
                double successProp = numSuccesses / (double) trials;
                System.out.println("The empirical success proportion is " + successProp);
                assertTrue(successProp >= prob - allowedError);
            }
        }
    }

}