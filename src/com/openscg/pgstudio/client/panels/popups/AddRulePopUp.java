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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.mastergaurav.codemirror.client.CodeMirror;
import com.mastergaurav.codemirror.client.CodeMirrorConfig;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.handlers.UtilityCommandAsyncCallback;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.models.RuleInfo;
import com.openscg.pgstudio.client.providers.ItemListProvider;
import com.openscg.pgstudio.client.providers.RuleListDataProvider;

public class AddRulePopUp implements StudioItemPopUp {
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();
	DialogBox dialog_child = null;

    private SingleSelectionModel<RuleInfo> selectionModel = null;
    private RuleListDataProvider dataProvider = null;
    
    private ModelInfo item = null;

	private TextBox ruleName = new TextBox();	
	
    private CheckBox evtSelectBox;
    private CheckBox evtInsertBox;
    private CheckBox evtUpdateBox;
    private CheckBox evtDeleteBox;

    private CheckBox typeAlsoBox;
    private CheckBox typeInsteadBox;

	private SimplePanel codePanel;
	private final TextArea codeArea = new TextArea();
	private CodeMirror cm;

	private String eventType = "SELECT";
	private String ruleType = "ALSO";
	
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
		this.dataProvider = (RuleListDataProvider) provider;
		
	}

	public void setItem(ModelInfo item) {
		this.item = item;
	}

	public void setupCodePanel() {
		Element e = codeArea.getElement();
		TextAreaElement tae = TextAreaElement.as(e);
		
		CodeMirrorConfig config = getConfig();
		cm = CodeMirror.fromTextArea(tae, config);	
	}

	private VerticalPanel getPanel(){
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("StudioPopup");

		VerticalPanel info = new VerticalPanel();
		
		Label lbl = new Label();
		lbl.setStyleName("StudioPopup-Msg-Strong");
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);	 
		lbl.setText("Add Rule");
		
		Label lblName = new Label();
		lblName.setStyleName("StudioPopup-Msg");
		lblName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblName.setText("Rule Name");

		ruleName = new TextBox();
		ruleName.setWidth("155px");
		
		HorizontalPanel namePanel = new HorizontalPanel();
		namePanel.setSpacing(10);
		namePanel.add(lblName);
		namePanel.add(ruleName);
		namePanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);

		info.add(lbl);
		info.add(namePanel);
		
		CaptionPanel eventPanel = new CaptionPanel("Event");
		eventPanel.setStyleName("StudioCaption");
		eventPanel.add(getEventPanel());
		evtSelectBox.setValue(true);

		CaptionPanel ruleTypePanel = new CaptionPanel("Rule Type");
		ruleTypePanel.setStyleName("StudioCaption");
		ruleTypePanel.add(getRuleTypePanel());

		CaptionPanel definitionPanel = new CaptionPanel("Definition");
		definitionPanel.setStyleName("StudioCaption");
		definitionPanel.add(getSQLPanel());

		panel.add(info);
		panel.add(eventPanel);
		panel.add(ruleTypePanel);
		panel.add(definitionPanel);		

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
				if (ruleName.getText() != null
						&& !ruleName.getText().equals("")) {

					UtilityCommandAsyncCallback ac = new UtilityCommandAsyncCallback(
							dialogBox, dataProvider);
					ac.setAutoRefresh(true);
					ac.setShowResultOutput(false);

					studioService.createRule(PgStudio.getToken(), 
							item.getId(), item.getItemType(), ruleName.getText(), eventType, ruleType, cm.getContent(), ac);
				} else {
					Window.alert("Name is mandatory to create a rule");
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
			
	private Widget getEventPanel() {
		HorizontalPanel eventBoxesPanel = new HorizontalPanel();
		eventBoxesPanel.setWidth("300px");

		// SELECT
		VerticalPanel selectBoxPanel = new VerticalPanel();
		selectBoxPanel.setWidth("75px");
		SimplePanel evtSelectBoxPanel = new SimplePanel();
		evtSelectBoxPanel.setStyleName("roundedCheck");
		evtSelectBox = new CheckBox();
		evtSelectBoxPanel.add(evtSelectBox);
		Label lblEvtSelect = new Label();
		lblEvtSelect.setStyleName("StudioPopup-Msg");
		lblEvtSelect.setText("Select");
		evtSelectBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				evtInsertBox.setValue(false);
				evtUpdateBox.setValue(false);
				evtDeleteBox.setValue(false);
				eventType = "SELECT";
			}			
		});
		selectBoxPanel.add(evtSelectBoxPanel);
		selectBoxPanel.add(lblEvtSelect);
		selectBoxPanel.setCellHorizontalAlignment(evtSelectBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		selectBoxPanel.setCellHorizontalAlignment(lblEvtSelect, HasHorizontalAlignment.ALIGN_CENTER);
		
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
		evtInsertBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				evtSelectBox.setValue(false);
				evtUpdateBox.setValue(false);
				evtDeleteBox.setValue(false);
				eventType = "INSERT";
			}			
		});
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
		evtUpdateBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				evtSelectBox.setValue(false);
				evtInsertBox.setValue(false);
				evtDeleteBox.setValue(false);
				eventType = "UPDATE";
			}			
		});
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
		evtDeleteBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				evtSelectBox.setValue(false);
				evtInsertBox.setValue(false);
				evtUpdateBox.setValue(false);
				eventType = "DELETE";
			}			
		});
		deleteBoxPanel.add(evtDeleteBoxPanel);
		deleteBoxPanel.add(lblEvtDelete);
		deleteBoxPanel.setCellHorizontalAlignment(evtDeleteBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		deleteBoxPanel.setCellHorizontalAlignment(lblEvtDelete, HasHorizontalAlignment.ALIGN_CENTER);

		eventBoxesPanel.add(selectBoxPanel);
		eventBoxesPanel.add(insertBoxPanel);
		eventBoxesPanel.add(updateBoxPanel);
		eventBoxesPanel.add(deleteBoxPanel);

		return eventBoxesPanel.asWidget();
	}

	private Widget getRuleTypePanel() {
		HorizontalPanel ruleTypeBoxesPanel = new HorizontalPanel();
		ruleTypeBoxesPanel.setWidth("300px");

		// ALSO
		VerticalPanel alsoBoxPanel = new VerticalPanel();
		alsoBoxPanel.setWidth("75px");
		SimplePanel typeAlsoBoxPanel = new SimplePanel();
		typeAlsoBoxPanel.setStyleName("roundedCheck");
		typeAlsoBox = new CheckBox();
		typeAlsoBoxPanel.add(typeAlsoBox);
		Label lblTypeAlso = new Label();
		lblTypeAlso.setStyleName("StudioPopup-Msg");
		lblTypeAlso.setText("Also");
		typeAlsoBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				typeInsteadBox.setValue(false);
				ruleType = "ALSO";
			}			
		});
		alsoBoxPanel.add(typeAlsoBoxPanel);
		alsoBoxPanel.add(lblTypeAlso);
		alsoBoxPanel.setCellHorizontalAlignment(typeAlsoBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		alsoBoxPanel.setCellHorizontalAlignment(lblTypeAlso, HasHorizontalAlignment.ALIGN_CENTER);

		// INSTEAD
		VerticalPanel insteadBoxPanel = new VerticalPanel();
		insteadBoxPanel.setWidth("75px");
		SimplePanel typeInsteadBoxPanel = new SimplePanel();
		typeInsteadBoxPanel.setStyleName("roundedCheck");
		typeInsteadBox = new CheckBox();
		typeInsteadBoxPanel.add(typeInsteadBox);
		Label lblTypeInstead = new Label();
		lblTypeInstead.setStyleName("StudioPopup-Msg");
		lblTypeInstead.setText("Instead");
		typeInsteadBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				typeAlsoBox.setValue(false);
				ruleType = "INSTEAD";
			}			
		});
		insteadBoxPanel.add(typeInsteadBoxPanel);
		insteadBoxPanel.add(lblTypeInstead);
		insteadBoxPanel.setCellHorizontalAlignment(typeInsteadBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		insteadBoxPanel.setCellHorizontalAlignment(lblTypeInstead, HasHorizontalAlignment.ALIGN_CENTER);

		ruleTypeBoxesPanel.add(alsoBoxPanel);
		ruleTypeBoxesPanel.add(insteadBoxPanel);
		ruleTypeBoxesPanel.setCellHorizontalAlignment(alsoBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);
		ruleTypeBoxesPanel.setCellHorizontalAlignment(insteadBoxPanel, HasHorizontalAlignment.ALIGN_CENTER);

		return ruleTypeBoxesPanel.asWidget();
	}

	private Widget getSQLPanel() {
		codePanel = new SimplePanel();
				
		codeArea.setWidth("300px");
		codeArea.setHeight("100px");

		codeArea.setReadOnly(false);
		
		codePanel.add(codeArea);
		
		return codePanel.asWidget();
	}
	
	private CodeMirrorConfig getConfig() {
		CodeMirrorConfig config = CodeMirrorConfig.getDefault();
		
		String parserFile = GWT.getModuleBaseURL() + "cm/contrib/sql/js/parsesql.js";
		String styleSheet = GWT.getModuleBaseURL() + "cm/contrib/sql/css/sqlcolors.css";
		
		config.setParserFile(parserFile);
		config.setStylesheet(styleSheet);
		
		config.setWidth(300, Unit.PX);
		config.setHeight(100, Unit.PX);
		
		return config;
	}
}
