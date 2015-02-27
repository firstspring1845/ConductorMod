package net.firsp.mods.conductor

import cofh.api.energy.IEnergyHandler
import net.firsp.lib.ChainableTag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BlockPump extends BlockConductor({
  new TilePump
}) {
  setBlockName("conductor.pump")
  setBlockTextureName("firsp_cond:pump")

  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, side: Int, i: Float, j: Float, k: Float): Boolean = {
    world.getTileEntity(x, y, z) match {
      case t: TilePump => t.energy += 100
      case _ =>
    }
    true
  }
}

class TilePump extends TileEntity with IFluidHandler with IEnergyHandler {
  val tank = new FluidTank(8000)
  var energy = 0

  case class Position(x: Int, y: Int, z: Int)

  var finder = Future {}
  val validFluids = mutable.Stack[Position]()

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = 0

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = null

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = null

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = Array(new FluidTankInfo(tank.getFluid, tank.getCapacity))

  override def extractEnergy(from: ForgeDirection, maxExtract: Int, simulate: Boolean): Int = 0

  override def getEnergyStored(from: ForgeDirection): Int = energy

  override def getMaxEnergyStored(from: ForgeDirection): Int = 10000

  override def receiveEnergy(from: ForgeDirection, maxReceive: Int, simulate: Boolean): Int = {
    val m = Math.min(maxReceive, 10000 - energy)
    if (!simulate) energy += m
    m
  }

  override def canConnectEnergy(from: ForgeDirection): Boolean = true

  override def readFromNBT(nbt: NBTTagCompound) = {
    super.readFromNBT(nbt)
    tank.readFromNBT(nbt.getCompoundTag("tank"))
    energy = nbt.getInteger("energy")
  }

  override def writeToNBT(nbt: NBTTagCompound) = {
    super.writeToNBT(nbt)
    ChainableTag.of(nbt).tag("tank", tank.writeToNBT(new NBTTagCompound)).integer("energy", energy)
  }

  override def updateEntity: Unit = {
    val w = getWorldObj
    if (!w.isRemote) {
      ForgeDirection.VALID_DIRECTIONS.foreach(o => w.getTileEntity(xCoord + o.offsetX, yCoord + o.offsetY, zCoord + o.offsetZ) match {
        case handler: IFluidHandler => if (tank.getFluid != null && tank.getFluid.getFluid != null) {
          tank.drain(handler.fill(o.getOpposite, tank.getFluid, true), true)
        }
        case _ =>
      })
      if (finder.value == None) return // can't process at finder not complete
      while(!validFluids.isEmpty && !canDrainFluid(w, validFluids.top)) validFluids.pop
      if (!validFluids.isEmpty) {
        val p = validFluids.top
        if (drainFluid(w, p)) validFluids.pop
        return
      }
      (0 to yCoord - 1).reverse.find(y => w.getBlock(xCoord, y, zCoord) != Blocks.glass) match {
        case Some(y) => {
          val f = getFluid(w, Position(xCoord, y, zCoord))
          if (f != null) {
            finder = Future {
              findFluid(w, Position(xCoord, y, zCoord), f)
            }
            return
          }
          else if (w.getBlock(xCoord, y, zCoord) == Blocks.air && energy >= 100) {
            w.setBlock(xCoord, y, zCoord, Blocks.glass)
            energy -= 100
          }
        }
        case None =>
      }
    }
  }

  def getFluid(world: World, position: Position) = FluidRegistry.lookupFluidForBlock(world.getBlock(position.x, position.y, position.z))

  def findFluid(world: World, position: Position, fluid: Fluid) = {
    validFluids.clear
    val find = mutable.Queue(position)
    val visited = mutable.HashSet[Position]()

    val d = ForgeDirection.VALID_DIRECTIONS.filter(_ != ForgeDirection.DOWN)
    while (!find.isEmpty) {
      val p = find.dequeue
      if (!visited.contains(p)) {
        validFluids.push(p)
        visited += p
        if (Math.sqrt(Math.pow(p.x - xCoord, 2) + Math.pow(p.z - zCoord, 2)) < 64) {
          ForgeDirection.VALID_DIRECTIONS.foreach(o => {
            val pos = Position(p.x + o.offsetX, p.y + o.offsetY, p.z + o.offsetZ)
            if (getFluid(world, pos) == fluid) {
              find.enqueue(pos)
            }
          })
        }
      }
    }
  }

  def canDrainFluid(world: World, position: Position) = getFluid(world, position) != null && world.getBlockMetadata(position.x, position.y, position.z) == 0

  def drainFluid(world: World, position: Position): Boolean = {
    val f = getFluid(world, position)
    if (tank.getFluid == null || tank.getFluid.getFluid == f) {
      if (energy >= 100 && tank.getCapacity - tank.getFluidAmount >= 1000) {
        world.setBlockToAir(position.x, position.y, position.z)
        tank.fill(new FluidStack(f, 1000), true)
        energy -= 100
      } else return false
    }
    true
  }
}
