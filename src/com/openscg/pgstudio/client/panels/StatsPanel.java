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
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.models.StatsInfo;
import com.openscg.pgstudio.client.providers.StatsListDataProvider;

public class StatsPanel extends Composite implements DetailsPanel {

	private static String MAIN_HEIGHT = "300px";

	private DataGrid<StatsInfo> dataGrid;
    private StatsListDataProvider dataProvider = new StatsListDataProvider();


	private static interface GetValue<C> {
	    C getValue(StatsInfo column);
	  }

	public void setItem(ModelInfo item) {
		dataProvider.setItem(item.getId(), item.getItemType());		
	}
	
	public StatsPanel() {
		
		VerticalPanel mainPanel = new VerticalPanel();

		mainPanel.add(getButtonBar());
		mainPanel.add(getMainPanel());
				
		initWidget(mainPanel);
	}

	private Widget getButtonBar() {
		HorizontalPanel bar = new HorizontalPanel();
		
		PushButton refresh = getRefreshButton();
		
		bar.add(refresh);
		
		return bar.asWidget();
	}
	
	private PushButton getRefreshButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.refresh()));
		button.setTitle("Refresh");
		
		return button;
	}
	
	private Widget getMainPanel() {
		SimplePanel panel = new SimplePanel();
		panel.setWidth("100%");
		panel.setHeight(MAIN_HEIGHT);
		
		dataGrid = new DataGrid<StatsInfo>(25, StatsInfo.KEY_PROVIDER);
		dataGrid.setHeight(MAIN_HEIGHT);
		
		Column<StatsInfo, String> name = addNameColumn(new TextCell(), "Name", new GetValue<String>() {
	        public String getValue(StatsInfo stat) {
	          return stat.getName();
	        }
	      }, null);

		Column<StatsInfo, String> value = addNameColumn(new TextCell(), "Value", new GetValue<String>() {
	        public String getValue(StatsInfo stat) {
	          return stat.getValue();
	        }
	      }, null);

		dataGrid.setLoadingIndicator(new Image(PgStudio.Images.spinner()));
		dataProvider.addDataDisplay(dataGrid);

		panel.add(dataGrid);
		
		return panel.asWidget();
	}	
	
	private <C> Column<StatsInfo, C> addNameColumn(Cell<C> cell, String headerText,
		      final GetValue<C> getter, FieldUpdater<StatsInfo, C> fieldUpdater) {
		    Column<StatsInfo, C> column = new Column<StatsInfo, C>(cell) {
		      @Override
		      public C getValue(StatsInfo object) {
		        return getter.getValue(object);
		      }
		    };
		    column.setFieldUpdater(fieldUpdater);

		    dataGrid.addColumn(column, headerText);
		    return column;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

}
