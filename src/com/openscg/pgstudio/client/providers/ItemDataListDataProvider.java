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
package com.openscg.pgstudio.client.providers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.messages.ItemDataJsObject;
import com.openscg.pgstudio.client.messages.QueryMetaDataJsObject;
import com.openscg.pgstudio.client.messages.StatsJsObject;
import com.openscg.pgstudio.client.models.ItemDataInfo;
import com.openscg.pgstudio.client.models.ModelInfo;

	public class ItemDataListDataProvider extends AsyncDataProvider<ItemDataInfo>
	{
			private static int PIX_PER_CHAR = 10;
			
			private List<ItemDataInfo> itemDataList = new ArrayList<ItemDataInfo>();

			private ModelInfo item = null;
			private int count = 0;

			private boolean isNewItem = true;
			
			private DataGrid<ItemDataInfo> dataDisplay;
			private List<Integer> columnWidths = new ArrayList<Integer>();
			
			public void setItem(ModelInfo item, int count) {
				Date start = new Date();
				if (this.item.equals(item)) {
					isNewItem = false;
				} else {
					isNewItem = true;
				}
				
				if (isNewItem) {
					this.item = item;
					this.count = count;
					
					itemDataList.clear();
					columnWidths.clear();
					for (Object display : this.getDataDisplays()) {
						if (display instanceof DataGrid) {
							dataDisplay = (DataGrid<ItemDataInfo>) display;												
							for (int i = dataDisplay.getColumnCount(); i > 0; i--) {
								dataDisplay.removeColumn(i-1);
							}
						}
					}
				}
					
    			updateRowCount(itemDataList.size(), true);
    			updateRowData(0, itemDataList);

				getData();
				Window.alert("Event Finished: " + ((new Date()).getTime() - start.getTime()));

			}
			
			@Override
			protected void onRangeChanged(HasData<ItemDataInfo> display)
			{
				//getData();
				
				int start = display.getVisibleRange().getStart();
		        int end = start + display.getVisibleRange().getLength();
		        end = end >= itemDataList.size() ? itemDataList.size() : end;
		        List<ItemDataInfo> sub = itemDataList.subList(start, end);
		        updateRowData(start, sub);
		        
			}

			private void getData() {
				PgStudio.studioService.getItemData(PgStudio.getToken(), item.getId(), item.getItemType(), count, new AsyncCallback<String>() {
		            public void onFailure(Throwable caught) {
		            	itemDataList.clear();
		            	columnWidths.clear();
		            	
		                // Show the RPC error message to the user
		                Window.alert(caught.getMessage());
		            }

		            public void onSuccess(String result) {
		            	itemDataList = new ArrayList<ItemDataInfo>();
		            	
	        			JsArray<ItemDataJsObject> res = json2Messages(result);
	        			
	        			if (res.length() > 0) {
	        				int numCols = columnWidths.size();
	        				if (isNewItem) {
		        				JsArray<QueryMetaDataJsObject> metadata = res.get(0).getMetaData();
				            	
		        				numCols = metadata.length();
				            	for (int i = 0; i < numCols; i++) {
				            		QueryMetaDataJsObject col = metadata.get(i);
				            		String name = col.getName();
				            		
				            		columnWidths.add(i, name.length());
				            		final int colNum = i;
				            	    Column<ItemDataInfo, String> column = new Column<ItemDataInfo, String>(
				            		        new TextCell()) {
				            		      @Override
				            		      public String getValue(ItemDataInfo object) {
				            		        return object.getColumnValue(colNum);
				            		      }
				            		    };
				            					            		    
				            		dataDisplay.addColumn(column, name);
				            	}
	        				}

			            	JSONValue results = JSONParser.parseStrict(result);
			            	JSONArray rows = results.isArray().get(0).isObject().get("resultset").isArray();
			            	
			            	for (int i = 0; i < rows.size(); i++) {
			            		JSONObject row = rows.get(i).isObject();
			            		
			            		ItemDataInfo rowInfo = new ItemDataInfo(numCols);
			            		for (int j = 1; j <= numCols; j++) {
			            			String key = "c" + j;
			            			String val = row.get(key).isString().stringValue();
			            			rowInfo.setValue(j-1, val);
			            			
			            			if (columnWidths.get(j-1) < val.length()) {
			            				columnWidths.set(j-1, val.length());
			            			}
			            			
			            		}
			            		itemDataList.add(rowInfo);
			            	}
	        			}
		            	
	        			// Update the column widths
						for (int i = 0; i < dataDisplay.getColumnCount(); i++) {
							dataDisplay.setColumnWidth(dataDisplay.getColumn(i), 
									    (columnWidths.get(i) * PIX_PER_CHAR), Unit.PX);
						}
	        			
	        			
	        			updateRowCount(itemDataList.size(), true);
	        			updateRowData(0, itemDataList);

		            }
		          });		
			}

			private ItemDataInfo msgToColumnInfo(StatsJsObject msg) {
				
//				ItemDataInfo stat = new ItemDataInfo(msg.getName(), msg.getValue());

//				return stat;
				return null;
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

	}