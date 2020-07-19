package com.github.derrop.labymod.addons.emotechat.asm;

import com.github.derrop.labymod.addons.emotechat.asm.chat.ChatKeyTypedClassTransformer;
import com.github.derrop.labymod.addons.emotechat.asm.chat.ChatWidthCalculateClassTransformer;
import com.github.derrop.labymod.addons.emotechat.asm.packet.PacketHandlerClassTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.Arrays;

public class EmoteClassTransformer implements IClassTransformer {

    private static final PredicateClassTransformer[] CLASS_TRANSFORMERS = new PredicateClassTransformer[]{
            new PacketHandlerClassTransformer(),
            new ChatKeyTypedClassTransformer(),
            new ChatWidthCalculateClassTransformer()
    };

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return Arrays.stream(CLASS_TRANSFORMERS)
                .filter(transformer -> transformer.transforms(name, transformedName, basicClass))
                .findFirst()
                .map(transformer -> transformer.transform(name, transformedName, basicClass))
                .orElse(basicClass);
    }

}
