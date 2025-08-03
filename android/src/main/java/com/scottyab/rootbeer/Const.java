package com.scottyab.rootbeer;

import java.util.ArrayList;
import java.util.Arrays;

/* loaded from: classes.jar:com/scottyab/rootbeer/Const.class */
final class Const {
    static final String BINARY_SU = "su";
    static final String BINARY_BUSYBOX = "busybox";
    static final String[] knownRootAppsPackages = {"com.noshufou.android.su", "com.noshufou.android.su.elite", "eu.chainfire.supersu", "com.koushikdutta.superuser", "com.thirdparty.superuser", "com.yellowes.su", "com.topjohnwu.magisk", "com.kingroot.kinguser", "com.kingo.root", "com.smedialink.oneclickroot", "com.zhiqupk.root.global", "com.alephzain.framaroot"};
    public static final String[] knownDangerousAppsPackages = {"com.koushikdutta.rommanager", "com.koushikdutta.rommanager.license", "com.dimonvideo.luckypatcher", "com.chelpus.lackypatch", "com.ramdroid.appquarantine", "com.ramdroid.appquarantinepro", "com.android.vending.billing.InAppBillingService.COIN", "com.android.vending.billing.InAppBillingService.LUCK", "com.chelpus.luckypatcher", "com.blackmartalpha", "org.blackmart.market", "com.allinone.free", "com.repodroid.app", "org.creeplays.hack", "com.baseappfull.fwd", "com.zmapp", "com.dv.marketmod.installer", "org.mobilism.android", "com.android.wp.net.log", "com.android.camera.update", "cc.madkite.freedom", "com.solohsu.android.edxp.manager", "org.meowcat.edxposed.manager", "com.xmodgame", "com.cih.game_cih", "com.charles.lpoqasert", "catch_.me_.if_.you_.can_"};
    public static final String[] knownRootCloakingPackages = {"com.devadvance.rootcloak", "com.devadvance.rootcloakplus", "de.robv.android.xposed.installer", "com.saurik.substrate", "com.zachspong.temprootremovejb", "com.amphoras.hidemyroot", "com.amphoras.hidemyrootadfree", "com.formyhm.hiderootPremium", "com.formyhm.hideroot"};
    private static final String[] suPaths = {"/data/local/", "/data/local/bin/", "/data/local/xbin/", "/sbin/", "/su/bin/", "/system/bin/", "/system/bin/.ext/", "/system/bin/failsafe/", "/system/sd/xbin/", "/system/usr/we-need-root/", "/system/xbin/", "/cache/", "/data/", "/dev/"};
    static final String[] pathsThatShouldNotBeWritable = {"/system", "/system/bin", "/system/sbin", "/system/xbin", "/vendor/bin", "/sbin", "/etc"};

    private Const() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    static String[] getPaths() {
        ArrayList<String> paths = new ArrayList<>(Arrays.asList(suPaths));
        String sysPaths = System.getenv("PATH");
        if (sysPaths == null || "".equals(sysPaths)) {
            return (String[]) paths.toArray(new String[0]);
        }
        String[] strArrSplit = sysPaths.split(":");
        int length = strArrSplit.length;
        for (int i = 0; i < length; i++) {
            String path = strArrSplit[i];
            if (!path.endsWith("/")) {
                path = path + '/';
            }
            if (!paths.contains(path)) {
                paths.add(path);
            }
        }
        return (String[]) paths.toArray(new String[0]);
    }
}
