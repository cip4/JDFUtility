/*
 * Created on Jul 12, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.cip4.jdflib.generator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.generator.Generator;
import org.cip4.jdflib.generator.SchemaComplexType;
import org.cip4.jdflib.generator.SchemaDoc;
import org.cip4.jdflib.generator.StringCollector;

/**
 * @author matternk
 * 
 *         To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ListButtonPanel extends JPanel implements ActionListener, MouseListener
{
	private static final long serialVersionUID = 1L;
	private final ComplexTypeList m_motherComp;
	private JButton com_selectAll;
	private JButton com_deSelectAll;
	protected JButton com_Generate;
	private JLabel com_schemaPath;
	private JLabel com_OutputPath;
	private JTextField com_SchemaField;
	private JTextField com_OutputField;
	private final JFileChooser fc;
	private String strOutputPath = "";
	private String strSchemaPath = "";
	private File schemaFile_JDFTypes;
	private File schemaFile_JDFResource;
	private File schemaFile_JDFResourceElems;
	private File schemaFile_JDFMessage;
	private File schemaFile_JDFCore;
	private File schemaFile_JDFCapability;

	protected Vector m_vCore = new Vector();
	protected ArrayList m_vToGenerate = new ArrayList();
	protected ArrayList allNamesToOrganize = new ArrayList();
	protected Vector m_vBuffer = new Vector();
	protected String fileSep = System.getProperty("file.separator");

	protected ArrayList m_docs = new ArrayList();

	protected boolean bParsedReady = false;
	protected boolean bSchemaDocsReady = false;
	boolean bSchemaPathSet = false;
	boolean bOutputPathSet = false;

	public ListButtonPanel(final ComplexTypeList mother)
	{
		m_motherComp = mother;
		fc = new JFileChooser("/");
		init();
	}

	private void init()
	{
		com_SchemaField = new JTextField(20);
		com_SchemaField.setEditable(false);
		com_SchemaField.addMouseListener(this);

		com_schemaPath = new JLabel("Schema Path: ");
		com_schemaPath.setHorizontalAlignment(SwingConstants.LEFT);
		com_schemaPath.setLabelFor(com_SchemaField);
		com_schemaPath.addMouseListener(this);

		com_OutputField = new JTextField(20);
		com_OutputField.setEditable(false);
		com_OutputField.addMouseListener(this);

		com_OutputPath = new JLabel("Output Path: ");
		com_OutputPath.setHorizontalAlignment(SwingConstants.LEFT);
		com_schemaPath.setLabelFor(com_OutputField);
		com_OutputPath.addMouseListener(this);

		com_selectAll = new JButton("Select All");
		com_selectAll.addActionListener(this);

		com_deSelectAll = new JButton("Deselect All");
		com_deSelectAll.addActionListener(this);

		com_Generate = new JButton("Generate");
		com_Generate.addActionListener(this);
		com_Generate.setEnabled(false);

		final GridBagLayout layout_gridBag = new GridBagLayout();
		setLayout(layout_gridBag);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;

		// setFont(new Font("SansSerif", Font.PLAIN, 14));

		gbc.gridwidth = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		layout_gridBag.setConstraints(com_schemaPath, gbc);
		add(com_schemaPath);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.5;
		layout_gridBag.setConstraints(com_SchemaField, gbc);
		add(com_SchemaField);

		gbc.gridwidth = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.LAST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		layout_gridBag.setConstraints(com_OutputPath, gbc);
		add(com_OutputPath);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.LAST_LINE_END;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.5;
		layout_gridBag.setConstraints(com_OutputField, gbc);
		add(com_OutputField);

		final JPanel buttPanel = new JPanel();
		buttPanel.setLayout(new GridLayout(0, 2));
		buttPanel.add(com_selectAll);
		buttPanel.add(com_deSelectAll);

		gbc.insets = new Insets(2, 0, 0, 0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		layout_gridBag.setConstraints(buttPanel, gbc);
		add(buttPanel);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		layout_gridBag.setConstraints(com_Generate, gbc);
		add(com_Generate);
	}

	public ComplexTypeList getComplexTypeList()
	{
		return m_motherComp;
	}

	public Vector getVCore()
	{
		return m_vCore;
	}

	public ArrayList getSchemaDocs()
	{
		return m_docs;
	}

	public void setVCore(final Vector v)
	{
		m_vCore = v;
		com_Generate.setEnabled(true);
	}

	public boolean schemaDocsReady()
	{
		return bSchemaDocsReady;
	}

	public JTextField getOutputField()
	{
		return com_OutputField;
	}

	public void setBoolOutputPathSet(final boolean b)
	{
		bOutputPathSet = b;
	}

	public void setGenerateButtonEnabled(final boolean enabled)
	{
		com_Generate.setEnabled(enabled);
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		if (e.getSource() == com_selectAll)
		{
			final int iMax = ComplexTypeList.getDefaultListModel().size();
			final int[] iSelEntrys = new int[iMax];
			for (int i = 0; i < iMax; i++)
			{
				iSelEntrys[i] = i;
			}
			m_motherComp.getList().setSelectedIndices(iSelEntrys);
		}
		if (e.getSource() == com_deSelectAll)
		{
			m_motherComp.getList().clearSelection();
		}
		if (e.getSource() == com_Generate)
		{
			generate();
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e)
	{
		{
			if ((e.getSource() == com_schemaPath) || (e.getSource() == com_SchemaField))
			{
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setCurrentDirectory(new File(this.getComplexTypeList().getMainFrame().getDefaultSchemaLocation()));
				final int returnVal = fc.showOpenDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					final File file = fc.getSelectedFile();
					final String strBuff = file.getAbsolutePath();
					schemaFile_JDFTypes = new File(strBuff + fileSep + "JDFTypes.xsd");
					schemaFile_JDFResource = new File(strBuff + fileSep + "JDFResource.xsd");
					schemaFile_JDFResourceElems = new File(strBuff + fileSep + "JDFResourceElements.xsd");
					schemaFile_JDFMessage = new File(strBuff + fileSep + "JDFMessage.xsd");
					schemaFile_JDFCore = new File(strBuff + fileSep + "JDFCore.xsd");
					schemaFile_JDFCapability = new File(strBuff + fileSep + "JDFCapability.xsd");

					if (schemaFile_JDFTypes.canRead() && schemaFile_JDFResource.canRead() && schemaFile_JDFMessage.canRead() && schemaFile_JDFCore.canRead()
							&& schemaFile_JDFCapability.canRead())
					{
						if (!strBuff.equals(strSchemaPath))
						{
							strSchemaPath = strBuff;
							com_SchemaField.setText(strSchemaPath);
							com_SchemaField.setToolTipText(strSchemaPath);
							com_SchemaField.setCaretPosition(0);
							bSchemaPathSet = true;
							getComplexTypeList().getMainFrame().getSerializeDataVector().setEnabled(false);
							com_Generate.setEnabled(false);

							final ArrayList schemaFiles = new ArrayList();
							schemaFiles.add(schemaFile_JDFTypes);
							schemaFiles.add(schemaFile_JDFResource);
							schemaFiles.add(schemaFile_JDFResourceElems);
							schemaFiles.add(schemaFile_JDFMessage);
							schemaFiles.add(schemaFile_JDFCore);
							schemaFiles.add(schemaFile_JDFCapability);

							schemaFiles.addAll(m_motherComp.getMainFrame().getExternalXSDs());
							final Object[] f = schemaFiles.toArray();
							loadFiles(f, true);
						}
					}
					else
					{
						JOptionPane.showMessageDialog(this, "Schema files not found in " + strBuff, "File not found error", JOptionPane.ERROR_MESSAGE);
						bSchemaPathSet = false;
						bParsedReady = false;
					}

					if (bParsedReady && bSchemaPathSet && bOutputPathSet)
					{
						com_Generate.setEnabled(true);
					}
				}
			}

			if ((e.getSource() == com_OutputPath) || (e.getSource() == com_OutputField))
			{
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setCurrentDirectory(new File(this.getComplexTypeList().getMainFrame().getDefaultOutputLocation()));
				final int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					final File file = fc.getSelectedFile();
					if (file.canWrite())
					{
						strOutputPath = file.getAbsolutePath();
						com_OutputField.setText(strOutputPath);
						com_OutputField.setToolTipText(strOutputPath);
						com_OutputField.setCaretPosition(0);

						Generator.m_strJdfCoreJava = strOutputPath + fileSep + "Java" + fileSep + "auto";
						Generator.m_strJdfCoreCpp = strOutputPath + fileSep + "Cpp" + fileSep + "auto";
						Generator.m_strJdfLostAndFound = strOutputPath + fileSep + "JDFLibGeneratorOutput" + fileSep + "LostAndFound";

						bOutputPathSet = true;
					}
					else
					{
						JOptionPane.showMessageDialog(this, "Cant write to " + strOutputPath, "Write access error", JOptionPane.ERROR_MESSAGE);
						bOutputPathSet = false;
					}

					if (bParsedReady && bSchemaPathSet && bOutputPathSet)
					{
						com_Generate.setEnabled(true);
					}
				}
			}
		}
	}

	protected Vector removeElementsNotInComplexTypeList(final Vector v)
	{
		final Vector buff = new Vector();
		final DefaultListModel dtm = ComplexTypeList.getDefaultListModel();
		final boolean all = dtm.contains("*");
		if (all)
		{
			return v;
		}

		for (int i = 0; i < dtm.size(); i++)
		{
			String strListElement = (String) dtm.getElementAt(i);
			strListElement = strListElement.substring(7);
			strListElement = strListElement.substring(0, strListElement.length() - 5);
			for (int j = 0; j < v.size(); j++)
			{
				final String strComplexTypeName = ((SchemaComplexType) v.elementAt(j)).m_SchemaComplexTypeName;
				if (strComplexTypeName.equals(strListElement))
				{
					buff.add(v.elementAt(j));
				}
			}
		}
		return buff;
	}

	private void generate()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				com_Generate.setEnabled(false);
				setPriority(1);
				allNamesToOrganize.clear();
				final GeneratorUI mainFrame = getComplexTypeList().getMainFrame();

				// copy all complex types which correspond to the selection from m_vCore into m_vToGenerate
				m_vToGenerate = new ArrayList();
				final Object[] selectedClasses = mainFrame.getComplexTypeList().getList().getSelectedValues();

				for (int i = 0; i < selectedClasses.length; i++)
				{
					String strClassName = (String) selectedClasses[i];
					if (!"*".equals(strClassName))
					{
						strClassName = strClassName.substring(7);
						strClassName = strClassName.substring(0, strClassName.length() - 5);
					}
					for (int j = 0; j < m_vCore.size(); j++)
					{
						final SchemaComplexType schemaComplexType = (SchemaComplexType) m_vCore.elementAt(j);
						final String strComplexTypeName = schemaComplexType.m_SchemaComplexTypeName;
						if ("*".equals(strClassName) || strComplexTypeName.equals(strClassName))
						{
							m_vToGenerate.add(schemaComplexType);
						}
					}
				}

				final String s = mainFrame.getTitle();
				int iGenerateCount = selectedClasses.length;
				mainFrame.setTitle(s + " - " + iGenerateCount + " left to generate");

				for (int i = 0; i < m_vToGenerate.size(); i++)
				{
					// m_vBuffer is a workaround so we dont need to change the
					// command shell generator
					m_vBuffer.add(m_vToGenerate.get(i));

					// get the file strNameOfFile to generate
					final DefaultTableModel dtm = mainFrame.getStatusPanel().getDefaultTableModel();
					final String strNameOfFile = ((SchemaComplexType) m_vToGenerate.get(i)).m_SchemaComplexTypeName;
					dtm.insertRow(0, new Object[] { "Generating " + strNameOfFile, "Working..." });

					final int iIndex1 = dtm.getRowCount();
					allNamesToOrganize.add(Generator.m_strJdfCoreJava + fileSep + "JDFAuto" + strNameOfFile + ".java");

					// generate the file
					SchemaDoc.toCoreJava(m_vBuffer, true);
					// SchemaDoc.toCoreCpp (m_vBuffer, true);

					final int iIndex2 = dtm.getRowCount();
					try
					{
						dtm.setValueAt("Done", iIndex2 - iIndex1, 1);
						mainFrame.setTitle(s + " - " + iGenerateCount-- + " left to generate");
					}
					catch (final ArrayIndexOutOfBoundsException e)
					{
						e.hashCode(); // to remove e never used warning
					}

					m_vBuffer.removeAllElements();
				}

				mainFrame.setTitle(s);

				// organize imports
				if (mainFrame.organizeImports())
				{
					final OrganizeImports imp = new OrganizeImports();
					imp.organizeImports(allNamesToOrganize, getMe());
				}
				else
				{
					com_Generate.setEnabled(true);
				}
				StringCollector.getAttribs().flush("Attributes");
				StringCollector.getElems().flush("Elements");
			}
		}.start();
	}

	protected ListButtonPanel getMe()
	{
		return this;
	}

	@Override
	public void mousePressed(final MouseEvent e)
	{
		e.getID(); // to remove e never used warning
	}

	@Override
	public void mouseReleased(final MouseEvent e)
	{
		e.getID(); // to remove e never used warning
	}

	@Override
	public void mouseEntered(final MouseEvent e)
	{
		e.getID(); // to remove e never used warning
	}

	@Override
	public void mouseExited(final MouseEvent e)
	{
		e.getID(); // to remove e never used warning
	}

	public Thread loadFiles(final Object[] files, final boolean b)
	{
		final LoadThread l = new LoadThread(files, b);
		l.start();
		return l;
	}

	class LoadThread extends Thread
	{
		private boolean bCollect = false;
		private final Object[] m_schemaFiles;

		public LoadThread(final Object[] files, final boolean bCollectInfos)
		{
			bCollect = bCollectInfos;
			m_schemaFiles = files;
		}

		@Override
		public void run()
		{
			setPriority(1);

			final XMLDoc doc = new XMLDoc("Schema");

			for (int i = 0; i < m_schemaFiles.length; i++)
			{
				final File schemaFile = (File) m_schemaFiles[i];
				parseSchemaFromFileToDoc(schemaFile, doc);
			}

			if (bCollect)
			{
				parseSchemaFromDocToSchemaComplexType(doc);
			}
		}

		/**
         */
		private void parseSchemaFromFileToDoc(final File schemaFile, final XMLDoc doc)
		{
			final String schemaFilePath = schemaFile.getAbsolutePath();

			final DefaultTableModel dtm = getComplexTypeList().getMainFrame().getStatusPanel().getDefaultTableModel();
			dtm.insertRow(0, new Object[] { "Parsing " + schemaFilePath, "Working..." });
			final int iIndex1 = dtm.getRowCount();

			final KElement n = KElement.parseFile(schemaFilePath);
			final VElement vElem = n.getChildElementVector(null, null, null, true, 0, false);
			for (final KElement e : vElem)
			{
				doc.getRoot().copyElement(e, null);
			}

			final int iIndex2 = dtm.getRowCount();
			try
			{
				dtm.setValueAt("Done", iIndex2 - iIndex1, 1);
			}
			catch (final ArrayIndexOutOfBoundsException e)
			{
				e.hashCode(); // to remove e never used warning
			}
		}

		/**
         */
		private void parseSchemaFromDocToSchemaComplexType(final XMLDoc doc)
		{
			final SchemaDoc schemaDoc = new SchemaDoc(doc);

			final DefaultTableModel dtm = getComplexTypeList().getMainFrame().getStatusPanel().getDefaultTableModel();
			dtm.insertRow(0, new Object[] { "Collecting informations", "Working..." });

			Vector vec = schemaDoc.getSchemaInfo("Core", true);

			try
			{
				dtm.setValueAt("Done", 0, 1);
			}
			catch (final ArrayIndexOutOfBoundsException e)
			{
				e.hashCode(); // to remove a never used warning
			}

			if (bParsedReady && bSchemaPathSet && bOutputPathSet)
			{
				com_Generate.setEnabled(true);
			}

			getComplexTypeList().getMainFrame().getSerializeDataVector().setEnabled(true);

			vec = removeElementsNotInComplexTypeList(vec);

			setVCore(vec);

			bParsedReady = true;
			bSchemaDocsReady = true;
		}
	}
}
