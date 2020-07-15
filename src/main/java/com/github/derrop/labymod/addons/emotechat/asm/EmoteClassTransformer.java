package com.github.derrop.labymod.addons.emotechat.asm;

import com.github.derrop.labymod.addons.emotechat.asm.packet.PacketHandlerClassTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.Collections;
import java.util.List;

public class EmoteClassTransformer implements IClassTransformer {

    private static final List<PredicateClassTransformer> CLASS_TRANSFORMERS = Collections.singletonList(
            new PacketHandlerClassTransformer()
    );

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return CLASS_TRANSFORMERS.stream()
                .filter(transformer -> transformer.transforms(name, transformedName, basicClass))
                .findFirst()
                .map(transformer -> transformer.transform(name, transformedName, basicClass))
                .orElse(basicClass);
    }

}
