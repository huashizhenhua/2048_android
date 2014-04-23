package us.shandian.game.twozero;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Window;
import android.view.WindowManager;

import us.shandian.game.twozero.settings.SettingsProvider;
import us.shandian.game.twozero.settings.SettingsActivity;

public class MainActivity extends Activity {

    public static boolean save = true;
    
    MainView view;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsProvider.initPreferences(this);
        InputListener.loadSensitivity();
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        
        view = new MainView(getBaseContext());
        
        // Restore state
        SharedPreferences prefs = getSharedPreferences("state", Context.MODE_WORLD_READABLE);
        int size = prefs.getInt("size", 0);
        if (size == view.game.numSquaresX) {
            Tile[][] field = view.game.grid.field;
            String[] saveState = new String[field[0].length];
            for (int xx = 0; xx < saveState.length; xx++) {
                saveState[xx] = prefs.getString("" + xx, "");
            }
            for (int xx = 0; xx < saveState.length; xx++) {
                String[] array = saveState[xx].split("\\|");
                for (int yy = 0; yy < array.length; yy++) {
                    if (!array[yy].startsWith("0")) {
                        view.game.grid.field[xx][yy] = new Tile(xx, yy, Integer.valueOf(array[yy]));
                    } else {
                        view.game.grid.field[xx][yy] = null;
                    }
                }
            }
            view.game.score = prefs.getLong("score", 0);
            view.game.highScore = prefs.getLong("high score", 0);
            view.game.won = prefs.getBoolean("won", false);
            view.game.lose = prefs.getBoolean("lose", false);
        }
        setContentView(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        if (view.inverseMode) {
            menu.findItem(R.id.menu_undo).setEnabled(false);
            menu.findItem(R.id.menu_autorun).setEnabled(false);
            menu.findItem(R.id.menu_stopautorun).setEnabled(false);
        } else if (view.aiRunning) {
            menu.findItem(R.id.menu_undo).setEnabled(false);
            menu.findItem(R.id.menu_autorun).setEnabled(false);
            menu.findItem(R.id.menu_stopautorun).setEnabled(true);
        } else {
            menu.findItem(R.id.menu_undo).setEnabled(view.game.grid.canRevert);
            menu.findItem(R.id.menu_autorun).setEnabled(true);
            menu.findItem(R.id.menu_stopautorun).setEnabled(false);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_undo:
                view.game.revertState();
                return true;
            case R.id.menu_settings:
                Intent i = new Intent();
                i.setAction(Intent.ACTION_MAIN);
                i.setClass(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.menu_autorun:
                view.startAi();
                return true;
            case R.id.menu_stopautorun:
                view.stopAi();
                return true;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        
        // If variety switched, do not save
        if (!save) return;
        
        SharedPreferences prefs = getSharedPreferences("state", Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor edit = prefs.edit();
        Tile[][] field = view.game.grid.field;
        String[] saveState = new String[field[0].length];
        for (int xx = 0; xx < field.length; xx++) {
            saveState[xx] = new String();
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] != null) {
                    saveState[xx] += String.valueOf(field[xx][yy].getValue());
                } else {
                    saveState[xx] += "0";
                }
                if (yy < field[0].length - 1) {
                    saveState[xx] += "|";
                }
            }
        }
        for (int xx = 0; xx < saveState.length; xx++) {
            edit.putString("" + xx, saveState[xx]);
        }
        edit.putLong("score", view.game.score);
        edit.putLong("high score", view.game.highScore);
        edit.putBoolean("won", view.game.won);
        edit.putBoolean("lose", view.game.lose);
        edit.putInt("size", view.game.numSquaresX);
        edit.commit();
    }
}
