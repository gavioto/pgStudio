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
import com.openscg.pgstudio.client.messages.RulesJsObject;
import com.openscg.pgstudio.client.models.RuleInfo;

public class RuleListDataProvider extends AsyncDataProvider<RuleInfo> implements
		ItemListProvider {
	private List<RuleInfo> ruleList = new ArrayList<RuleInfo>();

	private int item = -1;
	private int schema = -1;
	private ITEM_TYPE type;

	private final PgStudioServiceAsync studioService = GWT
			.create(PgStudioService.class);

	public void setItem(int schema, int item, ITEM_TYPE type) {
		this.schema = schema;
		this.item = item;
		this.type = type;

		getData();
	}

	@Override
	public void refresh() {
		getData();
	}

	@Override
	protected void onRangeChanged(HasData<RuleInfo> display) {
		getData();

		int start = display.getVisibleRange().getStart();
		int end = start + display.getVisibleRange().getLength();
		end = end >= ruleList.size() ? ruleList.size() : end;
		List<RuleInfo> sub = ruleList.subList(start, end);
		updateRowData(start, sub);

	}

	private void getData() {
		if (item > 0) {
			studioService.getItemObjectList(PgStudio.getToken(), item, type,
					ITEM_OBJECT_TYPE.RULE, new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							ruleList.clear();
							// Show the RPC error message to the user
							Window.alert(caught.getMessage());
						}

						public void onSuccess(String result) {
							ruleList = new ArrayList<RuleInfo>();

							JsArray<RulesJsObject> rules = json2Messages(result);

							if (rules != null) {
								ruleList.clear();

								for (int i = 0; i < rules.length(); i++) {
									RulesJsObject rule = rules.get(i);
									ruleList.add(msgToColumnInfo(rule));
								}
							}

							updateRowCount(ruleList.size(), true);
							updateRowData(0, ruleList);

						}
					});
		}
	}

	private RuleInfo msgToColumnInfo(RulesJsObject msg) {
		int id = Integer.parseInt(msg.getId());

		RuleInfo rule = new RuleInfo(schema, id, msg.getRuleName());

		rule.setType(msg.getRuleType());
		rule.setEnabled(msg.getEnabled().equalsIgnoreCase("true"));
		rule.setInstead(msg.getInstead().equalsIgnoreCase("true"));
		rule.setDefinition(msg.getDefinition());

		return rule;
	}

	private static final native JsArray<RulesJsObject> json2Messages(String json)
	/*-{
		return eval(json);
	}-*/;
}