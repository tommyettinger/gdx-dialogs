package com.mrz07.gdxdialogs.desktop.test;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.Clipboard;
import com.mrz07.gdxdialogs.core.dialogs.GDXButtonDialog;
import com.mrz07.gdxdialogs.core.dialogs.GDXProgressDialog;
import com.mrz07.gdxdialogs.core.dialogs.GDXTextPrompt;
import com.mrz07.gdxdialogs.core.listener.ButtonClickListener;
import com.mrz07.gdxdialogs.desktop.DesktopGDXDialogs;

import javax.swing.*;

public class DesktopDialogTest {

    public static void main(String[] args) throws Exception {
        // Install a minimal Gdx.app stub so internal debug/postRunnable calls work
        Gdx.app = new StubApplication();

        DesktopGDXDialogs dialogs = new DesktopGDXDialogs();

        // 1. Button Dialog
        GDXButtonDialog buttonDialog = dialogs.newDialog(GDXButtonDialog.class);
        buttonDialog.setTitle("Button Dialog Test")
                .setMessage("Which option do you prefer?")
                .addButton("Yes")
                .addButton("No")
                .addButton("Cancel")
                .setClickListener(new ButtonClickListener() {
                    @Override
                    public void click(int button) {
                        System.out.println("[ButtonDialog] Button clicked: " + button);
                    }
                })
                .build()
                .show();

        // Give Swing time to process (invokeLater is used inside show())
        Thread.sleep(200);

        // 2. Progress Dialog — shown for 2 seconds then dismissed
        GDXProgressDialog progressDialog = dialogs.newDialog(GDXProgressDialog.class);
        progressDialog.setTitle("Progress Dialog Test")
                .setMessage("Loading, please wait...")
                .build()
                .show();

        Thread.sleep(2000);
        progressDialog.dismiss();

        // 3. Text Prompt
        GDXTextPrompt textPrompt = dialogs.newDialog(GDXTextPrompt.class);
        textPrompt.setTitle("Text Prompt Test")
                .setMessage("Enter your name:")
                .setConfirmButtonLabel("OK")
                .setCancelButtonLabel("Cancel")
                .build()
                .show();

        // Wait for Swing EDT to finish (invokeLater schedules async)
        Thread.sleep(300);

        // Keep JVM alive until all Swing dialogs are closed
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                // no-op — just flushes the EDT queue
            }
        });

        // Brief delay to allow listener callbacks to complete
        Thread.sleep(500);
        System.out.println("[Test] Done.");
    }

    /** Minimal Application stub — only implements the methods used by desktop dialogs. */
    private static class StubApplication implements Application {
        @Override public void debug(String tag, String message) { System.out.println("[DEBUG] " + tag + ": " + message); }
        @Override public void debug(String tag, String message, Throwable exception) { debug(tag, message); exception.printStackTrace(); }
        @Override public void log(String tag, String message) { System.out.println("[LOG] " + tag + ": " + message); }
        @Override public void log(String tag, String message, Throwable exception) { log(tag, message); exception.printStackTrace(); }
        @Override public void error(String tag, String message) { System.err.println("[ERROR] " + tag + ": " + message); }
        @Override public void error(String tag, String message, Throwable exception) { error(tag, message); exception.printStackTrace(); }
        @Override public void postRunnable(Runnable runnable) { runnable.run(); }
        @Override public int getLogLevel() { return Application.LOG_DEBUG; }
        @Override public void setLogLevel(int logLevel) {}
        @Override public ApplicationLogger getApplicationLogger() { return null; }
        @Override public void setApplicationLogger(ApplicationLogger applicationLogger) {}
        @Override public ApplicationType getType() { return ApplicationType.Desktop; }
        @Override public int getVersion() { return 0; }
        @Override public long getJavaHeap() { return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); }
        @Override public long getNativeHeap() { return getJavaHeap(); }
        @Override public Preferences getPreferences(String name) { return null; }
        @Override public Clipboard getClipboard() { return null; }
        @Override public void exit() { System.exit(0); }
        @Override public void addLifecycleListener(LifecycleListener listener) {}
        @Override public void removeLifecycleListener(LifecycleListener listener) {}
        @Override public Audio getAudio() { return null; }
        @Override public Input getInput() { return null; }
        @Override public Files getFiles() { return null; }
        @Override public Graphics getGraphics() { return null; }
        @Override public Net getNet() { return null; }
        @Override public com.badlogic.gdx.ApplicationListener getApplicationListener() { return null; }
    }
}
