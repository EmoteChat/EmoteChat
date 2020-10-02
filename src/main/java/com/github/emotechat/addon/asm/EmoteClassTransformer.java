package com.github.emotechat.addon.asm;

import com.github.emotechat.addon.asm.chat.ChatKeyTypedClassTransformer;
import com.github.emotechat.addon.asm.chat.ChatWidthCalculateClassTransformer;
import com.github.emotechat.addon.asm.packet.PacketHandlerClassTransformer;
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
