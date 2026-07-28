package com.xevaryth.mysticism.magicarmory.registry;

import com.xevaryth.mysticism.magicarmory.MagicArmory;
import com.xevaryth.mysticism.magicarmory.block.AcrylGlassBlock;
import com.xevaryth.mysticism.magicarmory.block.AcrylGlassPaneBlock;
import com.xevaryth.mysticism.magicarmory.block.AcrylGlassSlabBlock;
import com.xevaryth.mysticism.magicarmory.block.AcrylGlassStairBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MagicArmoryBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MagicArmory.MOD_ID);
    private static final List<Entry> ENTRIES = new ArrayList<>();
    public static final OpaqueFamily ACRYL = registerOpaqueFamily(null);
    public static final GlassFamily ACRYL_GLASS = registerGlassFamily(null);
    public static final Map<DyeColor, OpaqueFamily> COLORED_ACRYL = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, GlassFamily> COLORED_ACRYL_GLASS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            COLORED_ACRYL.put(color, registerOpaqueFamily(color));
            COLORED_ACRYL_GLASS.put(color, registerGlassFamily(color));
        }
    }

    private MagicArmoryBlocks() {}
    public static void register(IEventBus modBus) { BLOCKS.register(modBus); }
    public static List<Entry> entries() { return Collections.unmodifiableList(ENTRIES); }

    private static OpaqueFamily registerOpaqueFamily(DyeColor color) {
        String prefix = color == null ? "" : color.getName() + "_";
        String baseId = prefix + "acryl_block";
        String bricksId = prefix + "acryl_bricks";
        String chiseledId = "chiseled_" + prefix + "acryl_block";
        String pillarId = prefix + "acryl_pillar";
        String smoothId = "smooth_" + prefix + "acryl_block";
        String stairsId = prefix + "acryl_stairs";
        String slabId = prefix + "acryl_slab";
        String wallId = prefix + "acryl_wall";

        DeferredBlock<Block> base = simpleOpaque(baseId, color, Shape.BLOCK);
        DeferredBlock<Block> bricks = simpleOpaque(bricksId, color, Shape.BRICKS);
        DeferredBlock<Block> chiseled = simpleOpaque(chiseledId, color, Shape.CHISELED);
        DeferredBlock<RotatedPillarBlock> pillar = BLOCKS.registerBlock(
            pillarId, RotatedPillarBlock::new, opaqueProperties(color));
        add(pillarId, false, color, Shape.PILLAR, pillar);
        DeferredBlock<Block> smooth = simpleOpaque(smoothId, color, Shape.SMOOTH);
        DeferredBlock<StairBlock> stairs = BLOCKS.registerBlock(
            stairsId, p -> new StairBlock(base.get().defaultBlockState(), p), opaqueProperties(color));
        add(stairsId, false, color, Shape.STAIRS, stairs);
        DeferredBlock<SlabBlock> slab = BLOCKS.registerBlock(slabId, SlabBlock::new, opaqueProperties(color));
        add(slabId, false, color, Shape.SLAB, slab);
        DeferredBlock<WallBlock> wall = BLOCKS.registerBlock(wallId, WallBlock::new, opaqueProperties(color));
        add(wallId, false, color, Shape.WALL, wall);
        return new OpaqueFamily(base, bricks, chiseled, pillar, smooth, stairs, slab, wall);
    }

    private static GlassFamily registerGlassFamily(DyeColor color) {
        String prefix = color == null ? "" : color.getName() + "_";
        String baseId = prefix + "acryl_glass";
        String bricksId = prefix + "acryl_glass_bricks";
        String chiseledId = "chiseled_" + prefix + "acryl_glass";
        String smoothId = "smooth_" + prefix + "acryl_glass";
        String paneId = prefix + "acryl_glass_pane";
        String brickPaneId = prefix + "acryl_glass_brick_pane";
        String chiseledPaneId = "chiseled_" + prefix + "acryl_glass_pane";
        String smoothPaneId = "smooth_" + prefix + "acryl_glass_pane";
        String stairsId = prefix + "acryl_glass_stairs";
        String slabId = prefix + "acryl_glass_slab";

        DeferredBlock<AcrylGlassBlock> base = glassBlock(baseId, color, Shape.BLOCK);
        DeferredBlock<AcrylGlassBlock> bricks = glassBlock(bricksId, color, Shape.BRICKS);
        DeferredBlock<AcrylGlassBlock> chiseled = glassBlock(chiseledId, color, Shape.CHISELED);
        DeferredBlock<AcrylGlassBlock> smooth = glassBlock(smoothId, color, Shape.SMOOTH_GLASS);
        DeferredBlock<AcrylGlassPaneBlock> pane = glassPane(paneId, color, Shape.PANE);
        DeferredBlock<AcrylGlassPaneBlock> brickPane = glassPane(brickPaneId, color, Shape.BRICK_PANE);
        DeferredBlock<AcrylGlassPaneBlock> chiseledPane = glassPane(chiseledPaneId, color, Shape.CHISELED_PANE);
        DeferredBlock<AcrylGlassPaneBlock> smoothPane = glassPane(smoothPaneId, color, Shape.SMOOTH_PANE);
        DeferredBlock<AcrylGlassStairBlock> stairs = BLOCKS.registerBlock(
            stairsId, p -> new AcrylGlassStairBlock(base.get().defaultBlockState(), color, p),
            glassProperties(color));
        add(stairsId, true, color, Shape.STAIRS, stairs);
        DeferredBlock<AcrylGlassSlabBlock> slab = BLOCKS.registerBlock(
            slabId, p -> new AcrylGlassSlabBlock(color, p), glassProperties(color));
        add(slabId, true, color, Shape.SLAB, slab);
        return new GlassFamily(base, bricks, chiseled, smooth, pane, brickPane,
            chiseledPane, smoothPane, stairs, slab);
    }

    private static DeferredBlock<Block> simpleOpaque(String id, DyeColor color, Shape shape) {
        DeferredBlock<Block> block = BLOCKS.registerSimpleBlock(id, opaqueProperties(color));
        add(id, false, color, shape, block);
        return block;
    }

    private static DeferredBlock<AcrylGlassBlock> glassBlock(String id, DyeColor color, Shape shape) {
        DeferredBlock<AcrylGlassBlock> block = BLOCKS.registerBlock(
            id, p -> new AcrylGlassBlock(color, p), glassProperties(color));
        add(id, true, color, shape, block);
        return block;
    }

    private static DeferredBlock<AcrylGlassPaneBlock> glassPane(String id, DyeColor color, Shape shape) {
        DeferredBlock<AcrylGlassPaneBlock> block = BLOCKS.registerBlock(
            id, p -> new AcrylGlassPaneBlock(color, p), glassProperties(color));
        add(id, true, color, shape, block);
        return block;
    }

    private static void add(String id, boolean glass, DyeColor color, Shape shape,
                            DeferredBlock<? extends Block> block) {
        ENTRIES.add(new Entry(id, glass, color, shape, block));
    }

    private static BlockBehaviour.Properties opaqueProperties(DyeColor color) {
        MapColor mapColor = color == null ? MapColor.COLOR_PINK : color.getMapColor();
        return BlockBehaviour.Properties.ofLegacyCopy(Blocks.PURPUR_BLOCK)
            .mapColor(mapColor).requiresCorrectToolForDrops().strength(3.0F, 1200.0F)
            .sound(SoundType.AMETHYST);
    }

    private static BlockBehaviour.Properties glassProperties(DyeColor color) {
        MapColor mapColor = color == null ? MapColor.COLOR_PINK : color.getMapColor();
        return BlockBehaviour.Properties.ofLegacyCopy(Blocks.TINTED_GLASS)
            .mapColor(mapColor).requiresCorrectToolForDrops().strength(1.5F, 1200.0F)
            .sound(SoundType.AMETHYST).noOcclusion();
    }

    public enum Shape {
        BLOCK, BRICKS, CHISELED, PILLAR, SMOOTH, STAIRS, SLAB, WALL,
        PANE, BRICK_PANE, CHISELED_PANE, SMOOTH_GLASS, SMOOTH_PANE
    }
    public record Entry(String id, boolean glass, DyeColor color, Shape shape,
                        DeferredBlock<? extends Block> block) {}
    public record OpaqueFamily(DeferredBlock<? extends Block> block,
                               DeferredBlock<? extends Block> bricks,
                               DeferredBlock<? extends Block> chiseled,
                               DeferredBlock<? extends Block> pillar,
                               DeferredBlock<? extends Block> smooth,
                               DeferredBlock<? extends Block> stairs,
                               DeferredBlock<? extends Block> slab,
                               DeferredBlock<? extends Block> wall) {}
    public record GlassFamily(DeferredBlock<? extends Block> block,
                              DeferredBlock<? extends Block> bricks,
                              DeferredBlock<? extends Block> chiseled,
                              DeferredBlock<? extends Block> smooth,
                              DeferredBlock<? extends Block> pane,
                              DeferredBlock<? extends Block> brickPane,
                              DeferredBlock<? extends Block> chiseledPane,
                              DeferredBlock<? extends Block> smoothPane,
                              DeferredBlock<? extends Block> stairs,
                              DeferredBlock<? extends Block> slab) {}
}
