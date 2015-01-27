package net.firsp.mods.conductor

import java.util

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.world.{ChunkCoordIntPair, World}
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.{Type, Ticket}

import scala.collection.JavaConversions._

class BlockAnchor extends Block(Material.air) {
  setHardness(0.3F)
  setStepSound(Block.soundTypeGlass)
  setCreativeTab(CreativeTabs.tabTransport)
  setBlockName("firsp_anchor")
  setBlockTextureName("firsp_cond:anchor")

  override def isOpaqueCube = false

  override def renderAsNormalBlock = false

  override def onBlockPlaced(world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, meta: Int) = {
    forceChunk(world, x, y, z, None)
    meta
  }

  def forceChunk(world: World, x: Int, y: Int, z: Int, ticket: Option[Ticket]) = {
    val validTicket = ticket match {
      case Some(t) => t
      case None => ForgeChunkManager.requestTicket(ConductorMod.instance, world, Type.NORMAL)
    }
    ChainableTag.of(validTicket.getModData).integer("x",x).integer("y",y).integer("z",z)
    val chunkX = x >> 4
    val chunkZ = z >> 4
    for (xx <- (x - 1 to x + 1).toList; zz <- (z - 1 to z + 1).toList) {
      ForgeChunkManager.forceChunk(validTicket, new ChunkCoordIntPair(xx, zz))
    }
  }
}

object AnchorChunkLoadCallback extends ForgeChunkManager.OrderedLoadingCallback {
  override def ticketsLoaded(tickets: util.List[Ticket], world: World, maxTicketCount: Int): util.List[Ticket] = tickets.filter { t =>
    val m = t.getModData
    world.getBlock(m.getInteger("x"), m.getInteger("y"), m.getInteger("z")).isInstanceOf[BlockAnchor]
  }

  override def ticketsLoaded(tickets: util.List[Ticket], world: World): Unit = tickets.foreach { t =>
    val m = t.getModData
    ConductorMod.anchor.forceChunk(world, m.getInteger("x"), m.getInteger("y"), m.getInteger("z"), Some(t))
  }
}
