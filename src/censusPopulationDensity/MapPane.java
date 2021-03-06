package censusPopulationDensity;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;
import javax.swing.border.EtchedBorder;
import javax.swing.JFrame;
import javax.swing.JPanel;
@SuppressWarnings("serial")
public class MapPane extends JPanel implements Observer{
	private final double MAX_WEST = -173.033005;
	private final double MAX_EAST = -65.300858;
	private final double MAX_SOUTH1 = 17.941346;	
	private final double MAX_SOUTH = mercatorConversion(MAX_SOUTH1);
	private final double MAX_NORTH1 = 71.300949;
	private final double MAX_NORTH = mercatorConversion(MAX_NORTH1);
	private final double CONT_MAX_WEST = -126;
	private final double CONT_MAX_EAST = -66;
	private final double CONT_MAX_SOUTH1 = 24;
	private final double CONT_MAX_SOUTH = mercatorConversion(CONT_MAX_SOUTH1);
	private final double CONT_MAX_NORTH1 = 50;
	private final double CONT_MAX_NORTH = mercatorConversion(CONT_MAX_NORTH1);
	private final double H_ZOOM_FACTOR = (MAX_NORTH - MAX_SOUTH) / (CONT_MAX_NORTH - CONT_MAX_SOUTH);
	private final double W_ZOOM_FACTOR = (MAX_EAST - MAX_WEST) / (CONT_MAX_EAST - CONT_MAX_WEST);
	private final double DOWN_SHIFT = (MAX_NORTH - CONT_MAX_NORTH) / (MAX_NORTH - MAX_SOUTH);
	private final double RIGHT_SHIFT = (MAX_WEST - CONT_MAX_WEST) / (MAX_WEST - MAX_EAST);
	private double mercatorConversion(double lat){
		double latpi = lat * Math.PI / 180;
		double x = Math.log(Math.tan(latpi) + 1 / Math.cos(latpi));
		return x;
	}
	
	private int rows, columns;
	private Image wholeUSImage, contUSImage, currentImage;
	private double sStartColumn, sStartRow;
	private double sEndColumn, sEndRow;
	private boolean selecting, zoomed;
	
	public int getRows(){
		return rows; 
	}
	public int getColumns(){
		return columns;
	}
	public int getNorth(){
		return rows - (int)Math.min(sStartRow, sEndRow);
	}
	public int getSouth(){
		return rows - (int)Math.max(sStartRow, sEndRow);
	}
	public int getEast(){
		return (int)Math.max(sStartColumn, sEndColumn) + 1;
	}
	public int getWest(){
		return (int)Math.min(sStartColumn, sEndColumn) + 1;
	}
	public void zoom(){
		zoomed = true;
		currentImage = contUSImage;
		repaint();
	}
	public void unzoom(){
		zoomed = false;
		currentImage = wholeUSImage;
		repaint();
	}
	public void paint(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		int height = this.getHeight();
		int width = this.getWidth();
		double rHeight = 1.0 * height / rows;
		double rWidth = 1.0 * width / columns;
		g2.drawImage(currentImage, 1, 1, width, height, null);
		if(zoomed){
			rHeight *= H_ZOOM_FACTOR;
			rWidth *= W_ZOOM_FACTOR;
			int startX = -(int)(width * W_ZOOM_FACTOR * RIGHT_SHIFT);
			int startY = -(int)(height * H_ZOOM_FACTOR * DOWN_SHIFT);
			for(int x = 0; x < columns; x++){
				for(int y = 0; y < rows; y++){
					double rX = x * rWidth + startX;
					double rY = y * rHeight + startY;
					if((rX + rWidth > 0 && rY + rHeight > 0) ||
							(rX < width && rY < height)){
						Rectangle2D r = new Rectangle2D.Double(rX, rY, rWidth, rHeight);
						g2.draw(r);
					}					
				}
			}
		} else {
			for(int x = 0; x < columns; x++){
				for(int y = 0; y < rows; y++){
					Rectangle2D r = new Rectangle2D.Double(x * rWidth, y * rHeight, rWidth, rHeight);
					g2.draw(r);
				}
			}
		}
		if(selecting){
			int trueStartColumn = (int)Math.min(sStartColumn, sEndColumn);
			int trueStartRow = (int)Math.min(sStartRow, sEndRow);
			int trueEndColumn = (int)Math.max(sStartColumn, sEndColumn);
			int trueEndRow = (int)Math.max(sStartRow, sEndRow);
			int trueHeight = trueEndRow - trueStartRow + 1;
			int trueWidth = trueEndColumn - trueStartColumn + 1;
			double recX = trueStartColumn * rWidth;
			double recY = trueStartRow * rHeight;
			double recWidth = trueWidth * rWidth;
			double recHeight = trueHeight * rHeight;
			if(zoomed){
				int startX = -(int)(width * W_ZOOM_FACTOR * RIGHT_SHIFT);
				int startY = -(int)(height * H_ZOOM_FACTOR * DOWN_SHIFT);
				recX += startX;
				recY += startY;

			}
			Rectangle2D r = new Rectangle2D.Double(recX, recY, recWidth, recHeight);
			Color shade = Color.YELLOW;
			g2.setColor(shade);
			g2.setColor(new Color(shade.getRed(), shade.getGreen(), shade.getBlue(), 120));
			g2.fill(r);
			g2.setColor(Color.BLACK);
		}
		
		selecting = false;

	}
	
	public MapPane(final JFrame appFrame){
		super(new BorderLayout(0,1));
		this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		selecting = false;
		zoomed = false;
		MediaTracker mt = new MediaTracker(this);
		wholeUSImage = Toolkit.getDefaultToolkit().getImage("USMap.jpg");
		contUSImage = Toolkit.getDefaultToolkit().getImage("contUSmap.jpg");
		currentImage = wholeUSImage;
		mt.addImage(wholeUSImage, 0);
		mt.addImage(contUSImage, 1);
		try{
			mt.waitForAll();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		this.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) { }
			public void mouseEntered(MouseEvent e) { }

			public void mouseExited(MouseEvent e) {
				if(selecting){
					Component mapPane = e.getComponent();
					int w = mapPane.getWidth();
					int h = mapPane.getHeight();
					int x = e.getX();
					int y = e.getY();
					if(zoomed){
						w *= W_ZOOM_FACTOR;
						h *= H_ZOOM_FACTOR;
						x = (int)(x + w * RIGHT_SHIFT);
						y = (int)(y + h * DOWN_SHIFT);
					}
					sEndColumn = Math.min(Math.max(x / (1.0 * w / columns), 0), columns - 1);
					sEndRow = Math.min(Math.max(y / (1.0 * h / rows), 0), rows - 1);
					mapPane.repaint();
				}
			}
        public void mousePressed(MouseEvent e) {
				Component mapPane = e.getComponent();
				int w = mapPane.getWidth();
				int h = mapPane.getHeight();
				int x = e.getX();
				int y = e.getY();
				if(zoomed){
					w *= W_ZOOM_FACTOR;
					h *= H_ZOOM_FACTOR;
					x = (int)(x + w * RIGHT_SHIFT);
					y = (int)(y + h * DOWN_SHIFT);
				}
				selecting = true;
				sStartColumn = x / (1.0 * w / columns);
				sStartRow = y / (1.0 * h / rows);

			}

			public void mouseReleased(MouseEvent e) {
				if(selecting){
					Component mapPane = e.getComponent();
					int w = mapPane.getWidth();
					int h = mapPane.getHeight();
					int x = e.getX();
					int y = e.getY();
					if(zoomed){
						w *= W_ZOOM_FACTOR;
						h *= H_ZOOM_FACTOR;
						x = (int)(x + w * RIGHT_SHIFT);
						y = (int)(y + h * DOWN_SHIFT);
					}
					sEndColumn = x / (1.0 * w / columns);
					sEndRow = y / (1.0 * h / rows);
					mapPane.repaint();
				}
			}
		});
	}

	public void update(Observable o, Object arg) {
		if(o instanceof InteractionPane.MapGrid){
			InteractionPane.MapGrid mg = (InteractionPane.MapGrid)o;
			rows = mg.getRows();
			columns = mg.getColumns();
			this.repaint();
		}
	}
}
