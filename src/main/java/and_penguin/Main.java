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
        ChunkRand rand = new ChunkRand();
        Random numRand = new Random();
        long seed = numRand.nextLong() % (1L << 48);
        while (seeds.size() < 1) {
            if (filterStructureSeed(seed, rand)) {
                System.out.println("Structure seed found, getting biome match");
                for (long biomeSeed = 0L; biomeSeed < 1L << 16; biomeSeed++) {
                    if (filterSeed(biomeSeed<<48|seed)) {
                        long finalSeed = biomeSeed<<48|seed;
                        seeds.add(finalSeed);
                        System.out.println("Seed: " + finalSeed + " Time: " + new Date());
                        break;
                    }
                }
           }
            seed = numRand.nextLong() % (1L << 48);
       }
        System.out.println(seeds);
        System.out.println(seeds.size());
    }

    public static boolean filterStructureSeed(Long seed, ChunkRand rand) {
        return  new OverworldFilter(seed, rand).filterOverworld() &&
                new NetherFilter(seed, rand).filterNether() &&
                new EndFilter(seed, rand).filterEnd();
    }

    public static boolean filterSeed(Long seed) {
        return new BiomeFilter(seed).filterBiomeSeed();
    }
}
