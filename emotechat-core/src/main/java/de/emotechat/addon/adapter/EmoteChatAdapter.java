package de.emotechat.addon.adapter;


import de.emotechat.addon.adapter.mappings.Mappings;

public interface EmoteChatAdapter {

    void registerChatWidthCalculatorTransformer();

    Mappings getMappings();


}
