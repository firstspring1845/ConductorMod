package net.firsp.mods.conductor

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._

class TileFluidConductor extends TileEntity with IFluidHandler {

  val tank = new FluidTank(1000)

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = tank.fill(resource, doFill)

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = if (resource == null || !canDrain(from, resource.getFluid)) null else drain(from, resource.amount, doDrain)

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = tank.drain(maxDrain, doDrain)

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = fluid != null && tank.getFluid == null || tank.getFluid.fluidID == fluid.getID

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = fluid != null && tank.getFluid != null && tank.getFluid.fluidID == fluid.getID

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = Array(tank.getInfo)

  override def readFromNBT(nbt:NBTTagCompound) = {
    super.readFromNBT(nbt)
    tank.readFromNBT(nbt.getCompoundTag("tank"))
  }

  override def writeToNBT(nbt:NBTTagCompound) = {
    super.writeToNBT(nbt)
    nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound))
  }

  override def updateEntity = {
    ForgeDirection.VALID_DIRECTIONS.foreach(o => {
      getWorldObj.getTileEntity(xCoord + o.offsetX, yCoord + o.offsetY, zCoord + o.offsetZ) match {
        case to: TileFluidConductor => transferFluid(to)
        case handler: IFluidHandler => {
          val opposite = o.getOpposite
          if (getWorldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
            handler.drain(opposite, tank.fill(handler.drain(opposite, Integer.MAX_VALUE, false), true), true)
          } else {
            if (tank.getFluid != null) {
              val fluid = tank.getFluid.copy
              fluid.amount /= 2
              tank.drain(handler.fill(opposite, fluid, true), true)
            }
          }
        }
        case _ =>
      }
    })
  }

  def transferFluid(to: TileFluidConductor) = {
    if (tank.getFluid != null) {
      to.tank.getFluid match {
        case f if f == null => to.tank.fill(tank.drain(tank.getFluidAmount / 2, true), true)
        case f if tank.getFluid.isFluidEqual(f) && tank.getFluidAmount > f.amount => to.tank.fill(tank.drain((tank.getFluidAmount - f.amount) / 2, true), true)
        case _ =>
      }
    }
  }

}

class TileTeleportFluidConductor extends TileFluidConductor with TeleportableTile {
  override def updateEntity = {
    super.updateEntity
    getTeleportables.collect { case t: TileFluidConductor => t}.foreach(transferFluid)
  }
}