package net.firsp.mods.conductor

import cofh.api.energy.IEnergyHandler
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection

class TileEnergyConductor extends TileEntity with IEnergyHandler {

  val MAX_ENERGY = 1000
  var internalEnergy = 0

  override def receiveEnergy(from: ForgeDirection, maxReceive: Int, simulate: Boolean) = {
    val energy = Math.min(maxReceive, MAX_ENERGY - internalEnergy)
    if (!simulate) internalEnergy += energy
    energy
  }

  override def extractEnergy(from: ForgeDirection, maxExtract: Int, simulate: Boolean) = {
    val energy = Math.min(maxExtract, internalEnergy)
    if (!simulate) internalEnergy -= energy
    energy
  }

  override def getEnergyStored(from: ForgeDirection) = internalEnergy

  override def getMaxEnergyStored(from: ForgeDirection) = MAX_ENERGY

  override def canConnectEnergy(from: ForgeDirection) = true

  override def readFromNBT(nbt:NBTTagCompound) = {
    super.readFromNBT(nbt)
    internalEnergy = nbt.getInteger("energy")
  }

  override def writeToNBT(nbt:NBTTagCompound) = {
    super.writeToNBT(nbt)
    nbt.setInteger("energy", internalEnergy)
  }

  override def updateEntity = {
    ForgeDirection.VALID_DIRECTIONS.foreach(o => {
      getWorldObj().getTileEntity(xCoord + o.offsetX, yCoord + o.offsetY, zCoord + o.offsetZ) match {
        case to: TileEnergyConductor => transferEnergy(to)
        case handler: IEnergyHandler => internalEnergy -= handler.receiveEnergy(o.getOpposite, internalEnergy, false)
        case _ =>
      }
    })
  }

  def transferEnergy(to: TileEnergyConductor) = {
    if (internalEnergy > to.internalEnergy) {
      val move = (internalEnergy - to.internalEnergy) / 2
      internalEnergy -= move
      to.internalEnergy += move
    }
  }

}

class TileTeleportEnergyConductor extends TileEnergyConductor with TeleportableTile {
  override def updateEntity = {
    super.updateEntity
    getTeleportables.collect { case t: TileEnergyConductor => t}.foreach(transferEnergy)
  }

}
