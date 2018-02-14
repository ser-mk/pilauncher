package sermk.pipi.pilauncher.externalcooperation;

import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import sermk.pipi.pilauncher.GlobalController;
import sermk.pipi.pilauncher.LauncherAct;
import sermk.pipi.pilauncher.R;
import sermk.pipi.pilib.ProblemStatusAPI;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by ser on 09.02.18.
 */
@RunWith(AndroidJUnit4.class)
public class ProblemReceiverTest {
    @Rule
    public ActivityTestRule<LauncherAct> ar = new ActivityTestRule<>(LauncherAct.class);

    @Test
    public void condsiderProblem() throws Exception {
        final int TIMEOUT = 1555;
        final String val1 = "11";
        final String val2 = "22";
        ProblemStatusAPI.setStatus(ar.getActivity(),val2);
        ProblemStatusAPI.setStatus(ar.getActivity(),val1);
        SystemClock.sleep(TIMEOUT);
        GlobalController gb = (GlobalController) ar.getActivity().getApplicationContext();
        HashSet<String> hs = gb.problem.problemSet();
        System.out.println(hs.toString());
        assertEquals(2, hs.size());
        assertTrue(hs.contains(val2));
        assertTrue(hs.contains(val1));

        ProblemStatusAPI.clearStatus(ar.getActivity(),val1);
        SystemClock.sleep(TIMEOUT);
        assertFalse(hs.contains(val1));
        assertTrue(hs.contains(val2));
        assertEquals(1, hs.size());
        System.out.println(hs.toString());

        LauncherAct la = (LauncherAct)ar.getActivity();
        la.addFragment(LauncherAct.Screen.Status);
        SystemClock.sleep(TIMEOUT);
        onView(withId(R.id.status)).check(matches(withText(val2)));
    }

}