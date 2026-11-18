# RouteSync - Smart Route and Driver Allocation (Java, console)

## Folder structure

Put all files directly inside one folder named `RouteSync` (no sub-folders, no `package` lines):

```
RouteSync/
├── Main.java
├── CityGraph.java
├── Location.java
├── Road.java
├── Driver.java
├── DriverManager.java
├── TrafficAnalyzer.java
├── RouteAnalyzer.java
├── README.md
└── DOCUMENTATION.md
```

## Compile and run (needs JDK 8 or newer)

```
cd RouteSync
javac *.java
java Main
```

Compiling creates `.class` files in the same folder. That is normal.

## IDE tips

- **VS Code / IntelliJ / Eclipse / NetBeans:** create a new Java project, then copy the 8 `.java`
  files into its `src` folder (the default package). Run `Main`.
- Do not put the files in different folders unless you add matching `package` statements.

## Which file implements what

| File | DSA concept |
|---|---|
| CityGraph.java | Graph, Adjacency List, Dijkstra (Min Heap), road closure |
| DriverManager.java | PriorityQueue ranking of drivers by pickup ETA |
| TrafficAnalyzer.java | Sliding Window |
| RouteAnalyzer.java | Prefix Sum |
| Main.java | Menu and input only |
