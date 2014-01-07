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
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.ViewInfo;
import com.openscg.pgstudio.client.providers.ModelListProvider;
import com.openscg.pgstudio.client.providers.ViewListDataProvider;

public class AddViewPopUp implements StudioModelPopUp {
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();
	DialogBox dialog_child = null;

    private SingleSelectionModel<ViewInfo> selectionModel = null;
    private ViewListDataProvider dataProvider = null;
    
    private DatabaseObjectInfo schema = null;
    
	private TextBox viewName = new TextBox();	
	
    private CheckBox materializedBox;

	private SimplePanel codePanel;
	private final TextArea codeArea = new TextArea();
	private CodeMirror cm;

	private final int databaseVersion;
	
	public AddViewPopUp(int databaseVersion) {
		this.databaseVersion = databaseVersion;
	}
	
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
		this.dataProvider = (ViewListDataProvider) provider;
		
	}

	public void setSchema(DatabaseObjectInfo schema) {
		this.schema = schema;
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
		lbl.setText("Add View");
		
		Label lblName = new Label();
		lblName.setStyleName("StudioPopup-Msg");
		lblName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblName.setText("View Name");

		viewName = new TextBox();
		viewName.setWidth("155px");
		
		HorizontalPanel namePanel = new HorizontalPanel();
		namePanel.setSpacing(10);
		namePanel.add(lblName);
		namePanel.add(viewName);
		namePanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);

		info.add(lbl);
		info.add(namePanel);
		
		CaptionPanel definitionPanel = new CaptionPanel("Definition");
		definitionPanel.setStyleName("StudioCaption");
		definitionPanel.add(getSQLPanel());

		panel.add(info);
		panel.add(definitionPanel);

		if (databaseVersion >= 930) {
			HorizontalPanel materializePanel = new HorizontalPanel();
			materializePanel.setSpacing(5);

			SimplePanel materializeBoxPanel = new SimplePanel();
			materializeBoxPanel.setStyleName("roundedCheck");

			materializedBox = new CheckBox();
			materializeBoxPanel.add(materializedBox);

			Label lblMaterialize = new Label();
			lblMaterialize.setStyleName("StudioPopup-Msg");
			lblMaterialize.setText("Materialized");

			materializePanel.add(materializeBoxPanel);
			materializePanel.add(lblMaterialize);
			materializePanel.setCellVerticalAlignment(materializeBoxPanel,
					HasVerticalAlignment.ALIGN_MIDDLE);
			materializePanel.setCellVerticalAlignment(lblMaterialize,
					HasVerticalAlignment.ALIGN_MIDDLE);

			panel.add(materializePanel);
			panel.setCellHorizontalAlignment(materializePanel,
					HasHorizontalAlignment.ALIGN_CENTER);
		}		
		
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
				if (viewName.getText() != null
						&& !viewName.getText().equals("")) {

					boolean materialized = false;
					if (databaseVersion >= 930) {
						materialized = materializedBox.getValue();
					}
					
					UtilityCommandAsyncCallback ac = new UtilityCommandAsyncCallback(
							dialogBox, dataProvider);
					ac.setAutoRefresh(true);
					ac.setShowResultOutput(false);

					studioService.createView(PgStudio.getToken(), schema.getName(),
							viewName.getText(), cm.getContent(), "",
							materialized, ac);
				} else {
					Window.alert("Name is mandatory to create a view");
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
			
	private Widget getSQLPanel() {
		codePanel = new SimplePanel();
				
		codeArea.setWidth("300px");
		codeArea.setHeight("150px");

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
		config.setHeight(150, Unit.PX);
		
		return config;
	}

	private void refresh() {
		dataProvider.setSchema(schema);
	}
}
