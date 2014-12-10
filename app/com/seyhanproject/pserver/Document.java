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
