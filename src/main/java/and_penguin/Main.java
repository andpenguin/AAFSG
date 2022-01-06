package and_penguin;

import and_penguin.filters.BiomeFilter;
import and_penguin.filters.EndFilter;
import and_penguin.filters.NetherFilter;
import and_penguin.filters.OverworldFilter;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.mcutils.rand.ChunkRand;

import java.util.*;

public class Main {
    public static final MCVersion VERSION = MCVersion.v1_16_1;
    private static final ArrayList<Long> seeds = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Generating a seed");
        ChunkRand rand = new ChunkRand(); // Random for seed checking
        Random numRand = new Random(); // Random for choosing a seed
        long seed = numRand.nextLong() % (1L << 48); // first seed
        while (seeds.size() < 1) { // Until 1 seed is generated
            if (filterStructureSeed(seed, rand)) { // Check if structureseed is valid
                System.out.println("Structure seed found, getting biome match");
                for (long biomeSeed = 0L; biomeSeed < 1L << 16; biomeSeed++) { // Check 2^16 biome seeds
                    if (filterSeed(biomeSeed<<48|seed)) { // If the biome seed matches
                        long finalSeed = biomeSeed<<48|seed;
                        seeds.add(finalSeed);
                        System.out.println("Seed: " + finalSeed + " Time: " + new Date()); // Print out the seed and time
                        break; // stop checking 2^16 biome seeds
                    }
                }
           }
            seed = numRand.nextLong() % (1L << 48);
       }
        System.out.println(seeds); // Print all matching seeds (useful when generating more than 1)
    }

    /**
     *
     * @param seed a structure seed to be checked
     * @param rand a ChunkRand to be used to check the seed
     * @return true if the seed has a good overworld, nether, and end, otherwise,
     *         false
     */
    public static boolean filterStructureSeed(Long seed, ChunkRand rand) {
        return  new OverworldFilter(seed, rand).filterOverworld() &&
                new NetherFilter(seed, rand).filterNether() &&
                new EndFilter(seed, rand).filterEnd();
    }

    /**
     *
     * @param seed a worldseed to be checked
     * @return true if the biomeseed is good, otherwise,
     *         false
     */
    public static boolean filterSeed(Long seed) {
        return new BiomeFilter(seed).filterBiomeSeed();
    }
}
