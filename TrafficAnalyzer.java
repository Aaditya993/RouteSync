import java.util.ArrayList;

/**
 * Traffic Analysis module (SLIDING WINDOW).
 * Stores sample delay readings (extra minutes of delay, one reading every
 * 10 minutes) for some roads and computes moving averages over them.
 * It does not depend on the graph, only on road names.
 */
public class TrafficAnalyzer {
    private final ArrayList<String> roadNames = new ArrayList<>();
    private final ArrayList<int[]> delayData = new ArrayList<>();

    public TrafficAnalyzer() {
        loadSampleData();
    }

    private void addRoadData(String roadName, int[] delays) {
        roadNames.add(roadName);
        delayData.add(delays);
    }

    private void loadSampleData() {
        addRoadData("Central Park <-> Railway Station", new int[]{5, 8, 12, 20, 15, 10, 7, 6});
        addRoadData("Market Square <-> Shopping Mall", new int[]{3, 4, 6, 9, 14, 18, 16, 11, 7});
        addRoadData("City Hospital <-> Bus Terminal", new int[]{2, 2, 3, 5, 4, 3, 2, 2});
        addRoadData("University <-> Tech Park", new int[]{10, 12, 15, 22, 30, 28, 18, 12, 9, 8});
        addRoadData("Bus Terminal <-> Stadium", new int[]{6, 9, 13, 25, 27, 19, 11, 8});
        addRoadData("City Library <-> Old Town", new int[]{1, 2, 2, 4, 6, 5, 3, 2});
    }

    public int getRoadCount() {
        return roadNames.size();
    }

    public String getRoadName(int index) {
        return roadNames.get(index);
    }

    public int[] getDelays(int index) {
        return delayData.get(index);
    }

    /**
     * OPTIMIZED SLIDING WINDOW.
     * Returns the average of every window of k consecutive readings.
     * The sum is computed fully only ONCE (first window). After that the
     * window slides one step: subtract the element leaving, add the
     * element entering.
     */
    public double[] slidingWindowAverages(int[] data, int k) {
        int n = data.length;
        if (k < 1 || k > n) {
            return new double[0];                  // invalid window size
        }

        double[] averages = new double[n - k + 1]; // number of windows
        int windowSum = 0;

        // Step 1: sum of the first window
        for (int i = 0; i < k; i++) {
            windowSum += data[i];
        }
        averages[0] = (double) windowSum / k;

        // Steps 2-4: slide the window one position at a time
        for (int i = k; i < n; i++) {
            windowSum = windowSum - data[i - k] + data[i];   // out: data[i-k], in: data[i]
            averages[i - k + 1] = (double) windowSum / k;
        }
        return averages;
    }

    /**
     * SLOW VERSION, for comparison only: re-adds all k elements for
     * every window. Gives the same answers as the sliding window.
     */
    public double[] repeatedSumAverages(int[] data, int k) {
        int n = data.length;
        if (k < 1 || k > n) {
            return new double[0];
        }
        double[] averages = new double[n - k + 1];
        for (int start = 0; start <= n - k; start++) {
            int sum = 0;
            for (int j = start; j < start + k; j++) {   // k additions EVERY window
                sum += data[j];
            }
            averages[start] = (double) sum / k;
        }
        return averages;
    }

    /** Additions/subtractions needed by the sliding window: k + 2(n-k). */
    public static int operationsSlidingWindow(int n, int k) {
        return k + 2 * (n - k);
    }

    /** Additions needed by repeated summing: k per window, (n-k+1) windows. */
    public static int operationsRepeatedSum(int n, int k) {
        return k * (n - k + 1);
    }
}
