package and_penguin;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.version.MCVersion;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

    public static final MCVersion VERSION = MCVersion.v1_16_1;
    public static long finalSeed;

    public static void main(String[] args) {
        System.out.println("Generating a seed");
        ChunkRand rand = new ChunkRand();
        Random random = new Random();
        long seed;
        while (true) { // Continuously generate seeds
            seed = random.nextLong() % (1L << 48);
            finalSeed = SeedFilter.filterSeed(seed,rand);
            if (finalSeed != 0) {
                String output = "Seed: " + finalSeed + " Time: " + new Date();
                try {
                    FileWriter writer = new FileWriter("./js/seeds.txt", true);
                    writer.write(finalSeed + "\n");
                    writer.close();
                    System.out.println(output); // Print out the seed and time
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }
}