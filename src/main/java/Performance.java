import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.matching.DenseEdmondsMaximumCardinalityMatching;
import org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GnpRandomBipartiteGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;

import java.util.*;
import java.util.function.Supplier;

public class Performance {
    private static void testALTPerformance() {
        int partitionSize = 10000;
        double p = 0.0006 / 10;
        long seed = 42;
        Pair<Graph<Integer, DefaultEdge>, GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge>> pair = GraphUtils.generateRandomBipartiteGraph(partitionSize, p, seed);
        Graph<Integer, DefaultEdge> g = pair.getValue0();
        GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator = pair.getValue1();
        Set<DefaultEdge> originalEdgeSet = g.edgeSet();
        List<DefaultEdge> stream = new ArrayList<>(originalEdgeSet);
        HopcroftKarpMaximumCardinalityBipartiteMatching<Integer, DefaultEdge> actualMaxMatching
                = new HopcroftKarpMaximumCardinalityBipartiteMatching<>(g, generator.getFirstPartition(), generator.getSecondPartition());

        List<Double> epsValues = Arrays.asList(1.0/2, 2.0/5, 3.0/10, 2.0/10, 1.0/10, 1.0/20, 1.0/40, 1.0/80, 1.0/160); // values of eps to test
        for (double eps : epsValues) {
            long time1 = System.nanoTime();
            ALT alt = new ALT(stream);
            Set<DefaultEdge> matching = alt.findApproximateMaxMatching(eps);
            long time2 = System.nanoTime();
            double seconds = (time2 - time1) / Math.pow(10, 9);
            System.out.println("Seconds taken for eps = " + eps + " is " + (seconds));
            System.out.println("The matching size is " + matching.size());
        }
    }

    private static void testMcGregorWithErdosRenyi() {
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        int n = 1000;

        List<Double> pValues = Arrays.asList(1.0/500, 1.0/250, 1.0/100, 1.0/50, 1.0/25);

        long seed = 42;

        for (double p : pValues) {
            System.out.println("Now testing p value " + p);
            Graph<Integer, DefaultEdge> gnpRandomGraph =
                    new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

            GnpRandomGraphGenerator<Integer, DefaultEdge> gnpRandomGraphGenerator =
                    new GnpRandomGraphGenerator<>(n, p, seed);

            gnpRandomGraphGenerator.generateGraph(gnpRandomGraph);

            Set<DefaultEdge> originalEdgeSet = gnpRandomGraph.edgeSet();

            System.out.println("The size of the stream is " + originalEdgeSet.size());

            DenseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> actualMaxMatching
                    = new DenseEdmondsMaximumCardinalityMatching<>(gnpRandomGraph);
            int actualMaxMatchingSize = actualMaxMatching.getMatching().getEdges().size();
            System.out.println("The max matching size in the original random graph is " + actualMaxMatchingSize);

            List<DefaultEdge> stream = new ArrayList<>(originalEdgeSet);
            double eps = 0.5;
            McGregor mcGregor = new McGregor(stream);
            long startTime = System.nanoTime();
            Set<DefaultEdge> matching = mcGregor.findApproximateMaxMatching(eps);
            long elapsedTime = System.nanoTime() - startTime;

            System.out.println("Total execution time in milliseconds: "
                    + elapsedTime/1000000);
        }

    }

    private static void testMcGregorWithPlantedMatching() {
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        int n = 1000;
        long seed = 42;

        List<Double> pValues = Arrays.asList(0.0008, 1.0/500);

        for (double p : pValues) {
            Graph<Integer, DefaultEdge> gnpRandomGraph =
                    new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

            GnpRandomGraphGenerator<Integer, DefaultEdge> gnpRandomGraphGenerator =
                    new GnpRandomGraphGenerator<>(n, p, seed);

            gnpRandomGraphGenerator.generateGraph(gnpRandomGraph);

            Graph<Integer, DefaultEdge> completeGraph =
                    new SimpleGraph<>(DefaultEdge.class);

            for (int vertex : gnpRandomGraph.vertexSet()) {
                completeGraph.addVertex(vertex);
            }

            for (int v1 : completeGraph.vertexSet()) {
                for (int v2 : completeGraph.vertexSet()) {
                    if (v1 == v2) continue;
                    completeGraph.addEdge(v1, v2);
                }
            }

            System.out.println(completeGraph.vertexSet());
            System.out.println(gnpRandomGraph.vertexSet());

            Set<DefaultEdge> originalEdgeSet = gnpRandomGraph.edgeSet();



            DenseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> actualMaxMatching
                    = new DenseEdmondsMaximumCardinalityMatching<>(gnpRandomGraph);
            int actualMaxMatchingSize = actualMaxMatching.getMatching().getEdges().size();
            System.out.println("The max matching size in the original random graph is " + actualMaxMatchingSize);

            DenseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> completeMaxMatching
                    = new DenseEdmondsMaximumCardinalityMatching<>(completeGraph);
            int completeMaxMatchingSize = completeMaxMatching.getMatching().getEdges().size();
            System.out.println("The max matching size in the complete graph is " + completeMaxMatchingSize);


            for (DefaultEdge edge : completeMaxMatching.getMatching().getEdges()) {
                int source = completeGraph.getEdgeSource(edge);
                int target = completeGraph.getEdgeTarget(edge);
                gnpRandomGraph.addEdge(source, target);
            }

            DenseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> plantedMatching
                    = new DenseEdmondsMaximumCardinalityMatching<>(completeGraph);
            int plantedMatchingSize = completeMaxMatching.getMatching().getEdges().size();
            System.out.println("The max matching size after planting the perfect matching is " + plantedMatchingSize);

            Set<DefaultEdge> edgeSet = gnpRandomGraph.edgeSet();

            List<DefaultEdge> stream = new ArrayList<>(edgeSet);
            System.out.println("The size of the stream is " + stream.size());
            double eps = 0.5;
            McGregor mcGregor = new McGregor(stream);
            long startTime = System.nanoTime();
            Set<DefaultEdge> matching = mcGregor.findApproximateMaxMatching(eps);
            long elapsedTime = System.nanoTime() - startTime;

            System.out.println("Total execution time in milliseconds: "
                    + elapsedTime/1000000);
        }

    }

    private static void testALTWithErdosRenyi() {
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        int n = 1000;

        List<Double> pValues = Arrays.asList(0.001, 1.0/500, 1.0/250, 1.0/100);

        long seed = 42;

        for (double p : pValues) {
            System.out.println("Now testing probability " + p);
            Pair<Graph<Integer, DefaultEdge>, GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge>> pair = GraphUtils.generateRandomBipartiteGraph(n, p, seed);
            Graph<Integer, DefaultEdge> g = pair.getValue0();
            GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator = pair.getValue1();
            Set<DefaultEdge> originalEdgeSet = g.edgeSet();
            List<DefaultEdge> stream = new ArrayList<>(originalEdgeSet);
            HopcroftKarpMaximumCardinalityBipartiteMatching<Integer, DefaultEdge> actualMaxMatching
                    = new HopcroftKarpMaximumCardinalityBipartiteMatching<>(g, generator.getFirstPartition(), generator.getSecondPartition());
            double eps = 1.0/3;
            System.out.println("The length of the stream is " + stream.size());
            System.out.println("The actual max matching size is " + actualMaxMatching.getMatching().getEdges().size());

            ALT alt = new ALT(stream);
            long time1 = System.nanoTime();
            Set<DefaultEdge> matching = alt.findApproximateMaxMatching(eps);
            long elapsedTime = System.nanoTime() - time1;
            System.out.println("The size of the approx matching is " + matching.size());

            System.out.println("Total execution time in milliseconds: "
                    + elapsedTime/1000000);


        }

    }

    private static void testALTWithPlantedMatching() {
        Supplier<Integer> vSupplier = new Supplier<>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };

        int n = 1000;
        long seed = 42;

        List<Double> pValues = Arrays.asList(0.0005, 0.001);

        for (double p : pValues) {
            System.out.println("Now testing probability " + p);
            Pair<Graph<Integer, DefaultEdge>, GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge>> pair = GraphUtils.generateRandomBipartiteGraph(n, p, seed);
            Graph<Integer, DefaultEdge> g = pair.getValue0();
            GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator = pair.getValue1();
            Set<DefaultEdge> originalEdgeSet = g.edgeSet();

            Graph<Integer, DefaultEdge> completeBipartiteGraph = new SimpleGraph<>(DefaultEdge.class);
            Set<Integer> firstPartition = new HashSet<>();
            Set<Integer> secondPartition = new HashSet<>();
            for (int v1 : generator.getFirstPartition()) {
                firstPartition.add(v1);
                for (int v2: generator.getSecondPartition()) {
                    secondPartition.add(v2);
                    completeBipartiteGraph.addVertex(v1);
                    completeBipartiteGraph.addVertex(v2);
                    completeBipartiteGraph.addEdge(v1, v2);
                }
            }

            HopcroftKarpMaximumCardinalityBipartiteMatching<Integer, DefaultEdge> completeMaxMatching = new HopcroftKarpMaximumCardinalityBipartiteMatching<>(completeBipartiteGraph, firstPartition, secondPartition);
            HopcroftKarpMaximumCardinalityBipartiteMatching<Integer, DefaultEdge> actualMaxMatching
                    = new HopcroftKarpMaximumCardinalityBipartiteMatching<>(g, generator.getFirstPartition(), generator.getSecondPartition());
            double eps = 1.0/3;
            System.out.println("The actual max matching size before adding the planted matching was " + actualMaxMatching.getMatching().getEdges().size());

            for (DefaultEdge edge : completeMaxMatching.getMatching().getEdges()) {
                int source = completeBipartiteGraph.getEdgeSource(edge);
                int target = completeBipartiteGraph.getEdgeTarget(edge);
                g.addEdge(source, target);
            }

            List<DefaultEdge> stream = new ArrayList<>(g.edgeSet());
            System.out.println("The length of the stream is " + stream.size());

            ALT alt = new ALT(stream);
            long time1 = System.nanoTime();
            Set<DefaultEdge> matching = alt.findApproximateMaxMatching(eps);
            long elapsedTime = System.nanoTime() - time1;
            System.out.println("The size of the approx matching is " + matching.size());

            System.out.println("Total execution time in milliseconds: "
                    + elapsedTime/1000000);

        }

    }

    public static void main(String[] args) {
        // Uncomment the line corresponding to the desired performance test
        // testALTWithErdosRenyi();
        // testALTWithPlantedMatching();
        // testMcGregorWithErdosRenyi();
        // testMcGregorWithPlantedMatching();
    }
}
