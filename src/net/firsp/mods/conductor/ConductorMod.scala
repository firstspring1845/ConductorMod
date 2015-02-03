package net.firsp.mods.conductor

import cpw.mods.fml.common.{SidedProxy, Mod}
import cpw.mods.fml.common.Mod.{Instance, EventHandler}
import cpw.mods.fml.common.event.{FMLServerStoppingEvent, FMLServerStartingEvent, FMLInitializationEvent}
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import net.minecraft.util.StringTranslate
import net.minecraftforge.common.ForgeChunkManager

@Mod(modid = "Conductor", modLanguage = "scala")
class ConductorMod {
  @EventHandler
  def init(event: FMLInitializationEvent) = {
    ConductorMod.instance = this
    GameRegistry.registerTileEntity(classOf[TileEnergyConductor], "firsp:energyConductor")
    GameRegistry.registerTileEntity(classOf[TileFluidConductor], "firsp:fluidConductor")
    GameRegistry.registerTileEntity(classOf[TileTeleportEnergyConductor], "firsp:teleportEnergyConductor")
    GameRegistry.registerTileEntity(classOf[TileTeleportFluidConductor], "firsp:teleportFluidConductor")
    GameRegistry.registerTileEntity(classOf[TileEnergyCapacitor], "firsp:energyCapacitor")

    val eConductor = new BlockConductor({
      new TileEnergyConductor
    })
    eConductor.setBlockName("econd")
    eConductor.getUnlocalizedName
    eConductor.setBlockTextureName("firsp_cond:econd")
    GameRegistry.registerBlock(eConductor, "econd")

    val eConductorTeleport = new BlockConductorTeleport({
      new TileTeleportEnergyConductor
    })
    eConductorTeleport.setBlockName("econd_t")
    eConductorTeleport.setBlockTextureName("firsp_cond:econd_t")
    GameRegistry.registerBlock(eConductorTeleport, "econd_t")

    val fConductor = new BlockConductor({
      new TileFluidConductor
    })
    fConductor.setBlockName("fcond")
    fConductor.setBlockTextureName("firsp_cond:fcond")
    GameRegistry.registerBlock(fConductor, "fcond")

    val fConductorTeleport = new BlockConductorTeleport({
      new TileTeleportFluidConductor
    })
    fConductorTeleport.setBlockName("fcond_t")
    fConductorTeleport.setBlockTextureName("firsp_cond:fcond_t")
    GameRegistry.registerBlock(fConductorTeleport, "fcond_t")

    val anchor = new BlockAnchor
    ConductorMod.anchor = anchor
    GameRegistry.registerBlock(anchor, "firsp_anchor")
    ForgeChunkManager.setForcedChunkLoadingCallback(this, AnchorChunkLoadCallback)

    val capacitor = new BlockEnergyCapacitor
    GameRegistry.registerBlock(capacitor, classOf[ItemEnergyCapacitor], "firsp_capacitor")

    GameRegistry.addShapedRecipe(new ItemStack(fConductor, 8),
      "iii",
      "igi",
      "iii",
      Character.valueOf('i'),
      Items.iron_ingot,
      Character.valueOf('g'),
      Blocks.glass)
    GameRegistry.addShapelessRecipe(new ItemStack(eConductor, 1), fConductor, Items.redstone)
    GameRegistry.addShapedRecipe(new ItemStack(fConductorTeleport, 8),
      "fff",
      "fdf",
      "fff",
      Character.valueOf('f'),
      fConductor,
      Character.valueOf('d'),
      Items.diamond)
    GameRegistry.addShapedRecipe(new ItemStack(eConductorTeleport, 8),
      "eee",
      "ede",
      "eee",
      Character.valueOf('e'),
      eConductor,
      Character.valueOf('d'),
      Items.diamond)
    GameRegistry.addShapedRecipe(new ItemStack(anchor, 8),
      "igi",
      "ggg",
      "igi",
      Character.valueOf('i'),
      Items.gold_ingot,
      Character.valueOf('g'),
      Blocks.glass)
    GameRegistry.addShapedRecipe(new ItemStack(capacitor, 1, 1000),
      "rrr",
      "rgr",
      "rrr",
      Character.valueOf('r'),
      Blocks.redstone_block,
      Character.valueOf('g'),
      Blocks.glass)
    ConductorMod.proxy.registerTESR
  }

  @EventHandler
  def serverStarting(event: FMLServerStartingEvent) = TeleportManager.teleportables.clear

  @EventHandler
  def serverStopping(event: FMLServerStoppingEvent) = TeleportManager.teleportables.clear
}

object ConductorMod extends ConductorMod {
  var instance: ConductorMod = _
  var anchor: BlockAnchor = _
  @SidedProxy(clientSide = "net.firsp.mods.conductor.ClientProxy", serverSide = "net.firsp.mods.conductor.CommonProxy")
  var proxy:CommonProxy = _
}
