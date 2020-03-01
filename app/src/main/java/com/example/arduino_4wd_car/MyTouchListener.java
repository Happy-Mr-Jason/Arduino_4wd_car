package com.example.arduino_4wd_car;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

public class MyTouchListener implements View.OnTouchListener {
    private final Context context;
    private float startPositionX;
    private float startPositionY;

    private float startEventPosX;
    private float startEventPosY;
    private float moveAmountX;
    private float moveAmountY;
    private ContextListernerInterface contextListernerInterface;

    public MyTouchListener(Context context) {
        this.context = context;
        contextListernerInterface = (ContextListernerInterface)context;
    }

    public float getMoveAmountX() {
        return moveAmountX;
    }

    public float getMoveAmountY() {
        return moveAmountY;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startEventPosX = event.getX();
                startEventPosY = event.getY();
                startPositionX = v.getX();
                startPositionY = v.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveAmountX = event.getX() - startEventPosX;
                moveAmountY = event.getY() - startEventPosY;
                v.setX(startPositionX + moveAmountX);
                v.setY(startPositionY + moveAmountY);
                break;
            case MotionEvent.ACTION_UP:
                v.setX(startPositionX);
                v.setY(startPositionY);
                moveAmountX = 0;
                moveAmountY = 0;
                break;
        }

        try {
            contextListernerInterface = (ContextListernerInterface)context;
            contextListernerInterface.sendMovePosition(moveAmountX, -moveAmountY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}