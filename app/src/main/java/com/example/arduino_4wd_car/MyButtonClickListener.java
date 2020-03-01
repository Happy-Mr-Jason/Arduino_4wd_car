package com.example.arduino_4wd_car;

import android.content.Context;
import android.view.View;

public class MyButtonClickListener implements View.OnClickListener {

    int[] camerabuttonIDs = {R.id.btnTiltingNeutral, R.id.btnTiltingUp, R.id.btnTiltingDown, R.id.btnTiltingLeft, R.id.btnTiltingRight, R.id.btnTracking};

    Context context;
    ContextListernerInterface contextListernerInterface;

    public MyButtonClickListener(Context context) {
        this.context = context;
        contextListernerInterface = (ContextListernerInterface)context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnTiltingNeutral:
                contextListernerInterface.sendTiltingCommand("TN", 0, 0);
                break;
        case R.id.btnTiltingUp:
                contextListernerInterface.sendTiltingCommand("TU", 0, 0);
                break;
        case R.id.btnTiltingDown:
                contextListernerInterface.sendTiltingCommand("TD", 0, 0);
                break;
        case R.id.btnTiltingLeft:
                contextListernerInterface.sendTiltingCommand("TL", 0, 0);
                break;
        case R.id.btnTiltingRight:
                contextListernerInterface.sendTiltingCommand("TR", 0, 0);
                break;
        case R.id.btnTracking:
                contextListernerInterface.sendTiltingCommand("TT", 0, 0);
                break;
        }
    }
}
