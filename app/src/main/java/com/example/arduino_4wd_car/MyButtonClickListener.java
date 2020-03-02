package com.example.arduino_4wd_car;

import android.content.Context;
import android.view.View;

public class MyButtonClickListener implements View.OnClickListener {

    int[] carModeIDs = {R.id.btnCarJoystickMode, R.id.btnCarButtonMode, R.id.btnCarSensorMode};
    int[] carbuttonIDs = {R.id.btnCarTurnLeft, R.id.btnCarTurnRight, R.id.btnCarForward, R.id.btnCarBackward, R.id.btnCarStop, R.id.btnSensorCarStart, R.id.btnSensorCarStop};

    int[] cameraModeIDs = {R.id.btnTiltingTrackingMode, R.id.btnTiltingButtonMode};
    int[] camerabuttonIDs = {R.id.btnTiltingNeutral, R.id.btnTiltingUp, R.id.btnTiltingDown, R.id.btnTiltingLeft, R.id.btnTiltingRight};


    Context context;
    ContextListernerInterface contextListernerInterface;

    public MyButtonClickListener(Context context) {
        this.context = context;
        contextListernerInterface = (ContextListernerInterface) context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCarJoystickMode:
                contextListernerInterface.sendTiltingCommand("CM", 1, 0);
                contextListernerInterface.carModeChanged(v, 1);
                break;
            case R.id.btnCarButtonMode:
                contextListernerInterface.sendTiltingCommand("CM", 2, 0);
                contextListernerInterface.carModeChanged(v, 2);
                break;
            case R.id.btnCarSensorMode:
                contextListernerInterface.sendTiltingCommand("CM", 3, 0);
                contextListernerInterface.carModeChanged(v, 3);
                break;
            case R.id.btnCarTurnLeft:
                contextListernerInterface.sendTiltingCommand("CL", 0, 0);
                break;
            case R.id.btnCarTurnRight:
                contextListernerInterface.sendTiltingCommand("CR", 0, 0);
                break;
            case R.id.btnCarForward:
                contextListernerInterface.sendTiltingCommand("CF", 0, 0);
                break;
            case R.id.btnCarBackward:
                contextListernerInterface.sendTiltingCommand("CB", 0, 0);
                break;
            case R.id.btnCarStop:
                contextListernerInterface.sendTiltingCommand("CS", 0, 0);
                break;
             case R.id.btnSensorCarStart:
                contextListernerInterface.sendTiltingCommand("CA", 1, 0);
                break;
             case R.id.btnSensorCarStop:
                contextListernerInterface.sendTiltingCommand("CA", 0, 0);
                break;
            case R.id.btnTiltingButtonMode:
                contextListernerInterface.sendTiltingCommand("TM", 1, 0);
                contextListernerInterface.tiltingModeChanged(v, 1);
                break;
            case R.id.btnTiltingTrackingMode:
                contextListernerInterface.sendTiltingCommand("TM", 2, 0);
                contextListernerInterface.tiltingModeChanged(v, 2);
                break;
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
        }
    }
}
