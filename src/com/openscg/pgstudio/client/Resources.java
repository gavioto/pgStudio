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
package com.openscg.pgstudio.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface Resources extends ClientBundle {

	@Source("images/analyze.gif")
	ImageResource analyze();

	@Source("images/close.png")
	ImageResource close();

	@Source("images/column.gif")
	ImageResource column();

	@Source("images/composite.gif")
	ImageResource composite();

	@Source("images/constraint.gif")
	ImageResource constraint();

	@Source("images/check-constraint.gif")
	ImageResource constraintCheck();

	@Source("images/null-constraint.gif")
	ImageResource constraintExclusion();

	@Source("images/foreign-key.gif")
	ImageResource constraintForeignKey();

	@Source("images/primary-key.gif")
	ImageResource constraintPrimaryKey();

	@Source("images/null-constraint.gif")
	ImageResource constraintTrigger();

	@Source("images/unique-key.gif")
	ImageResource constraintUniqueKey();

	@Source("images/create.gif")
	ImageResource create();

	@Source("images/browser.gif")
	ImageResource data();

	@Source("images/delete.gif")
	ImageResource delete();

	@Source("images/disable.gif")
	ImageResource disable();

	@Source("images/disable-all.gif")
	ImageResource disableAll();

	@Source("images/connection-off.gif")
	ImageResource disconnect();

	@Source("images/distribute.png")
	ImageResource distributionKey();

	@Source("images/domain.gif")
	ImageResource domain();

	@Source("images/drop.gif")
	ImageResource drop();

	@Source("images/down.png")
	ImageResource down();

	@Source("images/down_last.png")
	ImageResource downLast();

	@Source("images/enable.gif")
	ImageResource enable();

	@Source("images/enable-all.gif")
	ImageResource enableAll();

	@Source("images/enum.gif")
	ImageResource enumuration();

	@Source("images/explain.gif")
	ImageResource explain();

	@Source("images/false.gif")
	ImageResource falseBox();

	@Source("images/file-open.png")
	ImageResource fileOpen();

	@Source("images/foreign-table.gif")
	ImageResource foreignTable();

	@Source("images/foreign-tables.gif")
	ImageResource foreignTables();

	@Source("images/function.gif")
	ImageResource function();

	@Source("images/hadoop.gif")
	ImageResource hadoop();

	@Source("images/HorizontalSeparatorLine.png")
	ImageResource HorizontalSeparatorLine();

	@Source("images/indextype.gif")
	ImageResource index();

	@Source("images/index-not-unique.gif")
	ImageResource indexNotUnique();

	@Source("images/index-unique.gif")
	ImageResource indexUnique();

	@Source("images/left.png")
	ImageResource left();

	@Source("images/logo.png")
	ImageResource logo();

	@Source("images/noimage.png")
	ImageResource noimage();

	@Source("images/mat-view.gif")
	ImageResource materializedView();

	@Source("images/null-constraint.gif")
	ImageResource nullable();

	@Source("images/partial.png")
	ImageResource partial();

	@Source("images/postgresql.gif")
	ImageResource postgresql();

	@Source("images/primary-key.gif")
	ImageResource primaryKey();

	@Source("images/privilege.gif")
	ImageResource privilege();

	@Source("images/procedures.gif")
	ImageResource procedures();

	@Source("images/range.gif")
	ImageResource range();

	@Source("images/refresh.gif")
	ImageResource refresh();

	@Source("images/view-refresh.png")
	ImageResource refreshView();

	@Source("images/rename.gif")
	ImageResource rename();

	@Source("images/right.png")
	ImageResource right();

	@Source("images/rules.gif")
	ImageResource rules();

	@Source("images/run.gif")
	ImageResource run();

	@Source("images/typedsql.gif")
	ImageResource script();

	@Source("images/grant.gif")
	ImageResource security();

	@Source("images/sequences.gif")
	ImageResource sequences();

	@Source("images/sort-columns.gif")
	ImageResource sortColumns();

	@Source("images/spinner.gif")
	ImageResource spinner();

	@Source("images/worksheet.gif")
	ImageResource sqlWorksheet();

	@Source("images/percent.gif")
	ImageResource stats();

	@Source("images/save.png")
	ImageResource save();

	@Source("images/table.gif")
	ImageResource table();

	@Source("images/table-distributed.gif")
	ImageResource tableDistributed();

	@Source("images/tables.gif")
	ImageResource tables();

	@Source("images/terminate.gif")
	ImageResource terminate();

	@Source("images/trigger.gif")
	ImageResource trigger();

	@Source("images/triggers.gif")
	ImageResource triggers();

	@Source("images/true.gif")
	ImageResource trueBox();

	@Source("images/truncate.gif")
	ImageResource truncate();

	@Source("images/types.gif")
	ImageResource types();

	@Source("images/up.png")
	ImageResource up();

	@Source("images/up_first.png")
	ImageResource upFirst();

	@Source("images/view.gif")
	ImageResource view();

	@Source("images/views.gif")
	ImageResource views();

	@Source("images/warning.png")
	ImageResource warning();
}
