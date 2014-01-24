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
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.openscg.pgstudio.client.Resources;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.ViewInfo;
import com.openscg.pgstudio.client.panels.popups.AddViewPopUp;
import com.openscg.pgstudio.client.panels.popups.DropItemPopUp;
import com.openscg.pgstudio.client.panels.popups.PopUpException;
import com.openscg.pgstudio.client.panels.popups.RefreshMaterializedViewPopUp;
import com.openscg.pgstudio.client.panels.popups.RenameItemPopUp;
import com.openscg.pgstudio.client.providers.ViewListDataProvider;

public class ViewsPanel extends Composite implements MenuPanel {

	private static final Resources Images =  GWT.create(Resources.class);
	
	private static interface GetValue<C> {
	    C getValue(ViewInfo object);
	  }

	private DatabaseObjectInfo schema = null;

	private DataGrid<ViewInfo> dataGrid;
	
    private ViewListDataProvider dataProvider = new ViewListDataProvider();
	
    DialogBox dialogBox = null;
    
    PushButton refreshView;
    
    private final SingleSelectionModel<ViewInfo> selectionModel = 
    	new SingleSelectionModel<ViewInfo>(ViewInfo.KEY_PROVIDER);
        
	private final PgStudio main;
	
	public void setSchema(DatabaseObjectInfo schema) {
		this.schema = schema;
		dataProvider.setSchema(schema);
	}
	
	public ViewsPanel(final PgStudio main) {
		this.main = main;
		
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("95%");

		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		panel.add(getButtonBar());
		panel.add(getViewList());		
						
		dataGrid.addCellPreviewHandler(new CellPreviewEvent.Handler<ViewInfo>() {
			@Override
			public void onCellPreview(CellPreviewEvent<ViewInfo> event) {
				if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType())) {
					if (dataGrid.getRowCount() == 1) {
						ViewInfo i = dataProvider.getList().get(0);

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

		bar.add(refresh);
		bar.add(rename);
		bar.add(drop);
		bar.add(create);
		
		if (main.getDatabaseVersion() >= 930) {
			refreshView = getRefreshViewButton();
			bar.add(refreshView);
		}
		
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
		button.setTitle("Drop View");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionModel.getSelectedObject() != null
						&& !"".equals(selectionModel.getSelectedObject()
								.getName())) {
					DropItemPopUp pop = new DropItemPopUp();
					pop.setSelectionModel(selectionModel);
					pop.setDataProvider(dataProvider);
					pop.setSchema(schema);
					pop.setItemType(selectionModel.getSelectedObject().getItemType());
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
		button.setTitle("Create View");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AddViewPopUp pop = new AddViewPopUp(main.getDatabaseVersion());
				pop.setSelectionModel(selectionModel);
				pop.setDataProvider(dataProvider);
				pop.setSchema(schema);
				try {
					pop.getDialogBox();
				} catch (PopUpException caught) {
					Window.alert(caught.getMessage());
				}
				pop.setupCodePanel();
			}
		});
		return button;
	}

	private PushButton getRenameButton() {
		PushButton button = new PushButton(new Image(Images.rename()));
		button.setTitle("Rename View");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionModel.getSelectedObject() != null
						&& !"".equals(selectionModel.getSelectedObject()
								.getName())) {
					RenameItemPopUp pop = new RenameItemPopUp(selectionModel.getSelectedObject().getItemType());
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

	private PushButton getRefreshViewButton() {
		PushButton button = new PushButton(new Image(Images.refreshView()));
		button.setTitle("Refresh Materialized View");

		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionModel.getSelectedObject() != null
						&& !"".equals(selectionModel.getSelectedObject()
								.getName())) {
					if (selectionModel.getSelectedObject().getItemType() == ITEM_TYPE.MATERIALIZED_VIEW) {
						RefreshMaterializedViewPopUp pop = new RefreshMaterializedViewPopUp();
						pop.setSelectionModel(selectionModel);
						pop.setDataProvider(dataProvider);
						pop.setSchema(schema);
						pop.setItem(selectionModel.getSelectedObject()
								.getName());
						try {
							pop.getDialogBox();
						} catch (PopUpException caught) {
							Window.alert(caught.getMessage());
						}
					}
				}

			}
		});

		return button;
	}

	private Widget getViewList() {
		dataGrid = new DataGrid<ViewInfo>(PgStudio.MAX_PANEL_ITEMS, ViewInfo.KEY_PROVIDER);
		dataGrid.setHeight(PgStudio.LEFT_PANEL_HEIGHT);

	
		Column<ViewInfo, ImageResource> icon = addColumn(new ImageResourceCell(), "", new GetValue<ImageResource>() {
	        public ImageResource getValue(ViewInfo column) {
	        	if (column.isMaterialized())
	        		return Images.materializedView();
	        				
        		return Images.view();
	        }
	      }, null);

		Column<ViewInfo, String> viewName = addColumn(new TextCell(), "", new GetValue<String>() {
	        public String getValue(ViewInfo column) {
	          return column.getName();
	        }
	      }, null);

		dataGrid.setColumnWidth(icon, "35px");
		icon.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		dataProvider.addDataDisplay(dataGrid);

		dataGrid.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler((Handler) main.getSelectionChangeHandler());
		
		if (main.getDatabaseVersion() >= 930) {
			selectionModel.addSelectionChangeHandler(new Handler() {
				@Override
				public void onSelectionChange(SelectionChangeEvent event) {
					if (selectionModel.getSelectedObject().isMaterialized()) {
						refreshView.setVisible(true);
						refreshView.setEnabled(true);
					} else {
						refreshView.setVisible(false);
						refreshView.setEnabled(false);
					}
				}
			});
		}
		
		return dataGrid.asWidget();
	}
	
	private <C> Column<ViewInfo, C> addColumn(Cell<C> cell, String headerText,
		      final GetValue<C> getter, FieldUpdater<ViewInfo, C> fieldUpdater) {
		    Column<ViewInfo, C> column = new Column<ViewInfo, C>(cell) {
		      @Override
		      public C getValue(ViewInfo object) {
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
				ViewInfo v = dataProvider.getList().get(0);
				dataGrid.getSelectionModel().setSelected(v, true);
				main.setSelectedItem(v);
				return true;
			}
		}
		
		return false;
	}
}
