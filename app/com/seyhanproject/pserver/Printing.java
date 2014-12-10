package com.seyhanproject.pserver;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.PrintWriter;
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
						out.println(rows.get(i));
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
