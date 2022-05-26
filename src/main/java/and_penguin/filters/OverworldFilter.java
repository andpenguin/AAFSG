package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.loot.ChestContent;
import kaptainwutax.featureutils.loot.item.Item;
import kaptainwutax.featureutils.loot.item.ItemStack;
import kaptainwutax.featureutils.loot.item.Items;
import kaptainwutax.featureutils.structure.DesertPyramid;
import kaptainwutax.featureutils.structure.Village;
import kaptainwutax.featureutils.structure.generator.structure.DesertPyramidGenerator;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.terrainutils.terrain.OverworldTerrainGenerator;

import java.util.List;

public class OverworldFilter {
    public static final double MAX_DIST = 16.0D * 16.0D;
    private final long seed;
    private final ChunkRand rand;
    public static DesertPyramid pyramid = new DesertPyramid(Main.VERSION);
    public static Village village = new Village(Main.VERSION);

    /**
     * Creates an OverworldFilter object with a given structureseed and random value
     * @param seed a structureseed to be filtered
     * @param rand a ChunkRand to check with
     */
    public OverworldFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
    }

    /**
     * Filters the overworld of the structureseed, checking for a
     * A temple within 0,0 quadrant
     * and an village within 16 chunks
     * @return true if all structures are within the given range, otherwise,
     *         false
     */
    public boolean filterOverworld() {
        CPos templeLoc = pyramid.getInRegion(seed, 0, 0, rand); // get the temple in the region
        if (templeLoc != null && templeLoc.distanceTo( // check the distance to 0,0
                new BPos(0,0,0), DistanceMetric.EUCLIDEAN_SQ) <= MAX_DIST) {
            Storage.templeCoords = templeLoc;
        }
        else return false;
        for (int x = -1; x < 1; x++) { // loop through quadrants
            for (int z = -1; z < 1; z++) {
                CPos villageLoc = village.getInRegion(seed, x, z, rand); // get the village in the region
                if (villageLoc != null && villageLoc.distanceTo( // check the distance to 0,0
                        new BPos(0,0,0), DistanceMetric.EUCLIDEAN_SQ) <= MAX_DIST) {
                    Storage.villageCoords = villageLoc;
                    return isWicked();
                }
            }
        }
        return false;
    }

    /**
     * Finds how wicked the chest loot of the desert temple is
     * @return true if the temple has wicked loot, otherwise,
     *         false
     */
    public boolean isWicked() {
        OverworldBiomeSource source = new OverworldBiomeSource(Main.VERSION, seed);
        DesertPyramidGenerator gen = new DesertPyramidGenerator(Main.VERSION);
        boolean highTier = false;
        boolean lowTier = false;
        int gunpowderCount = 0;
        gen.generate(new OverworldTerrainGenerator(source), Storage.templeCoords);
        List<ChestContent> loot = OverworldFilter.pyramid.getLoot(seed, gen,false);
        for (ChestContent chest: loot) {
            if (chest.contains(Items.ENCHANTED_GOLDEN_APPLE)) {
                highTier = true;
            }
            for (ItemStack stack: chest.getItems()) {
                Item item = stack.getItem();
                if (item.equals(Items.GUNPOWDER))
                    gunpowderCount += stack.getCount();
                if (item.getName().equals("enchanted_book")) {
                    String name = item.getEnchantments().get(0).getFirst();
                    int level = item.getEnchantments().get(0).getSecond();
                    if (name.equals("mending"))
                        highTier = true;
                    else if (name.equals("unbreaking") && level == 3)
                        highTier = true;
                    else if (name.equals("channelling"))
                        highTier = true;
                    else if (name.equals("looting") && level >= 2)
                        highTier = true;
                    else if (name.equals("depth_strider") && level == 3)
                        lowTier = true;
                    else if (name.equals("aqua_affinity"))
                        lowTier = true;
                    else if (name.equals("respiration") && level == 3)
                        lowTier = true;
                    else if (name.equals("piercing") && level == 4)
                        lowTier = true;
                    else if (name.equals("unbreaking") && level == 2)
                        lowTier = true;
                    else if (name.equals("silk_touch"))
                        lowTier = true;
                }
            }
        }
        return (highTier && gunpowderCount >= 5) || (lowTier && gunpowderCount >= 10);
    }
}
