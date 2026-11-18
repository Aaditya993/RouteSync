/** One driver. The location is stored as a location index in CityGraph. */
public class Driver {
    private final String id;
    private final String name;
    private int location;          // index of the location the driver is at
    private boolean available;

    public Driver(String id, String name, int location, boolean available) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLocation() {
        return location;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
