package us.shandian.game.twozero;

import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;

import us.shandian.game.twozero.settings.SettingsProvider;

public class InputListener implements View.OnTouchListener, View.OnKeyListener {

    private static final int SWIPE_MIN_DISTANCE = 0;
    private static int SWIPE_THRESHOLD_VELOCITY = 40;
    private static int MOVE_THRESHOLD = 250;
    private static final int RESET_STARTING = 10;

    private float x;
    private float y;
    private float lastdx;
    private float lastdy;
    private float previousX;
    private float previousY;
    private float startingX;
    private float startingY;
    private int previousDirection = 1;
    private int veryLastDirection = 1;
    private boolean moved = false;

    MainView mView;

    public InputListener(MainView view) {
        super();
        this.mView = view;
    }
    
    public static void loadSensitivity() {
        int sensitivity = SettingsProvider.getInt(SettingsProvider.KEY_SENSITIVITY, 1);
        switch (sensitivity) {
            case 0:
                SWIPE_THRESHOLD_VELOCITY = 20;
                MOVE_THRESHOLD = 200;
                break;
            case 1:
                SWIPE_THRESHOLD_VELOCITY = 60;
                MOVE_THRESHOLD = 250;
                break;
            case 2:
                SWIPE_THRESHOLD_VELOCITY = 100;
                MOVE_THRESHOLD = 300;
                break;
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                startingX = x;
                startingY = y;
                previousX = x;
                previousY = y;
                lastdx = 0;
                lastdy = 0;
                moved = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (MainView.inverseMode) return true;
                x = event.getX();
                y = event.getY();
                if (!mView.game.won && !mView.game.lose) {
                    float dx = x - previousX;
                    
                    // Horizonal
                    if (Math.abs(lastdx + dx) < Math.abs(lastdx) + Math.abs(dx) && Math.abs(dx) > RESET_STARTING
                            &&  Math.abs(x - startingX) > SWIPE_MIN_DISTANCE) {
                        startingX = x;
                        startingY = y;
                        lastdx = dx;
                        previousDirection = veryLastDirection;
                    }
                    if (lastdx == 0) {
                        lastdx = dx;
                    }
                    
                    if (!moved && pathMoved() > SWIPE_MIN_DISTANCE * SWIPE_MIN_DISTANCE) {
                        if (((dx >= SWIPE_THRESHOLD_VELOCITY  && previousDirection == 1) || x - startingX >= MOVE_THRESHOLD) && previousDirection % 5 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 5;
                            veryLastDirection = 5;
                            mView.game.move(1);
                        } else if (((dx <= -SWIPE_THRESHOLD_VELOCITY  && previousDirection == 1) || x - startingX <= -MOVE_THRESHOLD) && previousDirection % 7 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 7;
                            veryLastDirection = 7;
                            mView.game.move(3);
                        }
                    }
                    
                    // Vertical
                    float dy = y - previousY;
                    if (Math.abs(lastdy + dy) < Math.abs(lastdy) + Math.abs(dy) && Math.abs(dy) > RESET_STARTING
                            && Math.abs(y - startingY) > SWIPE_MIN_DISTANCE) {
                        startingX = x;
                        startingY = y;
                        lastdy = dy;
                        previousDirection = veryLastDirection;
                    }
                    if (lastdy == 0) {
                        lastdy = dy;
                    }
                    if (!moved && pathMoved() > SWIPE_MIN_DISTANCE * SWIPE_MIN_DISTANCE) {
                        if (((dy >= SWIPE_THRESHOLD_VELOCITY && previousDirection == 1) || y - startingY >= MOVE_THRESHOLD) && previousDirection % 2 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 2;
                            veryLastDirection = 2;
                            mView.game.move(2);
                        } else if (((dy <= -SWIPE_THRESHOLD_VELOCITY && previousDirection == 1) || y - startingY <= -MOVE_THRESHOLD ) && previousDirection % 3 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 3;
                            veryLastDirection = 3;
                            mView.game.move(0);
                        }
                    }
                    
                    if (moved) {
                        startingX = x;
                        startingY = y;
                    }
                }
                previousX = x;
                previousY = y;
                return true;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                previousDirection = 1;
                veryLastDirection = 1;
                if (!moved && pathMoved() <= MainView.iconSize
                        && inRange(MainView.sXNewGame, x, MainView.sXNewGame + MainView.iconSize)
                        && inRange(MainView.sYIcons, y, MainView.sYIcons + MainView.iconSize)) {
                    mView.game.newGame();
                }
                
                if (mView.inverseMode) {
                    for (Cell cell : mView.game.grid.getAvailableCells()) {
                        int xx = cell.getX();
                        int yy = cell.getY();
                        int sX = mView.startingX + mView.gridWidth + (mView.cellSize + mView.gridWidth) * xx;
                        int eX = sX + mView.cellSize;
                        int sY = mView.startingY + mView.gridWidth + (mView.cellSize + mView.gridWidth) * yy;
                        int eY = sY + mView.cellSize;
                        
                        if (inRange(sX, x, eX) && inRange(sY, y, eY)) {
                            mView.game.addRandomTile(cell);
                            mView.invalidate();
                            mView.startAi();
                            break;
                        }
                    }
                }
        }
        return true;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    mView.game.move(2);
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP:
                    mView.game.move(0);
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    mView.game.move(3);
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    mView.game.move(1);
                    return true;
            }
        }
        return false;
    }

    public float pathMoved() {
        return (x - startingX) * (x - startingX) + (y - startingY) * (y - startingY);
    }

    public boolean inRange(float left, float check, float right) {
        return (left <= check && check <= right);
    }

}
