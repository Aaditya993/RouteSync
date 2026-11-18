# RouteSync - Project Documentation

## 1. Abstract
RouteSync is a terminal-based Java application that simulates a small fictional city (12 locations,
22 roads). It finds the fastest route between two places using Dijkstra's algorithm, reroutes around
closed roads, assigns the nearest available driver using a PriorityQueue, and analyses traffic data
with Sliding Window and Prefix Sum. It is a DSA mini-project: every algorithm is implemented by hand.

## 2. Problem statement
Navigation and ride-hailing apps rely on graph algorithms and efficient data processing. This project
demonstrates how a few core DSA concepts solve the basic versions of those problems.

## 3. Objectives
1. Represent a road network as a weighted, undirected graph.
2. Find shortest travel-time routes and handle road closures.
3. Allocate the nearest available driver using real shortest-path times.
4. Analyse traffic delays (Sliding Window) and route segments (Prefix Sum).

## 4. Scope and limitations
In scope: in-memory sample data, console menu, positive integer travel times, bidirectional roads.
Not included: GUI, database, GPS or live traffic, payments, authentication, reopening closed roads,
real-time movement of drivers (a driver's location does not change after assignment).
Traffic data is predefined and independent of the graph's travel times.

## 5. DSA concepts used
| Concept | Where |
|---|---|
| Graph + Adjacency List | CityGraph: `ArrayList<ArrayList<Road>> adj` |
| Dijkstra + Min Heap | CityGraph.dijkstra using `PriorityQueue<int[]>` |
| Priority Queue ranking | DriverManager.allocateNearestDriver |
| Sliding Window | TrafficAnalyzer.slidingWindowAverages |
| Prefix Sum | RouteAnalyzer.buildPrefixSum / rangeSum |
| Arrays / ArrayList | Throughout |

## 6. Algorithm explanations

**Adjacency list.** `adj.get(u)` holds the roads leaving location `u`. A bidirectional road is stored
twice (once in each endpoint's list), so closing a road switches off both entries.

**Dijkstra.** `dist[]` starts at infinity (0 for the source) and `parent[]` at -1. A Min Heap of
`{location, distance}` gives the nearest unfinished location. For each active road u->v, if
`dist[u] + time < dist[v]` we update `dist[v]`, `parent[v]` and push v (relaxation). Stale heap
entries (`d > dist[u]`) are skipped. The route is rebuilt by following `parent[]` backwards from the
destination and reversing. Positive weights guarantee that a location polled with the smallest
distance cannot be improved later.

**Road closure.** Roads have an `active` flag. Dijkstra skips inactive roads, so rerouting is just
another Dijkstra run. If `dist[destination]` stays infinity, there is no route.

**Driver allocation.** For each available driver, run Dijkstra from the driver's location and read
`dist[pickup]`. Drivers who cannot reach the pickup are skipped. The rest go into a PriorityQueue
ordered by ETA (ties by driver ID). `poll()` returns the best driver, who is then marked unavailable.

**Sliding Window.** Sum the first k readings once. For each next window: subtract the reading that
leaves, add the reading that enters, divide by k. Each window costs O(1) instead of O(k).

**Prefix Sum.** `prefix[i] = prefix[i-1] + segments[i]`. The total of segments l..r is
`prefix[r] - prefix[l-1]` (just `prefix[r]` when l = 0). One subtraction answers any range, and the
array can be reused for many queries. Example: segments [5,8,12,6,9], prefix [5,13,25,31,40];
segments 2..4 = 31 - 5 = 26.

## 7. Complexity (V = locations, E = roads, D = available drivers, n = readings, k = window, m = segments)

| Algorithm | Time | Space | In this project |
|---|---|---|---|
| Adjacency list (build) | O(V + E) | O(V + E) | 12 lists, 44 entries (22 roads x 2) |
| Adjacency list (neighbours of u) | O(deg u) | - | 1 to 4 roads per location |
| Dijkstra with Min Heap | O((V + E) log V) | O(V + E) | Each heap push/poll is O(log V); at most one push per successful relaxation |
| Road closure | O(deg u + deg v) | O(1) | Two short list scans |
| Driver allocation | O(D (V + E) log V + D log D) | O(V + E + D) | One Dijkstra per available driver dominates |
| Sliding Window | O(n) | O(n - k + 1) for the output | k + 2(n-k) additions vs k(n-k+1) for repeated summing |
| Prefix Sum build | O(m) | O(m) | One pass over the route's segments |
| Prefix Sum range query | O(1) | O(1) | One subtraction |

## 8. Test cases and results (all run on the final program)

| # | Test | Expected | Result |
|---|---|---|---|
| 1 | Central Park -> City Hospital | via Railway Station, 13 min | Pass |
| 2 | Close Central Park <-> Railway Station, recalculate | via Market Square, 15 min | Pass |
| 3 | Close Railway Station <-> City Hospital too | via University, 17 min | Pass |
| 4 | Close Airport <-> Tech Park, route to Airport | "No route available" | Pass |
| 5 | Close an already-closed road | "already closed" message | Pass |
| 6 | Driver pickup at Central Park | D102 (Market Square), 4 min | Pass |
| 7 | Driver at the pickup but unavailable (Driver C) | Ignored | Pass |
| 8 | Pickup at Airport with road closed | Only the driver already there (D112) is assigned; next request: "no available driver can reach" | Pass |
| 9 | Every driver already assigned | "No drivers are available right now" | Pass |
| 10 | Sliding Window [5,8,12,20,15,10,7], k = 3 | 8.33, 13.33, 15.67, 15.00, 10.67 | Pass |
| 11 | Prefix Sum [5,8,12,6,9] | [5,13,25,31,40]; segments 2..4 = 26 | Pass |
| 12 | Route Central Park -> Airport, segments 2..3 | [9,10,15], prefix [9,19,34], 34 - 9 = 25 | Pass |
| 13 | Invalid menu input ("abc", 9), invalid location | Re-prompts, no crash | Pass |

## 9. Sample output

```
Shortest route:
Central Park -> Market Square -> Railway Station -> City Hospital
Estimated travel time: 15 minutes
```
```
Average delay over each window of 3 readings:
Window 1: [5, 8, 12]  Average = 8.33
Window 2: [8, 12, 20]  Average = 13.33
Window 3: [12, 20, 15]  Average = 15.67
Check against repeated summing: results match.
Additions/subtractions needed -> Sliding Window: 13, Repeated summing: 18
```
```
Segment times : [9, 10, 15]
Prefix sums   : [9, 19, 34]
Travel time, segment 2 to 3: prefix[3] - prefix[1] = 34 - 9 = 25 minutes
```
```
Driver assigned!
Driver ID       : D102
Driver name     : Driver B
Driver location : Market Square
Pickup ETA      : 4 minutes
```

## 10. Future improvements
Reopen closed roads; one Dijkstra run from the pickup instead of one per driver; drivers that move
after assignment; load roads and drivers from a file; live-style traffic weights feeding Dijkstra;
a simple GUI.

## 11. Viva questions and answers

1. **Why an adjacency list and not a matrix?** The city is sparse (22 roads among 12 places). The
   list uses O(V + E) space, and Dijkstra only visits roads that exist.
2. **Why is each road stored twice?** The graph is undirected, so each direction needs its own entry
   in the starting location's list. Closing a road must switch off both.
3. **Why a PriorityQueue in Dijkstra?** It returns the unfinished location with the smallest distance
   in O(log V). A plain scan would cost O(V) per step.
4. **Why does Dijkstra need positive weights?** Once a location has the smallest distance, no later
   path can be shorter because every road adds time. Negative weights would break that.
5. **What are stale entries?** Java's PriorityQueue cannot update an entry in place, so we push a new
   one when we find a shorter distance. The older, larger entry is skipped when polled (`d > dist[u]`).
6. **How is the route reconstructed?** Follow `parent[]` from the destination back to the source and
   reverse. If the walk does not end at the source, the destination is unreachable.
7. **How does closing a road affect Dijkstra?** Dijkstra skips roads with `active == false`.
   No other code changes. If the destination stays at infinity, there is no route.
8. **Why use a PriorityQueue for drivers?** It ranks drivers by ETA, so `poll()` gives the best one,
   and the next-best is available if the first declines. (Picking only the minimum could also be done
   with a loop in O(D).)
9. **Can driver allocation be faster?** Yes. Roads are undirected, so one Dijkstra run from the
   pickup gives every driver's ETA at once. We ran one per driver, as the spec asked.
10. **What does Sliding Window save?** Re-adding k readings for every window costs k(n-k+1); sliding
    costs k + 2(n-k). The time per window drops from O(k) to O(1).
11. **How does Prefix Sum answer a range query?** `prefix[r]` is the total of everything up to r;
    subtracting `prefix[l-1]` removes everything before l. That is one subtraction, O(1).
12. **Why 0-based internally and 1-based on screen?** Arrays start at 0, but users count from 1. The
    conversion (`from - 1`, `to - 1`) happens in one place in Main.
13. **What if the source and destination are the same?** Dijkstra gives 0 minutes and a one-location
    route. Option 6 reports that there are no segments to analyse.
