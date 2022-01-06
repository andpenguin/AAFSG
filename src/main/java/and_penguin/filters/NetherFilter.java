package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.featureutils.structure.BastionRemnant;
import kaptainwutax.featureutils.structure.Fortress;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.CPos;

public class NetherFilter {
    private final long seed;
    private final ChunkRand rand;
    public static final double MAX_DIST = 10.0D * 10.0D;
    public static Fortress fortress = new Fortress(Main.VERSION);
    public static BastionRemnant bastion = new BastionRemnant(Main.VERSION);

    /**
     * Creates an NetherFilter object with a given structureseed and random value
     * @param seed a structureseed to be filtered
     * @param rand a ChunkRand to check with
     */
    public NetherFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
    }

    /**
     * Filters the overworld of the structureseed, checking for a
     * Non Basalt Bastion within MAX_DIST of a fortress
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
                fortLocs[2 + x + z] = fortress.getInRegion(seed, x, z, rand);
                bastionLocs[2 + x + z] = bastion.getInRegion(seed, x, z, rand);
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
        if (Storage.fortCoords == null || Storage.bastionCoords == null) // if fastion not found
            return false;
        NetherBiomeSource nether = new NetherBiomeSource(Main.VERSION, seed);
        if (!NetherFilter.bastion.canSpawn(Storage.bastionCoords.getX(), // check if the bastion can spawn
                Storage.bastionCoords.getZ(), nether))
            return false;
        if (!NetherFilter.fortress.canSpawn(Storage.fortCoords.getX(), // check if the fortress can spawn
                Storage.fortCoords.getZ(), nether))
            return false;
        return NetherFilter.bastion.getBiome() != Biomes.BASALT_DELTAS; // check if the bastion is in a basalt
    }
}
