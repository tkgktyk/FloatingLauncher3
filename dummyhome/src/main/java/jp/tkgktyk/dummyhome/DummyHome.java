package jp.tkgktyk.dummyhome;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by tkgktyk on 2015/06/16.
 */
public class DummyHome extends Activity {
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("jp.tkgktyk.floatinglauncher3", "com.android.launcher3.Launcher");
        startActivity(intent);
    }
}
