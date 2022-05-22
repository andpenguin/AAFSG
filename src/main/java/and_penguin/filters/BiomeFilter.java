package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.biomeutils.layer.land.BambooJungleLayer;
import kaptainwutax.biomeutils.layer.land.MushroomLayer;
import kaptainwutax.biomeutils.layer.temperature.ClimateLayer;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.loot.ChestContent;
import kaptainwutax.featureutils.loot.item.Item;
import kaptainwutax.featureutils.loot.item.ItemStack;
import kaptainwutax.featureutils.loot.item.Items;
import kaptainwutax.featureutils.misc.SpawnPoint;
import kaptainwutax.featureutils.structure.generator.structure.DesertPyramidGenerator;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.block.BlockBox;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.terrainutils.terrain.OverworldTerrainGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BiomeFilter {
    private final long seed;
    public final BiomeLayer MUSHROOM = getLayer(MushroomLayer.class);
    public final BiomeLayer BAMBOO_JUNGLE = getLayer(BambooJungleLayer.class);
    public final BiomeLayer SPECIAL = getLayer(ClimateLayer.Special.class);
    private static boolean hasBadlands;
    private static boolean hasGiantTree;
    private static boolean hasSnowy;
    public static List<BlockBox> mushroomCoords;
    public static List<BlockBox> bambooCoords;
    public static List<BlockBox> specialCoords;


    /**
     * Creates a BiomeFilter object
     * @param seed the world seed to filter
     */
    public BiomeFilter(long seed) {
        this.seed = seed;
        hasSnowy = false;
        hasGiantTree = false;
        hasBadlands = false;
    }

    /**
     * Returns wheter the seed has biome shortcuts, structures spawning,
     * and the biomes at the end of world gen
     * @return true if shortcuts, strucutres, and biomes are found, otherwise,
     *         false
     */
    public boolean filterBiomeSeed() {
            return hasShortcuts() && hasStructures() && hasBiomes();
    }

    /**
     * Returns if the world seed has the biome shortcuts for every biome
     * @return true if all shortcuts are found, otherwise,
     *         false
     */
    public boolean hasShortcuts() {
        mushroomCoords = hasShortcut(MUSHROOM, 100, 8);
        if (mushroomCoords == null) return false;
        bambooCoords = hasShortcut(BAMBOO_JUNGLE, 10, 8);
        if (bambooCoords == null) return false;
        specialCoords = hasShortcut(SPECIAL, 13, 10);
        if (specialCoords.size() >= 3) {
            Main.specialCount++;
            return true;
        }
        return false;
    }
    /**
     * Gets a biome layer to initialize the static biome layer variables
     * @param layerClass the class (world generation stage) where the biomelayer is present
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
     * @return the local seed of the biomelayer at a given x and z
     */
    public long getLocalSeed(BiomeLayer biomeLayer, long seed, int posX, int posZ) {
        long layerSeed = BiomeLayer.getLayerSeed(seed, biomeLayer.salt);
        return BiomeLayer.getLocalSeed(layerSeed, posX, posZ);
    }

    /**
     * Finds where if a BiomeLayer can be found in an early stage of world generation
     * @param layer The biome layer to be found
     * @param nextInt the rarity of the biome
     * @param scale the scale of the biome size
     * @return the list of regions where that biome is found
     * (will only be one box if the layer is mushroom or bamboo)
     */
    public List<BlockBox> hasShortcut(BiomeLayer layer, int nextInt, int scale) {
        List<BlockBox> boxes = new ArrayList<>();
        for (int x = -3000 >> scale; x < 3000 >> scale; x++) {
            for (int z = -3000 >> scale; z < 3000 >> scale; z++) {
                long localSeed = getLocalSeed(layer, seed, x, z);
                if (Math.floorMod(localSeed >> 24, nextInt) == 0) {
                    boxes.add(new BlockBox(x << scale, z << scale, (x + 1) << scale, (z + 1) << scale));
                }
            }
        }
        return boxes;
    }

    /**
     * Checks if the structures from the structureseed
     * actually spawn on this worldseed
     * @return true if all structures can spawn, otherwise
     *         false
     */
    public boolean hasStructures() {
        OverworldBiomeSource source = new OverworldBiomeSource(Main.VERSION, seed);
        Main.templeTotal++;
        if (!OverworldFilter.pyramid.canSpawn(Storage.templeCoords.getX(), // Pyramid check
                Storage.templeCoords.getZ(), source))
            return false;
        Main.templeCount++;
        Main.villageTotal++;
        if (!OverworldFilter.village.canSpawn(Storage.villageCoords.getX(), // Village check
                Storage.villageCoords.getZ(), source)) {
            return false;
        }
        Main.villageCount++;
        return true;
    }

    /**
     * Filters the overworld of the worldseed, checking for a
     * Mushroom Biomes, Giant Tree Biomes, Badlands Biomes, Bamboo Jungle, and Snowy Biome
     * within a square with corners -3k,-3k 3k,3k
     * @return true if all biomes are within the given range, otherwise,
     *         false
     */
    public boolean hasBiomes() {
        Biome[] mushroomBiomes = new Biome[] {Biomes.MUSHROOM_FIELDS, Biomes.MUSHROOM_FIELD_SHORE};
        Biome[] jungleBiomes = new Biome[] {Biomes.BAMBOO_JUNGLE_HILLS};
        Biome[] specialBiomes = new Biome[] {Biomes.BADLANDS_PLATEAU, Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.SNOWY_TAIGA,
        Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TAIGA_HILLS, Biomes.SNOWY_MOUNTAINS};
        return  hasBiome(MUSHROOM, mushroomCoords, 50, mushroomBiomes) &&
                hasBiome(BAMBOO_JUNGLE, bambooCoords, 25, jungleBiomes) &&
                hasBiome(SPECIAL, specialCoords, 50, specialBiomes);
    }

    /**
     * Finds if the world has a rare biome within given boxes from
     * earlier stages of world generation
     * @param layer The BiomeLayer that needs to be found
     * @param boxes The spaces from earlier stages of world generation
     *              that may contain the biome
     * @param increment The spacing of how often to check for the biome
     * @param biomes The valid matching biomes for the BiomeLayer
     * @return true if the world contains the biome within any of the boxes, otherwise,
     *         false
     */
    public boolean hasBiome(BiomeLayer layer, List<BlockBox> boxes, int increment, Biome[] biomes) {
        OverworldBiomeSource source = new OverworldBiomeSource(Main.VERSION, seed);
            outer: for (BlockBox box : boxes) {
                for (int x = box.minX; x < box.maxX; x += increment) {
                    for (int z = box.minZ; z < box.maxZ; z += increment) {
                        boolean newBiome = findNewBiome(source, layer, x, z, biomes);
                        if (layer == MUSHROOM && newBiome)
                            return true;
                        else if (layer == BAMBOO_JUNGLE && newBiome)
                            return true;
                        else if (layer == SPECIAL && newBiome) {
                            if (hasBadlands && hasGiantTree && hasSnowy)
                                return true;
                            else
                                continue outer;
                        }
                    }
                }
            }
        return false;
    }

    /**
     * Finds if a matching biome can be found at a given x and z location
     * @param source the source of the Overworld of the worldseed
     * @param layer the type of biome being searched for
     * @param x the x coordinate to look for a biome at
     * @param z the z coordinate to look for a biome at
     * @param biomes the array of valid biomes to be looked for
     * @return true if the biome matches one of the biomes in the array, otherwise,
     *         false
     */
    public boolean findNewBiome(OverworldBiomeSource source, BiomeLayer layer, int x, int z, Biome[] biomes) {
        Biome b = source.getBiome(x, 0, z);
        for (Biome biome : biomes) {
            if (b.equals(biome)) {
                if (layer == MUSHROOM) {
                    return true;
                }
                else if (layer == BAMBOO_JUNGLE) {
                    return true;
                }
                else {
                    if (!hasBadlands && (b.equals(Biomes.BADLANDS_PLATEAU))) {
                        hasBadlands = true;
                        return true;
                    } else if (!hasGiantTree && (b.equals(Biomes.GIANT_TREE_TAIGA) || b.equals(Biomes.GIANT_TREE_TAIGA_HILLS))) {
                        hasGiantTree = true;
                        return true;
                    } else if (!hasSnowy && (b.equals(Biomes.SNOWY_TUNDRA) || b.equals(Biomes.SNOWY_TAIGA) ||
                            b.equals(Biomes.SNOWY_TAIGA_HILLS) || b.equals(Biomes.SNOWY_MOUNTAINS))) {
                        hasSnowy = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
