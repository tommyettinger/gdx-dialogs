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
import com.mrz07.gdxdialogs.core.dialogs.GDXProgressDialog;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public class DesktopGDXProgressDialog implements GDXProgressDialog {

	private JOptionPane optionPane;

	private JDialog dialog;

	private CharSequence title = "";
	private CharSequence message = "";
	private boolean cancelable = false;

	public DesktopGDXProgressDialog() {
	}

	@Override
	public GDXProgressDialog setMessage(CharSequence message) {
		this.message = message;
		return this;
	}

	@Override
	public GDXProgressDialog setTitle(CharSequence title) {
		this.title = title;
		return this;
	}

	@Override
	public GDXProgressDialog show() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Gdx.app.debug(GDXDialogsVars.LOG_TAG, DesktopGDXProgressDialog.class.getSimpleName() +
						" now shown.");
				dialog.setVisible(true);
			}
		});
		return this;
	}


	@Override
	public GDXProgressDialog dismiss() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				dialog.dispose();
				Gdx.app.debug(GDXDialogsVars.LOG_TAG, DesktopGDXProgressDialog.class.getSimpleName() + " dismissed.");
			}
		});
		return this;
	}

	@Override
	public GDXProgressDialog setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
		return this;
	}

	@Override
	public GDXProgressDialog build() {

		optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				new Object[] {}, null);
		dialog = new JDialog();

		dialog.setTitle((String) title);
		dialog.setModal(true);

		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(cancelable ? JDialog.DISPOSE_ON_CLOSE : JDialog.DO_NOTHING_ON_CLOSE);
		dialog.pack();

		return this;
	}

}
