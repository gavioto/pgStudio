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

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.i18n.client.NumberFormat;
public class ExplainGridData {
	static int rowCount = 0;
	static JSONObject jsonObject = new JSONObject();
	static JSONObject[] jsonObjectArray = new JSONObject[7];
	static int JSONcount = 0;
	static String columnsArray[] = {"Level","Node Type", "Replicated", "Node List", "Join Type", "Startup Cost", "Total Cost", "Plan Rows", "Plan Width", "Merge Cond", "Sort Key", "Parent Relationship", "Relation Name", "Alias"};
	static int colCount = columnsArray.length;
	static String explainGridElements[][] = new String[1+rowCount][colCount];
	static String jsonString;
	static String initialString;
	static int[] rowLevel; //rowLevel keeps track of the depth of each jsonObject

	public ExplainGridData(String inputString, String[] colNames)
	{
		initialString = inputString;
		jsonString = inputString.substring(1,inputString.length()-1);
		rowCount = getRowCount();
		rowLevel = new int[rowCount];
		columnsArray = colNames;
		//resize Array to number of "{" in the input String (Identifies 1+number of json Objects)
		jsonObjectArray =  new JSONObject[inputString.length()-inputString.replaceAll("\\{", "").length()-1];
	}

	public String[][] CreateExplanGridArray()
	{
		explainGridElements = new String[rowCount][colCount];
		return explainGridElements;
	}

		public int getRowCount(){
			rowCount = 0;
			String tempVal = jsonString;
			for( int i=0; i<tempVal.length(); i++ ) {
			    if( tempVal.charAt(i) == '{' ) {
			        rowCount++;
			    }
			}
		return rowCount;
	}

		public String[][] getGridElements()
	{
		CreateExplanGridArray();
		runGrid();
		return explainGridElements;
	}

	//recursively store ALL elements(jsonObjects) in an array of jsonObject type.
	//parameters indicate: jsonObjectArray, sequential jsonObject number, jsonArray to evaluate, jsonObject depth/indent/level
	public static void jsonToArray(JSONObject jsonObject, JSONObject[] jsonObjectArray, int count, JSONArray jArray, int level)
	{

		try{
			rowLevel[JSONcount] = level;
			jsonObjectArray[JSONcount] = jsonObject;

			if (jsonObject.containsKey("Plans"))
			{			jArray = (JSONArray)JSONParser.parse(jsonObject.get("Plans").toString());			}

			if (jArray.size() < 1 || !jsonObject.containsKey("Plans"))
			{
				++count;
			}

			else if (jArray.size() >= 1)
			{
				++level;
				for (int i = 0; i < jArray.size(); i++)
				{
					jsonObject = (JSONObject) JSONParser.parse(jArray.get(i).toString());
					jsonToArray(jsonObject, jsonObjectArray, ++JSONcount,jArray,level);
				}

			}

		}
		catch(Exception e){
		}

	return;
	}

	//Grabs all elements of each jsonObject in jsonObject array and stores in 2d Array format
	public static void arrayToGrid(String[] columnsArray, String[][] explainGridElements)
	{
	// double value = 12345.6789;
	 //String format = NumberFormat.getFormat("0.0").format(value);
	for (int i = 0; i< explainGridElements[0].length; i++)
	{
		explainGridElements[0][i] = columnsArray[i];
	}

	for (int rows = 1; rows< explainGridElements.length; rows ++)
	{

		for (int cols = 0; cols < explainGridElements[0].length; cols++)
		{
			try{
				//Object's data matching the column name as key
				if (jsonObjectArray[rows-1].containsKey(explainGridElements[0][cols]) && cols >= 10 && cols <=13){
					explainGridElements[rows][cols] =  NumberFormat.getFormat("0.0").format(Double.parseDouble((jsonObjectArray[rows-1].get(explainGridElements[0][cols]).toString()))); 

				}
				else if (jsonObjectArray[rows-1].containsKey(explainGridElements[0][cols])){
					explainGridElements[rows][cols]= jsonObjectArray[rows-1].get(explainGridElements[0][cols]).toString();
				}
			}
			catch(Exception e){

			}
		}

		String levelDash = "";
		for (int lev = 0; lev < rowLevel[rows-1]; lev++ )
		{
			levelDash += ">-";

		}
		explainGridElements[rows][0] = levelDash + "> " + rowLevel[rows-1];
	}

	}
	public  void runGrid() {
		JSONcount = 0;
		JSONArray jArray = new JSONArray();

		try{

		jsonObjectArray[JSONcount] = (JSONObject)JSONParser.parse(jsonString);
		jArray = (JSONArray)JSONParser.parse(initialString);
		jsonObjectArray[JSONcount] = (JSONObject)((JSONObject)jArray.get(0)).get("Plan");
		jsonObject = (JSONObject)JSONParser.parse(jsonString);
		}
		catch(Exception e){e.getMessage();}
		try{
		jsonObject = (JSONObject) JSONParser.parse(jsonObject.get("Plan").toString());
		}
		catch(Exception e)
		{

		}
		jsonToArray(jsonObject, jsonObjectArray, JSONcount,jArray,1);

		arrayToGrid(columnsArray, explainGridElements);

	}

}
