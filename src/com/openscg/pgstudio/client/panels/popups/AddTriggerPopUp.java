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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.handlers.UtilityCommandAsyncCallback;
import com.openscg.pgstudio.client.messages.FunctionsJsObject;
import com.openscg.pgstudio.client.models.FunctionInfo;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.models.TriggerInfo;
import com.openscg.pgstudio.client.providers.FunctionListDataProvider;
import com.openscg.pgstudio.client.providers.ItemListProvider;
import com.openscg.pgstudio.client.providers.TriggerListDataProvider;

public class AddTriggerPopUp implements StudioItemPopUp {
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();
	DialogBox dialog_child = null;

    private SingleSelectionModel<TriggerInfo> selectionModel = null;
    private TriggerListDataProvider dataProvider = null;
    
    private ModelInfo item = null;

	private TextBox triggerName = new TextBox();	
	private ListBox trgFunction;
	
    private CheckBox evtInsertBox;
    private CheckBox evtUpdateBox;
    private CheckBox evtDeleteBox;
    private CheckBox evtTruncateBox;

    private CheckBox typeBeforeBox;
    private CheckBox typeAfterBox;
    
    private CheckBox forEachRowBox;
    private CheckBox forEachStatementBox;

	private String triggerType = "BEFORE";
	private String forEach = "ROW";
	
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
		this.dataProvider = (TriggerListDataProvider) provider;
		
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
		lbl.setText("Add Trigger");
		
		Label lblName = new Label();
		lblName.setStyleName("StudioPopup-Msg");
		lblName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblName.setText("Trigger Name");

		triggerName = new TextBox();
		triggerName.setWidth("155px");
		
		HorizontalPanel namePanel = new HorizontalPanel();
		namePanel.setSpacing(10);
		namePanel.setWidth("285px");
		namePanel.add(lblName);
		namePanel.add(triggerName);
		namePanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);

		Widget trgPanel = getTriggerFunctionPanel();

		info.add(lbl);
		info.add(namePanel);		
		info.add(trgPanel);
		info.setCellHorizontalAlignment(namePanel, HasHorizontalAlignment.ALIGN_RIGHT);
		info.setCellHorizontalAlignment(trgPanel, HasHorizontalAlignment.ALIGN_RIGHT);
		
		CaptionPanel triggerTypePanel = new CaptionPanel("Trigger Type");
		triggerTypePanel.setStyleName("StudioCaption");
		triggerTypePanel.add(getTriggerTypePanel());
		typeBeforeBox.setValue(true);

		CaptionPanel forEachPanel = new CaptionPanel("For Each");
		forEachPanel.setStyleName("StudioCaption");
		forEachPanel.add(getForEachPanel());
		forEachRowBox.setValue(true);

		HorizontalPanel detailPanel = new HorizontalPanel();
		detailPanel.add(triggerTypePanel);
		detailPanel.add(forEachPanel);
		
		CaptionPanel eventPanel = new CaptionPanel("Event");
		eventPanel.setStyleName("StudioCaption");
		eventPanel.add(getEventPanel());
		evtInsertBox.setValue(true);

		panel.add(info);
		panel.add(detailPanel);
		panel.add(eventPanel);

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
				if (triggerName.getText() != null
						&& !triggerName.getText().equals("")) {

					String eventType = "";
					
					if (evtInsertBox.getValue()) {
						if (!eventType.equals("")) {
							eventType = eventType + " OR ";
						}
						eventType = eventType + "INSERT";
					}

					if (evtUpdateBox.getValue()) {
						if (!eventType.equals("")) {
							eventType = eventType + " OR ";
						}
						eventType = eventType + "UPDATE";
					}

					if (evtDeleteBox.getValue()) {
						if (!eventType.equals("")) {
							eventType = eventType + " OR ";
						}
						eventType = eventType + "DELETE";
					}

					if (evtTruncateBox.getValue()) {
						if (!eventType.equals("")) {
							eventType = eventType + " OR ";
						}
						eventType = eventType + "TRUNCATE";
					}

					UtilityCommandAsyncCallback ac = new UtilityCommandAsyncCallback(
							dialogBox, dataProvider);
					ac.setAutoRefresh(true);
					ac.setShowResultOutput(false);

					studioService.createTrigger(PgStudio.getToken(), item.getId(), item.getItemType(),
							triggerName.getText(), eventType,
							triggerType, forEach, trgFunction
									.getValue(trgFunction.getSelectedIndex()),
							ac);
				} else {
					Window.alert("Name is mandatory to create a trigger");
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
			
	private Widget getTriggerFunctionPanel() {
		Label lbl = new Label();
		lbl.setWidth("90px");
		lbl.setStyleName("StudioPopup-Msg");
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lbl.setText("Execute");

		trgFunction = new ListBox();
		trgFunction.setWidth("155px");
		
		studioService.getTriggerFunctionList(PgStudio.getToken(),
				item.getSchema(), 
				new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						trgFunction.clear();
						// Show the RPC error message to the user
						Window.alert(caught.getMessage());
					}

					public void onSuccess(String result) {
						trgFunction.clear();

						JsArray<FunctionsJsObject> funcs =  FunctionListDataProvider.json2Messages(result);

						for (int i = 0; i < funcs.length(); i++) {
							FunctionInfo info = FunctionListDataProvider.msgToInfo(funcs.get(i), item.getSchema());
							trgFunction.addItem(info.getName());
						}
					}
				});

		
		HorizontalPanel panel = new HorizontalPanel();
		panel.setWidth("285px");
		panel.setSpacing(10);
		panel.add(lbl);
		panel.add(trgFunction);
		panel.setCellVerticalAlignment(lbl, HasVerticalAlignment.ALIGN_MIDDLE);

		return panel.asWidget();
	}
	
	private Widget getEventPanel() {
		HorizontalPanel eventBoxesPanel = new HorizontalPanel();
		eventBoxesPanel.setWidth("300px");
		
		// INSERT
		VerticalPanel insertBoxPanel = new VerticalPanel();
		insertBoxPanel.setWidth("75px");
		SimplePanel evtInsertBoxPanel = new SimplePanel();
		evtInsertBoxPanel.setStyleName("roundedCheck");
		evtInsertBox = new CheckBox();
		evtInsertBoxPanel.add(evtInsertBox);
		Label lblEvtInsert = new Label();
		lblEvtInsert.setStyleName("StudioPopup-Msg");
		lblEvtInsert.setText("Insert");

		insertBoxPanel.add(evtInsertBoxPanel);
		insertBoxPanel.add(lblEvtInsert);
		insertBoxPanel.setCellHorizontalAlignment(evtInsertBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		insertBoxPanel.setCellHorizontalAlignment(lblEvtInsert, HasHorizontalAlignment.ALIGN_CENTER);

		// UPDATE
		VerticalPanel updateBoxPanel = new VerticalPanel();
		updateBoxPanel.setWidth("75px");
		SimplePanel evtUpdateBoxPanel = new SimplePanel();
		evtUpdateBoxPanel.setStyleName("roundedCheck");
		evtUpdateBox = new CheckBox();
		evtUpdateBoxPanel.add(evtUpdateBox);
		Label lblEvtUpdate = new Label();
		lblEvtUpdate.setStyleName("StudioPopup-Msg");
		lblEvtUpdate.setText("Update");

		updateBoxPanel.add(evtUpdateBoxPanel);
		updateBoxPanel.add(lblEvtUpdate);
		updateBoxPanel.setCellHorizontalAlignment(evtUpdateBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		updateBoxPanel.setCellHorizontalAlignment(lblEvtUpdate, HasHorizontalAlignment.ALIGN_CENTER);

		// DELETE
		VerticalPanel deleteBoxPanel = new VerticalPanel();
		deleteBoxPanel.setWidth("75px");
		SimplePanel evtDeleteBoxPanel = new SimplePanel();
		evtDeleteBoxPanel.setStyleName("roundedCheck");
		evtDeleteBox = new CheckBox();
		evtDeleteBoxPanel.add(evtDeleteBox);
		Label lblEvtDelete = new Label();
		lblEvtDelete.setStyleName("StudioPopup-Msg");
		lblEvtDelete.setText("Delete");

		deleteBoxPanel.add(evtDeleteBoxPanel);
		deleteBoxPanel.add(lblEvtDelete);
		deleteBoxPanel.setCellHorizontalAlignment(evtDeleteBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		deleteBoxPanel.setCellHorizontalAlignment(lblEvtDelete, HasHorizontalAlignment.ALIGN_CENTER);

		// TRUNCATE
		VerticalPanel truncateBoxPanel = new VerticalPanel();
		truncateBoxPanel.setWidth("75px");
		SimplePanel evtTruncateBoxPanel = new SimplePanel();
		evtTruncateBoxPanel.setStyleName("roundedCheck");
		evtTruncateBox = new CheckBox();
		evtTruncateBoxPanel.add(evtTruncateBox);
		Label lblEvtTruncate = new Label();
		lblEvtTruncate.setStyleName("StudioPopup-Msg");
		lblEvtTruncate.setText("Truncate");

		evtTruncateBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Truncate trigger can only be at the statement level
				if (evtTruncateBox.getValue()) {
					forEachStatementBox.setValue(true);
					forEach = "STATEMENT";
					forEachRowBox.setValue(false);
					forEachRowBox.setEnabled(false);
				} else {
					forEachRowBox.setEnabled(true);					
				}
				
			}
			
		});
		
		truncateBoxPanel.add(evtTruncateBoxPanel);
		truncateBoxPanel.add(lblEvtTruncate);
		truncateBoxPanel.setCellHorizontalAlignment(evtTruncateBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		truncateBoxPanel.setCellHorizontalAlignment(lblEvtTruncate, HasHorizontalAlignment.ALIGN_CENTER);

		eventBoxesPanel.add(insertBoxPanel);
		eventBoxesPanel.add(updateBoxPanel);
		eventBoxesPanel.add(deleteBoxPanel);
		eventBoxesPanel.add(truncateBoxPanel);

		return eventBoxesPanel.asWidget();
	}

	private Widget getTriggerTypePanel() {
		HorizontalPanel triggerTypeBoxesPanel = new HorizontalPanel();
		triggerTypeBoxesPanel.setWidth("150px");

		// BEFORE
		VerticalPanel beforeBoxPanel = new VerticalPanel();
		beforeBoxPanel.setWidth("70px");
		SimplePanel typeBeforeBoxPanel = new SimplePanel();
		typeBeforeBoxPanel.setStyleName("roundedCheck");
		typeBeforeBox = new CheckBox();
		typeBeforeBoxPanel.add(typeBeforeBox);
		Label lblTypeBefore = new Label();
		lblTypeBefore.setStyleName("StudioPopup-Msg");
		lblTypeBefore.setText("Before");
		typeBeforeBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				typeAfterBox.setValue(false);
				triggerType = "BEFORE";
			}			
		});
		beforeBoxPanel.add(typeBeforeBoxPanel);
		beforeBoxPanel.add(lblTypeBefore);
		beforeBoxPanel.setCellHorizontalAlignment(typeBeforeBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		beforeBoxPanel.setCellHorizontalAlignment(lblTypeBefore, HasHorizontalAlignment.ALIGN_CENTER);

		// AFTER
		VerticalPanel afterBoxPanel = new VerticalPanel();
		afterBoxPanel.setWidth("70px");
		SimplePanel typeAfterBoxPanel = new SimplePanel();
		typeAfterBoxPanel.setStyleName("roundedCheck");
		typeAfterBox = new CheckBox();
		typeAfterBoxPanel.add(typeAfterBox);
		Label lblTypeAfter = new Label();
		lblTypeAfter.setStyleName("StudioPopup-Msg");
		lblTypeAfter.setText("After");
		typeAfterBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				typeBeforeBox.setValue(false);
				triggerType = "AFTER";
			}			
		});
		afterBoxPanel.add(typeAfterBoxPanel);
		afterBoxPanel.add(lblTypeAfter);
		afterBoxPanel.setCellHorizontalAlignment(typeAfterBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		afterBoxPanel.setCellHorizontalAlignment(lblTypeAfter, HasHorizontalAlignment.ALIGN_CENTER);

		triggerTypeBoxesPanel.add(beforeBoxPanel);
		triggerTypeBoxesPanel.add(afterBoxPanel);
		triggerTypeBoxesPanel.setCellHorizontalAlignment(beforeBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		triggerTypeBoxesPanel.setCellHorizontalAlignment(afterBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);

		return triggerTypeBoxesPanel.asWidget();
	}

	private Widget getForEachPanel() {
		HorizontalPanel forEachBoxesPanel = new HorizontalPanel();
		forEachBoxesPanel.setWidth("150px");

		// ROW
		VerticalPanel rowBoxPanel = new VerticalPanel();
		rowBoxPanel.setWidth("70px");
		SimplePanel forEachRowBoxPanel = new SimplePanel();
		forEachRowBoxPanel.setStyleName("roundedCheck");
		forEachRowBox = new CheckBox();
		forEachRowBoxPanel.add(forEachRowBox);
		Label lblRow = new Label();
		lblRow.setStyleName("StudioPopup-Msg");
		lblRow.setText("Row");
		forEachRowBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				forEachStatementBox.setValue(false);
				forEach = "ROW";
			}			
		});
		rowBoxPanel.add(forEachRowBoxPanel);
		rowBoxPanel.add(lblRow);
		rowBoxPanel.setCellHorizontalAlignment(forEachRowBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		rowBoxPanel.setCellHorizontalAlignment(lblRow, HasHorizontalAlignment.ALIGN_CENTER);

		// STATEMENT
		VerticalPanel statementBoxPanel = new VerticalPanel();
		statementBoxPanel.setWidth("70px");
		SimplePanel forEachStatementBoxPanel = new SimplePanel();
		forEachStatementBoxPanel.setStyleName("roundedCheck");
		forEachStatementBox = new CheckBox();
		forEachStatementBoxPanel.add(forEachStatementBox);
		Label lblStatement = new Label();
		lblStatement.setStyleName("StudioPopup-Msg");
		lblStatement.setText("Statement");
		forEachStatementBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				forEachRowBox.setValue(false);
				forEach = "STATEMENT";
			}			
		});
		statementBoxPanel.add(forEachStatementBoxPanel);
		statementBoxPanel.add(lblStatement);
		statementBoxPanel.setCellHorizontalAlignment(forEachStatementBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		statementBoxPanel.setCellHorizontalAlignment(lblStatement, HasHorizontalAlignment.ALIGN_CENTER);

		forEachBoxesPanel.add(rowBoxPanel);
		forEachBoxesPanel.add(statementBoxPanel);
		forEachBoxesPanel.setCellHorizontalAlignment(rowBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		forEachBoxesPanel.setCellHorizontalAlignment(statementBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);

		return forEachBoxesPanel.asWidget();
	}

	private void refresh() {
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
}
