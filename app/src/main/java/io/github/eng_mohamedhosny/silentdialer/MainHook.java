package io.github.eng_mohamedhosny.silentdialer;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String DIALER_PKG = "com.google.android.dialer";

    private static final String SILENT_WAV_B64 =
            "UklGRkQAAABXQVZFZm10IBAAAAABAAEAgD4AAAB9AAACABAAZGF0YSAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";

    private static final String[] TARGET_DIRS = new String[]{
            "callrecordingprompt",
            "audioinjector"
    };

    private static final String[] PRECREATE_FILES = new String[]{
            "starting_custom.wav",
            "ending_custom.wav",
            "starting_voice-ar_EG.wav",
            "ending_voice-ar_EG.wav",
            "starting_voice-ar_XA.wav",
            "ending_voice-ar_XA.wav",
            "starting_voice-ar_XA.m4a",
            "ending_voice-ar_XA.m4a",
            "starting_voice-en_US.wav",
            "ending_voice-en_US.wav"
    };

    private static byte[] getSilentWavBytes() {
        return Base64.decode(SILENT_WAV_B64, Base64.DEFAULT);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!DIALER_PKG.equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log("SilentDialerRecord: Loaded into " + lpparam.packageName);

        XposedHelpers.findAndHookMethod(
                Application.class,
                "onCreate",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Application app = (Application) param.thisObject;
                        provisionSilentFiles(app.getApplicationContext());
                    }
                }
        );

        XposedBridge.hookAllConstructors(
                File.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        File file = (File) param.thisObject;
                        String path = file.getAbsolutePath();
                        if (isPromptAudioFile(path)) {
                            writeSilentWavIfNeeded(file);
                        }
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                FileOutputStream.class,
                "close",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            File filesDir = new File("/data/data/" + DIALER_PKG + "/files");
                            if (filesDir.exists()) {
                                sweepDirectories(filesDir);
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                MediaPlayer.class,
                "setDataSource",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String path = (String) param.args[0];
                        if (path != null && isPromptAudioFile(path)) {
                            writeSilentWavIfNeeded(new File(path));
                        }
                    }
                }
        );
    }

    private static boolean isPromptAudioFile(String path) {
        if (path == null) {
            return false;
        }
        boolean isAudio = path.endsWith(".wav") || path.endsWith(".m4a") || path.endsWith(".mp3") || path.endsWith(".ogg");
        return isAudio && (path.contains("/callrecordingprompt/") || path.contains("/audioinjector/"));
    }

    private static void provisionSilentFiles(Context context) {
        if (context == null) return;
        File filesDir = context.getFilesDir();
        if (filesDir == null) return;
        sweepDirectories(filesDir);
    }

    private static void sweepDirectories(File filesDir) {
        for (String dirName : TARGET_DIRS) {
            File dir = new File(filesDir, dirName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            dir.setReadable(true, false);
            dir.setExecutable(true, false);

            for (String name : PRECREATE_FILES) {
                writeSilentWavIfNeeded(new File(dir, name));
            }

            File[] existing = dir.listFiles();
            if (existing != null) {
                for (File f : existing) {
                    if (f.isFile()) {
                        writeSilentWavIfNeeded(f);
                    }
                }
            }
        }
    }

    private static synchronized void writeSilentWavIfNeeded(File file) {
        try {
            byte[] silentBytes = getSilentWavBytes();
            if (file.exists() && file.length() == silentBytes.length) {
                return;
            }
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
                parent.setReadable(true, false);
                parent.setExecutable(true, false);
            }
            file.setWritable(true, true);
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(silentBytes);
                fos.flush();
            }
            file.setReadable(true, false);
        } catch (IOException e) {
            XposedBridge.log("SilentDialerRecord: Failed to write silent WAV: " + e.getMessage());
        }
    }
}
