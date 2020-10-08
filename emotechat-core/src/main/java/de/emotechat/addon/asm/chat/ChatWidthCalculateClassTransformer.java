package de.emotechat.addon.asm.chat;

import de.emotechat.addon.adapter.mappings.ClassMapping;
import de.emotechat.addon.adapter.mappings.Mappings;
import de.emotechat.addon.adapter.mappings.MethodMapping;
import de.emotechat.addon.asm.PredicateClassNodeTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ChatWidthCalculateClassTransformer extends PredicateClassNodeTransformer {

    private static final ClassMapping GUI_UTIL_MAPPING = Mappings.ACTIVE_MAPPINGS.getGuiUtilRenderComponentsMapping();

    private static final MethodMapping CALCULATE_LINES_MAPPING = Mappings.ACTIVE_MAPPINGS.getCalculateLinesMapping();

    @Override
    public void transform(String name, String transformedName, ClassNode node) {
        boolean obfuscated = name.equals(GUI_UTIL_MAPPING.getObfuscatedName());

        String methodName = obfuscated ? CALCULATE_LINES_MAPPING.getObfuscatedName() : CALCULATE_LINES_MAPPING.getName();
        String methodDesc = obfuscated ? CALCULATE_LINES_MAPPING.getObfuscatedDesc() : CALCULATE_LINES_MAPPING.getDesc();

        for (MethodNode method : node.methods) {
            if (method.name.equals(methodName) && method.desc.equals(methodDesc)) {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                list.add(new VarInsnNode(Opcodes.ILOAD, 3));
                list.add(new VarInsnNode(Opcodes.ILOAD, 4));

                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Mappings.ACTIVE_MAPPINGS.getChatWidthCalculatorClassName(), "calculateLines", methodDesc, false));
                LabelNode label = new LabelNode();
                list.add(new JumpInsnNode(Opcodes.ARETURN, label));
                list.add(label);

                method.instructions.insertBefore(method.instructions.getFirst(), list);
            }
        }

    }

    @Override
    public boolean transforms(String name, String transformedName, byte[] basicClass) {
        return name.equals(GUI_UTIL_MAPPING.getName()) || name.equals(GUI_UTIL_MAPPING.getObfuscatedName());
    }

}
