package com.dolinsek.elias.cashcockpit.components;

import android.content.Context;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.R;

/**
 * Created by Elias Dolinsek on 06.07.2018 for cash-cockpit.
 */
public class Toolkit {

    public static void displayPleaseCheckInputsToast(Context context){
        Toast.makeText(context, context.getString(R.string.toast_please_check_inputs), Toast.LENGTH_SHORT).show();
    }
}
