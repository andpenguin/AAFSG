package and_penguin.filters;

import and_penguin.Main;
import and_penguin.Storage;

import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mcfeature.loot.item.Item;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcfeature.loot.item.Items;
import com.seedfinding.mcfeature.loot.ChestContent;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.Village;
import com.seedfinding.mcfeature.structure.generator.structure.DesertPyramidGenerator;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcterrain.terrain.OverworldTerrainGenerator;


import java.util.List;

public class OverworldFilter {
    public static final double TEMPLE_MAX_DIST = 24.0D * 24.0D;
    public static final double VILLAGE_MAX_DIST = 14.0D * 14.0D;
    private final long seed;
    private final ChunkRand rand;
    public static DesertPyramid pyramid = new DesertPyramid(Main.VERSION);
    public static Village village = new Village(Main.VERSION);

    /**
     * Creates an OverworldFilter object with a given structure seed and random value
     * @param seed a structure seed to be filtered
     * @param rand a ChunkRand to check with
     */
    public OverworldFilter(long seed, ChunkRand rand) {
        this.seed = seed;
        this.rand = rand;
    }

    /**
     * Filters the overworld of the structure seed, checking for
     * A temple within TEMPLE_MAX_DIST of 0,0
     * and a village within VILLAGE_MAX_DIST of the temple
     * @return true if all structures are within the given range, otherwise,
     *         false
     */
    public boolean filterOverworld() {
        Storage.templeCoords = null;
        Storage.villageCoords = null;
        CPos[] templeLocs = new CPos[4];
        CPos[] villageLocs = new CPos[4];
        for (int x = -1; x < 1; x++) { // loop through quadrants looking for a structures
            for (int z = -1; z < 1; z++) {
                if (pyramid.getInRegion(seed, x, z, rand).getMagnitudeSq() <= TEMPLE_MAX_DIST) {
                    templeLocs[(x+1) + (z+1)*2] = pyramid.getInRegion(seed, x, z, rand);
                }
                villageLocs[(x+1) + (z+1)*2] = village.getInRegion(seed, x, z, rand);
            }
        }
        overworldLoop: for (CPos villageLoc: villageLocs) {
            if (villageLoc == null) continue;
            for (CPos templeLoc : templeLocs) {
                if (templeLoc == null) continue;
                if (templeLoc.distanceTo(villageLoc, DistanceMetric.EUCLIDEAN_SQ)
                        <= VILLAGE_MAX_DIST) {
                    Storage.templeCoords = templeLoc; // save the coords
                    Storage.villageCoords = villageLoc;
                    break overworldLoop;
                }
            }
        }
        if (Storage.villageCoords == null || Storage.templeCoords == null) {
            return false;
        }
        return hasGunpowder();
    }

    /**
     * Checks if the temple has more than 12 gunpowder.
     * @return true if the temple has more than 12 gunpowder, otherwise,
     *         false
     *
     */
    public boolean hasGunpowder() {
        OverworldBiomeSource source = new OverworldBiomeSource(Main.VERSION, seed);
        DesertPyramidGenerator gen = new DesertPyramidGenerator(Main.VERSION);
        int gunpowderCount = 0;
        gen.generate(new OverworldTerrainGenerator(source), Storage.templeCoords);
        List<ChestContent> loot = OverworldFilter.pyramid.getLoot(seed, gen,false);
        for (ChestContent chest: loot) {
            for (ItemStack stack: chest.getItems()) {
                Item item = stack.getItem();
                if (item.equals(Items.GUNPOWDER))
                    gunpowderCount += stack.getCount();
            }
        }
        return gunpowderCount >= 12;
    }
}