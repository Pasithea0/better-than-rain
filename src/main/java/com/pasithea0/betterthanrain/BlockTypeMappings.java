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

        // STONE_BLOCKS
        // WOOD_BLOCKS
        // PLASTIC_BLOCKS
    }
}
