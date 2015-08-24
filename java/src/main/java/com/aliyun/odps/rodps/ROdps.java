/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package com.aliyun.odps.rodps;


import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.aliyun.odps.Odps;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.rodps.DataTunnel.RDTUploader;
import com.aliyun.odps.rodps.DataTunnel.ROdpsException;
import com.aliyun.odps.tunnel.TableTunnel.DownloadSession;
import com.aliyun.odps.tunnel.TableTunnel.UploadSession;
import com.aliyun.odps.*;
import com.aliyun.openservices.odps.console.*;
import com.aliyun.odps.rodps.DataTunnel.*;

public class ROdps {

    static Log LOG = LogFactory.getLog(ROdps.class);
    private String ODPS_PROJECT_NAME;
    private final Odps odps;
    private final String ODPS_ACCESS_ID;
    private final String ODPS_ACCESS_KEY;
    private final String ODPS_ENDPOINT;
    private final String DT_ENDPOINT;
    private final String USER_COMMANDS;
    private final static int RETRY_MAX = 3;
    private ROdpsClient console;
    //private final ODPSConnection client;
    private Project mProject;

    public ROdps(String projectName, String accessID, String accessKey,
            String endPoint, String dtEndpoint,
            String userCommands,String log4j_properties) throws ROdpsException, OdpsException {

        if(log4j_properties.isEmpty())
			LOG = LogFactory.getLog(ROdps.class);
		else{
			PropertyConfigurator.configure(log4j_properties);  
			LOG = LogFactory.getLog(ROdps.class);
		}
        LOG.info("start init Odps");
        if (projectName.equals("NA") || accessID.equals("NA")
                || accessKey.equals("NA") || endPoint.equals("NA")) {
            throw new ROdpsException(
                    "NA found with project/accessID/accessKey/endPoint");
        }
        
        ODPS_PROJECT_NAME = projectName;
        ODPS_ACCESS_ID = accessID;
        ODPS_ACCESS_KEY = accessKey;
        ODPS_ENDPOINT = endPoint;
        DT_ENDPOINT = dtEndpoint;
        USER_COMMANDS = userCommands;
        odps = new Odps(new AliyunAccount(accessID, accessKey));
        odps.setEndpoint(endPoint);
        odps.setDefaultProject(projectName);
        //client = getODPSConnection();

        try {
            console = new ROdpsClient(this.ODPS_ACCESS_ID,
                    this.ODPS_ACCESS_KEY, this.ODPS_ENDPOINT,
                    this.ODPS_PROJECT_NAME, this.USER_COMMANDS);
        } catch (Exception e) {
            LOG.error(e);
            throw new ROdpsException(e, "Init OdpsROperator exception");
        }
        //mProject = new Project(client, projectName);
        mProject = odps.projects().get(projectName);
        mProject.reload();
    }

    
    //use tunnel sdk to download table
    public void writeTableFromDT(String projectName, String tableName,
            String partition, String dataFilePathName,
            String columnDelimiter, String rowDelimiter,
            long recordCount,int threadNumber) throws ROdpsException {
    	
        if (DT_ENDPOINT.equals("NA")) {
            throw new ROdpsException("DT endpoint is invalid, please check odps_config.ini");
        }
        int retryTimes = 0;
        while (true) {
          try {        	
            LOG.info("before create RDTUploader");
            if(projectName==null){
              projectName=this.ODPS_PROJECT_NAME;
            }
            if(partition!=null){
             partition=formatPartition(partition, "", ",");
            }
            Context<UploadSession> context = new Context<UploadSession>(odps,DT_ENDPOINT,
                projectName,tableName,partition,-1,columnDelimiter,rowDelimiter,threadNumber);
            context.setRecordCount(recordCount);
            RDTUploader uploader=new RDTUploader(context);            
            uploader.upload(dataFilePathName);
            return;
          } catch (IOException e) {
            if (++retryTimes <= RETRY_MAX){
              LOG.error("write table encounter exception:" + e.getMessage()
                  + ", retry times = " + retryTimes);
              try {
                Thread.sleep(5000);
              } catch (InterruptedException e1) {
                LOG.error("Sleep interrupted!", e1);
              }
              continue;
            }
            throw new ROdpsException(e);
          } catch (Exception e) {
            throw new ROdpsException(e);
          }
        }
    }
    
    /*
     * *use tunnel sdk to load table from odps
      * *
     * */
    public List<List<String>> loadTableFromDT(String projectName,
            String tableName, String partition, String tempFile,
            String colDelimiter, String rowDelimiter, int limit,
            int threadNumber)
            throws ROdpsException {
        if (DT_ENDPOINT.equals("NA")) {
            throw new ROdpsException("DT endpoint is invalid, please check odps_config.ini");
        }
        int retryTimes = 0;
        while (true) {
          try {
  
            if (projectName == null) {
              projectName = ODPS_PROJECT_NAME;
            }
            if(partition!=null){
             	partition=formatPartition(partition, "", ",");
            }
            Context<DownloadSession> context=new Context<DownloadSession>(odps,DT_ENDPOINT,
                projectName,tableName,partition,limit,colDelimiter,rowDelimiter,threadNumber);
            RDTDownloader downloader = new RDTDownloader(context);
            return downloader.downloadTable(tempFile);
          } catch (IOException e) {
            if (++retryTimes <= RETRY_MAX){
              LOG.error("load table encounter exception:" + e.getMessage()
                  + ", retry times = " + retryTimes);
              try {
                Thread.sleep(5000);
              } catch (InterruptedException e1) {
                LOG.error("Sleep interrupted!", e1);
              }
              continue;
            }
            throw new ROdpsException(e);
          } catch (Exception e){
            throw new ROdpsException(e);
          }
        }
    }

    /**
     * @throws OdpsException 
     * @throws ROdpsException
     *             use project 命令
     * @Title: useProject
     * @Description: TODO
     * @param projectName
     * @return
     * @return boolean
     * @throws
     */
    public boolean useProject(String projectName) throws ROdpsException, OdpsException {
        if (projectName == null) {
            throw new ROdpsException("ProjectName is null");
        }
        projectName = projectName.trim().toLowerCase();
        this.runSqlTask("use " + projectName);
        this.ODPS_PROJECT_NAME = projectName;
        this.console.updateCurrentProject(projectName);
        //this.mProject = new Project(client, projectName);
        this.mProject = odps.projects().get(projectName);
        this.mProject.reload();
        
        return true;
    }

    

    /**
     * @throws ROdpsException
     *             将json转化成name:type的map
     * @Title: createSchema
     * @Description: TODO
     * @param schemaJson
     * @return
     * @throws JSONException
     * @return Map<String,String>
     * @throws
     */
    private Map<String, Schema> createSchema(String schemaJson, String type)
            throws ROdpsException {
        Map<String, Schema> ret = new LinkedHashMap<String, Schema>();
        try {
            JSONObject jsonMap = new JSONObject(schemaJson);
            if (jsonMap.get(type) != null) {
                JSONArray jsonArray = jsonMap.getJSONArray(type);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject col = (JSONObject) (jsonArray.get(i));
                    Schema schema = new Schema(col.getString("name"),
                            col.getString("type"),
                            (col.has("comment") ? col.getString("comment")
                            : null));
                    schema.setPartitionKey(type.equals("partitionKeys"));
                    ret.put(col.getString("name"), schema);
                }
            }
            return ret;
        } catch (Exception e) {
            LOG.error(e);
            throw new ROdpsException(e);
        }
    }

    /**
     * @throws OdpsException 
     * 根据projectName创建Project对象
     * 
     * @Title: getProjectObject
     * @Description: TODO
     * @param projectName
     * @return
     * @return Project
     * @throws
     */
    private Project getProjectObject(String projectName) throws OdpsException {
        if (projectName == null || projectName.isEmpty()
                || projectName.equals(this.ODPS_PROJECT_NAME)) {
            return this.mProject;
        } else {
            //return new Project(client, projectName);
            Project TempProject = odps.projects().get(projectName);
            TempProject.reload();
            return TempProject;
        }
    }

    /**
     * 根据projectName是否为null返回projectName
     * 
     * @Title: getProjectName
     * @Description: TODO
     * @param projectName
     * @return
     * @return String
     * @throws
     */
    public String getProjectName(String projectName) {
        if (projectName == null || projectName.isEmpty()
                || projectName.equals(this.ODPS_PROJECT_NAME)) {
            return this.ODPS_PROJECT_NAME;
        } else {
            return projectName;
        }
    }

    /**
     * @throws OdpsException 
     * @throws ROdpsException
     * @throws CloneNotSupportedException
     * @throws ClientException
     * @throws ODPSConsoleException
     * @throws ODPSException
     *             查看表详细信息
     * @Title: DescribeTable
     * @Description: TODO
     * @param projectName
     * @param tableName
     * @return
     * @return String
     * @throws
     */
    public List<DataFrameItem> describeTable(String projectName,
            String tableName,String partition) throws ROdpsException, OdpsException {
        Project project = this.getProjectObject(projectName);
        //Table tbl = new Table(project, tableName);
        Table tbl = odps.tables().get(this.getProjectName(projectName),tableName);
        List<DataFrameItem> ps = new ArrayList<DataFrameItem>();
        if(partition!=null){
        	partition=this.formatPartition(partition, "'", ",");
        }
        try {
            tbl.reload();
            ps.add(createSingleValueFrame("owner", "String", tbl.getOwner()));
            //ps.add(this.createSingleValueFrame("project", "String", tbl.getProject().getName()));
            ps.add(this.createSingleValueFrame("project", "String", tbl.getProject()));
            ps.add(this.createSingleValueFrame("comment", "String",
                    tbl.getComment()));
            ps.add(this.createSingleValueFrame("create_time", "DateTime",
                    formatDateTime(tbl.getCreatedTime())));
            ps.add(this.createSingleValueFrame("last_modified_time",
                    "DateTime", formatDateTime(tbl.getLastMetaModifiedTime())));
            String desc = this.getDescText(projectName, tableName, partition);
            if(this.isInternalTable(desc)){
	            long size=this.getTableSize(projectName, tableName, partition);
	            if(partition==null || partition.isEmpty()){
	            	ps.add(this.createSingleValueFrame("size", "Long",size ));
	            }else{
	            	ps.add(this.createSingleValueFrame("partition_size", "Long",size ));
	            	ps.add(this.createSingleValueFrame("partition_name", "String",partition ));
	            }
	        	ps.add(this.createSingleValueFrame("is_internal_table", "boolean",true));
            }else{
            	ps.add(this.createSingleValueFrame("is_internal_table", "boolean",false));
            }
            //Map<String, Schema> columns = this.createSchema(tbl.getSchema().toJson(), "columns");
            Map<String, Schema> columns = this.createSchema(tbl.getJsonSchema(), "columns");
       
            DataFrameItem item = new DataFrameItem("columns", "string");
            for (Map.Entry<String, Schema> entry : columns.entrySet()) {
                item.getData().add(entry.getValue().toString());
            }
            ps.add(item);
            Map<String, Schema> ptKeys = this.createSchema(tbl.getJsonSchema(), "partitionKeys");
            if (ptKeys != null && ptKeys.size() > 0) {
                DataFrameItem ptItem = new DataFrameItem("partition_keys",
                        "String");
                for (Map.Entry<String, Schema> entry : ptKeys.entrySet()) {
                    ptItem.getData().add(entry.getValue().toString());
                }
                ps.add(ptItem);
            }
            return ps;
        } catch (Exception e) {
            LOG.error(e);
            throw new ROdpsException(e);
        }
    }

    private String formatDateTime(Date date) {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    private DataFrameItem createSingleValueFrame(String name, String type,
            Object obj) {
        DataFrameItem item = new DataFrameItem(name, type);
        if (obj instanceof List) {
            item.setData((List) obj);
        } else {
            item.getData().add(obj);
        }
        return item;
    }

    /**
     * @throws ROdpsException
     * @throws CloneNotSupportedException
     * @throws ClientException
     * @throws ODPSConsoleException
     * @throws ODPSException
     *             删除一个表
     * @Title: DropTable
     * @Description: TODO
     * @param projectName
     * @param tableName
     * @return
     * @return boolean
     * @throws
     */
    public boolean dropTable(String projectName, String tableName)
            throws ROdpsException {
        try {
            this.runSqlTask("drop table "
                    + getTableName(this.getProjectName(projectName), tableName));
            return true;
        } catch (Exception e) {
            LOG.error(e);
            throw new ROdpsException(e);
        }
    }
    private String getDescText(String projectName, String tableName,String partition)throws ROdpsException {
    	if(partition!=null){
        	partition=formatPartition(partition, "'", ",");
        }
        String sql = "desc "+getTableName(getProjectName(projectName), tableName);
        if(partition!=null && !partition.isEmpty()){
        	sql += " partition("+partition+")";
        }
        System.out.println("sql:"+sql);
        List<String> ret = this.runSqlTask(sql);
        if (ret == null) {
            throw new ROdpsException("Desc result is null");
        }
        return ret.get(1);
    }
    /**
     * @throws ROdpsException
     * @throws CloneNotSupportedException
     * @throws ClientException
     * @throws ODPSConsoleException
     * @throws ODPSException
     *             取表大小
     * @Title: getTableSize
     * @Description: TODO
     * @param tableName
     * @return
     * @return int
     * @throws
     */
    public long getTableSize(String projectName, String tableName,String partition)
            throws ROdpsException {    	
        String desc = getDescText(projectName,tableName,partition);
        if(!isInternalTable(desc)){
        	return 0;
        }
        return findSize(desc);
    }
    private long findSize(String descText)throws ROdpsException{
    	Pattern pattern = Pattern.compile("\\| (Size|PartitionSize):\\s*(\\d+)\\s");
        Matcher matcher = pattern.matcher(descText);
        if (matcher.find()) {
            try {
                return Long.valueOf(matcher.group(2));
            } catch (NumberFormatException e) {
                throw new ROdpsException(
                        "table size is larger than Long, please check it.");
            }
        } else {
            throw new ROdpsException("size not found in desc");
        }
    }
    
    private boolean isInternalTable(String descText){
    	return descText.indexOf("VirtualView  : YES")<0;
    }
    /**
     * @throws ROdpsException
     * @throws ODPSException
     * @throws ClientException
     * @throws ClientException
     * @throws ODPSException
     *             表是否存在
     * @Title: isTableExist
     * @Description: TODO
     * @param tableName
     * @return
     * @return boolean
     * @throws
     */
    public boolean isTableExist(String projectName, String tableName, String partition)
            throws ROdpsException {
    	if(partition!=null){
        	partition=formatPartition(partition, "'", ",");
        }
        //Table table = new Table(this.getProjectObject(projectName), tableName);
    	//Table table = odps.tables().get(projectName,tableName);
    	Table table = odps.tables().get(this.getProjectName(projectName),tableName);
        try {
        	if(partition==null || partition.isEmpty()){
	            table.reload();
	            return true;
        	}else{
        		//List<String> pts = table.listPartitions(); 
        		//return pts!=null && pts.size()>0 && pts.contains(partition);
        	    return table.hasPartition(new PartitionSpec(partition));
        	}
        } catch (OdpsException e) {
            if (e.getMessage().indexOf("Table not found") > 0) {
                return false;
            }
            LOG.error(e);
            throw new ROdpsException(e);
        }
    }

    /**
     * 以Json字符串格式取得一个指定Table的Schema
     * 
     * @Title: getTableSchemaJson
     * @Description: TODO
     * @param projectName
     * @param tableName
     * 
     */
    public String getTableSchemaJson(String projectName, String tableName) {
        String tableSchemaJson;
        //Table table = new Table(this.getProjectObject(projectName), tableName);
        Table table = odps.tables().get(this.getProjectName(projectName), tableName);
        
        try {
            table.reload();
            tableSchemaJson = table.getJsonSchema();
        } catch (OdpsException e) {
            tableSchemaJson = e.getMessage();
        }
        return tableSchemaJson;
    }

    /**
     * 通过列名获取该列在Table Schema中的Index
     * 
     * @Title: getIndexFromColName
     * @Description: TODO
     * @param colName
     * @param tableSchemaJson
     * 
     */
    public int getIndexFromColName(String colName, String tableSchemaJson) {
        if (0 >= tableSchemaJson.length()) {
            return -1;
        }
        try {
            JSONObject schema = new JSONObject(tableSchemaJson);
            JSONArray columns = schema.getJSONArray("columns");
            for (int i = 0; i < columns.length(); ++i) {
                JSONObject column = (JSONObject) columns.get(i);
                String columnName = (String) column.get("name");
                if (colName.equals(columnName)) {
                    return i + 1;
                }
            }
            return -1;
        } catch (JSONException e) {
            return -1;
        }
    }

    /**
     * @throws ROdpsException
     * @throws CloneNotSupportedException
     * @throws ClientException
     * @throws ODPSConsoleException
     * @throws ODPSException
     *             执行一个SQL TASK
     * @Title: runSqlTask
     * @Description: TODO
     * @param sql
     * @param limit
     * @return
     * @return List<DataFrameItem>
     * @throws
     */
    public List<String> runSqlTask(String sql) throws ROdpsException {
        try {
            console.parse(sql);
            console.run();
            List<String> ret = console.getResult();
            return ret;
        } catch (Exception e) {
            LOG.error("runSqlTask error,sql=" + sql, e);
            throw new ROdpsException(e);
        }
    }


    private String getTableName(String projectName, String tableName)
            throws ROdpsException {
        if (tableName == null || tableName.isEmpty()) {
            throw new ROdpsException("tableName is empty");
        }
        return (projectName == null || projectName.isEmpty() ? ""
                : (projectName + ".")) + tableName;
    }

    public class Schema {

        public Schema(String name, String type, String comment) {
            this.name = name;
            this.type = type;
            this.comment = comment;
        }
        private String name;
        private String type;
        private String comment;
        private boolean isPartitionKey;

        public String getName() {

            return name;
        }

        public void setName(String name) {

            this.name = name;
        }

        public String getType() {

            return type;
        }

        public void setType(String type) {

            this.type = type;
        }

        public String getComment() {

            return comment;
        }

        public void setComment(String comment) {

            this.comment = comment;
        }

        public boolean isPartitionKey() {

            return isPartitionKey;
        }

        public void setPartitionKey(boolean isPartitionKey) {
            this.isPartitionKey = isPartitionKey;
        }

        public String toString() {
            return name + "|" + type + "|" + (comment != null ? comment : "");
        }
    }

    /**
     * 
     * @ClassName: DataFrame
     * @Description: TODO select输出对象
     * @author dendi.ywd
     * @date 2015-8-7 17:50:22
     * 
     */
    /**
     * @throws ROdpsException
     * @throws CloneNotSupportedException
     * @throws ODPSConsoleException
     * @throws ClientException
     * @throws ODPSException
     *             读表清单
     * @Title: getTables
     * @Description: TODO
     * @return
     * @return List<String>
     * @throws
     */
    public List<DataFrameItem> getTables(String projectName, String pattern)
            throws ROdpsException {
        DataFrameItem<String> owner = new DataFrameItem<String>("owner",
                "string");
        DataFrameItem<String> tableName = new DataFrameItem<String>(
                "table_name", "string");
        List<DataFrameItem> data = new ArrayList<DataFrameItem>();
        data.add(owner);
        data.add(tableName);
        String sql = "show tables ";
        if (pattern != null && !pattern.isEmpty()) {
            sql += "'" + pattern + "'";
        }
        if (projectName != null && !projectName.isEmpty()) {
            sql += " in " + projectName;
        }
        List<String> ret = this.runSqlTask(sql);
        if (ret == null || ret.size() == 0) {
            return data;
        }
        /*old version*/
        /*String[] tbls = ret.get(0).split("\n");
        for (String t : tbls) {
            String[] items = t.split(":");
            if (items.length != 2) {
                continue;
            }
            owner.getData().add(items[0]);
            tableName.getData().add(items[1]);
        }*/
        /*new version*/
        for(int i=1;i<ret.size();i++){
        	String[] items = ret.get(i).split(":");
        	if(items.length != 2){
        		continue;
        	}
        	owner.getData().add(items[0]);
        	tableName.getData().add(items[1]);        	
        }
        return data;
    }
    public static String formatPartition(String part,String valueDim,String fieldDim) throws ROdpsException{
    	LinkedHashMap<String,String> kv=parsePartition(part);
    	return partitionMap2String(kv,valueDim,fieldDim);
    }
    /**
     * @throws ROdpsException 
     * 解析partition
    * @Title: parsePartition
    * @Description: TODO 
    * @param part
    * @return 
    * @return LinkedHashMap<String,String>     
    * @throws
     */
    private static LinkedHashMap<String,String> parsePartition(String part) throws ROdpsException{
    	LinkedHashMap<String,String> ret=new LinkedHashMap<String,String>(); 
    	String[] pts=part.split(",|/");
    	for(String p:pts){
    		String[] kv=p.split("=");
    		if(kv.length!=2){
    			throw new ROdpsException("Partition expression error:"+part);
    		}
    		if(kv[1].startsWith("'") && kv[1].endsWith("'") 
    				|| kv[1].startsWith("\"") && kv[1].endsWith("\"")){
    			kv[1]=kv[1].substring(1,kv[1].length()-1);
    		}
    		ret.put(kv[0], kv[1]);    		
    	}
    	return ret;
    }
    /**
     * 
    * @Title: partitionMap2String
    * @Description: TODO 
    * @param sepc
    * @param valueDim 值的分隔符
    * @param fieldDim　字段间的分隔符
    * @return 
    * @return String     
    * @throws
     */
    private static String partitionMap2String(Map<String,String> sepc,String valueDim,String fieldDim){
    	StringBuffer ret=new StringBuffer();
    	for(Map.Entry<String, String> entry:sepc.entrySet()){
    		if(ret.length()>0){
    			ret.append(fieldDim);
    		}
    		ret.append(entry.getKey()+"="+valueDim+entry.getValue()+valueDim);
    	}
    	return ret.toString();
    }
	
	/*
	 * *set log path*
	 */
	public boolean setLogPath(String log_path) throws IOException {
		String fileName = ROdps.class.getClassLoader().getResource("log4j.properties").getPath();
		String mode = "loghome";
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			CharArrayWriter  tempStream = new CharArrayWriter();  
			String tempString = null;
			int line = 1;
			while ((tempString = reader.readLine()) != null) {
				if(tempString.contains(mode)&&(!tempString.contains("${"+mode+"}"))){
					tempString = tempString.substring(0,tempString.indexOf('=')+1) + log_path;
				}	
				tempStream.write(tempString);
				tempStream.append(System.getProperty("line.separator"));				
			}
			reader.close();
			FileWriter out = new FileWriter(fileName);
			tempStream.writeTo(out);
	        out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return true;
	}
}
