/*******************************************************************************
 * Copyright (c) 2013 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.notes.core.views.list;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.hangum.tadpole.notes.core.Activator;
import com.hangum.tadpole.notes.core.dialogs.NewNoteDialog;
import com.hangum.tadpole.sql.dao.system.NotesDAO;
import com.hangum.tadpole.sql.session.manager.SessionManager;
import com.hangum.tadpole.sql.system.TadpoleSystem_Notes;
import com.swtdesigner.ResourceManager;

/**
 * Notes
 * 
 * @author hangum
 *
 */
public class NoteListViewPart extends ViewPart {
	public static final String ID = "com.hangum.tadpole.notes.core.view.list";
	private static final Logger logger = Logger.getLogger(NoteListViewPart.class);

	private Combo comboTypes;
	private TableViewer tableViewer;
	private Text textFilter;
	
	public NoteListViewPart() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_parent = new GridLayout(1, false);
		gl_parent.verticalSpacing = 1;
		gl_parent.horizontalSpacing = 1;
		gl_parent.marginHeight = 1;
		gl_parent.marginWidth = 1;
		parent.setLayout(gl_parent);
		
		Composite compositeToolbar = new Composite(parent, SWT.NONE);
		compositeToolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_compositeToolbar = new GridLayout(1, false);
		gl_compositeToolbar.horizontalSpacing = 1;
		gl_compositeToolbar.marginHeight = 1;
		gl_compositeToolbar.marginWidth = 1;
		compositeToolbar.setLayout(gl_compositeToolbar);
		
		ToolBar toolBar = new ToolBar(compositeToolbar, SWT.FLAT | SWT.RIGHT);
		toolBar.setBounds(0, 0, 88, 20);
		
		ToolItem tltmRefresh = new ToolItem(toolBar, SWT.NONE);
		tltmRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				initData();
			}
		});
		tltmRefresh.setToolTipText("Refresh");
		tltmRefresh.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "resources/icons/refresh.png")); //$NON-NLS-1$
		
		ToolItem tltmCreate = new ToolItem(toolBar, SWT.NONE);
		tltmCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewNoteDialog dialog = new NewNoteDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				dialog.open();
			}
		});
		tltmCreate.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "resources/icons/notes_new.png")); //$NON-NLS-1$
		tltmCreate.setToolTipText("Create");
		
		ToolItem tltmDelete = new ToolItem(toolBar, SWT.NONE);
		tltmDelete.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "resources/icons/notes_delete.png")); //$NON-NLS-1$
		tltmDelete.setToolTipText("Delete");
		
		Composite compositeBody = new Composite(parent, SWT.NONE);
		GridLayout gl_compositeBody = new GridLayout(3, false);
		gl_compositeBody.marginHeight = 1;
		gl_compositeBody.verticalSpacing = 1;
		gl_compositeBody.horizontalSpacing = 1;
		gl_compositeBody.marginWidth = 1;
		compositeBody.setLayout(gl_compositeBody);
		compositeBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblFilter = new Label(compositeBody, SWT.NONE);
		lblFilter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFilter.setText("Filter");
		
		comboTypes = new Combo(compositeBody, SWT.READ_ONLY);
		comboTypes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				initData();
			}
		});
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_combo.widthHint = 100;
		gd_combo.minimumWidth = 100;
		comboTypes.setLayoutData(gd_combo);
		
		comboTypes.add("Send");
		comboTypes.add("Receive");
		comboTypes.select(1);
		
		textFilter = new Text(compositeBody, SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		tableViewer = new TableViewer(compositeBody, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		createColumns();
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new NoteListLabelProvider());
		
		initData();
	}
	
	/**
	 * initialize data
	 */
	private void initData() {
		try {
			List<NotesDAO> listNotes = TadpoleSystem_Notes.getNoteList(comboTypes.getText(), SessionManager.getSeq());
			
			tableViewer.setInput(listNotes);
		} catch(Exception e) {
			logger.error("Get note list", e);
		}
	}
	
	/**
	 * create columns
	 */
	private void createColumns() {
		String[] names 	= {"User", "Is Read?", "create date", "Title"};
		int[] sizes		= {160, 40, 100, 200};
		
		for(int i=0; i<names.length; i++) {
			String name = names[i];
			int size = sizes[i];
			
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tblclmnEngine = tableViewerColumn.getColumn();
			tblclmnEngine.setWidth(size);
			tblclmnEngine.setText(name);

		}
	}

	@Override
	public void setFocus() {
	}

}
/**
 * note list label provider
 * 
 * @author hangum
 *
 */
class NoteListLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		NotesDAO dto = (NotesDAO)element;

		switch(columnIndex) {
		case 0: return ""+dto.getReceiveUserId();
		case 1: return dto.getIs_read();
		case 2: return dto.getCreate_time();
		case 3: return dto.getTitle();
		}
		
		return "*** not set column ***"; //$NON-NLS-1$
	}
	
}