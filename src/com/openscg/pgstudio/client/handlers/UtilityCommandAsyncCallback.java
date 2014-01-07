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
package com.openscg.pgstudio.client.handlers;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.openscg.pgstudio.client.messages.CommandJsObject;
import com.openscg.pgstudio.client.panels.popups.PopUpException;
import com.openscg.pgstudio.client.panels.popups.ResultsPopUp;
import com.openscg.pgstudio.client.providers.ListProvider;

public class UtilityCommandAsyncCallback implements AsyncCallback<String> {

	private final DialogBox dialogBox;
	private final ListProvider dataProvider;
	private boolean autoRefresh = false;
	private boolean showResultOutput = false;
	
	public UtilityCommandAsyncCallback (DialogBox dialogBox, ListProvider dataProvider) {
		this.dialogBox = dialogBox;
		this.dataProvider = dataProvider;
	}
	
	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}

	public boolean isShowResultOutput() {
		return showResultOutput;
	}

	public void setShowResultOutput(boolean showResultOutput) {
		this.showResultOutput = showResultOutput;
	}

	@Override
	public void onFailure(Throwable caught) {
		ResultsPopUp pop = new ResultsPopUp(
				"Error", caught.getMessage());
		try {
			pop.getDialogBox();
		} catch (PopUpException ex) {
			Window.alert(ex.getMessage());
		}
		
		if (dialogBox != null)
			dialogBox.hide(true);
	}

	@Override
	public void onSuccess(String result) {
		JsArray<CommandJsObject> jArray = json2Messages(result);

		CommandJsObject res = jArray.get(0);
		if (res.getStatus() != null
				&& res.getStatus()
						.contains("ERROR")) {
			ResultsPopUp pop = new ResultsPopUp(
					"Error", res.getMessage());
			try {
				pop.getDialogBox();
			} catch (PopUpException caught) {
				Window.alert(caught.getMessage());
			}
		} else {
			if (showResultOutput) {
				ResultsPopUp pop = new ResultsPopUp("Results", res.getMessage());
				try {
					pop.getDialogBox();
				} catch (PopUpException caught) {
					Window.alert(caught.getMessage());
				}
			}
			
			if (autoRefresh)
				dataProvider.refresh();
			
			if (dialogBox != null)
				dialogBox.hide(true);
		}
	}

	private static final native JsArray<CommandJsObject> json2Messages(String json)
	/*-{ 
	  	return eval(json); 
	}-*/;

}
