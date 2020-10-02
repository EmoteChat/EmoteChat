package com.github.emotechat.addon.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public abstract class PredicateClassNodeTransformer implements PredicateClassTransformer {

    public abstract void transform(String name, String transformedName, ClassNode node);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        this.transform(name, transformedName, node);

        int flags = name.contains("GuiChatSymbols") ? ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS;
        ClassWriter writer = new ClassWriter(flags);

        node.accept(writer);

        return writer.toByteArray();
    }

}
