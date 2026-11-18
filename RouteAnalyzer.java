import java.util.ArrayList;

/**
 * Route Segment Analysis module (PREFIX SUM).
 * Works on a route that Dijkstra has ALREADY found; it never finds routes.
 */
public class RouteAnalyzer {

    /**
     * Travel time of each road segment along the route.
     * A route with m locations has m-1 segments.
     */
    public static int[] getSegmentTimes(CityGraph city, ArrayList<Integer> path) {
        int[] segments = new int[path.size() - 1];
        for (int i = 0; i < segments.length; i++) {
            segments[i] = city.getRoadTime(path.get(i), path.get(i + 1));
        }
        return segments;
    }

    /**
     * Builds the PREFIX SUM array:
     *   prefix[i] = segments[0] + segments[1] + ... + segments[i]
     * Each entry reuses the previous one, so it takes a single pass.
     */
    public static int[] buildPrefixSum(int[] segments) {
        int[] prefix = new int[segments.length];
        prefix[0] = segments[0];
        for (int i = 1; i < segments.length; i++) {
            prefix[i] = prefix[i - 1] + segments[i];
        }
        return prefix;
    }

    /**
     * Total time of segments left..right (0-based, inclusive) in O(1):
     *   prefix[right] holds everything up to 'right',
     *   prefix[left-1] holds everything BEFORE 'left'; subtract it.
     */
    public static int rangeSum(int[] prefix, int left, int right) {
        if (left == 0) {
            return prefix[right];              // nothing to subtract
        }
        return prefix[right] - prefix[left - 1];
    }
}
