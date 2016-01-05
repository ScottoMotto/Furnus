package mrriegel.furnus.block;

import java.util.Random;

import mrriegel.furnus.CreativeTab;
import mrriegel.furnus.Furnus;
import mrriegel.furnus.handler.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFurnus extends BlockContainer {
	public static final PropertyDirection FACING = PropertyDirection.create("facing",
			EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool STATE = PropertyBool.create("state");
	public static final Block furnus = new BlockFurnus();

	public BlockFurnus() {
		super(Material.rock);
		this.setHardness(4.0F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
				.withProperty(STATE, false));
		this.setCreativeTab(CreativeTab.tab1);
		this.setUnlocalizedName(Furnus.MODID + ":" + "furnus");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockState getStateForEntityRender(IBlockState state) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.SOUTH);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta % 6);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}
		return this.getDefaultState().withProperty(FACING, enumfacing)
				.withProperty(STATE, meta > 5);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int p = state.getValue(STATE) ? 0 : 6;
		return state.getValue(FACING).getIndex() + p;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { FACING, STATE });
	}

	public void setFurnusState(World world, BlockPos pos, IBlockState state, boolean on) {
		TileEntity tileentity = world.getTileEntity(pos);
		world.setBlockState(pos, state.withProperty(STATE, on), 2);
		if (tileentity != null) {
			tileentity.validate();
			world.setTileEntity(pos, tileentity);
		}
		world.markBlockForUpdate(pos);

	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		this.setDefaultFacing(worldIn, pos, state);
	}

	private void setDefaultFacing(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			Block block = worldIn.getBlockState(pos.north()).getBlock();
			Block block1 = worldIn.getBlockState(pos.south()).getBlock();
			Block block2 = worldIn.getBlockState(pos.west()).getBlock();
			Block block3 = worldIn.getBlockState(pos.east()).getBlock();
			EnumFacing enumfacing = state.getValue(FACING);
			TileFurnus tile = (TileFurnus) worldIn.getTileEntity(pos);
			tile.setFace(enumfacing.toString().substring(0, 1).toUpperCase());
			if (enumfacing == EnumFacing.NORTH && block.isFullBlock() && !block1.isFullBlock()) {
				enumfacing = EnumFacing.SOUTH;
			} else if (enumfacing == EnumFacing.SOUTH && block1.isFullBlock()
					&& !block.isFullBlock()) {
				enumfacing = EnumFacing.NORTH;
			} else if (enumfacing == EnumFacing.WEST && block2.isFullBlock()
					&& !block3.isFullBlock()) {
				enumfacing = EnumFacing.EAST;
			} else if (enumfacing == EnumFacing.EAST && block3.isFullBlock()
					&& !block2.isFullBlock()) {
				enumfacing = EnumFacing.WEST;
			}

			worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing), 2);
			worldIn.markBlockForUpdate(pos);
		}
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
			float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState()
				.withProperty(FACING, placer.getHorizontalFacing().getOpposite())
				.withProperty(STATE, false);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state,
			EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos,
				state.withProperty(FACING, placer.getHorizontalFacing().getOpposite())
						.withProperty(STATE, false), 2);
		TileFurnus tile = (TileFurnus) worldIn.getTileEntity(pos);
		tile.setFace(placer.getHorizontalFacing().getOpposite().toString().substring(0, 1)
				.toUpperCase());
		worldIn.markBlockForUpdate(pos);

	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
			EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileFurnus tile = (TileFurnus) worldIn.getTileEntity(pos);
		if (worldIn.isRemote) {
			return true;
		} else {
			playerIn.openGui(Furnus.instance, GuiHandler.FURNUS, worldIn, pos.getX(), pos.getY(),
					pos.getZ());
			return true;
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		return world.getBlockState(pos).getValue(STATE) ? 13 : 0;
	}

	@Override
	public int getRenderType() {
		return 3;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileFurnus tileentity = (TileFurnus) worldIn.getTileEntity(pos);
		InventoryHelper.dropInventoryItems(worldIn, pos, tileentity);
		worldIn.updateComparatorOutputLevel(pos, this);
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileFurnus();
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("incomplete-switch")
	public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		TileFurnus tile = (TileFurnus) worldIn.getTileEntity(pos);
		if (tile.isBurning()) {
			EnumFacing enumfacing = state.getValue(FACING);
			double d0 = pos.getX() + 0.5D;
			double d1 = pos.getY() + rand.nextDouble() * 6.0D / 16.0D;
			double d2 = pos.getZ() + 0.5D;
			double d3 = 0.52D;
			double d4 = rand.nextDouble() * 0.6D - 0.3D;

			for (int i = 0; i < tile.getSpeed() + 1; i++)
				switch (enumfacing) {
				case WEST:
					worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 - d3, d1, d2 + d4,
							0.0D, 0.0D, 0.0D, new int[0]);
					worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 - d3, d1, d2 + d4, 0.0D,
							0.0D, 0.0D, new int[0]);
					break;
				case EAST:
					worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d3, d1, d2 + d4,
							0.0D, 0.0D, 0.0D, new int[0]);
					worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + d3, d1, d2 + d4, 0.0D,
							0.0D, 0.0D, new int[0]);
					break;
				case NORTH:
					worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 - d3,
							0.0D, 0.0D, 0.0D, new int[0]);
					worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 - d3, 0.0D,
							0.0D, 0.0D, new int[0]);
					break;
				case SOUTH:
					worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 + d3,
							0.0D, 0.0D, 0.0D, new int[0]);
					worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 + d3, 0.0D,
							0.0D, 0.0D, new int[0]);
				}
		}
	}

	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}

	@Override
	public int getComparatorInputOverride(World worldIn, BlockPos pos) {
		return Container.calcRedstone(worldIn.getTileEntity(pos));
	}

	public static void init() {
		GameRegistry.registerBlock(furnus, "furnus");
		GameRegistry.registerTileEntity(TileFurnus.class, "tileFurnus");
	}

}
