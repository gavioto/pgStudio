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
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudio.DATABASE_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.TYPE_FORM;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.messages.DataTypesJsObject;
import com.openscg.pgstudio.client.messages.FunctionsJsObject;
import com.openscg.pgstudio.client.models.DataTypeInfo;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.FunctionInfo;
import com.openscg.pgstudio.client.models.TypeInfo;
import com.openscg.pgstudio.client.providers.FunctionListDataProvider;
import com.openscg.pgstudio.client.providers.ModelListProvider;
import com.openscg.pgstudio.client.providers.TypeListDataProvider;

public class AddTypePopUp implements StudioModelPopUp {
	
	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final DialogBox dialogBox = new DialogBox();
	DialogBox dialog_child = null;

    private SingleSelectionModel<TypeInfo> selectionModel = null;
    private TypeListDataProvider dataProvider = null;
    
    private DatabaseObjectInfo schema = null;
    
	private TextBox typeName = new TextBox();
	
	private ListBox typeForm;

	private ListBox baseType;
	private TextBox domainDefault;
	private TextBox domainCheck;
	private CheckBox domainNullBox;

	private ListBox subType;
	private ListBox diffFuncs;

	private HorizontalPanel compositePanel;
	private HorizontalPanel domainPanel;
	private HorizontalPanel enumPanel;
	private HorizontalPanel rangePanel;

	private FlexTable compositeAttributes;
	private FlexTable enumValues;

	private AddCompositeAttributePopUp compositePop; 
	private AddEnumValuePopUp enumPop; 
	
	private ArrayList<String> compositeAttrIndex = new ArrayList<String>();
	private ArrayList<String> enumValueIndex = new ArrayList<String>();

	private final int databaseVersion;
	
	public AddTypePopUp(int databaseVersion) {
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
		this.dataProvider = (TypeListDataProvider) provider;
		
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
		lbl.setText("Add Type");
		
		Label lblName = new Label();
		lblName.setStyleName("StudioPopup-Msg");
		lblName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblName.setText("Type Name");

		typeName = new TextBox();
		typeName.setWidth("155px");
		
		HorizontalPanel namePanel = new HorizontalPanel();
		namePanel.setSpacing(10);
		namePanel.add(lblName);
		namePanel.add(typeName);
		namePanel.setCellVerticalAlignment(lblName, HasVerticalAlignment.ALIGN_MIDDLE);

		typeForm = getTypeFormBox();

		info.add(lbl);
		info.add(namePanel);
		info.add(typeForm);
		
		compositePanel = new HorizontalPanel();
		compositePanel.add(getCompositePanel());
		compositePanel.setVisible(true);

		domainPanel = new HorizontalPanel();
		domainPanel.add(getDomainPanel());
		domainPanel.setVisible(false);

		enumPanel = new HorizontalPanel();
		enumPanel.add(getEnumPanel());
		enumPanel.setVisible(false);

		rangePanel = new HorizontalPanel();
		rangePanel.add(getRangePanel());
		rangePanel.setVisible(false);

		panel.add(info);
		panel.add(compositePanel);
		panel.add(domainPanel);
		panel.add(enumPanel);
		panel.add(rangePanel);
		
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
				if (typeName.getText() != null
						&& !typeName.getText().equals("")) {
					TYPE_FORM form = TYPE_FORM.valueOf(typeForm
							.getValue(typeForm.getSelectedIndex()));

					String baseTypeName = "";
					String definition = "";
					ArrayList<String> attributeList = new ArrayList<String>();

					switch (form) {
					case COMPOSITE:
						if(compositeAttributes.getRowCount()>1) {
							for(int i =1; i < compositeAttributes.getRowCount(); i++)	{
								String attrDef = compositeAttributes.getText(i,0).trim();
								
								// Add type
								attrDef = attrDef + " " + compositeAttributes.getText(i,1);
								
								attributeList.add(attrDef);
							}
						}
						break;
					case DOMAIN:
						baseTypeName = baseType.getValue(baseType.getSelectedIndex());
						
						if (!domainDefault.getValue().trim().equals(""))
							definition = definition + " DEFAULT " + domainDefault.getValue().trim(); 
						
						if (domainNullBox.getValue())
							definition = definition + " NOT NULL ";

						if (!domainCheck.getValue().trim().equals(""))
							definition = definition + " CHECK (" + domainCheck.getValue().trim() + ")"; 
						break;
					case ENUM:
						if(enumValues.getRowCount()>1) {
							for(int i =1; i < enumValues.getRowCount(); i++)	{
								String attrDef = enumValues.getText(i,0).trim();
								attributeList.add(attrDef);
							}
						}
						break;
					case RANGE:
						baseTypeName = subType.getValue(subType.getSelectedIndex());
						
						if (diffFuncs.getSelectedIndex() != -1)
							definition = definition + " SUBTYPE_DIFF = " + diffFuncs.getValue(diffFuncs.getSelectedIndex()); 
						break;
					default:
						// Something is not right and we should not get 
						// here so bug out
						return;					
					}
					
					studioService.createType(PgStudio.getToken(), schema.getName(),
							typeName.getText(), form, baseTypeName, definition,
							attributeList, new AsyncCallback<String>() {
								public void onSuccess(String result) {
									if (result != null
											&& result.contains("ERROR")) {
										Window.alert(result);
									} else {
										typeName.setText("");
										refresh();
										dialogBox.hide(true);
									}
								}

								public void onFailure(Throwable caught) {
									Window.alert(caught.getMessage());
								}
							});
				} else {
					Window.alert("Name is mandatory to create a type");
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
			
	private ListBox getTypeFormBox() {
		ListBox box = new ListBox();
		
		box.setWidth("200px");

		box.addItem("Composite", "COMPOSITE");
		box.addItem("Enum", "ENUM");
		box.addItem("Domain", "DOMAIN");
		
		if (databaseVersion >= 920) {
			box.addItem("Range", "RANGE");
		}
		
		box.setItemSelected(0, true);
		
		box.addChangeHandler(getFormChangeHandler());

		return box;
	}

	private ChangeHandler getFormChangeHandler() {
		ChangeHandler handler = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				TYPE_FORM form = TYPE_FORM.valueOf(typeForm.getValue(typeForm.getSelectedIndex()));
				
				switch (form) {
				case COMPOSITE:
					compositePanel.setVisible(true);
					domainPanel.setVisible(false);
					enumPanel.setVisible(false);
					rangePanel.setVisible(false);
					break;
				case DOMAIN:
					compositePanel.setVisible(false);
					domainPanel.setVisible(true);
					enumPanel.setVisible(false);
					rangePanel.setVisible(false);
					break;
				case ENUM:
					compositePanel.setVisible(false);
					domainPanel.setVisible(false);
					enumPanel.setVisible(true);
					rangePanel.setVisible(false);
					break;
				case RANGE:
					compositePanel.setVisible(false);
					domainPanel.setVisible(false);
					enumPanel.setVisible(false);
					rangePanel.setVisible(true);
					break;
				default:
					break;				
				}
			}
			
		};
		
		return handler;
	}

	private Widget getCompositePanel() {
		CaptionPanel panel = new CaptionPanel("Attributes");
		panel.setStyleName("StudioCaption");

		VerticalPanel vPanel = new VerticalPanel();
		ScrollPanel sPanel = new ScrollPanel();

		Button addAttr = new Button("Add");
		addAttr.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				compositePop = new AddCompositeAttributePopUp();
				
				try {
					DialogBox box = compositePop.getDialogBox();
					box.addCloseHandler(getAddCompositeAttributePopUpCloseHandler());
				} catch (PopUpException caught) {
					Window.alert(caught.getMessage());
				}
			}
		});

		
		compositeAttributes = new FlexTable();

		compositeAttributes.setText(0,0, "Name");
		compositeAttributes.setText(0,1, "Datatype");

		compositeAttributes.getColumnFormatter().setWidth(0, "80px");
		compositeAttributes.getColumnFormatter().setWidth(1, "120px");

		sPanel.setHeight("120px");
		sPanel.setWidth("220px");
		sPanel.add(compositeAttributes);

	    vPanel.add(addAttr);
	    vPanel.add(sPanel);
	    
		panel.add(vPanel);
		panel.setSize("250px","160px");

		return panel.asWidget();
	}

	private Widget getDomainPanel() {
		CaptionPanel panel = new CaptionPanel("Definition");
		panel.setStyleName("StudioCaption");

		VerticalPanel vPanel = new VerticalPanel();

		baseType = new ListBox();
		baseType.setWidth("140px");

		studioService.getList(PgStudio.getToken(), DATABASE_OBJECT_TYPE.DATA_TYPE, new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
            	baseType.clear();
                // Show the RPC error message to the user
                Window.alert(caught.getMessage());
            }

            public void onSuccess(String result) {
            	baseType.clear();
            	
    			JsArray<DataTypesJsObject> types = DataTypeInfo.json2Messages(result);

                for (int i = 0; i < types.length(); i++) {
                	DataTypeInfo info = DataTypeInfo.msgToInfo(types.get(i));                	
                	baseType.addItem(info.getName());    
                }
                
                baseType.setSelectedIndex(0);
            }
          });

		Label lblBase = new Label();
		lblBase.setStyleName("StudioPopup-Msg");
		lblBase.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblBase.setText("Base Type");

		HorizontalPanel basePanel = new HorizontalPanel();
		basePanel.setSpacing(10);
		basePanel.add(lblBase);
		basePanel.add(baseType);
		basePanel.setCellVerticalAlignment(lblBase, HasVerticalAlignment.ALIGN_MIDDLE);

		Label lblDefault = new Label();
		lblDefault.setStyleName("StudioPopup-Msg");
		lblDefault.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblDefault.setText("Default");

		domainDefault = new TextBox();
		domainDefault.setWidth("70px");
		
		HorizontalPanel defaultPanel = new HorizontalPanel();
		defaultPanel.setSpacing(10);
		defaultPanel.add(lblDefault);
		defaultPanel.add(domainDefault);
		defaultPanel.setCellVerticalAlignment(lblDefault, HasVerticalAlignment.ALIGN_MIDDLE);

		domainNullBox = new CheckBox();
		domainNullBox.setText("Not Null");

		HorizontalPanel paramPanel = new HorizontalPanel();
		paramPanel.add(defaultPanel);
		paramPanel.add(domainNullBox);
		paramPanel.setCellVerticalAlignment(defaultPanel, HasVerticalAlignment.ALIGN_MIDDLE);
		paramPanel.setCellVerticalAlignment(domainNullBox, HasVerticalAlignment.ALIGN_MIDDLE);

		domainCheck = new TextBox();
		domainCheck.setWidth("200px");

		CaptionPanel checkPanel = new CaptionPanel("Check");
		checkPanel.setStyleName("StudioCaption");

		checkPanel.add(domainCheck);
		
	    vPanel.add(basePanel);
	    vPanel.add(paramPanel);
	    vPanel.add(checkPanel);
	    
		panel.add(vPanel);
		panel.setSize("250px","190px");

		return panel.asWidget();
	}

	private Widget getEnumPanel() {
		CaptionPanel panel = new CaptionPanel("Enum Values");
		panel.setStyleName("StudioCaption");

		VerticalPanel vPanel = new VerticalPanel();
		ScrollPanel sPanel = new ScrollPanel();

		Button addValue = new Button("Add");
		addValue.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				enumPop = new AddEnumValuePopUp();
				
				try {
					DialogBox box = enumPop.getDialogBox();
					box.addCloseHandler(getAddEnumValuePopUpCloseHandler());
				} catch (PopUpException caught) {
					Window.alert(caught.getMessage());
				}
			}
		});

		
		enumValues = new FlexTable();

		enumValues.setText(0,0, "Values");

		enumValues.getColumnFormatter().setWidth(0, "120px");

		sPanel.setHeight("120px");
		sPanel.setWidth("160px");
		sPanel.add(enumValues);

	    vPanel.add(addValue);
	    vPanel.add(sPanel);
	    
		panel.add(vPanel);
		panel.setSize("180px","160px");

		return panel.asWidget();
	}

	private Widget getRangePanel() {
		CaptionPanel panel = new CaptionPanel("Definition");
		panel.setStyleName("StudioCaption");

		VerticalPanel vPanel = new VerticalPanel();

		subType = new ListBox();
		subType.setWidth("110px");
		subType.addChangeHandler(getSubTypeChangeHandler());

		studioService.getList(PgStudio.getToken(), DATABASE_OBJECT_TYPE.DATA_TYPE, new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
            	subType.clear();
                // Show the RPC error message to the user
                Window.alert(caught.getMessage());
            }

            public void onSuccess(String result) {
            	subType.clear();
            	
    			JsArray<DataTypesJsObject> types =  DataTypeInfo.json2Messages(result);

                for (int i = 0; i < types.length(); i++) {
                	DataTypeInfo info = DataTypeInfo.msgToInfo(types.get(i));                	
                	subType.addItem(info.getName());    
                }
                
                subType.setSelectedIndex(0);
            }
          });

		Label lblSub = new Label();
		lblSub.setStyleName("StudioPopup-Msg");
		lblSub.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblSub.setText("Sub Type");

		HorizontalPanel subPanel = new HorizontalPanel();
		subPanel.setSpacing(10);
		subPanel.add(lblSub);
		subPanel.add(subType);
		subPanel.setCellVerticalAlignment(lblSub, HasVerticalAlignment.ALIGN_MIDDLE);
		subPanel.setCellHorizontalAlignment(lblSub, HasHorizontalAlignment.ALIGN_RIGHT);
		subPanel.setCellHorizontalAlignment(subType, HasHorizontalAlignment.ALIGN_RIGHT);

		diffFuncs = new ListBox();
		diffFuncs.setWidth("110px");

		Label lblDiff = new Label();
		lblDiff.setStyleName("StudioPopup-Msg");
		lblDiff.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);	 
		lblDiff.setText("Diff Function");

		HorizontalPanel diffPanel = new HorizontalPanel();
		diffPanel.setSpacing(10);
		diffPanel.add(lblDiff);
		diffPanel.add(diffFuncs);
		diffPanel.setCellVerticalAlignment(lblDiff, HasVerticalAlignment.ALIGN_MIDDLE);
		diffPanel.setCellHorizontalAlignment(lblDiff, HasHorizontalAlignment.ALIGN_RIGHT);
		diffPanel.setCellHorizontalAlignment(diffFuncs, HasHorizontalAlignment.ALIGN_RIGHT);

	    vPanel.add(subPanel);
	    vPanel.add(diffPanel);
	    vPanel.setCellHorizontalAlignment(subPanel, HasHorizontalAlignment.ALIGN_RIGHT);
	    vPanel.setCellHorizontalAlignment(diffPanel, HasHorizontalAlignment.ALIGN_RIGHT);
	    
		panel.add(vPanel);
		panel.setSize("250px","120px");

		return panel.asWidget();
	}

	private ChangeHandler getSubTypeChangeHandler() {
		ChangeHandler handler = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				diffFuncs.clear();
				
				studioService.getRangeDiffFunctionList(PgStudio.getToken(),
						schema.getName(), subType.getValue(subType.getSelectedIndex()),
						new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								diffFuncs.clear();
								// Show the RPC error message to the user
								Window.alert(caught.getMessage());
							}

							public void onSuccess(String result) {
								diffFuncs.clear();

								JsArray<FunctionsJsObject> funcs =  FunctionListDataProvider.json2Messages(result);

								for (int i = 0; i < funcs.length(); i++) {
									FunctionInfo info = FunctionListDataProvider.msgToInfo(funcs.get(i), schema.getId());
									diffFuncs.addItem(info.getName());
								}
							}
						});
			}
		};
		
		return handler;
	}

	private CloseHandler<PopupPanel> getAddCompositeAttributePopUpCloseHandler() {
		CloseHandler<PopupPanel> handler = new CloseHandler<PopupPanel>() {

			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				if (compositePop.isAttributeAdded()) {
					ArrayList<String> attr = new ArrayList<String>();
					int init_row = compositeAttributes.getRowCount();

					compositeAttrIndex.add(compositePop.getAttributeName());

					attr.add(compositePop.getAttributeName());
					attr.add(compositePop.getDataType());

					for (int i = 0; i < attr.size(); i++) {
						compositeAttributes.setText(init_row, i, attr.get(i));
					}

					PushButton removeButton = new PushButton(new Image(
							PgStudio.Images.delete()));
					removeButton.setTitle("Remove this attribute");
					removeButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							int removedIndex = compositeAttrIndex.indexOf(compositePop
									.getAttributeName());
							compositeAttrIndex.remove(removedIndex);
							compositeAttributes.removeRow(removedIndex + 1);
						}
					});
					compositeAttributes.setWidget(init_row, 2, removeButton);
				}
			}
		};

		return handler;
	}

	private CloseHandler<PopupPanel> getAddEnumValuePopUpCloseHandler() {
		CloseHandler<PopupPanel> handler = new CloseHandler<PopupPanel>() {

			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				if (enumPop.isItemAdded()) {
					ArrayList<String> value = new ArrayList<String>();
					int init_row = enumValues.getRowCount();

					enumValueIndex.add(enumPop.getEnumValue());

					value.add(enumPop.getEnumValue());

					for (int i = 0; i < value.size(); i++) {
						enumValues.setText(init_row, i, value.get(i));
					}

					PushButton removeButton = new PushButton(new Image(
							PgStudio.Images.delete()));
					removeButton.setTitle("Remove this value");
					removeButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							int removedIndex = enumValueIndex.indexOf(enumPop.getEnumValue());
							enumValueIndex.remove(removedIndex);
							enumValues.removeRow(removedIndex + 1);
						}
					});
					enumValues.setWidget(init_row, 1, removeButton);
				}
			}
		};

		return handler;
	}

	private void refresh() {
		dataProvider.setSchema(schema);
	}
}
