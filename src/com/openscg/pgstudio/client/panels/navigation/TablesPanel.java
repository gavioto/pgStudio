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

import java.util.ArrayList;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.TableInfo;
import com.openscg.pgstudio.client.models.TableInfo.TABLE_TYPE;
import com.openscg.pgstudio.client.panels.popups.AddTablePopUp;
import com.openscg.pgstudio.client.panels.popups.AnalyzePopUp;
import com.openscg.pgstudio.client.panels.popups.DropItemPopUp;
import com.openscg.pgstudio.client.panels.popups.PopUpException;
import com.openscg.pgstudio.client.panels.popups.RenameItemPopUp;
import com.openscg.pgstudio.client.panels.popups.TruncateTablePopUp;
import com.openscg.pgstudio.client.providers.TableListDataProvider;

public class TablesPanel extends Composite implements MenuPanel {

	private static interface GetValue<C> {
	    C getValue(TableInfo object);
	  }

	private DatabaseObjectInfo schema = null;	
	private DataGrid<TableInfo> dataGrid;
	private TableListDataProvider dataProvider = new TableListDataProvider();
	final static ArrayList<String> temp = new ArrayList<String>();
	
	DialogBox dialogBox = null;

	private final SingleSelectionModel<TableInfo> selectionModel = 
    	new SingleSelectionModel<TableInfo>(TableInfo.KEY_PROVIDER);

	private final PgStudio main;
	
	public void setSchema(DatabaseObjectInfo schema) {
		this.schema = schema;
		dataProvider.setSchema(schema);
	}
	
	public TablesPanel(PgStudio main) {
		this.main = main;
		
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("95%");
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		panel.add(getButtonBar());
		panel.add(getTableList());
				
		dataGrid.addCellPreviewHandler(new CellPreviewEvent.Handler<TableInfo>() {
			@Override
			public void onCellPreview(CellPreviewEvent<TableInfo> event) {
				if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType())) {
					if (dataGrid.getRowCount() == 1) {
						TableInfo i = dataProvider.getList().get(0);

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
		PushButton rename = getRenameButton();
		PushButton create = getCreateButton();
		PushButton analyze = getAnalyzeButton();
		PushButton truncate = getTruncateButton();


		bar.add(refresh);
		bar.add(rename);
		bar.add(analyze);
		bar.add(truncate);
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
		button.setTitle("Drop Table");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(selectionModel.getSelectedObject() != null && !"".equals(selectionModel.getSelectedObject().getName())){
					DropItemPopUp pop = new DropItemPopUp();
					pop.setSelectionModel(selectionModel);
					pop.setDataProvider(dataProvider);
					pop.setSchema(schema);
					pop.setItemType(ITEM_TYPE.TABLE);
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
		PushButton button = new PushButton(new Image(PgStudio.Images.create()));
		button.setTitle("Create Table");
		
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AddTablePopUp pop = new AddTablePopUp();
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


	private PushButton getRenameButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.rename()));
		button.setTitle("Rename Table");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionModel.getSelectedObject() != null
						&& !"".equals(selectionModel.getSelectedObject()
								.getName())) {
					RenameItemPopUp pop = new RenameItemPopUp(ITEM_TYPE.TABLE);
					pop.setSelectionModel(selectionModel);
					pop.setDataProvider(dataProvider);
					pop.setSchema(schema);
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

	private PushButton getAnalyzeButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.analyze()));
		button.setTitle("Analyze");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionModel.getSelectedObject() != null
						&& !"".equals(selectionModel.getSelectedObject()
								.getName())) {
					AnalyzePopUp pop = new AnalyzePopUp(true);
					pop.setSelectionModel(selectionModel);
					pop.setDataProvider(dataProvider);
					pop.setSchema(schema);
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

	private PushButton getTruncateButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.truncate()));
		button.setTitle("Truncate");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(selectionModel.getSelectedObject() != null && !"".equals(selectionModel.getSelectedObject().getName())){
					TruncateTablePopUp pop = new TruncateTablePopUp();
					pop.setSelectionModel(selectionModel);
					pop.setDataProvider(dataProvider);
					pop.setSchema(schema);
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

	private Widget getTableList() {
		dataGrid = new DataGrid<TableInfo>(PgStudio.MAX_PANEL_ITEMS, TableInfo.KEY_PROVIDER);
		dataGrid.setHeight(PgStudio.LEFT_PANEL_HEIGHT);
		dataGrid.setLoadingIndicator(new Image(PgStudio.Images.spinner()));

		Column<TableInfo, ImageResource> icon = addColumn(new ImageResourceCell(), "", new GetValue<ImageResource>() {
	        public ImageResource getValue(TableInfo column) {
	        	if (column.getType() == TABLE_TYPE.HASH_PARTITIONED) {
	        		return PgStudio.Images.tableDistributed();
	        	}
	        	
        		return PgStudio.Images.table();
	        }
	      }, null);

		addColumn(new TextCell(), "", new GetValue<String>() {
	        public String getValue(TableInfo column) {
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

	
	private <C> Column<TableInfo, C> addColumn(Cell<C> cell, String headerText,
		      final GetValue<C> getter, FieldUpdater<TableInfo, C> fieldUpdater) {
		    Column<TableInfo, C> column = new Column<TableInfo, C>(cell) {
		      @Override
		      public C getValue(TableInfo object) {
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
				TableInfo t = dataProvider.getList().get(0);
				dataGrid.getSelectionModel().setSelected(t, true);
				main.setSelectedItem(t);
				return true;
			}
		}
		
		return false;
	}
}