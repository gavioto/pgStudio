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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.providers.ModelListProvider;

public class RenameItemPopUp implements StudioModelPopUp {

	private final PgStudioServiceAsync studioService = GWT
			.create(PgStudioService.class);

	private final ITEM_TYPE type;

	final DialogBox dialogBox = new DialogBox();

	private SingleSelectionModel<ModelInfo> selectionModel = null;
	private ModelListProvider dataProvider = null;

	private DatabaseObjectInfo schema = null;
	private String item = null;

	private TextBox itemName;

	public RenameItemPopUp(ITEM_TYPE type) {
		this.type = type;
	}

	@Override
	public DialogBox getDialogBox() throws PopUpException {
		if (selectionModel == null)
			throw new PopUpException("Selection Model is not set");

		if (dataProvider == null)
			throw new PopUpException("Data Provider is not set");

		if (schema == null || item == null)
			throw new PopUpException("Schema and item name are not set");

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
	public void setDataProvider(ModelListProvider provider) {
		this.dataProvider = (ModelListProvider) provider;

	}

	public void setSchema(DatabaseObjectInfo schema) {
		this.schema = schema;
	}

	public void setItem(String item) {
		this.item = item;
	}

	private VerticalPanel getPanel() {
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("StudioPopup");

		VerticalPanel info = new VerticalPanel();
		info.setSpacing(10);

		Label lbl = new Label();
		lbl.setStyleName("StudioPopup-Msg-Strong");
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		String label = "Rename ";

		switch (type) {
		case TABLE:
			label = label + "Table";
			break;
		case VIEW:
			label = label + "View";
			break;
		case FOREIGN_TABLE:
			label = label + "Foreign Table";
			break;
		case FUNCTION:
			label = label + "Function";
			break;
		case MATERIALIZED_VIEW:
			label = label + "Materialized View";
			break;
		default:
			break;
		}

		lbl.setText(label);

		itemName = new TextBox();
		itemName.setWidth("155px");
		itemName.setText(selectionModel.getSelectedObject().getName());

		info.add(lbl);
		info.add(itemName);

		panel.add(info);
		panel.add(getButtonPanel());

		return panel;
	}

	private Widget getButtonPanel() {
		HorizontalPanel bar = new HorizontalPanel();
		bar.setHeight("50px");
		bar.setSpacing(10);

		Button cancelButton = new Button("Cancel");
		Button renameButton = new Button("Rename");

		bar.add(renameButton);
		bar.add(cancelButton);

		bar.setCellHorizontalAlignment(renameButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		bar.setCellHorizontalAlignment(cancelButton,
				HasHorizontalAlignment.ALIGN_CENTER);

		renameButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionModel.getSelectedObject() != null
						&& !"".equals(selectionModel.getSelectedObject()
								.getName())) {
					if (itemName.getText() != null
							&& !itemName.getText().equals("")) {
						studioService.renameItem(PgStudio.getToken(),
								selectionModel.getSelectedObject().getId(),
								selectionModel.getSelectedObject()
										.getItemType(), itemName.getText(),
								new AsyncCallback<String>() {
									public void onSuccess(String result) {
										if (result != null
												&& result.contains("ERROR")) {
											Window.alert(result);
										} else {
											itemName.setText("");
											refresh();
											dialogBox.hide(true);
										}
									}

									public void onFailure(Throwable caught) {
										Window.alert(caught.getMessage());
										dialogBox.hide();
									}
								});
					}
				}
			}
		});

		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				itemName.setText("");
				refresh();
				dialogBox.hide(true);
			}
		});

		return bar.asWidget();
	}

	private void refresh() {
		dataProvider.setSchema(schema);
	}

}
