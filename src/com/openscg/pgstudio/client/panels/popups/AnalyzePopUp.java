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
package com.openscg.pgstudio.client.panels.popups;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.handlers.UtilityCommandAsyncCallback;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.providers.ModelListProvider;

public class AnalyzePopUp implements StudioModelPopUp {

	private final PgStudioServiceAsync studioService = GWT
			.create(PgStudioService.class);

	private final boolean analyzeTable;

	final DialogBox dialogBox = new DialogBox();

	private SingleSelectionModel<ModelInfo> selectionModel = null;
	private ModelListProvider dataProvider = null;

	private DatabaseObjectInfo schema = null;
	private String item = null;

	private boolean vacuum = false;
	private boolean vacuumFull = false;

	private boolean canVacuum = true;

	public AnalyzePopUp(boolean analyzeTable) {
		this.analyzeTable = analyzeTable;
	}

	@Override
	public DialogBox getDialogBox() throws PopUpException {
		if (selectionModel == null)
			throw new PopUpException("Selection Model is not set");

		if (dataProvider == null)
			throw new PopUpException("Data Provider is not set");

		if (schema == null || item == null)
			throw new PopUpException("Schema and table name are not set");

		dialogBox.setWidget(getPanel());

		dialogBox.setGlassEnabled(true);
		dialogBox.center();

		return dialogBox;
	}

	@Override
	public void setSelectionModel(SingleSelectionModel model) {
		this.selectionModel = model;
	}

	@Override
	public void setDataProvider(ModelListProvider provider) {
		this.dataProvider = provider;

	}

	public void setSchema(DatabaseObjectInfo schema) {
		this.schema = schema;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public void setCanVacuum(boolean value) {
		this.canVacuum = value;
	}

	private VerticalPanel getPanel() {
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("StudioPopup");

		VerticalPanel info = new VerticalPanel();
		info.setSpacing(10);

		Label lbl = new Label();
		lbl.setStyleName("StudioPopup-Msg-Strong");
		lbl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		lbl.setText("Analyze Table " + schema.getName() + "." + item);

		RadioButton rb1 = new RadioButton("AnalyzeGroup", "Analyze");
		rb1.addClickHandler(getAnalyzeButtonClickHandler());

		info.add(lbl);
		info.add(rb1);

		if (canVacuum) {
			RadioButton rb2 = new RadioButton("AnalyzeGroup", "Vacuum");
			rb2.addClickHandler(getVacuumButtonClickHandler());

			RadioButton rb3 = new RadioButton("AnalyzeGroup", "Vacuum Full");
			rb3.addClickHandler(getVacuumFullButtonClickHandler());

			info.add(rb2);
			info.add(rb3);
		}

		rb1.setValue(true);

		panel.add(info);

		Widget buttonBar = getButtonPanel();
		panel.add(buttonBar);
		panel.setCellHorizontalAlignment(buttonBar,
				HasHorizontalAlignment.ALIGN_CENTER);

		return panel;
	}

	private Widget getButtonPanel() {
		HorizontalPanel bar = new HorizontalPanel();
		bar.setHeight("50px");
		bar.setSpacing(10);

		Button okButton = new Button("OK");
		Button cancelButton = new Button("Cancel");

		bar.add(okButton);
		bar.add(cancelButton);

		bar.setCellHorizontalAlignment(okButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		bar.setCellHorizontalAlignment(cancelButton,
				HasHorizontalAlignment.ALIGN_CENTER);

		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionModel.getSelectedObject() != null
						&& !"".equals(selectionModel.getSelectedObject()
								.getName())) {

					UtilityCommandAsyncCallback ac = new UtilityCommandAsyncCallback(
							dialogBox, dataProvider);
					ac.setAutoRefresh(true);
					ac.setShowResultOutput(true);

					studioService.analyze(PgStudio.getToken(), selectionModel
							.getSelectedObject().getId(), selectionModel
							.getSelectedObject().getItemType(), vacuum,
							vacuumFull, ac);
				}
			}
		});

		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				refresh();
				dialogBox.hide(true);
			}
		});

		return bar.asWidget();
	}

	private ClickHandler getAnalyzeButtonClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				vacuum = false;
				vacuumFull = false;
			}
		};
	}

	private ClickHandler getVacuumButtonClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				vacuum = true;
				vacuumFull = false;
			}
		};
	}

	private ClickHandler getVacuumFullButtonClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				vacuum = true;
				vacuumFull = true;
			}
		};
	}

	private void refresh() {
		dataProvider.setSchema(schema);
	}
}
