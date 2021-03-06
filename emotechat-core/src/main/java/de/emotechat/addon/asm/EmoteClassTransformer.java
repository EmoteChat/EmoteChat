package de.emotechat.addon.asm;

import de.emotechat.addon.asm.chat.ChatKeyTypedClassTransformer;
import de.emotechat.addon.asm.chat.ChatMouseClickedTransformer;
import de.emotechat.addon.asm.chat.sending.SendMessageClassTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class EmoteClassTransformer implements IClassTransformer {

    private static final PredicateClassTransformer[] CLASS_TRANSFORMERS = new PredicateClassTransformer[]{
            new SendMessageClassTransformer(),
            new ChatKeyTypedClassTransformer(),
            new ChatMouseClickedTransformer()/*,
            new ChatWidthCalculateClassTransformer()*/ // TODO: fix ASM for 1.12.2 and chat width calculation in general
    };

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        for (PredicateClassTransformer classTransformer : CLASS_TRANSFORMERS) {
            if (classTransformer.transforms(name, transformedName, basicClass)) {
                basicClass = classTransformer.transform(name, transformedName, basicClass);
            }
        }

        return basicClass;
    }

}
