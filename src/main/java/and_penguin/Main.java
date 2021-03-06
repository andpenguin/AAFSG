package and_penguin;

import and_penguin.filters.BiomeFilter;
import and_penguin.filters.EndFilter;
import and_penguin.filters.NetherFilter;
import and_penguin.filters.OverworldFilter;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.mcutils.rand.ChunkRand;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    public static final MCVersion VERSION = MCVersion.v1_16_1;
    public static int specialCount;
    public static int templeCount;
    public static int templeTotal;
    public static int villageCount;
    public static int villageTotal;

    public static void main(String[] args) {
        System.out.println("Generating a seed");
        ChunkRand rand = new ChunkRand(); // Random for seed checking
        Random numRand = new Random(); // Random for choosing a seed
        long seed = numRand.nextLong() % (1L << 48); // first seed
        while (true) { // Continuously generate seeds
            if (filterStructureSeed(seed, rand)) { // Check if structureseed is valid
                System.out.println("Structure seed found, getting biome match");
                for (long biomeSeed = 0L; biomeSeed < 1L << 16; biomeSeed++) { // Check 2^16 biome seeds
                    if ((biomeSeed >= 10 && specialCount == 0) || (templeTotal >= 100 && templeCount == 0)
                        || (villageCount >= 10 && villageTotal == 0))
                        break;
                    if (filterSeed(biomeSeed<<48|seed)) { // If the biome seed matches
                        long finalSeed = biomeSeed<<48|seed;
                        String output = "Seed: " + finalSeed + " Time: " + new Date();
                        try {
                            FileWriter writer = new FileWriter("./js/seeds.txt", true);
                            writer.write(finalSeed + "\n");
                            writer.close();
                        }
                        catch (IOException e) { System.out.println(e); }
                        System.out.println(output); // Print out the seed and time
                        break; // stop checking 2^16 biome seeds
                    }
                }
                System.out.println(seed);
                getResults();
            }
            seed = numRand.nextLong() % (1L << 48);
        }
    }

    public static void getResults() {
        System.out.println("Specials passed: " + specialCount + " out of 65536" +
                "\nTemples passed: " + templeCount + " out of " + templeTotal +
                "\nVillages passed: " + villageCount + " out of " + villageTotal);
        specialCount = 0;
        templeCount = 0;
        templeTotal = 0;
        villageCount = 0;
        villageTotal = 0;
    }

    /**
     *
     * @param seed a structure seed to be checked
     * @param rand a ChunkRand to be used to check the seed
     * @return true if the seed has a good overworld, nether, and end, otherwise,
     *         false
     */
    public static boolean filterStructureSeed(Long seed, ChunkRand rand) {
        EndFilter endFilter = new EndFilter(seed, rand);
        NetherFilter netherFilter = new NetherFilter(seed, rand);
        OverworldFilter overworldFilter = new OverworldFilter(seed, rand);
        return  endFilter.filterEnd() && // end structures
                netherFilter.filterNether() && // nether structures
                overworldFilter.filterOverworld() && // overworld structures
                endFilter.filterEndBiomes(); // end structures can spawn
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
