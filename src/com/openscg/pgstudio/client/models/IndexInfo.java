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

public class IndexInfo implements ModelInfo, Comparable<IndexInfo> {

    public static final ProvidesKey<IndexInfo> KEY_PROVIDER = new ProvidesKey<IndexInfo>() {
      public Object getKey(IndexInfo IDX) {
        return IDX == null ? null : IDX.getId();
      }
    };

    private final int id;
    private final int schema;
    private final String name;
    
    private String owner;
    private String accessMethod;
    private boolean unique;
    private boolean exclusion;
    private boolean partial;
    private boolean primaryKey;
    private String definition;
    
    public IndexInfo(int schema, int id, String name) {
    	this.schema = schema;
        this.id = id;
        this.name = name;
    }
    
    public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getAccessMethod() {
		return accessMethod;
	}

	public void setAccessMethod(String accessMethod) {
		this.accessMethod = accessMethod;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isExclusion() {
		return exclusion;
	}

	public void setExclusion(boolean exclusion) {
		this.exclusion = exclusion;
	}

	public boolean isPartial() {
		return partial;
	}

	public void setPartial(boolean partial) {
		this.partial = partial;
	}

    public int compareTo(IndexInfo o) {
      return (o == null || o.name == null) ? -1
          : -o.name.compareTo(name);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof IndexInfo) {
        return id == ((IndexInfo) o).id;
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

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getDefinition() {
		String def = definition.replace(" ON ", "\n    ON ");
		def = def.replace(" USING ", "\n    USING ");
		def = def.replace(" WHERE ", "\n    WHERE ");
		return def;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
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
