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
import com.badlogic.gdx.utils.Array;
import com.mrz07.gdxdialogs.core.GDXDialogsVars;
import com.mrz07.gdxdialogs.core.dialogs.GDXButtonDialog;
import com.mrz07.gdxdialogs.core.listener.ButtonClickListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Locale;

public class DesktopGDXButtonDialog implements GDXButtonDialog {

	private CharSequence title = "";
	private CharSequence message = "";

	private ButtonClickListener listener;

	private Array<CharSequence> labels = new Array<CharSequence>();

	private boolean isBuild = false;

	public DesktopGDXButtonDialog() {
	}

	@Override
	public GDXButtonDialog setCancelable(boolean cancelable) {
		Gdx.app.debug(GDXDialogsVars.LOG_TAG, "INFO: Desktop Dialogs cannot be set cancelled");
		return this;
	}

	@Override
	public GDXButtonDialog show() {
		if (!isBuild) {
			throw new RuntimeException(GDXButtonDialog.class.getSimpleName() +
					" has not been build. Use build() before show().");
		}
		if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac")) {
			showMacOS();
		} else {
			showSwing();
		}
		return this;
	}

	// On macOS, creating or disposing any native AWT window (JDialog) requires
	// sending messages to the AppKit main thread. Since LWJGL3 owns that thread
	// via -XstartOnFirstThread, this deadlocks on click. osascript runs in a
	// completely separate OS process — no shared threads, no deadlock.
	private void showMacOS() {
		final Array<CharSequence> lbls = new Array<>(labels);
		final String t = title.toString();
		final String m = message.toString();
		final ButtonClickListener l = listener;
		new Thread(() -> {
			try {
				StringBuilder sb = new StringBuilder("button returned of (display dialog ");
				sb.append(appleStr(m)).append(" with title ").append(appleStr(t)).append(" buttons {");
				for (int i = 0; i < lbls.size; i++) {
					if (i > 0) sb.append(", ");
					sb.append(appleStr(lbls.get(i).toString()));
				}
				sb.append("} default button 1)");
				ProcessBuilder pb = new ProcessBuilder("osascript", "-e", sb.toString());
				pb.redirectError(ProcessBuilder.Redirect.DISCARD);
				Process proc = pb.start();
				String clicked = new String(proc.getInputStream().readAllBytes()).trim();
				int exitCode = proc.waitFor();
				if (exitCode == 0) {
					int idx = -1;
					for (int i = 0; i < lbls.size; i++) {
						if (lbls.get(i).toString().equals(clicked)) { idx = i; break; }
					}
					if (l != null && idx >= 0) {
						final int fi = idx;
						Gdx.app.postRunnable(() -> l.click(fi));
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
		final Object[] options = new Object[labels.size];
		for (int i = 0; i < labels.size; i++) options[i] = labels.get(i);
		SwingUtilities.invokeLater(() -> {
			Gdx.app.debug(GDXDialogsVars.LOG_TAG,
					DesktopGDXButtonDialog.class.getSimpleName() + " now shown.");
			final JDialog dialog = new JDialog((java.awt.Frame) null, (String) title, false);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.setLayout(new BorderLayout(8, 8));
			JLabel msg = new JLabel("<html>" + message + "</html>");
			msg.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
			dialog.add(msg, BorderLayout.CENTER);
			JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
			for (int i = 0; i < options.length; i++) {
				final int idx = i;
				JButton btn = new JButton(options[i].toString());
				btn.addActionListener(e -> {
					dialog.dispose();
					if (listener != null) {
						Gdx.app.postRunnable(() -> listener.click(idx));
					}
				});
				btnPanel.add(btn);
			}
			dialog.add(btnPanel, BorderLayout.SOUTH);
			dialog.pack();
			dialog.setMinimumSize(new Dimension(280, dialog.getHeight()));
			dialog.setLocationRelativeTo(null);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		});
	}

	@Override
	public GDXButtonDialog dismiss() {
		Gdx.app.debug(GDXDialogsVars.LOG_TAG, DesktopGDXButtonDialog.class.getSimpleName() + " dismiss " +
				"ignored. (Desktop ButtonDialogs cannot be dismissed)");
		return this;
	}

	@Override
	public GDXButtonDialog setClickListener(ButtonClickListener listener) {
		this.listener = listener;
		return this;
	}

	@Override
	public GDXButtonDialog addButton(CharSequence label) {
		if (labels.size >= 3) {
			throw new RuntimeException("You can only have up to three buttons added.");
		}
		labels.add(label);
		return this;
	}

	@Override
	public GDXButtonDialog build() {
		if (labels.size == 0) {
			throw new RuntimeException("You to add at least one button with addButton(..);");
		}
		isBuild = true;
		return this;
	}

	@Override
	public GDXButtonDialog setMessage(CharSequence message) {
		this.message = message;
		return this;
	}

	@Override
	public GDXButtonDialog setTitle(CharSequence title) {
		this.title = title;
		return this;
	}

}
