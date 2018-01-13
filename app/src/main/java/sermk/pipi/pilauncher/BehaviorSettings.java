package sermk.pipi.pilauncher;

/**
 * Created by echormonov on 13.01.18.
 */

public class BehaviorSettings {
    public class Run {
        public long TIMEOUT = 1111;
        public int TRY_MAX = 3;
    }

    Run run = new Run();

    public class Tranning {
        public long WARMING_UP_TIME = 1111;
        public long TRANNING_TIME = 1111;
        public int COUNT_TRANNING_FRAMES = 11;
    }

    Tranning tranning = new Tranning();

    public class CaptureFrame {
        public long CAPTURE_WAIT_TIME = 333;
        public long CAPTURE_FRAME_INTERVAL = 1000*60*60*3;
    }

    CaptureFrame captureFrame = new CaptureFrame();
}
