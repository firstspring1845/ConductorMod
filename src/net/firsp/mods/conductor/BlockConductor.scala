package net.firsp.mods.conductor

import net.minecraft.block.{Block, BlockContainer}
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

class BlockConductor(f: => TileEntity) extends BlockContainer(Material.glass) {
  setHardness(0.3F)
  setStepSound(Block.soundTypeGlass)
  setCreativeTab(CreativeTabs.tabTransport)

  override def isOpaqueCube = false

  override def renderAsNormalBlock = false

  override def createNewTileEntity(world: World, meta: Int) = f
}

class BlockConductorTeleport(f: => TileEntity) extends BlockConductor(f) with TeleportableBlock
