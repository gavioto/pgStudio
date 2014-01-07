/*
 * PostgreSQL Studio
 * 
 * Copyright (c) 2013 - 2014, Open Source Consulting Group, Inc.
 * Copyright (c) 2012 - 2013, StormDB, Inc.
 * 
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without a written agreement is
 * hereby granted, provided that the above copyright notice and this paragraph and
 * the following two paragraphs appear in all copies.
 * 
 * IN NO EVENT SHALL OPEN SOURCE CONSULTING GROUP BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST
 * PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * OPEN SOURCE CONSULTING GROUP HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * OPEN SOURCE CONSULTING GROUP SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND
 * OPEN SOURCE CONSULTING GROUP HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 */
package com.openscg.pgstudio.client.panels.popups;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.PgStudio.DATABASE_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.handlers.UtilityCommandAsyncCallback;
import com.openscg.pgstudio.client.messages.ListJsObject;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.models.PrivilegeInfo;
import com.openscg.pgstudio.client.providers.ItemListProvider;
import com.openscg.pgstudio.client.providers.PrivilegeListDataProvider;

public class GrantPopUp implements StudioItemPopUp {
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();

	private SingleSelectionModel<PrivilegeInfo> selectionModel = null;
    private PrivilegeListDataProvider dataProvider = null;
    
    private ModelInfo item = null;

	private ListBox roleList;
	
	private ArrayList<String> privs = new ArrayList<String>();
	
	@Override
	public DialogBox getDialogBox() throws PopUpException {
		if (selectionModel == null)
			throw new PopUpException("Selection Model is not set");

		if (dataProvider == null)
			throw new PopUpException("Data Provider is not set");

		if (item == null)
			throw new PopUpException("Item is not set");
		
		dialogBox.setWidget(getPanel());
		
		dialogBox.setGlassEnabled(true);
		dialogBox.center();

		return dialogBox;
	}

	@Override
	public void setSelectionModel(SingleSelectionModel model) {
		this.selectionModel = model;
	}

	@Override
	public void setDataProvider(ItemListProvider provider) {
		this.dataProvider = (PrivilegeListDataProvider) provider;
		
	}

	public void setItem(ModelInfo item) {
		this.item = item;
	}
	
	private VerticalPanel getPanel(){
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("StudioPopup");

		VerticalPanel info = new VerticalPanel();
		
		Label lbl = new Label();
		lbl.setStyleName("StudioPopup-Msg-Strong");
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);	 
		lbl.setText("Grant Privileges");
		
		Widget granteePanel = getGranteePanel();

		info.add(lbl);
		info.add(granteePanel);
		info.setCellHorizontalAlignment(granteePanel, HasHorizontalAlignment.ALIGN_RIGHT);
		
		CaptionPanel privPanel = new CaptionPanel("Privileges");
		privPanel.setStyleName("StudioCaption");
		privPanel.add(getPrivPanel(item.getItemType()));

		panel.add(info);
		panel.add(privPanel);

		Widget buttonBar = getButtonPanel(); 
		panel.add(buttonBar);
		panel.setCellHorizontalAlignment(buttonBar, HasHorizontalAlignment.ALIGN_CENTER);
		
		return panel;
	}
	
	private Widget getButtonPanel() {
		HorizontalPanel bar = new HorizontalPanel();
		bar.setHeight("50px");
		bar.setSpacing(10);

		Button addButton = new Button("Add");
		Button cancelButton = new Button("Cancel");

		bar.add(addButton);
		bar.add(cancelButton);

		bar.setCellHorizontalAlignment(addButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		bar.setCellHorizontalAlignment(cancelButton,
				HasHorizontalAlignment.ALIGN_CENTER);

		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (privs.size() > 0) {

					UtilityCommandAsyncCallback ac = new UtilityCommandAsyncCallback(
							dialogBox, dataProvider);
					ac.setAutoRefresh(true);
					ac.setShowResultOutput(false);

					studioService.grant(PgStudio.getToken(), item.getId(), item.getItemType(), privs, roleList.getValue(roleList.getSelectedIndex()), ac);
				} else {
					Window.alert("At least 1 privilege must be selected");
				}
			}
		});

		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide(true);
			}
		});

		return bar.asWidget();
	}
			
	private Widget getGranteePanel() {
		Label lbl = new Label();
		lbl.setWidth("90px");
		lbl.setStyleName("StudioPopup-Msg");
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lbl.setText("Grantee");

		roleList = new ListBox();
		roleList.setWidth("155px");
		
		studioService.getList(PgStudio.getToken(), DATABASE_OBJECT_TYPE.ROLE,
				new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						roleList.clear();
						// Show the RPC error message to the user
						Window.alert(caught.getMessage());
					}

					public void onSuccess(String result) {
						roleList.clear();
						roleList.addItem("PUBLIC");
						
						JsArray<ListJsObject> roles =  DatabaseObjectInfo.json2Messages(result);

						for (int i = 0; i < roles.length(); i++) {
							DatabaseObjectInfo info = DatabaseObjectInfo.msgToInfo(roles.get(i));
							roleList.addItem(info.getName());
						}
					}
				});

		
		HorizontalPanel panel = new HorizontalPanel();
		panel.setWidth("285px");
		panel.setSpacing(10);
		panel.add(lbl);
		panel.add(roleList);
		panel.setCellVerticalAlignment(lbl, HasVerticalAlignment.ALIGN_MIDDLE);

		return panel.asWidget();
	}
	
	private Widget getPrivPanel(ITEM_TYPE type) {
		switch(type) {
		case TABLE:
			return getTablePrivPanel();
		case FOREIGN_TABLE:
		case MATERIALIZED_VIEW:
		case VIEW:
			return getViewPrivPanel();
		case FUNCTION:
			return getFunctionPrivPanel();
		case SEQUENCE:
			return getSequencePrivPanel();
		case TYPE:
			return getTypePrivPanel();
		}
		return new SimplePanel().asWidget();
	}
	
	private Widget getTablePrivPanel() {
		VerticalPanel eventBoxesPanel = new VerticalPanel();
		HorizontalPanel eventBoxesPanel1 = new HorizontalPanel();
		eventBoxesPanel1.setWidth("300px");

		eventBoxesPanel1.add(getCheckBox("Select"));
		eventBoxesPanel1.add(getCheckBox("Insert"));
		eventBoxesPanel1.add(getCheckBox("Update"));
		eventBoxesPanel1.add(getCheckBox("Delete"));

		HorizontalPanel eventBoxesPanel2 = new HorizontalPanel();
		eventBoxesPanel2.setWidth("225px");

		eventBoxesPanel2.add(getCheckBox("Truncate"));
		eventBoxesPanel2.add(getCheckBox("References"));
		eventBoxesPanel2.add(getCheckBox("Trigger"));

		eventBoxesPanel.add(eventBoxesPanel1);
		eventBoxesPanel.add(eventBoxesPanel2);		
		
		return eventBoxesPanel.asWidget();
	}

	private Widget getViewPrivPanel() {
		VerticalPanel eventBoxesPanel = new VerticalPanel();
		HorizontalPanel eventBoxesPanel1 = new HorizontalPanel();
		eventBoxesPanel1.setWidth("75px");

		eventBoxesPanel1.add(getCheckBox("Select"));

		eventBoxesPanel.add(eventBoxesPanel1);
		
		return eventBoxesPanel.asWidget();
	}

	private Widget getFunctionPrivPanel() {
		VerticalPanel eventBoxesPanel = new VerticalPanel();
		HorizontalPanel eventBoxesPanel1 = new HorizontalPanel();
		eventBoxesPanel1.setWidth("75px");

		eventBoxesPanel1.add(getCheckBox("Execute"));

		eventBoxesPanel.add(eventBoxesPanel1);
		
		return eventBoxesPanel.asWidget();
	}

	private Widget getSequencePrivPanel() {
		VerticalPanel eventBoxesPanel = new VerticalPanel();
		HorizontalPanel eventBoxesPanel1 = new HorizontalPanel();
		eventBoxesPanel1.setWidth("225px");

		eventBoxesPanel1.add(getCheckBox("Usage"));
		eventBoxesPanel1.add(getCheckBox("Select"));
		eventBoxesPanel1.add(getCheckBox("Update"));

		eventBoxesPanel.add(eventBoxesPanel1);
		
		return eventBoxesPanel.asWidget();
	}

	private Widget getTypePrivPanel() {
		VerticalPanel eventBoxesPanel = new VerticalPanel();
		HorizontalPanel eventBoxesPanel1 = new HorizontalPanel();
		eventBoxesPanel1.setWidth("75px");

		eventBoxesPanel1.add(getCheckBox("Usage"));

		eventBoxesPanel.add(eventBoxesPanel1);
		
		return eventBoxesPanel.asWidget();
	}

	private Widget getCheckBox(final String label) {
		VerticalPanel boxPanel = new VerticalPanel();
		boxPanel.setWidth("75px");
		
		SimplePanel simpleBoxPanel = new SimplePanel();
		simpleBoxPanel.setStyleName("roundedCheck");
		
		final CheckBox checkbox = new CheckBox();
		
		checkbox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (checkbox.getValue()) {
					if (!privs.contains(label.toUpperCase())) {
						privs.add(label.toUpperCase());
					}
				} else {
					if (privs.contains(label.toUpperCase())) {
						privs.remove(label.toUpperCase());
					}					
				}
			}
			
		});
		
		simpleBoxPanel.add(checkbox);
		
		Label lbl = new Label();
		lbl.setStyleName("StudioPopup-Msg");
		lbl.setText(label);

		boxPanel.add(simpleBoxPanel);
		boxPanel.add(lbl);
		boxPanel.setCellHorizontalAlignment(simpleBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		boxPanel.setCellHorizontalAlignment(lbl, HasHorizontalAlignment.ALIGN_CENTER);

		return boxPanel.asWidget();
	}
}
