package com.gard.hub.gard;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

/**
 * Created by 重力以太 on 16-1-6.
 */
public class AboutDialog extends AlertDialog {
    public AboutDialog(Context context) {
        super(context);
        final View view = getLayoutInflater().inflate(R.layout.about,
                null);
        setButton(context.getText(R.string.close), (OnClickListener) null);
        setIcon(R.mipmap.ic_launcher);
        setTitle(R.string.about_title);
        setView(view);
    }
}
