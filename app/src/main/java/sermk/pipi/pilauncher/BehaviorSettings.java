package sermk.pipi.pilauncher;

/**
 * Created by echormonov on 13.01.18.
 */

public class BehaviorSettings {
    final public long TIMEOUT_BETWEEN_TRY_UVC_CONNECTION = 1000;
    final public int TRY_MAX_UVC_CONNECTION = 2;

    final public long WARMING_UP_TIME = 1000;
    final public long TRANNING_TIME = 1000;
    final public int COUNT_TRANNING_FRAMES = 10;

    final public long CAPTURE_WAIT_TIME = 300;
    final public long CAPTURE_FRAME_INTERVAL_PREV = 1000*60*60*12;
    public long CAPTURE_FRAME_INTERVAL = 1000*60*60*12;

    final public long VIBRATE_TIME_MS = 2*1000;

    final public int GAP_DECREASE_MASK = 50; // 1%20 from 1000
    public int MAX_PULSE_WIDTH = 500; //from 1000
    public int MIN_PULSE_WIDTH = 50; // from 1000

    final public long TIMEOUT_MS_FAIL_CONNECTION = 1000*60*10;
    final public long FINE_MS_FAIL_CONNECTION = 1000;

    final public String NAME_GAME_PACKAGE = "ser.pipi.piball";
    final public String NAME_GAME_ACTIVITY = "ser.pipi.piball.AndroidLauncher";
}
