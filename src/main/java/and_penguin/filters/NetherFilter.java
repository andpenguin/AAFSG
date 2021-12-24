package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.featureutils.structure.BastionRemnant;
import kaptainwutax.featureutils.structure.Fortress;
import kaptainwutax.featureutils.structure.Monument;
import kaptainwutax.featureutils.structure.RuinedPortal;
import kaptainwutax.featureutils.structure.generator.structure.RuinedPortalGenerator;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;

public class NetherFilter {
    private static long seed;
    private static ChunkRand rand;
    public static final double MAX_DIST = 10.0D * 10.0D;
    public static Fortress fortress = new Fortress(Main.VERSION);
    public static BastionRemnant bastion = new BastionRemnant(Main.VERSION);

    public NetherFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
    }

    public boolean filterNether() {
        Storage.fortCoords = null;
        Storage.bastionCoords = null;
        CPos[] fortLocs = new CPos[4];
        CPos[] bastionLocs = new CPos[4];
        for (int x = -1; x < 1; x++) {
            for (int z = -1; z < 1; z++) {
                 fortLocs[2 + x + z] = fortress.getInRegion(seed, x, z, rand);
                bastionLocs[2 + x + z] = bastion.getInRegion(seed, x, z, rand);
            }
        }
        for (CPos fortLoc: fortLocs) {
            if (fortLoc == null) continue;
            for (CPos bastionLoc : bastionLocs) {
                if (bastionLoc == null) continue;
                if (bastionLoc.distanceTo(fortLoc, DistanceMetric.EUCLIDEAN_SQ)
                    <= MAX_DIST && bastionLoc.getMagnitudeSq() <= MAX_DIST) {
                    Storage.fortCoords = fortLoc;
                    Storage.bastionCoords = bastionLoc;
                }
            }
        }
        if (Storage.fortCoords == null || Storage.bastionCoords == null)
            return false;
        NetherBiomeSource nether = new NetherBiomeSource(Main.VERSION, seed);
        if (!NetherFilter.bastion.canSpawn(Storage.bastionCoords.getX(),
                Storage.bastionCoords.getZ(), nether))
            return false;
        if (!NetherFilter.fortress.canSpawn(Storage.fortCoords.getX(),
                Storage.fortCoords.getZ(), nether))
            return false;
        if (NetherFilter.bastion.getBiome() != Biomes.BASALT_DELTAS)
            return true;
        return false;
    }
}
