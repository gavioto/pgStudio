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
import com.openscg.pgstudio.client.messages.ForeignServersJsObject;

public class ForeignServerInfo implements Comparable<ForeignServerInfo> {

    /**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<ForeignServerInfo> KEY_PROVIDER = new ProvidesKey<ForeignServerInfo>() {
      public Object getKey(ForeignServerInfo view) {
        return view == null ? null : view.getId();
      }
    };

    private static int nextId = 0;

    private final int id;
    
    private String name;
    
    public ForeignServerInfo(String name) {
      this.id = nextId;
      nextId++;
      setName(name);
    }

    public int compareTo(ForeignServerInfo o) {
      return (o == null || o.name == null) ? -1
          : -o.name.compareTo(name);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof ForeignServerInfo) {
        return id == ((ForeignServerInfo) o).id;
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

	public void setName(String name) {
		this.name = name;
	}
	
	public static ForeignServerInfo msgToInfo(ForeignServersJsObject msg) {
		
		ForeignServerInfo server = new ForeignServerInfo(msg.getName());

		return server;
	}

	public static final native JsArray<ForeignServersJsObject> json2Messages(
			String json)
	/*-{ 
	  	return eval(json); 
	}-*/;

  }
