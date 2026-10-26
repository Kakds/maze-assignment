/**
 * Mission 3. Escape: DFS from cell 0 to the last cell (rows * cols - 1) in the maze of Mission 1.
 * Two neighbouring cells are connected when the wall between them is broken.
 * The neighbours are tried in this order: up, right, down, left (cell - cols, cell + 1, cell + cols, cell - 1).
 * The DFS uses its own stack (arrays), not recursion, so a 1000 x 1000 maze does not overflow the call stack.
 */
public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int rows = w.rows(), cols = w.cols(), n = w.cells();
        int exit = n - 1;
        int[][] open = Graphs.open(w, broken);

        // Dead ends: cells with exactly one open door
        int deadEnds = 0;
        for (int v = 0; v < n; v++) if (open[v].length == 1) deadEnds++;

        if (n == 0) return new Result(new int[0], 0, deadEnds);
        if (n == 1) return new Result(new int[]{0}, 1, deadEnds);

        boolean[] visited = new boolean[n];
        int[] stack = new int[n];   // the cells of the current DFS path, start at stack[0]
        int[] dir = new int[n];     // dir[i] = next direction (0 up, 1 right, 2 down, 3 left) to try from stack[i]
        int top = 0;
        stack[0] = 0;
        dir[0] = 0;
        visited[0] = true;
        int marked = 1;
        boolean found = false;

        while (top >= 0) {
            int v = stack[top];
            if (dir[top] == 4) { top--; continue; }   // all four directions tried: go back
            int d = dir[top]++;
            int r = v / cols, c = v % cols, u;
            switch (d) {
                case 0 -> u = r > 0 ? v - cols : -1;            // up
                case 1 -> u = c + 1 < cols ? v + 1 : -1;        // right
                case 2 -> u = r + 1 < rows ? v + cols : -1;     // down
                default -> u = c > 0 ? v - 1 : -1;              // left
            }
            if (u < 0 || visited[u] || !isOpen(open[v], u)) continue;
            visited[u] = true;
            marked++;
            stack[++top] = u;
            dir[top] = 0;
            if (u == exit) { found = true; break; }
        }

        if (!found) return new Result(new int[0], marked, deadEnds);
        // the stack holds exactly the path from the start to the exit
        int[] path = new int[top + 1];
        System.arraycopy(stack, 0, path, 0, top + 1);
        return new Result(path, marked, deadEnds);
    }

    /** True if u is in the list of open neighbours (the list has at most 4 cells). */
    private static boolean isOpen(int[] neighbours, int u) {
        for (int x : neighbours) if (x == u) return true;
        return false;
    }
}
