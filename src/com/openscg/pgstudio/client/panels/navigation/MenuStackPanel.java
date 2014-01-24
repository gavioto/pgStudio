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
package com.openscg.pgstudio.client.panels.navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DecoratedStackPanel;
import com.google.gwt.user.client.ui.Widget;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.Resources;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.utils.TextFormat;

public class MenuStackPanel {
	
	public static final Resources Images =  GWT.create(Resources.class);
	
	
	private final PgStudio main;

	private final TablesPanel tables;
	private final ViewsPanel views;
	private final ForeignTablesPanel ftables;
	private final FunctionsPanel funcs;
	private final SequencesPanel seqs;
	private final TypesPanel types;
	private final DecoratedStackPanel panel =  new DecoratedStackPanel();
	
	private int selectedIndex = 0;
	
	public MenuStackPanel(PgStudio main) {
		this.main = main;
		tables = new TablesPanel(main);
		views = new ViewsPanel(main);
		ftables = new ForeignTablesPanel(main);
		funcs = new FunctionsPanel(main);
		seqs = new SequencesPanel(main);
		types = new TypesPanel(main);
	}
	
	public void setSchema(DatabaseObjectInfo schema) {
		if (schema.equals("")) {
			return;
		}
		
		tables.setSchema(schema);
		views.setSchema(schema);
		ftables.setSchema(schema);
		funcs.setSchema(schema);
		seqs.setSchema(schema);
		types.setSchema(schema);
		
		// Start a timer to wait for the table list to be returned and select
		// the first item, but only wait for a max of 1.5 seconds
		Timer poll = new Timer() {
			@Override
			public void run() {
				this.schedule(1000);
				
				if (tables.selectFirst()) {
					setSelected(ITEM_TYPE.TABLE);
					this.cancel();
				} else if (views.selectFirst()) {
					setSelected(ITEM_TYPE.VIEW);
					this.cancel();
				} else if (ftables.selectFirst()) {
					setSelected(ITEM_TYPE.FOREIGN_TABLE);
					this.cancel();
				} else if (funcs.selectFirst()) {
					setSelected(ITEM_TYPE.FUNCTION);
					this.cancel();
				} else if (seqs.selectFirst()) {
					setSelected(ITEM_TYPE.SEQUENCE);
					this.cancel();
				} else if (types.selectFirst()) {
					setSelected(ITEM_TYPE.TYPE);
					this.cancel();
				}
			}
			
		};
		poll.schedule(500);

	}
	
	public Widget asWidget() {
		panel.setWidth("95%");

	    panel.add(tables, TextFormat.getHeaderString("Tables", Images.tables()), true);
	    panel.add(views, TextFormat.getHeaderString("Views", Images.views()), true);
	    
	    if (main.getDatabaseVersion() >= 920) {
	    	panel.add(ftables, TextFormat.getHeaderString("Foreign Tables", Images.foreignTables()), true);
	    }
	    
	    panel.add(funcs, TextFormat.getHeaderString("Functions", Images.procedures()), true);
	    panel.add(seqs, TextFormat.getHeaderString("Sequences", Images.sequences()), true);
	    panel.add(types, TextFormat.getHeaderString("Types", Images.types()), true);
	    
	    panel.addHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (panel.getSelectedIndex() != selectedIndex) {
					selectedIndex = panel.getSelectedIndex();
					MenuPanel p = (MenuPanel) panel.getWidget(selectedIndex);
					p.refresh();
				}				
			}	    	
	    }, ClickEvent.getType());
	    	    
	    return panel.asWidget();
	}

	public void refreshCurrent()	{
		MenuPanel p = (MenuPanel) panel.getWidget(panel.getSelectedIndex());
		p.refresh();
	}
	
	public void setSelected(ITEM_TYPE type) {
		int index = 0;
		switch (type) {
		case TABLE:
			index = panel.getWidgetIndex(tables);
			break;
		case VIEW:
			index = panel.getWidgetIndex(views);
			break;
		case FOREIGN_TABLE:
			index = panel.getWidgetIndex(ftables);
			break;
		case FUNCTION:
			index = panel.getWidgetIndex(funcs);
			break;
		case SEQUENCE:
			index = panel.getWidgetIndex(seqs);
			break;
		case TYPE:
			index = panel.getWidgetIndex(types);
			break;
		default:
			break;
		}
		
		selectedIndex = index;
		panel.showStack(selectedIndex);
	}
}
