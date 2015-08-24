/*Copyright 1999-2015 Alibaba Group Holding Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

/**
 * @Title: ROdpsTest.java
 * @Package com.aliyun.odps.rodps.UnitTest
 * @Description: TODO(用一句话描述该文件做什么)
 * @author dendi.ywd
 * @date 2015-8-10 09:11:38
 * @version V1.0  
 */
package com.aliyun.odps.rodps;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.aliyun.odps.OdpsException;
import com.aliyun.odps.rodps.DataTunnel.*;
import com.aliyun.openservices.odps.console.ODPSConsoleException;

/**
 * @Title: ROdpsTest.java
 * @Package com.aliyun.odps.rodps.UnitTest
 * @Description: TODO(添加描述)
 * @author dendi.ywd
 * @date 2015-8-10 09:11:38
 * @version V1.0
 */

public class ROdpsTest extends TestCase {
	static ROdps opt;
	
	static String table = "odps_r_operator";
	static String odps_config_path = ROdpsTest.class.getClassLoader().getResource("odps_config.ini").getPath();;	
	static String file;
	
	static {
		Map<String,String> ret=loadConfig(odps_config_path);
		file = ret.get("sqlite_temp") + table;
		if(ret==null){
			System.exit(-1);
		}
		try {
			opt = new ROdps(ret.get("project_name"), 
					ret.get("access_id"), ret.get("access_key"),
					ret.get("end_point"), ret.get("dt_end_point"), 
					ret.containsKey("user_commands")?ret.get("user_commands"):null,"");
			assertTrue(opt != null);
			List<String> rett = opt
				.runSqlTask("create table if not exists " + table + "(id int) comment 'This is the test table for ROdps'");
			assertTrue(rett != null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testN1GetTablesList() throws 
			ODPSConsoleException, CloneNotSupportedException, ROdpsException {		
		List<DataFrameItem> ret = opt.getTables(null, null);
		this.assertTrue(ret != null && ret.size() == 2
				&& ret.get(0).getData().size() > 10);
		System.out.println("testN1GetTableList:"+ret);
	}
	
	@Test
	public void testN2IsTableExists() throws 
			ROdpsException {
		this.assertTrue(opt.isTableExist(null, table,null));
		this.assertFalse(opt.isTableExist(null, table+"not_exists",null));
	}
	
	@Test
	public void testN3ParsePartition() throws ROdpsException{
		this.assertTrue(opt.formatPartition("pt=1/ds=2", "'", " and ").equals("pt='1' and ds='2'"));
		this.assertTrue(opt.formatPartition("pt=1,ds=2", "'", " and ").equals("pt='1' and ds='2'"));
		this.assertTrue(opt.formatPartition("pt='1',ds='2'", "'", " and ").equals("pt='1' and ds='2'"));
		this.assertTrue(opt.formatPartition("pt=\"1\",ds=\"2\"", "'", " and ").equals("pt='1' and ds='2'"));
		this.assertTrue(opt.formatPartition("pt=\"1\",ds=\"2\"", "", ",").equals("pt=1,ds=2"));
		this.assertTrue(opt.formatPartition("pt='1',ds='2012-01-01 00:11:11'", "", ",").equals("pt=1,ds=2012-01-01 00:11:11"));
		try{
			this.assertTrue(opt.formatPartition("pt='1',ds='a,b'", "", ",").equals("pt=1,ds=2012-01-01 00:11:11"));
			this.assertTrue(false);
		}catch(Exception e){
			this.assertTrue(true);
		}
		try{
			this.assertTrue(opt.formatPartition("pt='1',ds='a=b'", "", ",").equals("pt=1,ds=2012-01-01 00:11:11"));
			this.assertTrue(false);
		}catch(Exception e){
			this.assertTrue(true);
		}
		try{
			this.assertTrue(opt.formatPartition("pt='1',ds=',a=b'", "", ",").equals("pt=1,ds=2012-01-01 00:11:11"));
			this.assertTrue(false);
		}catch(Exception e){
			this.assertTrue(true);
		}
	}
	
	@Test
	public void testN4DescribeTable() throws ODPSConsoleException,
			 CloneNotSupportedException, ROdpsException,OdpsException{
		List<DataFrameItem> ret = opt.describeTable(null, table,null);
		System.out.println("testN4DdescribeTable:"+ret);
		this.assertTrue(ret != null && !ret.isEmpty() && ret.size() == 8);
	}

	@Test
	public void testN5TableSize() throws ODPSConsoleException,
			CloneNotSupportedException, ROdpsException {
		Long ret = opt.getTableSize(null, table,null);
		System.out.println("testN5TableSize:"+ret);
		
	}
	
	@Test
	/*
	 * use tunnel sdk to load table from odps
	 * */
	public void testN6DtLoad() throws ROdpsException{
		String project=null;
		String partition=null;
		String colDelimiter="\u0001";
		String rowDelimiter="\n";
		int limit =863;
		List<List<String>> ret=opt.loadTableFromDT(project, table, partition, 
				file, colDelimiter, rowDelimiter, limit,8);
	}
	
	public void testN7Upload() throws ROdpsException{
		String project=null;
		String partition=null;
		String colDelimiter="\u0001";
		String rowDelimiter="\n";
		opt.writeTableFromDT(project, table, partition,  file, colDelimiter
				, rowDelimiter, 1,8);
	}
	
	@Test
	/*
	 * create table odps_r_operator ,then delete it
	 * **/
	public void testN8RunSqlTask() throws ODPSConsoleException,
			CloneNotSupportedException, ROdpsException {
		List<String> ret = opt
				.runSqlTask("create table if not exists odps_r_operator(id int)");
		this.assertTrue(ret != null);
		
	}

	@AfterClass
	public void DropTable() throws ROdpsException{
		this.assertTrue(opt.dropTable(null, table));
	}
	
	private static Map<String,String> loadConfig(String file){
		try{
			List<String> lines=FileUtils.readLines(new File(file));
			Map<String,String> ret=new HashMap<String,String>();
			for(String line:lines){
				if(line.startsWith("#")){
					continue;
				}
				int idx=line.indexOf("=");
				if(idx<0){
					System.out.println("odps_config.ini error line:"+line);
					continue;
				}
				ret.put(line.substring(0,idx).trim(), line.substring(idx+1,line.length()).trim());
			}
			return ret;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}	
			
}
