package jp.tkgktyk.floatinglauncher3;

/**
 * Created by tkgktyk on 2015/06/16.
 */
public class Launcher extends com.android.launcher3.Launcher {
    @Override
    protected void onStop() {
        super.onStop();
        moveTaskToBack(true);
    }
}
