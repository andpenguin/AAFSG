package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import kaptainwutax.featureutils.structure.DesertPyramid;
import kaptainwutax.featureutils.structure.Monument;
import kaptainwutax.featureutils.structure.PillagerOutpost;
import kaptainwutax.featureutils.structure.RuinedPortal;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;

public class OverworldFilter {
    public static final double MAX_DIST = 250.0D * 250.0D;
    private static long seed;
    private static ChunkRand rand;
    public static RuinedPortal ruinedPortal = new RuinedPortal(Dimension.OVERWORLD, Main.VERSION);
    public static DesertPyramid pyramid = new DesertPyramid(Main.VERSION);
    public static PillagerOutpost outpost = new PillagerOutpost(Main.VERSION);
    private static boolean hasPortal;
    private static boolean hasTemple;
    private static boolean hasOutpost;

    public OverworldFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
        hasPortal = false;
        hasTemple = false;
        hasOutpost = false;
    }

    public boolean filterOverworld() {
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                CPos ruinedPortalLoc = ruinedPortal.getInRegion(seed, x, z, rand);
                if (ruinedPortalLoc != null && ruinedPortalLoc.toBlockPos().distanceTo(
                        new BPos(0,0,0), DistanceMetric.EUCLIDEAN_SQ) <= MAX_DIST) {
                    Storage.ruinedPortalCoords = ruinedPortalLoc;
                    hasPortal = true;
                }
                CPos templeLoc = pyramid.getInRegion(seed, x, z, rand);
                if (templeLoc != null && templeLoc.toBlockPos().distanceTo(
                        new BPos(0,0,0), DistanceMetric.EUCLIDEAN_SQ) <= MAX_DIST) {
                    Storage.templeCoords = templeLoc;
                    hasTemple = true;
                }
                CPos outpostLoc = outpost.getInRegion(seed, x, z, rand);
                if (outpostLoc != null && outpostLoc.toBlockPos().distanceTo(
                        new BPos(0,0,0), DistanceMetric.EUCLIDEAN_SQ) <= 500.0D * 500.0D) {
                    Storage.outpostCoords = outpostLoc;
                    hasOutpost = true;
                }
                if (hasPortal && hasTemple && hasOutpost) {
                    return true;
                }
            }
        }
        return false;
    }
}
