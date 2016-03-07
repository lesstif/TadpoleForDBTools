/*******************************************************************************
 * Copyright (c) 2016 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.application.start.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;

import com.hangum.tadpole.application.start.BrowserActivator;
import com.hangum.tadpole.application.start.Messages;
import com.hangum.tadpole.application.start.dialog.update.NewVersionChecker;
import com.hangum.tadpole.application.start.dialog.update.NewVersionObject;
import com.hangum.tadpole.application.start.dialog.update.NewVersionViewDialog;
import com.swtdesigner.ResourceManager;

/**
 * New version check action
 */
public class NewVersionCheckAction extends Action {
	
	private final IWorkbenchWindow window;
	
	public NewVersionCheckAction(IWorkbenchWindow window) {
		super(Messages.get().NewVersionCheckAction_0);
		setId(this.getClass().getName());
		setImageDescriptor( ResourceManager.getPluginImageDescriptor(BrowserActivator.APPLICTION_ID, "resources/icons/refresh.png"));
		setToolTipText(Messages.get().NewVersionCheckAction_0); //$NON-NLS-1$
		
		this.window = window;
	}
	
	public void run() {
		if(window != null) {
			boolean isNew = NewVersionChecker.getInstance().getVersionInfoData();
			if(isNew) {
				NewVersionObject newVersionObj = NewVersionChecker.getInstance().getNewVersionObj();
	    		NewVersionViewDialog dialog = new NewVersionViewDialog(null, newVersionObj);
	    		dialog.open();
			} else {
				MessageDialog.openInformation(null, Messages.get().NewVersionCheckAction_0, Messages.get().NewVersionCheckAction_2);
			}
		}
	}
	
}