package sermk.pipi.pilauncher.externalcooperation;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.serenegiant.usb.UVCCamera;

import java.io.FileOutputStream;

import sermk.pipi.pilauncher.PiUtils;
import sermk.pipi.pilauncher.UVCReciver;

/**
 * Created by ser on 02.11.17.
 */

public final class AllSettings {

    final private String TAG = this.getClass().getName();

    private Context gContext;
    private StructSettings currentSettings = new StructSettings();
    private byte[] bytesMask = new byte[0];

    static String NAME_MC_PACKAGE(){return "sermk.pipi.mclient";}
    static String NAME_MCS_SERVICE(){return "sermk.pipi.mlib.MTransmitterService";}
    static String NAME_STRING_FIELD(){ return "STRING_FIELD";}

    private static final String NAME_FILE_ALL_SETTINGS = "all.settings";
    private static final String NAME_FIELD_STRUCT_SETTINGS = "currentSettings";
    private static final String NAME_FILE_MASK = "mask.byte";
    private SharedPreferences sharedPreferences;

    public void setInstance(final Context context){
        this.gContext = context;
        sharedPreferences = context.getSharedPreferences("NAME_FILE_ALL_SETTINGS",Context.MODE_PRIVATE);
    }

    private static AllSettings instance = null;

    static public AllSettings getInstance(){
        if (instance == null){
            instance = new AllSettings();
        }
        return instance;
    }

    public void setUVCSettings(UVCReciver.Settings settings){
        settings.width = UVCCamera.DEFAULT_PREVIEW_WIDTH;
        settings.height = UVCCamera.DEFAULT_PREVIEW_HEIGHT;;
        settings.minFps = 25;
        settings.maxFps = 30;
        settings.frameformat = UVCCamera.FRAME_FORMAT_YUYV;
        settings.bandwightFactor = 1.0f;
    }

    public void setCurrentSettings( String json, byte[] bytesMask) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        StructSettings tempSettings =  gson.fromJson(json, StructSettings.class);
        if(tempSettings != null ){
            this.currentSettings = tempSettings;
        }
        if (bytesMask != null){
            this.bytesMask = bytesMask;
        }
    }

    public void saveCurrentSettings(Context context){

        sharedPreferences.edit().putString(NAME_FIELD_STRUCT_SETTINGS, jsonCurrentSettings()).apply();
        try(FileOutputStream outputStream
                = context.openFileOutput(NAME_FILE_MASK, Context.MODE_PRIVATE) ) {
            outputStream.write(bytesMask);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(TAG, "save mask in " + NAME_FILE_MASK);
    }

    private String jsonCurrentSettings(){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        final String json = gson.toJson(currentSettings);
        return json;
    }


    public boolean saveMaskInReceiver(Context context, final byte[] byteMask, final PiUtils.RectMask rm){
        currentSettings.rectMask = rm;
        final String json = jsonCurrentSettings();
        Log.v(TAG, "json = " + json);
        PiReceiver.sendBroadCastData(gContext, PiReceiver.ACTION_RECIVER_SET_SETTINGS, json, byteMask);
        PiReceiver.sendBroadCastData(gContext, PiReceiver.ACTION_RECIVER_SAVE_SETTINGS, null, null);
        return true;
    }

}
