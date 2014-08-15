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

/*
 * TODO:
 * - Filter by database, user, application name, host
 * - Color instead show column for waiting =true
 *  - Client_port looks like not useful
 *  - Show more information regarding other variables (bg_writer, for example)
 */



package com.openscg.pgstudio.client.panels;


import org.vectomatic.file.Blob;
import org.vectomatic.file.File;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.FileUploadExt;
import org.vectomatic.file.FileUtils;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
//import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton; //added
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


import com.mastergaurav.codemirror.client.CodeMirror;
import com.mastergaurav.codemirror.client.CodeMirrorConfig;
import com.openscg.pgstudio.client.PgStudio;

import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.messages.QueryErrorJsObject;
import com.openscg.pgstudio.client.messages.QueryMetaDataJsObject;
import com.openscg.pgstudio.client.messages.QueryMetaDataResultJsObject;
import com.openscg.pgstudio.client.utils.PgRestDataSource;
import com.openscg.pgstudio.client.utils.TextFormat;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;


//
import com.openscg.pgstudio.server.util.DBVersionCheck;
import com.openscg.pgstudio.server.util.QueryExecutor;
//import com.openscg.pgstudio.server.util.DBVersionCheck.PG_FLAVORS;
/*
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
*/


public class MonitorPanel extends Composite {

	private final PgStudioServiceAsync studioService = GWT
			.create(PgStudioService.class);

	// final String columnsArray[] = {"Level","Node Type", "Relation Name",
	// "Replicated", "Alias",
	// "Join Type", "Merge Cond", "Sort Key", "Parent Relationship",
	// "Node List", "Startup Cost", "Total Cost", "Plan Rows", "Plan Width"};

	private final String DATA_PROXY_URL = "com.openscg.pgstudio.PgStudio/dataProxy";

	/*
	private final Connection conn;
	private final PG_FLAVORS pgFlavor;
	DBVersionCheck ver = new DBVersionCheck(conn);
	private final int dbVersion = ver.getPgFlavor();
	*/
	
	private final PgStudio main = new PgStudio();
	private final int dbVersion = main.getDatabaseVersion();
	private SimplePanel codePanel;
	private final TextArea codeArea = new TextArea();
	private CodeMirror cm;

	private DecoratedTabPanel tabPanel;
	private SimplePanel resultPanel;
	private SimplePanel infoPanel;
	// private SimplePanel explainPanel;

	private Widget resultsTabWidget = new HTML(TextFormat.getHeaderString(
			"Monitor", PgStudio.Images.column()));
	private Widget messageTabWidget = new
	 HTML(TextFormat.getHeaderString("General info", PgStudio.Images.column()));
	// private Widget explainTabWidget = new
	// HTML(TextFormat.getHeaderString("Explain", PgStudio.Images.column()));

	public static boolean closedByUser = false;

	private int refresh = 2000;
	private String filterDatabase = "";
	public boolean doMon = false;          // This variable is to toogle between monitoring on/off

	private String REFRESH_RATE = "Refresh rate";
	private String DB_FILTER    = "Filter by DB";


	public MonitorPanel() {

		//this.main = main;

		
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.setWidth("95%");
		mainPanel.setHeight("95%");

		mainPanel.add(getHeaderPanel());
		mainPanel.add(getOutputTablePanel());

		initWidget(mainPanel);
	}

	private Widget getHeaderPanel() {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		Widget buttons = getButtonBar();
		Widget limits = getRefreshRateBar();
		//Widget filters = getFilterDBBar(); //TODO implemented

		panel.add(buttons);
		//panel.add(filters); //TODO implemented, filter by db
		panel.add(limits);

		panel.setCellHorizontalAlignment(buttons,
				HasHorizontalAlignment.ALIGN_LEFT);
		//panel.setCellHorizontalAlignment(filters,
		//		HasHorizontalAlignment.ALIGN_RIGHT);
		panel.setCellHorizontalAlignment(limits,
				HasHorizontalAlignment.ALIGN_RIGHT);

		return panel.asWidget();
	}

	private Widget getButtonBar() {
		HorizontalPanel bar = new HorizontalPanel();

		ToggleButton run  = getRunButton();

		bar.add(run);

		return bar.asWidget();
	}

	private Widget getRefreshRateBar() {
		HorizontalPanel bar = new HorizontalPanel();

		Label lbl = new Label();
		lbl.setText(REFRESH_RATE);
		lbl.setStyleName("studio-Label-Small");

		final ListBox refreshRateBox = new ListBox();
		refreshRateBox.setStyleName("roundList");
		refreshRateBox.addItem("2");
		refreshRateBox.addItem("5");
		refreshRateBox.addItem("10");
		refreshRateBox.addItem("15");

		refreshRateBox.setSelectedIndex(0);

		refreshRateBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent arg0) {
				refresh = Integer.parseInt(refreshRateBox
						.getItemText(refreshRateBox.getSelectedIndex())) * 1000;
			}
		});

		bar.add(lbl);
		bar.add(refreshRateBox);

		bar.setCellVerticalAlignment(lbl, HasVerticalAlignment.ALIGN_MIDDLE);
		bar.setCellVerticalAlignment(refreshRateBox,
				HasVerticalAlignment.ALIGN_MIDDLE);

		return bar.asWidget();
	}
	
	/*
	 * To implement in a near future
	 * 
	private Widget getFilterDBBar() {
		HorizontalPanel bar = new HorizontalPanel();

		Label lbl = new Label();
		lbl.setText(DB_FILTER);
		lbl.setStyleName("studio-Label-Small");
		
		String searchDbs = "select datname "
				+ "from pg_database "
				+ "where datname NOT IN ('template1','template0');";
		

		final ListBox filterDBBox = new ListBox();
		filterDBBox.setStyleName("roundList");
		filterDBBox.addItem("2");
		filterDBBox.addItem("5");
		filterDBBox.addItem("10");
		filterDBBox.addItem("15");

		filterDBBox.setSelectedIndex(0);

		filterDBBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent arg0) {
				refresh = Integer.parseInt(filterDBBox
						.getItemText(filterDBBox.getSelectedIndex())) * 1000;
			}
		});

		bar.add(lbl);
		bar.add(filterDBBox);

		bar.setCellVerticalAlignment(lbl, HasVerticalAlignment.ALIGN_MIDDLE);
		bar.setCellVerticalAlignment(filterDBBox,
				HasVerticalAlignment.ALIGN_MIDDLE);

		return bar.asWidget();
	}
	*/
	
	private ToggleButton getRunButton() {
		final ToggleButton button = new ToggleButton(new Image(PgStudio.Images.run()),new Image(PgStudio.Images.terminate()));
		button.setTitle("Run");
	
		//Window.alert("Postgres version" + dbVersion);
		
		if (dbVersion >= 920) {
			final String text = "select datname, "
					+ "state, "
					+ "pid, "
					+ "usename, "
					+ "client_addr,"
					+ "backend_start::timestamp(0),"
					+ "xact_start::timestamp(0),"
					+ "justify_interval((clock_timestamp() - query_start)::interval(0)) as Query_Time,"
					+ "waiting,"
					+ "query "
					+ "from pg_stat_activity "
					+ "order by 7 desc"; // Order by Query Duration, I guess is the most obvious.
	    } else  {
	    	final String text = "select datname, "
					+ "procpid, "
					+ "usename, "
					+ "client_addr,"
					+ "backend_start::timestamp(0),"
					+ "xact_start::timestamp(0),"
					+ "justify_interval((clock_timestamp() - query_start)::interval(0)) as Query_Time,"
					+ "waiting,"
					+ "current_query "
					+ "from pg_stat_activity "
					+ "order by 7 desc"; // Order by Query Duration, I guess is the most obvious.
	    }
		
		// Detect version and change query (current_query / query)
		final String text = "select datname, "
				+ "procpid, "
				+ "usename, "
				+ "client_addr,"
				+ "backend_start::timestamp(0),"
				+ "xact_start::timestamp(0),"
				+ "justify_interval((clock_timestamp() - query_start)::interval(0)) as Query_Time,"
				+ "waiting,"
				+ "current_query "
				+ "from pg_stat_activity "
				+ "order by 7 desc"; // Order by Query Duration, I guess is the most obvious.
		
		//Not yet implemented
		final String _numbackends = "select sum(numbackends) "
				+ "from pg_stat_database ";
				
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
				final String monitorQuery = new String(text);
				final int monitorDelay = refresh;
					
				if (doMon){
					doMon = false;
				} else {
					doMon = true;
				}
				
				
				final Timer t = new Timer() { 
					@Override
					public void run() {
						
						if (!doMon){
							cancel();
							return;
						}
						studioService.getQueryMetaData(PgStudio.getToken(),
								monitorQuery, new AsyncCallback<String>() {
									public void onFailure(Throwable caught) {
										// Show the RPC error message to the user
										Window.alert(caught.getMessage());
									}

									public void onSuccess(String result) {
										JsArray<QueryMetaDataResultJsObject> res = json2Messages(result);

										if (res.length() > 0) {
											JsArray<QueryErrorJsObject> error = res
													.get(0).getError();

											if (error == null) {
												error = JavaScriptObject
														.createArray().cast();
											}

											if (error.length() > 0) {
												QueryErrorJsObject err = error
														.get(0);
												Window.alert(err.getError());
											} else {
												String _monitorQuery = monitorQuery;
												//tabPanel.selectTab(0);
												setupGrid(res.get(0).getMetaData(),
														_monitorQuery);
											}
											
										}

									}
								});
					}
												
				};
				
												
				//Window.alert("Debug: " + doMon + " timer status: " + t.isRunning());

				if (doMon) {
					tabPanel.selectTab(0);              // each time I start the monitoring, I choose the Monitor Tab
					t.scheduleRepeating(monitorDelay);
				} 
				
			}
		});
		
		return button;
	}
    

	private CodeMirrorConfig getConfig() {
		CodeMirrorConfig config = CodeMirrorConfig.getDefault();

		String parserFile = GWT.getModuleBaseURL()
				+ "cm/contrib/sql/js/parsesql.js";
		String styleSheet = GWT.getModuleBaseURL()
				+ "cm/contrib/sql/css/sqlcolors.css";

		config.setParserFile(parserFile);
		config.setStylesheet(styleSheet);

		config.setWidth(1200, Unit.PX);
		config.setHeight(150, Unit.PX);

		return config;
	}

	private Widget getOutputTablePanel() {
		tabPanel = new DecoratedTabPanel();

		tabPanel.setHeight("450px");
		tabPanel.setWidth("1200px");
		tabPanel.setStyleName("studio-DecoratedTabBar");

		tabPanel.add(getResultsPanel(), resultsTabWidget);
		tabPanel.add(getInfoPanel(), messageTabWidget);

		return tabPanel.asWidget();
	}

	private Widget getResultsPanel() {
		resultPanel = new SimplePanel();

		final ListGrid listGrid = new ListGrid();

		resultPanel.add(listGrid);

		return resultPanel.asWidget();
	}

	private Widget getInfoPanel(){
		infoPanel = new SimplePanel();
		return infoPanel.asWidget();
	}
	
	
	private void setupGrid(JsArray<QueryMetaDataJsObject> metadata, String query) {
		int numCols = metadata.length();

		ListGridField[] fields = new ListGridField[numCols];

		for (int i = 0; i < numCols; i++) {
			QueryMetaDataJsObject col = metadata.get(i);
			String name = col.getName();

			ListGridField c = new ListGridField(name, getColumnWidth(col));
			c.setType(getColumnType(col));
			fields[i] = c;
		}
		
		final ListGrid listGrid = new ListGrid();
		listGrid.setWidth(1200);
		listGrid.setHeight(420);

		PgRestDataSource dataSource = new PgRestDataSource();
		dataSource.setQuery(query);

		dataSource.setDataURL(DATA_PROXY_URL);

		listGrid.setDataSource(dataSource);
		listGrid.setFields(fields);
		listGrid.setDataFetchMode(FetchMode.LOCAL);
		listGrid.setAutoFetchData(true);

		resultPanel.remove(resultPanel.getWidget());
		resultPanel.add(listGrid);

	}


	private int getColumnWidth(QueryMetaDataJsObject col) {
		int dataType = col.getDataType();

		int width;
		switch (dataType) {
		case java.sql.Types.BIGINT:
		case java.sql.Types.FLOAT:
		case java.sql.Types.DECIMAL:
		case java.sql.Types.INTEGER:
		case java.sql.Types.NUMERIC:
		case java.sql.Types.REAL:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.TINYINT:
			width = 50;
			break;
		case java.sql.Types.LONGNVARCHAR:
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.VARCHAR:
			width = 125;
			break;
		case java.sql.Types.BOOLEAN:
			width = 20;
			break;
		case java.sql.Types.DATE:
		case java.sql.Types.TIME:
			width = 55;
			break;
		case java.sql.Types.TIMESTAMP:
			width = 110;
			break;

		default:
			width = 80;
		}

		return width;
	}

	private ListGridFieldType getColumnType(QueryMetaDataJsObject col) {
		int dataType = col.getDataType();

		ListGridFieldType type;
		switch (dataType) {
		case java.sql.Types.NUMERIC:
		case java.sql.Types.REAL:
		case java.sql.Types.DECIMAL:
		case java.sql.Types.FLOAT:
			type = ListGridFieldType.FLOAT;
			break;
		case java.sql.Types.BIGINT:
		case java.sql.Types.INTEGER:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.TINYINT:
			type = ListGridFieldType.INTEGER;
			break;
		case java.sql.Types.LONGNVARCHAR:
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.VARCHAR:
			type = ListGridFieldType.TEXT;
			break;
		case java.sql.Types.BOOLEAN:
			type = ListGridFieldType.BOOLEAN;
			break;
		case java.sql.Types.DATE:
			type = ListGridFieldType.DATE;
			break;
		case java.sql.Types.TIME:
		case java.sql.Types.TIMESTAMP:
			type = ListGridFieldType.TIME;
			break;

		default:
			type = ListGridFieldType.TEXT;
		}

		return type;
	}


	private static final native JsArray<QueryMetaDataResultJsObject> json2Messages(
			String json)
	/*-{ 
	  	return eval(json); 
	}-*/;

	private static final native JsArray<QueryMetaDataJsObject> json2MetaDataMessages(
			String json)
	/*-{ 
	  	return eval(json); 
	}-*/;

	private static final native JsArray<QueryErrorJsObject> json2ErrorMessages(
			String json)
	/*-{ 
	  	return eval(json); 
	}-*/;

}
