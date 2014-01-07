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
import com.openscg.pgstudio.client.PgStudio.ITEM_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.messages.TriggersJsObject;
import com.openscg.pgstudio.client.models.TriggerInfo;

public class TriggerListDataProvider extends AsyncDataProvider<TriggerInfo>
		implements ItemListProvider {
	private List<TriggerInfo> triggerList = new ArrayList<TriggerInfo>();

	private int schema = -1;
	private int item = -1;
	private ITEM_TYPE type;

	private final PgStudioServiceAsync studioService = GWT
			.create(PgStudioService.class);

	@Override
	public void setItem(int schema, int item, ITEM_TYPE type) {
		this.item = item;
		this.schema = schema;
		this.type = type;

		getData();
	}

	@Override
	public void refresh() {
		getData();
	}

	@Override
	protected void onRangeChanged(HasData<TriggerInfo> display) {
		getData();

		int start = display.getVisibleRange().getStart();
		int end = start + display.getVisibleRange().getLength();
		end = end >= triggerList.size() ? triggerList.size() : end;
		List<TriggerInfo> sub = triggerList.subList(start, end);
		updateRowData(start, sub);

	}

	private void getData() {
		if (item > 0) {
			studioService.getItemObjectList(PgStudio.getToken(), item, type,
					ITEM_OBJECT_TYPE.TRIGGER, new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							triggerList.clear();
							// Show the RPC error message to the user
							Window.alert(caught.getMessage());
						}

						public void onSuccess(String result) {
							triggerList = new ArrayList<TriggerInfo>();

							JsArray<TriggersJsObject> triggers = json2Messages(result);

							if (triggers != null) {
								triggerList.clear();

								for (int i = 0; i < triggers.length(); i++) {
									TriggersJsObject trigger = triggers.get(i);
									triggerList.add(msgToColumnInfo(trigger));
								}
							}

							updateRowCount(triggerList.size(), true);
							updateRowData(0, triggerList);

						}
					});
		}
	}

	private TriggerInfo msgToColumnInfo(TriggersJsObject msg) {
		int id = Integer.parseInt(msg.getId());

		TriggerInfo trigger = new TriggerInfo(schema, id, msg.getTriggerName());

		trigger.setDeferrable(msg.getDeferrable().equalsIgnoreCase("true"));
		trigger.setInitDeferrable(msg.getInitDeferrable().equalsIgnoreCase(
				"true"));
		trigger.setDefinition(msg.getDefinition());

		return trigger;
	}

	private static final native JsArray<TriggersJsObject> json2Messages(
			String json)
	/*-{
		return eval(json);
	}-*/;
}