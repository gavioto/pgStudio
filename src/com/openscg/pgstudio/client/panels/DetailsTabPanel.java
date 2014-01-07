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

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.models.ModelInfo;
import com.openscg.pgstudio.client.utils.TextFormat;

public class DetailsTabPanel {

	private ModelInfo selectedItem;
	private int selectedTab;
	private ITEM_TYPE type;

	private final DecoratedTabPanel panel = new DecoratedTabPanel();

	private ColumnPanel columnPanel = new ColumnPanel();
	private IndexPanel indexPanel = new IndexPanel();
	private ConstraintPanel constPanel = new ConstraintPanel();
	private TriggerPanel triggerPanel = new TriggerPanel();
	private RulePanel rulePanel = new RulePanel();
	private ItemDataPanel dataPanel = new ItemDataPanel();
	private StatsPanel statsPanel = new StatsPanel();
	private ScriptPanel scriptPanel = new ScriptPanel();
	private SecurityPanel secPanel = new SecurityPanel();
	
	private Widget columnTabWidget = new HTML(TextFormat.getHeaderString("Columns", PgStudio.Images.column()));
	private Widget indexTabWidget = new HTML(TextFormat.getHeaderString("Indexes", PgStudio.Images.index()));
	private Widget constTabWidget = new HTML(TextFormat.getHeaderString("Constraints", PgStudio.Images.constraint()));
	private Widget triggerTabWidget = new HTML(TextFormat.getHeaderString("Triggers", PgStudio.Images.triggers()));
	private Widget ruleTabWidget = new HTML(TextFormat.getHeaderString("Rules", PgStudio.Images.rules()));
	private Widget dataTabWidget = new HTML(TextFormat.getHeaderString("Data", PgStudio.Images.data()));
	private Widget statsTabWidget = new HTML(TextFormat.getHeaderString("Stats", PgStudio.Images.stats()));
	private Widget scriptTabWidget = new HTML(TextFormat.getHeaderString("Script", PgStudio.Images.script()));
	private Widget securityTabWidget = new HTML(TextFormat.getHeaderString("Security", PgStudio.Images.security()));

	private Widget indexWidget;
	private Widget constWidget;
	private Widget triggerWidget;
	private Widget ruleWidget;
	private Widget securityWidget;

	
	public void setSelectedItem(ModelInfo selected) {		
		this.selectedItem = selected;
		
		if (selectedItem.getItemType() != type) {
			this.type = selectedItem.getItemType();
			
			switch(type) {
				case TABLE :
					setupTablePanels();
					break;
				case MATERIALIZED_VIEW:
				case VIEW :
					setupViewPanels();
					break;
				case FOREIGN_TABLE :
					setupForeignTablePanels();
					break;
				case FUNCTION :
					setupFunctionPanels();
					break;
				case SEQUENCE :
					setupSequencePanels();
					break;
				case TYPE :
					setupTypePanels();
					break;
			default:
				break;

			}
		}

		if (panel.getTabBar().getSelectedTab() < 0) {
			panel.selectTab(0);
		}
		
		DetailsPanel p = (DetailsPanel) panel.getWidget(panel.getTabBar().getSelectedTab());
		p.setItem(selectedItem);	
		
		DOM.setStyleAttribute(RootPanel.get().getElement(), "cursor", "default");

	}
	
	public Widget asWidget() {

		indexWidget = indexPanel.asWidget();
		constWidget = constPanel.asWidget();
		triggerWidget = triggerPanel.asWidget();
		ruleWidget = rulePanel.asWidget();
		securityWidget = secPanel.asWidget();

		panel.setHeight("100%");
		panel.setWidth("100%");
		panel.setStyleName("studio-DecoratedTabBar");
		
		panel.add(columnPanel, columnTabWidget);
		panel.add(indexWidget, indexTabWidget);
		panel.add(constWidget, constTabWidget);
		panel.add(triggerWidget, triggerTabWidget);
		panel.add(ruleWidget, ruleTabWidget);
		panel.add(dataPanel, dataTabWidget);
		panel.add(statsPanel, statsTabWidget);
		panel.add(scriptPanel, scriptTabWidget);
		panel.add(securityWidget, securityTabWidget);

		panel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
					selectedTab = (Integer) event.getSelectedItem();
				/* I am sometimes HTML cast to DetailsPanel errors, so ignore this */
				try {
					DetailsPanel p = (DetailsPanel) panel.getWidget(selectedTab);
					p.setItem(selectedItem);
				} catch (Exception e) {}
			}
			
		});
		
	    return panel.asWidget();
	}
	
	private void setupTablePanels() {
		
		panel.insert(securityWidget, securityTabWidget, 0);
		panel.insert(scriptPanel, scriptTabWidget, 0);
		panel.insert(statsPanel, statsTabWidget, 0);
		panel.insert(dataPanel, dataTabWidget, 0);
		panel.insert(ruleWidget, ruleTabWidget, 0);
		panel.insert(triggerWidget, triggerTabWidget, 0);
		panel.insert(constWidget, constTabWidget, 0);
		panel.insert(indexWidget, indexTabWidget, 0);
		panel.insert(columnPanel, columnTabWidget, 0);

		removeExtraPanels(9);
}

	private void setupViewPanels() {		

		panel.insert(securityWidget, securityTabWidget, 0);
		panel.insert(scriptPanel, scriptTabWidget, 0);
		panel.insert(dataPanel, dataTabWidget, 0);
		panel.insert(ruleWidget, ruleTabWidget, 0);
		panel.insert(columnPanel, columnTabWidget, 0);

		removeExtraPanels(5);
}

	private void setupForeignTablePanels() {		

		panel.insert(securityWidget, securityTabWidget, 0);
		panel.insert(scriptPanel, scriptTabWidget, 0);
		panel.insert(dataPanel, dataTabWidget, 0);
		panel.insert(columnPanel, columnTabWidget, 0);

		removeExtraPanels(4);
}

	private void setupFunctionPanels() {		

		panel.insert(securityWidget, securityTabWidget, 0);
		panel.insert(scriptPanel, scriptTabWidget, 0);

		removeExtraPanels(2);
}

	private void setupSequencePanels() {		

		panel.insert(securityWidget, securityTabWidget, 0);
		panel.insert(scriptPanel, scriptTabWidget, 0);
		panel.insert(statsPanel, statsTabWidget, 0);

		removeExtraPanels(3);
	}

	private void setupTypePanels() {		

		panel.insert(securityWidget, securityTabWidget, 0);
		panel.insert(scriptPanel, scriptTabWidget, 0);
		
		removeExtraPanels(2);
	}

	private void removeExtraPanels(int numPanelsToKeep) {
		for (int i = panel.getWidgetCount(); i > numPanelsToKeep; i--) {
			panel.remove(i-1);
		}
	}

	public void refreshCurrent()	{
		DetailsPanel p = (DetailsPanel) panel.getWidget(selectedTab);
		p.setItem(selectedItem);
	}
}
