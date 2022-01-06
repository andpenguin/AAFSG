package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import kaptainwutax.featureutils.structure.DesertPyramid;
import kaptainwutax.featureutils.structure.PillagerOutpost;
import kaptainwutax.featureutils.structure.RuinedPortal;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;

public class OverworldFilter {
    public static final double MAX_DIST = 250.0D * 250.0D;
    private final long seed;
    private final ChunkRand rand;
    public static RuinedPortal ruinedPortal = new RuinedPortal(Dimension.OVERWORLD, Main.VERSION);
    public static DesertPyramid pyramid = new DesertPyramid(Main.VERSION);
    public static PillagerOutpost outpost = new PillagerOutpost(Main.VERSION);
    private boolean hasPortal;
    private boolean hasTemple;
    private boolean hasOutpost;

    /**
     * Creates an OverworldFilter object with a given structureseed and random value
     * @param seed a structureseed to be filtered
     * @param rand a ChunkRand to check with
     */
    public OverworldFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
        hasPortal = false;
        hasTemple = false;
        hasOutpost = false;
    }

    /**
     * Filters the overworld of the structureseed, checking for a
     * Ruined portal within MAX_DIST, a temple within MAX_DIST
     * and an outpost within 500 blocks
     * @return true if all structures are within the given range, otherwise,
     *         false
     */
    public boolean filterOverworld() {
        for (int x = -1; x < 2; x++) { // loop through quadrants
            for (int z = -1; z < 2; z++) {
                CPos ruinedPortalLoc = ruinedPortal.getInRegion(seed, x, z, rand); // get the ruined portal in the region
                if (ruinedPortalLoc != null && ruinedPortalLoc.toBlockPos().distanceTo( // check the distance to 0,0
                        new BPos(0,0,0), DistanceMetric.EUCLIDEAN_SQ) <= MAX_DIST) {
                    Storage.ruinedPortalCoords = ruinedPortalLoc;
                    hasPortal = true;
                }
                CPos templeLoc = pyramid.getInRegion(seed, x, z, rand); // get the temple in the region
                if (templeLoc != null && templeLoc.toBlockPos().distanceTo( // check the distance to 0,0
                        new BPos(0,0,0), DistanceMetric.EUCLIDEAN_SQ) <= MAX_DIST) {
                    Storage.templeCoords = templeLoc;
                    hasTemple = true;
                }
                CPos outpostLoc = outpost.getInRegion(seed, x, z, rand); // get the outpost in the region
                if (outpostLoc != null && outpostLoc.toBlockPos().distanceTo( // check the distance to 0,0
                        new BPos(0,0,0), DistanceMetric.EUCLIDEAN_SQ) <= 500.0D * 500.0D) {
                    Storage.outpostCoords = outpostLoc;
                    hasOutpost = true;
                }
                if (hasPortal && hasTemple && hasOutpost) { // if all structures are present
                    return true;
                }
            }
        }
        return false;
    }
}
