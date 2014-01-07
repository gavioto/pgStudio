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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.handlers.UtilityCommandAsyncCallback;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.models.PrivilegeInfo;
import com.openscg.pgstudio.client.providers.ItemListProvider;
import com.openscg.pgstudio.client.providers.PrivilegeListDataProvider;

public class RevokePopUp implements StudioItemPopUp {
	
	private final static String WARNING_MSG = 
			"This will remove access privileges for this object. Are you sure you want to continue?";
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();

    private SingleSelectionModel<PrivilegeInfo> selectionModel = null;
    private PrivilegeListDataProvider dataProvider = null;
    
    private ModelInfo item = null;
    
    private CheckBox cascadeBox;
    
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
		this.dataProvider =  (PrivilegeListDataProvider)provider;
		
	}

	public void setItem(ModelInfo item) {
		this.item = item;
	}
	
	private VerticalPanel getPanel(){
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("StudioPopup");

		VerticalPanel info = new VerticalPanel();
		info.setSpacing(10);
		
		Label lbl = new Label();
		lbl.setStyleName("StudioPopup-Msg-Strong");
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	
		
		PrivilegeInfo priv = selectionModel.getSelectedObject();
		
		String title = "REVOKE " + priv.getType() + " for " + priv.getGrantee();
		lbl.setText(title);
		
		HorizontalPanel warningPanel = new HorizontalPanel();
		Image icon = new Image(PgStudio.Images.warning());
		icon.setWidth("110px");
		
		VerticalPanel detailPanel = new VerticalPanel();
		
		Label lblWarning = new Label();
		lblWarning.setStyleName("StudioPopup-Msg");
		lblWarning.setText(WARNING_MSG);

		HorizontalPanel cascadePanel = new HorizontalPanel();
		cascadePanel.setSpacing(5);
		
		SimplePanel cascadeBoxPanel = new SimplePanel();
		cascadeBoxPanel.setStyleName("roundedCheck");
		
		cascadeBox = new CheckBox();
		cascadeBoxPanel.add(cascadeBox);

		Label lblCascade = new Label();
		lblCascade.setStyleName("StudioPopup-Msg");
		lblCascade.setText("Cascade");

		cascadePanel.add(cascadeBoxPanel);
		cascadePanel.add(lblCascade);
		cascadePanel.setCellVerticalAlignment(cascadeBoxPanel, HasVerticalAlignment.ALIGN_MIDDLE);
		cascadePanel.setCellVerticalAlignment(lblCascade, HasVerticalAlignment.ALIGN_MIDDLE);
		
		detailPanel.add(lblWarning);
		detailPanel.add(cascadePanel);
		
		warningPanel.add(icon);
		warningPanel.add(detailPanel);
		
		info.add(lbl);
		info.add(warningPanel);
		
		panel.add(info);
		
		Widget buttonBar = getButtonPanel(); 
		panel.add(buttonBar);
		panel.setCellHorizontalAlignment(buttonBar, HasHorizontalAlignment.ALIGN_CENTER);
		
		return panel;
	}
	
	private Widget getButtonPanel(){
		HorizontalPanel bar = new HorizontalPanel();
		bar.setHeight("50px");
		bar.setSpacing(10);
		
		Button yesButton = new Button("Yes");
		Button noButton = new Button("No");
		
		bar.add(yesButton);
		bar.add(noButton);
		
		bar.setCellHorizontalAlignment(yesButton, HasHorizontalAlignment.ALIGN_CENTER);
		bar.setCellHorizontalAlignment(noButton, HasHorizontalAlignment.ALIGN_CENTER);

		yesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				boolean cascade = cascadeBox.getValue();

				PrivilegeInfo priv = selectionModel.getSelectedObject();

				UtilityCommandAsyncCallback ac = new UtilityCommandAsyncCallback(
						dialogBox, dataProvider);
				ac.setAutoRefresh(true);
				ac.setShowResultOutput(false);

				studioService.revoke(PgStudio.getToken(), item.getId(), item.getItemType(),
						priv.getType(), priv.getGrantee(), cascade, ac);
			}
		});

		noButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				refresh();
				dialogBox.hide(true);
			}
		});

		return bar.asWidget();
	}
	
	private void refresh() {
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}

}
