package sermk.pipi.pilauncher;

/**
 * Created by echormonov on 13.01.18.
 */

public class BehaviorSettings {
    final public long TIMEOUT_BETWEEN_TRY_UVC_CONNECTION = 1111;
    final public int TRY_MAX_UVC_CONNECTION = 2;

    final public long WARMING_UP_TIME = 1111;
    final public long TRANNING_TIME = 1111;
    final public int COUNT_TRANNING_FRAMES = 11;

    final public long CAPTURE_WAIT_TIME = 333;
    public long CAPTURE_FRAME_INTERVAL = 1000*60*60*3;

    final public int MAX_PULSE_WIDTH = 500;
    final public int MIN_PULSE_WIDTH = 50;
    final public int GAP_DECREASE_MASK = 50; // 1%20

    final public long TIMEOUT_MS_FAIL_CONNECTION = 1000*100;
    final public long FINE_MS_FAIL_CONNECTION = 1000;

}
