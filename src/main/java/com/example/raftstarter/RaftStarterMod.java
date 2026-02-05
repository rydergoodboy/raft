package com.example.raftstarter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(RaftStarterMod.MOD_ID)
public class RaftStarterMod {
    public static final String MOD_ID = "raftstarter";

    private static final int PLATFORM_Y = 65;
    private static final int WATER_SURFACE_Y = 64;
    private static final int WATER_FLOOR_Y = 10;
    private static final int WATER_RADIUS = 96;
    private static final String PLAYER_SETUP_KEY = MOD_ID + ":player_setup";

    public RaftStarterMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(PLAYER_SETUP_KEY)) {
            return;
        }

        BlockPos center = new BlockPos(0, PLATFORM_Y, 0);
        createDeepWaterSpawnArea(level, center);
        createRaft(level, center);

        level.setDefaultSpawnPos(center.above(), 0.0F);
        level.getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(0, level.getServer());

        player.teleportTo(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D);
        data.putBoolean(PLAYER_SETUP_KEY, true);
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        if (!level.dimension().equals(Level.OVERWORLD) || level.getGameTime() % 40 != 0) {
            return;
        }

        ServerPlayer player = level.players().isEmpty() ? null : level.players().get(0);
        if (player == null) {
            return;
        }

        int spread = 30;
        double x = player.getX() + Mth.nextInt(level.random, -spread, spread);
        double z = player.getZ() - 40.0D;
        int y = Math.max(WATER_SURFACE_Y + 1, level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) x, (int) z) + 1);

        ItemEntity plank = new ItemEntity(level, x + 0.5D, y + 0.2D, z + 0.5D, new ItemStack(Items.OAK_PLANKS));
        plank.setPickUpDelay(10);

        double southSpeed = 0.12D;
        double sideDrift = level.random.nextDouble() * 0.04D - 0.02D;
        plank.setDeltaMovement(sideDrift, 0.0D, southSpeed);
        level.addFreshEntity(plank);
    }

    private static void createRaft(ServerLevel level, BlockPos center) {
        BlockPos raftBase = center;
        level.setBlockAndUpdate(raftBase, Blocks.OAK_PLANKS.defaultBlockState());
        level.setBlockAndUpdate(raftBase.east(), Blocks.OAK_PLANKS.defaultBlockState());
        level.setBlockAndUpdate(raftBase.south(), Blocks.OAK_PLANKS.defaultBlockState());
        level.setBlockAndUpdate(raftBase.south().east(), Blocks.OAK_PLANKS.defaultBlockState());
    }

    private static void createDeepWaterSpawnArea(ServerLevel level, BlockPos center) {
        int minX = center.getX() - WATER_RADIUS;
        int maxX = center.getX() + WATER_RADIUS;
        int minZ = center.getZ() - WATER_RADIUS;
        int maxZ = center.getZ() + WATER_RADIUS;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int distance = Math.abs(x - center.getX()) + Math.abs(z - center.getZ());
                if (distance > WATER_RADIUS + 20) {
                    continue;
                }

                for (int y = WATER_FLOOR_Y; y <= WATER_SURFACE_Y; y++) {
                    level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState());
                }

                for (int y = WATER_SURFACE_Y + 1; y <= WATER_SURFACE_Y + 20; y++) {
                    level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }

                level.setBlockAndUpdate(new BlockPos(x, WATER_FLOOR_Y - 1, z), Blocks.STONE.defaultBlockState());
            }
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos edge = center.relative(direction, WATER_RADIUS + 1);
            for (int y = WATER_FLOOR_Y; y <= WATER_SURFACE_Y; y++) {
                level.setBlockAndUpdate(new BlockPos(edge.getX(), y, edge.getZ()), Blocks.STONE.defaultBlockState());
            }
        }
    }
}
