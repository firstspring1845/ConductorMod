package net.firsp.mods.conductor

import cofh.api.energy.{IEnergyReceiver, IEnergyHandler}
import net.firsp.lib.ChainableTag
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.{Packet, NetworkManager}
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
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

  override def readFromNBT(nbt: NBTTagCompound) = {
    super.readFromNBT(nbt)
    internalEnergy = nbt.getInteger("energy")
  }

  override def writeToNBT(nbt: NBTTagCompound) = {
    super.writeToNBT(nbt)
    nbt.setInteger("energy", internalEnergy)
  }

  override def updateEntity = {
    if (!worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
    val te = ForgeDirection.VALID_DIRECTIONS.map(o => getWorldObj.getTileEntity(xCoord + o.offsetX, yCoord + o.offsetY, zCoord + o.offsetZ))
    smoothEnergy(te.collect { case t: TileEnergyConductor => t}.toList)
    ForgeDirection.VALID_DIRECTIONS.foreach(o => {
      te(o.ordinal) match {
        case t: TileEnergyConductor =>
        case receiver: IEnergyReceiver => internalEnergy -= receiver.receiveEnergy(o.getOpposite, internalEnergy, false)
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

  def smoothEnergy(e: List[TileEnergyConductor]) = {
    val energy = internalEnergy + e.map(_.internalEnergy).sum
    val average = energy / (e.length + 1)
    val mod = energy % (e.length + 1)
    internalEnergy = if(mod != 0) average + 1 else average
    e.zipWithIndex.foreach(t => t._1.internalEnergy = if (t._2 < mod - 1) average + 1 else average)
  }

  override def getDescriptionPacket: Packet = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -1, ChainableTag.newInstance.integer("e", internalEnergy).as)

  override def onDataPacket(net: NetworkManager, pkt: S35PacketUpdateTileEntity): Unit = {
    internalEnergy = pkt.func_148857_g.getInteger("e")
  }
}

class TileTeleportEnergyConductor extends TileEnergyConductor with TeleportableTile {
  override def updateEntity = {
    super.updateEntity
    smoothEnergy(getTeleportables.collect { case t: TileEnergyConductor => t}.toList)
  }

}
