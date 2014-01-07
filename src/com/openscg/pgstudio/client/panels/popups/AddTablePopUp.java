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
import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.handlers.UtilityCommandAsyncCallback;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.TableInfo;
import com.openscg.pgstudio.client.panels.ColumnPanel;
import com.openscg.pgstudio.client.providers.ModelListProvider;
import com.openscg.pgstudio.client.providers.TableListDataProvider;

public class AddTablePopUp implements StudioModelPopUp {
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();
	DialogBox dialog_child = null;
	ColumnPanel clm = new ColumnPanel();

    private SingleSelectionModel<TableInfo> selectionModel = null;
    private TableListDataProvider dataProvider = null;
    
    private DatabaseObjectInfo schema = null;
    
	private TextBox tableName = new TextBox();
	private TextBox fill = new TextBox();
	private TextArea comments = new TextArea();
	private CheckBox unloggedBox;
	private CheckBox temporaryBox;
	private FlexTable columns;
	private HashMap<Integer,String> commentLog = new HashMap<Integer,String>();

	private ArrayList<String> col_index = new ArrayList<String>();
    
    private AddColumnPopUp pop;
	
	@Override
	public DialogBox getDialogBox() throws PopUpException {
		if (selectionModel == null)
			throw new PopUpException("Selection Model is not set");

		if (dataProvider == null)
			throw new PopUpException("Data Provider is not set");

		if (schema == null )
			throw new PopUpException("Schema is not set");
		
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
		this.dataProvider = (TableListDataProvider) provider;
		
	}

	public void setSchema(DatabaseObjectInfo schema) {
		this.schema = schema;
	}
	
	private VerticalPanel getPanel(){
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("StudioPopup");

		VerticalPanel info = new VerticalPanel();
		info.setSpacing(10);
		
		Label lbl = new Label();
		lbl.setStyleName("StudioPopup-Msg-Strong");
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);	 
		lbl.setText("Add Table");
		
		Label lblName = new Label();
		lblName.setStyleName("StudioPopup-Msg");
		lblName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblName.setText("Table Name");

		tableName = new TextBox();
		tableName.setWidth("155px");
		
		HorizontalPanel namePanel = new HorizontalPanel();
		namePanel.setSpacing(10);
		namePanel.add(lblName);
		namePanel.add(tableName);
		namePanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);

		temporaryBox = new CheckBox();
		temporaryBox.setText("Temporary");

		unloggedBox = new CheckBox();
		unloggedBox.setText("Unlogged");

		HorizontalPanel paramPanel = new HorizontalPanel();
		paramPanel.add(temporaryBox);
		paramPanel.add(unloggedBox);
		paramPanel.setWidth("250px");
		paramPanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);
		

		CaptionPanel CommentPanel = new CaptionPanel("Comment");
		CommentPanel.setStyleName("StudioCaption");

		comments = new TextArea();
		comments.setVisibleLines(2);
		comments.setWidth("380px");
		CommentPanel.add(comments);
		
		info.add(lbl);
		info.add(namePanel);
//		info.add(paramPanel);
		
		info.add(getColumnPanel());
		info.add(CommentPanel);
		
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
		
		Button addButton = new Button("Add");
		Button cancelButton = new Button("Cancel");
		
		bar.add(addButton);
		bar.add(cancelButton);
		
		bar.setCellHorizontalAlignment(addButton, HasHorizontalAlignment.ALIGN_CENTER);
		bar.setCellHorizontalAlignment(cancelButton, HasHorizontalAlignment.ALIGN_CENTER);

		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (tableName.getText() != null
						&& !tableName.getText().equals("")) {
					ArrayList<String> col_list = new ArrayList<String>();
					/*
					 * col_list contains the column related query text, and in
					 * future will also contain the constraints related query
					 * text
					 */
					if (columns.getRowCount() > 1) {
						for (int i = 1; i < columns.getRowCount(); i++) {

							if (columns.getText(i, 4).length() != 0)
								commentLog.put(new Integer(i),
										columns.getText(i, 4));

							col_list.add(columns.getText(i, 0) + " "
									+ columns.getText(i, 1)
									+ columns.getText(i, 2));

						}
					}
					if (comments.getText() != "")
						commentLog.put(new Integer(0), comments.getText());

					unloggedBox = new CheckBox();
					fill = new TextBox();

					UtilityCommandAsyncCallback ac = new UtilityCommandAsyncCallback(
							dialogBox, dataProvider);
					ac.setAutoRefresh(true);
					ac.setShowResultOutput(false);

					studioService.createTable(PgStudio.getToken(), schema.getId(),
							tableName.getText(), unloggedBox.getValue(),
							temporaryBox.getValue(), fill.getText(), col_list,
							commentLog, col_index, ac);
				} else {
					Window.alert("Name is mandatory to create a table");
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
	
	
	
	private Widget getColumnPanel() {
		CaptionPanel panel = new CaptionPanel("Columns");
		panel.setStyleName("StudioCaption");

		VerticalPanel vPanel = new VerticalPanel();
		ScrollPanel columnPanel = new ScrollPanel();

		Button addColumn = new Button("Add");
		addColumn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				pop = new AddColumnPopUp(true);
				pop.setSelectionModel(selectionModel);
				
				try {
					DialogBox box = pop.getDialogBox();
					box.addCloseHandler(getAddColumnPopUpCloseHandler());
				} catch (PopUpException caught) {
					Window.alert(caught.getMessage());
				}
			}
		});

		
		columns = new FlexTable();

		columns.setText(0,0, "Name");
	    columns.setText(0,1, "Datatype");
	    columns.setText(0,2, "Definition");
	    columns.setText(0,3, "Comments");
	    columns.setCellPadding(2);
	    columns.setCellSpacing(3);

	    columns.getCellFormatter().setVisible(0,3,false);
	    columns.getColumnFormatter().setWidth(0, "60px");
	    columns.getColumnFormatter().setWidth(1, "50px");
	    columns.getColumnFormatter().setWidth(2, "100px");

	    columnPanel.setHeight("250px");
	    columnPanel.add(columns);

	    vPanel.add(addColumn);
	    vPanel.add(columnPanel);
	    
		panel.add(vPanel);
		panel.setSize("400px","260px");

		return panel.asWidget();
	}
	
	private CloseHandler<PopupPanel> getAddColumnPopUpCloseHandler() {
		CloseHandler<PopupPanel> handler = new CloseHandler<PopupPanel>() {

			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				ArrayList<String>col_data = new ArrayList<String>();
				int init_row = columns.getRowCount();
				
				col_index.add(pop.getColumnName());

				col_data.add(pop.getColumnName());
				col_data.add(pop.getDataType());
				col_data.add(pop.getExtendedDefition());
				col_data.add(pop.getComment());

				for(int i=0; i<col_data.size();i++)	{
					columns.setText(init_row,i,col_data.get(i));
					
					if(i==3)
						columns.getCellFormatter().setVisible(init_row, i, false);
				}

				PushButton removeButton = new PushButton(new Image(PgStudio.Images.delete()));
				removeButton.setTitle("Remove this column");
				removeButton.addClickHandler(new ClickHandler() {
			        public void onClick(ClickEvent event) {
			          int removedIndex = col_index.indexOf(pop.getColumnName());
			          col_index.remove(removedIndex);
			          columns.removeRow(removedIndex + 1);
			        }
			      });
			      columns.setWidget(init_row, 4, removeButton);
			}
			
		};
		
		return handler;
	}	
}
