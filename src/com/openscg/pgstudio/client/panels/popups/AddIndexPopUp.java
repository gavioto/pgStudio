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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio.INDEX_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.handlers.UtilityCommandAsyncCallback;
import com.openscg.pgstudio.client.messages.ColumnJsObject;
import com.openscg.pgstudio.client.models.ColumnInfo;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.providers.IndexListDataProvider;
import com.openscg.pgstudio.client.providers.ItemListProvider;
import com.openscg.pgstudio.client.utils.FieldPicker;

public class AddIndexPopUp implements StudioItemPopUp {
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();

    private SingleSelectionModel<ColumnInfo> selectionModel = null;
    private IndexListDataProvider dataProvider = null;

    private ModelInfo item = null;
    
    private TextBox indexName;

	private CheckBox uniqueBox;
	private CheckBox concurrentlyBox;

	private ListBox indexType;

	FieldPicker columnPicker;
	
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
		this.dataProvider = (IndexListDataProvider) provider;
		
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
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);	 
		lbl.setText("Add Index");

		Label lblName = new Label();
		lblName.setStyleName("StudioPopup-Msg");
		lblName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblName.setText("Index Name");

		indexName = new TextBox();
		indexName.setWidth("155px");
		
		HorizontalPanel namePanel = new HorizontalPanel();
		namePanel.setSpacing(10);
		namePanel.add(lblName);
		namePanel.add(indexName);
		namePanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);

		uniqueBox = new CheckBox();
		uniqueBox.setText("Unique");

		concurrentlyBox = new CheckBox();
		concurrentlyBox.setText("Concurrently");

		HorizontalPanel paramPanel = new HorizontalPanel();
		paramPanel.add(uniqueBox);
		paramPanel.add(concurrentlyBox);
		paramPanel.setWidth("250px");
		paramPanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);

		indexType = getIndexTypeBox();
		
		info.add(lbl);
		info.add(namePanel);
		info.add(paramPanel);
		info.add(indexType);
		info.add(getColumnPanel());
		
		panel.add(info);
		panel.add(getButtonPanel());
		
		return panel;
	}
	
	private Widget getButtonPanel(){
		HorizontalPanel bar = new HorizontalPanel();
		bar.setHeight("50px");
		bar.setSpacing(10);
		bar.setWidth("350px");
		
		Button addButton = new Button("Add");
		Button cancelButton = new Button("Cancel");
		
		bar.add(addButton);
		bar.add(cancelButton);
		
		bar.setCellHorizontalAlignment(addButton, HasHorizontalAlignment.ALIGN_RIGHT);
		bar.setCellHorizontalAlignment(cancelButton, HasHorizontalAlignment.ALIGN_LEFT);

		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
				if (indexType.getValue(indexType.getSelectedIndex()) != null
						&& !indexType.getValue(indexType.getSelectedIndex())
								.equals("")) {

					UtilityCommandAsyncCallback ac = new UtilityCommandAsyncCallback(dialogBox, dataProvider);
					ac.setAutoRefresh(true);
					ac.setShowResultOutput(false);
					
					studioService.createIndex(PgStudio.getToken(), 
							item.getId(), indexName.getText(), 
							INDEX_TYPE.valueOf(indexType.getValue(indexType.getSelectedIndex())),
							uniqueBox.getValue(), concurrentlyBox.getValue(), 
							columnPicker.getSelected(), ac);
				} else {
					Window.alert("Index Type is mandatory to create an index");
				}
				
			}
		});

		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				indexName.setText("");
				refresh();
				dialogBox.hide(true);
			}
		});
		
		return bar.asWidget();
	}


	private ListBox getIndexTypeBox() {
		ListBox box = new ListBox();
		
		box.setWidth("200px");

		box.addItem("B-tree", "BTREE");
		box.addItem("Hash", "HASH");
		box.addItem("GiST", "GIST");
		box.addItem("SP-GiST", "SPGIST");
		box.addItem("GIN", "GIN");
		
		return box;
	}
	
	private Widget getColumnPanel() {
		CaptionPanel columnCaptionPanel = new CaptionPanel("Columns");
		columnCaptionPanel.setStyleName("StudioCaption");

		columnPicker = new FieldPicker();
		columnPicker.setHeight("150px");
		columnPicker.setWidth("325px");
		
		studioService.getItemObjectList(PgStudio.getToken(), item.getId(), ITEM_TYPE.TABLE, ITEM_OBJECT_TYPE.COLUMN, new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
            	columnPicker.clear();
                Window.alert(caught.getMessage());
            }

            public void onSuccess(String result) {
            	columnPicker.clear();
            	
    			JsArray<ColumnJsObject> columns = json2Messages(result);

                for (int i = 0; i < columns.length(); i++) {
                	columnPicker.addItem(columns.get(i).getColumnName());

                }
            }
          });

		columnCaptionPanel.add(columnPicker);

		return columnCaptionPanel.asWidget();
	}
	
	private void refresh() {
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}

	private static final native JsArray<ColumnJsObject> json2Messages(
			String json)
	/*-{ 
	  	return eval(json); 
	}-*/;
}
