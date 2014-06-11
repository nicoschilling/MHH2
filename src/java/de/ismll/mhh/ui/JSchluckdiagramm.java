package de.ismll.mhh.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.ui.AbstractVolatileImagePanel;
import de.ismll.ui.JMatrixPanel;
import de.ismll.utilities.Scaler;

public class JSchluckdiagramm extends JMatrixPanel{

	
	@Override
	public void render(Graphics2D g) {
		super.render(g);
		
		int numRows = data.getNumRows();
		int numColumns = data.getNumColumns();
		
		BufferedImage img = new BufferedImage(numColumns, numRows, BufferedImage.TYPE_INT_RGB);
		
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numColumns; j++) {
				
				float value = data.get(i, j);
				value = Scaler.scale(minMatrixValue, maxMatrixValue, 0, 255, value);
				Color c = colors[(int) value];
				
				img.setRGB(j, i, c.getRGB());
			}
		}
		setSize(width, height);
		
		g.drawImage(img, 0, 0, this);
		
		
	}

}
