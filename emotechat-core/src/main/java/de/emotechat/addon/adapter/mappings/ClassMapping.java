package de.emotechat.addon.adapter.mappings;


public class ClassMapping {

    private final String name;

    private final String obfuscatedName;

    public ClassMapping(String name, String obfuscatedName) {
        this.name = name;
        this.obfuscatedName = obfuscatedName;
    }

    public String getName() {
        return name;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

}
