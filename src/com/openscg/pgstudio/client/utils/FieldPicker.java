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
package com.openscg.pgstudio.client.utils;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.models.ColumnInfo;

public class FieldPicker extends Composite implements ClickHandler {

	private HorizontalPanel panel;
	private ListBox available;
	private ListBox selected;
	private VerticalPanel buttons; 
	
	private int heightVal = 100;
	private int widthVal = 250;
	private int visibleItems = 10;
	
	public FieldPicker() {
		panel = new HorizontalPanel();
		panel.setHeight(heightVal + "px");
		panel.setWidth(widthVal + "px");
		
		VerticalPanel availablePanel = new VerticalPanel();
		
		Label availableLbl = new Label("Available");
		
		available = new ListBox(true);
		available.setVisibleItemCount(visibleItems);
		available.setHeight((heightVal - 20) + "px");
		available.setWidth(((widthVal - 50)/2) + "px");
		
		availablePanel.add(availableLbl);		
		availablePanel.add(available);
		
		buttons = new VerticalPanel();
		buttons.setHeight((heightVal - 20) + "px");
		panel.setWidth("30px");
		
		PushButton right = getMoveRightButton();
		PushButton left = getMoveLeftButton(); 
		
		right.setSize("24px", "22px");
		left.setSize("24px", "22px");
		
		buttons.add(right);
		buttons.add(left);
		buttons.setCellVerticalAlignment(right, HasVerticalAlignment.ALIGN_BOTTOM);
		buttons.setCellVerticalAlignment(left, HasVerticalAlignment.ALIGN_TOP);
		
		VerticalPanel selectedPanel = new VerticalPanel();

		Label selectedLbl = new Label("Selected");

		selected = new ListBox(true);
		selected.setVisibleItemCount(visibleItems);
		selected.setHeight((heightVal - 20) + "px");
		selected.setWidth(((widthVal - 50)/2) + "px");
		
		selectedPanel.add(selectedLbl);
		selectedPanel.add(selected);
		
		panel.add(availablePanel);
		panel.add(buttons);
		panel.add(selectedPanel);
		
		initWidget(panel);
	}
	
	public void addItem(String item) {
		available.addItem(item);
	}

	public void addItem(String item, String value) {
		available.addItem(item, value);
	}

	public void clear() {
		available.clear();
		selected.clear();
	}
	
	public ArrayList<String> getSelected() {
		ArrayList<String> ret = new ArrayList<String>();
		
		for (int i = 0; i < selected.getItemCount(); i++) {
			ret.add(selected.getValue(i));
		}
		
		return ret;
	}
	
	public void setHeight(String height) {
		if (height.endsWith("px")) {
			this.heightVal = Integer.parseInt(height.substring(0, height.length() - 2)); 
			
			if (heightVal > 20) {
				panel.setHeight(height);
				available.setHeight((heightVal - 20) + "px");
				buttons.setHeight((heightVal - 20) + "px");
				selected.setHeight((heightVal - 20) + "px");
			}
		}
	}

	public void setWidth(String width) {
		if (width.endsWith("px")) {
			this.widthVal = Integer.parseInt(width.substring(0, width.length() - 2)); 
			
			if (widthVal > 50) {
				panel.setWidth(width);
				available.setWidth(((widthVal - 50) / 2) + "px");
				selected.setWidth(((widthVal - 50) / 2) + "px");
			}
		}
	}

	private PushButton getMoveRightButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.right()));
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				while (available.getSelectedIndex() != -1) {
					selected.addItem(available.getValue(available.getSelectedIndex()));
					available.removeItem(available.getSelectedIndex());
				}
			}			
		});
		return button;
	}

	private PushButton getMoveLeftButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.left()));
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				while (selected.getSelectedIndex() != -1) {
					available.addItem(selected.getValue(selected.getSelectedIndex()));
					selected.removeItem(selected.getSelectedIndex());
				}
			}			
		});
		return button;
	}

	@Override
	public void onClick(ClickEvent event) {
		available.setEnabled(true);
	}

}
