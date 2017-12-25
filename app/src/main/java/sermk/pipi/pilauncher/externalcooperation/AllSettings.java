package sermk.pipi.pilauncher.externalcooperation;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.serenegiant.usb.UVCCamera;

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

    static private final String CURRENT_SETTINGS_NAME_FIELD = "last_all_settings";

    public void setInstance(final Context context){ this.gContext = context; }

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

    private String jsonCurrentSettings(){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        final String json = gson.toJson(currentSettings);
        return json;
    }


    public boolean saveMask(final PiUtils.RectMask rm, final byte[] byteMask){
        currentSettings.rectMask = rm;
        final String json = jsonCurrentSettings();
        Log.v(TAG, "json = " + json);
        PiReceiver.sendBroadCastData(gContext, PiReceiver.ACTION_RECIVER_SET_SETTINGS, json, byteMask);
        return true;
    }

}
