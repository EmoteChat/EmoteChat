package de.emotechat.addon.asm.chat;


import de.emotechat.addon.asm.PredicateClassNodeTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ChatMouseClickedTransformer extends PredicateClassNodeTransformer {

    @Override
    public void transform(String name, String transformedName, ClassNode node) {
        node.methods.stream()
                .filter(methodNode -> methodNode.name.equals("a") && methodNode.desc.equals("(III)V"))
                .findFirst()
                .ifPresent(methodNode -> {
                    InsnList list = new InsnList();

                    list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                    list.add(new VarInsnNode(Opcodes.ILOAD, 2));
                    list.add(new VarInsnNode(Opcodes.ILOAD, 3));
                    list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/emotechat/addon/gui/chat/MouseClickedHandler", "mouseClicked", "(III)V", false));

                    methodNode.instructions.insert(methodNode.instructions.getFirst(), list);
                });
    }

    @Override
    public boolean transforms(String name, String transformedName, byte[] basicClass) {
        return name.equals("net.labymod.ingamegui.ModuleGui") || transformedName.equals("net.labymod.ingamegui.ModuleGui");
    }

}
