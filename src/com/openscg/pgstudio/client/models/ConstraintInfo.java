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

public class ConstraintInfo implements ModelInfo, Comparable<ConstraintInfo> {

    public static final ProvidesKey<ConstraintInfo> KEY_PROVIDER = new ProvidesKey<ConstraintInfo>() {
      public Object getKey(ConstraintInfo IDX) {
        return IDX == null ? null : IDX.getId();
      }
    };

    private final int id;
    private final int schema;
    private final String name;
    
    private String owner;
    private String definition;
    private String type;
    private boolean deferrable;
    private boolean deferred;
    private String indexName;
    private String deleteType;
    private String updateType;
    
    
	public ConstraintInfo(int schema, int id, String name) {
    	this.schema = schema;
        this.id = id;
        this.name = name;;
    }

    public String getType() {
		return type;
	}

    public String getTypeDescription() {
    	String ret = type;
    	
    	if (type.equals("c")) 
    		ret = "Check";
    	else if (type.equals("f"))
    		ret = "Foreign Key";
    	else if (type.equals("p"))
    		ret = "Primary Key";
    	else if (type.equals("u"))
    		ret = "Unique";
    	else if (type.equals("t"))
    		ret = "Trigger";
    	else if (type.equals("x"))
    		ret = "Exclusion";

		return ret;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isDeferrable() {
		return deferrable;
	}

	public void setDeferrable(boolean deferrable) {
		this.deferrable = deferrable;
	}

	public boolean isDeferred() {
		return deferred;
	}

	public void setDeferred(boolean deferred) {
		this.deferred = deferred;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getDeleteType() {
		return formatAction(deleteType);
	}

	public void setDeleteType(String deleteType) {
		this.deleteType = deleteType;
	}

	public String getUpdateType() {
		return formatAction(updateType);
	}

	public void setUpdateType(String updateType) {
		this.updateType = updateType;
	}

    public int compareTo(ConstraintInfo o) {
      return (o == null || o.name == null) ? -1
          : -o.name.compareTo(name);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof ConstraintInfo) {
        return id == ((ConstraintInfo) o).id;
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

	public String getDefinition(String table) {
		String ret = "ALTER TABLE " + table + "\n";
		ret = ret + "    ADD CONSTRAINT " + name + "\n";
		ret = ret + "    " + definition;
		return ret;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

    public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	private String formatAction(String code) {
		String ret = "";
		
		if (code.equals("a")) 
			ret = "No Action";
		else if (code.equals("r"))
			ret = "Restrict";
		else if (code.equals("c"))
			ret = "Cascade";
		else if (code.equals("n"))
			ret = "Set Null";
		else if (code.equals("d"))
			ret = "Set Default";
		
		return ret;
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
