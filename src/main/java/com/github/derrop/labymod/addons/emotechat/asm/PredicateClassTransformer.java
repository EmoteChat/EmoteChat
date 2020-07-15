package com.github.derrop.labymod.addons.emotechat.asm;


import net.minecraft.launchwrapper.IClassTransformer;

public interface PredicateClassTransformer extends IClassTransformer {

    boolean transforms(String name, String transformedName, byte[] basicClass);

}
