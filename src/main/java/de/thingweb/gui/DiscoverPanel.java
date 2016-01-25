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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTextUI;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;

import de.thingweb.client.Client;
import de.thingweb.desc.DescriptionParser;
import de.thingweb.desc.pojo.Protocol;
import de.thingweb.desc.pojo.ThingDescription;
import de.thingweb.discovery.TDRepository;
import de.thingweb.gui.text.HintTextFieldUI;

public class DiscoverPanel extends JPanel {

	final ThingsClient thingsClient;
	private JFileChooser fileChooser;

	final List<TDCheckBox> tdSearches = new ArrayList<>();
	final JPanel searchPanel;

	class TDCheckBox extends JCheckBox {

		private static final long serialVersionUID = -5188829331062093048L;

		final String key;
		final ThingDescription td;

		public TDCheckBox(String key, ThingDescription td) {
			this.key = key;
			this.td = td;

			String htmlLabel = "<html>" + td.getMetadata().getName() + " (" + key + ")<br />"
					+ "<span style='color:gray'>";
			Map<String, Protocol> protocols = td.getMetadata().getProtocols();
			boolean first = true;
			for (String k : protocols.keySet()) {
				if (first) {
					first = false;
				} else {
					htmlLabel += ",";
				}
				Protocol p = protocols.get(k);
				htmlLabel += k + ":" + p.getUri();
			}
			htmlLabel += "</span></html>";
			this.setText(htmlLabel);
		}
	}

	public DiscoverPanel(ThingsClient thingsClient) {
		this.thingsClient = thingsClient;
		setBorder(new TitledBorder(null, "Discovery options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 147, 86, 65, 0 };
		gridBagLayout.rowHeights = new int[] { 23, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblNewLabel = new JLabel("Repository URI/IP (:Port):");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);

		textFieldIP = new JTextField();
		textFieldIP.setText(TDRepository.ETH_URI);
		GridBagConstraints gbc_textFieldIP = new GridBagConstraints();
		gbc_textFieldIP.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldIP.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldIP.gridx = 1;
		gbc_textFieldIP.gridy = 0;
		add(textFieldIP, gbc_textFieldIP);
		textFieldIP.setColumns(80);

		JLabel lblFreeSearchText = new JLabel("Free search text:");
		GridBagConstraints gbc_lblFreeSearchText = new GridBagConstraints();
		gbc_lblFreeSearchText.anchor = GridBagConstraints.EAST;
		gbc_lblFreeSearchText.insets = new Insets(0, 0, 5, 5);
		gbc_lblFreeSearchText.gridx = 0;
		gbc_lblFreeSearchText.gridy = 1;
		add(lblFreeSearchText, gbc_lblFreeSearchText);

		textFieldFreeText = new JTextField();
		textFieldFreeText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireFreeSearch();
			}
		});

		GridBagConstraints gbc_textFieldFreeText = new GridBagConstraints();
		gbc_textFieldFreeText.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldFreeText.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldFreeText.gridx = 1;
		gbc_textFieldFreeText.gridy = 1;
		add(textFieldFreeText, gbc_textFieldFreeText);
		textFieldFreeText.setColumns(80);

		JButton btnFreeSearch = new JButton("Start \"free search\"");
		GridBagConstraints gbc_btnFreeSearch = new GridBagConstraints();
		gbc_btnFreeSearch.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFreeSearch.insets = new Insets(0, 0, 5, 0);
		gbc_btnFreeSearch.gridx = 2;
		gbc_btnFreeSearch.gridy = 1;
		add(btnFreeSearch, gbc_btnFreeSearch);

		JLabel lblTripleSearch = new JLabel("Triple Search:");
		GridBagConstraints gbc_lblTripleSearch = new GridBagConstraints();
		gbc_lblTripleSearch.anchor = GridBagConstraints.EAST;
		gbc_lblTripleSearch.insets = new Insets(0, 0, 5, 5);
		gbc_lblTripleSearch.gridx = 0;
		gbc_lblTripleSearch.gridy = 2;
		add(lblTripleSearch, gbc_lblTripleSearch);

		textFieldTripleSearch = new JTextField();
		BasicTextUI textFieldUITripleSearch = new HintTextFieldUI(" " + "e.g., ?s ?p ?o", true, Color.GRAY);
		textFieldTripleSearch.setUI(textFieldUITripleSearch);
		textFieldTripleSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireTripleSearch();
			}
		});
		GridBagConstraints gbc_textFieldTripleSearch = new GridBagConstraints();
		gbc_textFieldTripleSearch.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldTripleSearch.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldTripleSearch.gridx = 1;
		gbc_textFieldTripleSearch.gridy = 2;
		add(textFieldTripleSearch, gbc_textFieldTripleSearch);
		textFieldTripleSearch.setColumns(80);

		JButton btnTripleSearch = new JButton("Start \"triple search\"\r\n");
		GridBagConstraints gbc_btnTripleSearch = new GridBagConstraints();
		gbc_btnTripleSearch.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnTripleSearch.insets = new Insets(0, 0, 5, 0);
		gbc_btnTripleSearch.gridx = 2;
		gbc_btnTripleSearch.gridy = 2;
		add(btnTripleSearch, gbc_btnTripleSearch);

		JButton btnShowAll = new JButton("Discover \"all\" thing descriptions");
		GridBagConstraints gbc_btnShowAll = new GridBagConstraints();
		gbc_btnShowAll.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnShowAll.insets = new Insets(0, 0, 5, 0);
		gbc_btnShowAll.gridx = 2;
		gbc_btnShowAll.gridy = 3;
		add(btnShowAll, gbc_btnShowAll);

		searchPanel = new JPanel();
		searchPanel.setBorder(
				new TitledBorder(null, "Search Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		searchPanel.setPreferredSize(new Dimension(200, 200));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridheight = 2;
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 3;
		add(searchPanel, gbc_panel);
		searchPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panelX = new GridBagConstraints();
		gbc_panelX.insets = new Insets(0, 0, 5, 0);
		gbc_panelX.fill = GridBagConstraints.BOTH;
		gbc_panelX.gridx = 2;
		gbc_panelX.gridy = 4;
		add(panel, gbc_panelX);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JButton btnLoadSelectedThings = new JButton("Load selected thing descriptions in GUI");
		GridBagConstraints gbc_btnLoadSelectedThings = new GridBagConstraints();
		gbc_btnLoadSelectedThings.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnLoadSelectedThings.insets = new Insets(0, 0, 5, 0);
		gbc_btnLoadSelectedThings.gridx = 0;
		gbc_btnLoadSelectedThings.gridy = 0;
		panel_1.add(btnLoadSelectedThings, gbc_btnLoadSelectedThings);

		JButton btnRemoveSelectedThings = new JButton("Remove selected thing descriptions in Repository");
		btnRemoveSelectedThings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireDeleteSelected();
			}
		});
		GridBagConstraints gbc_btnRemoveSelectedThings = new GridBagConstraints();
		gbc_btnRemoveSelectedThings.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemoveSelectedThings.gridx = 0;
		gbc_btnRemoveSelectedThings.gridy = 1;
		panel_1.add(btnRemoveSelectedThings, gbc_btnRemoveSelectedThings);

		JLabel lblAddUri = new JLabel("TD URI:");
		GridBagConstraints gbc_lblAddUri = new GridBagConstraints();
		gbc_lblAddUri.anchor = GridBagConstraints.EAST;
		gbc_lblAddUri.insets = new Insets(0, 0, 5, 5);
		gbc_lblAddUri.gridx = 0;
		gbc_lblAddUri.gridy = 5;
		add(lblAddUri, gbc_lblAddUri);

		textFieldAddUri = new JTextField();
		GridBagConstraints gbc_textFieldAddUri = new GridBagConstraints();
		gbc_textFieldAddUri.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldAddUri.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldAddUri.gridx = 1;
		gbc_textFieldAddUri.gridy = 5;
		add(textFieldAddUri, gbc_textFieldAddUri);
		textFieldAddUri.setColumns(10);

		JButton btnAddUri = new JButton("Add new TD to Repository");
		btnAddUri.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireAddNewTD(textFieldAddUri.getText());
			}
		});
		GridBagConstraints gbc_btnAddUri = new GridBagConstraints();
		gbc_btnAddUri.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddUri.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddUri.gridx = 2;
		gbc_btnAddUri.gridy = 5;
		add(btnAddUri, gbc_btnAddUri);

		JLabel lblUpdateKey = new JLabel("Repository Key:");
		GridBagConstraints gbc_lblUpdateKey = new GridBagConstraints();
		gbc_lblUpdateKey.anchor = GridBagConstraints.EAST;
		gbc_lblUpdateKey.insets = new Insets(0, 0, 5, 5);
		gbc_lblUpdateKey.gridx = 0;
		gbc_lblUpdateKey.gridy = 6;
		add(lblUpdateKey, gbc_lblUpdateKey);

		textFieldUpdateKey = new JTextField();
		BasicTextUI textFieldUIUpdateKey = new HintTextFieldUI(" " + "e.g., /td/{id}", true, Color.GRAY);
		textFieldUpdateKey.setUI(textFieldUIUpdateKey);
		GridBagConstraints gbc_textFieldUpdateKey = new GridBagConstraints();
		gbc_textFieldUpdateKey.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldUpdateKey.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldUpdateKey.gridx = 1;
		gbc_textFieldUpdateKey.gridy = 6;
		add(textFieldUpdateKey, gbc_textFieldUpdateKey);
		textFieldUpdateKey.setColumns(10);

		JButton btnUpdateTD = new JButton("Update existing TD in Repository");
		btnUpdateTD.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireUpdateTD(textFieldUpdateKey.getText(), textFieldAddUri.getText());
			}
		});
		GridBagConstraints gbc_btnUpdateTD = new GridBagConstraints();
		gbc_btnUpdateTD.insets = new Insets(0, 0, 5, 0);
		gbc_btnUpdateTD.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUpdateTD.gridx = 2;
		gbc_btnUpdateTD.gridy = 6;
		add(btnUpdateTD, gbc_btnUpdateTD);
		
		JButton btnAddFileTD = new JButton("Add TD from file");
		btnAddFileTD.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireAddNewTDFile();
			}
		});
		GridBagConstraints gbc_btnAddFileTD = new GridBagConstraints();
		gbc_btnAddFileTD.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddFileTD.gridx = 2;
		gbc_btnAddFileTD.gridy = 7;
		add(btnAddFileTD, gbc_btnAddFileTD);

		btnLoadSelectedThings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireLoadSelected();
			}
		});

		btnShowAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireAllSearch();
			}
		});

		btnFreeSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireFreeSearch();
			}
		});

		btnTripleSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireTripleSearch();
			}
		});
	}

	JFileChooser getJFileChooser() {
		if (this.fileChooser == null) {
			fileChooser = new JFileChooser();
		}
		return fileChooser;
	}
	
	protected void fireLoadSelected() {
		try {
			for (int i = 0; i < tdSearches.size(); i++) {
				TDCheckBox jb = tdSearches.get(i);

				if (jb.isSelected()) {
					Client client = thingsClient.getClientFactory().getClientFromTD(jb.td);
					String name = jb.td.getMetadata().getName();
					thingsClient.addThingPanel(client, name);
				}

			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void fireDeleteSelected() {
		int numberSel = 0;
		String success = "";
		String noSuccess = "";
		for (int i = 0; i < tdSearches.size(); i++) {
			TDCheckBox jb = tdSearches.get(i);
			try {
				if (jb.isSelected()) {
					numberSel++;
					TDRepository tdr = new TDRepository(textFieldIP.getText());
					tdr.deleteTD(jb.key);
					success += jb.key + ",";
				}
			} catch (Exception ex) {
				noSuccess += jb.key + ",";
			}
		}
		if (numberSel > 0) {
			String msg = "<html>Success for: " + success + "<br />";
			if (noSuccess.length() > 0) {
				msg += "No Success for: " + noSuccess;
			}
			msg += "</html>";
			JOptionPane.showMessageDialog(null, msg);
		}
	}

	protected void fireAllSearch() {
		try {
			cleanSearch();
			TDRepository tdr = new TDRepository(textFieldIP.getText());
			JSONObject jo = tdr.nameOfThings();

			addToSearchPanel(jo);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void fireFreeSearch() {
		try {
			cleanSearch();
			TDRepository tdr = new TDRepository(textFieldIP.getText());
			JSONObject jo = tdr.tdFreeTextSearch(textFieldFreeText.getText());

			addToSearchPanel(jo);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void fireTripleSearch() {
		try {
			cleanSearch();
			TDRepository tdr = new TDRepository(textFieldIP.getText());
			JSONObject jo = tdr.tdTripleSearch(textFieldTripleSearch.getText());

			addToSearchPanel(jo);

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void fireAddNewTD(String uri) {
		try {
			byte[] content = getTDBytes(uri);
			
			fireAddNewTDFile(content);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	protected void fireAddNewTDFile() {
		try {
			if (JFileChooser.APPROVE_OPTION == getJFileChooser().showOpenDialog(null)) {
				File f = getJFileChooser().getSelectedFile();
				InputStream is = new FileInputStream(f);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int b;
				while((b=is.read()) != -1) {
					baos.write(b);
				}
				is.close();
				
				byte[] content = baos.toByteArray();
				fireAddNewTDFile(content);

			}
			// ThingDescription td = DescriptionParser.fromURL(new URL(uri));

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	protected void fireAddNewTDFile(byte[] content) throws Exception {
		// check whether we deal with a valid TD
		@SuppressWarnings("unused")
		ThingDescription td = DescriptionParser.fromBytes(content);

		TDRepository tdr = new TDRepository(textFieldIP.getText());
		String key = tdr.addTD(content);

		JOptionPane.showMessageDialog(null, "<html>Added thing description with key: " + key
				+ ". You may need to update search results.</html>");
	}

	private byte[] getTDBytes(String uri) throws IOException {

		URL url = new URL(uri);
		InputStream is = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		while ((b = is.read()) != -1) {
			baos.write(b);
		}
		byte[] content = baos.toByteArray();

		return content;
	}

	protected void fireUpdateTD(String key, String uri) {
		try {
			byte[] content = getTDBytes(uri);
			// check whether we deal with a valid TD
			@SuppressWarnings("unused")
			ThingDescription td = DescriptionParser.fromBytes(content);

			TDRepository tdr = new TDRepository(textFieldIP.getText());
			tdr.updateTD(key, content);

			JOptionPane.showMessageDialog(null, "Updated thing description with key: " + key + " successfully");

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void cleanSearch() {
		// clean-up previous calls
		searchPanel.removeAll();
		tdSearches.clear();
		searchPanel.updateUI();
	}

	protected void addToSearchPanel(JSONObject jo) throws JsonParseException, IOException {
		Box box = Box.createVerticalBox();

		if (jo != null) {
			Iterator<String> iter = jo.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				Object o = jo.get(key);

				String text = o.toString();
				byte[] content = text.getBytes();
				ThingDescription td = DescriptionParser.fromBytes(content);

				if (td == null || td.getInteractions() == null || td.getMetadata() == null) {
					// sometimes repository reports strange JSON-LD files..
					String subset = text.length() < 100 ? text : text.substring(0, 100);
					JOptionPane.showMessageDialog(null,
							"Could not successfully load a JSON-LD message from repository for " + key
									+ ". JSON-LD starts with: " + subset,
							"Error", JOptionPane.ERROR_MESSAGE);
				} else {
					TDCheckBox jb = new TDCheckBox(key, td);
					// pretty print JSON
					String t = new JSONObject(text).toString(2);
					t = t.replace("\n", "<br />");
					t = "<html><div style='font-size: x-small;'><pre>" + t + "</pre></div></html>";
					jb.setToolTipText(t);

					box.add(jb);
					tdSearches.add(jb);
				}
			}
		}

		searchPanel.add(new JScrollPane(box));
		searchPanel.updateUI();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField textFieldIP;
	private JTextField textFieldFreeText;
	private JTextField textFieldTripleSearch;
	private JTextField textFieldAddUri;
	private JTextField textFieldUpdateKey;

}
