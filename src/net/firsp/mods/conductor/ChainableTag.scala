package net.firsp.mods.conductor

import net.minecraft.nbt.NBTTagCompound

/**
 * Created by owner on 2015/01/26.
 */
class ChainableTag(val nbt:NBTTagCompound = new NBTTagCompound) {
  def as = nbt
  def integer(name:String, value:Int) = {
    nbt.setInteger(name,value)
    this
  }
}

object ChainableTag{
  def newInstance = new ChainableTag
  def of(nbt:NBTTagCompound) = new ChainableTag(nbt)
}