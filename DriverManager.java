import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * Stores the drivers and allocates the nearest available one to a passenger.
 * Pickup ETAs come from real Dijkstra runs on the city graph.
 */
public class DriverManager {
    private final CityGraph city;
    private final ArrayList<Driver> drivers = new ArrayList<>();

    /** A driver together with his pickup ETA. These are the PriorityQueue entries. */
    public static class Allocation {
        public final Driver driver;
        public final int etaMinutes;

        public Allocation(Driver driver, int etaMinutes) {
            this.driver = driver;
            this.etaMinutes = etaMinutes;
        }
    }

    public DriverManager(CityGraph city) {
        this.city = city;
        loadSampleDrivers();
    }

    // ---------------------------------------------------------------
    // Sample data
    // ---------------------------------------------------------------

    private void addDriver(String id, String name, String locationName, boolean available) {
        int location = city.getLocationIndex(locationName);
        drivers.add(new Driver(id, name, location, available));
    }

    /** 12 drivers; 3 of them start unavailable. */
    private void loadSampleDrivers() {
        addDriver("D101", "Driver A", "Railway Station", true);
        addDriver("D102", "Driver B", "Market Square", true);
        addDriver("D103", "Driver C", "Central Park", false);
        addDriver("D104", "Driver D", "University", true);
        addDriver("D105", "Driver E", "Shopping Mall", true);
        addDriver("D106", "Driver F", "City Hospital", false);
        addDriver("D107", "Driver G", "Bus Terminal", true);
        addDriver("D108", "Driver H", "Tech Park", true);
        addDriver("D109", "Driver I", "Stadium", false);
        addDriver("D110", "Driver J", "Old Town", true);
        addDriver("D111", "Driver K", "City Library", true);
        addDriver("D112", "Driver L", "Airport", true);
    }

    // ---------------------------------------------------------------
    // Queries
    // ---------------------------------------------------------------

    public ArrayList<Driver> getAllDrivers() {
        return drivers;
    }

    /** Returns only the drivers whose availability is true. */
    public ArrayList<Driver> getAvailableDrivers() {
        ArrayList<Driver> available = new ArrayList<>();
        for (Driver driver : drivers) {
            if (driver.isAvailable()) {
                available.add(driver);
            }
        }
        return available;
    }

    public void displayDrivers() {
        System.out.println("\n===== DRIVERS =====");
        for (Driver driver : drivers) {
            String status = driver.isAvailable() ? "Available" : "Unavailable";
            System.out.println(driver.getId() + " - " + driver.getName() + " - "
                    + city.getLocationName(driver.getLocation()) + " - " + status);
        }
    }

    // ---------------------------------------------------------------
    // Allocation
    // ---------------------------------------------------------------

    /**
     * Shortest travel time from the driver's location to the pickup point,
     * using Dijkstra on the real graph. Returns CityGraph.INFINITY if the
     * pickup cannot be reached (for example, a closed road cuts it off).
     */
    private int calculatePickupEta(Driver driver, int pickup) {
        int n = city.getLocationCount();
        int[] dist = new int[n];
        int[] parent = new int[n];
        city.dijkstra(driver.getLocation(), dist, parent);
        return dist[pickup];
    }

    /**
     * Picks the available driver with the smallest pickup ETA and marks him
     * unavailable. Returns null if no available driver can reach the pickup.
     */
    public Allocation allocateNearestDriver(int pickup) {
        // Min Heap ordered by ETA (ties broken by driver ID so results are repeatable)
        PriorityQueue<Allocation> pq = new PriorityQueue<>((a, b) -> {
            if (a.etaMinutes != b.etaMinutes) {
                return Integer.compare(a.etaMinutes, b.etaMinutes);
            }
            return a.driver.getId().compareTo(b.driver.getId());
        });

        // Steps 2-4: for each available driver, compute the ETA and add to the heap
        for (Driver driver : getAvailableDrivers()) {
            int eta = calculatePickupEta(driver, pickup);
            if (eta != CityGraph.INFINITY) {       // skip drivers who cannot reach
                pq.add(new Allocation(driver, eta));
            }
        }

        if (pq.isEmpty()) {
            return null;                            // nobody available / reachable
        }

        Allocation best = pq.poll();                // smallest ETA is at the top
        best.driver.setAvailable(false);            // assigned: no longer available
        return best;
    }
}
