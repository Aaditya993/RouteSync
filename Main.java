import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * RouteSync main program: shows the menu, reads input and calls the modules.
 * All algorithms live in the other classes.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final CityGraph city = new CityGraph();
    private static final DriverManager driverManager = new DriverManager(city);
    private static final TrafficAnalyzer trafficAnalyzer = new TrafficAnalyzer();

    // Remember the last route request so Option 3 and Option 6 can reuse it
    private static int lastSource = -1;
    private static int lastDestination = -1;
    private static ArrayList<Integer> lastPath = null;   // null = no valid route stored

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            showMenu();
            int choice = readInt("Select an option: ", 1, 8);
            switch (choice) {
                case 1: city.displayNetwork(); break;
                case 2: findShortestRoute(); break;
                case 3: closeRoad(); break;
                case 4: allocateDriver(); break;
                case 5: trafficAnalysis(); break;
                case 6: routeSegmentAnalysis(); break;
                case 7: driverManager.displayDrivers(); break;
                case 8: running = false; break;
            }
        }
        System.out.println("Thank you for using RouteSync. Goodbye!");
    }

    private static void showMenu() {
        System.out.println("\n========== ROUTESYNC ==========");
        System.out.println("1. View City Road Network");
        System.out.println("2. Find Shortest Route");
        System.out.println("3. Close a Road");
        System.out.println("4. Allocate Nearest Driver");
        System.out.println("5. Traffic Analysis");
        System.out.println("6. Route Segment Analysis");
        System.out.println("7. View Available Drivers");
        System.out.println("8. Exit");
    }

    // ---------------------------------------------------------------
    // Option 2: shortest route (Dijkstra)
    // ---------------------------------------------------------------

    private static void findShortestRoute() {
        showLocationList();
        int source = readLocation("Starting location (number or name): ");
        int destination = readLocation("Destination (number or name): ");
        calculateAndShowRoute(source, destination);
    }

    /** Runs Dijkstra, prints the result and stores the route for later options. */
    private static void calculateAndShowRoute(int source, int destination) {
        lastSource = source;
        lastDestination = destination;

        int n = city.getLocationCount();
        int[] dist = new int[n];
        int[] parent = new int[n];
        city.dijkstra(source, dist, parent);

        if (dist[destination] == CityGraph.INFINITY) {
            lastPath = null;
            System.out.println("\nNo route available from " + city.getLocationName(source)
                    + " to " + city.getLocationName(destination)
                    + " (roads may be closed).");
            return;
        }

        lastPath = city.getPath(parent, source, destination);
        System.out.println("\nShortest route:");
        System.out.println(routeToString(lastPath));
        System.out.println("Estimated travel time: " + dist[destination] + " minutes");
    }

    private static String routeToString(ArrayList<Integer> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(city.getLocationName(path.get(i)));
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // Option 3: road closure
    // ---------------------------------------------------------------

    private static void closeRoad() {
        city.displayRoads();
        int roadCount = city.getRoadList().size();
        int number = readInt("Enter the road number to close (0 to cancel): ", 0, roadCount);
        if (number == 0) {
            return;
        }

        int[] road = city.getRoadList().get(number - 1);
        String roadName = city.getLocationName(road[0]) + " <-> " + city.getLocationName(road[1]);
        if (city.getRoadTime(road[0], road[1]) == -1) {
            System.out.println("That road is already closed.");
            return;
        }

        city.closeRoadByNumber(number);
        lastPath = null;                       // the stored route might use this road
        System.out.println("Road closed (both directions): " + roadName);

        if (lastSource == -1) {
            System.out.println("Use option 2 to calculate a route with the new road status.");
        } else if (readYesNo("Recalculate the last route ("
                + city.getLocationName(lastSource) + " to "
                + city.getLocationName(lastDestination) + ")?")) {
            calculateAndShowRoute(lastSource, lastDestination);
        }
    }

    // ---------------------------------------------------------------
    // Option 4: driver allocation (PriorityQueue)
    // ---------------------------------------------------------------

    private static void allocateDriver() {
        showLocationList();
        int pickup = readLocation("Passenger pickup location (number or name): ");

        if (driverManager.getAvailableDrivers().isEmpty()) {
            System.out.println("\nNo drivers are available right now.");
            return;
        }

        DriverManager.Allocation result = driverManager.allocateNearestDriver(pickup);
        if (result == null) {
            System.out.println("\nNo available driver can reach "
                    + city.getLocationName(pickup) + " (roads may be closed).");
            return;
        }

        Driver driver = result.driver;
        System.out.println("\nDriver assigned!");
        System.out.println("Driver ID       : " + driver.getId());
        System.out.println("Driver name     : " + driver.getName());
        System.out.println("Driver location : " + city.getLocationName(driver.getLocation()));
        System.out.println("Pickup ETA      : " + result.etaMinutes + " minutes");
        System.out.println("(" + driver.getName() + " is now marked unavailable.)");
    }

    // ---------------------------------------------------------------
    // Option 5: traffic analysis (Sliding Window)
    // ---------------------------------------------------------------

    private static void trafficAnalysis() {
        System.out.println("\n===== ROADS WITH TRAFFIC DATA =====");
        for (int i = 0; i < trafficAnalyzer.getRoadCount(); i++) {
            System.out.println((i + 1) + ". " + trafficAnalyzer.getRoadName(i));
        }
        int choice = readInt("Select a road: ", 1, trafficAnalyzer.getRoadCount());
        int[] data = trafficAnalyzer.getDelays(choice - 1);

        System.out.println("\nRoad: " + trafficAnalyzer.getRoadName(choice - 1));
        System.out.println("Delay (extra minutes), one reading every 10 minutes:");
        System.out.println(Arrays.toString(data));

        int k = readInt("Window size (1 to " + data.length + "): ", 1, data.length);
        double[] averages = trafficAnalyzer.slidingWindowAverages(data, k);

        System.out.println("\nAverage delay over each window of " + k + " readings:");
        for (int i = 0; i < averages.length; i++) {
            int[] window = Arrays.copyOfRange(data, i, i + k);
            System.out.println("Window " + (i + 1) + ": " + Arrays.toString(window)
                    + "  Average = " + String.format("%.2f", averages[i]));
        }

        // Show that the fast method gives the same answers as the slow one
        boolean same = Arrays.equals(averages, trafficAnalyzer.repeatedSumAverages(data, k));
        System.out.println("\nCheck against repeated summing: " + (same ? "results match." : "MISMATCH!"));
        System.out.println("Additions/subtractions needed -> Sliding Window: "
                + TrafficAnalyzer.operationsSlidingWindow(data.length, k)
                + ", Repeated summing: "
                + TrafficAnalyzer.operationsRepeatedSum(data.length, k));
    }

    // ---------------------------------------------------------------
    // Option 6: route segment analysis (Prefix Sum)
    // ---------------------------------------------------------------

    private static void routeSegmentAnalysis() {
        ArrayList<Integer> path = chooseRoute();
        if (path == null) {
            return;
        }
        if (path.size() < 2) {
            System.out.println("Start and destination are the same, so the route has no segments.");
            return;
        }

        int[] segments = RouteAnalyzer.getSegmentTimes(city, path);
        int[] prefix = RouteAnalyzer.buildPrefixSum(segments);

        System.out.println("\nRoute: " + routeToString(path));
        for (int i = 0; i < segments.length; i++) {
            System.out.println("Segment " + (i + 1) + ": " + city.getLocationName(path.get(i))
                    + " -> " + city.getLocationName(path.get(i + 1)) + " = " + segments[i] + " min");
        }
        System.out.println("\nSegment times : " + Arrays.toString(segments));
        System.out.println("Prefix sums   : " + Arrays.toString(prefix));
        System.out.println("(Prefix sum i = total time of segments 1 to i)");

        int from = readInt("\nStart segment (1 to " + segments.length + "): ", 1, segments.length);
        int to = readInt("End segment (" + from + " to " + segments.length + "): ", from, segments.length);

        int total = RouteAnalyzer.rangeSum(prefix, from - 1, to - 1);   // 1-based -> 0-based
        if (from == 1) {
            System.out.println("\nTravel time, segment 1 to " + to + ": prefix[" + to + "] = " + total + " minutes");
        } else {
            System.out.println("\nTravel time, segment " + from + " to " + to + ": prefix[" + to + "] - prefix["
                    + (from - 1) + "] = " + prefix[to - 1] + " - " + prefix[from - 2] + " = " + total + " minutes");
        }
        System.out.println("(Answered with one subtraction, no loop over the segments.)");
    }

    /** Reuses the last route if the user agrees, otherwise asks for a new one. */
    private static ArrayList<Integer> chooseRoute() {
        if (lastPath != null) {
            System.out.println("\nLast calculated route: " + routeToString(lastPath));
            if (readYesNo("Analyse this route?")) {
                return lastPath;
            }
        }
        showLocationList();
        int source = readLocation("Starting location (number or name): ");
        int destination = readLocation("Destination (number or name): ");
        calculateAndShowRoute(source, destination);
        return lastPath;                       // null if there is no route
    }

    // ---------------------------------------------------------------
    // Input helpers
    // ---------------------------------------------------------------

    private static void showLocationList() {
        System.out.println("\nLocations:");
        for (int i = 0; i < city.getLocationCount(); i++) {
            System.out.print(String.format("%2d. %-18s", i + 1, city.getLocationName(i)));
            if ((i + 1) % 3 == 0 || i == city.getLocationCount() - 1) {
                System.out.println();
            }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println("\nInput ended. Exiting RouteSync.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    /** Keeps asking until the user enters a whole number between min and max. */
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            String input = readLine(prompt);
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // not a number: fall through to the error message
            }
            System.out.println("Invalid input. Please enter a number from " + min + " to " + max + ".");
        }
    }

    /** Accepts a location number (1-based) or a location name. Returns its index. */
    private static int readLocation(String prompt) {
        int n = city.getLocationCount();
        while (true) {
            String input = readLine(prompt);
            int index = -1;
            try {
                int number = Integer.parseInt(input);
                if (number >= 1 && number <= n) {
                    index = number - 1;
                }
            } catch (NumberFormatException e) {
                index = city.getLocationIndex(input);     // not a number: try as a name
            }
            if (index != -1) {
                return index;
            }
            System.out.println("Invalid location. Enter a number from 1 to " + n + " or a valid name.");
        }
    }

    private static boolean readYesNo(String prompt) {
        while (true) {
            String input = readLine(prompt + " (y/n): ").toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("Please enter y or n.");
        }
    }
}
