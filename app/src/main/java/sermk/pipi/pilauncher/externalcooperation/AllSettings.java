package sermk.pipi.pilauncher.externalcooperation;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.opencv.core.Rect;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import sermk.pipi.pilib.CommandCollection;
import sermk.pipi.pilib.ErrorCollector;
import sermk.pipi.pilib.MClient;
import sermk.pipi.pilib.PiUtils;

/**
 * Created by ser on 02.11.17.
 */

public final class AllSettings {

    final private String TAG = this.getClass().getName();

    private StructSettings currentSettings = new StructSettings();
    private byte[] bytesMask = new byte[0];

    private static final String NAME_FILE_MASK = "mask.byte";

    private  static final String subjConfirm = "confirm.settings";

    public void init(final Context context){
        loadSettings(context);
    }

    private static AllSettings instance = null;

    static public AllSettings getInstance(){
        if (instance == null){
            instance = new AllSettings();
        }
        return instance;
    }

    public StructSettings getCurrentSettings(){
        return  currentSettings;
    }
    public byte[] getBytesMask(){ return  bytesMask; }

    public String setCurrentSettings( String json, byte[] bytesMask) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        StructSettings tempSettings =  gson.fromJson(json, StructSettings.class);
        if (bytesMask != null){
            this.bytesMask = bytesMask;
        } else {
            final String err ="setted byte mask not exist!";
            Log.e(TAG, err);
            return err;
        }
        if(tempSettings != null ){ // todo check correct subclass!
            this.currentSettings = tempSettings;
        } else {
            final String err = "setted Struct Settings not exist!";
            Log.e(TAG, err);
            return err;
        }
        return ErrorCollector.NO_ERROR;
    }

    public void clear(final Context context){
        Log.v(TAG,"clear all settings!");
        PiUtils.clearJson(context);
    }

    private String loadSettings(final Context context){
        final String json = PiUtils.getJsonFromShared(context);
        Log.i(TAG, "load settings : " + json );
        byte[] bytes = new byte[0];
        try(FileInputStream inputStream = context.openFileInput(NAME_FILE_MASK)){
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ErrorCollector.getStackTraceString(e);
        } catch (IOException e) {
            e.printStackTrace();
            return ErrorCollector.getStackTraceString(e);
        }
        return setCurrentSettings(json, bytes);
    }

    public String saveCurrentSettings(Context context){

        PiUtils.saveJson(context,jsonCurrentSettings());
        try(FileOutputStream outputStream
                = context.openFileOutput(NAME_FILE_MASK, Context.MODE_PRIVATE) ) {
            outputStream.write(bytesMask);
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorCollector.getStackTraceString(e);
        }
        Log.v(TAG, "save mask in " + NAME_FILE_MASK);
        return ErrorCollector.NO_ERROR;
    }

    private String jsonCurrentSettings(){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        final String json = gson.toJson(currentSettings);
        return json;
    }


    public boolean saveMaskInReceiver(Context context, final byte[] byteMask, final Rect rm){
        currentSettings.rectMask = rm;
        final String json = jsonCurrentSettings();
        Log.v(TAG, "json = " + json);
        SettingsReciever.sendBroadCastData(context, CommandCollection.ACTION_RECIVER_PILAUNCHER_SET_SETTINGS, json, byteMask);
        SettingsReciever.sendBroadCastData(context, CommandCollection.ACTION_RECIVER_PILAUNCHER_SAVE_SETTINGS, null, null);
        return true;
    }

    public boolean confirmSettings(Context context){
        final String json = jsonCurrentSettings();
        return MClient.sendMessage(context, subjConfirm,
            json, bytesMask);
    }

    public boolean confirmSettings(Context context, final byte[] frame){
        final String json = jsonCurrentSettings();
        return MClient.sendMessage(context, subjConfirm,
            json, frame);
    }

}
