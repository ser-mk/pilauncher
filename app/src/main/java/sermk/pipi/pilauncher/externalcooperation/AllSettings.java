package sermk.pipi.pilauncher.externalcooperation;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.serenegiant.usb.UVCCamera;

import org.opencv.core.Rect;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import sermk.pipi.pilauncher.CVResolver;
import sermk.pipi.pilauncher.UVCReciver;
import sermk.pipi.pilib.CommandCollection;
import sermk.pipi.pilib.ErrorCollector;
import sermk.pipi.pilib.MClient;

/**
 * Created by ser on 02.11.17.
 */

public final class AllSettings {

    final private String TAG = this.getClass().getName();

    private StructSettings currentSettings = new StructSettings();
    private byte[] bytesMask = new byte[0];

    static String NAME_MC_PACKAGE(){return "sermk.pipi.mclient";}
    static String NAME_MCS_SERVICE(){return "sermk.pipi.mlib.MTransmitterService";}
    static String NAME_STRING_FIELD(){ return "STRING_FIELD";}

    private static final String NAME_FILE_ALL_SETTINGS = "all.settings";
    private static final String NAME_FIELD_STRUCT_SETTINGS = "currentSettings";
    private static final String NAME_FILE_MASK = "mask.byte";

    private  static final String subjConfirm = "confirm.settings";
    private SharedPreferences sharedPreferences;

    public void init(final Context context){
        sharedPreferences = context.getSharedPreferences("NAME_FILE_ALL_SETTINGS",Context.MODE_PRIVATE);
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

    public void clear(){
        Log.v(TAG,"clear all settings!");
        sharedPreferences.edit().clear().apply();
    }

    private String loadSettings(final Context context){
        final String json = sharedPreferences.getString(NAME_FIELD_STRUCT_SETTINGS, "");
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

        sharedPreferences.edit().putString(NAME_FIELD_STRUCT_SETTINGS, jsonCurrentSettings()).apply();
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
        PiReceiver.sendBroadCastData(context, CommandCollection.ACTION_RECIVER_PILAUNCHER_SET_SETTINGS, json, byteMask);
        PiReceiver.sendBroadCastData(context, CommandCollection.ACTION_RECIVER_PILAUNCHER_SAVE_SETTINGS, null, null);
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
