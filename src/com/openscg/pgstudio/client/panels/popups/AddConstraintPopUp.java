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
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
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
import com.openscg.pgstudio.client.PgStudio.CONSTRAINT_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.handlers.UtilityCommandAsyncCallback;
import com.openscg.pgstudio.client.messages.ColumnJsObject;
import com.openscg.pgstudio.client.messages.TablesJsObject;
import com.openscg.pgstudio.client.models.ConstraintInfo;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.providers.ConstraintListDataProvider;
import com.openscg.pgstudio.client.providers.ItemListProvider;
import com.openscg.pgstudio.client.utils.FieldPicker;

public class AddConstraintPopUp implements StudioItemPopUp {
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();

    private SingleSelectionModel<ConstraintInfo> selectionModel = null;
    private ConstraintListDataProvider dataProvider = null;

    private ModelInfo item = null;
    
    private TextBox constraintName;

	private ListBox constraintType;
	private ListBox tableBox;

	private HorizontalPanel checkPanel;
	private HorizontalPanel columnPanel;
	private HorizontalPanel namePanel;
	private HorizontalPanel foreignKeyPanel;
	private TextBox definition;
	
	FieldPicker columnPicker;
	FieldPicker fkColumnPicker;
	FieldPicker referencePicker;
	
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
		this.dataProvider = (ConstraintListDataProvider) provider;
		
	}

	@Override
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
		lbl.setText("Add Constraint");

		Label lblName = new Label();
		lblName.setStyleName("StudioPopup-Msg");
		lblName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblName.setText("Constraint Name");

		constraintName = new TextBox();
		constraintName.setWidth("155px");
		
		namePanel = new HorizontalPanel();
		namePanel.setSpacing(10);
		namePanel.add(lblName);
		namePanel.add(constraintName);
		namePanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);

		constraintType = getConstraintTypeBox();
		
		checkPanel = new HorizontalPanel();
		CaptionPanel definitionPanel = new CaptionPanel("Definition");
		definitionPanel.setStyleName("StudioCaption");

		definition = new TextBox();
		definition.setWidth("300px");
		definitionPanel.add(definition);
		checkPanel.add(definitionPanel);
		checkPanel.setVisible(false);
		
		columnPanel = new HorizontalPanel();
		columnPanel.add(getColumnPanel());
		columnPanel.setVisible(false);
		
		foreignKeyPanel = new HorizontalPanel();
		foreignKeyPanel.add(getForeignKeyPanel());
		foreignKeyPanel.setVisible(true);
		
		info.add(lbl);
		info.add(namePanel);
		info.add(constraintType);
		info.add(checkPanel);
		info.add(columnPanel);
		info.add(foreignKeyPanel);
		
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

				CONSTRAINT_TYPE type = CONSTRAINT_TYPE.valueOf(constraintType.getValue(constraintType.getSelectedIndex()));
				
				if (type != null) {
					switch(type) {
					case UNIQUE:
						if (constraintName.getText().equals("")) {
							Window.alert("A constraint name is mandatory to create a unique constraint");
							break;
						}

						studioService.createUniqueConstraint(
								PgStudio.getToken(), item.getId(),
								constraintName.getText(), false,
								columnPicker.getSelected(),
								getCallbackHandler());
						break;
					case PRIMARY_KEY:
						studioService.createUniqueConstraint(
								PgStudio.getToken(), item.getId(),
								constraintName.getText(), true,
								columnPicker.getSelected(),
								getCallbackHandler());

						break;
					case CHECK:
						if (constraintName.getText().equals("")) {
							Window.alert("A constraint name is mandatory to create a check constraint");
							break;
						}

						if (definition.getText().equals("")) {
							Window.alert("A definition is mandatory to create a check constraint");
							break;
						}

						studioService.createCheckConstraint(
								PgStudio.getToken(), item.getId(),
								constraintName.getText(), definition.getText(), 
								getCallbackHandler());
						break;
					case FOREIGN_KEY:
						if (constraintName.getText().equals("")) {
							Window.alert("A constraint name is mandatory to create a foreign key constraint");
							break;
						}

						studioService.createForeignKeyConstraint(
								PgStudio.getToken(), item.getId(),
								constraintName.getText(), fkColumnPicker.getSelected(),
								tableBox.getItemText(tableBox.getSelectedIndex()),
								referencePicker.getSelected(),
								getCallbackHandler());

						break;
					default:
						Window.alert("Constraint Type is mandatory to create a constraint");
						break;
					}
					
				} else {
					Window.alert("Constraint Type is mandatory to create a constraint");
				}

			}
		});

		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				constraintName.setText("");
				refresh();
				dialogBox.hide(true);
			}
		});
		
		return bar.asWidget();
	}

	private UtilityCommandAsyncCallback getCallbackHandler() {
		UtilityCommandAsyncCallback handler = new UtilityCommandAsyncCallback(dialogBox, dataProvider);
		handler.setAutoRefresh(true);
		handler.setShowResultOutput(false);
		
		return handler;
	}

	private ListBox getConstraintTypeBox() {
		ListBox box = new ListBox();
		
		box.setWidth("200px");

		box.addItem("Check", "CHECK");
		box.addItem("Foreign Key", "FOREIGN_KEY");
		box.addItem("Primary Key", "PRIMARY_KEY");
		box.addItem("Unique", "UNIQUE");
		
		// TODO: Add handling from exclusion constraints
		// box.addItem("Exclusion", "EXCLUSION");
		
		box.setItemSelected(1, true);
		
		box.addChangeHandler(getTypeChangeHandler());
		
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

	private Widget getForeignKeyPanel() {
		VerticalPanel panel = new VerticalPanel();
		
		CaptionPanel columnCaptionPanel = new CaptionPanel("Columns");
		columnCaptionPanel.setStyleName("StudioCaption");

		fkColumnPicker = new FieldPicker();
		fkColumnPicker.setHeight("100px");
		fkColumnPicker.setWidth("325px");
		
		studioService.getItemObjectList(PgStudio.getToken(), item.getId(), ITEM_TYPE.TABLE, ITEM_OBJECT_TYPE.COLUMN, new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
            	fkColumnPicker.clear();
                Window.alert(caught.getMessage());
            }

            public void onSuccess(String result) {
            	fkColumnPicker.clear();
            	
    			JsArray<ColumnJsObject> columns = json2Messages(result);

                for (int i = 0; i < columns.length(); i++) {
                	fkColumnPicker.addItem(columns.get(i).getColumnName());

                }
            }
          });

		columnCaptionPanel.add(fkColumnPicker);

		CaptionPanel referenceCaptionPanel = new CaptionPanel("References");
		referenceCaptionPanel.setStyleName("StudioCaption");

		VerticalPanel reference = new VerticalPanel();

		Label lbl = new Label();
		lbl.setStyleName("StudioPopup-Msg");
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lbl.setText("Table");

		tableBox = getTableBox();
		
		HorizontalPanel tablePanel = new HorizontalPanel();
		tablePanel.setSpacing(10);
		tablePanel.add(lbl);
		tablePanel.add(tableBox);
		tablePanel.setCellVerticalAlignment(lbl, HasVerticalAlignment.ALIGN_MIDDLE);

		referencePicker = new FieldPicker();
		referencePicker.setHeight("100px");
		referencePicker.setWidth("325px");
		
		reference.add(tablePanel);
		reference.add(referencePicker);
		
		referenceCaptionPanel.add(reference);
		
		panel.add(columnCaptionPanel);
		panel.add(referenceCaptionPanel);
		
		return panel.asWidget();
	}

	private ListBox getTableBox() {
		final ListBox box = new ListBox();
		
		box.setWidth("200px");

		box.addChangeHandler(getTableChangeHandler());
		
		studioService.getList(PgStudio.getToken(), item.getSchema(), ITEM_TYPE.TABLE, new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
            	box.clear();
                Window.alert(caught.getMessage());
            }

            public void onSuccess(String result) {
            	box.clear();
            	
    			JsArray<TablesJsObject> tables = json2TableMessages(result);

                for (int i = 0; i < tables.length(); i++) {             
                	box.addItem(tables.get(i).getName());
                }
                
                if (box.getItemCount()> 0) {
                	box.setItemSelected(0, true);
                	DomEvent.fireNativeEvent(Document.get().createChangeEvent(), box);
                }
            }
          });

		return box;
	}

	private ChangeHandler getTableChangeHandler() {
		ChangeHandler handler = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String table = tableBox.getItemText(tableBox.getSelectedIndex());
				
				studioService.getItemObjectList(PgStudio.getToken(), item.getId(), ITEM_TYPE.TABLE, ITEM_OBJECT_TYPE.COLUMN, new AsyncCallback<String>() {
		            public void onFailure(Throwable caught) {
		            	referencePicker.clear();
		                Window.alert(caught.getMessage());
		            }

		            public void onSuccess(String result) {
		            	referencePicker.clear();
		            	
		    			JsArray<ColumnJsObject> columns = json2Messages(result);

		                for (int i = 0; i < columns.length(); i++) {
		                	referencePicker.addItem(columns.get(i).getColumnName());

		                }
		            }
		          });
			}
			
		};
		
		return handler;
	}

	private ChangeHandler getTypeChangeHandler() {
		ChangeHandler handler = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				CONSTRAINT_TYPE type = CONSTRAINT_TYPE.valueOf(constraintType.getValue(constraintType.getSelectedIndex()));
				
				if (type == CONSTRAINT_TYPE.CHECK ) {
					namePanel.setVisible(true);
					checkPanel.setVisible(true);
					columnPanel.setVisible(false);
					foreignKeyPanel.setVisible(false);
					definition.setEnabled(true);
				} else if (type == CONSTRAINT_TYPE.PRIMARY_KEY)  {
					namePanel.setVisible(false);
					constraintName.setValue("");
					checkPanel.setVisible(false);
					columnPanel.setVisible(true);
					foreignKeyPanel.setVisible(false);
				} else if (type == CONSTRAINT_TYPE.UNIQUE) {
					namePanel.setVisible(true);
					checkPanel.setVisible(false);
					columnPanel.setVisible(true);
					foreignKeyPanel.setVisible(false);										
				} else if (type == CONSTRAINT_TYPE.FOREIGN_KEY) {
					namePanel.setVisible(true);
					checkPanel.setVisible(false);
					columnPanel.setVisible(false);
					foreignKeyPanel.setVisible(true);															
				}
			}
			
		};
		
		return handler;
	}
	
	private void refresh() {
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}

	private static final native JsArray<ColumnJsObject> json2Messages(
			String json)
	/*-{ 
	  	return eval(json); 
	}-*/;

	private static final native JsArray<TablesJsObject> json2TableMessages(
			String json)
	/*-{ 
	  	return eval(json); 
	}-*/;
}
