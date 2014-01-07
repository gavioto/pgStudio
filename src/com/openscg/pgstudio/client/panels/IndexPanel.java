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
import com.openscg.pgstudio.client.models.IndexInfo;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.models.StatsInfo;
import com.openscg.pgstudio.client.panels.popups.AddIndexPopUp;
import com.openscg.pgstudio.client.panels.popups.DropItemObjectPopUp;
import com.openscg.pgstudio.client.panels.popups.PopUpException;
import com.openscg.pgstudio.client.panels.popups.RenameItemObjectPopUp;
import com.openscg.pgstudio.client.providers.IndexListDataProvider;
import com.openscg.pgstudio.client.providers.StatsListDataProvider;

public class IndexPanel extends Composite implements DetailsPanel {
	
	private static interface GetValue<C> {
	    C getValue(IndexInfo column);
	}

	private DataGrid<IndexInfo> dataGrid;
    private IndexListDataProvider dataProvider = new IndexListDataProvider();

	private DataGrid<StatsInfo> dataDetailGrid;
    private StatsListDataProvider dataDetailProvider = new StatsListDataProvider();

    private TextArea indexDef;
    
    private ModelInfo item = null;
    
    private final SingleSelectionModel<IndexInfo> selectionModel = 
        	new SingleSelectionModel<IndexInfo>(IndexInfo.KEY_PROVIDER);

	private String MAIN_HEIGHT = "250px";
	
	public static int ROWS_PER_PAGE = 5;
	public static int MAX_INDEXES = 1600;
	
	public void setItem(ModelInfo item) {
		this.item = item;
		indexDef.setText("");
		dataDetailProvider.setItem(item.getId(), item.getItemType());
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
	
	public IndexPanel() {
		
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
		button.setTitle("Drop Index");
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
					pop.setObjectType(ITEM_OBJECT_TYPE.INDEX);
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
		button.setTitle("Create Index");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AddIndexPopUp pop = new AddIndexPopUp();
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
		button.setTitle("Rename Index");
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
					pop.setObjectType(ITEM_OBJECT_TYPE.INDEX);
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

		
		dataGrid = new DataGrid<IndexInfo>(MAX_INDEXES, IndexInfo.KEY_PROVIDER);
		dataGrid.setHeight(MAIN_HEIGHT);
		
		Column<IndexInfo, String> columnName = addColumn(new TextCell(), "Index Name", new GetValue<String>() {
	        public String getValue(IndexInfo index) {
	          return index.getName();
	        }
	      }, null);

		Column<IndexInfo, String> accessMethod = addColumn(new TextCell(), "Access Method", new GetValue<String>() {
	        public String getValue(IndexInfo index) {
	          return index.getAccessMethod().toUpperCase();
	        }
	      }, null);

		Column<IndexInfo, ImageResource> primaryKey = addColumn(new ImageResourceCell(), "Primary Key", new GetValue<ImageResource>() {
	        public ImageResource getValue(IndexInfo index) {
	        	if (index.isPrimaryKey()) {
	        		return PgStudio.Images.primaryKey();
	        	}
	          return null;
	        }
	      }, null);

		Column<IndexInfo, ImageResource> unique = addColumn(new ImageResourceCell(), "Unique", new GetValue<ImageResource>() {
	        public ImageResource getValue(IndexInfo index) {
	        	if (index.isUnique()) {
	        		return PgStudio.Images.indexUnique();
	        	}
        		return PgStudio.Images.indexNotUnique();
	        }
	      }, null);

		Column<IndexInfo, ImageResource> partial = addColumn(new ImageResourceCell(), "Partial", new GetValue<ImageResource>() {
	        public ImageResource getValue(IndexInfo index) {
	        	if (index.isPartial()) {
	        		return PgStudio.Images.partial();
	        	}
        		return null;
	        }
	      }, null);

		dataGrid.setColumnWidth(columnName, "200px");
		dataGrid.setColumnWidth(accessMethod, "120px");
		dataGrid.setColumnWidth(primaryKey, "100px");
		dataGrid.setColumnWidth(unique, "100px");
		dataGrid.setColumnWidth(partial, "100px");

		accessMethod.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		primaryKey.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		unique.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);		
		partial.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);		

		dataGrid.setLoadingIndicator(new Image(PgStudio.Images.spinner()));

		dataProvider.addDataDisplay(dataGrid);

		dataGrid.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler((new 
				SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						IndexInfo index = selectionModel.getSelectedObject();
						indexDef.setText(index.getDefinition());
						dataDetailProvider.setItem(index.getId(), item.getItemType());
					}			
		}));

		panel.add(dataGrid);
		
		return panel.asWidget();
	}
	

	private <C> Column<IndexInfo, C> addColumn(Cell<C> cell, String headerText,
		      final GetValue<C> getter, FieldUpdater<IndexInfo, C> fieldUpdater) {
		    Column<IndexInfo, C> column = new Column<IndexInfo, C>(cell) {
		      @Override
		      public C getValue(IndexInfo object) {
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
		panel.setHeight("100%");

		panel.setStyleName("studio-Bottom-Panel");
		
		VerticalPanel left = new VerticalPanel();
		left.setWidth("95%");
		
		Label leftLbl = new Label();
		leftLbl.setText("Index Statistics");
		leftLbl.setStyleName("studio-Label-Small");

		dataDetailGrid = new DataGrid<StatsInfo>(8, StatsInfo.KEY_PROVIDER);
		dataDetailGrid.setWidth("100%");
		dataDetailGrid.setHeight("100px");
		
		Column<StatsInfo, String> Name =
		        new Column<StatsInfo, String>(new TextCell()) {
		          @Override
		          public String getValue(StatsInfo object) {
		            return object.getName();
		          }
		 };

		Column<StatsInfo, String> Value =
		        new Column<StatsInfo, String>(new TextCell()) {
		          @Override
		          public String getValue(StatsInfo object) {
		            return object.getValue();
		          }
		};

		dataDetailGrid.addColumn(Name, "");
		dataDetailGrid.addColumn(Value, "");

		dataDetailGrid.setLoadingIndicator(new Image(PgStudio.Images.spinner()));
		dataDetailProvider.addDataDisplay(dataDetailGrid);
		
		left.add(leftLbl);
		left.add(dataDetailGrid);

		VerticalPanel right = new VerticalPanel();
		right.setWidth("95%");

		Label rightLbl = new Label();
		rightLbl.setText("Index Definition");
		rightLbl.setStyleName("studio-Label-Small");

		indexDef = new TextArea();
		indexDef.setWidth("100%");
		indexDef.setVisibleLines(5);
		indexDef.setReadOnly(true);
		
		right.add(rightLbl);
		right.add(indexDef);
		
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
