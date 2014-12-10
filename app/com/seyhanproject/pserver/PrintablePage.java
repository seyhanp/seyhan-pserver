package com.seyhanproject.pserver;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.List;

class PrintablePage implements Printable {
	
	private List<String> lines;
	private boolean isCompressed;
	
	PrintablePage(List<String> lines, boolean isCompressed) {
		this.lines = lines;
		this.isCompressed = isCompressed;
	}

	public int print(Graphics g, PageFormat format, int page) {
		// --- Create the Graphics2D object
		Graphics2D g2d = (Graphics2D) g;

		Font font = new Font("Courier New", Font.PLAIN, (isCompressed ? 8 : 10));
		FontMetrics fm = g2d.getFontMetrics(font);
		int linespacing = (int) (font.getSize() * 1.1f);
		int baseline = fm.getAscent();

		// --- Translate the origin to 0,0 for the top left corner
		g2d.translate(format.getImageableX(), format.getImageableY());

		// --- Set the drawing color to black
		g2d.setPaint(Color.black);

	    // Set the font and the color we will be drawing with.
		// Note that you cannot assume that black is the default color!
		g2d.setFont(font);
		g2d.setColor(Color.black);			

		int x0 = (int) format.getImageableX();
		int y0 = (int) format.getImageableY() + baseline;

		// Loop through the lines, drawing them all to the page.
		for (String line : lines) {
			if (line.length() > 0) g2d.drawString(line, x0, y0);
		    y0 += linespacing;
		}
		
		return PAGE_EXISTS;
	}
}
