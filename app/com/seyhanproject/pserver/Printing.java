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

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javax.print.PrintService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Printing {

	private final static Logger log = LoggerFactory.getLogger(Printing.class);
	
	public static void stopQueues() {
		Server.stop();
	}

	public static void send(Document doc) {
		if (doc.isLocal) {
			print(doc);
		} else {
			Server.send(doc);
		}
	}

	public static String print(Document doc) {
		String result = null;
		List<String> rows = doc.rows;
		
		StringBuilder pathSB = new StringBuilder();

		switch (doc.targetType) {
			case Document.FILE:
			case Document.DOT_MATRIX: {
				if (doc.targetType == Document.FILE) {
					pathSB.append(trimLastSlash(doc.path));
					pathSB.append(File.separator);
					pathSB.append(doc.right);
					pathSB.append("_");
					pathSB.append(doc.id);
					pathSB.append(".txt");
				} else {
					pathSB.append(doc.path);
				}
				try {
					PrintWriter out = new PrintWriter(pathSB.toString());
					if (doc.targetType == Document.DOT_MATRIX && doc.isCompressed) out.print("\u0014");
					for (int i = 0; i < rows.size(); i++) {
						if (doc.targetType == Document.DOT_MATRIX) {
							String newline = Normalizer.normalize(rows.get(i), Normalizer.Form.NFD).replaceAll("\\p{Mn}", "");
							newline = newline.replace('ı', 'i');
							out.println(newline);
						} else {
							out.println(rows.get(i));
						}
					}
					out.close();
					result = pathSB.toString();
				} catch (Exception e) {
					log.error("ERROR", e);
				}
				break;
			}
			case Document.LASER: {
				PrinterJob job = PrinterJob.getPrinterJob();
				Book book = new Book();

				PageFormat format = getMinimumMarginPageFormat(job);
				if (doc.viewType == Document.PORTRAIT) {
					format.setOrientation(PageFormat.PORTRAIT);
			    } else {
			    	format.setOrientation(PageFormat.LANDSCAPE);
				}
				
				//ozel olarak bir printer belirtilmisse ona gonderilir!!!
				if (doc.path != null && ! doc.path.trim().isEmpty()) {
					PrintService printService = null;
					PrintService[] printServices = PrinterJob.lookupPrintServices();
					for (PrintService ps : printServices) {
						if (ps.getName().indexOf(doc.path) > 0) {
							printService = ps;
							break;
						}
					}
					if (printService != null) {
						try {
							job.setPrintService(printService);
							result = pathSB.toString();
						} catch (Exception e) {
							log.error("ERROR", e);
						}
					}
				} else {
					result = "default_printer";
				}

				List<List<String>> chopedLineList = chopped(rows, doc.pageRows);
				for (List<String> list : chopedLineList) {
					book.append(new PrintablePage(list, doc.isCompressed), format);
				}
				job.setPageable(book);

				try {
					job.print();
				} catch (Exception e) {
					log.error("ERROR", e);
				}

				break;
			}
		}

		return result;
	}

	private static PageFormat getMinimumMarginPageFormat(PrinterJob printJob) {
	    PageFormat pf0 = printJob.defaultPage();
	    PageFormat pf1 = (PageFormat) pf0.clone();
	    Paper p = pf0.getPaper();
	    p.setImageableArea(0, 0,pf0.getWidth(), pf0.getHeight());
	    pf1.setPaper(p);
	    PageFormat pf2 = printJob.validatePage(pf1);
		return pf2;     
	}

	private static <T> List<List<T>> chopped(List<T> list, final int L) {
	    List<List<T>> parts = new ArrayList<List<T>>();
	    final int N = list.size();
	    for (int i = 0; i < N; i += L) {
	        parts.add(new ArrayList<T>(
	            list.subList(i, Math.min(N, i + L)))
	        );
	    }
	    return parts;
	}

	private static String trimLastSlash(String path) {
		if (path != null && path.endsWith("/") || path.endsWith("\\")) {
			return path.substring(0, path.length() - 1);
		}
		return path;
	}

}
