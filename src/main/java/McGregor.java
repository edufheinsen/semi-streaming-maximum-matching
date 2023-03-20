import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.color.GreedyColoring;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.util.*;

public class McGregor {
    private final List<DefaultEdge> stream;

    public McGregor(List<DefaultEdge> stream) {
        this.stream = stream;
    }

    public Set<DefaultEdge> findApproximateMaxMatching(double eps) {
        if (eps <= 0 || eps >= 1) {
            throw new IllegalArgumentException("Argument to findApproximateMatching must be a real number strictly between 0 and 1");
        }

        Set<DefaultEdge> approxMatching = new HashSet<>();

        return approxMatching;
    }
}
