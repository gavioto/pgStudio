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

import java.util.ArrayList;
import java.util.List;

import org.gwt.advanced.client.ui.widget.SimpleGrid;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.messages.ItemDataJsObject;
import com.openscg.pgstudio.client.messages.QueryMetaDataJsObject;
import com.openscg.pgstudio.client.models.ItemDataInfo;
import com.openscg.pgstudio.client.models.ModelInfo;

public class ItemDataPanel extends Composite implements DetailsPanel {

	private SimpleGrid dataGrid;
	
	private DialogBox waitingBox;
	
	private static int PIX_PER_CHAR = 10;
	private static String MAIN_HEIGHT = "300px";
	private static int PAGE_SIZE = 14;
	
	private int activeColCount = 0;

	private static interface GetValue<C> {
	    C getValue(ItemDataInfo column);
	  }

	public void setItem(final ModelInfo item) {

		final int count = 25;
		dataGrid.clear(true);
		dataGrid.removeAllRows();
		
		for (int i = 0; i < activeColCount; i++) {
			dataGrid.removeHeaderWidget(0);
		}

		final List<Integer> columnWidths = new ArrayList<Integer>();

		PgStudio.studioService.getItemMetaData(PgStudio.getToken(), item.getId(), item.getItemType(), new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
                // Show the RPC error message to the user
                Window.alert(caught.getMessage());
            }

            public void onSuccess(String result) {
            	/************************************************************/
                /*      Adjust the table for the Meta Data                  */            	
            	/************************************************************/

    			JsArray<ItemDataJsObject> res = json2Messages(result);

    			int numCols = 0;
    			if (res.length() > 0) {
    				JsArray<QueryMetaDataJsObject> metadata = res.get(0).getMetaData();
		            	
    				numCols = metadata.length();
	            	for (int i = 0; i < numCols; i++) {
	            		QueryMetaDataJsObject col = metadata.get(i);
	            		String name = col.getName();
	            		
	            		columnWidths.add(i, name.length());
	            		
	            		Label hdr = new Label(name);
	            		hdr.setStyleName("studio-Label-Small");
	            		hdr.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	            		
	            		dataGrid.setHeaderWidget(i, hdr.asWidget());		            		
	            	}    				
	            }
				activeColCount = numCols;

				
				waitingBox.setVisible(true);
				/************************************************************/
                /*            Load the actual data                          */            	
            	/************************************************************/
        		PgStudio.studioService.getItemData(PgStudio.getToken(), item.getId(), item.getItemType(), count, new AsyncCallback<String>() {
                    public void onFailure(Throwable caught) {
        				waitingBox.setVisible(false);
                        // Show the RPC error message to the user
		                Window.alert(caught.getMessage());
                    }

                    public void onSuccess(String result) {
                    	int numCols = columnWidths.size();
                    			
            			JsArray<ItemDataJsObject> res = json2Messages(result);
            			
            			if (res.length() > 0) {
        	            	JSONValue results = JSONParser.parseStrict(result);
        	            	JSONArray rows = results.isArray().get(0).isObject().get("resultset").isArray();
                	     
        	            	for (int i = 0; i < rows.size(); i++) {
        	            		JSONObject row = rows.get(i).isObject();
        	            		
        	            		for (int j = 1; j <= numCols; j++) {
        	            			String key = "c" + j;
        	            			String val = row.get(key).isString().stringValue();
        	            			
        	            			dataGrid.setText(i, j-1, val);
        	            			
        	            			if (columnWidths.get(j-1) < val.length()) {
        	            				columnWidths.set(j-1, val.length());
        	            			}
        	            			
        	            		}
        	            	}
                    	
        	    			// Update the column widths
        					for (int i = 0; i < numCols; i++) {
        						dataGrid.setColumnWidth(i, getColumnWidth(columnWidths, i));
        					}
        	            }
            			
        				waitingBox.setVisible(false);

                    }});		
            }});		

		
		}
	
	public ItemDataPanel() {
		
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
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight(MAIN_HEIGHT);
		
		ScrollPanel scroll = new ScrollPanel();
		scroll.setWidth("100%");
		scroll.setHeight(MAIN_HEIGHT);
		
		dataGrid = new SimpleGrid();
		dataGrid.enableVerticalScrolling(true);

		scroll.add(dataGrid);
		panel.add(scroll);

		waitingBox = getWaitingBox();
		waitingBox.setVisible(false);
		panel.add(waitingBox);
		panel.setCellHorizontalAlignment(waitingBox, HasHorizontalAlignment.ALIGN_CENTER);
				
		return panel.asWidget();
	}

	private DialogBox getWaitingBox() {
		DialogBox box = new DialogBox();
		box.setAnimationEnabled(true);
		box.setGlassEnabled(true);

		VerticalPanel v = new VerticalPanel();
		v.add(new Image(PgStudio.Images.spinner()));
		
		Label msg = new Label();
		msg.setText("Loading Data...");
		msg.setStyleName("studio-Label-Small");
		v.add(msg);
		
		box.add(v);
		
		return box;
	}
	
	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	private static final native JsArray<ItemDataJsObject> json2Messages(
			String json)
	/*-{ 
	  	return eval(json); 
	}-*/;

	private static final native JsArray<QueryMetaDataJsObject> json2MetaDataMessages(
			String json)
	/*-{ 
	  	return eval(json); 
	}-*/;

	private int getColumnWidth(List<Integer> columnWidths, int index) {
		int pad = 15;
		
		return (columnWidths.get(index) * PIX_PER_CHAR) + pad;
	}
}
