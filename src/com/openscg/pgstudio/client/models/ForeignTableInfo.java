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

public class ForeignTableInfo implements ModelInfo, Comparable<ForeignTableInfo> {

    public static final ProvidesKey<ForeignTableInfo> KEY_PROVIDER = new ProvidesKey<ForeignTableInfo>() {
      public Object getKey(ForeignTableInfo view) {
        return view == null ? null : view.getId();
      }
    };

    private final int id;
    private final int schema;
    private final String name;
    
    private String type;
    private String comment;
    
    public ForeignTableInfo(int schema, int id, String name) {
    	this.schema = schema;
        this.id = id;
        this.name = name;
    }

    public int compareTo(ForeignTableInfo o) {
      return (o == null || o.name == null) ? -1
          : -o.name.compareTo(name);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof ForeignTableInfo) {
        return id == ((ForeignTableInfo) o).id;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public ITEM_TYPE getItemType() {
		return ITEM_TYPE.FOREIGN_TABLE;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isPostgreSQL() {
		return type.equals("postgres_fdw");
	}
	
	public boolean isHadoop() {
		return type.equals("hive_fdw") || type.equals("hadoop_fdw");
	}

	@Override
	public int getSchema() {
		return schema;
	}
	
  }
