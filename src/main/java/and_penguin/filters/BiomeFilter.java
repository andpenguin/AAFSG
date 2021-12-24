package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.biomeutils.layer.land.BambooJungleLayer;
import kaptainwutax.biomeutils.layer.land.MushroomLayer;
import kaptainwutax.biomeutils.layer.temperature.ClimateLayer;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.generator.structure.RuinedPortalGenerator;
import kaptainwutax.mcutils.block.Block;
import kaptainwutax.mcutils.block.Blocks;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.block.BlockBox;
import kaptainwutax.mcutils.util.data.Pair;
import kaptainwutax.mcutils.util.pos.BPos;

import java.util.ArrayList;
import java.util.List;

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

    public BiomeFilter(long seed) {
        this.seed = seed;
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
        return specialCoords.size() >= 3;
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
                    boxes.add(new BlockBox(x << scale, z << scale, (x + 1) << scale, (z + 1) << scale));
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
            for (Pair<Block, BPos> pair : portal) {
                if (pair.getFirst() == Blocks.CRYING_OBSIDIAN)
                    return false;
                else if (pair.getFirst() == Blocks.OBSIDIAN)
                    obiCount++;
            }
            return obiCount >= 7;
        }
        return false;
    }

    public boolean hasBiomes() {
        Biome[] mushroomBiomes = new Biome[] {Biomes.MUSHROOM_FIELDS, Biomes.MUSHROOM_FIELD_SHORE};
        Biome[] jungleBiomes = new Biome[] {Biomes.BAMBOO_JUNGLE, Biomes.BAMBOO_JUNGLE_HILLS};
        Biome[] specialBiomes = new Biome[] {Biomes.BADLANDS, Biomes.BADLANDS_PLATEAU,
        Biomes.WOODED_BADLANDS_PLATEAU, Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.SNOWY_TAIGA,
        Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TAIGA_HILLS};
        return hasBiome(MUSHROOM, mushroomCoords, 256, mushroomBiomes) &&
                hasBiome(BAMBOO_JUNGLE, bambooCoords, 50, jungleBiomes) &&
                hasBiome(SPECIAL, specialCoords, 100, specialBiomes);
    }

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
                    if (!hasBadlands && (b.equals(Biomes.BADLANDS) || b.equals(Biomes.BADLANDS_PLATEAU) ||
                            b.equals(Biomes.WOODED_BADLANDS_PLATEAU))) {
                        hasBadlands = true;
                        return true;
                    } else if (!hasGiantTree && (b.equals(Biomes.GIANT_TREE_TAIGA) || b.equals(Biomes.GIANT_TREE_TAIGA_HILLS))) {
                        hasGiantTree = true;
                        return true;
                    } else if (!hasSnowy && (b.equals(Biomes.SNOWY_TUNDRA) || b.equals(Biomes.SNOWY_TAIGA) ||
                            b.equals(Biomes.SNOWY_TAIGA_HILLS))) {
                        hasSnowy = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
