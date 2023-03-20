import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.color.GreedyColoring;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ALT {
    private final List<DefaultEdge> stream;

    public ALT(List<DefaultEdge> stream) {
        this.stream = stream;
        validateBipartite(stream);
    }

    private void validateBipartite(List<DefaultEdge> stream) {
        Graph<Integer, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);
        for (DefaultEdge edge : stream) {
            Graphs.addEdgeWithVertices(g, g.getEdgeSource(edge), g.getEdgeTarget(edge));
        }
        GreedyColoring<Integer, DefaultEdge> greedyColoring = new GreedyColoring<>(g);
        int numColors = greedyColoring.getColoring().getNumberColors();
        if (numColors != 2) {
            throw new IllegalArgumentException("Argument to ALT must be an edge stream of a bipartite graph.");
        }
    }

    private static List<Set<Integer>> classifyBiddersAndItems(List<DefaultEdge> stream, Graph<Integer, DefaultEdge> g) {
        Set<Integer> bidders = new HashSet<>();
        Set<Integer> items = new HashSet<>();
        for (DefaultEdge edge : stream) {
            int source = g.getEdgeSource(edge);
            int target = g.getEdgeTarget(edge);
            if (bidders.contains(source) || items.contains(target)) {
                bidders.add(source);
                items.add(target);
            } else {
                bidders.add(target);
                items.add(source);
            }
        }
        return List.of(bidders, items);
    }

    public Set<DefaultEdge> findApproximateMaxMatching(double eps) {
        if (eps <= 0 || eps >= 1) {
            throw new IllegalArgumentException("Argument to findApproximateMatching must be a real number strictly between 0 and 1");
        }

        Graph<Integer, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);

        List<Set<Integer>> biddersItemsList = classifyBiddersAndItems(this.stream, g);

        Set<Integer> bidders = biddersItemsList.get(0);
        Set<Integer> items = biddersItemsList.get(1);


        Map<Integer, Double> prices = new HashMap<>();
        Map<Integer, Integer> bidderAllocations = new HashMap<>();
        Map<Integer, Integer> itemOwners = new HashMap<>();

        for (int item : items) {
            prices.put(item, 0.0);
            itemOwners.put(item, -1);
        }

        for (int bidder : bidders) {
            bidderAllocations.put(bidder, -1);
        }

        int maxIterations = (int) Math.ceil(2 / (eps * eps));

        for (int i = 0; i < maxIterations; i++) {
            Map<Integer, Double> demands = new HashMap<>();
            for (DefaultEdge edge : this.stream) {

                int source = g.getEdgeSource(edge);
                int target = g.getEdgeTarget(edge);
                int bidder;
                int item;
                if (bidders.contains(source)) {
                    bidder = source;
                    item = target;
                } else {
                    bidder = target;
                    item = source;
                }

                double price = prices.get(item);

                if (!demands.containsKey(bidder) || price < demands.get(bidder)) {
                    demands.put(bidder, price);
                }
            }

            Set<DefaultEdge> maximalMatching = new HashSet<>();
            Set<Integer> verticesInMaximalMatching = new HashSet<>();

            for (DefaultEdge edge : this.stream) {
                int source = g.getEdgeSource(edge);
                int target = g.getEdgeTarget(edge);
                int bidder;
                int item;
                if (bidders.contains(source)) {
                    bidder = source;
                    item = target;
                } else {
                    bidder = target;
                    item = source;
                }

                double price = prices.get(item);

                boolean isUnallocated = (bidderAllocations.get(bidder) == -1);

                boolean priceIsMin = (price == demands.get(bidder) && price < 1);

                if (isUnallocated && priceIsMin) {
                    if (!verticesInMaximalMatching.contains(source) && !verticesInMaximalMatching.contains(target)) {
                        maximalMatching.add(edge);
                        verticesInMaximalMatching.add(source);
                        verticesInMaximalMatching.add(target);
                    }
                }
            }

            assert isMatching(maximalMatching, g);

            for (DefaultEdge edge : maximalMatching) {
                int source = g.getEdgeSource(edge);
                int target = g.getEdgeTarget(edge);
                int bidder;
                int item;
                if (bidders.contains(source)) {
                    bidder = source;
                    item = target;
                } else {
                    bidder = target;
                    item = source;
                }

                double price = prices.get(item);
                int previousOwner = itemOwners.get(item);
                if (previousOwner != -1) {
                    bidderAllocations.put(previousOwner, -1);
                }
                bidderAllocations.put(bidder, item);
                prices.put(item, Math.min(price + eps, 1.0));
            }
        }

        Set<DefaultEdge> approxMatching = new HashSet<>();

        for (int bidder : bidderAllocations.keySet()) {
            int item = bidderAllocations.get(bidder);
            if (item == -1) {
                continue;
            }
            g.addVertex(bidder);
            g.addVertex(item);
            DefaultEdge edge = g.addEdge(bidder, item);
            approxMatching.add(edge);
        }

        return approxMatching;
    }

    private boolean isMatching(Set<DefaultEdge> matching, Graph<Integer, DefaultEdge> g) {
        Set<Integer> verticesCoveredByReturnedMatching = new HashSet<>();
        for (DefaultEdge edge : matching) {
            int s = g.getEdgeSource(edge);
            int t = g.getEdgeTarget(edge);
            if (verticesCoveredByReturnedMatching.contains(s) || verticesCoveredByReturnedMatching.contains(t)) {
                return false;
            }
            verticesCoveredByReturnedMatching.add(s);
            verticesCoveredByReturnedMatching.add(t);
        }
        return true;
    }
}
