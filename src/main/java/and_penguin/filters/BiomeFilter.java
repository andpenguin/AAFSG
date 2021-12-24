package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.biomeutils.layer.land.BambooJungleLayer;
import kaptainwutax.biomeutils.layer.land.MushroomLayer;
import kaptainwutax.biomeutils.layer.temperature.ClimateLayer;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.DesertPyramid;
import kaptainwutax.featureutils.structure.Monument;
import kaptainwutax.featureutils.structure.PillagerOutpost;
import kaptainwutax.featureutils.structure.RuinedPortal;
import kaptainwutax.featureutils.structure.generator.Generator;
import kaptainwutax.featureutils.structure.generator.structure.RuinedPortalGenerator;
import kaptainwutax.mcutils.block.Block;
import kaptainwutax.mcutils.block.Blocks;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.block.BlockBox;
import kaptainwutax.mcutils.util.data.Pair;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.terrainutils.TerrainGenerator;
import kaptainwutax.terrainutils.terrain.OverworldTerrainGenerator;

import java.util.ArrayList;
import java.util.List;

public class BiomeFilter {
    private static long seed;
    private static ChunkRand rand;
    public final BiomeLayer MUSHROOM = getLayer(MushroomLayer.class);
    public final BiomeLayer BAMBOO_JUNGLE = getLayer(BambooJungleLayer.class);
    public final BiomeLayer SPECIAL = getLayer(ClimateLayer.Special.class);
    private static boolean hasBadlands;
    private static boolean hasGiantTree;
    private static boolean hasSnowy;
    public static List<BlockBox> mushroomCoords;
    public static List<BlockBox> bambooCoords;
    public static List<BlockBox> specialCoords;

    public BiomeFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
        hasSnowy = false;
        hasGiantTree = false;
        hasBadlands = false;
    }

    public boolean filterBiomeSeed() {
        return hasShortcuts() && hasStructures() && hasBiomes();
    }

    public boolean hasShortcuts() {
        mushroomCoords = hasShortcut(MUSHROOM, 100, 8);
        if (mushroomCoords == null) return false;
        bambooCoords = hasShortcut(BAMBOO_JUNGLE, 10, 8);
        if (bambooCoords == null) return false;
        specialCoords = hasShortcut(SPECIAL, 13, 10);
        if (specialCoords.size() < 3) return false;
        return true;
    }

    public BiomeLayer getLayer(Class<? extends BiomeLayer> layerClass) {
        OverworldBiomeSource source = new OverworldBiomeSource(Main.VERSION, seed);
        for (int i = 0; i < source.getLayers().size(); i++) {
            if (source.getLayer(i).getClass().equals(layerClass)) return source.getLayer(i);
        }
        return source.voronoi;
    }

    public long getLocalSeed(BiomeLayer biomeLayer, long seed, int posX, int posZ) {
        long layerSeed = BiomeLayer.getLayerSeed(seed, biomeLayer.salt);
        return BiomeLayer.getLocalSeed(layerSeed, posX, posZ);
    }

    public List<BlockBox> hasShortcut(BiomeLayer layer, int nextInt, int scale) {
        List<BlockBox> boxes = new ArrayList<>();
        for (int x = -3000 >> scale; x < 3000 >> scale; x++) {
            for (int z = -3000 >> scale; z < 3000 >> scale; z++) {
                long localSeed = getLocalSeed(layer, seed, x, z);
                if (Math.floorMod(localSeed >> 24, nextInt) == 0) {
                    if (layer == MUSHROOM || layer == BAMBOO_JUNGLE) {
                        boxes.add(new BlockBox(x << scale, z << scale, (x + 1) >> scale, (z + 1) << scale));
                        return boxes;
                    }
                    if (layer == SPECIAL) {
                        boxes.add(new BlockBox(x << scale, z << scale, (x + 1) << scale, (z + 1) << scale));
                    }
                }
            }
        }
        return boxes;
    }

    public boolean hasStructures() {
        OverworldBiomeSource source = new OverworldBiomeSource(Main.VERSION, seed);
        if (!OverworldFilter.pyramid.canSpawn(Storage.templeCoords.getX(),
                Storage.templeCoords.getZ(), source))
            return false;
        if (!OverworldFilter.outpost.canSpawn(Storage.outpostCoords.getX(),
                Storage.outpostCoords.getZ(), source))
            return false;
        if (OverworldFilter.ruinedPortal.canSpawn(Storage.ruinedPortalCoords.getX(),
                Storage.ruinedPortalCoords.getZ(), source)) {
            RuinedPortalGenerator gen = new RuinedPortalGenerator(Main.VERSION);
            gen.generate(seed, Dimension.OVERWORLD, Storage.ruinedPortalCoords.getX(),
                    Storage.ruinedPortalCoords.getZ());
            List<Pair<Block, BPos>> portal = gen.getMinimalPortal();
            int obiCount = 0;
            for (Pair pair : portal) {
                if (pair.getFirst() == Blocks.CRYING_OBSIDIAN)
                    return false;
                else if (pair.getFirst() == Blocks.OBSIDIAN)
                    obiCount++;
            }
            if (obiCount >= 7) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBiomes() {
        Biome[] mushroomBiomes = new Biome[] {Biomes.MUSHROOM_FIELDS, Biomes.MUSHROOM_FIELD_SHORE};
        Biome[] jungleBiomes = new Biome[] {Biomes.BAMBOO_JUNGLE, Biomes.BAMBOO_JUNGLE_HILLS};
        Biome[] specialBiomes = new Biome[] {Biomes.BADLANDS, Biomes.BADLANDS_PLATEAU,
        Biomes.WOODED_BADLANDS_PLATEAU, Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.SNOWY_TAIGA,
        Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TAIGA_HILLS};
        return hasBiome(MUSHROOM, mushroomCoords, 100, mushroomBiomes) &&
                hasBiome(BAMBOO_JUNGLE, bambooCoords, 100, jungleBiomes) &&
                hasBiome(SPECIAL, specialCoords, 100, specialBiomes);
    }

    public boolean hasBiome(BiomeLayer layer, List<BlockBox> boxes, int increment, Biome[] biomes) {
        OverworldBiomeSource source = new OverworldBiomeSource(Main.VERSION, seed);
        for (BlockBox box : boxes) {
            for (int x = box.minX; x < box.maxX; x += increment) {
                for (int z = box.minZ; z < box.maxZ; z+= increment) {
                    Biome b = source.getBiome(x, 0, z);
                    for (Biome biome : biomes) {
                        if (b.equals(biome)) {
                            System.out.println("Seed: " + seed + " Biome: " + b.getName() + " " + x + " " + z);
                            if (layer == MUSHROOM || layer == BAMBOO_JUNGLE)
                                return true;
                            if (layer == SPECIAL) {
                                if (!hasBadlands && (b.equals(Biomes.BADLANDS) || b.equals(Biomes.BADLANDS_PLATEAU) ||
                                        b.equals(Biomes.WOODED_BADLANDS_PLATEAU))) {
                                    hasBadlands = true;
                                } else if (!hasGiantTree && (b.equals(Biomes.GIANT_TREE_TAIGA) || b.equals(Biomes.GIANT_TREE_TAIGA_HILLS))) {
                                    hasGiantTree = true;
                                } else if (!hasSnowy && (b.equals(Biomes.SNOWY_TUNDRA) || b.equals(Biomes.SNOWY_TAIGA) ||
                                        b.equals(Biomes.SNOWY_TAIGA_HILLS))) {
                                    hasSnowy = true;
                                }
                                if (hasBadlands && hasSnowy && hasGiantTree) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
