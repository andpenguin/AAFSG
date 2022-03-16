package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import kaptainwutax.featureutils.structure.DesertPyramid;
import kaptainwutax.featureutils.structure.Village;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;

public class OverworldFilter {
    public static final double MAX_DIST = 16.0D * 16.0D;
    private final long seed;
    private final ChunkRand rand;
    public static DesertPyramid pyramid = new DesertPyramid(Main.VERSION);
    public static Village village = new Village(Main.VERSION);

    /**
     * Creates an OverworldFilter object with a given structureseed and random value
     * @param seed a structureseed to be filtered
     * @param rand a ChunkRand to check with
     */
    public OverworldFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
    }

    /**
     * Filters the overworld of the structureseed, checking for a
     * A temple within 0,0 quadrant
     * and an village within 16 chunks
     * @return true if all structures are within the given range, otherwise,
     *         false
     */
    public boolean filterOverworld() {
        CPos templeLoc = pyramid.getInRegion(seed, 0, 0, rand); // get the temple in the region
        if (templeLoc != null && templeLoc.distanceTo( // check the distance to 0,0
                new BPos(0,0,0), DistanceMetric.EUCLIDEAN_SQ) <= MAX_DIST) {
            Storage.templeCoords = templeLoc;
        }
        else return false;
        for (int x = -1; x < 1; x++) { // loop through quadrants
            for (int z = -1; z < 1; z++) {
                CPos villageLoc = village.getInRegion(seed, x, z, rand); // get the village in the region
                if (villageLoc != null && villageLoc.distanceTo( // check the distance to 0,0
                        new BPos(0,0,0), DistanceMetric.EUCLIDEAN_SQ) <= MAX_DIST) {
                    Storage.villageCoords = villageLoc;
                    return true;
                }
            }
        }
        return false;
    }
}
