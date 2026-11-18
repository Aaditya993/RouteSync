/**
 * A place in the city (a vertex of the graph).
 * The id is its index in CityGraph's locations list and adjacency list.
 */
public class Location {
    private final int id;
    private final String name;

    public Location(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
