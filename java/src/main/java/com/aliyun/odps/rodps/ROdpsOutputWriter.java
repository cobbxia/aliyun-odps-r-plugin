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

import java.util.ArrayList;
import java.util.List;
import com.aliyun.openservices.odps.console.output.DefaultOutputWriter;

/**
 * @Title: ROutputWriter.java
 * @Package com.aliyun.odps.rodps
 * @Description: TODO(添加描述)
 * @author dendi.ywd
 * @date 2015-8-7 17:49:10
 * @version V1.0
 */
public class ROdpsOutputWriter extends DefaultOutputWriter {
	private final List<String> result;
	public ROdpsOutputWriter(){
		result=new ArrayList<String>();
	}
	@Override
	public void writeResult(String str) {
		result.add(str);
	}
	public List<String> getResult() {
		return result;
	}
}
