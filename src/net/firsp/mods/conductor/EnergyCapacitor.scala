package net.firsp.mods.conductor

import java.util

import net.firsp.lib.ChainableTag
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack, ItemBlock}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World

object EnergyCapacitor {
  val MAX_CHARGED = 15000000 // One bucket fuel energy
}

class BlockEnergyCapacitor extends BlockConductor({
  new TileEnergyCapacitor
}) {
  setHardness(0.3F)
  setStepSound(Block.soundTypeGlass)
  setCreativeTab(CreativeTabs.tabTransport)
  setBlockName("firsp_capacitor")
  setBlockTextureName("firsp_cond:ecapacitor")

  override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, stack: ItemStack) = stack.getTagCompound match {
    case nbt: NBTTagCompound => world.getTileEntity(x, y, z) match {
      case t: TileEnergyCapacitor => {
        t.charged = nbt.getInteger("charged")
      }
      case _ =>
    }
    case _ =>
  }

  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, side: Int, i: Float, j: Float, k: Float): Boolean = {
    world.getTileEntity(x, y, z) match {
      case t: TileEnergyCapacitor => {
        if (!world.isRemote) {
          player.addChatComponentMessage(new ChatComponentText("" + t.charged + "/" + EnergyCapacitor.MAX_CHARGED + "RF(" + (100F * t.charged / EnergyCapacitor.MAX_CHARGED).toInt + "%) Charged"))
        }
        true
      }
      case _ => false
    }
  }

  override def getDrops(world: World, x: Int, y: Int, z: Int, metadata: Int, fortune: Int) = {
    import scala.collection.JavaConversions._
    val is = new ItemStack(this, 1)
    is.setTagCompound(ChainableTag.newInstance.integer("charged", dropCharged).as)
    is.setItemDamage(1000 - (1000F * dropCharged / EnergyCapacitor.MAX_CHARGED).toInt)
    new java.util.ArrayList(Seq(is))
  }

  var dropCharged = 0

  override def removedByPlayer(world: World, player: EntityPlayer, x: Int, y: Int, z: Int, w: Boolean) = {
    world.getTileEntity(x, y, z) match {
      case t: TileEnergyCapacitor => dropCharged = t.charged
      case _ => dropCharged = 0
    }
    super.removedByPlayer(world, player, x, y, z, w)
  }
}

class ItemEnergyCapacitor(block: Block) extends ItemBlock(block) {
  setMaxStackSize(1)
  setMaxDamage(1000)

  override def addInformation(stack:ItemStack, player: EntityPlayer, list:java.util.List[_], adv:Boolean) = stack.getTagCompound match {
    case nbt: NBTTagCompound => list.asInstanceOf[java.util.List[String]].add("" + nbt.getInteger("charged") + "RF Charged")
    case _ => list.asInstanceOf[java.util.List[String]].add("0RF Charged")
  }

  override def getSubItems(item : Item, tabs : CreativeTabs, list : util.List[_]): Unit = {
    val l = list.asInstanceOf[util.List[ItemStack]]
    val is = new ItemStack(this)
    is.setItemDamage(1000)
    l.add(is.copy)
    is.setItemDamage(0)
    is.setTagCompound(ChainableTag.newInstance.integer("charged", EnergyCapacitor.MAX_CHARGED).as)
    l.add(is.copy)
  }
}

class TileEnergyCapacitor extends TileEnergyConductor {

  var charged = 0

  override def readFromNBT(nbt: NBTTagCompound) = {
    super.readFromNBT(nbt)
    charged = nbt.getInteger("charged")
  }

  override def writeToNBT(nbt: NBTTagCompound) = {
    super.writeToNBT(nbt)
    nbt.setInteger("charged", charged)
  }

  override def updateEntity = {
    if (getWorldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
      val move = Math.min(charged, MAX_ENERGY - internalEnergy)
      charged -= move
      internalEnergy += move
    } else {
      val move = Math.min(EnergyCapacitor.MAX_CHARGED - charged, internalEnergy)
      internalEnergy -= move
      charged += move
    }
    super.updateEntity
  }

}
