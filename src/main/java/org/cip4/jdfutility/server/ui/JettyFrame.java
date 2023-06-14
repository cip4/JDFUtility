/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2015 The International Cooperation for the Integration of 
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        The International Cooperation for the Integration of 
 *        Processes in  Prepress, Press and Postpress (www.cip4.org)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of 
 *    Processes in  Prepress, Press and Postpress" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4",
 *    nor may "CIP4" appear in their name, without prior written
 *    permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR
 * THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration 
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software 
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.jdfutility.server.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.KeyStore;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.thread.MyMutex;
import org.cip4.jdfutility.server.JettyServer;
import org.cip4.jdfutility.server.JettyServer.JettySSLData;

/**
 * @author rainer prosi
 *
 */
public abstract class JettyFrame extends JFrame implements ActionListener
{

	private class StopListener extends WindowAdapter
	{
		@Override
		public void windowClosing(final WindowEvent e)
		{
			new DieHard().start();
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final JettyServer server;

	/**
	 * 
	 * get my server
	 * 
	 * @return
	 */
	protected JettyServer getServer()
	{
		return server;
	}

	private final JButton button;
	private final MyMutex stopper;
	JTextField portField;
	JTextField sslPortField;
	protected final Log log;
	private JCheckBox flakyBox;
	private JTextField urlField;
	private JTextField sslField;
	private JTextField keystoreField;
	private JComboBox<String> keystoreType;
	private JPasswordField keystorePW;

	/**
	 * 
	 * @param server
	 */
	public JettyFrame(JettyServer server)
	{
		super();
		log = LogFactory.getLog(getClass());
		setTitle(getFrameName());
		this.server = server;
		this.stopper = new MyMutex();
		JPanel panel = createPanel();
		button = new JButton("Start Server");
		button.addActionListener(this);
		panel.add(button);
		getContentPane().add(panel);
		setVisible(true);
	}

	protected JPanel createPanel()
	{
		addWindowListener(new StopListener());

		JPanel panel = new JPanel();
		BoxLayout mgr = new BoxLayout(panel, BoxLayout.Y_AXIS);

		panel.setLayout(mgr);
		setSize(new Dimension(500, 222));

		JPanel p0 = new JPanel();
		p0.setLayout(new BoxLayout(p0, BoxLayout.Y_AXIS));
		p0.add(new JLabel("http web ui address:"));
		p0.add(new JLabel("please copy / paste one of the next lines into a browser of your choice:"));

		panel.add(p0);

		JPanel p1 = new JPanel();
		p1.add(new JLabel("port: "));
		portField = new JTextField();
		portField.setEditable(true);
		portField.setText("" + server.getPort());
		p1.add(portField);
		String baseURL = server.getBaseURL(false);
		urlField = new JTextField(baseURL);
		urlField.setEditable(false);
		p1.add(urlField);

		panel.add(p1);

		JPanel p2 = new JPanel();

		p2.add(new JLabel("ssl port: "));
		sslPortField = new JTextField();
		sslPortField.setEditable(true);
		sslPortField.setText("" + server.getSSLPort());
		p2.add(sslPortField);
		baseURL = server.getBaseURL(true);
		sslField = new JTextField(baseURL);
		sslField.setEditable(false);
		p2.add(sslField);

		flakyBox = new JCheckBox("Allow flaky SSL");
		flakyBox.setSelected(false);
		p2.add(flakyBox);

		keystoreField = new JTextField();
		keystoreField.setText(JettyServer.getDefaultKeyStorePath());
		p2.add(keystoreField);

		keystorePW = new JPasswordField();
		keystorePW.setText("changeit");
		p2.add(keystorePW);

		keystoreType = new JComboBox<>(new String[] { KeyStore.getDefaultType(), "PKCS12" });
		p2.add(keystoreType);
		panel.add(p2);

		return panel;
	}

	/**
	 * 
	 * call this in main to wait for an end
	 * 
	 * @return
	 */
	public int waitCompleted()
	{
		ThreadUtil.wait(stopper, 0);
		return 0;
	}

	/**
	 * 
	 * @return
	 */
	protected abstract String getFrameName();

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (button.equals(arg0.getSource()))
		{
			if (button.getText().contains("Start"))
			{
				started();
				try
				{
					server.runServer();
				}
				catch (Throwable e)
				{
					log.error("Exception cought starting server", e);
				}
			}
			else
			{
				new Stopper().start();
			}
		}
	}

	/**
	 * 
	 */
	protected void started()
	{
		button.setText("Stop Server");
		server.setPort(StringUtil.parseInt(portField.getText(), server.getPort()));
		server.setSSLPort(StringUtil.parseInt(sslPortField.getText(), server.getSSLPort()));
		JettySSLData ssl = server.getSSLData();
		ssl.setAllowFlakySSL(flakyBox.isSelected());
		ssl.setPassword(new String(keystorePW.getText()));
		ssl.setKeystoreType((String) keystoreType.getSelectedItem());
		ssl.setKeystorePath(keystoreField.getText());
		portField.setEditable(false);
		sslPortField.setEditable(false);
		flakyBox.setEnabled(false);
		sslField.setText(server.getBaseURL(true));
		urlField.setText(server.getBaseURL(false));
		keystoreField.setEditable(false);
		keystorePW.setEditable(false);
		keystoreType.setEditable(false);

	}

	protected void stopped()
	{
		button.setText("Start Server");
		button.setEnabled(true);
		portField.setEditable(true);
		sslPortField.setEditable(true);
		flakyBox.setEnabled(true);
		keystoreField.setEditable(true);
		keystorePW.setEditable(true);
		keystoreType.setEditable(true);
	}

	/**
	 * 
	 */
	private class Stopper extends Thread
	{
		private Stopper()
		{
			super("StopThread");
			button.setText("Stopping server");
			button.setEnabled(false);
		}

		@Override
		public void run()
		{
			server.stop();
			while (server.isStopping())
				ThreadUtil.sleep(100);
			stopped();
		}
	}

	private class DieHard extends Thread
	{
		private DieHard()
		{
			super("DieHard");
			button.setText("shutting down");
			button.setEnabled(false);
		}

		/**
		 * 
		 */
		@Override
		public void run()
		{
			server.stop();
			server.destroy();
			ThreadUtil.notify(stopper);
		}
	}
}
