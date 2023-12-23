package net.kyrptonaught.customportalapi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.kyrptonaught.customportalapi.interfaces.EntityInCustomPortal;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.CustomTeleporter;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CustomPortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
    protected static final VoxelShape X_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
    protected static final VoxelShape Y_SHAPE = Block.createCuboidShape(0.0D, 6.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public CustomPortalBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(AXIS)) {
            case Z -> Z_SHAPE;
            case Y -> Y_SHAPE;
            default -> X_SHAPE;
        };
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        Block block = getPortalBase((World) world, pos);
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
        if (link != null) {
            PortalFrameTester portalFrameTester = link.getFrameTester().createInstanceOfPortalFrameTester().init(world, pos, CustomPortalHelper.getAxisFrom(state), block);
            if (portalFrameTester.isAlreadyLitPortalFrame())
                return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
        }
        //todo handle unknown portallink

        return Blocks.AIR.getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
    }


    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        EntityInCustomPortal entityInPortal = (EntityInCustomPortal) entity;
        entityInPortal.tickInPortal(pos.toImmutable());
        if (!entityInPortal.didTeleport()) {
            if (entityInPortal.getTimeInPortal() >= entity.getMaxNetherPortalTime()) {
                entityInPortal.setDidTP(true);
                if (!world.isClient)
                    CustomTeleporter.TPToDim(world, entity, getPortalBase(world, pos), pos);
            }
        }
    }

    public Block getPortalBase(World world, BlockPos pos) {
        return CustomPortalHelper.getPortalBaseDefault(world, pos);
    }
}