package net.firsp.lib;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class ChainableTag {

    NBTTagCompound nbt;

    public ChainableTag(NBTTagCompound nbt) {
        this.nbt = nbt;
    }

    public static ChainableTag newInstance() {
        return new ChainableTag(new NBTTagCompound());
    }

    public static ChainableTag of(NBTTagCompound nbt) {
        return new ChainableTag(nbt);
    }

    public NBTTagCompound as() {
        return nbt;
    }

    public ChainableTag setTag(String name, NBTBase nbtBase) {
        nbt.setTag(name, nbtBase);
        return this;
    }

    public ChainableTag tag(String name, NBTBase nbtBase) {
        return setTag(name, nbtBase);
    }

    public ChainableTag setByte(String name, byte b) {
        nbt.setByte(name, b);
        return this;
    }

    public ChainableTag setShort(String name, short s) {
        nbt.setShort(name, s);
        return this;
    }

    public ChainableTag setInteger(String name, int integer) {
        nbt.setInteger(name, integer);
        return this;
    }

    public ChainableTag integer(String name, int integer) {
        return setInteger(name, integer);
    }

    public ChainableTag setLong(String name, long l) {
        nbt.setLong(name, l);
        return this;
    }

    public ChainableTag setFloat(String name, float f) {
        nbt.setFloat(name, f);
        return this;
    }

    public ChainableTag setDouble(String name, double d) {
        nbt.setDouble(name, d);
        return this;
    }

    public ChainableTag setString(String name, String str) {
        nbt.setString(name, str);
        return this;
    }

    public ChainableTag string(String name, String str) {
        return setString(name, str);
    }

    public ChainableTag setByteArray(String name, byte[] arr) {
        nbt.setByteArray(name, arr);
        return this;
    }

    public ChainableTag byteArray(String name, byte[] arr) {
        return setByteArray(name, arr);
    }

    public ChainableTag setIntArray(String name, int[] arr) {
        nbt.setIntArray(name, arr);
        return this;
    }

    public ChainableTag intArray(String name, int[] arr) {
        return setIntArray(name, arr);
    }

    public ChainableTag setBoolean(String name, boolean b) {
        nbt.setBoolean(name, b);
        return this;
    }

    //useful method
    public ChainableTag setCoord(int x, int y, int z) {
        return integer("x", x).integer("y", y).integer("z", z);
    }

    public ChainableTag coord(int x, int y, int z) {
        return setCoord(x, y, z);
    }

}
