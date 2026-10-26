import java.util.Arrays;

/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1, slide i goes from s.from()[i] to s.to()[i]
 * and takes s.time()[i]. There are no slides from a cell to itself, and the exit can always be reached from the start.
 *
 * Graphs.slidesFrom(s)[u] is the list of the slides that leave cell u, and Graphs.slidesInto(s)[v] is the list
 * of the slides that enter cell v (reversed graph).
 */
public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * The zones are numbered in the order in which the second pass finds them: a topological order of the zone graph.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] out = Graphs.slidesFrom(s);
        int[][] in = Graphs.slidesInto(s);

        // Pass 1: iterative DFS on the slides, record cells in the order they are finished
        int[] order = new int[n];
        int finished = 0;
        boolean[] seen = new boolean[n];
        int[] stack = new int[n];
        int[] next = new int[n];   // next[i] = index of the next slide to look at from stack[i]
        for (int start = 0; start < n; start++) {
            if (seen[start]) continue;
            int top = 0;
            stack[0] = start;
            next[0] = 0;
            seen[start] = true;
            while (top >= 0) {
                int v = stack[top];
                if (next[top] < out[v].length) {
                    int u = s.to()[out[v][next[top]++]];
                    if (!seen[u]) {
                        seen[u] = true;
                        stack[++top] = u;
                        next[top] = 0;
                    }
                } else {
                    order[finished++] = v;   // all slides out of v are done: v is finished
                    top--;
                }
            }
        }

        // Pass 2: from the last finished cell to the first, DFS on the reversed slides
        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);
        int zones = 0;
        int[] todo = new int[n];   // plain stack; order inside a zone does not matter here
        for (int i = n - 1; i >= 0; i--) {
            int start = order[i];
            if (zoneOf[start] != -1) continue;
            int top = 0;
            todo[0] = start;
            zoneOf[start] = zones;
            while (top >= 0) {
                int v = todo[top--];
                for (int e : in[v]) {
                    int u = s.from()[e];
                    if (zoneOf[u] == -1) {
                        zoneOf[u] = zones;
                        todo[++top] = u;
                    }
                }
            }
            zones++;
        }
        return zoneOf;
    }

    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        int n = s.n();
        if (zoneCount == 0) return new long[]{0, 0};

        // Build the zone graph: group the slides that go between two different zones by their source zone.
        // Several slides X -> Y may exist; relaxing all of them gives the same result as keeping only the cheapest.
        int[] deg = new int[zoneCount];
        for (int i = 0; i < s.count(); i++) {
            int x = zoneOf[s.from()[i]], y = zoneOf[s.to()[i]];
            if (x != y) deg[x]++;
        }
        int[][] edges = new int[zoneCount][];
        for (int z = 0; z < zoneCount; z++) edges[z] = new int[deg[z]];
        int[] fill = new int[zoneCount];
        for (int i = 0; i < s.count(); i++) {
            int x = zoneOf[s.from()[i]], y = zoneOf[s.to()[i]];
            if (x != y) edges[x][fill[x]++] = i;
        }

        // One pass in topological order (zone numbers): every edge goes from a smaller to a larger number,
        // so when we reach zone z, all edges into z have already been relaxed.
        long INF = Long.MAX_VALUE / 4;
        long[] chain = new long[zoneCount];   // chain[z] = zones on the longest chain that ends in z
        long[] dist = new long[zoneCount];    // dist[z] = cheapest time from the zone of the start to z
        Arrays.fill(chain, 1);
        Arrays.fill(dist, INF);
        int startZone = zoneOf[0], exitZone = zoneOf[n - 1];
        dist[startZone] = 0;
        long stages = 0;

        for (int x = 0; x < zoneCount; x++) {
            stages = Math.max(stages, chain[x]);
            for (int e : edges[x]) {
                int y = zoneOf[s.to()[e]];
                if (y <= x) throw new IllegalStateException("zone order is not topological");
                chain[y] = Math.max(chain[y], chain[x] + 1);
                if (dist[x] != INF) dist[y] = Math.min(dist[y], dist[x] + s.time()[e]);
            }
        }
        long cost = dist[exitZone] == INF ? -1 : dist[exitZone];
        return new long[]{stages, cost};
    }
}
