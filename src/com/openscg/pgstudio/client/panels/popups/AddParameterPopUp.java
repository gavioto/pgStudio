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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
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
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.PgStudio.DATABASE_OBJECT_TYPE;
import com.openscg.pgstudio.client.messages.DataTypesJsObject;
import com.openscg.pgstudio.client.models.DataTypeInfo;

public class AddParameterPopUp implements StudioPopUp {
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();

    private String mode = "";
    
	private ArrayList<DataTypeInfo> dataTypes = null;

    private TextBox paramName;

	private TextBox length;
	private ListBox dataType;
	
	private TextBox defaultValue;
	
	private boolean paramAdded = false;

	@Override
	public DialogBox getDialogBox() throws PopUpException {
		dialogBox.setWidget(getPanel());
		
		dialogBox.setGlassEnabled(true);
		dialogBox.center();

		return dialogBox;
	}

	public String getParameterName() {
		return paramName.getText();
	}
	
	public String getDataType() {	
		StringBuffer dt = new StringBuffer(dataType.getValue(dataType.getSelectedIndex()));
		
		if (length.getText().length() > 0) {
			if (!(dataType.getValue(dataType.getSelectedIndex())
					.contains("[]")))
				dt.append("(" + length.getText() + ")");
			else
				dt.insert((dt.length() - 2), "("
						+ length.getText() + ")");
		}

		return dt.toString();
	}

	public String getMode() {
		return mode;
	}

	public String getDefaultValue() {
		return defaultValue.getText();
	}
	
	public boolean isParameterAdded() {
		return paramAdded;
	}
	
	private VerticalPanel getPanel(){
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("StudioPopup");

		VerticalPanel info = new VerticalPanel();
		info.setSpacing(10);
		
		Label lbl = new Label();
		lbl.setStyleName("StudioPopup-Msg-Strong");
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);	 
		lbl.setText("Add Parameter");

		Label lblName = new Label();
		lblName.setStyleName("StudioPopup-Msg");
		lblName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblName.setText("Parameter Name");

		paramName = new TextBox();
		paramName.setWidth("155px");
		
		HorizontalPanel namePanel = new HorizontalPanel();
		namePanel.setSpacing(10);
		namePanel.add(lblName);
		namePanel.add(paramName);
		namePanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);

		RadioButton rb1 = new RadioButton("ModeGroup", "IN");
		RadioButton rb2 = new RadioButton("ModeGroup", "OUT");
		RadioButton rb3 = new RadioButton("ModeGroup", "INOUT");
		
		rb1.setValue(true);
		mode = "IN";
				
		rb1.addClickHandler(getRB1ButtonClickHandler());
		rb2.addClickHandler(getRB2ButtonClickHandler());
		rb3.addClickHandler(getRB3ButtonClickHandler());

		HorizontalPanel modePanel = new HorizontalPanel();
		modePanel.add(rb1);
		modePanel.add(rb2);
		modePanel.add(rb3);
		CaptionPanel modeCaptionPanel = new CaptionPanel("Mode");
		modeCaptionPanel.setStyleName("StudioCaption");
		modeCaptionPanel.add(modePanel);
		
		Label lblDefault = new Label();
		lblDefault.setStyleName("StudioPopup-Msg");
		lblDefault.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblDefault.setText("Default");

		defaultValue = new TextBox();
		defaultValue.setWidth("100px");
		
		HorizontalPanel defaultPanel = new HorizontalPanel();
		defaultPanel.setSpacing(10);
		defaultPanel.add(lblDefault);
		defaultPanel.add(defaultValue);
		defaultPanel.setCellVerticalAlignment(lblDefault, HasVerticalAlignment.ALIGN_MIDDLE);
		
		HorizontalPanel detailsPanel = new HorizontalPanel();
		detailsPanel.add(modeCaptionPanel);
		detailsPanel.add(defaultPanel);
		detailsPanel.setCellVerticalAlignment(modePanel, HasVerticalAlignment.ALIGN_MIDDLE);
		detailsPanel.setCellVerticalAlignment(defaultPanel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		
		dataType = new ListBox();
		dataType.setWidth("200px");
		
		length = new TextBox();
		length.setWidth("35px");
		
		dataTypes = new ArrayList<DataTypeInfo>();

		studioService.getList(PgStudio.getToken(), DATABASE_OBJECT_TYPE.DATA_TYPE, new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
            	dataType.clear();
            	dataTypes.clear();
                // Show the RPC error message to the user
                Window.alert(caught.getMessage());
            }

            public void onSuccess(String result) {
            	dataType.clear();
            	
    			JsArray<DataTypesJsObject> types = DataTypeInfo.json2Messages(result);

    			int max = 0;
                for (int i = 0; i < types.length(); i++) {
                	DataTypeInfo info = DataTypeInfo.msgToInfo(types.get(i));                	
                	dataType.addItem(info.getName());    
                	dataTypes.add(info);
                	
                	if (info.getUsageCount() >= max) {
                		max = info.getUsageCount();
                		dataType.setSelectedIndex(i);
                		if (info.isHasLength())
                			length.setEnabled(true);
                		else 
                			length.setEnabled(false);
                	}
                }
            }
          });

		dataType.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event)	{
				if (dataTypes.get(dataType.getSelectedIndex()).isHasLength()) 
					length.setEnabled(true);
				else
					length.setEnabled(false);
			}
		});
		
		Label lblLength = new Label();
		lblLength.setStyleName("StudioPopup-Msg");
		lblLength.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblLength.setText("Length");

		length.setEnabled(false);
		length.addKeyPressHandler(getLengthKeyPressHandler());

		HorizontalPanel lengthPanel = new HorizontalPanel();
		lengthPanel.add(lblLength);
		lengthPanel.add(length);
		lengthPanel.setWidth("120px");
		lengthPanel.setCellVerticalAlignment(lblLength, HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel dtPanel = new HorizontalPanel();
		dtPanel.add(dataType);
		dtPanel.add(lengthPanel);
		dtPanel.setCellVerticalAlignment(dataType, HasVerticalAlignment.ALIGN_MIDDLE);

		CaptionPanel dtCaptionPanel = new CaptionPanel("Data Type");
		dtCaptionPanel.setStyleName("StudioCaption");
		dtCaptionPanel.add(dtPanel);

		info.add(lbl);
		info.add(namePanel);
		info.add(detailsPanel);
		info.add(dtCaptionPanel);
		
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
				paramAdded = true;
				dialogBox.hide(true);
			}
		});

		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				paramAdded = false;
				paramName.setText("");
				dialogBox.hide(true);
			}
		});
		
		return bar.asWidget();
	}

	private ClickHandler getRB1ButtonClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mode = "IN";
			}
		};
	}	

	private ClickHandler getRB2ButtonClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mode = "OUT";
			}
		};
	}	

	private ClickHandler getRB3ButtonClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mode = "INOUT";
			}
		};
	}	

	private KeyPressHandler getLengthKeyPressHandler() {
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
