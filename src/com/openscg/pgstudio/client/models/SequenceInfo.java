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

public class SequenceInfo implements ModelInfo, Comparable<SequenceInfo> {

    public static final ProvidesKey<SequenceInfo> KEY_PROVIDER = new ProvidesKey<SequenceInfo>() {
      public Object getKey(SequenceInfo seq) {
        return seq == null ? null : seq.getId();
      }
    };

    private final int id;
    private final int schema;
    private final String name;
        
    public SequenceInfo(int schema, int id, String name) {
    	this.schema = schema;
        this.id = id;
        this.name = name;
    }

    public int compareTo(SequenceInfo o) {
      return (o == null || o.name == null) ? -1
          : -o.name.compareTo(name);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof SequenceInfo) {
        return id == ((SequenceInfo) o).id;
      }
      return false;
    }

    @Override
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
		return ITEM_TYPE.SEQUENCE;
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
