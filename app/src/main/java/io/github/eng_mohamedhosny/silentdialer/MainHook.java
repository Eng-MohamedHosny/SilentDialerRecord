package io.github.eng_mohamedhosny.silentdialer;

import android.app.Application;
import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.speech.tts.TextToSpeech;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "SilentDialerRecord: ";
    private static final String DIALER_PKG = "com.google.android.dialer";

    private static final ThreadLocal<Boolean> IS_INTERNAL_OP = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    private static final byte[] SILENT_WAV_BYTES = new byte[]{
        (byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46, (byte) 0x44, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x57, (byte) 0x41, (byte) 0x56, (byte) 0x45, (byte) 0x66, (byte) 0x6D, (byte) 0x74, (byte) 0x20, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x80, (byte) 0x3E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7D, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x64, (byte) 0x61, (byte) 0x74, (byte) 0x61, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };

    private static final byte[] SILENT_M4A_BYTES = new byte[]{
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1C, (byte) 0x66, (byte) 0x74, (byte) 0x79, (byte) 0x70, (byte) 0x4D, (byte) 0x34, (byte) 0x41, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x4D, (byte) 0x34, (byte) 0x41, (byte) 0x20, (byte) 0x69, (byte) 0x73, (byte) 0x6F, (byte) 0x6D, (byte) 0x69, (byte) 0x73, (byte) 0x6F, (byte) 0x32, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x66, (byte) 0x72, (byte) 0x65, (byte) 0x65, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1F, (byte) 0x6D, (byte) 0x64, (byte) 0x61, (byte) 0x74, (byte) 0xDC, (byte) 0x00, (byte) 0x4C, (byte) 0x61, (byte) 0x76, (byte) 0x63, (byte) 0x36, (byte) 0x33, (byte) 0x2E, (byte) 0x37, (byte) 0x2E, (byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x0E, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xFA, (byte) 0x6D, (byte) 0x6F, (byte) 0x6F, (byte) 0x76, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6C, (byte) 0x6D, (byte) 0x76, (byte) 0x68, (byte) 0x64, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3E, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x20, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x25, (byte) 0x74, (byte) 0x72, (byte) 0x61, (byte) 0x6B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x5C, (byte) 0x74, (byte) 0x6B, (byte) 0x68, (byte) 0x64, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x24, (byte) 0x65, (byte) 0x64, (byte) 0x74, (byte) 0x73, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1C, (byte) 0x65, (byte) 0x6C, (byte) 0x73, (byte) 0x74, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x9D, (byte) 0x6D, (byte) 0x64, (byte) 0x69, (byte) 0x61, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x6D, (byte) 0x64, (byte) 0x68, (byte) 0x64, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3E, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x55, (byte) 0xC4, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x2D, (byte) 0x68, (byte) 0x64, (byte) 0x6C, (byte) 0x72, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x73, (byte) 0x6F, (byte) 0x75, (byte) 0x6E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x53, (byte) 0x6F, (byte) 0x75, (byte) 0x6E, (byte) 0x64, (byte) 0x48, (byte) 0x61, (byte) 0x6E, (byte) 0x64, (byte) 0x6C, (byte) 0x65, (byte) 0x72, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x48, (byte) 0x6D, (byte) 0x69, (byte) 0x6E, (byte) 0x66, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x73, (byte) 0x6D, (byte) 0x68, (byte) 0x64, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x24, (byte) 0x64, (byte) 0x69, (byte) 0x6E, (byte) 0x66, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1C, (byte) 0x64, (byte) 0x72, (byte) 0x65, (byte) 0x66, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x75, (byte) 0x72, (byte) 0x6C, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x0C, (byte) 0x73, (byte) 0x74, (byte) 0x62, (byte) 0x6C, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6A, (byte) 0x73, (byte) 0x74, (byte) 0x73, (byte) 0x64, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x5A, (byte) 0x6D, (byte) 0x70, (byte) 0x34, (byte) 0x61, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3E, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x36, (byte) 0x65, (byte) 0x73, (byte) 0x64, (byte) 0x73, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x25, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x17, (byte) 0x40, (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7D, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x9D, (byte) 0x05, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x05, (byte) 0x14, (byte) 0x08, (byte) 0x56, (byte) 0xE5, (byte) 0x00, (byte) 0x06, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x73, (byte) 0x74, (byte) 0x74, (byte) 0x73, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1C, (byte) 0x73, (byte) 0x74, (byte) 0x73, (byte) 0x63, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1C, (byte) 0x73, (byte) 0x74, (byte) 0x73, (byte) 0x7A, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x13, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x14, (byte) 0x73, (byte) 0x74, (byte) 0x63, (byte) 0x6F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1A, (byte) 0x73, (byte) 0x67, (byte) 0x70, (byte) 0x64, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x72, (byte) 0x6F, (byte) 0x6C, (byte) 0x6C, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1C, (byte) 0x73, (byte) 0x62, (byte) 0x67, (byte) 0x70, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x72, (byte) 0x6F, (byte) 0x6C, (byte) 0x6C, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x61, (byte) 0x75, (byte) 0x64, (byte) 0x74, (byte) 0x61, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x59, (byte) 0x6D, (byte) 0x65, (byte) 0x74, (byte) 0x61, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x21, (byte) 0x68, (byte) 0x64, (byte) 0x6C, (byte) 0x72, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6D, (byte) 0x64, (byte) 0x69, (byte) 0x72, (byte) 0x61, (byte) 0x70, (byte) 0x70, (byte) 0x6C, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x2C, (byte) 0x69, (byte) 0x6C, (byte) 0x73, (byte) 0x74, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x24, (byte) 0xA9, (byte) 0x74, (byte) 0x6F, (byte) 0x6F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1C, (byte) 0x64, (byte) 0x61, (byte) 0x74, (byte) 0x61, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x4C, (byte) 0x61, (byte) 0x76, (byte) 0x66, (byte) 0x36, (byte) 0x33, (byte) 0x2E, (byte) 0x35, (byte) 0x2E, (byte) 0x31, (byte) 0x30, (byte) 0x31
    };

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

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!DIALER_PKG.equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log(TAG + "Loaded into " + lpparam.packageName + " (v1.0.4)");

        // 1. Hook FileOutputStream constructors and close()
        XposedBridge.hookAllConstructors(
            FileOutputStream.class,
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (IS_INTERNAL_OP.get()) return;
                    Object arg0 = param.args.length > 0 ? param.args[0] : null;
                    if (arg0 instanceof File) {
                        XposedHelpers.setAdditionalInstanceField(param.thisObject, "sd_file", arg0);
                    } else if (arg0 instanceof String) {
                        XposedHelpers.setAdditionalInstanceField(param.thisObject, "sd_file", new File((String) arg0));
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
                    if (IS_INTERNAL_OP.get()) return;
                    File file = (File) XposedHelpers.getAdditionalInstanceField(param.thisObject, "sd_file");
                    if (file != null && isPromptAudioFile(file.getAbsolutePath())) {
                        writeSilentAudioIfNeeded(file);
                    }
                }
            }
        );

        // 2. Hook FileInputStream constructors
        XposedBridge.hookAllConstructors(
            FileInputStream.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (IS_INTERNAL_OP.get()) return;
                    Object arg0 = param.args.length > 0 ? param.args[0] : null;
                    File file = null;
                    if (arg0 instanceof File) {
                        file = (File) arg0;
                    } else if (arg0 instanceof String) {
                        file = new File((String) arg0);
                    }
                    if (file != null && isPromptAudioFile(file.getAbsolutePath())) {
                        writeSilentAudioIfNeeded(file);
                    }
                }
            }
        );

        // 3. Hook ParcelFileDescriptor.open(File, int)
        try {
            XposedHelpers.findAndHookMethod(
                ParcelFileDescriptor.class,
                "open",
                File.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (IS_INTERNAL_OP.get()) return;
                        File file = (File) param.args[0];
                        if (file != null && isPromptAudioFile(file.getAbsolutePath())) {
                            writeSilentAudioIfNeeded(file);
                        }
                    }
                }
            );
        } catch (Throwable ignored) {}

        // 4. Hook MediaExtractor.setDataSource(String)
        try {
            XposedHelpers.findAndHookMethod(
                MediaExtractor.class,
                "setDataSource",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (IS_INTERNAL_OP.get()) return;
                        String path = (String) param.args[0];
                        if (path != null && isPromptAudioFile(path)) {
                            writeSilentAudioIfNeeded(new File(path));
                        }
                    }
                }
            );
        } catch (Throwable ignored) {}

        // 5. Hook MediaPlayer.setDataSource(String) & MediaPlayer.start()
        try {
            XposedHelpers.findAndHookMethod(
                MediaPlayer.class,
                "setDataSource",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (IS_INTERNAL_OP.get()) return;
                        String path = (String) param.args[0];
                        if (path != null && isPromptAudioFile(path)) {
                            XposedHelpers.setAdditionalInstanceField(param.thisObject, "sd_is_prompt", Boolean.TRUE);
                            writeSilentAudioIfNeeded(new File(path));
                        }
                    }
                }
            );

            XposedHelpers.findAndHookMethod(
                MediaPlayer.class,
                "start",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Boolean isPrompt = (Boolean) XposedHelpers.getAdditionalInstanceField(param.thisObject, "sd_is_prompt");
                        if (Boolean.TRUE.equals(isPrompt)) {
                            MediaPlayer mp = (MediaPlayer) param.thisObject;
                            try {
                                mp.setVolume(0.0f, 0.0f);
                            } catch (Throwable ignored) {}
                        }
                    }
                }
            );
        } catch (Throwable ignored) {}

        // 6. Hook TextToSpeech.speak to mute automated announcements
        try {
            XposedHelpers.findAndHookMethod(
                TextToSpeech.class,
                "speak",
                CharSequence.class,
                int.class,
                Bundle.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        CharSequence text = (CharSequence) param.args[0];
                        if (isTtsAnnouncement(text)) {
                            XposedBridge.log(TAG + "Silenced TTS speak: " + text);
                            param.setResult(TextToSpeech.SUCCESS);
                        }
                    }
                }
            );
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(
                TextToSpeech.class,
                "speak",
                String.class,
                int.class,
                HashMap.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String text = (String) param.args[0];
                        if (isTtsAnnouncement(text)) {
                            XposedBridge.log(TAG + "Silenced legacy TTS speak: " + text);
                            param.setResult(TextToSpeech.SUCCESS);
                        }
                    }
                }
            );
        } catch (Throwable ignored) {}

        // 7. Provision on Application startup
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

        // 8. Hook File constructors
        XposedBridge.hookAllConstructors(
            File.class,
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (IS_INTERNAL_OP.get()) return;
                    File file = (File) param.thisObject;
                    String path = file.getAbsolutePath();
                    if (isPromptAudioFile(path)) {
                        writeSilentAudioIfNeeded(file);
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

    private static boolean isTtsAnnouncement(CharSequence cs) {
        if (cs == null) return false;
        String s = cs.toString().toLowerCase();
        return s.contains("تسجيل") || s.contains("المكالمة") || s.contains("record") || s.contains("call");
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
                writeSilentAudioIfNeeded(new File(dir, name));
            }

            File[] existing = dir.listFiles();
            if (existing != null) {
                for (File f : existing) {
                    if (f.isFile() && isPromptAudioFile(f.getAbsolutePath())) {
                        writeSilentAudioIfNeeded(f);
                    }
                }
            }
        }
    }

    private static void writeSilentAudioIfNeeded(File file) {
        if (file == null || IS_INTERNAL_OP.get()) {
            return;
        }
        IS_INTERNAL_OP.set(Boolean.TRUE);
        try {
            boolean isM4a = file.getName().toLowerCase().endsWith(".m4a");
            byte[] silentBytes = isM4a ? SILENT_M4A_BYTES : SILENT_WAV_BYTES;

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
        } catch (Throwable e) {
            XposedBridge.log(TAG + "Failed to write silent audio: " + e.getMessage());
        } finally {
            IS_INTERNAL_OP.set(Boolean.FALSE);
        }
    }
}
