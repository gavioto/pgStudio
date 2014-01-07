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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.handlers.UtilityCommandAsyncCallback;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.SequenceInfo;
import com.openscg.pgstudio.client.providers.ModelListProvider;
import com.openscg.pgstudio.client.providers.SequenceListDataProvider;

public class AddSequencePopUp implements StudioModelPopUp {
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();
	DialogBox dialog_child = null;

    private SingleSelectionModel<SequenceInfo> selectionModel = null;
    private SequenceListDataProvider dataProvider = null;
    
    private DatabaseObjectInfo schema = null;
    
	private TextBox seqName = new TextBox();	
	
	private CheckBox temporaryBox;
	private CheckBox cycleBox;

	private TextBox increment;
	private TextBox minValue;
	private TextBox maxValue;
	private TextBox start;
	private TextBox cache;
	
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
		this.dataProvider = (SequenceListDataProvider) provider;
		
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
		lbl.setText("Add Sequence");
		
		Label lblName = new Label();
		lblName.setStyleName("StudioPopup-Msg");
		lblName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblName.setText("Sequence Name");

		seqName = new TextBox();
		seqName.setWidth("155px");
		
		HorizontalPanel namePanel = new HorizontalPanel();
		namePanel.setSpacing(10);
		namePanel.add(lblName);
		namePanel.add(seqName);
		namePanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);

		info.add(lbl);
		info.add(namePanel);
		
		DisclosurePanel advanced = new DisclosurePanel("Advanced Options");
		advanced.setWidth("325px");

		VerticalPanel checkPanel = new VerticalPanel();

		temporaryBox = new CheckBox();
		temporaryBox.setText("Temporary");

		cycleBox = new CheckBox();
		cycleBox.setText("Cycle");
		
		checkPanel.add(temporaryBox);
		checkPanel.add(cycleBox);

		Label lblStart = new Label();
		lblStart.setStyleName("StudioPopup-Msg");
		lblStart.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		lblStart.setWidth("100px");
		lblStart.setText("Starting Value");

		start = new TextBox();
		start.setWidth("50px");
		start.addKeyPressHandler(getOnlyNumbersKeyPressHandler());
		
		HorizontalPanel startPanel = new HorizontalPanel();
		startPanel.setSpacing(1);
		startPanel.add(lblStart);
		startPanel.add(start);
		startPanel.setCellVerticalAlignment(lblStart, HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel panel1 = new HorizontalPanel();
		panel1.setWidth("325px");
		panel1.setSpacing(10);
		panel1.add(checkPanel);
		panel1.add(startPanel);
		panel1.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);
		panel1.setCellHorizontalAlignment(checkPanel, HasHorizontalAlignment.ALIGN_CENTER);
		panel1.setCellHorizontalAlignment(startPanel, HasHorizontalAlignment.ALIGN_RIGHT);

		Label lblIncrement = new Label();
		lblIncrement.setStyleName("StudioPopup-Msg");
		lblIncrement.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblIncrement.setWidth("75px");
		lblIncrement.setText("Increment");

		increment = new TextBox();
		increment.setWidth("50px");
		increment.addKeyPressHandler(getOnlyNumbersKeyPressHandler());
		
		HorizontalPanel incrementPanel = new HorizontalPanel();
		incrementPanel.setSpacing(1);
		incrementPanel.add(lblIncrement);
		incrementPanel.add(increment);
		incrementPanel.setCellVerticalAlignment(lblIncrement, HasVerticalAlignment.ALIGN_MIDDLE);

		
		Label lblCache = new Label();
		lblCache.setStyleName("StudioPopup-Msg");
		lblCache.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		lblCache.setWidth("75px");
		lblCache.setText("Cache");

		cache = new TextBox();
		cache.setWidth("50px");
		cache.addKeyPressHandler(getOnlyNumbersKeyPressHandler());
		
		HorizontalPanel cachePanel = new HorizontalPanel();
		cachePanel.setSpacing(1);
		cachePanel.add(lblCache);
		cachePanel.add(cache);
		cachePanel.setCellVerticalAlignment(lblCache, HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel panel2 = new HorizontalPanel();
		panel2.setWidth("325px");
		panel2.setSpacing(10);
		panel2.add(incrementPanel);
		panel2.add(cachePanel);
		panel2.setCellHorizontalAlignment(incrementPanel, HasHorizontalAlignment.ALIGN_LEFT);
		panel2.setCellHorizontalAlignment(cachePanel, HasHorizontalAlignment.ALIGN_RIGHT);

		Label lblMinValue = new Label();
		lblMinValue.setStyleName("StudioPopup-Msg");
		lblMinValue.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblMinValue.setWidth("75px");
		lblMinValue.setText("Min Value");

		minValue = new TextBox();
		minValue.setWidth("50px");
		minValue.addKeyPressHandler(getOnlyNumbersKeyPressHandler());
		
		HorizontalPanel minValuePanel = new HorizontalPanel();
		minValuePanel.setSpacing(1);
		minValuePanel.add(lblMinValue);
		minValuePanel.add(minValue);
		minValuePanel.setCellVerticalAlignment(lblMinValue, HasVerticalAlignment.ALIGN_MIDDLE);

		Label lblMaxValue = new Label();
		lblMaxValue.setStyleName("StudioPopup-Msg");
		lblMaxValue.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		lblMaxValue.setWidth("75px");
		lblMaxValue.setText("Max Value");

		maxValue = new TextBox();
		maxValue.setWidth("50px");
		maxValue.addKeyPressHandler(getOnlyNumbersKeyPressHandler());
		
		HorizontalPanel maxValuePanel = new HorizontalPanel();
		maxValuePanel.setSpacing(1);
		maxValuePanel.add(lblMaxValue);
		maxValuePanel.add(maxValue);
		maxValuePanel.setCellVerticalAlignment(lblMaxValue, HasVerticalAlignment.ALIGN_MIDDLE);
		
		HorizontalPanel panel3 = new HorizontalPanel();
		panel3.setWidth("325px");
		panel3.setSpacing(10);
		panel3.add(minValuePanel);
		panel3.add(maxValuePanel);
		panel3.setCellHorizontalAlignment(minValuePanel, HasHorizontalAlignment.ALIGN_LEFT);
		panel3.setCellHorizontalAlignment(maxValuePanel, HasHorizontalAlignment.ALIGN_RIGHT);
				
		VerticalPanel optionPanel = new VerticalPanel();
		optionPanel.add(panel1);
		optionPanel.add(panel2);
		optionPanel.add(panel3);
		
		advanced.add(optionPanel);
		
		panel.add(info);
		panel.add(advanced);
		
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
				if(seqName.getText() != null && !seqName.getText().equals("")) {
					boolean temp = temporaryBox.getValue();
					boolean cycle = cycleBox.getValue();

					int incr = 0;
					int min = 0;
					int max = 0;
					int st = 0;
					int ca = 0;

					if (!increment.getText().trim().equals("")) 
						incr = Integer.parseInt(increment.getText());

					if (!minValue.getText().trim().equals("")) 
						min = Integer.parseInt(minValue.getText());

					if (!maxValue.getText().trim().equals("")) 
						max = Integer.parseInt(maxValue.getText());

					if (!start.getText().trim().equals("")) 
						st = Integer.parseInt(start.getText());

					if (!cache.getText().trim().equals("")) 
						ca = Integer.parseInt(cache.getText());

					UtilityCommandAsyncCallback ac = new UtilityCommandAsyncCallback(dialogBox, dataProvider);
					ac.setAutoRefresh(true);
					ac.setShowResultOutput(false);
					
					studioService.createSequence(PgStudio.getToken(), schema.getId(), seqName.getText(), 
							temp, incr, min, max, st, ca, cycle, ac);
				} else {
					Window.alert("Name is mandatory to create a sequence");
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
			
	private KeyPressHandler getOnlyNumbersKeyPressHandler() {
		KeyPressHandler kph = new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				char keyCode = event.getCharCode();

				if ((!Character.isDigit(keyCode))
						&& (keyCode != (char) KeyCodes.KEY_TAB)
						&& (keyCode != (char) KeyCodes.KEY_BACKSPACE)
						&& (keyCode != (char) KeyCodes.KEY_DELETE)
						&& (keyCode != (char) KeyCodes.KEY_ENTER)
						&& (keyCode != (char) KeyCodes.KEY_HOME)
						&& (keyCode != (char) KeyCodes.KEY_END)
						&& (keyCode != (char) KeyCodes.KEY_LEFT)
						&& (keyCode != (char) KeyCodes.KEY_UP)
						&& (keyCode != (char) KeyCodes.KEY_RIGHT)
						&& (keyCode != (char) KeyCodes.KEY_DOWN)) {
					// TextBox.cancelKey() suppresses the current keyboard
					// event.		
					((TextBox)event.getSource()).cancelKey();
				}

			}
		};

		return kph;
	}	
}
