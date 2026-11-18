/**
 * One road leaving a location (an entry in an adjacency list).
 * It stores only the DESTINATION, because the source is implied by
 * which list the Road sits in: adj.get(source) contains this Road.
 */
public class Road {
    private final int destination;  // index of the location this road leads to
    private final int travelTime;   // weight of the edge, in minutes (positive)
    private boolean active;         // false = road is closed (used in Phase 4)

    public Road(int destination, int travelTime) {
        this.destination = destination;
        this.travelTime = travelTime;
        this.active = true;         // roads start open
    }

    public int getDestination() {
        return destination;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
