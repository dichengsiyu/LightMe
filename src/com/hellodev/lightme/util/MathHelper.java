package com.hellodev.lightme.util;

public class MathHelper {
    /**
     * 弹力动画效果（step：0->100，value:scale->0）[http://khanlou.com/2012/01/cakeyframeanimation-make-it-bounce/]
     * @param step
     * @param scale
     * @return
     */
    public static double bounceValue(long step, long scale){
        double value = scale * java.lang.Math.exp(-0.055 * step) * java.lang.Math.cos(0.08 * step);
        return value;
    }

}
