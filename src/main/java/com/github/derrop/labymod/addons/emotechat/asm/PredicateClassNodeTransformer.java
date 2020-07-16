package com.github.derrop.labymod.addons.emotechat.asm;

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

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

}
