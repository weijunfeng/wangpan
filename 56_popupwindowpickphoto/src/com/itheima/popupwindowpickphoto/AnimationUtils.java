package com.itheima.popupwindowpickphoto;

import android.view.View;
import android.view.animation.AlphaAnimation;

/**
 * Created by huotu on 2016/4/27.
 */
public class AnimationUtils
{
    public static void hideAlpha(View viewMask)
    {
        viewMask.setVisibility(View.INVISIBLE);
        AlphaAnimation alpha = new AlphaAnimation(1, 0);
        alpha.setDuration(200);
        viewMask.startAnimation(alpha);
    }
    
    public static void showAlpha(View viewMask)
    {
        viewMask.setVisibility(View.VISIBLE);
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        alpha.setDuration(200);
        viewMask.startAnimation(alpha);
    }
}
