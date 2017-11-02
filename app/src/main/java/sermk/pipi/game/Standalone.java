package sermk.pipi.game;

import android.app.Activity;
import android.os.Bundle;

import com.orhanobut.logger.Logger;

public class Standalone extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standalone);

        getFragmentManager().beginTransaction()
                .add(R.id.container, new TestCV_Fragment()).commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
