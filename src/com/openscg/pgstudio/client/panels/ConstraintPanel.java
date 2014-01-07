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
import com.openscg.pgstudio.client.models.ConstraintInfo;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.panels.popups.AddConstraintPopUp;
import com.openscg.pgstudio.client.panels.popups.DropItemObjectPopUp;
import com.openscg.pgstudio.client.panels.popups.PopUpException;
import com.openscg.pgstudio.client.panels.popups.RenameItemObjectPopUp;
import com.openscg.pgstudio.client.providers.ConstraintListDataProvider;
import com.openscg.pgstudio.client.providers.StatsListDataProvider;

public class ConstraintPanel extends Composite implements DetailsPanel {

	private static interface GetValue<C> {
	    C getValue(ConstraintInfo column);
	}

	private ModelInfo item = null;
	private DataGrid<ConstraintInfo> dataGrid;
    private ConstraintListDataProvider dataProvider = new ConstraintListDataProvider();

    private StatsListDataProvider dataDetailProvider = new StatsListDataProvider();

    private TextArea constDef;
    
    private final SingleSelectionModel<ConstraintInfo> selectionModel = 
        	new SingleSelectionModel<ConstraintInfo>(ConstraintInfo.KEY_PROVIDER);

	private String MAIN_HEIGHT = "250px";
	
	public static int ROWS_PER_PAGE = 5;
	public static int MAX_INDEXES = 1600;
	
	public void setItem(ModelInfo item) {
		this.item = item;
		constDef.setText("");
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
	
	public ConstraintPanel() {
		
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
		
		return button;
	}
	
	private PushButton getDropButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.drop()));
		button.setTitle("Drop Constraint");
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
					pop.setObjectType(ITEM_OBJECT_TYPE.CONSTRAINT);
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
		button.setTitle("Create Constraint");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AddConstraintPopUp pop = new AddConstraintPopUp();
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
		button.setTitle("Rename Constraint");
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
					pop.setObjectType(ITEM_OBJECT_TYPE.CONSTRAINT);
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
				
		dataGrid = new DataGrid<ConstraintInfo>(MAX_INDEXES, ConstraintInfo.KEY_PROVIDER);
		dataGrid.setHeight(MAIN_HEIGHT);

		Column<ConstraintInfo, ImageResource> typeImg = addColumn(new ImageResourceCell(), "", new GetValue<ImageResource>() {
	        public ImageResource getValue(ConstraintInfo c) {
	        	if (c.getType().equals("c")) {
	        		return PgStudio.Images.constraintCheck();
	        	} else if (c.getType().equals("f")) {
	        		return PgStudio.Images.constraintForeignKey();
	        	} else if (c.getType().equals("p")) {
	        		return PgStudio.Images.constraintPrimaryKey();
	        	} else if (c.getType().equals("u")) {
	        		return PgStudio.Images.constraintUniqueKey();
	        	} else if (c.getType().equals("t")) {
	        		return PgStudio.Images.constraintTrigger();
	        	} else if (c.getType().equals("x")) {
	        		return PgStudio.Images.constraintExclusion();
	        	} 
	        		
	          return null;
	        }
	      }, null);

		Column<ConstraintInfo, String> constName = addColumn(new TextCell(), "Constraint Name", new GetValue<String>() {
	        public String getValue(ConstraintInfo c) {
	          return c.getName();
	        }
	      }, null);

		Column<ConstraintInfo, String> type = addColumn(new TextCell(), "Type", new GetValue<String>() {
	        public String getValue(ConstraintInfo c) {
	          return c.getTypeDescription();
	        }
	      }, null);

		Column<ConstraintInfo, ImageResource> deferrable = addColumn(new ImageResourceCell(), "Deferrable", new GetValue<ImageResource>() {
	        public ImageResource getValue(ConstraintInfo c) {
	        	if (c.isDeferrable()) {
	        		return PgStudio.Images.trueBox();
	        	}
	          return null;
	        }
	      }, null);

		Column<ConstraintInfo, ImageResource> deferred = addColumn(new ImageResourceCell(), "Deferred", new GetValue<ImageResource>() {
	        public ImageResource getValue(ConstraintInfo c) {
	        	if (c.isDeferred()) {
	        		return PgStudio.Images.trueBox();
	        	}
	          return null;
	        }
	      }, null);

		Column<ConstraintInfo, String> updateAction = addColumn(new TextCell(), "Update Action", new GetValue<String>() {
	        public String getValue(ConstraintInfo c) {
	          return c.getUpdateType();
	        }
	      }, null);

		Column<ConstraintInfo, String> deleteAction = addColumn(new TextCell(), "Delete Action", new GetValue<String>() {
	        public String getValue(ConstraintInfo c) {
	          return c.getDeleteType();
	        }
	      }, null);
		
		
		
		dataGrid.setColumnWidth(typeImg, "30px");
		dataGrid.setColumnWidth(constName, "160px");
		dataGrid.setColumnWidth(type, "100px");
		dataGrid.setColumnWidth(deferrable, "70px");
		dataGrid.setColumnWidth(deferred, "70px");
		dataGrid.setColumnWidth(updateAction, "90px");
		dataGrid.setColumnWidth(deleteAction, "90px");

		typeImg.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		dataGrid.setLoadingIndicator(new Image(PgStudio.Images.spinner()));

		dataProvider.addDataDisplay(dataGrid);

		dataGrid.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler((new 
				SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						ConstraintInfo index = selectionModel.getSelectedObject();
						constDef.setText(index.getDefinition(item.getFullName()));
						dataDetailProvider.setItem(index.getId(), ITEM_TYPE.TABLE);
					}			
		}));

		panel.add(dataGrid);
		
		return panel.asWidget();
	}
	

	private <C> Column<ConstraintInfo, C> addColumn(Cell<C> cell, String headerText,
		      final GetValue<C> getter, FieldUpdater<ConstraintInfo, C> fieldUpdater) {
		    Column<ConstraintInfo, C> column = new Column<ConstraintInfo, C>(cell) {
		      @Override
		      public C getValue(ConstraintInfo object) {
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
		
		VerticalPanel right = new VerticalPanel();
		right.setWidth("95%");

		Label rightLbl = new Label();
		rightLbl.setText("Constraint Definition");
		rightLbl.setStyleName("studio-Label-Small");

		constDef = new TextArea();
		constDef.setWidth("100%");
		constDef.setVisibleLines(5);
		constDef.setReadOnly(true);
		
		right.add(rightLbl);
		right.add(constDef);
		
		panel.add(right);
				
		return panel.asWidget();
	}

	@Override
	public void refresh() {
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
}
