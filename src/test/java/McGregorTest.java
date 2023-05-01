import org.jgrapht.Graph;
import org.jgrapht.alg.matching.DenseEdmondsMaximumCardinalityMatching;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class McGregorTest {
    @Test
    void testFindApproximateMaxMatching() {
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;
            @Override
            public Integer get() {
                return id++;
            }
        };
        int nRandom = 100;
        int nComplete = 20; // Number of vertices in the complete graph is smaller to avoid excessive runtime
        double p = 0.008;
        long seed = 42;
        Graph<Integer, DefaultEdge> gnpRandomGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);
        GnpRandomGraphGenerator<Integer, DefaultEdge> gnpRandomGraphGenerator =
                new GnpRandomGraphGenerator<>(nRandom, p, seed);
        gnpRandomGraphGenerator.generateGraph(gnpRandomGraph);
        Graph<Integer, DefaultEdge> completeGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);
        CompleteGraphGenerator<Integer, DefaultEdge> completeGraphGenerator =
                new CompleteGraphGenerator<>(nComplete);
        completeGraphGenerator.generateGraph(completeGraph);
        for (Graph<Integer, DefaultEdge> graph : Arrays.asList(gnpRandomGraph, completeGraph)) {
            // Compute the actual MCM using the Blossom algorithm
            DenseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> actualMaxMatching
                    = new DenseEdmondsMaximumCardinalityMatching<>(graph);
            int actualMaxMatchingSize = actualMaxMatching.getMatching().getEdges().size();
            Set<DefaultEdge> originalEdgeSet = graph.edgeSet();
            List<DefaultEdge> stream = new ArrayList<>(originalEdgeSet);
            McGregor mcGregor = new McGregor(stream);
            List<Double> epsValues = Arrays.asList(4.0/5, 2.0/3, 1.0/2); // values of eps to test
            List<Double> probabilities = Arrays.asList(0.5, 0.75, 0.875);
            int trials = 3;
            double allowedError = 0.005;
            for (double eps : epsValues) {
                for (double prob : probabilities) {
                    int numSuccesses = 0;
                    int parallelRuns = (int) Math.ceil(Math.log(1 / (1 - prob)));
                    for (int i = 0; i < trials; i++) {
                        for (int j = 0; j < parallelRuns; j++) {
                            Set<DefaultEdge> matching = mcGregor.findApproximateMaxMatching(eps);
                            GraphUtils.isMatching(matching, originalEdgeSet); // verify that the returned edge set is a matching
                            if (matching.size() >= (1.0 / (1 + eps)) * actualMaxMatchingSize) {
                                numSuccesses++;
                                break;
                            }
                        }
                    }
                    double successProp = numSuccesses / (double) trials;
                    assertTrue(successProp >= prob - allowedError); // verify that the algorithm returns a 1/(1+eps)-approximation with the desired probability
                }
            }
        }

    }

}