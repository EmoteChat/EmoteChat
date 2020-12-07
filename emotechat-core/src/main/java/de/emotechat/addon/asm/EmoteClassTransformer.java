package de.emotechat.addon.asm;

import de.emotechat.addon.asm.chat.ChatKeyTypedClassTransformer;
import de.emotechat.addon.asm.chat.ChatMouseClickedTransformer;
import de.emotechat.addon.asm.packet.PacketHandlerClassTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.Arrays;

public class EmoteClassTransformer implements IClassTransformer {

    private static final PredicateClassTransformer[] CLASS_TRANSFORMERS = new PredicateClassTransformer[]{
            new PacketHandlerClassTransformer(),
            new ChatKeyTypedClassTransformer(),
            new ChatMouseClickedTransformer()/*,
            new ChatWidthCalculateClassTransformer()*/ // TODO: fix ASM for 1.12.2 and chat width calculation in general
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
