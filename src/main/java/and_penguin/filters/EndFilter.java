package and_penguin.filters;

import and_penguin.Main;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.featureutils.structure.EndCity;
import kaptainwutax.featureutils.structure.generator.structure.EndCityGenerator;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.util.pos.RPos;
import kaptainwutax.terrainutils.TerrainGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class EndFilter {
    private final long seed;
    private final ChunkRand rand;
    private static final double MAX_DIST = 100.0D * 100.0D;
    private static final EndCity END_CITY = new EndCity(Main.VERSION);

    /**
     * Creates an EndFilter object with a given structureseed and random value
     * @param seed a structureseed to be filtered
     * @param rand a ChunkRand to check with
     */
    public EndFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
    }

    /**
     * Finds the block position of the first gateway
     * @param structureSeed the structure seed to be checked
     * @return the Block position of the gateway
     */
    public static BPos firstGateway(long structureSeed) {
        ArrayList<Integer> gateways = new ArrayList<>();
        for (int i = 0; i < 20; i++) gateways.add(i);
        Collections.shuffle(gateways, new Random(structureSeed));
        double angle = 2.0 * (-1*Math.PI + 0.15707963267948966 * (gateways.remove(gateways.size() - 1)));
        int gateway_x = (int)(1000.0 * Math.cos(angle));
        int gateway_z = (int)(1000.0 * Math.sin(angle));
        return new BPos(gateway_x, 0, gateway_z);
    }

    /**
     * Filters the overworld of the structureseed, checking for a
     * Ship containing End City within MAX_DIST from the gateway
     * @return true if all structures are within the given range, otherwise,
     *         false
     */
    public boolean filterEnd() {
        BPos gatewayLocation = firstGateway(seed);

        RPos region = gatewayLocation.toRegionPos(20 << 4);
        CPos city = END_CITY.getInRegion(seed, region.getX(), region.getZ(), rand);
        if (city.toBlockPos().distanceTo(gatewayLocation, DistanceMetric.EUCLIDEAN_SQ) > MAX_DIST) return false;

        BiomeSource biomeSource = BiomeSource.of(Dimension.END, Main.VERSION, seed);
        TerrainGenerator generator = TerrainGenerator.of(Dimension.END, biomeSource);

        if (!END_CITY.canSpawn(city.getX(), city.getZ(), biomeSource)) return false;
        if (!END_CITY.canGenerate(city.getX(), city.getZ(), generator)) return false;

        EndCityGenerator endCityGenerator = new EndCityGenerator(Main.VERSION);
        endCityGenerator.generate(generator, city, rand);

        return endCityGenerator.hasShip();
    }
}
