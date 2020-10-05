package de.emotechat.addon.asm.chat;


import de.emotechat.addon.asm.PredicateClassNodeTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ChatKeyTypedClassTransformer extends PredicateClassNodeTransformer {

    @Override
    public void transform(String name, String transformedName, ClassNode node) {
        node.methods.stream()
                .filter(methodNode -> methodNode.name.equals("a") && methodNode.desc.equals("(CI)V"))
                .findFirst()
                .ifPresent(methodNode -> {
                    InsnList list = new InsnList();

                    list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                    list.add(new VarInsnNode(Opcodes.ILOAD, 2));
                    list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/emotechat/addon/gui/chat/suggestion/KeyTypedHandler", "blockExecution", "(CI)Z", false));

                    LabelNode labelNode = new LabelNode();
                    list.add(new JumpInsnNode(Opcodes.IFNE, labelNode));

                    methodNode.instructions.insert(methodNode.instructions.getFirst(), list);
                    methodNode.instructions.insertBefore(methodNode.instructions.getLast().getPrevious(), labelNode);
                    methodNode.instructions.insertBefore(labelNode, new FrameNode(Opcodes.F_SAME, 0, null, 1, null));
                });
    }

    @Override
    public boolean transforms(String name, String transformedName, byte[] basicClass) {
        return name.startsWith("net.labymod.ingamechat.tabs")
                || transformedName.startsWith("net.labymod.ingamechat.tabs")
                || name.equals("net.labymod.ingamechat.GuiChatCustom")
                || transformedName.equals("net.labymod.ingamechat.GuiChatCustom");
    }

}
