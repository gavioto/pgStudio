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
package com.openscg.pgstudio.client.panels;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudio.ITEM_OBJECT_TYPE;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.models.TriggerInfo;
import com.openscg.pgstudio.client.panels.popups.AddTriggerPopUp;
import com.openscg.pgstudio.client.panels.popups.DropItemObjectPopUp;
import com.openscg.pgstudio.client.panels.popups.PopUpException;
import com.openscg.pgstudio.client.panels.popups.RenameItemObjectPopUp;
import com.openscg.pgstudio.client.providers.TriggerListDataProvider;

public class TriggerPanel extends Composite implements DetailsPanel {
	
	private static interface GetValue<C> {
	    C getValue(TriggerInfo column);
	}

	private DataGrid<TriggerInfo> dataGrid;
    private TriggerListDataProvider dataProvider = new TriggerListDataProvider();

    private TextArea triggerDef;
    
    private ModelInfo item = null;
    
    private final SingleSelectionModel<TriggerInfo> selectionModel = 
        	new SingleSelectionModel<TriggerInfo>(TriggerInfo.KEY_PROVIDER);

	private String MAIN_HEIGHT = "250px";
	
	public static int ROWS_PER_PAGE = 5;
	public static int MAX_TRIGGERS = 1600;
	
	public void setItem(ModelInfo item) {
		this.item = item;
		triggerDef.setText("");
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
	
	public TriggerPanel() {
		
		VerticalPanel panel = new VerticalPanel();

		panel.add(getButtonBar());
		panel.add(getMainPanel());
		panel.add(getDetailSection());
				
		initWidget(panel);
	}

	private Widget getButtonBar() {
		HorizontalPanel bar = new HorizontalPanel();
		
		PushButton refresh = getRefreshButton();
		PushButton drop = getDropButton();
		PushButton rename = getRenameButton();
		PushButton create = getCreateButton();
		
		bar.add(refresh);
		bar.add(rename);		
		bar.add(drop);
		bar.add(create);
		
		return bar.asWidget();
	}
	
	private PushButton getRefreshButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.refresh()));
		button.setTitle("Refresh");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();				
			}			
		});
		return button;
	}
	
	private PushButton getDropButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.drop()));
		button.setTitle("Drop Trigger");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionModel.getSelectedObject() != null
						&& !"".equals(selectionModel.getSelectedObject()
								.getName())) {
					DropItemObjectPopUp pop = new DropItemObjectPopUp();
					pop.setSelectionModel(selectionModel);
					pop.setDataProvider(dataProvider);
					pop.setItem(item);
					pop.setObject(selectionModel.getSelectedObject().getName());
					pop.setObjectType(ITEM_OBJECT_TYPE.TRIGGER);
					try {
						pop.getDialogBox();
					} catch (PopUpException caught) {
						Window.alert(caught.getMessage());
					}
				}
			}
		});
		return button;
	}

	private PushButton getCreateButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.create()));
		button.setTitle("Create Trigger");
		
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AddTriggerPopUp pop = new AddTriggerPopUp();
				pop.setSelectionModel(selectionModel);
				pop.setDataProvider(dataProvider);
				pop.setItem(item);
				try {
					pop.getDialogBox();
				} catch (PopUpException caught) {
					Window.alert(caught.getMessage());
				}
			}
		});

		return button;
	}

	private PushButton getRenameButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.rename()));
		button.setTitle("Rename Trigger");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionModel.getSelectedObject() != null
						&& !"".equals(selectionModel.getSelectedObject()
								.getName())) {

					RenameItemObjectPopUp pop = new RenameItemObjectPopUp();
					pop.setSelectionModel(selectionModel);
					pop.setDataProvider(dataProvider);
					pop.setItem(item);
					pop.setObjectType(ITEM_OBJECT_TYPE.TRIGGER);
					try {
						pop.getDialogBox();
					} catch (PopUpException caught) {
						Window.alert(caught.getMessage());
					}
				}
			}
		});
		return button;
	}

	private Widget getMainPanel() {
		SimplePanel panel = new SimplePanel();
		panel.setWidth("100%");
		panel.setHeight("100%");

		dataGrid = new DataGrid<TriggerInfo>(MAX_TRIGGERS, TriggerInfo.KEY_PROVIDER);
		dataGrid.setHeight(MAIN_HEIGHT);
		
		Column<TriggerInfo, String> columnName = addColumn(new TextCell(), "Trigger Name", new GetValue<String>() {
	        public String getValue(TriggerInfo index) {
	          return index.getName();
	        }
	      }, null);

		
		Column<TriggerInfo, ImageResource> deferrable = addColumn(new ImageResourceCell(), "Deferrable", new GetValue<ImageResource>() {
	        public ImageResource getValue(TriggerInfo index) {
	        	if (index.isDeferrable()) {
	        		return PgStudio.Images.trueBox();
	        	}
	          return PgStudio.Images.falseBox();
	        }
	      }, null);

		Column<TriggerInfo, ImageResource> initDeferrable = addColumn(new ImageResourceCell(), "Initially Deferred", new GetValue<ImageResource>() {
	        public ImageResource getValue(TriggerInfo index) {
	        	if (index.isInitDeferrable()) {
	        		return PgStudio.Images.trueBox();
	        	}
	          return PgStudio.Images.falseBox();
	        }
	      }, null);

		dataGrid.setColumnWidth(columnName, "200px");
		dataGrid.setColumnWidth(deferrable, "100px");
		dataGrid.setColumnWidth(initDeferrable, "100px");

		deferrable.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		initDeferrable.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);		

		dataGrid.setLoadingIndicator(new Image(PgStudio.Images.spinner()));

		dataProvider.addDataDisplay(dataGrid);

		dataGrid.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler((new 
				SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						TriggerInfo trigger = selectionModel.getSelectedObject();
						triggerDef.setText(trigger.getDefinition());
					}			
		}));

		panel.add(dataGrid);
		
		return panel.asWidget();
	}
	

	private <C> Column<TriggerInfo, C> addColumn(Cell<C> cell, String headerText,
		      final GetValue<C> getter, FieldUpdater<TriggerInfo, C> fieldUpdater) {
		    Column<TriggerInfo, C> column = new Column<TriggerInfo, C>(cell) {
		      @Override
		      public C getValue(TriggerInfo object) {
		        return getter.getValue(object);
		      }
		    };
		    column.setFieldUpdater(fieldUpdater);

		    dataGrid.addColumn(column, headerText);
		    return column;
	}

	private Widget getDetailSection() {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setWidth("100%");
		panel.setStyleName("studio-Bottom-Panel");
				
		VerticalPanel triggerDefPanel = new VerticalPanel();
		triggerDefPanel.setWidth("95%");

		Label lbl = new Label();
		lbl.setText("Trigger Definition");
		lbl.setStyleName("studio-Label-Small");

		triggerDef = new TextArea();
		triggerDef.setWidth("100%");
		triggerDef.setVisibleLines(5);
		triggerDef.setReadOnly(true);
		
		triggerDefPanel.add(lbl);
		triggerDefPanel.add(triggerDef);
		
		panel.add(triggerDefPanel);
				
		return panel.asWidget();
	}

	@Override
	public void refresh() {
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
}
