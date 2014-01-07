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
import com.google.gwt.user.client.ui.PushButton;
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

public class SQLWorksheet extends Composite  {

	private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

	final String columnsArray[] = {"Level","Node Type", "Relation Name", "Replicated", "Alias",
									"Join Type", "Merge Cond", "Sort Key",  "Parent Relationship",
									"Node List",  "Startup Cost", "Total Cost", "Plan Rows", "Plan Width"};

	private final String DATA_PROXY_URL = "com.openscg.pgstudio.PgStudio/dataProxy";
	
	private SimplePanel codePanel;
	private final TextArea codeArea = new TextArea();
	private CodeMirror cm;
	
	private final FileUploadExt upload = new FileUploadExt();
	
	private DecoratedTabPanel tabPanel;
	private SimplePanel resultPanel;
	private SimplePanel messagePanel;
	private SimplePanel explainPanel;

	private Widget resultsTabWidget = new HTML(TextFormat.getHeaderString("Results", PgStudio.Images.column()));
	private Widget messageTabWidget = new HTML(TextFormat.getHeaderString("Messages", PgStudio.Images.column()));
	private Widget explainTabWidget = new HTML(TextFormat.getHeaderString("Explain", PgStudio.Images.column()));

	public static boolean closedByUser = false;
	
	private int limit = 100;

	private String LIMIT_STR = "Limit";
	private String NO_LIMIT_STR = "No Limit";
	private String INVALID_FILE_STR = "Invalid file type";
	private String PROBLEM_READING_FILE_STR = "Problem reading file";
	
	public SQLWorksheet() {
		
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.setWidth("95%");
		mainPanel.setHeight("95%");

		mainPanel.add(getHeaderPanel());
		mainPanel.add(getSQLPanel());
		mainPanel.add(getOutputTablePanel());

		mainPanel.add(upload);
		upload.setVisible(false);

		initWidget(mainPanel);
	}

	public void setupCodePanel() {
		Element e = codeArea.getElement();
		TextAreaElement tae = TextAreaElement.as(e);
		
		CodeMirrorConfig config = getConfig();
		cm = CodeMirror.fromTextArea(tae, config);	
	}
	
	public void setupCodePanel(String textAreaContent) {
		Element e = codeArea.getElement();
		TextAreaElement tae = TextAreaElement.as(e);
		tae.setInnerText(textAreaContent);
		CodeMirrorConfig config = getConfig();
		cm = CodeMirror.fromTextArea(tae, config);	
	}
	
	private Widget getHeaderPanel() {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		
		Widget buttons = getButtonBar();
		Widget limits = getLimitBar();
		
		panel.add(buttons);
		panel.add(limits);
		
		panel.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_LEFT);
		panel.setCellHorizontalAlignment(limits, HasHorizontalAlignment.ALIGN_RIGHT);
		
		return panel.asWidget();
	}
	
	private Widget getButtonBar() {
		HorizontalPanel bar = new HorizontalPanel();
		
		PushButton save = getSaveButton();
		PushButton open = getOpenButton();
		PushButton stop = getTerminateButton();
		PushButton run = getRunButton();
		PushButton explain = getExplainButton();
		
		try {
			if (FileUtils.supportsFileAPI()) {
				bar.add(save);
				bar.add(open);
			}
		} catch (Exception ex) {
			// Chrome does not expose the DataTransfer object
			if (ex.getMessage().contains("DataTransfer is not defined")) {
				bar.add(save);
				bar.add(open);
			}
		}
		
		bar.add(stop);
		bar.add(run);
		bar.add(explain);
		
		return bar.asWidget();
	}
		
	private Widget getLimitBar() {
		HorizontalPanel bar = new HorizontalPanel();

		Label lbl = new Label();
		lbl.setText(LIMIT_STR);
		lbl.setStyleName("studio-Label-Small");

		final ListBox limitBox = new ListBox();
		limitBox.setStyleName("roundList");
		limitBox.addItem("100");
		limitBox.addItem("500");
		limitBox.addItem("1000");
		limitBox.addItem(NO_LIMIT_STR);
		
		limitBox.setSelectedIndex(0);
		
		limitBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent arg0) {
				if (limitBox.getItemText(limitBox.getSelectedIndex()).equals(NO_LIMIT_STR)) {
					limit = -1;
				} else {
					limit = Integer.parseInt(limitBox.getItemText(limitBox.getSelectedIndex()));
				}
			}
		});
		
		bar.add(lbl);
		bar.add(limitBox);
		
		bar.setCellVerticalAlignment(lbl, HasVerticalAlignment.ALIGN_MIDDLE);
		bar.setCellVerticalAlignment(limitBox, HasVerticalAlignment.ALIGN_MIDDLE);
		
		return bar.asWidget();
	}
	
	private PushButton getRunButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.run()));
		button.setTitle("Run");
		
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String text;
				if (cm.getSelectedText().equals("")) {
					text = cm.getContent();
				} else {
					text = cm.getSelectedText();
				}
				String preQuery = new String(text);

				if (!preQuery.equals("")) {
					// If it is a select, let's chop everything after including the first ";"
					if (preQuery.trim().toLowerCase().startsWith("select") 
								&& preQuery.trim().indexOf(";") > 0) {
						preQuery = preQuery.trim().substring(0,preQuery.trim().indexOf(";"));
					}
					
					final String query = preQuery;
					
					if(query.toUpperCase().startsWith("EXPLAIN")){
						tabPanel.selectTab(2);
						setupExplain(query.substring(8));
					} else {
						studioService.getQueryMetaData(PgStudio.getToken(), query, new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user
				                Window.alert(caught.getMessage());
							}
	
							public void onSuccess(String result) {						
				    			JsArray<QueryMetaDataResultJsObject> res = json2Messages(result);
				    			
				    			if (res.length() > 0) {
				    				JsArray<QueryErrorJsObject> error = res.get(0).getError();
	
				    				// If there is no error record in the message, the array is null
				    				// so set it to an empty array.
				    				if (error == null) {
				    					error = JavaScriptObject.createArray().cast();;
				    				}
	
				    				if (error.length() > 0) {
					            		QueryErrorJsObject err = error.get(0);
										Window.alert(err.getError());			    					
				    				} else {
			    						String selectQuery = query;
		    							if (res.get(0).getQueryType().equals("SELECT")) {
				    						tabPanel.selectTab(0);
				    						if(!selectQuery.toLowerCase().contains("limit")){
				    							if (limit > 0)
				    								selectQuery = selectQuery + " LIMIT " + limit;	
				    						}
				    						setupGrid(res.get(0).getMetaData(), selectQuery);
				    					} else {
				    						tabPanel.selectTab(1);
				    						setupMessage(query, res.get(0).getQueryType());
				    					}
				    				}
				    			}
							}
						});		
					}
				}
			}			
		});
		
		return button;
	}

	private PushButton getSaveButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.save()));
		button.setTitle("Save");
		
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				String url = FileUtils.createDataUrl("application/octet-stream", cm.getContent());
				Window.open(url, "_self", null);
			}
			
		});
		
		return button;
	}

	private PushButton getOpenButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.fileOpen()));
		button.setTitle("Open");

		upload.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent arg0) {
				processFiles(upload.getFiles());			}
			
		});
		
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				upload.click();
			}
			
		});

		return button;
	}

	private PushButton getTerminateButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.terminate()));
		button.setTitle("Stop");
		
		return button;
	}

	private PushButton getExplainButton() {
		PushButton button = new PushButton(new Image(PgStudio.Images.explain()));
		button.setTitle("Explain");
		button.addClickHandler(getExplainClickHandler());
		
		return button;
	}
	
	private ClickHandler getExplainClickHandler() {
		ClickHandler handler = new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				String text;
				String query = "";
				
				if (cm.getSelectedText().equals("")) {
					text = cm.getContent();
				} else {
					text = cm.getSelectedText();
				}
				String preQuery = new String(text);

				String firstWord = preQuery.split(" ")[0].toUpperCase();
				
				if (firstWord.equals("SELECT") || firstWord.equals("INSERT") || 
					firstWord.equals("UPDATE") || firstWord.equals("DELETE")){
					// Valid query for explain so no need to massage it further
					query = preQuery;
				} else if (firstWord.equals("EXPLAIN")) {
					String validVerb = "";
					
					// Find the first valid verb
					String[] words = preQuery.split(" ");
					for (String word : words) {
						if (word.toUpperCase().equals("SELECT") || word.toUpperCase().equals("INSERT") || 
							word.toUpperCase().equals("UPDATE") || word.toUpperCase().equals("DELETE")){
							validVerb = word;
							break;
						}
					}
					
					if (validVerb.equals("")) {
						Window.alert("EXPLAIN is not valid with this query");
						// Stop the event						
					} else {
						query = preQuery.substring(preQuery.indexOf(validVerb));
					}
					
				} else {
					Window.alert("EXPLAIN is not valid with this query");
					// Stop the event
				}

				// We can only EXPLAIN a single query at a time so remove any additional
				// queries if they exist
				if (!query.equals("")) {
					if (query.indexOf(";") > 0) {
						query = query.trim().substring(0,query.indexOf(";"));
					}
				}
				
				tabPanel.selectTab(2);
				setupExplain(query);
			}
			
		};
		
		return handler;
	}
	
	private Widget getSQLPanel() {
		codePanel = new SimplePanel();
				
		codeArea.setWidth("1200px");
		codeArea.setHeight("150px");

		codeArea.setReadOnly(false);
		
		codePanel.add(codeArea);
		
		return codePanel.asWidget();
	}
	
	private CodeMirrorConfig getConfig() {
		CodeMirrorConfig config = CodeMirrorConfig.getDefault();
		
		String parserFile = GWT.getModuleBaseURL() + "cm/contrib/sql/js/parsesql.js";
		String styleSheet = GWT.getModuleBaseURL() + "cm/contrib/sql/css/sqlcolors.css";
		
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
		tabPanel.add(getMessagePanel(), messageTabWidget);
		tabPanel.add(getExplainPanel(), explainTabWidget);

		
		return tabPanel.asWidget();
	}
	
	private Widget getResultsPanel() {
		resultPanel = new SimplePanel();
		        
        final ListGrid listGrid = new ListGrid();

        resultPanel.add(listGrid);
        
		return resultPanel.asWidget();
	}

	private Widget getMessagePanel() {
		messagePanel = new SimplePanel();
		
		TextArea msg = new TextArea();
		msg.setWidth("1200px");
		msg.setHeight("420px");
		msg.setReadOnly(true);
		
		messagePanel.add(msg);
		
		return messagePanel.asWidget();
	}

	private Widget getExplainPanel() {
		explainPanel = new SimplePanel();
		
		return explainPanel.asWidget();
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
	
	private void setupMessage(String query, String queryType) {
		// Clear out area so we can try and see it "flash" so we know it ran.
		// TODO: Would be great to show the number of affect rows		
		((TextArea)messagePanel.getWidget()).setText("");
		studioService.executeQuery(PgStudio.getToken(), query, queryType, new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// Show the RPC error message to the user
                Window.alert(caught.getMessage());
			}

			public void onSuccess(String result) {
				((TextArea)messagePanel.getWidget()).setText(result);
			}
		});		
		
	}
	
	private void setupExplain(String query){
		final ListGrid explainGrid = new ListGrid();
		explainGrid.setWidth(1200);
		explainGrid.setHeight(224);
		explainGrid.setAlternateRecordStyles(true);
		explainGrid.setShowAllRecords(true);

		final ListGridField[] listGridField = new ListGridField[columnsArray.length];//Array of columns

		for (int i = 0; i < columnsArray.length; i ++) { //Dynamic population of columns from columnsArray
			listGridField[i] = new ListGridField(columnsArray[i], columnsArray[i], 80);
			listGridField[i].setType(ListGridFieldType.TEXT);
		}

		listGridField[1].setWidth(135);
		listGridField[2].setWidth(95);
		listGridField[3].setWidth(65);
		listGridField[4].setWidth(50);
		listGridField[5].setWidth(65);
		listGridField[6].setWidth(125);
		listGridField[7].setWidth(75);
		listGridField[8].setWidth(100);
		listGridField[9].setWidth(200);
		listGridField[10].setWidth(70);

		for (int i = 11; i <columnsArray.length; i ++) {
			listGridField[i].setAlign(Alignment.RIGHT);
			listGridField[i].setWidth(65);
		}

		studioService.getExplainResult(PgStudio.getToken(), query, new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// Show the RPC error message to the user
                Window.alert(caught.getMessage());
			}
			
			public void onSuccess(String result) {
				
				//Constructor passing in result as json String and array of column names.
				try{
					ExplainGridData data = new ExplainGridData(result,columnsArray);
					String[][] elements = data.getGridElements();
					ListGridField[] fields = new ListGridField[columnsArray.length]; //dynamic length

					//Populate fields with list grid fields
					for (int i = 0; i < columnsArray.length; i++)
						fields[i] = listGridField[i];

					explainGrid.setFields(fields);

					ListGridRecord record = new ListGridRecord();

					for (int eachRow = 0; eachRow < elements.length-1; eachRow++) {
						record = new ListGridRecord();

						for (int eachCol = 0; eachCol < elements[0].length; eachCol++)
							record.setAttribute(elements[0][eachCol], elements[eachRow+1][eachCol]);

						explainGrid.addData(record);
					}

					explainPanel.setWidget(explainGrid);
				}
				catch(Exception e) {
					Window.alert("ERROR: Invalid explain - " + e.getMessage() );
				}
			}
		});
	}

	private int getColumnWidth(QueryMetaDataJsObject col) {
		int dataType = col.getDataType();
		
		int width;
		switch(dataType) {
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
				width = 50;
				break;
			case java.sql.Types.DATE:
			case java.sql.Types.TIME:
				width = 50;
				break;
			case java.sql.Types.TIMESTAMP:
				width = 100;
				break;
				
			default:
				width = 100;
		}
		
		return width;
	}

	private ListGridFieldType getColumnType(QueryMetaDataJsObject col) {
		int dataType = col.getDataType();
		
		ListGridFieldType type;
		switch(dataType) {
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

	private void processFiles(FileList files) {
		final FileReader reader = new FileReader();

		reader.addLoadEndHandler(new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				if (reader.getError() == null) {
					try {
						cm.setContent(reader.getStringResult());
					} catch (Exception ex) {
						Window.alert(PROBLEM_READING_FILE_STR);
					}
				}
			}

		});

		try {
			for (File file : files) {
				String type = file.getType();

				if (type.equals("") || type.startsWith("text/")) {
					Blob blob = file;
					if (file.getSize() > 0) {
						blob = file.slice();
					}
					reader.readAsText(blob);
				} else {
					Window.alert(INVALID_FILE_STR);
				}
			}
		} catch (Exception ex) {
			Window.alert(PROBLEM_READING_FILE_STR);
		}
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
