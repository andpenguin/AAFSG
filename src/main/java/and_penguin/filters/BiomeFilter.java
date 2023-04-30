package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.layer.BiomeLayer;
import com.seedfinding.mcbiome.layer.land.MushroomLayer;
import com.seedfinding.mcbiome.layer.temperature.ClimateLayer;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mcfeature.misc.SpawnPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BiomeFilter {
    public long seed;
    private final OverworldBiomeSource source;
    public final BiomeLayer MUSHROOM_LAYER = getLayer(MushroomLayer.class);
    public final BiomeLayer SPECIAL_LAYER = getLayer(ClimateLayer.Special.class);
    public final BiomeLayer COLD_LAYER = getLayer(ClimateLayer.Cold.class);
    private static boolean hasBadlands;
    private static boolean hasGiantTree;
    private static boolean hasSnowy;
    private static boolean hasJungle;
    private static boolean hasMushroom;
    private static boolean hasBamboo;
    private static boolean hasOcean;
    public static List<BiomeCoords> mushroomTiles;
    public static List<BiomeCoords> specialTiles;
    public static List<BiomeCoords> snowyTiles;
    private final List<BiomeCoords> OCEAN_TILE = new ArrayList<>(List.of(new BiomeCoords(0, 0, 1024)));
    private final ArrayList<Biome> MUSHROOM_BIOMES = new ArrayList<>(Arrays.asList(Biomes.MUSHROOM_FIELDS,Biomes.MUSHROOM_FIELD_SHORE));
    private final ArrayList<Biome> JUNGLE_BIOMES = new ArrayList<>(Arrays.asList(Biomes.JUNGLE,Biomes.JUNGLE_HILLS,Biomes.JUNGLE_EDGE,Biomes.BAMBOO_JUNGLE,Biomes.BAMBOO_JUNGLE_HILLS));
    private final ArrayList<Biome> BAMBOO_BIOMES = new ArrayList<>(Arrays.asList(Biomes.BAMBOO_JUNGLE_HILLS, Biomes.BAMBOO_JUNGLE));
    private final ArrayList<Biome> GIANT_TREE_TAIGA_BIOMES = new ArrayList<>(Arrays.asList(Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS));
    private final ArrayList<Biome> SNOWY_BIOMES = new ArrayList<>(Arrays.asList(Biomes.SNOWY_TUNDRA,Biomes.SNOWY_MOUNTAINS,Biomes.SNOWY_TAIGA,Biomes.SNOWY_TAIGA_HILLS,Biomes.SNOWY_BEACH));
    private final ArrayList<Biome> OCEAN_BIOMES = new ArrayList<>(Arrays.asList(Biomes.OCEAN,Biomes.DEEP_OCEAN,Biomes.COLD_OCEAN,Biomes.DEEP_COLD_OCEAN,Biomes.FROZEN_OCEAN,Biomes.LUKEWARM_OCEAN,Biomes.DEEP_LUKEWARM_OCEAN,Biomes.WARM_OCEAN));


    /**
     * Creates a BiomeFilter object
     * @param seed the world seed to filter
     */
    public BiomeFilter(long seed) {
        this.seed = seed;
        hasGiantTree = false;
        hasBadlands = false;
        hasSnowy = false;
        hasMushroom = false;
        hasJungle = false;
        hasBamboo = false;
        source = new OverworldBiomeSource(Main.VERSION, seed);
    }

    /**
     * Filters a biome seed for biomes and a spawn point
     * @return true if the biomes and spawn are present, otherwise,
     *         false
     */
    public boolean filterBiomeSpawn() {
        return hasBiomes() && hasCloseSpawnPoint();
    }

    /**
     * Checks if the structures from the structure seed
     * actually spawn on this world seed
     * @return true if all structures can spawn, otherwise,
     *         false
     */
    public boolean hasStructures() {
        if (!OverworldFilter.pyramid.canSpawn(Storage.templeCoords.getX(), // Pyramid check
                Storage.templeCoords.getZ(), source))
            return false;
        return OverworldFilter.village.canSpawn(Storage.villageCoords.getX(), // Village check
                Storage.villageCoords.getZ(), source);
    }

    /**
     * Checks if the World Spawn Point of the seed is within 12 chunks of the
     * temple coordinates.
     * @return true if the spawn point is within the distance, otherwise,
     *         false
     */
    public boolean hasCloseSpawnPoint() {
        return Storage.templeCoords.distanceTo(SpawnPoint.getApproximateSpawn(source).toChunkPos(), DistanceMetric.EUCLIDEAN_SQ) <= 12.0D * 12.0D;
    }

    /**
     * Checks the overworld of the world seed, checking for
     * Mushroom Biomes, Giant Tree Biomes, Badlands Biomes, Bamboo Jungle, and Snowy Biomes
     * within a square with corners -3k,-3k, 3k,3k
     *
     * Does this by first checking if the tiles to have the aforementioned biomes exist,
     * and then checking those tiles to see if they actually have the biomes.
     *
     * @return true if all biomes are within the given range, otherwise,
     *         false
     */
    public boolean hasBiomes() {
        return hasSnowyTiles() && hasSpecialTiles() && hasMushroomTiles() && tilesHaveBiomes(); //Mushroom is by far the slowest tile check. Obv biome checking last though.
    }

    /**
     * Attempts to find the Cold Biome Tiles and stores them to
     * check for Snowy Biomes later.
     * @return true if it does, otherwise,
     *         false
     */
    public boolean hasSnowyTiles() { //Rarity could be different, this is just what 100% worked to my testing..
        snowyTiles = getTiles(COLD_LAYER, 3, 10);
        return !snowyTiles.isEmpty();
    }

    /**
     * Attempts to find Special Tiles and stores them to
     * check for Special Biomes later.
     * @return true if 3 or more Special Tiles are found, otherwise,
     *         false
     */
    public boolean hasSpecialTiles() {
        specialTiles = getTiles(SPECIAL_LAYER, 13, 10);
        return specialTiles.size() >= 3;
    }

    /**
     * Attempts to find Mushroom Biome Tiles and stores them to
     * check for Mushroom Biomes later.
     * @return true if it does, otherwise,
     *         false
     */
    public boolean hasMushroomTiles() {
        mushroomTiles = getTiles(MUSHROOM_LAYER, 100, 8);
        return !mushroomTiles.isEmpty();
    }

    /**
     * Finds where if a BiomeLayer can be found in an early stage of world generation
     * @param layer The biome layer to be found
     * @param rarity the rarity of the biome
     * @param scale the scale of the biome size
     * @return the list of regions where that biome is found
     */
    public List<BiomeCoords> getTiles(BiomeLayer layer, int rarity, int scale) {
        List<BiomeCoords> tiles = new ArrayList<>();
        for (int x = -3072 >> scale; x <= 3072 >> scale; x++) {
            for (int z = -3072 >> scale; z <= 3072 >> scale; z++) {
                long localSeed = getLocalSeed(layer, seed, x, z);
                if (Math.floorMod(localSeed >> 24, rarity) == 0) {
                    tiles.add(new BiomeCoords(x << scale, z << scale, 128));
                }
            }
        }
        return tiles;
    }

    /**
     * Checks the found tiles for Mushroom Biomes, Giant Tree Biomes, Badlands Biomes, Bamboo Jungle,
     * and Snowy Biomes within a square with corners -3k,-3k, 3k,3k
     * @return true if all biomes are within the given range, otherwise,
     *         false
     */
    public boolean tilesHaveBiomes() {
        checkTileCenters(snowyTiles);
        if (!hasSnowy) {
            return false;
        }
        checkTileCenters(mushroomTiles);
        if (!hasMushroom) {
            return false;
        }
        checkBiomes(OCEAN_TILE,256);
        if (!hasOcean) {
            return false;
        }
        checkBiomes(specialTiles, 64);
        return (hasGiantTree && hasBamboo && hasBadlands);
    }

    /**
     * Finds if the world has a rare biome within given boxes from
     * earlier stages of world generation
     * @param tiles The spaces from earlier stages of world generation
     *              that may contain the biome
     * @param increment The spacing of how often to check for the biome
     */
    public void checkBiomes(List<BiomeCoords> tiles, int increment) {
        checkBiome: for (BiomeCoords tile : tiles) {
            for (int x = tile.xMin; x <= tile.xMax; x += increment) {
                for (int z = tile.zMin; z <= tile.zMax; z += increment) {
                    Biome currentBiome = source.getBiome(x, 0, z);
                    if (!hasBadlands && hasOcean && currentBiome.equals(Biomes.BADLANDS_PLATEAU)) {
                        hasBadlands = true;
                        continue checkBiome;
                    }
                    else if (!hasGiantTree && hasOcean && GIANT_TREE_TAIGA_BIOMES.contains(currentBiome)) {
                        hasGiantTree = true;
                        continue checkBiome;
                    }
                    else if (!hasBamboo && hasOcean && BAMBOO_BIOMES.contains(currentBiome)) {
                        hasBamboo = true;
                        return;
                    }
                    else if (!hasJungle && hasOcean && JUNGLE_BIOMES.contains(currentBiome)) {
                        hasJungle = true;
                        List<BiomeCoords> bambooTile = new ArrayList<>(List.of(new BiomeCoords(tile.xCenter,tile.zCenter,1024)));
                        checkBiomes(bambooTile,256);
                        if (!hasBamboo) {
                            hasJungle = false;
                        }
                        continue checkBiome;
                    }
                    else if (!hasOcean && OCEAN_BIOMES.contains(currentBiome)) {
                        hasOcean = true;
                        return;
                    }
                }
            }
        }
    }

    /**
     * Iterates through the list of tiles and checks only the centers of
     * the tiles for the biomes.
     * Only checks for Mushroom and Snowy this way because of how they
     * generate.
     * @param tiles The list of tiles the biome could generate on.
     */
    public void checkTileCenters(List<BiomeCoords> tiles) {
        for (BiomeCoords tile : tiles) {
            Biome currentBiome = source.getBiome(tile.xCenter, 0, tile.zCenter);
            if (!hasSnowy && SNOWY_BIOMES.contains(currentBiome)) {
                hasSnowy = true;
                return;
            }
            else if (!hasMushroom && hasSnowy && MUSHROOM_BIOMES.contains(currentBiome)) {
                hasMushroom = true;
                return;
            }
        }
    }

    /**
     * Gets a biome layer to initialize the static biome layer variables
     * @param layerClass the class (world generation stage) where the biome layer is present
     * @return the BiomeLayer that is found on that world gen stage
     */
    public BiomeLayer getLayer(Class<? extends BiomeLayer> layerClass) {
        OverworldBiomeSource source = new OverworldBiomeSource(Main.VERSION, seed);
        for (int i = 0; i < source.getLayers().size(); i++) {
            if (source.getLayer(i).getClass().equals(layerClass)) return source.getLayer(i);
        }
        return source.voronoi;
    }

    /**
     * Gets the local seed for the given biome layer at that stage of generation
     * @param biomeLayer The biome layer to find the local seed of
     * @param seed the world seed
     * @param posX the x coordinate to get the local seed of
     * @param posZ the z coordinate to get the local seed of
     * @return the local seed of the biome layer at a given x and z
     */
    public long getLocalSeed(BiomeLayer biomeLayer, long seed, int posX, int posZ) {
        long layerSeed = BiomeLayer.getLayerSeed(seed, biomeLayer.salt);
        return BiomeLayer.getLocalSeed(layerSeed, posX, posZ);
    }

    /**
     * Storage sub-class for Biome Centers and Min/Max Coordinates
     * for the Bounding Box used in checking around the center for
     * biomes.
     */
    private static class BiomeCoords {

        public final int xCenter;
        public final int zCenter;
        public final int xMin;
        public final int zMin;
        public final int xMax;
        public final int zMax;

        /**
         *
         * @param xCenter The x coordinate of the Biome's center
         * @param zCenter The z coordinate of the Biome's center
         * @param length The side-length of the bounding box centered on
         *               the Biome's Center.
         */
        public BiomeCoords(int xCenter, int zCenter, int length) {
            this.xCenter = xCenter;
            this.zCenter = zCenter;
            this.xMax = xCenter + length/2;
            this.zMax = zCenter + length/2;
            this.xMin = xCenter - length/2;
            this.zMin = zCenter - length/2;
        }
    }
}