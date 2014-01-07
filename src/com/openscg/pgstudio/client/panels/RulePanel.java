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
import com.openscg.pgstudio.client.models.RuleInfo;
import com.openscg.pgstudio.client.panels.popups.AddRulePopUp;
import com.openscg.pgstudio.client.panels.popups.DropItemObjectPopUp;
import com.openscg.pgstudio.client.panels.popups.PopUpException;
import com.openscg.pgstudio.client.providers.RuleListDataProvider;

public class RulePanel extends Composite implements DetailsPanel {
	
	private static interface GetValue<C> {
	    C getValue(RuleInfo column);
	}

	private DataGrid<RuleInfo> dataGrid;
    private RuleListDataProvider dataProvider = new RuleListDataProvider();

    private TextArea ruleDef;
    
    private ModelInfo item = null;
    
    private final SingleSelectionModel<RuleInfo> selectionModel = 
        	new SingleSelectionModel<RuleInfo>(RuleInfo.KEY_PROVIDER);

	private String MAIN_HEIGHT = "250px";
	
	public static int ROWS_PER_PAGE = 5;
	public static int MAX_RULES = 1600;
	
	public void setItem(ModelInfo item) {
		this.item = item;
		
		ruleDef.setText("");
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
	
	public RulePanel() {
		
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
		button.setTitle("Drop RUle");
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
					pop.setObjectType(ITEM_OBJECT_TYPE.RULE);
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
		button.setTitle("Create Rule");

		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AddRulePopUp pop = new AddRulePopUp();
				pop.setSelectionModel(selectionModel);
				pop.setDataProvider(dataProvider);
				pop.setItem(item);
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

	private Widget getMainPanel() {
		SimplePanel panel = new SimplePanel();
		panel.setWidth("100%");
		panel.setHeight("100%");
		
		dataGrid = new DataGrid<RuleInfo>(MAX_RULES, RuleInfo.KEY_PROVIDER);
		dataGrid.setHeight(MAIN_HEIGHT);
		
		Column<RuleInfo, String> columnName = addColumn(new TextCell(), "Rule Name", new GetValue<String>() {
	        public String getValue(RuleInfo rule) {
	          return rule.getName();
	        }
	      }, null);

		
		Column<RuleInfo, String> ruleType = addColumn(new TextCell(), "Type", new GetValue<String>() {
	        public String getValue(RuleInfo rule) {
	          return rule.getType().toUpperCase();
	        }
	      }, null);

		Column<RuleInfo, ImageResource> ruleEnabled = addColumn(new ImageResourceCell(), "Enabled", new GetValue<ImageResource>() {
	        public ImageResource getValue(RuleInfo rule) {
	        	if (rule.isEnabled()) {
	        		return PgStudio.Images.trueBox();
	        	}
	          return PgStudio.Images.falseBox();
	        }
	      }, null);

		Column<RuleInfo, ImageResource> instead = addColumn(new ImageResourceCell(), "Instead", new GetValue<ImageResource>() {
	        public ImageResource getValue(RuleInfo rule) {
	        	if (rule.isInstead()) {
	        		return PgStudio.Images.trueBox();
	        	}
        		return PgStudio.Images.falseBox();
	        }
	      }, null);

		dataGrid.setColumnWidth(columnName, "200px");
		dataGrid.setColumnWidth(ruleType, "120px");
		dataGrid.setColumnWidth(ruleEnabled, "100px");
		dataGrid.setColumnWidth(instead, "100px");

		ruleType.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		ruleEnabled.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		instead.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);		

		dataGrid.setLoadingIndicator(new Image(PgStudio.Images.spinner()));

		dataProvider.addDataDisplay(dataGrid);

		dataGrid.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler((new 
				SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						RuleInfo rule = selectionModel.getSelectedObject();
						ruleDef.setText(rule.getDefinition());
					}			
		}));

		panel.add(dataGrid);
		
		return panel.asWidget();
	}
	

	private <C> Column<RuleInfo, C> addColumn(Cell<C> cell, String headerText,
		      final GetValue<C> getter, FieldUpdater<RuleInfo, C> fieldUpdater) {
		    Column<RuleInfo, C> column = new Column<RuleInfo, C>(cell) {
		      @Override
		      public C getValue(RuleInfo object) {
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
				
		VerticalPanel ruleDefPanel = new VerticalPanel();
		ruleDefPanel.setWidth("95%");

		Label lbl = new Label();
		lbl.setText("Rule Definition");
		lbl.setStyleName("studio-Label-Small");

		ruleDef = new TextArea();
		ruleDef.setWidth("100%");
		ruleDef.setVisibleLines(5);
		ruleDef.setReadOnly(true);
		
		ruleDefPanel.add(lbl);
		ruleDefPanel.add(ruleDef);
		
		panel.add(ruleDefPanel);
				
		return panel.asWidget();
	}

	@Override
	public void refresh() {
		dataProvider.setItem(item.getSchema(), item.getId(), item.getItemType());
	}
}
