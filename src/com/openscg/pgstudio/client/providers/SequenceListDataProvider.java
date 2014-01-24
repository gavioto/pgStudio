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
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.messages.SequencesJsObject;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.SequenceInfo;

	public class SequenceListDataProvider extends AsyncDataProvider<SequenceInfo> implements ModelListProvider
	{
			private List<SequenceInfo> seqList = new ArrayList<SequenceInfo>();

			private DatabaseObjectInfo schema = null;
			
			private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

			public void setSchema(DatabaseObjectInfo schema) {
				this.schema = schema;
				getData();
			}
			
			public List<SequenceInfo> getList()	{
				return seqList;
			}

			public void refresh() {
				getData();				
			}

			@Override
			protected void onRangeChanged(HasData<SequenceInfo> display)
			{
				getData();
				
				int start = display.getVisibleRange().getStart();
		        int end = start + display.getVisibleRange().getLength();
		        end = end >= seqList.size() ? seqList.size() : end;
		        List<SequenceInfo> sub = seqList.subList(start, end);
		        updateRowData(start, sub);
		        
			}

	private void getData() {
		if (schema != null) {
			studioService.getList(PgStudio.getToken(), schema.getId(),
					ITEM_TYPE.SEQUENCE, new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							seqList.clear();
							// Show the RPC error message to the user
							Window.alert(caught.getMessage());
						}

						public void onSuccess(String result) {
							seqList = new ArrayList<SequenceInfo>();

							JsArray<SequencesJsObject> seqs = json2Messages(result);

							if (seqs != null) {
								seqList.clear();

								for (int i = 0; i < seqs.length(); i++) {
									SequencesJsObject seq = seqs.get(i);
									seqList.add(msgToColumnInfo(seq));
								}
							}

							updateRowCount(seqList.size(), true);
							updateRowData(0, seqList);

						}
					});
		}
	}

			private SequenceInfo msgToColumnInfo(SequencesJsObject msg) {
				int id = Integer.parseInt(msg.getId());

				SequenceInfo seq = new SequenceInfo(schema.getId(), id, msg.getName());

				return seq;
			}

			private static final native JsArray<SequencesJsObject> json2Messages(
					String json)
			/*-{ 
			  	return eval(json); 
			}-*/;

	}