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

import com.google.gwt.view.client.ProvidesKey;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;

public class TriggerInfo implements ModelInfo, Comparable<TriggerInfo> {

    public static final ProvidesKey<TriggerInfo> KEY_PROVIDER = new ProvidesKey<TriggerInfo>() {
      public Object getKey(TriggerInfo trigger) {
        return trigger == null ? null : trigger.getId();
      }
    };

    private final int id;
    private final int schema;
    private final String name;
    
    private boolean deferrable;
    private boolean init_deferrable;
    private String definition;
    
    
    public TriggerInfo(int schema, int id, String name) {
    	this.schema = schema;
        this.id = id;
        this.name = name;
    }

    public int compareTo(TriggerInfo o) {
      return (o == null || o.name == null) ? -1
          : -o.name.compareTo(name);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof TriggerInfo) {
        return id == ((TriggerInfo) o).id;
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

	public String getName() {
		return name;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public boolean isDeferrable() {
		return deferrable;
	}

	public void setDeferrable(boolean deferrable) {
		this.deferrable = deferrable;
	}

	public boolean isInitDeferrable() {
		return init_deferrable;
	}

	public void setInitDeferrable(boolean init_deferrable) {
		this.init_deferrable = init_deferrable;
	}

	@Override
	public String getFullName() {
		return name;
	}

	@Override
	public ITEM_TYPE getItemType() {
		return null;
	}

	@Override
	public String getComment() {
		return "";
	}

	@Override
	public int getSchema() {
		return schema;
	}

  }
