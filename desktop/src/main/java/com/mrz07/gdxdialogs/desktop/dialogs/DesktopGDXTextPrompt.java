/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mrz07.gdxdialogs.desktop.dialogs;

import com.badlogic.gdx.Gdx;
import com.mrz07.gdxdialogs.core.GDXDialogsVars;
import com.mrz07.gdxdialogs.core.dialogs.GDXTextPrompt;
import com.mrz07.gdxdialogs.core.listener.TextPromptListener;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Locale;

public class DesktopGDXTextPrompt implements GDXTextPrompt {

    private CharSequence title = "";
    private CharSequence message = "";
    private CharSequence value = "";

    private CharSequence cancelButtonLabel = "Cancel", confirmButtonLabel = "OK";

    private TextPromptListener listener;

    private InputType inputType = InputType.PLAIN_TEXT;

    public DesktopGDXTextPrompt() {
    }

    @Override
    public GDXTextPrompt show() {
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac")) {
            showMacOS();
        } else {
            showSwing();
        }
        return this;
    }

    // On macOS, disposing any native AWT window requires the AppKit main thread.
    // LWJGL3 owns that thread via -XstartOnFirstThread, causing a deadlock on click.
    // osascript runs in a separate OS process — no shared threads, no deadlock.
    private void showMacOS() {
        final String t = title.toString();
        final String m = message.toString();
        final String def = value.toString();
        final String confirm = confirmButtonLabel.toString();
        final String cancel = cancelButtonLabel.toString();
        final TextPromptListener l = listener;
        new Thread(() -> {
            try {
                String script =
                        "set r to display dialog " + appleStr(m) +
                        " default answer " + appleStr(def) +
                        " with title " + appleStr(t) +
                        " buttons {" + appleStr(cancel) + ", " + appleStr(confirm) + "}" +
                        " default button " + appleStr(confirm) + "\n" +
                        "if button returned of r is " + appleStr(confirm) + " then\n" +
                        "    return text returned of r\n" +
                        "else\n" +
                        "    error number -128\n" +
                        "end if";
                ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                Process proc = pb.start();
                String text = new String(proc.getInputStream().readAllBytes()).trim();
                int exitCode = proc.waitFor();
                if (l != null) {
                    if (exitCode == 0) {
                        Gdx.app.postRunnable(() -> l.confirm(text));
                    } else {
                        Gdx.app.postRunnable(l::cancel);
                    }
                }
            } catch (Exception ex) {
                Gdx.app.error(GDXDialogsVars.LOG_TAG, "osascript dialog failed", ex);
            }
        }, "gdx-dialog-mac").start();
    }

    private static String appleStr(String s) {
        return "\"" + s.replace("\"", "\" & quote & \"") + "\"";
    }

    private void showSwing() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Gdx.app.debug(GDXDialogsVars.LOG_TAG,
                        DesktopGDXTextPrompt.class.getSimpleName() + " now shown");

                final JDialog dialog = new JDialog((java.awt.Frame) null, (String) title, false);
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dialog.setLayout(new BorderLayout(8, 8));

                JPanel content = new JPanel();
                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                content.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
                content.add(new JLabel(message.toString()));

                final JTextField textField;
                switch (inputType) {
                    case PASSWORD:
                        JPasswordField pf = new JPasswordField();
                        pf.setText(String.valueOf(value));
                        textField = pf;
                        break;
                    case PLAIN_TEXT:
                    default:
                        textField = new JTextField();
                        textField.setText(String.valueOf(value));
                }
                content.add(textField);
                dialog.add(content, BorderLayout.CENTER);

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
                JButton confirmBtn = new JButton(confirmButtonLabel.toString());
                JButton cancelBtn = new JButton(cancelButtonLabel.toString());

                confirmBtn.addActionListener(e -> {
                    dialog.dispose();
                    if (listener != null) {
                        final String result;
                        switch (inputType) {
                            case PASSWORD:
                                result = new String(((JPasswordField) textField).getPassword());
                                break;
                            case PLAIN_TEXT:
                            default:
                                result = textField.getText();
                        }
                        Gdx.app.postRunnable(() -> listener.confirm(result));
                    }
                });

                cancelBtn.addActionListener(e -> {
                    dialog.dispose();
                    if (listener != null) {
                        Gdx.app.postRunnable(() -> listener.cancel());
                    }
                });

                btnPanel.add(confirmBtn);
                btnPanel.add(cancelBtn);
                dialog.add(btnPanel, BorderLayout.SOUTH);
                dialog.pack();
                dialog.setMinimumSize(new Dimension(320, dialog.getHeight()));
                dialog.setLocationRelativeTo(null);
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
            }
        });
    }

    @Override
    public GDXTextPrompt setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    @Override
    public GDXTextPrompt setMaxLength(int maxLength) {
        if (maxLength < 1) {
            throw new RuntimeException("Char limit must be >= 1");
        }
        return this;
    }

    @Override
    public GDXTextPrompt setMessage(CharSequence message) {
        this.message = message;
        return this;
    }

    @Override
    public GDXTextPrompt setCancelButtonLabel(CharSequence label) {
        cancelButtonLabel = label;
        return this;
    }

    @Override
    public GDXTextPrompt setConfirmButtonLabel(CharSequence label) {
        confirmButtonLabel = label;
        return this;
    }

    @Override
    public GDXTextPrompt build() {
        return this;
    }

    @Override
    public GDXTextPrompt setTextPromptListener(TextPromptListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public GDXTextPrompt setInputType(InputType inputType) {
        this.inputType = inputType;
        return this;
    }

    @Override
    public GDXTextPrompt setValue(CharSequence inputTip) {
        this.value = inputTip;
        return this;
    }

    @Override
    public GDXTextPrompt dismiss() {
        Gdx.app.debug(GDXDialogsVars.LOG_TAG, DesktopGDXTextPrompt.class.getSimpleName() + " dismiss " +
                "ignored. (Desktop TextPrompt cannot be dismissed)");
        return this;
    }

}
