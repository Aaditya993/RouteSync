import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;

/**
 * The city road network: a weighted, undirected graph stored as an
 * ADJACENCY LIST.
 *
 *   locations.get(i)  -> the Location with id i
 *   adj.get(i)        -> list of Roads leaving location i
 */
public class CityGraph {
    /** Distance value meaning "not reachable (yet)". */
    public static final int INFINITY = Integer.MAX_VALUE;

    private final ArrayList<Location> locations = new ArrayList<>();
    private final ArrayList<ArrayList<Road>> adj = new ArrayList<>();

    public CityGraph() {
        loadSampleCity();
    }

    // ---------------------------------------------------------------
    // Building the graph
    // ---------------------------------------------------------------

    /** Adds a location and gives it an empty list of roads. */
    private void addLocation(String name) {
        int id = locations.size();                 // next free index
        locations.add(new Location(id, name));
        adj.add(new ArrayList<>());                // adj.get(id) = no roads yet
    }

    /**
     * Adds a bidirectional road. An undirected edge is stored as TWO
     * directed entries: one in u's list and one in v's list.
     */
    private void addRoad(int u, int v, int minutes) {
        adj.get(u).add(new Road(v, minutes));
        adj.get(v).add(new Road(u, minutes));
    }

    /** Same as above but uses names, which keeps the sample data readable. */
    private void addRoad(String from, String to, int minutes) {
        addRoad(getLocationIndex(from), getLocationIndex(to), minutes);
    }

    /** 12 locations, 22 roads. Airport has only ONE road (for the no-route demo). */
    private void loadSampleCity() {
        String[] names = {
            "Central Park", "City Hospital", "Railway Station", "University",
            "Shopping Mall", "Airport", "Market Square", "Bus Terminal",
            "Tech Park", "City Library", "Stadium", "Old Town"
        };
        for (String name : names) {
            addLocation(name);
        }

        addRoad("Central Park", "Market Square", 4);
        addRoad("Central Park", "City Library", 6);
        addRoad("Central Park", "Railway Station", 7);
        addRoad("Central Park", "University", 9);
        addRoad("Market Square", "Railway Station", 5);
        addRoad("Market Square", "Shopping Mall", 8);
        addRoad("Market Square", "Old Town", 3);
        addRoad("Railway Station", "City Hospital", 6);
        addRoad("Railway Station", "Bus Terminal", 4);
        addRoad("City Hospital", "Bus Terminal", 5);
        addRoad("City Hospital", "University", 8);
        addRoad("University", "City Library", 5);
        addRoad("University", "Tech Park", 10);
        addRoad("City Library", "Shopping Mall", 7);
        addRoad("Shopping Mall", "Old Town", 6);
        addRoad("Shopping Mall", "Stadium", 9);
        addRoad("Bus Terminal", "Stadium", 7);
        addRoad("Bus Terminal", "Tech Park", 12);
        addRoad("Tech Park", "Stadium", 8);
        addRoad("Tech Park", "Airport", 15);       // Airport's ONLY road
        addRoad("Old Town", "City Library", 4);
        addRoad("Stadium", "City Hospital", 11);
    }

    // ---------------------------------------------------------------
    // Accessing the graph
    // ---------------------------------------------------------------

    public int getLocationCount() {
        return locations.size();
    }

    public String getLocationName(int index) {
        return locations.get(index).getName();
    }

    /** Roads leaving a location. Dijkstra (Phase 3) will use this. */
    public ArrayList<Road> getRoadsFrom(int index) {
        return adj.get(index);
    }

    /** Returns the index of a location by name, or -1 if not found. */
    public int getLocationIndex(String name) {
        for (Location loc : locations) {
            if (loc.getName().equalsIgnoreCase(name.trim())) {
                return loc.getId();
            }
        }
        return -1;
    }

    // ---------------------------------------------------------------
    // Dijkstra's algorithm (shortest travel time)
    // ---------------------------------------------------------------

    /**
     * Finds the shortest travel time from 'source' to EVERY location.
     *
     * The caller passes in two arrays of size getLocationCount();
     * this method fills them:
     *   dist[v]   = shortest time from source to v (INFINITY if unreachable)
     *   parent[v] = the location visited just before v on that shortest
     *               route (-1 if none), used to rebuild the route.
     */
    public void dijkstra(int source, int[] dist, int[] parent) {
        Arrays.fill(dist, INFINITY);       // nothing reached yet
        Arrays.fill(parent, -1);           // no parents yet
        dist[source] = 0;                  // the source is 0 minutes from itself

        // Min Heap of {location, distance}; the smallest distance is polled first
        PriorityQueue<int[]> pq =
                new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));
        pq.add(new int[]{source, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();     // nearest not-yet-finished location
            int u = current[0];
            int d = current[1];

            // The queue can hold OLD entries for u if a shorter distance was
            // found later. If this entry is out of date, skip it.
            if (d > dist[u]) {
                continue;
            }

            for (Road road : adj.get(u)) {
                if (!road.isActive()) {
                    continue;              // closed roads cannot be used
                }
                int v = road.getDestination();
                int newDist = d + road.getTravelTime();

                // RELAXATION: is going through u a shorter way to v?
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    parent[v] = u;
                    pq.add(new int[]{v, newDist});
                }
            }
        }
    }

    /**
     * Rebuilds the route source -> destination by following parents
     * backwards from the destination, then reversing.
     * Returns an EMPTY list if the destination cannot be reached.
     */
    public ArrayList<Integer> getPath(int[] parent, int source, int destination) {
        ArrayList<Integer> path = new ArrayList<>();
        for (int at = destination; at != -1; at = parent[at]) {
            path.add(at);
        }
        Collections.reverse(path);

        // If the walk did not end at the source, there is no route
        if (path.get(0) != source) {
            path.clear();
        }
        return path;
    }

    // ---------------------------------------------------------------
    // Road closure
    // ---------------------------------------------------------------

    /** Finds the Road object going from 'from' to 'to', or null if none. */
    private Road findRoad(int from, int to) {
        for (Road road : adj.get(from)) {
            if (road.getDestination() == to) {
                return road;
            }
        }
        return null;
    }

    /** Travel time of the ACTIVE road from u to v, or -1 if it is closed / missing. */
    public int getRoadTime(int u, int v) {
        Road road = findRoad(u, v);
        if (road == null || !road.isActive()) {
            return -1;
        }
        return road.getTravelTime();
    }

    /**
     * Closes the road between u and v. Because every road is stored in
     * BOTH adjacency lists, both entries are switched off.
     * Returns false if no such road exists.
     */
    public boolean closeRoad(int u, int v) {
        Road forward = findRoad(u, v);     // entry in u's list
        Road backward = findRoad(v, u);    // entry in v's list
        if (forward == null || backward == null) {
            return false;
        }
        forward.setActive(false);
        backward.setActive(false);
        return true;
    }

    /**
     * Lists each road ONCE as {u, v} with u < v (the adjacency lists hold
     * every road twice, so we keep only the entry where u < v).
     * The menu number of a road is its position in this list + 1.
     */
    public ArrayList<int[]> getRoadList() {
        ArrayList<int[]> roads = new ArrayList<>();
        for (int u = 0; u < adj.size(); u++) {
            for (Road road : adj.get(u)) {
                if (u < road.getDestination()) {
                    roads.add(new int[]{u, road.getDestination()});
                }
            }
        }
        return roads;
    }

    /** Closes the road shown with this menu number (1-based). */
    public boolean closeRoadByNumber(int number) {
        ArrayList<int[]> roads = getRoadList();
        if (number < 1 || number > roads.size()) {
            return false;
        }
        int[] road = roads.get(number - 1);
        return closeRoad(road[0], road[1]);
    }

    /** Prints every road once, numbered, with its time and status. */
    public void displayRoads() {
        System.out.println("\n===== ROADS =====");
        ArrayList<int[]> roads = getRoadList();
        for (int i = 0; i < roads.size(); i++) {
            int u = roads.get(i)[0];
            int v = roads.get(i)[1];
            Road road = findRoad(u, v);
            String status = road.isActive() ? "" : "  [CLOSED]";
            System.out.println((i + 1) + ". " + getLocationName(u) + " <-> "
                    + getLocationName(v) + " (" + road.getTravelTime() + " min)" + status);
        }
    }

    // ---------------------------------------------------------------
    // Display
    // ---------------------------------------------------------------

    /** Prints every location with its connected locations and travel times. */
    public void displayNetwork() {
        System.out.println("\n===== CITY ROAD NETWORK =====");
        for (Location loc : locations) {
            System.out.println((loc.getId() + 1) + ". " + loc.getName());
            for (Road road : adj.get(loc.getId())) {
                String status = road.isActive() ? "" : "  [CLOSED]";
                System.out.println("     -> " + getLocationName(road.getDestination())
                        + " (" + road.getTravelTime() + " min)" + status);
            }
        }
    }
}
