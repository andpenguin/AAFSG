package and_penguin;

import and_penguin.filters.*;
import com.seedfinding.mccore.rand.ChunkRand;

import java.util.HashSet;

public class SeedFilter {

    /**
     * Checks to see if the randomly generated structure seed, or its world seeds pass
     * the filter.
     * @param structureSeed A structure seed to be checked
     * @param rand a ChunkRand to be used to check the seed
     * @return the seed if it is valid, otherwise, 0 is returned
     */
    public static long filterSeed(long structureSeed, ChunkRand rand) {
        structureSeed: if (seedHasStructures(structureSeed, rand)) {
            HashSet<Long> seedSet = new HashSet<>();
            for (long biomeSeed = 0L; biomeSeed < 1L << 16; biomeSeed++) { // Check 2^16 biome seeds
                long worldSeed = biomeSeed<<48|structureSeed;
                BiomeFilter biomeFilter = new BiomeFilter(worldSeed);
                if (biomeFilter.hasStructures()) {
                    seedSet.add(worldSeed);
                }

                if (biomeSeed == 100 && seedSet.size() == 0) {
                    break structureSeed;
                }
            }

            for (long currentSeed : seedSet) {
                BiomeFilter biomeFilter = new BiomeFilter(currentSeed);
                if (biomeFilter.filterBiomeSpawn()) {
                    return currentSeed;
                }
            }
        }
        return 0;
    }

    /**
     *
     * @param structureSeed a structure seed to be checked
     * @param rand a ChunkRand to be used to check the seed
     * @return true if the seed has a good overworld, nether, and end, otherwise,
     *         false
     */
    public static boolean seedHasStructures(long structureSeed, ChunkRand rand) {
        NetherFilter netherFilter = new NetherFilter(structureSeed, rand);
        OverworldFilter overworldFilter = new OverworldFilter(structureSeed, rand);
        EndFilter endFilter = new EndFilter(structureSeed, rand);
        return (netherFilter.filterNether() && overworldFilter.filterOverworld() && endFilter.filterEnd()); //This order because of speed hierarchy.
    }
}