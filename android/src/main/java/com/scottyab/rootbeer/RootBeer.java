package com.scottyab.rootbeer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.scottyab.rootbeer.util.QLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/* loaded from: classes.jar:com/scottyab/rootbeer/RootBeer.class */
public class RootBeer {
    private final Context mContext;
    private boolean loggingEnabled = true;

    public RootBeer(Context context) {
        this.mContext = context;
    }

    public boolean isRooted() throws Throwable {
        return detectRootManagementApps() || detectPotentiallyDangerousApps() || checkForBinary("su") || checkForDangerousProps() || checkForRWPaths() || detectTestKeys() || checkSuExists() || checkForRootNative() || checkForMagiskBinary();
    }

    @Deprecated
    public boolean isRootedWithoutBusyBoxCheck() throws Throwable {
        return isRooted();
    }

    public boolean isRootedWithBusyBoxCheck() throws Throwable {
        return detectRootManagementApps() || detectPotentiallyDangerousApps() || checkForBinary("su") || checkForBinary("busybox") || checkForDangerousProps() || checkForRWPaths() || detectTestKeys() || checkSuExists() || checkForRootNative() || checkForMagiskBinary();
    }

    public boolean detectTestKeys() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    public boolean detectRootManagementApps() throws PackageManager.NameNotFoundException {
        return detectRootManagementApps(null);
    }

    public boolean detectRootManagementApps(String[] additionalRootManagementApps) throws PackageManager.NameNotFoundException {
        ArrayList<String> packages = new ArrayList<>(Arrays.asList(Const.knownRootAppsPackages));
        if (additionalRootManagementApps != null && additionalRootManagementApps.length > 0) {
            packages.addAll(Arrays.asList(additionalRootManagementApps));
        }
        return isAnyPackageFromListInstalled(packages);
    }

    public boolean detectPotentiallyDangerousApps() throws PackageManager.NameNotFoundException {
        return detectPotentiallyDangerousApps(null);
    }

    public boolean detectPotentiallyDangerousApps(String[] additionalDangerousApps) throws PackageManager.NameNotFoundException {
        ArrayList<String> packages = new ArrayList<>();
        packages.addAll(Arrays.asList(Const.knownDangerousAppsPackages));
        if (additionalDangerousApps != null && additionalDangerousApps.length > 0) {
            packages.addAll(Arrays.asList(additionalDangerousApps));
        }
        return isAnyPackageFromListInstalled(packages);
    }

    public boolean detectRootCloakingApps() throws PackageManager.NameNotFoundException {
        return detectRootCloakingApps(null) || (canLoadNativeLibrary() && !checkForNativeLibraryReadAccess());
    }

    public boolean detectRootCloakingApps(String[] additionalRootCloakingApps) throws PackageManager.NameNotFoundException {
        ArrayList<String> packages = new ArrayList<>(Arrays.asList(Const.knownRootCloakingPackages));
        if (additionalRootCloakingApps != null && additionalRootCloakingApps.length > 0) {
            packages.addAll(Arrays.asList(additionalRootCloakingApps));
        }
        return isAnyPackageFromListInstalled(packages);
    }

    public boolean checkForSuBinary() {
        return checkForBinary("su");
    }

    public boolean checkForMagiskBinary() {
        return checkForBinary("magisk");
    }

    public boolean checkForBusyBoxBinary() {
        return checkForBinary("busybox");
    }

    public boolean checkForBinary(String filename) {
        String[] pathsArray = Const.getPaths();
        boolean result = false;
        for (String path : pathsArray) {
            String completePath = path + filename;
            File f = new File(path, filename);
            boolean fileExists = f.exists();
            if (fileExists) {
                QLog.v(completePath + " binary detected!");
                result = true;
            }
        }
        return result;
    }

    public void setLogging(boolean logging) {
        this.loggingEnabled = logging;
        QLog.LOGGING_LEVEL = logging ? 5 : 0;
    }

    private String[] propsReader() {
        try {
            InputStream inputstream = Runtime.getRuntime().exec("getprop").getInputStream();
            if (inputstream == null) {
                return null;
            }
            String propVal = new Scanner(inputstream).useDelimiter("\\A").next();
            return propVal.split("\n");
        } catch (IOException | NoSuchElementException e) {
            QLog.e(e);
            return null;
        }
    }

    private String[] mountReader() {
        try {
            InputStream inputstream = Runtime.getRuntime().exec("mount").getInputStream();
            if (inputstream == null) {
                return null;
            }
            String propVal = new Scanner(inputstream).useDelimiter("\\A").next();
            return propVal.split("\n");
        } catch (IOException | NoSuchElementException e) {
            QLog.e(e);
            return null;
        }
    }

    private boolean isAnyPackageFromListInstalled(List<String> packages) throws PackageManager.NameNotFoundException {
        boolean result = false;
        PackageManager pm = this.mContext.getPackageManager();
        for (String packageName : packages) {
            try {
                pm.getPackageInfo(packageName, 0);
                QLog.e(packageName + " ROOT management app detected!");
                result = true;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return result;
    }

    public boolean checkForDangerousProps() {
        Map<String, String> dangerousProps = new HashMap<>();
        dangerousProps.put("ro.debuggable", "1");
        dangerousProps.put("ro.secure", "0");
        boolean result = false;
        String[] lines = propsReader();
        if (lines == null) {
            return false;
        }
        for (String line : lines) {
            for (String key : dangerousProps.keySet()) {
                if (line.contains(key)) {
                    String badValue = "[" + dangerousProps.get(key) + "]";
                    if (line.contains(badValue)) {
                        QLog.v(key + " = " + badValue + " detected!");
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    public boolean checkForRWPaths() {
        String mountPoint;
        String mountOptions;
        boolean result = false;
        String[] lines = mountReader();
        if (lines == null) {
            return false;
        }
        int sdkVersion = Build.VERSION.SDK_INT;
        for (String line : lines) {
            String[] args = line.split(" ");
            if ((sdkVersion <= 23 && args.length < 4) || (sdkVersion > 23 && args.length < 6)) {
                QLog.e("Error formatting mount line: " + line);
            } else {
                if (sdkVersion > 23) {
                    mountPoint = args[2];
                    mountOptions = args[5];
                } else {
                    mountPoint = args[1];
                    mountOptions = args[3];
                }
                for (String pathToCheck : Const.pathsThatShouldNotBeWritable) {
                    if (mountPoint.equalsIgnoreCase(pathToCheck)) {
                        if (Build.VERSION.SDK_INT > 23) {
                            mountOptions = mountOptions.replace("(", "").replace(")", "");
                        }
                        String[] strArrSplit = mountOptions.split(",");
                        int length = strArrSplit.length;
                        int i = 0;
                        while (true) {
                            if (i < length) {
                                String option = strArrSplit[i];
                                if (!option.equalsIgnoreCase("rw")) {
                                    i++;
                                } else {
                                    QLog.v(pathToCheck + " path is mounted with rw permissions! " + line);
                                    result = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public boolean checkSuExists() throws Throwable {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            boolean z = in.readLine() != null;
            if (process != null) {
                process.destroy();
            }
            return z;
        } catch (Throwable th) {
            if (process != null) {
                process.destroy();
            }
            throw th;
        }
    }

    public boolean checkForNativeLibraryReadAccess() {
        RootBeerNative rootBeerNative = new RootBeerNative();
        try {
            rootBeerNative.setLogDebugMessages(this.loggingEnabled);
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    public boolean canLoadNativeLibrary() {
        return new RootBeerNative().wasNativeLibraryLoaded();
    }

    public boolean checkForRootNative() {
        if (!canLoadNativeLibrary()) {
            QLog.e("We could not load the native library to test for root");
            return false;
        }
        String[] paths = Const.getPaths();
        String[] checkPaths = new String[paths.length];
        for (int i = 0; i < checkPaths.length; i++) {
            checkPaths[i] = paths[i] + "su";
        }
        RootBeerNative rootBeerNative = new RootBeerNative();
        try {
            rootBeerNative.setLogDebugMessages(this.loggingEnabled);
            return rootBeerNative.checkForRoot(checkPaths) > 0;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }
}
