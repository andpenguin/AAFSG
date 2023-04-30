package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.source.NetherBiomeSource;
import com.seedfinding.mcfeature.structure.BastionRemnant;
import com.seedfinding.mcfeature.structure.Fortress;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.rand.ChunkRand;

public class NetherFilter {
    private final long seed;
    private final ChunkRand rand;
    public static final double MAX_DIST = 14.0D * 14.0D;
    public static Fortress fortress = new Fortress(Main.VERSION);
    public static BastionRemnant bastion = new BastionRemnant(Main.VERSION);

    /**
     * Creates an NetherFilter object with a given structure seed and random value
     * @param seed a structure seed to be filtered
     * @param rand a ChunkRand to check with
     */
    public NetherFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
    }

    /**
     * Filters the overworld of the structure seed, checking for a
     * Bastion within MAX_DIST of a fortress
     * and the Bastion is within MAX_DIST from 0,0
     * @return true if all structures are within the given range, otherwise,
     *         false
     */
    public boolean filterNether() {
        Storage.fortCoords = null;
        Storage.bastionCoords = null;
        CPos[] fortLocs = new CPos[4];
        CPos[] bastionLocs = new CPos[4];
        for (int x = -1; x < 1; x++) { // loop through quadrants looking for a structures
            for (int z = -1; z < 1; z++) {
                fortLocs[(x+1) + (z+1)*2] = fortress.getInRegion(seed, x, z, rand);
                bastionLocs[(x+1) + (z+1)*2] = bastion.getInRegion(seed, x, z, rand);
            }
        }
        fastionLoop: for (CPos fortLoc: fortLocs) {
            if (fortLoc == null) continue;
            for (CPos bastionLoc : bastionLocs) {
                if (bastionLoc == null) continue;
                if (bastionLoc.distanceTo(fortLoc, DistanceMetric.EUCLIDEAN_SQ) // if bastion is close to fort and spawn
                        <= MAX_DIST && bastionLoc.getMagnitudeSq() <= MAX_DIST) {
                    Storage.fortCoords = fortLoc; // save the coords
                    Storage.bastionCoords = bastionLoc;
                    break fastionLoop;
                }
            }
        }
        if (Storage.fortCoords == null || Storage.bastionCoords == null)
            return false;
        return NetherFilter.bastion.canSpawn(Storage.bastionCoords, new NetherBiomeSource(Main.VERSION, seed));
    }
}
