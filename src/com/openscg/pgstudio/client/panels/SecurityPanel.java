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
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.models.PrivilegeInfo;
import com.openscg.pgstudio.client.panels.popups.GrantPopUp;
import com.openscg.pgstudio.client.panels.popups.PopUpException;
import com.openscg.pgstudio.client.panels.popups.RevokePopUp;
import com.openscg.pgstudio.client.providers.PrivilegeListDataProvider;

public class SecurityPanel extends Composite implements DetailsPanel {

	
	private static interface GetValue<C> {
	    C getValue(PrivilegeInfo column);
	}

    private ModelInfo item = null;
    
    private DataGrid<PrivilegeInfo> dataGrid;
    private PrivilegeListDataProvider dataProvider = new PrivilegeListDataProvider();

    
    private final SingleSelectionModel<PrivilegeInfo> selectionModel = 
        	new SingleSelectionModel<PrivilegeInfo>(PrivilegeInfo.KEY_PROVIDER);

	private String MAIN_HEIGHT = "400px";
	
	public static int ROWS_PER_PAGE = 5;
	public static int MAX_PRIVS = 1600;
	
	public void setItem(ModelInfo item) {
		this.item = item;
		
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
	
	public SecurityPanel() {
		
		VerticalPanel panel = new VerticalPanel();

		panel.add(getButtonBar());
		panel.add(getMainPanel());
				
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
		button.setTitle("Revoke Privilege");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionModel.getSelectedObject() != null
						&& !"".equals(selectionModel.getSelectedObject()
								.getType())) {
					
					RevokePopUp pop = new RevokePopUp();
					pop.setSelectionModel(selectionModel);
					pop.setDataProvider(dataProvider);
					pop.setItem(item);
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
		button.setTitle("Add Privileges");

		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				GrantPopUp pop = new GrantPopUp();
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

	private Widget getMainPanel() {
		SimplePanel panel = new SimplePanel();
		panel.setWidth("100%");
		panel.setHeight("100%");
		
		dataGrid = new DataGrid<PrivilegeInfo>(MAX_PRIVS, PrivilegeInfo.KEY_PROVIDER);
		dataGrid.setWidth("100%");
		dataGrid.setHeight(MAIN_HEIGHT);

		Column<PrivilegeInfo, ImageResource> icon = addColumn(new ImageResourceCell(), "", new GetValue<ImageResource>() {
	        public ImageResource getValue(PrivilegeInfo priv) {
        		return PgStudio.Images.privilege();
	        }
	      }, null);

		Column<PrivilegeInfo, String> grantee = addColumn(new TextCell(), "Grantee", new GetValue<String>() {
	        public String getValue(PrivilegeInfo priv) {
	          return priv.getGrantee();
	        }
	      }, null);

		Column<PrivilegeInfo, String> privilege = addColumn(new TextCell(), "Privilege", new GetValue<String>() {
	        public String getValue(PrivilegeInfo priv) {
	          return priv.getType();
	        }
	      }, null);

		Column<PrivilegeInfo, ImageResource> grantable = addColumn(new ImageResourceCell(), "Grantable", new GetValue<ImageResource>() {
	        public ImageResource getValue(PrivilegeInfo priv) {
	        	if (priv.isGrantable()) {
	        		return PgStudio.Images.trueBox();
	        	}
	          return PgStudio.Images.falseBox();
	        }
	      }, null);

		Column<PrivilegeInfo, String> grantor = addColumn(new TextCell(), "Grantor", new GetValue<String>() {
	        public String getValue(PrivilegeInfo priv) {
	          return priv.getGrantor();
	        }
	      }, null);


		dataGrid.setColumnWidth(icon, "70px");
		dataGrid.setColumnWidth(grantee, "150px");
		dataGrid.setColumnWidth(privilege, "120px");
		dataGrid.setColumnWidth(grantable, "100px");
		dataGrid.setColumnWidth(grantor, "150px");

		grantee.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		privilege.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		grantable.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);		
		grantor.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);		

		dataGrid.setLoadingIndicator(new Image(PgStudio.Images.spinner()));

		dataProvider.addDataDisplay(dataGrid);

		dataGrid.setSelectionModel(selectionModel);
		
		/* TODO: Fix up
		selectionModel.addSelectionChangeHandler((new 
				SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						IndexInfo index = selectionModel.getSelectedObject();
						indexDef.setText(index.getDefinition());
						dataDetailProvider.setItem(schema, index.getName());
					}			
		}));
		 */
		
		panel.add(dataGrid);
		
		return panel.asWidget();
	}
	

	private <C> Column<PrivilegeInfo, C> addColumn(Cell<C> cell, String headerText,
		      final GetValue<C> getter, FieldUpdater<PrivilegeInfo, C> fieldUpdater) {
		    Column<PrivilegeInfo, C> column = new Column<PrivilegeInfo, C>(cell) {
		      @Override
		      public C getValue(PrivilegeInfo object) {
		        return getter.getValue(object);
		      }
		    };
		    column.setFieldUpdater(fieldUpdater);

		    dataGrid.addColumn(column, headerText);
		    return column;
	}

	@Override
	public void refresh() {
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
}
