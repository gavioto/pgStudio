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
package com.openscg.pgstudio.client.panels.navigation;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
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
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.Resources;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.TypeInfo;
import com.openscg.pgstudio.client.models.TypeInfo.TYPE_KIND;
import com.openscg.pgstudio.client.panels.popups.AddTypePopUp;
import com.openscg.pgstudio.client.panels.popups.DropItemPopUp;
import com.openscg.pgstudio.client.panels.popups.PopUpException;
import com.openscg.pgstudio.client.providers.TypeListDataProvider;

public class TypesPanel extends Composite implements MenuPanel {

	private static final Resources Images =  GWT.create(Resources.class);
	
	private static interface GetValue<C> {
	    C getValue(TypeInfo object);
	  }

	private DatabaseObjectInfo schema = null;

	private DataGrid<TypeInfo> dataGrid;
    private TypeListDataProvider dataProvider = new TypeListDataProvider();
	
    private final SingleSelectionModel<TypeInfo> selectionModel = 
    	new SingleSelectionModel<TypeInfo>(TypeInfo.KEY_PROVIDER);
        
	private final PgStudio main;
	
	public void setSchema(DatabaseObjectInfo schema) {
		this.schema = schema;
		dataProvider.setSchema(schema);
	}
	
	public TypesPanel(PgStudio main) {
		this.main = main;
		
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("95%");

		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		panel.add(getButtonBar());
		panel.add(getTypeList());		
		
		dataGrid.addCellPreviewHandler(new CellPreviewEvent.Handler<TypeInfo>() {
			@Override
			public void onCellPreview(CellPreviewEvent<TypeInfo> event) {
				if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType())) {
					if (dataGrid.getRowCount() == 1) {
						TypeInfo i = dataProvider.getList().get(0);

						if (dataGrid.getSelectionModel().isSelected(i)) {
							selectFirst();
						}
					}
	            }
			}
		});

		initWidget(panel);
	}

	private Widget getButtonBar() {
		HorizontalPanel bar = new HorizontalPanel();
		
		PushButton refresh = getRefreshButton();
		PushButton drop = getDropButton();
		PushButton create = getCreateButton();
		
		bar.add(refresh);
		bar.add(drop);
		bar.add(create);
		
		return bar.asWidget();
	}
	
	private PushButton getRefreshButton() {
		PushButton button = new PushButton(new Image(Images.refresh()));
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
		PushButton button = new PushButton(new Image(Images.drop()));
		button.setTitle("Drop Type");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(selectionModel.getSelectedObject() != null && !"".equals(selectionModel.getSelectedObject().getName())){
					DropItemPopUp pop = new DropItemPopUp();
					pop.setSelectionModel(selectionModel);
					pop.setDataProvider(dataProvider);
					pop.setSchema(schema);
					pop.setItemType(ITEM_TYPE.TYPE);
					pop.setItem(selectionModel.getSelectedObject().getName());
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
		PushButton button = new PushButton(new Image(Images.create()));
		button.setTitle("Create Type");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AddTypePopUp pop = new AddTypePopUp(main.getDatabaseVersion());
				pop.setSelectionModel(selectionModel);
				pop.setDataProvider(dataProvider);
				pop.setSchema(schema);
				try {
					pop.getDialogBox();
				} catch (PopUpException caught) {
					Window.alert(caught.getMessage());
				}
			}
		});

		return button;
	}

	private Widget getTypeList() {
		dataGrid = new DataGrid<TypeInfo>(PgStudio.MAX_PANEL_ITEMS, TypeInfo.KEY_PROVIDER);
		dataGrid.setHeight(PgStudio.LEFT_PANEL_HEIGHT);

	
		Column<TypeInfo, ImageResource> icon = addColumn(new ImageResourceCell(), "", new GetValue<ImageResource>() {
	        public ImageResource getValue(TypeInfo column) {
	        	if (column.getKind() == TYPE_KIND.COMPOSITE) {
	        		return Images.composite();
	        	} else if (column.getKind() == TYPE_KIND.DOMAIN) {
	        		return Images.domain();
	        	} else if (column.getKind() == TYPE_KIND.RANGE) {
	        		return Images.range();
	        	}
	        	
	        	return Images.enumuration();
	        }
	      }, null);

		Column<TypeInfo, String> typeName = addColumn(new TextCell(), "", new GetValue<String>() {
	        public String getValue(TypeInfo column) {
	          return column.getName();
	        }
	      }, null);

		dataGrid.setColumnWidth(icon, "35px");
		icon.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		dataProvider.addDataDisplay(dataGrid);

		dataGrid.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler((Handler) main.getSelectionChangeHandler());

		return dataGrid.asWidget();
	}
	
	private <C> Column<TypeInfo, C> addColumn(Cell<C> cell, String headerText,
		      final GetValue<C> getter, FieldUpdater<TypeInfo, C> fieldUpdater) {
		    Column<TypeInfo, C> column = new Column<TypeInfo, C>(cell) {
		      @Override
		      public C getValue(TypeInfo object) {
		        return getter.getValue(object);
		      }
		    };
		    column.setFieldUpdater(fieldUpdater);

		    dataGrid.addColumn(column, headerText);
		    return column;
	}

	public void refresh() {
		dataProvider.setSchema(schema);	
	}
	
	@Override
	public Boolean selectFirst() {
		if (dataProvider != null) {
			if (!dataProvider.getList().isEmpty()) {
				TypeInfo i = dataProvider.getList().get(0);
				dataGrid.getSelectionModel().setSelected(i, true);
				main.setSelectedItem(i);
				return true;
			}
		}
		
		return false;
	}

}
