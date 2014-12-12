/**
* Copyright (c) 2015 Mustafa DUMLUPINAR, mdumlupinar@gmail.com
*
* This file is part of seyhan project.
*
* seyhan is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.seyhanproject.pserver;

import java.io.Serializable;
import java.util.List;

public class Document implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final int FILE = 0;
	public static final int DOT_MATRIX = 1;
	public static final int LASER = 2;

	public static final int PORTRAIT = 0;
	public static final int LANDSCAPE = 1;

	public String brokerIp;
	public String userIp;

	public String username;
	public String workspace;
	public String right;
	public Integer id;

	public List<String> rows;
	public String targetName;
	public int targetType;
	public int viewType;
	
	public boolean isLocal;
	public int pageRows;
	public String path;
	public boolean isCompressed;

}
