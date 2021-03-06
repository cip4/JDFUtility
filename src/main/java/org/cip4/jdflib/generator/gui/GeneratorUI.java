package org.cip4.jdflib.generator.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.cip4.jdflib.generator.Generator;
import org.cip4.jdflib.util.StringUtil;

/**
 * @author matternk
 *
 *         Structure of the Autofile Generator The schema files contain snippets of form xs:complexType and others. The autofile generator only uses these xs:complexType snippets and transfers them
 *         into data of type SchemaComplexType.
 *
 *
 *         Click on field "Schema Path:"
 *
 *         ListButtonPanel.mouseClicked(MouseEvent e) >>> set schema files ListButtonPanel.loadFiles(Object[] files, boolean b) ListButtonPanel.LoadThread.run DocumentJDFImpl doc =
 *         parseSchemaFromFilesToDoc(); (5 sec) "Parsing " + schemaFilePath, "Working..." result stored to ArrayList m_docs and to DocumentJDFImpl doc "Done"
 *
 *         if (bCollect) { parseSchemaFromDocToSchemaComplexType(doc); (5 min.) schemaDoc.setSchemaDoc(doc); v is vector of SchemaComplexType v = parseSchema(schemaDoc); "Collecting informations",
 *         "Working..." doc.getSchemaInfo("Core", true) GeneratorUtil.unifyComplexTypNames(...) removeDuplicates(Vector m_vMyCompleteSchema) "Done" v = removeElementsNotInComplexTypeList(v);
 *         setVCore(v); // set m_vCore (vector of SchemaComplexType of all xs:complexType) }
 *
 *
 *         Selection of files and click on button "Generate"
 *
 *         ListButtonPanel.gemerate().new Thread().run() copy all complex types which correspond to the selection from m_vCore into m_vToGenerate generate all files javaCoreDoc.toCoreJava(m_vBuffer,
 *         true); getStrJavaCoreFile(SchemaComplexType nSchemaComplexType)
 *
 */

public class GeneratorUI extends JFrame implements ActionListener, MouseListener, ItemListener
{
	private static final long serialVersionUID = 1L;

	private static Properties defaultProps = new Properties();

	final public static Map jarClasses = Collections.synchronizedMap(new HashMap(5000));
	final public static Map jarInnerClasses = Collections.synchronizedMap(new HashMap(2000));

	protected ComplexTypeList complexTypeList = null;
	private ResourceTextPane resourceTextPane = null;
	private StatusTable statusTable = null;
	private ButtonGroup buttonGroup = null;
	private JScrollPane jsp_resourceTextPane = null;
	private JScrollPane jsp_statusPane = null;

	private final JMenuBar menuBar = new JMenuBar();
	private final JMenu tools = new JMenu("Tools");
	private final JMenu menu_LookandFeel = new JMenu("Look and Feel");
	private final JMenuItem menuItem_serializeData = new JMenuItem("Serialize Datavector");
	protected JMenuItem menuItem_unserializeData = new JMenuItem("Unserialize Datavector");
	private final JMenuItem menuItem_defaults = new JMenuItem("Defaults...");
	private final JCheckBoxMenuItem menuItem_OrganizeImports = new JCheckBoxMenuItem("Organize Imports");
	private final JMenuItem menuItem_addExternalJar = new JMenuItem("Add external jars...");
	private final JMenuItem menuItem_addExternalXML = new JMenuItem("Add external xsds...");

	private final JMenuItem menuItem_exit = new JMenuItem("Exit");

	private JSplitPane splitPane = new JSplitPane();
	private JSplitPane splitPane2 = new JSplitPane();

	private String m_defaultSchemaLocation = "";
	private String m_defaultOutputLocation = "";
	protected String m_defaultSerialLocation = "";
	protected String m_serializedSchemaFileName = "";
	private String m_defaultFileListPath = "";
	protected String fileSep = System.getProperty("file.separator");

	private boolean bPaneMaxed = false;
	private boolean bOrganizeImports = false;

	private int iSPane2DivLoc = 100;
	private int iSPane1DivLoc = 100;

	private final ArrayList externalJars = new ArrayList();
	private final ArrayList externalXSDs = new ArrayList();

	public GeneratorUI(final String strName)
	{
		super(strName);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent event)
			{
				event.getID(); // remove event never use warning
				saveProperties();
				System.exit(0);
			}
		});
		JFrame.setDefaultLookAndFeelDecorated(true);
		// setIconImage(null)
		init();
	}

	private void init()
	{
		complexTypeList = new ComplexTypeList(this);

		resourceTextPane = new ResourceTextPane(this);
		jsp_resourceTextPane = new JScrollPane(resourceTextPane);
		jsp_resourceTextPane.setWheelScrollingEnabled(true);
		resourceTextPane.addMouseListener(this);
		resourceTextPane.setPreferredSize(new Dimension(600, 500));

		statusTable = new StatusTable(this);
		jsp_statusPane = new JScrollPane(statusTable);
		jsp_statusPane.setWheelScrollingEnabled(true);
		jsp_statusPane.setPreferredSize(new Dimension(600, 100));

		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(complexTypeList);
		splitPane.setRightComponent(jsp_resourceTextPane);
		splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane2.setLeftComponent(splitPane);
		splitPane2.setRightComponent(jsp_statusPane);
		splitPane2.setDividerLocation(0.8);
		getContentPane().add(splitPane2, BorderLayout.CENTER);

		menuItem_serializeData.addActionListener(this);
		menuItem_serializeData.setEnabled(false);
		menuItem_unserializeData.addActionListener(this);
		menuItem_unserializeData.setEnabled(false);
		menuItem_exit.addActionListener(this);
		menuItem_defaults.addActionListener(this);
		menuItem_OrganizeImports.addActionListener(this);
		menuItem_addExternalJar.addActionListener(this);
		menuItem_addExternalXML.addActionListener(this);
		menuItem_OrganizeImports.addItemListener(this);

		tools.add(menuItem_serializeData);
		tools.add(menuItem_unserializeData);
		tools.addSeparator();
		addInstalledLookandFeels();
		tools.add(menu_LookandFeel);
		tools.add(menuItem_defaults);
		tools.addSeparator();
		menuItem_OrganizeImports.setSelected(false);
		menuItem_OrganizeImports.setEnabled(true);
		tools.add(menuItem_OrganizeImports);
		tools.add(menuItem_addExternalJar);
		tools.add(menuItem_addExternalXML);
		tools.addSeparator();
		tools.add(menuItem_exit);
		menuBar.add(tools);
		setJMenuBar(menuBar);

		loadProperties();
	}

	public ComplexTypeList getComplexTypeList()
	{
		return complexTypeList;
	}

	public void addExternalJar(final File f)
	{
		externalJars.add(f);
	}

	public void removeExternalJar(final int i)
	{
		if (i < externalJars.size())
		{
			externalJars.remove(i);
		}
	}

	public ArrayList getExternalJars()
	{
		return externalJars;
	}

	public boolean organizeImports()
	{
		return bOrganizeImports;
	}

	public ResourceTextPane getResourceTextPane()
	{
		return resourceTextPane;
	}

	public StatusTable getStatusPanel()
	{
		return statusTable;
	}

	public String getDefaultSchemaLocation()
	{
		return m_defaultSchemaLocation;
	}

	public String getDefaultOutputLocation()
	{
		return m_defaultOutputLocation;
	}

	public String getDefaultCompilierLocation()
	{
		return m_defaultFileListPath;
	}

	public JMenuItem getSerializeDataVector()
	{
		return menuItem_serializeData;
	}

	public JMenuItem getUnserializeDataVector()
	{
		return menuItem_unserializeData;
	}

	public static void main(final String args[])
	{
		final GeneratorUI gen = new GeneratorUI("Generator");
		gen.getJarFiles(true, new ArrayList());
		final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		final int genHeight = gen.getPreferredSize().height;
		final int genWidth = gen.getPreferredSize().width;
		gen.setBounds((d.width / 2) - genWidth / 2, (d.height / 2) - genHeight / 2, genWidth, genHeight);
		gen.pack();
		gen.setVisible(true);

	}

	private void addInstalledLookandFeels()
	{
		buttonGroup = new ButtonGroup();
		final UIManager.LookAndFeelInfo[] laf = UIManager.getInstalledLookAndFeels();
		boolean lafSelected = false;
		for (int i = 0; i < laf.length; i++)
		{
			final JRadioButtonMenuItem mnuItem = new JRadioButtonMenuItem(laf[i].getName());
			mnuItem.setActionCommand(laf[i].getClassName());
			if (!lafSelected && laf[i].getClassName().equals(UIManager.getLookAndFeel().getClass().getName()))
			{
				lafSelected = true;
				mnuItem.setSelected(true);
			}
			mnuItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					try
					{
						UIManager.setLookAndFeel(e.getActionCommand());
						SwingUtilities.updateComponentTreeUI(GeneratorUI.this);
						pack();
					}
					catch (final ClassNotFoundException cnfe)
					{/* do nothing */
					}
					catch (final InstantiationException ine)
					{/* do nothing */
					}
					catch (final IllegalAccessException ile)
					{/* do nothing */
					}
					catch (final UnsupportedLookAndFeelException ulafe)
					{/* do nothing */
					}

				}
			});
			buttonGroup.add(mnuItem);
			menu_LookandFeel.add(mnuItem);
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		if (e.getSource() == menuItem_unserializeData)
		{
			getSerializedVector();
		}

		if (e.getSource() == menuItem_addExternalJar)
		{
			final AddJarDialog jar = new AddJarDialog(this);
			jar.pack();
			jar.setLocationRelativeTo(this);
			jar.setVisible(true);
		}

		if (e.getSource() == menuItem_addExternalXML)
		{
			final AddXSDDialog jar = new AddXSDDialog(this);
			jar.pack();
			jar.setLocationRelativeTo(this);
			jar.setVisible(true);
		}

		if (e.getSource() == menuItem_serializeData)
		{
			new Thread()
			{

				@Override
				public void run()
				{
					try
					{
						getStatusPanel().getDefaultTableModel().insertRow(0, new Object[] { "Serializing " + m_serializedSchemaFileName, "Working..." });
						final Vector v = getComplexTypeList().getListButtonPanel().getVCore();
						final String fileName = m_defaultSerialLocation + fileSep + m_serializedSchemaFileName;
						final ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
						objOut.writeObject(v);
						objOut.close();
						try
						{
							getStatusPanel().getDefaultTableModel().setValueAt("Done", 0, 1);
						}
						catch (final ArrayIndexOutOfBoundsException aioobe)
						{/* do nothing */
						}
						menuItem_unserializeData.setEnabled(true);
					}
					catch (final FileNotFoundException fnfe)
					{
						fnfe.printStackTrace();
					}
					catch (final IOException ioe)
					{
						ioe.printStackTrace();
					}
				}
			}.start();
		}
		if (e.getSource() == menuItem_exit)
		{
			saveProperties();
			System.exit(0);
		}
		if (e.getSource() == menuItem_defaults)
		{
			String[] paths = new String[] { m_defaultSchemaLocation, m_defaultOutputLocation, m_defaultSerialLocation, m_defaultFileListPath };

			String strVer = "";
			if (m_serializedSchemaFileName != null && !m_serializedSchemaFileName.equals(""))
			{
				strVer = m_serializedSchemaFileName.substring(6, 8);
			}
			final DefaultsDialog dd = new DefaultsDialog(this, paths, strVer);
			dd.setLocationRelativeTo(this);
			dd.pack();
			dd.setResizable(false);
			dd.setLocationRelativeTo(this);
			dd.setVisible(true);
			paths = dd.getNewlySelectedPaths();

			m_defaultSchemaLocation = paths[0];
			m_defaultOutputLocation = paths[1];
			m_defaultSerialLocation = paths[2];
			String buff = paths[3];
			m_defaultFileListPath = paths[4];

			buff = buff.substring(8, buff.length());
			setTitle("Generator - Version " + buff);
			m_serializedSchemaFileName = "schema" + buff + ".ser";
			final File f = new File(m_defaultSerialLocation + fileSep + m_serializedSchemaFileName);
			if (f.canRead())
			{
				menuItem_unserializeData.setEnabled(true);
			}
			else
			{
				menuItem_unserializeData.setEnabled(false);
			}
			if (m_defaultOutputLocation != null && !m_defaultOutputLocation.equals(""))
			{
				getComplexTypeList().getListButtonPanel().getOutputField().setText(m_defaultOutputLocation);
				getComplexTypeList().getListButtonPanel().getOutputField().setToolTipText(m_defaultOutputLocation);
				getComplexTypeList().getListButtonPanel().getOutputField().setCaretPosition(0);

			}

		}
	}

	@Override
	public void mouseClicked(final MouseEvent e)
	{
		final int iClick = e.getClickCount();

		if (iClick % 2 == 0)
		{
			getContentPane().removeAll();
			if (bPaneMaxed)
			{
				jsp_resourceTextPane = new JScrollPane(resourceTextPane);

				splitPane = new JSplitPane();
				splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				splitPane.setLeftComponent(complexTypeList);
				splitPane.setRightComponent(jsp_resourceTextPane);

				splitPane2 = new JSplitPane();
				splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane2.setLeftComponent(splitPane);
				splitPane2.setRightComponent(jsp_statusPane);

				splitPane.setDividerLocation(iSPane1DivLoc);
				splitPane2.setDividerLocation(iSPane2DivLoc);

				getContentPane().add(splitPane2, BorderLayout.CENTER);

				bPaneMaxed = false;
				validate();
			}
			else
			{
				iSPane2DivLoc = splitPane2.getDividerLocation();
				iSPane1DivLoc = splitPane.getDividerLocation();

				if ((Component) e.getSource() == resourceTextPane)
				{
					getContentPane().add(jsp_resourceTextPane);
				}
				bPaneMaxed = true;
				validate();
			}
		}
		repaint();
	}

	private void getSerializedVector()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				getStatusPanel().getDefaultTableModel().insertRow(0, new Object[] { "Deserializing " + m_serializedSchemaFileName, "Working..." });
				setPriority(1);
				final File f = new File(m_defaultSerialLocation + fileSep + m_serializedSchemaFileName);
				if (f.canRead())
				{
					try
					{
						final FileInputStream fis = new FileInputStream(f);
						final ObjectInputStream ois = new ObjectInputStream(fis);
						final Vector v = (Vector) ois.readObject();
						ois.close();
						complexTypeList.getListButtonPanel().setVCore(v);
						getStatusPanel().getDefaultTableModel().setValueAt("Done", 0, 1);
					}
					catch (final IOException ioe)
					{
						ioe.printStackTrace();
					}
					catch (final ClassNotFoundException cnfe)
					{
						cnfe.printStackTrace();
					}
					catch (final ArrayIndexOutOfBoundsException e)
					{/* do nothing */
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Cant find " + m_defaultSerialLocation + fileSep + m_serializedSchemaFileName, "File error", JOptionPane.ERROR_MESSAGE);
					getStatusPanel().getDefaultTableModel().setValueAt("Error", 0, 1);
				}
			}
		}.start();
	}

	private void loadProperties()
	{
		InputStream propsFile = null;
		try
		{
			final Properties tempProp = new Properties(defaultProps);
			propsFile = new FileInputStream("generatorUI.props");
			tempProp.load(propsFile);

			m_defaultSchemaLocation = (String) tempProp.get("SchemaPath");
			m_defaultOutputLocation = (String) tempProp.get("OutputPath");

			Generator.m_strJdfCoreJava = m_defaultOutputLocation + fileSep + "Java" + fileSep + "auto";
			Generator.m_strJdfCoreCpp = m_defaultOutputLocation + fileSep + "Cpp" + fileSep + "auto";

			m_defaultSerialLocation = (String) tempProp.get("SerializePath");
			m_defaultFileListPath = (String) tempProp.get("FileListPath");
			m_serializedSchemaFileName = (String) tempProp.get("Version");

			int i = 0;
			String strLib = "";
			while (strLib != null)
			{
				strLib = (String) tempProp.get("Lib" + i);
				i++;
				if (strLib != null)
				{
					final File f = new File(strLib);
					if (f.isFile() && f.canRead())
					{
						externalJars.add(f);
					}
				}
			}

			i = 0;
			String strXSD = "";
			while (strXSD != null)
			{
				strXSD = (String) tempProp.get("XSD" + i);
				i++;
				if (strXSD != null)
				{
					final File f = new File(strXSD);
					if (f.isFile() && f.canRead())
					{
						externalXSDs.add(f);
					}
				}
			}
		}
		catch (final FileNotFoundException ioe)
		{
			m_defaultSchemaLocation = "../schema/Version_1_5";
			final File schema = new File(m_defaultSchemaLocation);
			try
			{
				m_defaultSchemaLocation = schema.getCanonicalPath();
			}
			catch (final IOException e)
			{
				m_defaultSchemaLocation = "../schema";
			}

			m_defaultOutputLocation = fileSep + "JDFLibGenerator";
			Generator.m_strJdfCoreJava = m_defaultOutputLocation + fileSep + "Java" + fileSep + "auto";
			Generator.m_strJdfCoreCpp = m_defaultOutputLocation + fileSep + "Cpp" + fileSep + "auto";
		}
		catch (final IOException e)
		{/* do nothing */
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if (propsFile != null)
			{
				try
				{
					propsFile.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		if (m_serializedSchemaFileName.length() >= 10)
		{
			final String buff = m_serializedSchemaFileName.substring(6, 8);
			setTitle("Generator - Version " + buff);
		}

		final File f = new File(m_defaultSerialLocation + fileSep + m_serializedSchemaFileName);
		if (f.canRead() && f.isFile())
		{
			menuItem_unserializeData.setEnabled(true);
		}
		else
		{
			menuItem_unserializeData.setEnabled(false);
		}

		getComplexTypeList().getListButtonPanel().getOutputField().setText(m_defaultOutputLocation);
		getComplexTypeList().getListButtonPanel().getOutputField().setToolTipText(m_defaultOutputLocation);
		getComplexTypeList().getListButtonPanel().getOutputField().setCaretPosition(0);

		final File fileList = new File(m_defaultFileListPath);
		if (fileList.canRead() && fileList.isFile())
		{
			getComplexTypeList().loadFileList(fileList);
		}
	}

	public void saveProperties()
	{
		try
		{
			final Properties props = new Properties();
			props.setProperty("SchemaPath", m_defaultSchemaLocation);
			props.setProperty("OutputPath", m_defaultOutputLocation);
			props.setProperty("SerializePath", m_defaultSerialLocation);
			props.setProperty("FileListPath", m_defaultFileListPath);
			props.setProperty("Version", m_serializedSchemaFileName);

			for (int i = 0; i < externalJars.size(); i++)
			{
				props.setProperty("Lib" + i, ((File) externalJars.get(i)).getAbsolutePath());
			}

			for (int i = 0; i < externalXSDs.size(); i++)
			{
				props.setProperty("XSD" + i, ((File) externalXSDs.get(i)).getAbsolutePath());
			}

			OutputStream stream = null;
			try
			{
				stream = new FileOutputStream("generatorUI.props");
				props.store(stream, "Properties File for the Generator GUI");
			}
			catch (final FileNotFoundException e)
			{/* do nothing */
			}
			finally
			{
				if (stream != null)
					stream.close();
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void mousePressed(final MouseEvent e)
	{
		e.getID(); // remove e never used warning;
	}

	@Override
	public void mouseReleased(final MouseEvent e)
	{
		e.getID(); // remove e never used warning;}
	}

	@Override
	public void mouseEntered(final MouseEvent e)
	{
		e.getID(); // remove e never used warning;}
	}

	@Override
	public void mouseExited(final MouseEvent e)
	{
		e.getID(); // remove e never used warning;
	}

	@Override
	public void itemStateChanged(final ItemEvent e)
	{
		if (e.getSource() == menuItem_OrganizeImports)
		{
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				bOrganizeImports = true;
			}
			else
			{
				bOrganizeImports = false;
			}
		}
	}

	/**
	 *
	 * @param internalScan true if the source was internal
	 * @param jarFiles array of File
	 */
	public void getJarFiles(final boolean internalScan, final ArrayList jarFiles)
	{
		final ArrayList jarFileList = jarFiles;

		if (internalScan)
		{
			jarFileList.addAll(getExtJars());
			jarFileList.addAll(getClassPathJars());
			jarFileList.addAll(externalJars);
		}

		// we got all jars, now its time to open the tree
		for (int i = 0; i < jarFileList.size(); i++)
		{
			try
			{
				// namesOfAllClasses
				final File f = (File) jarFileList.get(i);
				final ZipFile zippi = new ZipFile(f);
				final Enumeration enu = zippi.entries();
				while (enu.hasMoreElements())
				{
					String strClassName = ((ZipEntry) enu.nextElement()).toString();
					if (strClassName.endsWith(".class"))
					{
						strClassName = strClassName.substring(0, strClassName.length() - 6);
						// TODO whats up with this damn file separator?
						final String toReplace = fileSep;
						if (toReplace.equals("\\") && strClassName.indexOf("\\") != -1)
						{
							strClassName = strClassName.replaceAll("\\\\", ".");
						}
						else if (strClassName.indexOf("/") != -1)
						{
							strClassName = strClassName.replaceAll("/", ".");
						}
						else
						{
							strClassName = strClassName.replaceAll(toReplace, ".");
						}

						final int iLast = strClassName.lastIndexOf(".");
						final String key = strClassName.substring(iLast + 1, strClassName.length());
						if (strClassName.indexOf("$") == -1)
						{
							jarClasses.put(key, strClassName);
						}
						else
						{
							jarInnerClasses.put(key, strClassName);
						}
					}
				}
			}
			catch (final IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
		setAmbiguousImportantClasses();
	}

	private ArrayList getClassPathJars()
	{
		final ArrayList jarFileList = new ArrayList();
		// also add all jars in the system property "sun.boot.class.path"
		final String strSunBootClashPath = System.getProperty("sun.boot.class.path");
		if (!StringUtil.isEmpty(strSunBootClashPath))
		{
			final StringTokenizer sto = new StringTokenizer(strSunBootClashPath, ";");
			while (sto.hasMoreTokens())
			{
				final String strJarListToken = sto.nextToken();
				final File f = new File(strJarListToken);
				if (f.isFile() && f.canRead() && f.getName().endsWith(".jar"))
				{
					jarFileList.add(f);
				}
			}
		}
		return jarFileList;
	}

	private ArrayList getExtJars()
	{
		final ArrayList libList = new ArrayList();
		final ArrayList jarFileList = new ArrayList();
		// scan dir external dirs for jars
		final String strLibPath = System.getProperty("java.ext.dirs");
		if (!StringUtil.isEmpty(strLibPath))
		{
			if (strLibPath.indexOf(";") != -1)
			{
				final StringTokenizer st = new StringTokenizer(strLibPath, ";");
				while (st.hasMoreTokens())
				{
					libList.add(st.nextToken());
				}
			}
			else
			{
				libList.add(strLibPath);
			}
		}
		for (int i = 0; i < libList.size(); i++)
		{
			final File f = new File((String) libList.get(i));
			if (f.isDirectory() && f.canRead())
			{
				final String[] strFileList = f.list();
				for (int j = 0; j < strFileList.length; j++)
				{
					final File xfile = new File(f.getAbsolutePath() + fileSep + strFileList[j]);
					if (xfile.getName().endsWith(".jar") && xfile.isFile() && xfile.canRead())
					{
						jarFileList.add(xfile);
					}
				}
			}
		}
		return jarFileList;
	}

	public void addExternalXSD(final File f)
	{
		externalXSDs.add(f);
	}

	public ArrayList getExternalXSDs()
	{
		return externalXSDs;
	}

	public void removeExternalXSDs(final int iIndex)
	{
		externalXSDs.remove(iIndex);
	}

	private void setAmbiguousImportantClasses()
	{
		jarClasses.put("Map", "java.util.Map");
		jarClasses.put("List", "java.util.List");
	}

	static
	{
		defaultProps.setProperty("SchemaPath", "");
		defaultProps.setProperty("OutputPath", "");
		defaultProps.setProperty("SerializePath", "");
		defaultProps.setProperty("FileListPath", "");
		defaultProps.setProperty("Version", "");
	}
}
