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
package com.openscg.pgstudio.client.models;

import java.util.ArrayList;

import com.google.gwt.view.client.ProvidesKey;

public class ItemDataInfo implements Comparable<ItemDataInfo> {

    /**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<ItemDataInfo> KEY_PROVIDER = new ProvidesKey<ItemDataInfo>() {
      public Integer getKey(ItemDataInfo item) {
        return item == null ? null : item.getId();
      }
    };

    private static int nextId = 0;

    private final int id;
        
    private ArrayList<String> items;
    
    public ItemDataInfo(int columnCount) {
      this.id = nextId;
      nextId++;
      
      items = new ArrayList<String>(columnCount);      
    }

    public int compareTo(ItemDataInfo o) {
      return (o == null || o.getColumnValue(0) == null) ? -1
          : 0;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof ItemDataInfo) {
        return id == ((ItemDataInfo) o).id;
      }
      return false;
    }

    /**
     * @return the unique ID of the contact
     */
    public int getId() {
      return this.id;
    }

    @Override
    public int hashCode() {
      return id;
    }

    public void setValue(int columnIndex, String value) {
    	items.add(columnIndex, value);
    }
    
    public String getColumnValue(int columnIndex) {
    	return items.get(columnIndex);
    }
  }
