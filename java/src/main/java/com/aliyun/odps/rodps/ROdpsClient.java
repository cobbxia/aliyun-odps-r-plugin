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

import java.util.List;
import com.aliyun.odps.OdpsException;
import com.aliyun.openservices.odps.console.ODPSConsoleException;
import com.aliyun.openservices.odps.console.ExecutionContext;
import com.aliyun.openservices.odps.console.commands.AbstractCommand;
import com.aliyun.openservices.odps.console.commands.UseProjectCommand;
import com.aliyun.openservices.odps.console.utils.CommandParserUtils;
 
/**   
 * @Title: ODPSClient4R.java    
 * @Package com.aliyun.odps.rodps
 * @Description: TODO(添加描述)   
 * @author    dendi.ywd
 * @date 2015-8-7 17:42:26   
 * @version V1.0   
 */
public class ROdpsClient {
	private ExecutionContext context;
	ThreadLocal<Query> threadQuery;
	public ROdpsClient(String accessId, String accessKey, String endPoint,
			String defaultProject,String userCommands) throws OdpsException, ODPSConsoleException,  OdpsException{
		context=new ExecutionContext();
		ROdpsOutputWriter output=new ROdpsOutputWriter();
		context.setAccessId(accessId);
		context.setAccessKey(accessKey);
		context.setEndpoint(endPoint);
		context.setProjectName(defaultProject);
		context.setMachineReadable(true);
		context.setOutputWriter(output);
		context.setUserCommands(userCommands);
		UseProjectCommand use = new UseProjectCommand(defaultProject,"",context);
		use.run();
		threadQuery=new ThreadLocal<Query>(); 
	}
	public Class parse(String query) throws ODPSConsoleException, OdpsException,  CloneNotSupportedException{
		if(!query.endsWith(";")){
			query+=";";
		}
		threadQuery.remove();
		ExecutionContext ctx=context.clone();
		ctx.setOutputWriter(new ROdpsOutputWriter());
		threadQuery.set(new Query(ctx));
		return threadQuery.get().parse(query);
	}
	public List<String> getResult(){
		return threadQuery.get().getResult();
	}
	public void run() throws OdpsException, ODPSConsoleException, OdpsException{
		threadQuery.get().run();
	}
	public void updateCurrentProject(String projectName){
		this.context.setProjectName(projectName);
	}
	class Query{
		private AbstractCommand command;
		private String query;
		private Class commandClass;
		private ExecutionContext context;
		public Query(ExecutionContext context){
			this.context=context;
		}
		public Class parse(String query) throws ODPSConsoleException{
			command = CommandParserUtils.parseCommand(query, context);
			this.commandClass=command.getClass();
			return this.commandClass;
		}
		public List<String> getResult(){
			ROdpsOutputWriter output=(ROdpsOutputWriter) context.getOutputWriter();
			return output.getResult();
		}
		public void run() throws OdpsException, ODPSConsoleException, OdpsException{
			command.run();
		}
	}
}
    
     