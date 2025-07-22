package com.pasithea0.betterthanrain;

import java.util.HashSet;
import java.util.Set;

public class BlockTypeMappings {
    public static final Set<Integer> METAL_BLOCKS = new HashSet<>();
    public static final Set<Integer> GLASS_BLOCKS = new HashSet<>();
    public static final Set<Integer> FABRIC_BLOCKS = new HashSet<>();
    public static final Set<Integer> FOLIAGE_BLOCKS = new HashSet<>();
    public static final Set<Integer> WATER_BLOCKS = new HashSet<>();
    public static final Set<Integer> LAVA_BLOCKS = new HashSet<>();
    public static final Set<Integer> NOTEBLOCK_BLOCKS = new HashSet<>();
    public static final Set<Integer> STONE_BLOCKS = new HashSet<>();
    public static final Set<Integer> WOOD_BLOCKS = new HashSet<>();
    public static final Set<Integer> PLASTIC_BLOCKS = new HashSet<>();
    public static final Set<Integer> METAL_BLOCKS_THIN = new HashSet<>();
    public static final Set<Integer> FABRIC_BLOCKS_THIN = new HashSet<>();

    static {
        METAL_BLOCKS.add(431); // Block of Iron
        METAL_BLOCKS.add(432); // Block of Gold
        METAL_BLOCKS.add(437); // Block of Steel

        GLASS_BLOCKS.add(730); // Ice
        GLASS_BLOCKS.add(190); // Glass
        GLASS_BLOCKS.add(191); // Tinted Glass
        GLASS_BLOCKS.add(192); // Reinforced Glass

        FABRIC_BLOCKS.add(110); // Wool
        FABRIC_BLOCKS.add(850); // Lamp idle
        FABRIC_BLOCKS.add(851); // Lamp active
        FABRIC_BLOCKS.add(852); // Lamp inverted idle
        FABRIC_BLOCKS.add(853); // Lamp inverted active
        FABRIC_BLOCKS.add(910); // Paper Wall

        FOLIAGE_BLOCKS.add(290); // Oak Leaves
        FOLIAGE_BLOCKS.add(291); // Retro Leaves
        FOLIAGE_BLOCKS.add(292); // Pine Leaves
        FOLIAGE_BLOCKS.add(293); // Birch Leaves
        FOLIAGE_BLOCKS.add(294); // Cherry Leaves
        FOLIAGE_BLOCKS.add(295); // Eucalyptus Leaves
        FOLIAGE_BLOCKS.add(296); // Shrub Leaves
        FOLIAGE_BLOCKS.add(297); // Flowering Cherry Leaves
        FOLIAGE_BLOCKS.add(298); // Cacao Leaves
        FOLIAGE_BLOCKS.add(299); // Thorn Leaves
        FOLIAGE_BLOCKS.add(300); // Palm Leaves

        WATER_BLOCKS.add(270); // Flowing Water
        WATER_BLOCKS.add(271); // Still Water

        LAVA_BLOCKS.add(272); // Flowing Lava
        LAVA_BLOCKS.add(273); // Still Lava
        LAVA_BLOCKS.add(801); // Igneous Cobbled Netherrack
        LAVA_BLOCKS.add(420); // Nether Coal Ore
        LAVA_BLOCKS.add(436); // Block of Nether Coal
        LAVA_BLOCKS.add(233); // Molten Pumice

        NOTEBLOCK_BLOCKS.add(530); // Noteblock

		WOOD_BLOCKS.add(50); // Wooden Plank
		WOOD_BLOCKS.add(51); // Painted Wood
		WOOD_BLOCKS.add(140); // Wooden Slab
		WOOD_BLOCKS.add(160); // Wooden Stairs
		WOOD_BLOCKS.add(280); // Oak Log
		WOOD_BLOCKS.add(281); // Pine Log
		WOOD_BLOCKS.add(282); // Birch Log
		WOOD_BLOCKS.add(283); // Cherry Log
		WOOD_BLOCKS.add(284); // Eucalyptus Log
		WOOD_BLOCKS.add(285); // Mossy Oak Log
		WOOD_BLOCKS.add(286); // Thorn Log
		WOOD_BLOCKS.add(287); // Palm Log

        STONE_BLOCKS.add(10); // Cobblestone
		STONE_BLOCKS.add(11); // Mossy Cobblestone
        STONE_BLOCKS.add(12); // Cobbled Basalt
        STONE_BLOCKS.add(13); // Cobbled Limestone
        STONE_BLOCKS.add(14); // Cobbled Granite

        // PLASTIC_BLOCKS
		// METAL_BLOCKS_THIN
		// FABRIC_BLOCKS_THIN
    }

    public enum MaterialType {
        METAL,
        GLASS,
        FABRIC,
        FOLIAGE,
        WATER,
        LAVA,
        NOTEBLOCK,
        STONE,
        WOOD,
        PLASTIC,
        OTHER
    }

    public static MaterialType getMaterialType(int blockId) {
        if (METAL_BLOCKS.contains(blockId) || METAL_BLOCKS_THIN.contains(blockId)) return MaterialType.METAL;
        if (GLASS_BLOCKS.contains(blockId)) return MaterialType.GLASS;
        if (FABRIC_BLOCKS.contains(blockId) || FABRIC_BLOCKS_THIN.contains(blockId)) return MaterialType.FABRIC;
        if (FOLIAGE_BLOCKS.contains(blockId)) return MaterialType.FOLIAGE;
        if (WATER_BLOCKS.contains(blockId)) return MaterialType.WATER;
        if (LAVA_BLOCKS.contains(blockId)) return MaterialType.LAVA;
        if (NOTEBLOCK_BLOCKS.contains(blockId)) return MaterialType.NOTEBLOCK;
        if (STONE_BLOCKS.contains(blockId)) return MaterialType.STONE;
        if (WOOD_BLOCKS.contains(blockId)) return MaterialType.WOOD;
        if (PLASTIC_BLOCKS.contains(blockId)) return MaterialType.PLASTIC;
        return MaterialType.OTHER;
    }
}
