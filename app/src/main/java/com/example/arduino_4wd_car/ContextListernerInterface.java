package com.example.arduino_4wd_car;

import android.view.View;

public interface ContextListernerInterface {
    void sendMovePosition(float positionX, float positionY);
    void sendTiltingCommand(String cmd, int data1, int data2);
    void carModeChanged(View selectedButton, int selectedMode);
    void tiltingModeChanged(View selectedButton, int selectedMode);
}
