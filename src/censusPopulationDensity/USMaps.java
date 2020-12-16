package censusPopulationDensity;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
public class USMaps{
	enum Version { ONE, TWO, THREE, FOUR, FIVE };
	static Version running = Version.ONE;
	private static MapPane mapPane;
	private static InteractionPane interactionPane;
	private static JFrame appFrame;
	private static final String FILENAME = "C:\\Users\\lella\\Downloads\\populationQuery\\CenPop2010.txt";
	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.exit(1);
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable() {			
			public void run() {
				int initX = 100; 
				int initY = 500; 
				appFrame = new JFrame("USA Population Density");
				appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				appFrame.setSize(screen.width * 7 / 8, screen.height * 7 / 8);
				appFrame.setLocation(screen.width / 16, screen.height / 16 - 20);
				appFrame.setVisible(true);
                JMenuBar menuToolbar = createToolbar();
				appFrame.setJMenuBar(menuToolbar);
				Container mainContentPane = appFrame.getContentPane();
				BoxLayout mainLayout = new BoxLayout(mainContentPane, BoxLayout.Y_AXIS);
				mainContentPane.setLayout(mainLayout);
				mapPane = new MapPane(appFrame);
				appFrame.add(mapPane);
				interactionPane = new InteractionPane(appFrame);
				interactionPane.initMapGrid(initY, initX, mapPane);
				appFrame.add(interactionPane, BorderLayout.SOUTH);
				appFrame.validate();
			}

			private JMenuBar createToolbar() {
				JMenuBar toolbar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
			     JMenuItem exitItem = new JMenuItem("Exit");
				exitItem.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				});
				fileMenu.add(exitItem);
				JMenu runMenu = new JMenu("Run");
                JMenu changeRunSubMenu = new JMenu("Change Run");
				JMenu zoomMenu = new JMenu("Zoom");
				final JMenuItem changeToV1 = new JMenuItem("V.1");
				changeToV1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, 
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
				changeToV1.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						running = Version.ONE;
						interactionPane.deselectAllButtons();
						interactionPane.selectButton(1);
					}
				});
				final JMenuItem changeToV2 = new JMenuItem("V.2");
				changeToV2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, 
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
				changeToV2.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						running = Version.TWO;
						interactionPane.deselectAllButtons();
						interactionPane.selectButton(2);
					}
				});
				final JMenuItem changeToV3 = new JMenuItem("V.3");
				changeToV3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, 
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
				changeToV3.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						running = Version.THREE;
						interactionPane.deselectAllButtons();
						interactionPane.selectButton(3);
					}
				});
				final JMenuItem changeToV4 = new JMenuItem("V.4");
				changeToV4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, 
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
				changeToV4.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						running = Version.FOUR;
						interactionPane.deselectAllButtons();
						interactionPane.selectButton(4);
					}
				});
				final JMenuItem changeToV5 = new JMenuItem("V.5");
				changeToV5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, 
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
				changeToV5.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						running = Version.FIVE;
						interactionPane.deselectAllButtons();
						interactionPane.selectButton(5);
					}
				});
				final JMenuItem runItem = new JMenuItem("Run");
				runItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, 
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
				runItem.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						try {
							runProgram(appFrame);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				});
				final JMenuItem noZoom = new JMenuItem("None");
				noZoom.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent arg0) {
						mapPane.unzoom();
					}					
				});
				final JMenuItem zoom = new JMenuItem("Continental U.S.");
				zoom.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent arg0) {
						mapPane.zoom();
					}					
				});
				changeRunSubMenu.add(changeToV1);
				changeRunSubMenu.add(changeToV2);
				changeRunSubMenu.add(changeToV3);
				changeRunSubMenu.add(changeToV4);
				changeRunSubMenu.add(changeToV5);
				runMenu.add(changeRunSubMenu);
				runMenu.addSeparator();
				runMenu.add(runItem);
				zoomMenu.add(noZoom);
				zoomMenu.add(zoom);
                toolbar.add(fileMenu);
				toolbar.add(runMenu);
				toolbar.add(zoomMenu);
				
				return toolbar;
			}
		});
	}
	
	static void runProgram(Component parent) throws IOException{
		int w = mapPane.getWest();
		int s = mapPane.getSouth();
		int e = mapPane.getEast();
		int n = mapPane.getNorth();
		System.out.println(w + ", " + s + ", " + e + ", " + n);
		Pair<Integer,Float> result = PopulationQuery.singleInteraction(FILENAME, 
				mapPane.getColumns(), mapPane.getRows(), getVersionNum(), w, s, e, n);
		InteractionPane.displayCensusData(result.getElementA(), result.getElementB());
	}
	
	public static String getVersionNum(){
		switch(running){
		case ONE: return "-v1";
		case TWO: return "-v2";
		case THREE: return "-v3";
		case FOUR: return "-v4";
		case FIVE: return "-v5";
		default: return "X";
		}
	}
}
