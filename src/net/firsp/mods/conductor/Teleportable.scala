package net.firsp.mods.conductor

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World

import scala.collection.mutable.HashSet

object TeleportManager {
  val teleportables = new HashSet[TeleportableTile]
}

trait TeleportableTile extends TileEntity {
  self: TileEntity =>
  var freq = 0

  def changeFreq(a: Any) = a match {
    case is: ItemStack => freq = is.stackSize
    case _ => freq = 0
  }

  def getTeleportables = if(freq == 0) Set.empty else TeleportManager.teleportables.filter(t => t.freq == freq)

  override def readFromNBT(nbt: NBTTagCompound) = {
    super.readFromNBT(nbt)
    freq = nbt.getInteger("freq")
  }

  override def writeToNBT(nbt: NBTTagCompound) = {
    super.writeToNBT(nbt)
    nbt.setInteger("freq", freq)
  }

  override def validate: Unit = {
    super.validate
    TeleportManager.teleportables.add(this)
  }

  override def invalidate: Unit = {
    super.invalidate
    TeleportManager.teleportables.remove(this)
  }

  override def onChunkUnload: Unit = TeleportManager.teleportables.remove(this)

  override def hashCode = xCoord ^ yCoord ^ zCoord
}

trait TeleportableBlock {
  self: Block =>

  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, side: Int, i: Float, j: Float, k: Float): Boolean = {
    world.getTileEntity(x, y, z) match {
      case t: TeleportableTile => {
        t.changeFreq(player.getCurrentEquippedItem)
        if(!world.isRemote){
          player.addChatComponentMessage(new ChatComponentText(if (t.freq == 0) "Reset frequency" else "Change frequency to " + t.freq))
        }
        true
      }
      case _ =>false
    }
  }
}
