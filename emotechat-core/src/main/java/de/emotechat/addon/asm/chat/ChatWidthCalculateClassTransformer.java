package de.emotechat.addon.asm.chat;

import de.emotechat.addon.asm.PredicateClassNodeTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ChatWidthCalculateClassTransformer extends PredicateClassNodeTransformer {

    @Override
    public void transform(String name, String transformedName, ClassNode node) {
        boolean obfuscated = name.equals("avu");

        String methodName = obfuscated ? "a" : "func_178908_a";
        String methodDesc = obfuscated ? "(Leu;ILavn;ZZ)Ljava/util/List;" : "(Lnet.minecraft.util.IChatComponent;ILnet.minecraft.client.gui.FontRenderer;ZZ)Ljava/util/List;";

        for (MethodNode method : node.methods) {
            if (method.name.equals(methodName) && method.desc.equals(methodDesc)) {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                list.add(new VarInsnNode(Opcodes.ILOAD, 3));
                list.add(new VarInsnNode(Opcodes.ILOAD, 4));

                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/emotechat/addon/gui/chat/render/ChatWidthCalculator", "calculateLines", methodDesc, false));
                LabelNode label = new LabelNode();
                list.add(new JumpInsnNode(Opcodes.ARETURN, label));
                list.add(label);

                method.instructions.insertBefore(method.instructions.getFirst(), list);
            }
        }

    }

    @Override
    public boolean transforms(String name, String transformedName, byte[] basicClass) {
        return name.equals("net.minecraft.client.gui.GuiUtilRenderComponents") || name.equals("avu");
    }

}
