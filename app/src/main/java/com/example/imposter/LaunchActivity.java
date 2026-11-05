package com.example.imposter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.CrossProfileApps;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Запускает ru.oneme.app, где бы он ни стоял. Порядок попыток:
 * 1. тот же профиль, что и у нас (основной, либо Папка Knox, если эта копия imposter стоит внутри неё);
 * 2. LauncherApps: чужие профили, которые система показывает приложению
 *    (Папка Knox на One UI 7 и старше — это managed-профиль);
 * 3. CrossProfileApps: запускаем свою копию imposter внутри Папки Knox, она откроет MAX у себя;
 * 4. открываем саму Папку Knox, MAX внутри выбирается вручную
 *    (на One UI 8+ Папка Knox — private-профиль, снаружи он закрыт для обычных приложений).
 */
public class LaunchActivity extends Activity {
    static final String TAG = "imposter";
    static final String TARGET = "ru.oneme.app";
    static final String KNOX = "com.samsung.knox.securefolder";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            go();
        } catch (Throwable t) {
            Log.w(TAG, "launch failed", t);
            toast(R.string.missing);
        }
        finish();
    }

    private void go() {
        // 1. Тот же профиль
        Intent i = getPackageManager().getLaunchIntentForPackage(TARGET);
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            Log.i(TAG, "tier1: same profile");
            return;
        }
        // 2. Другие профили через LauncherApps
        if (viaLauncherApps()) { Log.i(TAG, "tier2: LauncherApps"); return; }
        // 3. Своя копия внутри другого профиля
        if (viaRelay()) { Log.i(TAG, "tier3: CrossProfileApps relay"); return; }
        // 4. Просто открыть Папку Knox
        Intent k = getPackageManager().getLaunchIntentForPackage(KNOX);
        if (k != null) {
            k.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(k);
            Log.i(TAG, "tier4: open Knox folder");
            toast(R.string.open_in_knox);
            return;
        }
        toast(R.string.missing);
    }

    private boolean viaLauncherApps() {
        try {
            LauncherApps la = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
            if (la == null) return false;
            UserHandle me = Process.myUserHandle();
            for (UserHandle u : la.getProfiles()) {
                if (u.equals(me)) continue;
                List<LauncherActivityInfo> l = la.getActivityList(TARGET, u);
                if (l == null || l.isEmpty()) continue;
                la.startMainActivity(l.get(0).getComponentName(), u, null, null);
                return true;
            }
        } catch (Throwable t) {
            Log.w(TAG, "LauncherApps failed", t);
        }
        return false;
    }

    /** Только из основного профиля, чтобы копии не гоняли запуск друг другу по кругу. */
    private boolean viaRelay() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || inProfile()) return false;
        try {
            CrossProfileApps cpa = getSystemService(CrossProfileApps.class);
            if (cpa == null) return false;
            List<UserHandle> targets = cpa.getTargetUserProfiles();
            if (targets == null || targets.isEmpty()) return false;
            cpa.startMainActivity(new ComponentName(this, LaunchActivity.class), targets.get(0));
            return true;
        } catch (Throwable t) {
            Log.w(TAG, "CrossProfileApps failed", t);
        }
        return false;
    }

    private boolean inProfile() {
        UserManager um = getSystemService(UserManager.class);
        if (um == null) return false;
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? um.isProfile() : um.isManagedProfile();
    }

    private void toast(int res) { Toast.makeText(this, res, Toast.LENGTH_LONG).show(); }
}
