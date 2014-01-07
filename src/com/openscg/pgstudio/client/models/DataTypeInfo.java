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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.view.client.ProvidesKey;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.messages.DataTypesJsObject;

public class DataTypeInfo implements ModelInfo, Comparable<DataTypeInfo> {

    public static final ProvidesKey<DataTypeInfo> KEY_PROVIDER = new ProvidesKey<DataTypeInfo>() {
      public Object getKey(DataTypeInfo type) {
        return type == null ? null : type.getId();
      }
    };


    private final int id;
    private final String name;
    
    private boolean hasLength;
    private int usageCount;
    
    public DataTypeInfo(int schema, int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int compareTo(DataTypeInfo o) {
      return (o == null || o.name == null) ? -1
          : -o.name.compareTo(name);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof DataTypeInfo) {
        return id == ((DataTypeInfo) o).id;
      }
      return false;
    }

    public int getId() {
      return this.id;
    }

    @Override
    public int hashCode() {
      return id;
    }

	public String getName() {
		return name;
	}

	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public ITEM_TYPE getItemType() {
		return ITEM_TYPE.TYPE;
	}

	public boolean isHasLength() {
		return hasLength;
	}

	public void setHasLength(boolean hasLength) {
		this.hasLength = hasLength;
	}

	public int getUsageCount() {
		return usageCount;
	}

	public void setUsageCount(int usageCount) {
		this.usageCount = usageCount;
	}

	public static final DataTypeInfo msgToInfo(DataTypesJsObject msg) {
		
		DataTypeInfo type = new DataTypeInfo(0, Integer.parseInt(msg.getId()), msg.getTypeName());

		type.setUsageCount(Integer.parseInt(msg.getUsageCount()));
		
		if (msg.getHasLength().equalsIgnoreCase("true")) {
			type.setHasLength(true);
		} else {
			type.setHasLength(false);
		}
		return type;
	}

	public static final native JsArray<DataTypesJsObject> json2Messages(
			String json)
	/*-{ 
	  	return eval(json); 
	}-*/;

	@Override
	public String getComment() {
		return "";
	}

	@Override
	public int getSchema() {
		return 0;
	}

  }
