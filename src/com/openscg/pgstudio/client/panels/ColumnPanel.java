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
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.models.ColumnInfo;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.panels.popups.AddColumnPopUp;
import com.openscg.pgstudio.client.panels.popups.DropItemObjectPopUp;
import com.openscg.pgstudio.client.panels.popups.PopUpException;
import com.openscg.pgstudio.client.panels.popups.RenameItemObjectPopUp;
import com.openscg.pgstudio.client.providers.ColumnListDataProvider;

public class ColumnPanel extends Composite implements DetailsPanel {

	private static interface GetValue<C> {
	    C getValue(ColumnInfo column);
	  }

	private DataGrid<ColumnInfo> dataGrid;
    
	private ColumnListDataProvider dataProvider = new ColumnListDataProvider();

    private final SingleSelectionModel<ColumnInfo> selectionModel = 
        	new SingleSelectionModel<ColumnInfo>(ColumnInfo.KEY_PROVIDER);
	
	PushButton create = null;
	Label commentLbl = null;
	TextArea itemComment = null;
	TextArea colComment = null;

    private ColumnListDataProvider columnListDataProvider = new ColumnListDataProvider();
    
    private ModelInfo item = null;

	private String TEXT_WIDTH = "300px";
	
	private String MAIN_HEIGHT = "300px";
	
	public static int ROWS_PER_PAGE = 10;
	
	public static int MAX_COLUMNS = 1600;
	
	public void setItem(ModelInfo item) {
		if (item == null)
			return;
		
		this.item = item;
		
		if (item.getComment() != null)
			itemComment.setText(item.getComment());
		else
			itemComment.setText("");

		if (commentLbl != null) {
			if (item.getItemType() == ITEM_TYPE.TABLE || item.getItemType() == ITEM_TYPE.FOREIGN_TABLE)
				commentLbl.setText("Table comment");
			else if (item.getItemType() == ITEM_TYPE.VIEW || item.getItemType() == ITEM_TYPE.MATERIALIZED_VIEW)
				commentLbl.setText("View comment");
			else
				commentLbl.setText("Comment");
		}

		if (create != null) {
			if (item.getItemType().equals(ITEM_TYPE.TABLE)) {
				create.setEnabled(true);
				create.setVisible(true);
			} else {
				create.setEnabled(false);
				create.setVisible(false);
			}
		}
		
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
	
	public ColumnPanel() {
		
		VerticalPanel panel = new VerticalPanel();

		panel.add(getButtonBar());
		panel.add(getMainPanel());
		panel.add(getCommentSection());
				
		initWidget(panel);
	}

	private Widget getButtonBar() {
		HorizontalPanel bar = new HorizontalPanel();
		
		PushButton refresh = getRefreshButton();
		PushButton drop = getDropButton();
		PushButton rename = getRenameButton();
		create = getCreateButton();

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
		button.setTitle("Drop Column");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(selectionModel.getSelectedObject() != null && !"".equals(selectionModel.getSelectedObject().getName())){
					DropItemObjectPopUp pop = new DropItemObjectPopUp();
					pop.setSelectionModel(selectionModel);
					pop.setDataProvider(dataProvider);
					pop.setItem(item);
					pop.setObject(selectionModel.getSelectedObject().getName());
					pop.setObjectType(ITEM_OBJECT_TYPE.COLUMN);
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
		button.setTitle("Create Column");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AddColumnPopUp pop = new AddColumnPopUp();
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
		button.setTitle("Rename Column");
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
					pop.setObjectType(ITEM_OBJECT_TYPE.COLUMN);
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
		
		dataGrid = new DataGrid<ColumnInfo>(MAX_COLUMNS, ColumnInfo.KEY_PROVIDER);
		dataGrid.setHeight(MAIN_HEIGHT);
		
		Column<ColumnInfo, String> columnName = addColumn(new TextCell(), "Column Name", new GetValue<String>() {
	        public String getValue(ColumnInfo column) {
	          return column.getName();
	        }
	      }, null);

		Column<ColumnInfo, ImageResource> distributionKey = addColumn(new ImageResourceCell(), "DK", new GetValue<ImageResource>() {
	        public ImageResource getValue(ColumnInfo column) {
	        	if (column.isDistributionKey()) {
	        		return PgStudio.Images.distributionKey();
	        	}
	          return null;
	        }
	      }, null);

		Column<ColumnInfo, ImageResource> primaryKey = addColumn(new ImageResourceCell(), "PK", new GetValue<ImageResource>() {
	        public ImageResource getValue(ColumnInfo column) {
	        	if (column.isPrimaryKey()) {
	        		return PgStudio.Images.primaryKey();
	        	}
	          return null;
	        }
	      }, null);

		Column<ColumnInfo, String> dataType = addColumn(new TextCell(), "Data Type", new GetValue<String>() {
	        public String getValue(ColumnInfo column) {
	          return column.getDataType();
	        }
	      }, null);

		Column<ColumnInfo, ImageResource> nullable = addColumn(new ImageResourceCell(), "Nullable", new GetValue<ImageResource>() {
	        public ImageResource getValue(ColumnInfo column) {
	        	if (column.getName() == null) {
	        		return null;
	        	}

	        	if (!column.isNullable()) {
	        		return PgStudio.Images.nullable();
	        	}
	          return null;
	        }
	      }, null);

		Column<ColumnInfo, String> defaultCol = addColumn(new TextCell(), "Default", new GetValue<String>() {
	        public String getValue(ColumnInfo column) {
	          return column.getDefault();
	        }
	      }, null);

	    
		dataGrid.setColumnWidth(columnName, "170px");
		dataGrid.setColumnWidth(distributionKey, "35px");
		dataGrid.setColumnWidth(primaryKey, "35px");
		dataGrid.setColumnWidth(dataType, "210px");
		dataGrid.setColumnWidth(nullable, "70px");
		dataGrid.setColumnWidth(defaultCol, "180px");
		
		primaryKey.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		nullable.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);		

		dataGrid.setLoadingIndicator(new Image(PgStudio.Images.spinner()));

		dataProvider.addDataDisplay(dataGrid);

		panel.add(dataGrid);
		
		dataGrid.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler((new 
				SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						ColumnInfo index = selectionModel.getSelectedObject();
						columnListDataProvider.setItem(index.getSchema(), index.getId(), index.getItemType());
						
						if (colComment != null)
							colComment.setText(index.getComment());
					}			
		}));
		
		return panel.asWidget();
	}
	

	private <C> Column<ColumnInfo, C> addColumn(Cell<C> cell, String headerText,
		      final GetValue<C> getter, FieldUpdater<ColumnInfo, C> fieldUpdater) {
		    Column<ColumnInfo, C> column = new Column<ColumnInfo, C>(cell) {
		      @Override
		      public C getValue(ColumnInfo object) {
		        return getter.getValue(object);
		      }
		    };
		    column.setFieldUpdater(fieldUpdater);

		    dataGrid.addColumn(column, headerText);
		    return column;
	}

	private Widget getCommentSection() {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setWidth("100%");
		panel.setStyleName("studio-Bottom-Panel");
		
		VerticalPanel left = new VerticalPanel();
		left.setWidth("95%");
		
		commentLbl = new Label();		
		commentLbl.setText("Comment");
		commentLbl.setStyleName("studio-Label-Small");

		itemComment = new TextArea();
		itemComment.setWidth("100%");
		itemComment.setVisibleLines(3);
		itemComment.setReadOnly(true);
				
		left.add(commentLbl);
		left.add(itemComment);

		VerticalPanel right = new VerticalPanel();
		right.setWidth("95%");

		Label rightLbl = new Label();
		rightLbl.setText("Column comment");
		rightLbl.setStyleName("studio-Label-Small");

		colComment = new TextArea();
		colComment.setWidth("100%");
		colComment.setVisibleLines(3);
		colComment.setReadOnly(true);
		
		right.add(rightLbl);
		right.add(colComment);
		
		panel.add(left);
		panel.add(PgStudio.filler);
		panel.add(right);
				
		return panel.asWidget();
	}
	
	@Override
	public void refresh() {
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
}