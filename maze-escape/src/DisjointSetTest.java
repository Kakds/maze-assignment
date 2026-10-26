/**
 * Checks of Mission 0. Run:  java -cp out DisjointSetTest
 * Add your own checks below: a large chain, a union of a set with itself, count after many unions.
 */
public class DisjointSetTest {

    static int failed = 0;

    static void check(boolean ok, String what) {
        System.out.println((ok ? "ok    " : "FAIL  ") + what);
        if (!ok) failed++;
    }

    public static void main(String[] args) {
        DisjointSet d = new DisjointSet(6);
        check(d.count() == 6, "6 sets at the start");
        check(!d.connected(0, 1), "0 and 1 are apart at the start");
        check(d.union(0, 1), "union(0, 1) joins two sets");
        check(d.connected(0, 1), "0 and 1 are together");
        check(!d.union(1, 0), "union(1, 0) again returns false");
        check(d.count() == 5, "5 sets after one union");
        d.union(2, 3); d.union(1, 3);
        check(d.connected(0, 2), "0 and 2 are together through 1 and 3");
        check(d.count() == 3, "3 sets: {0,1,2,3} {4} {5}");
        check(d.find(4) == 4 && d.find(5) == 5, "a single cell is its own root");

        // own checks
        check(!d.union(0, 0), "union of a cell with itself returns false");
        check(!d.union(2, 0), "union of two cells of one set returns false");
        check(d.count() == 3, "count does not change after failed unions");
        check(d.sizeOf(3) == 4 && d.sizeOf(4) == 1, "sizes: {0,1,2,3} has 4, {4} has 1");
        d.union(4, 5);
        check(d.connected(4, 5) && !d.connected(3, 4), "4 and 5 together, apart from 3");
        d.union(5, 0);
        check(d.count() == 1, "one set after all unions");
        check(d.find(0) == d.find(5), "all cells share one root");

        // a large chain: union(i, i+1) for a million cells, must not overflow the stack
        int n = 1_000_000;
        DisjointSet big = new DisjointSet(n);
        boolean allTrue = true;
        for (int i = 0; i + 1 < n; i++) allTrue &= big.union(i, i + 1);
        check(allTrue, "large chain: every union(i, i+1) returns true");
        check(big.count() == 1, "large chain: one set at the end");
        check(big.connected(0, n - 1), "large chain: first and last cell are connected");
        check(big.sizeOf(12345) == n, "large chain: the set has all " + n + " cells");

        // count after many unions: pairs (2i, 2i+1)
        DisjointSet pairs = new DisjointSet(1000);
        for (int i = 0; i < 1000; i += 2) pairs.union(i, i + 1);
        check(pairs.count() == 500, "500 sets after joining 500 pairs");
        check(!pairs.connected(1, 2), "different pairs stay apart");

        System.out.println(failed == 0 ? "All checks passed." : failed + " checks failed.");
    }
}
