package de.emotechat.addon;


public class MathHelper {

    public static int floor(float p_floor_0_) {
        int lvt_1_1_ = (int) p_floor_0_;
        return p_floor_0_ < (float) lvt_1_1_ ? lvt_1_1_ - 1 : lvt_1_1_;
    }

}
