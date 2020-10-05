package de.emotechat.addon.adapter.mappings;


public class MethodMapping extends ClassMapping {

    private final String desc;

    private final String obfuscatedDesc;

    public MethodMapping(String name, String obfuscatedName, String desc, String obfuscatedDesc) {
        super(name, obfuscatedName);
        this.desc = desc;
        this.obfuscatedDesc = obfuscatedDesc;
    }

    public String getDesc() {
        return desc;
    }

    public String getObfuscatedDesc() {
        return obfuscatedDesc;
    }

}
