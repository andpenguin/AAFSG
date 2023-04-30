package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcfeature.structure.EndCity;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mcterrain.TerrainGenerator;
import com.seedfinding.mcfeature.structure.generator.structure.EndCityGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class EndFilter {
    private final long seed;
    private final ChunkRand rand;
    private static final double MAX_DIST = 512.0D * 512.0D;
    private static final EndCity END_CITY = new EndCity(Main.VERSION);
    private CPos city;

    /**
     * Creates an EndFilter object with a given structureseed and random value
     * @param seed a structureseed to be filtered
     * @param rand a ChunkRand to check with
     */
    public EndFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
        city = null;
        Storage.cityPosition = null;
    }

    /**
     * Finds the block position of the first gateway
     * @param seed the structure seed to be checked
     * @return the Block position of the gateway
     */
    public static BPos firstGateway(long seed) {
        ArrayList<Integer> gateways = new ArrayList<>();
        for (int i = 0; i < 20; i++) gateways.add(i);
        Collections.shuffle(gateways, new Random(seed));
        double angle = 2.0 * (-1*Math.PI + 0.15707963267948966 * (gateways.remove(gateways.size() - 1)));
        int gateway_x = (int)(1000.0 * Math.cos(angle));
        int gateway_z = (int)(1000.0 * Math.sin(angle));
        return new BPos(gateway_x, 0, gateway_z);
    }

    /**
     * Filters the end for an End City with a ship within
     * MAX_DIST, and then filters for another End City
     * with a ship within MAX_DIST of the first city.
     * @return true if it finds both cities, otherwise,
     *         false
     */
    public boolean filterEnd() {
        if (hasCloseCity(firstGateway(seed))) {
            return hasCloseCity(Storage.cityPosition);
        }
        return false;
    }

    /**
     * Filters the end of the structure seed, checking for an
     * End City within MAX_DIST from the key-position with a ship.
     * @return true if it finds an End City within the distance, otherwise,
     *         false
     */
    public boolean hasCloseCity(BPos position) {
        RPos region = position.toRegionPos(20 << 4);
        RPos[] regions = new RPos[9];
        int counter = 0;
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                if (Storage.cityPosition != null && x == 0 && z == 0) {
                    continue;
                }
                regions[counter++] = region.add(x,z);
            }
        }
        for (RPos r: regions) {
            city = END_CITY.getInRegion(seed, r.getX(), r.getZ(), rand);
            if (city.toBlockPos().distanceTo(position, DistanceMetric.EUCLIDEAN_SQ) <= MAX_DIST) {
                Storage.cityPosition = city.toBlockPos();
                return citySpawnsWithShip();
            }
        }
        return false;
    }

    /**
     * Checks if the end city can spawn and if it contains a ship
     * @return true if the end city can spawn and has a ship, otherwise,
     *         false
     */
    public boolean citySpawnsWithShip() {
        BiomeSource biomeSource = BiomeSource.of(Dimension.END, Main.VERSION, seed);
        TerrainGenerator generator = TerrainGenerator.of(Dimension.END, biomeSource);
        if (city == null) return false;
        if (!END_CITY.canSpawn(city.getX(), city.getZ(), biomeSource)) return false;
        if (!END_CITY.canGenerate(city.getX(), city.getZ(), generator)) return false;
        EndCityGenerator endCityGenerator = new EndCityGenerator(Main.VERSION);
        endCityGenerator.generate(generator, city, rand);
        return endCityGenerator.hasShip();
    }
}