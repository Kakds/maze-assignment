import java.util.*;

/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet dsu = new DisjointSet(w.cells());
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int k = 0;
        for (int i = 0; i < w.count() && k < broken.length; i++) {
            if (dsu.union(w.a()[i], w.b()[i])) broken[k++] = i;
        }
        return Arrays.copyOf(broken, k);
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        int m = w.count();
        int[] cost = w.cost();
        // sort wall indexes by cost: pack (cost, index) into one long and sort primitives (fast, no boxing)
        long[] keys = new long[m];
        for (int i = 0; i < m; i++) keys[i] = ((long) cost[i] << 32) | i;
        Arrays.sort(keys);

        DisjointSet dsu = new DisjointSet(w.cells());
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int k = 0;
        for (int j = 0; j < m && k < broken.length; j++) {
            int i = (int) keys[j];   // low 32 bits = wall index
            if (dsu.union(w.a()[i], w.b()[i])) broken[k++] = i;
        }
        return Arrays.copyOf(broken, k);
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells();
        if (n == 0) return 0;
        // adjacency list: for every cell, the indexes of the walls touching it
        int[] deg = new int[n];
        for (int i = 0; i < w.count(); i++) { deg[w.a()[i]]++; deg[w.b()[i]]++; }
        int[][] adj = new int[n][];
        for (int v = 0; v < n; v++) adj[v] = new int[deg[v]];
        int[] fill = new int[n];
        for (int i = 0; i < w.count(); i++) {
            adj[w.a()[i]][fill[w.a()[i]]++] = i;
            adj[w.b()[i]][fill[w.b()[i]]++] = i;
        }

        boolean[] inTree = new boolean[n];
        // queue entries are (cost << 32 | wall index), so the cheapest wall comes out first
        PriorityQueue<Long> pq = new PriorityQueue<>();
        long total = 0;
        int added = 1;
        inTree[0] = true;
        for (int e : adj[0]) pq.add(((long) w.cost()[e] << 32) | e);

        while (!pq.isEmpty() && added < n) {
            long top = pq.poll();
            int e = (int) (top & 0xFFFFFFFFL);
            int a = w.a()[e], b = w.b()[e];
            int next = inTree[a] ? b : a;
            if (inTree[next]) continue;      // both ends already in the tree: this wall would make a cycle
            inTree[next] = true;
            added++;
            total += w.cost()[e];
            for (int f : adj[next]) {
                int other = w.a()[f] == next ? w.b()[f] : w.a()[f];
                if (!inTree[other]) pq.add(((long) w.cost()[f] << 32) | f);
            }
        }
        return total;
    }
}
