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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.PlainDocument;

import org.json.JSONArray;

import com.fasterxml.jackson.core.JsonParseException;

import de.thingweb.client.Client;
import de.thingweb.desc.DescriptionParser;
import de.thingweb.desc.pojo.ThingDescription;
import de.thingweb.discovery.TDRepository;
import de.thingweb.gui.text.IntegerRangeDocumentFilter;

public class DiscoverPanel extends JPanel {
	
	final ThingsClient thingsClient;
	
	final List<TDCheckBox> tdSearches = new ArrayList<>();
	
	class TDCheckBox extends JCheckBox {
		
		private static final long serialVersionUID = -5188829331062093048L;
		
		final ThingDescription td;
		
		public TDCheckBox(ThingDescription td, String name) {
			super(name);
			this.td = td;
		}
	}
	
	public DiscoverPanel(ThingsClient thingsClient) {
		this.thingsClient = thingsClient;
		setBorder(new TitledBorder(null, "Discovery options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{147, 86, 65, 0};
		gridBagLayout.rowHeights = new int[]{23, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Repository URI/IP");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		textFieldIP = new JTextField();
		textFieldIP.setText("localhost");
		GridBagConstraints gbc_textFieldIP = new GridBagConstraints();
		gbc_textFieldIP.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldIP.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldIP.gridx = 1;
		gbc_textFieldIP.gridy = 0;
		add(textFieldIP, gbc_textFieldIP);
		textFieldIP.setColumns(80);
		
		JLabel lblPort = new JLabel("Repository Port:");
		GridBagConstraints gbc_lblPort = new GridBagConstraints();
		gbc_lblPort.anchor = GridBagConstraints.EAST;
		gbc_lblPort.insets = new Insets(0, 0, 5, 5);
		gbc_lblPort.gridx = 0;
		gbc_lblPort.gridy = 1;
		add(lblPort, gbc_lblPort);
		
		textFieldPort = new JTextField();
		textFieldPort.setText("3030");
		GridBagConstraints gbc_textFieldPort = new GridBagConstraints();
		gbc_textFieldPort.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPort.gridx = 1;
		gbc_textFieldPort.gridy = 1;
		add(textFieldPort, gbc_textFieldPort);
		textFieldPort.setColumns(80);
		PlainDocument pd = (PlainDocument) textFieldPort.getDocument();
		pd.setDocumentFilter(new IntegerRangeDocumentFilter(BigInteger.ZERO, BigInteger.valueOf(65535)));
		
		JLabel lblFreeSearchText = new JLabel("Free search text:");
		GridBagConstraints gbc_lblFreeSearchText = new GridBagConstraints();
		gbc_lblFreeSearchText.anchor = GridBagConstraints.EAST;
		gbc_lblFreeSearchText.insets = new Insets(0, 0, 5, 5);
		gbc_lblFreeSearchText.gridx = 0;
		gbc_lblFreeSearchText.gridy = 2;
		add(lblFreeSearchText, gbc_lblFreeSearchText);
		
		textFieldFreeText = new JTextField();
		GridBagConstraints gbc_textFieldFreeText = new GridBagConstraints();
		gbc_textFieldFreeText.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldFreeText.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldFreeText.gridx = 1;
		gbc_textFieldFreeText.gridy = 2;
		add(textFieldFreeText, gbc_textFieldFreeText);
		textFieldFreeText.setColumns(80);
		
		JButton btnFreeSearch = new JButton("Start \"free search\"");
		GridBagConstraints gbc_btnFreeSearch = new GridBagConstraints();
		gbc_btnFreeSearch.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFreeSearch.insets = new Insets(0, 0, 5, 0);
		gbc_btnFreeSearch.gridx = 2;
		gbc_btnFreeSearch.gridy = 2;
		add(btnFreeSearch, gbc_btnFreeSearch);
		
		JLabel lblTripleSearch = new JLabel("Triple Search:");
		GridBagConstraints gbc_lblTripleSearch = new GridBagConstraints();
		gbc_lblTripleSearch.anchor = GridBagConstraints.EAST;
		gbc_lblTripleSearch.insets = new Insets(0, 0, 5, 5);
		gbc_lblTripleSearch.gridx = 0;
		gbc_lblTripleSearch.gridy = 3;
		add(lblTripleSearch, gbc_lblTripleSearch);
		
		textFieldTripleSearch = new JTextField();
		GridBagConstraints gbc_textFieldTripleSearch = new GridBagConstraints();
		gbc_textFieldTripleSearch.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldTripleSearch.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldTripleSearch.gridx = 1;
		gbc_textFieldTripleSearch.gridy = 3;
		add(textFieldTripleSearch, gbc_textFieldTripleSearch);
		textFieldTripleSearch.setColumns(80);
		
		JButton btnTripleSearch = new JButton("Start \"triple search\"\r\n");
		GridBagConstraints gbc_btnTripleSearch = new GridBagConstraints();
		gbc_btnTripleSearch.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnTripleSearch.insets = new Insets(0, 0, 5, 0);
		gbc_btnTripleSearch.gridx = 2;
		gbc_btnTripleSearch.gridy = 3;
		add(btnTripleSearch, gbc_btnTripleSearch);
		
		JPanel searchPanel = new JPanel();
		searchPanel.setBorder(new TitledBorder(null, "Search Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		searchPanel.setPreferredSize(new Dimension(100, 100));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 4;
		add(searchPanel, gbc_panel);
		searchPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JButton btnNewButton = new JButton("Add thing descriptions");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 4;
		add(btnNewButton, gbc_btnNewButton);
		
		btnFreeSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					TDRepository tdr = new TDRepository(textFieldIP.getText(), Integer.parseInt(textFieldPort.getText()));
					JSONArray ja = tdr.tdFreeTextSearch(textFieldFreeText.getText());
					
					addToSearchPanel(ja, searchPanel);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}	
			}
		});
		
		btnTripleSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {					
					TDRepository tdr = new TDRepository(textFieldIP.getText(), Integer.parseInt(textFieldPort.getText()));
					JSONArray ja = tdr.tdTripleSearch(textFieldTripleSearch.getText());

					addToSearchPanel(ja, searchPanel);
					
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}	
			}
		});
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					for(int i=0; i<tdSearches.size(); i++) {
						TDCheckBox jb = tdSearches.get(i);
						
						if(jb.isSelected()) {
							Client client = thingsClient.getClientFactory().getClientFromTD(jb.td);
							String name = jb.td.getMetadata().getName();
							thingsClient.addThingPanel(client, name);							
						}

					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
	
	
	private void addToSearchPanel(JSONArray ja, JPanel panel) throws JsonParseException, IOException {
		// clean-up previous calls
		panel.removeAll();
		tdSearches.clear();
		
		Box box = Box.createVerticalBox();
		
		if(ja != null) {
			for(int i=0; i<ja.length(); i++) {
				Object o = ja.get(i);
				String text = o.toString(); 
				byte[] content = text.getBytes();
				ThingDescription td = DescriptionParser.fromBytes(content);
				
				TDCheckBox jb = new TDCheckBox(td, td.getMetadata().getName());
				jb.setToolTipText(text);
				
				box.add(jb);
				tdSearches.add(jb);
			}
		}
		
		panel.add(new JScrollPane(box));
		panel.updateUI();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField textFieldPort;
	private JTextField textFieldIP;
	private JTextField textFieldFreeText;
	private JTextField textFieldTripleSearch;

}
