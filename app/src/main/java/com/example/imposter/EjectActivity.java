package com.example.imposter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

/** Кнопка-переключатель: иконка видна -> выбрасываем; скрыта -> возвращаем. */
public class EjectActivity extends Activity {
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        PackageManager pm = getPackageManager();
        ComponentName cn = new ComponentName(this, LaunchActivity.class);
        boolean shown = pm.getComponentEnabledSetting(cn) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        if (!shown) {
            set(pm, cn, true);
            Toast.makeText(this, R.string.back, Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(0, 0);
            return;
        }
        set(pm, cn, false);           // настоящая иконка исчезает с рабочего стола
        setContentView(new FlyView(this)); // а её "двойник" улетает
    }

    private static void set(PackageManager pm, ComponentName cn, boolean on) {
        pm.setComponentEnabledSetting(cn,
                on ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
