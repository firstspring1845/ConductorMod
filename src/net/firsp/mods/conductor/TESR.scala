package net.firsp.mods.conductor

import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.{OpenGlHelper, RenderBlocks, RenderHelper, Tessellator}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidStack, IFluidHandler}
import org.lwjgl.opengl.GL11

object TESR extends TileEntitySpecialRenderer {
  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, f: Float): Unit = {
    tile match {
      case e: TileEnergyConductor => {
        val from = 0.5D - e.internalEnergy / 2000D
        val to = 0.5D + e.internalEnergy / 2000D
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z)
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
        RenderHelper.disableStandardItemLighting();
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        // down
        Tessellator.instance.startDrawing(GL11.GL_LINE_LOOP);
        Tessellator.instance.setColorRGBA(255, 0, 0, 255);
        Tessellator.instance.addVertex(from, from, from);
        Tessellator.instance.addVertex(to, from, from);
        Tessellator.instance.addVertex(to, from, to);
        Tessellator.instance.addVertex(from, from, to);
        Tessellator.instance.draw();
        // up
        Tessellator.instance.startDrawing(GL11.GL_LINE_LOOP);
        Tessellator.instance.setColorRGBA(255, 0, 0, 255);
        Tessellator.instance.addVertex(from, to, from);
        Tessellator.instance.addVertex(to, to, from);
        Tessellator.instance.addVertex(to, to, to);
        Tessellator.instance.addVertex(from, to, to);
        Tessellator.instance.draw();
        // west
        Tessellator.instance.startDrawing(GL11.GL_LINE_LOOP);
        Tessellator.instance.setColorRGBA(255, 0, 0, 255);
        Tessellator.instance.addVertex(from, from, from);
        Tessellator.instance.addVertex(from, to, from);
        Tessellator.instance.addVertex(from, to, to);
        Tessellator.instance.addVertex(from, from, to);
        Tessellator.instance.draw();
        // east
        Tessellator.instance.startDrawing(GL11.GL_LINE_LOOP);
        Tessellator.instance.setColorRGBA(255, 0, 0, 255);
        Tessellator.instance.addVertex(to, from, from);
        Tessellator.instance.addVertex(to, to, from);
        Tessellator.instance.addVertex(to, to, to);
        Tessellator.instance.addVertex(to, from, to);
        Tessellator.instance.draw();
        // 他の描画に影響するので戻す
        GL11.glPopMatrix();
        RenderHelper.enableStandardItemLighting();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
      }
      case f: TileFluidConductor => {
        val r = RenderBlocks.getInstance
        val d = net.minecraftforge.common.util.ForgeDirection.DOWN
        f.tank.getFluid match {
          case fs: FluidStack => {
            bindTexture(TextureMap.locationBlocksTexture)
            val icon = fs.getFluid.getStillIcon
            val v = ForgeDirection.VALID_DIRECTIONS
            val doRenderSide = v.map(o => f.getWorldObj.getTileEntity(f.xCoord + o.offsetX, f.yCoord + o.offsetY, f.zCoord + o.offsetZ).isInstanceOf[IFluidHandler])
            val internalDoRenderSide = doRenderSide.map(_ ^ true)
            val from = 0.5 - fs.amount / 2001D
            val to = 0.5 + fs.amount / 2001D
            //internal
            drawBox(x, y, z, from, from, from, to, to, to, internalDoRenderSide, icon)
            //down
            if(doRenderSide(0)) drawBox(x, y, z, from, 0.001, from, to, from, to, v.map(_ != ForgeDirection.DOWN), icon)
            //up
            if(doRenderSide(1)) drawBox(x, y, z, from, to, from, to, 0.999, to, v.map(_ != ForgeDirection.UP), icon)
            //north
            if(doRenderSide(2)) drawBox(x, y, z, from, from, 0.001, to, to, from, v.map(_ != ForgeDirection.NORTH), icon)
            //south
            if(doRenderSide(3)) drawBox(x, y, z, from, from, to, to, to, 0.999, v.map(_ != ForgeDirection.SOUTH), icon)
            //west
            if(doRenderSide(4)) drawBox(x, y, z, 0.001, from, from, from, to, to, v.map(_ != ForgeDirection.WEST), icon)
            //east
            if(doRenderSide(5)) drawBox(x, y, z, to, from, from, 0.999, to, to, v.map(_ != ForgeDirection.EAST), icon)
          }
          case _ =>
        }
      }
    }
  }

  def drawBox(x: Double, y: Double, z: Double, fromX: Double, fromY: Double, fromZ: Double, toX: Double, toY: Double, toZ: Double, doRenderSide: Array[Boolean], icon: IIcon) = {
    import org.lwjgl.opengl.GL11._
    val t = Tessellator.instance
    glPushMatrix
    glPushAttrib(GL_ENABLE_BIT)
    glTranslated(x, y, z)
    glDisable(GL_CULL_FACE)
    glDisable(GL_LIGHTING)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glDepthMask(false)
    //down
    if (doRenderSide(0)) {
      t.startDrawingQuads
      t.addVertexWithUV(fromX, fromY, fromZ, icon.getMinU, icon.getMinV)
      t.addVertexWithUV(toX, fromY, fromZ, icon.getMinU, icon.getMaxV)
      t.addVertexWithUV(toX, fromY, toZ, icon.getMaxU, icon.getMaxV)
      t.addVertexWithUV(fromX, fromY, toZ, icon.getMaxU, icon.getMinV)
      t.draw
    }
    //up
    if (doRenderSide(1)) {
      t.startDrawingQuads
      t.addVertexWithUV(fromX, toY, fromZ, icon.getMinU, icon.getMinV)
      t.addVertexWithUV(toX, toY, fromZ, icon.getMinU, icon.getMaxV)
      t.addVertexWithUV(toX, toY, toZ, icon.getMaxU, icon.getMaxV)
      t.addVertexWithUV(fromX, toY, toZ, icon.getMaxU, icon.getMinV)
      t.draw
    }
    //north
    if (doRenderSide(2)) {
      t.startDrawingQuads
      t.addVertexWithUV(fromX, fromY, fromZ, icon.getMinU, icon.getMinV)
      t.addVertexWithUV(fromX, toY, fromZ, icon.getMinU, icon.getMaxV)
      t.addVertexWithUV(toX, toY, fromZ, icon.getMaxU, icon.getMaxV)
      t.addVertexWithUV(toX, fromY, fromZ, icon.getMaxU, icon.getMinV)
      t.draw
    }
    //south
    if (doRenderSide(3)) {
      t.startDrawingQuads
      t.addVertexWithUV(fromX, fromY, toZ, icon.getMinU, icon.getMinV)
      t.addVertexWithUV(fromX, toY, toZ, icon.getMinU, icon.getMaxV)
      t.addVertexWithUV(toX, toY, toZ, icon.getMaxU, icon.getMaxV)
      t.addVertexWithUV(toX, fromY, toZ, icon.getMaxU, icon.getMinV)
      t.draw
    }
    //west
    if (doRenderSide(4)) {
      t.startDrawingQuads
      t.addVertexWithUV(fromX, fromY, fromZ, icon.getMinU, icon.getMinV)
      t.addVertexWithUV(fromX, toY, fromZ, icon.getMinU, icon.getMaxV)
      t.addVertexWithUV(fromX, toY, toZ, icon.getMaxU, icon.getMaxV)
      t.addVertexWithUV(fromX, fromY, toZ, icon.getMaxU, icon.getMinV)
      t.draw
    }
    //east
    if (doRenderSide(5)) {
      t.startDrawingQuads
      t.addVertexWithUV(toX, fromY, fromZ, icon.getMinU, icon.getMinV)
      t.addVertexWithUV(toX, toY, fromZ, icon.getMinU, icon.getMaxV)
      t.addVertexWithUV(toX, toY, toZ, icon.getMaxU, icon.getMaxV)
      t.addVertexWithUV(toX, fromY, toZ, icon.getMaxU, icon.getMinV)
      t.draw
    }

    glEnable(GL_CULL_FACE)
    glEnable(GL_LIGHTING)
    glDisable(GL_BLEND)
    glDepthMask(true)
    glPopAttrib
    glPopMatrix
  }
}
