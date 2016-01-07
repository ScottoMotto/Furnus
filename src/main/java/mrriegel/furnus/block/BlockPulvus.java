package mrriegel.furnus.block;

import java.util.Random;

import mrriegel.furnus.CreativeTab;
import mrriegel.furnus.Furnus;
import mrriegel.furnus.handler.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPulvus extends BlockContainer {
	@SideOnly(Side.CLIENT)
	private IIcon top, side, front, front_lit, bottom;

	public BlockPulvus() {
		super(Material.rock);
		this.setHardness(4.0F);
		this.setCreativeTab(CreativeTab.tab1);
		this.setBlockName(Furnus.MODID + ":" + "pulvus");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		top = reg.registerIcon(Furnus.MODID + ":pulvus_top");
		side = reg.registerIcon(Furnus.MODID + ":pulvus_side");
		front = reg.registerIcon(Furnus.MODID + ":pulvus_front");
		front_lit = reg.registerIcon(Furnus.MODID + ":pulvus_front_lit");
		bottom = reg.registerIcon(Furnus.MODID + ":pulvus_bottom");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		int meta = world.getBlockMetadata(x, y, z);
		TilePulvus tile = (TilePulvus) world.getTileEntity(x, y, z);
		IIcon f = tile.isBurning() ? front_lit : front;
		if (side == 3 && meta == 0)
			return f;
		switch (side) {
		case 0:
			return bottom;
		case 1:
			return top;
		}
		return (side != meta ? this.side : f);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		if (side == 3 && meta == 0)
			return front;
		switch (side) {
		case 0:
			return bottom;
		case 1:
			return top;
		}
		return (side != meta ? this.side : front);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player,
			ItemStack stack) {
		TilePulvus tile = (TilePulvus) world.getTileEntity(x, y, z);
		int l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		if (l == 0) {
			world.setBlockMetadataWithNotify(x, y, z, 2, 2);
			tile.setFace("N");
		}
		if (l == 1) {
			world.setBlockMetadataWithNotify(x, y, z, 5, 2);
			tile.setFace("E");
		}
		if (l == 2) {
			world.setBlockMetadataWithNotify(x, y, z, 3, 2);
			tile.setFace("S");
		}
		if (l == 3) {
			world.setBlockMetadataWithNotify(x, y, z, 4, 2);
			tile.setFace("W");
		}
		world.markBlockForUpdate(x, y, z);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
			int side, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		TilePulvus tile = (TilePulvus) world.getTileEntity(x, y, z);
		if (world.isRemote) {
			return true;
		} else {
			player.openGui(Furnus.instance, GuiHandler.PULVUS, world, x, y, z);
			return true;
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		TilePulvus tile = (TilePulvus) world.getTileEntity(x, y, z);
		return tile.isBurning() ? 13 : 0;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		TilePulvus tile = (TilePulvus) world.getTileEntity(x, y, z);
		for (ItemStack s : tile.getInv()) {
			if (s != null && !world.isRemote) {
				EntityItem ei = new EntityItem(world, x + 0.5d, y + 1, z + 0.5d, s.copy());
				if (s.hasTagCompound())
					ei.getEntityItem().setTagCompound((NBTTagCompound) s.getTagCompound().copy());
				world.spawnEntityInWorld(ei);
			}
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TilePulvus();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		TilePulvus tile = (TilePulvus) world.getTileEntity(x, y, z);
		if (tile.isBurning()) {
			int l = world.getBlockMetadata(x, y, z);
			float f = x + 0.5F;
			float f1 = y + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
			float f2 = z + 0.5F;
			float f3 = 0.52F;
			float f4 = rand.nextFloat() * 0.6F - 0.3F;
			for (int i = 0; i < tile.getSpeed() + 1; i++)
				if (l == 4) {
					world.spawnParticle("smoke", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
					world.spawnParticle("flame", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
				} else if (l == 5) {
					world.spawnParticle("smoke", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
					world.spawnParticle("flame", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
				} else if (l == 2) {
					world.spawnParticle("smoke", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
					world.spawnParticle("flame", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
				} else if (l == 3) {
					world.spawnParticle("smoke", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
					world.spawnParticle("flame", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
				}
		}
	}

	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}

	@Override
	public int getComparatorInputOverride(World p_149736_1_, int p_149736_2_, int p_149736_3_,
			int p_149736_4_, int p_149736_5_) {
		return Container.calcRedstoneFromInventory((IInventory) p_149736_1_.getTileEntity(
				p_149736_2_, p_149736_3_, p_149736_4_));
	}

}