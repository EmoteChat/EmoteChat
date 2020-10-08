package de.emotechat.addon;


public class MathHelper {

    public static int floor(float number) {
        int cutNumber = (int) number;
        return number < (float) cutNumber ? cutNumber - 1 : cutNumber;
    }

}
