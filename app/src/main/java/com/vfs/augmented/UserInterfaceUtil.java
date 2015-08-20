package com.vfs.augmented;

import android.content.Context;
import android.view.View;

/**
 * Created by andreia on 19/08/15.
 * HElper class to perform UI actions that are repeated often
 */
public class UserInterfaceUtil
{
    // Changes to clicked state and back after 1sec
    public static void showSkullButtonClick(final Context c, final View view)
    {
        view.setBackground(c.getResources().getDrawable(R.drawable.shape_icon_skull_click));
        new android.os.Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                view.setBackground(c.getResources().getDrawable(R.drawable.shape_icon_skull));
            }
        },500);
    }

    public static void hideView(View view)
    {
        view.setAlpha(0);
    }

    public static void showView(View view)
    {
        view.setAlpha(255);
    }
}
