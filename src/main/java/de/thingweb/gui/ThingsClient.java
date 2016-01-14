/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Siemens AG and the thingweb community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.thingweb.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import de.thingweb.client.Client;
import de.thingweb.client.ClientFactory;

/**
 *
 */
public class ThingsClient extends JFrame {

	private static final long serialVersionUID = 479681876826299109L;
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private JFileChooser fileChooser;

	private ClientFactory clientFactory;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            command-line args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ThingsClient frame = new ThingsClient();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	JFileChooser getJFileChooser() {
		if (this.fileChooser == null) {
			fileChooser = new JFileChooser();
		}
		return fileChooser;
	}

	ClientFactory getClientFactory() {
		if (this.clientFactory == null) {
			clientFactory = new ClientFactory();
		}
		return clientFactory;
	}

	void addThingPanelFile(String fname) {
		try {
			Client client = getClientFactory().getClientFile(fname);
			addThingPanel(client, fname);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Could not create panel for file '" + fname + "': " + e.getMessage(),
					"File Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	void addThingPanelUrl(String uri) {
		try {
			Client client = getClientFactory().getClientUrl(new URI(uri));
			addThingPanel(client, uri.toString());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Could not create panel for URI '" + uri + "': " + e.getMessage(),
					"URI Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@SuppressWarnings("serial")
	class URILabel extends JLabel {
		public URILabel(String sURI) {
			this.setText("<html><a href=\"" + sURI + "\">" + sURI + "</a></html>");
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			this.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() > 0) {
						if (Desktop.isDesktopSupported()) {
							Desktop desktop = Desktop.getDesktop();
							try {
								URI uri = new URI(sURI);
								desktop.browse(uri);
							} catch (IOException ex) {
								ex.printStackTrace();
							} catch (URISyntaxException ex) {
								ex.printStackTrace();
							}
						}
					}
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}

			});
		}
	}

	void addThingPanelInfo() {
		JPanel p = new JPanel();
		p.add(new JLabel(
				"<html>This <i>things client</i> allows to load thing descriptions following the rules specified in</html>"));
		final String sURITutorial = "https://github.com/w3c/wot/blob/master/TF-TD/Tutorial.md";

		p.add(new URILabel(sURITutorial));
		p.add(new JLabel("<html>. Examples can be found here: </html>"));
		final String sURIExamples = "https://github.com/w3c/wot/tree/master/TF-TD/TD%20Samples";
		p.add(new URILabel(sURIExamples));
		p.add(new JLabel("<html>.</html>"));

		tabbedPane.addTab("How to use", null, p);
	}

	void addThingPanel(Client cl, String tip) throws FileNotFoundException, IOException {
		JPanel panelLed = new ThingPanelUI(cl);

		JScrollPane sp = new JScrollPane(panelLed);

		String tabTitle = cl.getMetadata().getName();
		tabbedPane.addTab(tabTitle, null, sp, tip);
		tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(sp), getTitlePanel(tabbedPane, sp, tabTitle));
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
	}

	static JPanel getTitlePanel(final JTabbedPane tabbedPane, final JComponent comp, String title) {
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titlePanel.setOpaque(false);
		JLabel titleLbl = new JLabel(title);
		titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		titlePanel.add(titleLbl);
		JButton closeButton = new JButton("x");
		closeButton.setBorderPainted(false);
		// closeButton.setFocusPainted(false);
		closeButton.setContentAreaFilled(false);

		closeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int dialogResult = JOptionPane.showConfirmDialog(null,
						"Would you like to close th tab '" + title + "'?", "Close", JOptionPane.YES_NO_OPTION);
				if (dialogResult == JOptionPane.YES_OPTION) {
					tabbedPane.remove(comp);
				}
			}
		});
		titlePanel.add(closeButton);

		return titlePanel;
	}

	@SuppressWarnings("unchecked")
	protected void doDragAndDropFile(final JButton button) {
		button.setDropTarget(new DropTarget() {
			private static final long serialVersionUID = 1L;

			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					List<File> droppedFiles = (List<File>) evt.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);

					for (File f : droppedFiles) {
						// process file(s)
						addThingPanelFile(f.getAbsolutePath());
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null,
							"Errors while creating panels for dropped files: " + ex.getMessage(), "Drop Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	// protected void doDragAndDropText(final JButton button) {
	// button.setDropTarget(new DropTarget() {
	// private static final long serialVersionUID = 1L;
	//
	// public synchronized void drop(DropTargetDropEvent evt) {
	// try {
	// evt.acceptDrop(DnDConstants.ACTION_COPY);
	// InputStream is = (InputStream) evt.getTransferable().getTransferData(
	// DataFlavor.getTextPlainUnicodeFlavor());
	//
	//
	// BufferedReader br = new BufferedReader(new InputStreamReader(is));
	// String line;
	// StringBuilder sb = new StringBuilder();
	// while ((line = br.readLine()) != null) {
	// sb.append(line);
	// }
	// System.out.println(sb);
	// } catch (Exception ex) {
	// JOptionPane.showMessageDialog(null,
	// "Errors while creating panels for dropped content: " + ex.getMessage(),
	// "Drop Error",
	// JOptionPane.ERROR_MESSAGE);
	// }
	// }
	// });
	// }

	/**
	 * Create the frame.
	 */
	public ThingsClient() {
		// try to use system look and feel (if possible)
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException
				| ClassNotFoundException e1) {
		}

		setTitle("ThingsClient");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		// for now do not load any example instead load info panel
		addThingPanelInfo();

		// // load led example (local)
		// String jsonld = "jsonld" + File.separator + "led.jsonld";
		// this.addThingPanelFile(jsonld, "LED (local)");

		JPanel panel = new JPanel();
		panel.setBorder(
				new TitledBorder(null, "Add more \"Things\" ... ", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		contentPane.add(panel, BorderLayout.SOUTH);

		JButton btnAddJSONLDFile = new JButton("Add JSON-LD File");
		btnAddJSONLDFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (JFileChooser.APPROVE_OPTION == getJFileChooser().showOpenDialog(null)) {
					File f = getJFileChooser().getSelectedFile();
					try {
						addThingPanelFile(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		doDragAndDropFile(btnAddJSONLDFile);
		panel.add(btnAddJSONLDFile);

		JButton btnAddJSONLDURI = new JButton("Add JSON-LD URI");
		btnAddJSONLDURI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Create the JOptionPane.
				String url = JOptionPane.showInputDialog("URI");
				if (url != null) {
					// javas URL class can't yet handle coap
					// see java.net.MalformedURLException: unknown protocol:
					// coap
					// URL url = new URL(msg);
					// int ip = url.lastIndexOf("/");
					// String tabTitle = ip > 0 ? url.substring(ip) : "msg";
					addThingPanelUrl(url);
				}
			}
		});
		// doDragAndDropText(btnAddJSONLDURI);
		panel.add(btnAddJSONLDURI);

		JButton btnDiscoverTD = new JButton("Discover ThingDescriptions");
		btnDiscoverTD.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				final JDialog frame = new JDialog(ThingsClient.this, "Discover Things", true);
				frame.getContentPane().add(new DiscoverPanel(ThingsClient.this));
				frame.pack();
				frame.setVisible(true);
			}
		});
		panel.add(btnDiscoverTD);
	}

}
