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

import com.fasterxml.jackson.databind.JsonNode;
import de.thingweb.client.Callback;
import de.thingweb.client.Client;
import de.thingweb.client.UnsupportedException;
import de.thingweb.client.security.Registration;
import de.thingweb.client.security.Security4NicePlugfest;
import de.thingweb.desc.pojo.ActionDescription;
import de.thingweb.desc.pojo.EventDescription;
import de.thingweb.desc.pojo.PropertyDescription;
import de.thingweb.discovery.TDRepository;
import de.thingweb.gui.text.BooleanDocumentFilter;
import de.thingweb.gui.text.HintTextFieldUI;
import de.thingweb.gui.text.IntegerRangeDocumentFilter;
import de.thingweb.thing.Content;
import de.thingweb.thing.MediaType;
import de.thingweb.util.encoding.ContentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.border.TitledBorder;

public class ThingPanelUI extends JPanel implements ActionListener, Callback {
	
	private static final Logger log = LoggerFactory.getLogger(ThingPanelUI.class);

	private static final long serialVersionUID = 2117762031555752901L;
	
	// Security
	JToggleButton tglbtnSecurity;
	Security4NicePlugfest s4p;
	Registration sreg;
	JEditorPane textAreaSecurityToken;
	
	final Client client;
	// TODO support other media types
	final MediaType mediaType = MediaType.APPLICATION_JSON;
	
	// TODO remove if settled
	final boolean useValueInJsonInsteadOfName = true;
	final String JSON_VALUE = "value";

	JButton buttonPropertiesGET;
	
	JTextPane infoTextPane;

	Map<String, JTextField> propertyComponents;
	Map<String, JTextField> actionComponents;
	
	final static BigInteger MAX_UNSIGNED_LONG = new BigInteger("18446744073709551615");
	final static BigInteger MAX_UNSIGNED_INT = BigInteger.valueOf(4294967295L);
	final static BigInteger MAX_UNSIGNED_SHORT = BigInteger.valueOf(65535);
	final static BigInteger MAX_UNSIGNED_BYTE = BigInteger.valueOf(255);
	final static BigInteger MAX_BYTE = BigInteger.valueOf(127);
	final static BigInteger MIN_BYTE = BigInteger.valueOf(-128);
	final static BigInteger MAX_SHORT = BigInteger.valueOf(32767);
	final static BigInteger MIN_SHORT = BigInteger.valueOf(-32768);
	final static BigInteger MAX_INT = BigInteger.valueOf(2147483647);
	final static BigInteger MIN_INT = BigInteger.valueOf(-2147483648);
	final static BigInteger MAX_LONG = BigInteger.valueOf(9223372036854775807L);
	final static BigInteger MIN_LONG = BigInteger.valueOf(-9223372036854775808L);
	
	JTextField createTextField(String type, boolean editable) {
		if(type == null) {
			type = "";
		}
		JTextField textField = new JTextField();
		textField.setEditable(editable);
		BasicTextUI textFieldUI = new HintTextFieldUI(" " + type, editable, Color.GRAY);
		textField.setUI(textFieldUI);
		PlainDocument pd = (PlainDocument) textField.getDocument();
		textField.setToolTipText(type);
		switch(type) {
		case "xsd:unsignedLong":
			pd.setDocumentFilter(new IntegerRangeDocumentFilter(BigInteger.ZERO, MAX_UNSIGNED_LONG));
			break;
		case "xsd:unsignedInt":
			pd.setDocumentFilter(new IntegerRangeDocumentFilter(BigInteger.ZERO, MAX_UNSIGNED_INT));
			break;
		case "xsd:unsignedShort":
			pd.setDocumentFilter(new IntegerRangeDocumentFilter(BigInteger.ZERO, MAX_UNSIGNED_SHORT));
			break;
		case "xsd:unsignedByte":
			pd.setDocumentFilter(new IntegerRangeDocumentFilter(BigInteger.ZERO, MAX_UNSIGNED_BYTE));
			break;
		case "xsd:long":
			pd.setDocumentFilter(new IntegerRangeDocumentFilter(MIN_LONG, MAX_LONG));
			break;
		case "xsd:int":
			pd.setDocumentFilter(new IntegerRangeDocumentFilter(MIN_INT, MAX_INT));
			break;
		case "xsd:short":
			pd.setDocumentFilter(new IntegerRangeDocumentFilter(MIN_SHORT, MAX_SHORT));
			break;
		case "xsd:byte":
			pd.setDocumentFilter(new IntegerRangeDocumentFilter(MIN_BYTE, MAX_BYTE));
			break;
		case "xsd:boolean":
			pd.setDocumentFilter(new BooleanDocumentFilter());
			break;
		default:
			log.warn("TextField created without input control for type: " + type);
		}
		return textField;
	}
	
	private Security4NicePlugfest getSecurity() {
		if(s4p == null) {
			s4p = new Security4NicePlugfest();
		}
		return s4p;
	}

	/**
	 * Create the panel.
	 * @param client the client used in this panel
	 */
	public ThingPanelUI(Client client) {
		this.client = client;
		propertyComponents = new HashMap<>();
		actionComponents = new HashMap<>();
		
		JPanel gbPanel = new JPanel();
		GridBagLayout gbl_gbPanel = new GridBagLayout();
		gbl_gbPanel.rowWeights = new double[]{0.0, 1.0};
		gbl_gbPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0};
		gbPanel.setLayout(gbl_gbPanel);
		
		infoTextPane = new JTextPane();
		infoTextPane.setEditable(false);
		infoTextPane.setContentType("text/html");
		// infoTextPane.setPreferredSize(new Dimension(0, 50));
		// infoTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		
		this.setLayout(new BorderLayout());
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		// splitPane.setTopComponent(gbPanel);
		splitPane.setTopComponent(new JPanel());
		splitPane.setResizeWeight(1.0); // value of 1 specifies the left/top component gets all the extra space
		splitPane.setOneTouchExpandable(true);
		// splitPane.setDividerLocation(150);
		
		this.add(gbPanel, BorderLayout.NORTH);
		
		
		JScrollPane jsp = new JScrollPane(infoTextPane);
		jsp.setPreferredSize(new Dimension(-1, 50));
		// this.add(jsp, BorderLayout.SOUTH);
		splitPane.setBottomComponent(jsp);
		this.add(splitPane, BorderLayout.CENTER);

		Insets ins2 = new Insets(2, 2, 2, 2);
		int yline = 0;
		
		// URI and such
		GridBagConstraints gbc0_0 = new GridBagConstraints();
		gbc0_0.insets = new Insets(0, 0, 5, 5);
		gbc0_0.gridx = 0;
		gbc0_0.gridy = yline;
		gbc0_0.gridwidth = 3;
		gbPanel.add(new JLabel("<html><h4>" + client.getUsedProtocolURI() + " (" + mediaType + ")</h4></html>"), gbc0_0);
		
		final String labelStringOn = "Security (ON)";
		final String labelStringOff = "Security (OFF)";
		
		JButton btnAddTDRep = new JButton("Add TD to Repository");
		btnAddTDRep.setVisible(false);
		btnAddTDRep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String ip = JOptionPane.showInputDialog(
		                    null,
		                    "Repository IP",
		                    TDRepository.ETH_URI);
					if(ip != null) {
						@SuppressWarnings("unused")
						TDRepository td = new TDRepository(ip);
						// TODO how to retrieve bytes from TD ?
						
//						byte[] content = null;
//						td.addTD(content);						
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		GridBagConstraints gbc_btnAddTDRep = new GridBagConstraints();
		gbc_btnAddTDRep.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddTDRep.gridx = 3;
		gbc_btnAddTDRep.gridy = 0;
		gbPanel.add(btnAddTDRep, gbc_btnAddTDRep);
		
		tglbtnSecurity = new JToggleButton(labelStringOn);
		tglbtnSecurity.setSelected(true);
		tglbtnSecurity.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_tglbtnSecurity = new GridBagConstraints();
		gbc_tglbtnSecurity.insets = new Insets(0, 0, 5, 0);
		gbc_tglbtnSecurity.anchor = GridBagConstraints.EAST;
		gbc_tglbtnSecurity.gridx = 4;
		gbc_tglbtnSecurity.gridy = 0;		
		gbPanel.add(tglbtnSecurity, gbc_tglbtnSecurity);
		
		yline++;
		
		JPanel panelSecurity = new JPanel();
		panelSecurity.setBorder(new TitledBorder(null, "Security", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panelSecurity = new GridBagConstraints();
		gbc_panelSecurity.gridwidth = 5;
		gbc_panelSecurity.fill = GridBagConstraints.BOTH;
		gbc_panelSecurity.gridx = 0;
		gbc_panelSecurity.gridy = yline;
		gbPanel.add(panelSecurity, gbc_panelSecurity);
		GridBagLayout gbl_panelSecurity = new GridBagLayout();
		gbl_panelSecurity.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panelSecurity.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panelSecurity.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelSecurity.rowWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		panelSecurity.setLayout(gbl_panelSecurity);
		
		JButton btnSecurityRegister = new JButton("1. Register");
		GridBagConstraints gbc_btnSecurityRegister = new GridBagConstraints();
		gbc_btnSecurityRegister.anchor = GridBagConstraints.WEST;
		gbc_btnSecurityRegister.insets = new Insets(0, 0, 5, 5);
		gbc_btnSecurityRegister.gridx = 0;
		gbc_btnSecurityRegister.gridy = 0;
		panelSecurity.add(btnSecurityRegister, gbc_btnSecurityRegister);
		
		JButton btnSecurityRequestToken = new JButton("2. Request Token");
		GridBagConstraints gbc_btnSecurityRequestToken = new GridBagConstraints();
		gbc_btnSecurityRequestToken.anchor = GridBagConstraints.WEST;
		gbc_btnSecurityRequestToken.insets = new Insets(0, 0, 5, 5);
		gbc_btnSecurityRequestToken.gridx = 1;
		gbc_btnSecurityRequestToken.gridy = 0;
		panelSecurity.add(btnSecurityRequestToken, gbc_btnSecurityRequestToken);
		
		JCheckBox chckbxAllowModifyingSecurityToken = new JCheckBox("<html>Allow modifying security token <br/> (Warning: may break communication!)\r\n</html>");
		GridBagConstraints gbc_chckbxAllowModifyingSecurityToken = new GridBagConstraints();
		gbc_chckbxAllowModifyingSecurityToken.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxAllowModifyingSecurityToken.gridx = 3;
		gbc_chckbxAllowModifyingSecurityToken.gridy = 0;
		panelSecurity.add(chckbxAllowModifyingSecurityToken, gbc_chckbxAllowModifyingSecurityToken);
		

		textAreaSecurityToken = new JEditorPane();
		textAreaSecurityToken.setEditable(false);
		
		JScrollPane scrollPane = new JScrollPane(textAreaSecurityToken);
		scrollPane.setPreferredSize(new Dimension(100, 75));
		scrollPane.setMaximumSize(new Dimension(100, 75));
		scrollPane.setSize(new Dimension(100, 100));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 1;
		panelSecurity.add(scrollPane, gbc_scrollPane);
		
		chckbxAllowModifyingSecurityToken.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textAreaSecurityToken.setEditable(chckbxAllowModifyingSecurityToken.isSelected());
				if(chckbxAllowModifyingSecurityToken.isSelected()) {
					chckbxAllowModifyingSecurityToken.setForeground(Color.RED);
				} else {
					chckbxAllowModifyingSecurityToken.setForeground(null);
				}
			}
		});
		
		tglbtnSecurity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (tglbtnSecurity.isSelected()) {
					tglbtnSecurity.setForeground(null); // default
					tglbtnSecurity.setText(labelStringOn);
					panelSecurity.setVisible(true);
					gbPanel.repaint();
					gbPanel.updateUI();
			    } else {
			    	tglbtnSecurity.setForeground(Color.RED);
			    	tglbtnSecurity.setText(labelStringOff);
			    	panelSecurity.setVisible(false);
			    	sreg = null;
			    	textAreaSecurityToken.setText("");
			    }
			}
		});
		
		btnSecurityRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Security4NicePlugfest sec = getSecurity();
				try {
					sreg = sec.requestRegistrationAM();
					JOptionPane.showMessageDialog(null, "Registration succesful (client_id=" + sreg.c_id + ", client_security=" + sreg.c_secret + ")", "Security Registration", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "" + e1.getMessage(), "Security Registration", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		btnSecurityRequestToken.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(sreg == null) {
					JOptionPane.showMessageDialog(null, "Please first register!", "Security Request Token", JOptionPane.ERROR_MESSAGE);
				} else {
					Security4NicePlugfest sec = getSecurity();
					try {
						textAreaSecurityToken.setText(sec.requestASToken(sreg));
						JOptionPane.showMessageDialog(null, "Request Token succesful", "Security Request Token", JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "" + e1.getMessage(), "Security Request Token", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		
		yline++;

		// ###### Properties
		List<PropertyDescription> properties = client.getProperties();
		if(properties != null && properties.size() > 0) {
			GridBagConstraints gbcP_0 = new GridBagConstraints();
			gbcP_0.gridx = 0;
			gbcP_0.gridy = yline;
			gbcP_0.gridwidth = 2;
			gbcP_0.anchor = GridBagConstraints.NORTHWEST; // LINE_START;
			JLabel labelP = new JLabel("<html><h2>Properties</h2></html>");
			gbPanel.add(labelP, gbcP_0);

			GridBagConstraints gbcP_2 = new GridBagConstraints();
			gbcP_2.gridx = 2;
			gbcP_2.gridy = yline;
			gbcP_2.fill = GridBagConstraints.HORIZONTAL;
			buttonPropertiesGET = new JButton("Get all");
			buttonPropertiesGET.addActionListener(this);
			gbPanel.add(buttonPropertiesGET, gbcP_2);

			yline++;

			for (int i = 0; i < properties.size(); i++) {
				PropertyDescription p = properties.get(i);

				// label
				GridBagConstraints gbcX_0 = new GridBagConstraints();
				gbcX_0.gridx = 0;
				gbcX_0.gridy = yline;
				gbcX_0.anchor = GridBagConstraints.LINE_START;
				gbcX_0.insets = ins2;
				gbPanel.add(new JLabel(p.getName() + ": "), gbcX_0);

				// value
				GridBagConstraints gbcX_1 = new GridBagConstraints();
				gbcX_1.gridx = gbcX_0.gridx + 1;
				gbcX_1.gridy = yline;
				gbcX_1.fill = GridBagConstraints.HORIZONTAL;
				gbcX_1.weightx = 1;
				gbcX_1.insets = ins2;
				JTextField textField = createTextField(p.getOutputType(), p.isWritable());
				gbPanel.add(textField, gbcX_1);
				propertyComponents.put(p.getName(), textField);
				// refreshProperty(p.getName()); // refresh value

				// get button
				GridBagConstraints gbcX_2 = new GridBagConstraints();
				gbcX_2.gridx = gbcX_1.gridx + 1;
				gbcX_2.gridy = yline;
				gbcX_2.fill = GridBagConstraints.HORIZONTAL;
				gbcX_2.insets = ins2;
				JButton buttonGet = new JButton("Get");
				gbPanel.add(buttonGet, gbcX_2);
				buttonGet.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						clientGET(p.getName());
					}
				});

				// observe button
				GridBagConstraints gbcX_3 = new GridBagConstraints();
				gbcX_3.gridx = gbcX_2.gridx + 1;
				gbcX_3.gridy = yline;
				gbcX_3.fill = GridBagConstraints.HORIZONTAL;
				gbcX_3.insets = ins2;
				JToggleButton buttonObserve = new JToggleButton("Observe");
				gbPanel.add(buttonObserve, gbcX_3);
				buttonObserve.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ae) {
						clientObserve(p.getName());
					}
				});

				if (p.isWritable()) {
					// update/put button
					// TODO show on/off for xsd:boolean ?
					GridBagConstraints gbcX_4 = new GridBagConstraints();
					gbcX_4.gridx = gbcX_3.gridx + 1;
					gbcX_4.gridy = yline;
					gbcX_4.fill = GridBagConstraints.HORIZONTAL;
					gbcX_4.insets = ins2;
					JButton buttonPut = new JButton("Put");
					gbPanel.add(buttonPut, gbcX_4);
					buttonPut.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							clientPUT(p.getName(), p.getOutputType(), textField.getText());
						}
					});
				}

				yline++;
			}
		}


		// ###### Actions
		List<ActionDescription> actions = client.getActions();
		if(actions != null && actions.size() > 0) {
			GridBagConstraints gbcA_0 = new GridBagConstraints();
			gbcA_0.gridx = 0;
			gbcA_0.gridy = yline++;
			gbcA_0.gridwidth = 2;
			gbcA_0.anchor = GridBagConstraints.LINE_START;
			JLabel labelA = new JLabel("<html><h2>Actions</h2></html>");
			gbPanel.add(labelA, gbcA_0);

			
			for (int i = 0; i < actions.size(); i++) {
				ActionDescription a = actions.get(i);

				// label
				GridBagConstraints gbcX_0 = new GridBagConstraints();
				gbcX_0.gridx = 0;
				gbcX_0.gridy = yline;
				gbcX_0.anchor = GridBagConstraints.LINE_START;
				gbcX_0.insets = ins2;
				gbPanel.add(new JLabel(a.getName() + ": "), gbcX_0);

				// input
				GridBagConstraints gbcX_1 = new GridBagConstraints();
				gbcX_1.gridx = gbcX_0.gridx + 1;
				gbcX_1.gridy = yline;
				gbcX_1.fill = GridBagConstraints.HORIZONTAL;
				gbcX_1.weightx = 1;
				gbcX_1.insets = ins2;
				JTextField textField;
				if (a.getInputType() != null && a.getInputType().length() > 0) {
					textField = createTextField(a.getInputType(), true);
					gbPanel.add(textField, gbcX_1);
				} else {
					textField = createTextField(a.getInputType(), false);
				}
				actionComponents.put(a.getName(), textField);

				// fire button
				GridBagConstraints gbcX_2 = new GridBagConstraints();
				gbcX_2.gridx = gbcX_1.gridx + 1;
				gbcX_2.gridy = yline;
				gbcX_2.fill = GridBagConstraints.HORIZONTAL;
				gbcX_2.insets = ins2;
				JButton buttonAction = new JButton("Start '" + a.getName() + "'");
				gbPanel.add(buttonAction, gbcX_2);
				buttonAction.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						clientAction(a.getName(), a.getInputType(), textField.getText());
					}
				});

				yline++;
			}

		}
		

		// ###### Events
		List<EventDescription> events = client.getEvents();
		if(events != null && events.size() > 0) {
			GridBagConstraints gbcE_0 = new GridBagConstraints();
			gbcE_0.gridx = 0;
			gbcE_0.gridy = yline++;
			gbcE_0.gridwidth = 2;
			gbcE_0.anchor = GridBagConstraints.LINE_START;
			JLabel labelE = new JLabel("<html><h2>Events (TODO)</h2></html>");
			gbPanel.add(labelE, gbcE_0);
			
			
			for (int i = 0; i < events.size(); i++) {
				EventDescription e = events.get(i);

				// label
				GridBagConstraints gbcX_0 = new GridBagConstraints();
				gbcX_0.gridx = 0;
				gbcX_0.gridy = yline;
				gbcX_0.anchor = GridBagConstraints.LINE_START;
				gbcX_0.insets = ins2;
				gbPanel.add(new JLabel(e.getName() + ": "), gbcX_0);

				// input
				GridBagConstraints gbcX_1 = new GridBagConstraints();
				gbcX_1.gridx = gbcX_0.gridx + 1;
				gbcX_1.gridy = yline;
				gbcX_1.fill = GridBagConstraints.HORIZONTAL;
				gbcX_1.weightx = 1;
				gbcX_1.insets = ins2;
				JTextField textField = createTextField("", false);
				gbPanel.add(textField, gbcX_1);
				
				// TODO how to get informed about change (observe) ????


				yline++;
			}
		}


	}
	
	// https://github.com/w3c/wot/blob/master/TF-TD/Tutorial.md#coap-protocol-binding
	protected byte[] getPayload(String name, String type, String value) throws IllegalArgumentException {
		assert(this.mediaType == MediaType.APPLICATION_JSON);
		// {
	    //	"value" : 4000
		// }
		StringBuilder sb = new StringBuilder();
		sb.append("{\"");
		if(useValueInJsonInsteadOfName) {
			sb.append(JSON_VALUE);
		} else {
			sb.append(name);
		}
		sb.append("\":");
		
		switch(type) {
		case "xsd:unsignedLong":
		case "xsd:unsignedInt":
		case "xsd:unsignedShort":
		case "xsd:unsignedByte":
		case "xsd:long":
		case "xsd:int":
		case "xsd:short":
		case "xsd:byte":
			try {
				// parse integer
				new BigInteger(value);
				sb.append(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("'" + value + "' is not a valid integer");
			}
			break;
		case "xsd:boolean":
			boolean b = BooleanDocumentFilter.getBoolean(value);
			if(b) {
				sb.append("true");
			} else {
				sb.append("false");
			}
			break;
		default:
			// assume string --> add apostrophes
			// TODO how to deal with null
			// TODO	how to deal with complex types... nested textfields for each simple type?
			sb.append("\"");
			sb.append(value);
			sb.append("\"");
		}
		
		sb.append("}");
		
		return sb.toString().getBytes();
	}


	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == buttonPropertiesGET) {
			for (String prop : propertyComponents.keySet()) {
				clientGET(prop);
			}
		}

	}
	
	
	///////// Info Panel
	
	class InfoMessage {
		final String msg;
		final Date timestamp;
		final boolean error;
		public InfoMessage(String msg, Date timestamp, boolean error) {
			this.msg = msg;
			this.timestamp = timestamp;
			this.error = error;
		}
	}
	
	LinkedList<InfoMessage> infoMessages = new LinkedList<InfoMessage>();
	final int MAX_INFO_LINES = 20;
	final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	private void printInfo(String msg, boolean error) {
		synchronized(this) {
			if(infoMessages.size() > MAX_INFO_LINES) {
				infoMessages.removeFirst();
			}
			InfoMessage imsg = new InfoMessage(msg, new Date(), error);
			infoMessages.add(imsg);
			
			// update info panel
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body  style=\"font-size:small\"");
			for(InfoMessage im : infoMessages) {
				sb.append("<span");
				if(im.error) {
					sb.append(" style=\"color:red\"");
				}
				sb.append(">");
				sb.append(dateFormat.format(im.timestamp) + ": " + im.msg);
				sb.append("</span><br />");
			}
			sb.append("</body></html>");
			
			String s = sb.toString();
			infoTextPane.setText(s);
			int len = infoTextPane.getDocument().getLength();
			infoTextPane.setCaretPosition(len);

		}
		
	}
	
	///////// Client calls
	

	protected void clientGET(String prop) {
		try {
			printInfo("GET request for " + prop, false);
			if(tglbtnSecurity.isSelected()) {
				if(textAreaSecurityToken.getText() == null || textAreaSecurityToken.getText().length() == 0) {
					JOptionPane.showMessageDialog(null, "Unselect security or request as_token first", "Security", JOptionPane.ERROR_MESSAGE);
				} else {
					log.info("Start 'secure' get: " + prop);
					client.get(prop, this, textAreaSecurityToken.getText());
				}
			} else {
				client.get(prop, this);
			}
		} catch (UnsupportedException e) {
			JOptionPane.showMessageDialog(null, "" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	protected void clientObserve(String prop) {
		try {
			printInfo("Observe request for " + prop, false);
			if(tglbtnSecurity.isSelected()) {
				if(textAreaSecurityToken.getText() == null || textAreaSecurityToken.getText().length() == 0) {
					JOptionPane.showMessageDialog(null, "Unselect security or request as_token first", "Security", JOptionPane.ERROR_MESSAGE);
				} else {
					log.info("Start 'secure' observe: " + prop);
					client.observe(prop, this, textAreaSecurityToken.getText());
				}
			} else {
				client.observe(prop, this);
			}
		} catch (UnsupportedException e) {
			JOptionPane.showMessageDialog(null, "" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	protected void clientPUT(String prop, String outputType, String svalue) {
		try {
			byte[] payload = getPayload(prop, outputType, svalue);
			Content c = new Content(payload, mediaType);
			printInfo("PUT request for " + prop + ": " + new String(payload), false);
			if(tglbtnSecurity.isSelected()) {
				if(textAreaSecurityToken.getText() == null || textAreaSecurityToken.getText().length() == 0) {
					JOptionPane.showMessageDialog(null, "Unselect security or request as_token first", "Security", JOptionPane.ERROR_MESSAGE);
				} else {
					log.info("Start 'secure' put: " + prop);
					client.put(prop, c, this, textAreaSecurityToken.getText());
				}
			} else {
				client.put(prop, c, this);
			}
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, "No valid value of type '" + outputType + "' given: " + e.getMessage(), "Value Error", JOptionPane.ERROR_MESSAGE);
		} catch (UnsupportedException e) {
			JOptionPane.showMessageDialog(null, "" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	protected void clientAction(String prop, String inputType, String svalue) {
		try {
			byte[] payload = getPayload(prop, inputType, svalue);
			Content c = new Content(payload, mediaType);
			printInfo("Action request for " + prop + ": " + new String(payload), false);
			if(tglbtnSecurity.isSelected()) {
				if(textAreaSecurityToken.getText() == null || textAreaSecurityToken.getText().length() == 0) {
					JOptionPane.showMessageDialog(null, "Unselect security or request as_token first", "Security", JOptionPane.ERROR_MESSAGE);
				} else {
					log.info("Start 'secure' action: " + prop);
					client.action(prop, c, this, textAreaSecurityToken.getText());
				}
			} else {
				client.action(prop, c, this);
			}
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, "No valid value of type '" + inputType + "' given", "Value Error", JOptionPane.ERROR_MESSAGE);
		} catch (UnsupportedException e) {
			JOptionPane.showMessageDialog(null, "" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	///////// Thing CALLBACKS

	@Override
	public void onPut(String propertyName, Content response) {
		// refreshProperty(propertyName);
		printInfo("PUT response success for " + propertyName, false);
		
		JTextField text = propertyComponents.get(propertyName);
		if(text != null) {
			text.setBackground(UIManager.getColor("TextField.background")); // default, override possible error
		} else {
			log.error("No text-field found for propertyName: " + propertyName);
		}
	}

	@Override
	public void onPutError(String propertyName) {
		printInfo("PUT failure for " + propertyName, true);
		
		JTextField text = propertyComponents.get(propertyName);
		if(text != null) {
			text.setBackground(Color.RED);
		} else {
			log.error("No text-field found for propertyName: " + propertyName);
		}
	}
	
	
	private void get(String msgPrefix, String propertyName, Content response) {
		// TODO deal with other media-types
		assert(response.getMediaType() == MediaType.TEXT_PLAIN || response.getMediaType() == MediaType.APPLICATION_JSON);
		JTextField text = propertyComponents.get(propertyName);
		if(text != null) {
			text.setBackground(UIManager.getColor("TextField.background")); // default, override possible error
			try {
				JsonNode n = ContentHelper.readJSON(response.getContent());
				String t;
				if(useValueInJsonInsteadOfName) {
					t = n.get(JSON_VALUE).asText();
				} else {
					t = n.get(propertyName).asText();
				}
				text.setText(t);
				if(text.getText().equals(t)) {
					printInfo(msgPrefix + " success for " + propertyName + ": " + new String(response.getContent()), false);
				} else {
					// Note: should not happen though
					printInfo(msgPrefix + " error for " + propertyName + ": setting text-field value '" + t + "' failed", true);
				}
			} catch (Exception e) {
				printInfo(msgPrefix + " parsing error for " + propertyName + " and value = '" + new String(response.getContent()) + "'. Invalid or empty message?", true);
				text.setBackground(Color.RED);
			}
		} else {
			log.error("No text-field found for propertyName: " + propertyName);
		}
	
	}

	@Override
	public void onGet(String propertyName, Content response) {
		get("GET response", propertyName, response);
	}

	@Override
	public void onGetError(String propertyName) {
		printInfo("GET failure for " + propertyName, true);
		JTextField text = propertyComponents.get(propertyName);
		if(text != null) {
			text.setBackground(Color.RED);
		} else {
			log.error("No text-field found for propertyName: " + propertyName);
		}
	}

	@Override
	public void onObserve(String propertyName, Content response) {
		get("Observe response", propertyName, response);
	}

	@Override
	public void onObserveError(String propertyName) {
		printInfo("Observe failure for " + propertyName, true);
		JTextField text = propertyComponents.get(propertyName);
		if(text != null) {
			text.setBackground(Color.RED);
		} else {
			log.error("No text-field found for propertyName: " + propertyName);
		}
	}

	@Override
	public void onAction(String actionName, Content response) {
		printInfo("Action response success for " + actionName + ": '" + new String(response.getContent()) + "'", false);
		// TODO deal with other media-types
		assert(response.getMediaType() == MediaType.TEXT_PLAIN || response.getMediaType() == MediaType.APPLICATION_JSON);
		// TODO how to deal with action response?
		@SuppressWarnings("unused")
		String sresp = new String(response.getContent());
		
		JTextField text = actionComponents.get(actionName);
		if(text != null) {
			text.setBackground(UIManager.getColor("TextField.background")); // default, override possible error
		} else {
			log.error("No text-field found for actionName: " + actionName);
		}
	}

	@Override
	public void onActionError(String actionName) {
		printInfo("Action failure for " + actionName, true);
		
		JTextField text = actionComponents.get(actionName);
		if(text != null) {
			text.setBackground(Color.RED);
		} else {
			log.error("No text-field found for actionName: " + actionName);
		}
	}

}
