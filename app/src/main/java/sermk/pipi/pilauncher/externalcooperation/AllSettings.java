package sermk.pipi.pilauncher.externalcooperation;

import android.content.Context;

import com.serenegiant.usb.UVCCamera;

import sermk.pipi.pilauncher.UVCReciver;

/**
 * Created by ser on 02.11.17.
 */

public final class AllSettings {

    private Context gContext;

    void setInstance(final Context context){ this.gContext = context; }

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

    static String NAME_MC_PACKAGE(){
        return "sermk.pipi.mclient";
    }
    static String NAME_MCS_SERVICE(){return "sermk.pipi.mclient.MCSService";}
    static String NAME_STRING_FIELD(){ return "STRING_FIELD";}

}
