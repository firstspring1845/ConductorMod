package net.firsp.mods.conductor

import cofh.api.energy.IEnergyHandler
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.{OpenGlHelper, RenderHelper, Tessellator}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidRegistry, FluidStack, IFluidHandler}
import org.lwjgl.opengl.GL11._

object TESR extends TileEntitySpecialRenderer {
  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, f: Float): Unit = {
    tile match {
      case e: TileEnergyConductor => {
        val from = 0.5D - e.internalEnergy / 2001D
        val to = 0.5D + e.internalEnergy / 2001D

        val doRenderSide = ForgeDirection.VALID_DIRECTIONS.map(o => e.getWorldObj.getTileEntity(e.xCoord + o.offsetX, e.yCoord + o.offsetY, e.zCoord + o.offsetZ).isInstanceOf[IEnergyHandler])
        drawBox(x, y, z, from, from, from, to, to, to, 0, 255, 255, 255)
        //down
        if (doRenderSide(0)) drawBox(x, y, z, from, 0.001, from, to, from, to, 0, 255, 255, 255)
        //up
        if (doRenderSide(1)) drawBox(x, y, z, from, to, from, to, 0.999, to, 0, 255, 255, 255)
        //north
        if (doRenderSide(2)) drawBox(x, y, z, from, from, 0.001, to, to, from, 0, 255, 255, 255)
        //south
        if (doRenderSide(3)) drawBox(x, y, z, from, from, to, to, to, 0.999, 0, 255, 255, 255)
        //west
        if (doRenderSide(4)) drawBox(x, y, z, 0.001, from, from, from, to, to, 0, 255, 255, 255)
        //east
        if (doRenderSide(5)) drawBox(x, y, z, to, from, from, 0.999, to, to, 0, 255, 255, 255)
      }
      case f: TileFluidConductor => {
        f.tank.getFluid match {
          case fs: FluidStack => {
            bindTexture(TextureMap.locationBlocksTexture)
            val fluid = if (fs.getFluid != null) fs.getFluid else FluidRegistry.WATER
            val icon = fluid.getIcon
            val v = ForgeDirection.VALID_DIRECTIONS
            val doRenderSide = v.map(o => f.getWorldObj.getTileEntity(f.xCoord + o.offsetX, f.yCoord + o.offsetY, f.zCoord + o.offsetZ).isInstanceOf[IFluidHandler])
            val internalDoRenderSide = doRenderSide.map(_ ^ true)
            val from = 0.5 - fs.amount / 2001D
            val to = 0.5 + fs.amount / 2001D
            //internal
            drawBox(x, y, z, from, from, from, to, to, to, internalDoRenderSide, fluid)

            //down
            if (doRenderSide(0)) drawBox(x, y, z, from, 0.001, from, to, from, to, v.map(_ != ForgeDirection.UP), fluid)
            //up
            if (doRenderSide(1)) drawBox(x, y, z, from, to, from, to, 0.999, to, v.map(_ != ForgeDirection.DOWN), fluid)
            //north
            if (doRenderSide(2)) drawBox(x, y, z, from, from, 0.001, to, to, from, v.map(_ != ForgeDirection.SOUTH), fluid)
            //south
            if (doRenderSide(3)) drawBox(x, y, z, from, from, to, to, to, 0.999, v.map(_ != ForgeDirection.NORTH), fluid)
            //west
            if (doRenderSide(4)) drawBox(x, y, z, 0.001, from, from, from, to, to, v.map(_ != ForgeDirection.EAST), fluid)
            //east
            if (doRenderSide(5)) drawBox(x, y, z, to, from, from, 0.999, to, to, v.map(_ != ForgeDirection.WEST), fluid)
          }
          case _ =>
        }
      }
    }
  }

  def drawBox(x: Double, y: Double, z: Double, fromX: Double, fromY: Double, fromZ: Double, toX: Double, toY: Double, toZ: Double, doRenderSide: Array[Boolean], fluid: Fluid) = {
    val icon = fluid.getStillIcon
    val color = fluid.getColor
    val r = (color >> 16) & 255
    val g = (color >> 8) & 255
    val b = color & 255
    val t = Tessellator.instance
    glPushMatrix
    glPushAttrib(GL_ENABLE_BIT)
    glTranslated(x, y, z)
    glDisable(GL_LIGHTING)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glDepthMask(false)
    //down
    if (doRenderSide(0)) {
      t.startDrawingQuads
      t.setColorRGBA(r, g, b, 255)
      t.addVertexWithUV(fromX, fromY, fromZ, icon.getInterpolatedU(fromX * 16), icon.getInterpolatedV(fromZ * 16))
      t.addVertexWithUV(toX, fromY, fromZ, icon.getInterpolatedU(toX * 16), icon.getInterpolatedV(fromZ * 16))
      t.addVertexWithUV(toX, fromY, toZ, icon.getInterpolatedU(toX * 16), icon.getInterpolatedV(toZ * 16))
      t.addVertexWithUV(fromX, fromY, toZ, icon.getInterpolatedU(fromX * 16), icon.getInterpolatedV(toZ * 16))
      t.draw
    }
    //up
    if (doRenderSide(1)) {
      glCullFace(GL_FRONT)
      t.startDrawingQuads
      t.setColorRGBA(r, g, b, 255)
      t.addVertexWithUV(fromX, toY, fromZ, icon.getInterpolatedU(fromX * 16), icon.getInterpolatedV(fromZ * 16))
      t.addVertexWithUV(toX, toY, fromZ, icon.getInterpolatedU(toX * 16), icon.getInterpolatedV(fromZ * 16))
      t.addVertexWithUV(toX, toY, toZ, icon.getInterpolatedU(toX * 16), icon.getInterpolatedV(toZ * 16))
      t.addVertexWithUV(fromX, toY, toZ, icon.getInterpolatedU(fromX * 16), icon.getInterpolatedV(toZ * 16))
      t.draw
      glCullFace(GL_BACK)
    }
    //north
    if (doRenderSide(2)) {
      t.startDrawingQuads
      t.setColorRGBA(r, g, b, 255)
      t.addVertexWithUV(fromX, fromY, fromZ, icon.getInterpolatedU(fromX * 16), icon.getInterpolatedV(fromY * 16))
      t.addVertexWithUV(fromX, toY, fromZ, icon.getInterpolatedU(fromX * 16), icon.getInterpolatedV(toY * 16))
      t.addVertexWithUV(toX, toY, fromZ, icon.getInterpolatedU(toX * 16), icon.getInterpolatedV(toY * 16))
      t.addVertexWithUV(toX, fromY, fromZ, icon.getInterpolatedU(toX * 16), icon.getInterpolatedV(fromY * 16))
      t.draw
    }
    //south
    if (doRenderSide(3)) {
      glCullFace(GL_FRONT)
      t.startDrawingQuads
      t.setColorRGBA(r, g, b, 255)
      t.addVertexWithUV(fromX, fromY, toZ, icon.getInterpolatedU(fromX * 16), icon.getInterpolatedV(fromY * 16))
      t.addVertexWithUV(fromX, toY, toZ, icon.getInterpolatedU(fromX * 16), icon.getInterpolatedV(toY * 16))
      t.addVertexWithUV(toX, toY, toZ, icon.getInterpolatedU(toX * 16), icon.getInterpolatedV(toY * 16))
      t.addVertexWithUV(toX, fromY, toZ, icon.getInterpolatedU(toX * 16), icon.getInterpolatedV(fromY * 16))
      t.draw
      glCullFace(GL_BACK)
    }
    //west
    if (doRenderSide(4)) {
      glCullFace(GL_FRONT)
      t.startDrawingQuads
      t.setColorRGBA(r, g, b, 255)
      t.addVertexWithUV(fromX, fromY, fromZ, icon.getInterpolatedU(fromZ * 16), icon.getInterpolatedV(fromY * 16))
      t.addVertexWithUV(fromX, toY, fromZ, icon.getInterpolatedU(fromZ * 16), icon.getInterpolatedV(toY * 16))
      t.addVertexWithUV(fromX, toY, toZ, icon.getInterpolatedU(toZ * 16), icon.getInterpolatedV(toY * 16))
      t.addVertexWithUV(fromX, fromY, toZ, icon.getInterpolatedU(toZ * 16), icon.getInterpolatedV(fromY * 16))
      t.draw
      glCullFace(GL_BACK)
    }
    //east
    if (doRenderSide(5)) {
      t.startDrawingQuads
      t.setColorRGBA(r, g, b, 255)
      t.addVertexWithUV(toX, fromY, fromZ, icon.getInterpolatedU(fromZ * 16), icon.getInterpolatedV(fromY * 16))
      t.addVertexWithUV(toX, toY, fromZ, icon.getInterpolatedU(fromZ * 16), icon.getInterpolatedV(toY * 16))
      t.addVertexWithUV(toX, toY, toZ, icon.getInterpolatedU(toZ * 16), icon.getInterpolatedV(toY * 16))
      t.addVertexWithUV(toX, fromY, toZ, icon.getInterpolatedU(toZ * 16), icon.getInterpolatedV(fromY * 16))
      t.draw
    }

    glEnable(GL_LIGHTING)
    glDisable(GL_BLEND)
    glDepthMask(true)
    glPopAttrib
    glPopMatrix
  }

  def drawBox(x: Double, y: Double, z: Double, fromX: Double, fromY: Double, fromZ: Double, toX: Double, toY: Double, toZ: Double, r: Int, g: Int, b: Int, a: Int) = {
    val t = Tessellator.instance
    glPushMatrix
    glTranslated(x, y, z)
    glDisable(GL_TEXTURE_2D)
    glDisable(GL_LIGHTING)
    glDisable(GL_CULL_FACE)
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f)

    t.setColorRGBA(r, g, b, a)
    //down
    t.startDrawingQuads
    t.setColorRGBA(r, g, b, a)
    t.addVertex(fromX, fromY, fromZ)
    t.addVertex(toX, fromY, fromZ)
    t.addVertex(toX, fromY, toZ)
    t.addVertex(fromX, fromY, toZ)
    t.draw
    //up
    /*t.startDrawingQuads
    t.setColorRGBA(r, g, b, a)
    t.addVertex(fromX, toY, fromZ)
    t.addVertex(toX, toY, fromZ)
    t.addVertex(toX, toY, toZ)
    t.addVertex(fromX, toY, toZ)
    t.draw*/
    glBegin(GL_QUADS)
    glColor3d(0, 1, 1)
    glVertex3d(fromX, toY, fromZ)
    glVertex3d(toX, toY, fromZ)
    glVertex3d(toX, toY, toZ)
    glVertex3d(fromX, toY, toZ)
    glEnd
    //north
    t.startDrawingQuads
    t.setColorRGBA(r, g, b, a)
    t.addVertex(fromX, fromY, fromZ)
    t.addVertex(fromX, toY, fromZ)
    t.addVertex(toX, toY, fromZ)
    t.addVertex(toX, fromY, fromZ)
    t.draw
    //south
    t.startDrawingQuads
    t.setColorRGBA(r, g, b, a)
    t.addVertex(fromX, fromY, toZ)
    t.addVertex(fromX, toY, toZ)
    t.addVertex(toX, toY, toZ)
    t.addVertex(toX, fromY, toZ)
    t.draw
    //west
    t.startDrawingQuads
    t.setColorRGBA(r, g, b, a)
    t.addVertex(fromX, fromY, fromZ)
    t.addVertex(fromX, toY, fromZ)
    t.addVertex(fromX, toY, toZ)
    t.addVertex(fromX, fromY, toZ)
    t.draw

    //east
    t.startDrawingQuads
    t.setColorRGBA(r, g, b, a)
    t.addVertex(toX, fromY, fromZ)
    t.addVertex(toX, toY, fromZ)
    t.addVertex(toX, toY, toZ)
    t.addVertex(toX, fromY, toZ)
    t.draw

    glEnable(GL_TEXTURE_2D)
    glEnable(GL_LIGHTING)
    glEnable(GL_CULL_FACE)
    glPopMatrix
  }

}
