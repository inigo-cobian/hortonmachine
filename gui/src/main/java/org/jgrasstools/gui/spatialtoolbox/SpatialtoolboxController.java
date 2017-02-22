/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.gui.spatialtoolbox;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.jgrasstools.gears.io.geopaparazzi.geopap4.TimeUtilities;
import org.jgrasstools.gears.libs.logging.JGTLogger;
import org.jgrasstools.gears.utils.DataUtilities;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gui.console.ProcessLogConsoleController;
import org.jgrasstools.gui.spatialtoolbox.core.JGrasstoolsModulesManager;
import org.jgrasstools.gui.spatialtoolbox.core.ModuleDescription;
import org.jgrasstools.gui.spatialtoolbox.core.ParametersPanel;
import org.jgrasstools.gui.spatialtoolbox.core.SpatialToolboxConstants;
import org.jgrasstools.gui.spatialtoolbox.core.StageScriptExecutor;
import org.jgrasstools.gui.spatialtoolbox.core.ViewerFolder;
import org.jgrasstools.gui.spatialtoolbox.core.ViewerModule;
import org.jgrasstools.gui.utils.DefaultGuiBridgeImpl;
import org.jgrasstools.gui.utils.GuiBridgeHandler;
import org.jgrasstools.gui.utils.GuiUtilities;
import org.jgrasstools.gui.utils.GuiUtilities.IOnCloseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oms3.annotations.Out;

/**
 * The spatialtoolbox view controller.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class SpatialtoolboxController extends SpatialtoolboxView implements IOnCloseListener {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SpatialtoolboxController.class);

    protected ParametersPanel pPanel;

    protected HashMap<String, String> prefsMap = new HashMap<>();

    private GuiBridgeHandler guiBridge;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SpatialtoolboxController( GuiBridgeHandler guiBridge ) {
        this.guiBridge = guiBridge;
        setPreferredSize(new Dimension(900, 600));

        HashMap<String, String> prefsMapTmp = guiBridge.getSpatialToolboxPreferencesMap();
        if (prefsMapTmp != null) {
            prefsMap = (HashMap) prefsMapTmp;
        }

        init();
    }

    protected void preInit() {

    }

    @SuppressWarnings({"unchecked", "serial"})
    private void init() {
        preInit();

        Class<SpatialtoolboxController> class1 = SpatialtoolboxController.class;
        ImageIcon processingIcon = new ImageIcon(class1.getResource("/org/jgrasstools/images/processingregion.png"));
        ImageIcon startIcon = new ImageIcon(class1.getResource("/org/jgrasstools/images/start.gif"));
        ImageIcon runScriptIcon = new ImageIcon(class1.getResource("/org/jgrasstools/images/run_script.gif"));
        ImageIcon generateScriptIcon = new ImageIcon(class1.getResource("/org/jgrasstools/images/generate_script.gif"));
        ImageIcon trashIcon = new ImageIcon(class1.getResource("/org/jgrasstools/images/trash.gif"));
        final ImageIcon categoryIcon = new ImageIcon(class1.getResource("/org/jgrasstools/images/category.gif"));
        final ImageIcon moduleIcon = new ImageIcon(class1.getResource("/org/jgrasstools/images/module.gif"));
        final ImageIcon moduleExpIcon = new ImageIcon(class1.getResource("/org/jgrasstools/images/module_exp.gif"));

        _parametersPanel.setLayout(new BorderLayout());

        addComponentListener(new ComponentListener(){

            public void componentShown( ComponentEvent e ) {
            }

            public void componentResized( ComponentEvent e ) {
            }

            public void componentMoved( ComponentEvent e ) {
            }

            public void componentHidden( ComponentEvent e ) {
                onClose();
            }
        });

        pPanel = new ParametersPanel(guiBridge);
        addMouseListenerToContext(pPanel);

        pPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollpane = new JScrollPane(pPanel);
        scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _parametersPanel.add(scrollpane, BorderLayout.CENTER);

        _processingRegionButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
            }
        });

        _processingRegionButton.setIcon(processingIcon);

        // TODO enable when used
        _processingRegionButton.setVisible(false);

        _startButton.setToolTipText("Start the current module.");
        _startButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {

                final ProcessLogConsoleController logConsole = new ProcessLogConsoleController();
                guiBridge.showWindow(logConsole.asJComponent(), "Console Log");

                try {
                    runModuleInNewJVM(logConsole);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        _startButton.setIcon(startIcon);

        _runScriptButton.setToolTipText("Run a script from file.");
        _runScriptButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                File[] loadFiles = guiBridge.showOpenFileDialog("Load script", GuiUtilities.getLastFile());
                if (loadFiles != null && loadFiles.length > 0) {
                    try {
                        GuiUtilities.setLastPath(loadFiles[0].getAbsolutePath());
                        String readFile = FileUtilities.readFile(loadFiles[0]);

                        final ProcessLogConsoleController logConsole = new ProcessLogConsoleController();
                        guiBridge.showWindow(logConsole.asJComponent(), "Console Log");

                        StageScriptExecutor exec = new StageScriptExecutor(guiBridge.getLibsFolder());
                        exec.addProcessListener(logConsole);

                        String logLevel = _debugCheckbox.isSelected()
                                ? SpatialToolboxConstants.LOGLEVEL_GUI_ON
                                : SpatialToolboxConstants.LOGLEVEL_GUI_OFF;
                        String ramLevel = _heapCombo.getSelectedItem().toString();

                        String sessionId = "File: " + loadFiles[0].getName() + " - "
                                + TimeUtilities.INSTANCE.TIMESTAMPFORMATTER_LOCAL.format(new Date());
                        Process process = exec.exec(sessionId, readFile, logLevel, ramLevel, null);
                        logConsole.beginProcess(process, sessionId);

                    } catch (Exception e1) {
                        e1.printStackTrace();
                        guiBridge.messageDialog("ERROR", "an error occurred while running the script: " + e1.getMessage(),
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
        });

        _runScriptButton.setIcon(runScriptIcon);

        _generateScriptButton.setToolTipText("Save the current module as a script to file.");
        _generateScriptButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                ModuleDescription module = pPanel.getModule();
                HashMap<String, Object> fieldName2ValueHolderMap = pPanel.getFieldName2ValueHolderMap();
                List<String> outputFieldNames = pPanel.getOutputFieldNames();
                final HashMap<String, String> outputStringsMap = new HashMap<>();
                Class< ? > moduleClass = module.getModuleClass();
                StringBuilder scriptBuilder = getScript(fieldName2ValueHolderMap, outputFieldNames, outputStringsMap,
                        moduleClass);

                File[] saveFiles = guiBridge.showSaveFileDialog("Save script", GuiUtilities.getLastFile());
                if (saveFiles != null && saveFiles.length > 0) {
                    try {
                        GuiUtilities.setLastPath(saveFiles[0].getAbsolutePath());
                        FileUtilities.writeFile(scriptBuilder.toString(), saveFiles[0]);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        _generateScriptButton.setIcon(generateScriptIcon);

        _clearFilterButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                _filterField.setText("");
                layoutTree(false);
            }
        });
        _clearFilterButton.setIcon(trashIcon);

        _loadExperimentalCheckbox.setSelected(true);
        _loadExperimentalCheckbox.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                layoutTree(true);
            }
        });

        boolean doDebug = false;
        String debugStr = prefsMap.get(GuiBridgeHandler.DEBUG_KEY);
        if (debugStr != null && debugStr.trim().length() > 0) {
            doDebug = Boolean.parseBoolean(debugStr);
        }
        _debugCheckbox.setSelected(doDebug);
        _heapCombo.setModel(new DefaultComboBoxModel<>(SpatialToolboxConstants.HEAPLEVELS));
        String heapStr = prefsMap.get(GuiBridgeHandler.HEAP_KEY);
        if (heapStr == null) {
            heapStr = SpatialToolboxConstants.HEAPLEVELS[0];
        }
        _heapCombo.setSelectedItem(heapStr);

        _filterField.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased( KeyEvent e ) {
                layoutTree(true);
            }
        });

        try {
            _modulesTree.setCellRenderer(new DefaultTreeCellRenderer(){
                @Override
                public java.awt.Component getTreeCellRendererComponent( JTree tree, Object value, boolean selected,
                        boolean expanded, boolean leaf, int row, boolean hasFocus ) {

                    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                    if (value instanceof Modules) {
                        setIcon(categoryIcon);
                    } else if (value instanceof ViewerFolder) {
                        setIcon(categoryIcon);
                    } else if (value instanceof ViewerModule) {
                        if (isExperimental(value)) {
                            setIcon(moduleExpIcon);
                        } else {
                            setIcon(moduleIcon);
                        }
                    }
                    return this;
                }

                private boolean isExperimental( Object node ) {
                    if (node instanceof ViewerModule) {
                        ViewerModule module = (ViewerModule) node;
                        ModuleDescription md = module.getModuleDescription();
                        if (md.getStatus() == ModuleDescription.Status.experimental) {
                            return true;
                        }
                    }
                    return false;
                }
            });

            _modulesTree.addTreeSelectionListener(new TreeSelectionListener(){
                public void valueChanged( TreeSelectionEvent evt ) {
                    TreePath[] paths = evt.getPaths();

                    for( int i = 0; i < paths.length; i++ ) {
                        Object lastPathComponent = paths[i].getLastPathComponent();
                        if (lastPathComponent instanceof ViewerModule) {
                            ViewerModule module = (ViewerModule) lastPathComponent;
                            ModuleDescription moduleDescription = module.getModuleDescription();
                            pPanel.setModule(moduleDescription);

                            // SwingUtilities.invokeLater(new Runnable(){
                            // public void run() {
                            // parametersPanel.invalidate();
                            _parametersPanel.validate();
                            _parametersPanel.repaint();
                            // }
                            // });
                            break;
                        }
                        if (lastPathComponent instanceof ViewerFolder) {
                            pPanel.setModule(null);
                            _parametersPanel.validate();
                            _parametersPanel.repaint();
                            break;
                        }
                    }
                }
            });

            layoutTree(false);
        } catch (Exception e1) {
            JGTLogger.logError(this, "Error", e1);
        }
    }

    private void layoutTree( boolean expandNodes ) {
        TreeMap<String, List<ModuleDescription>> availableModules = JGrasstoolsModulesManager.getInstance().getModulesMap();

        final List<ViewerFolder> viewerFolders = ViewerFolder.hashmap2ViewerFolders(availableModules, _filterField.getText(),
                _loadExperimentalCheckbox.isSelected());
        Modules modules = new Modules();
        modules.viewerFolders = viewerFolders;
        ObjectTreeModel model = new ObjectTreeModel();
        model.setRoot(modules);
        _modulesTree.setModel(model);

        if (expandNodes)
            expandAllNodes(_modulesTree, 0, _modulesTree.getRowCount());
    }

    private void expandAllNodes( JTree tree, int startingIndex, int rowCount ) {
        for( int i = startingIndex; i < rowCount; ++i ) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    class Modules {
        List<ViewerFolder> viewerFolders;
        @Override
        public String toString() {
            if (viewerFolders != null && !viewerFolders.isEmpty()) {
                return "Modules";
            } else {
                return "No modules found";
            }
        }
    }

    class ObjectTreeModel implements TreeModel {

        private Modules root;
        private EventListenerList listenerList = new EventListenerList();
        /**
        * Constructs an empty tree.
        */
        public ObjectTreeModel() {
            root = null;
        }

        /**
        * Sets the root to a given variable.
        * @param v the variable that is being described by this tree
        */
        public void setRoot( Modules v ) {
            Modules oldRoot = v;
            root = v;
            fireTreeStructureChanged(oldRoot);
        }

        public Object getRoot() {
            return root;
        }

        @SuppressWarnings("rawtypes")
        public int getChildCount( Object parent ) {
            if (parent instanceof Modules) {
                Modules modules = (Modules) parent;
                return modules.viewerFolders.size();
            } else if (parent instanceof ViewerFolder) {
                ViewerFolder folder = (ViewerFolder) parent;
                return folder.getModules().size() + folder.getSubFolders().size();
            } else if (parent instanceof List) {
                List list = (List) parent;
                return list.size();
            }
            return 0;
        }

        @SuppressWarnings("rawtypes")
        public Object getChild( Object parent, int index ) {
            if (parent instanceof Modules) {
                Modules modules = (Modules) parent;
                return modules.viewerFolders.get(index);
            } else if (parent instanceof ViewerFolder) {
                ViewerFolder folder = (ViewerFolder) parent;
                int modulesSize = folder.getModules().size();
                if (index < modulesSize) {
                    return folder.getModules().get(index);
                } else {
                    index = index - modulesSize;
                    return folder.getSubFolders().get(index);
                }
            } else if (parent instanceof List) {
                List list = (List) parent;
                Object item = list.get(index);
                return item;
            }
            return null;
        }

        public int getIndexOfChild( Object parent, Object child ) {
            int n = getChildCount(parent);
            for( int i = 0; i < n; i++ )
                if (getChild(parent, i).equals(child))
                    return i;
            return -1;
        }

        public boolean isLeaf( Object node ) {
            return getChildCount(node) == 0;
        }

        public void valueForPathChanged( TreePath path, Object newValue ) {
        }

        public void addTreeModelListener( TreeModelListener l ) {
            listenerList.add(TreeModelListener.class, l);
        }

        public void removeTreeModelListener( TreeModelListener l ) {
            listenerList.remove(TreeModelListener.class, l);
        }

        protected void fireTreeStructureChanged( Object oldRoot ) {
            TreeModelEvent event = new TreeModelEvent(this, new Object[]{oldRoot});
            EventListener[] listeners = listenerList.getListeners(TreeModelListener.class);
            for( int i = 0; i < listeners.length; i++ )
                ((TreeModelListener) listeners[i]).treeStructureChanged(event);
        }

    }

    public JComponent asJComponent() {
        return this;
    }

    public void onClose() {
        String ramLevel = _heapCombo.getSelectedItem().toString();
        prefsMap.put(GuiBridgeHandler.DEBUG_KEY, _debugCheckbox.isSelected() + "");
        prefsMap.put(GuiBridgeHandler.HEAP_KEY, ramLevel);
        guiBridge.setSpatialToolboxPreferencesMap(prefsMap);

        removeMouseListenerFromContext(pPanel);
        if (pPanel != null)
            pPanel.freeResources();
    }

    private void runModuleInNewJVM( ProcessLogConsoleController logConsole ) throws Exception {
        ModuleDescription module = pPanel.getModule();
        HashMap<String, Object> fieldName2ValueHolderMap = pPanel.getFieldName2ValueHolderMap();
        List<String> outputFieldNames = pPanel.getOutputFieldNames();
        final HashMap<String, String> outputStringsMap = new HashMap<>();
        Class< ? > moduleClass = module.getModuleClass();

        StringBuilder scriptBuilder = getScript(fieldName2ValueHolderMap, outputFieldNames, outputStringsMap, moduleClass);

        StageScriptExecutor exec = new StageScriptExecutor(guiBridge.getLibsFolder());
        exec.addProcessListener(logConsole);

        Runnable finishRunnable = new Runnable(){
            public void run() {
                // finished, try to load results
                for( Entry<String, String> outputStringFieldEntry : outputStringsMap.entrySet() ) {
                    try {
                        String value = outputStringFieldEntry.getValue();
                        File file = new File(value);
                        if (file.exists()) {
                            if (DataUtilities.isSupportedVectorExtension(value)) {
                                loadVectorLayer(file);
                            } else if (DataUtilities.isSupportedRasterExtension(value)) {
                                loadRasterLayer(file);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        logConsole.addFinishRunnable(finishRunnable);

        String logLevel = _debugCheckbox.isSelected()
                ? SpatialToolboxConstants.LOGLEVEL_GUI_ON
                : SpatialToolboxConstants.LOGLEVEL_GUI_OFF;
        String ramLevel = _heapCombo.getSelectedItem().toString();

        String sessionId = moduleClass.getSimpleName() + " " + TimeUtilities.INSTANCE.TIMESTAMPFORMATTER_LOCAL.format(new Date());
        Process process = exec.exec(sessionId, scriptBuilder.toString(), logLevel, ramLevel, null);
        logConsole.beginProcess(process, sessionId);
    }

    private StringBuilder getScript( HashMap<String, Object> fieldName2ValueHolderMap, List<String> outputFieldNames,
            final HashMap<String, String> outputStringsMap, Class< ? > moduleClass ) {
        String canonicalName = moduleClass.getCanonicalName();
        String simpleName = moduleClass.getSimpleName();
        String objectName = "_" + simpleName.toLowerCase();

        StringBuilder scriptBuilder = new StringBuilder();
        // TODO check if this is ok
        // scriptBuilder.append("import " + StageScriptExecutor.ORG_JGRASSTOOLS_MODULES +
        // ".*;\n\n");
        // scriptBuilder.append("import " + canonicalName + ";\n\n");

        scriptBuilder.append(canonicalName).append(" ").append(objectName).append(" = new ").append(canonicalName)
                .append("();\n");

        for( Entry<String, Object> entry : fieldName2ValueHolderMap.entrySet() ) {
            try {
                String fieldName = entry.getKey();
                String value = stringFromObject(entry.getValue());

                if (value.trim().length() == 0) {
                    continue;
                }

                // make sure there are no backslashes
                value = FileUtilities.replaceBackSlashesWithSlashes(value);

                scriptBuilder.append(objectName).append(".").append(fieldName).append(" = ");

                Field field = moduleClass.getField(fieldName);
                field.setAccessible(true);
                Class< ? > type = field.getType();
                if (type.isAssignableFrom(String.class)) {
                    scriptBuilder.append("\"").append(value).append("\"");
                    if (outputFieldNames.contains(fieldName)) {
                        outputStringsMap.put(fieldName, value);
                    }
                } else if (type.isAssignableFrom(double.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Double.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(int.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Integer.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(long.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Long.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(float.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Float.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(short.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Short.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(boolean.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Boolean.class)) {
                    scriptBuilder.append(value);
                } else {
                    JGTLogger.logInfo(this, "NOT SUPPORTED TYPE: " + type);
                }
                scriptBuilder.append(";\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scriptBuilder.append(objectName).append(".process();\n");

        scriptBuilder.append("println \"\"\n");
        scriptBuilder.append("println \"\"\n");
        Field[] fields = moduleClass.getDeclaredFields();
        for( Field field : fields ) {
            // If the field is annotated by @ExcelColumn
            try {
                if (field.isAnnotationPresent(Out.class)) {
                    String fieldName = field.getName();
                    field.setAccessible(true);
                    Class< ? > type = field.getType();

                    dumpSimpleOutputs(objectName, type, fieldName, scriptBuilder);
                    // scriptBuilder.append(";\n");
                }

                // scriptBuilder.append(objectName).append(".").append(fieldName).append(" = ");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return scriptBuilder;
    }

    private void dumpSimpleOutputs( String objectName, Class< ? > type, String fieldName, StringBuilder scriptSb ) {
        scriptSb.append("println \"\"\n");
        scriptSb.append("println \"\"\n");

        if (type.isAssignableFrom(String.class) || type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)//
                || type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class) || type.isAssignableFrom(long.class)//
                || type.isAssignableFrom(Long.class) || type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)//
                || type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)
                || type.isAssignableFrom(boolean.class)//
                || type.isAssignableFrom(Boolean.class)) {

            scriptSb.append("println \"");
            scriptSb.append(fieldName);
            scriptSb.append(" = \" + ");
            scriptSb.append(fieldName);
            scriptSb.append("\n");
        }

        // in case make print double[] and double[][] outputs
        scriptSb.append("println \"\"\n\n");
        if (type.isAssignableFrom(double[][].class) || type.isAssignableFrom(float[][].class)
                || type.isAssignableFrom(int[][].class)) {

            String ifString = "if( " + objectName + "." + fieldName + " != null ) {\n";
            scriptSb.append(ifString);
            String typeStr = null;
            if (type.isAssignableFrom(double[][].class)) {
                typeStr = "double[][]";
            } else if (type.isAssignableFrom(float[][].class)) {
                typeStr = "float[][]";
            } else if (type.isAssignableFrom(int[][].class)) {
                typeStr = "int[][]";
            }

            scriptSb.append("println \"");
            scriptSb.append(fieldName);
            scriptSb.append("\"\n");
            scriptSb.append("println \"-----------------------------------\"\n");
            scriptSb.append(typeStr);
            scriptSb.append(" matrix = ");
            scriptSb.append(objectName + "." + fieldName);
            scriptSb.append("\n");

            scriptSb.append("for( int i = 0; i < matrix.length; i++ ) {\n");
            scriptSb.append("for( int j = 0; j < matrix[0].length; j++ ) {\n");
            scriptSb.append("print matrix[i][j] + \" \";\n");
            scriptSb.append("}\n");
            scriptSb.append("println \" \";\n");
            scriptSb.append("}\n");
            scriptSb.append("}\n");
            scriptSb.append("\n");
        } else if (type.isAssignableFrom(double[].class) || type.isAssignableFrom(float[].class)
                || type.isAssignableFrom(int[].class)) {

            String ifString = "if( " + objectName + "." + fieldName + " != null ) {\n";
            scriptSb.append(ifString);

            String typeStr = null;
            if (type.isAssignableFrom(double[].class)) {
                typeStr = "double[]";
            } else if (type.isAssignableFrom(float[].class)) {
                typeStr = "float[]";
            } else if (type.isAssignableFrom(int[].class)) {
                typeStr = "int[]";
            }
            scriptSb.append("println \"");
            scriptSb.append(fieldName);
            scriptSb.append("\"\n");
            scriptSb.append("println \"-----------------------------------\"\n");
            scriptSb.append(typeStr);
            scriptSb.append(" array = ");
            scriptSb.append(objectName);
            scriptSb.append(".");
            scriptSb.append(fieldName);
            scriptSb.append("\n");

            scriptSb.append("for( int i = 0; i < array.length; i++ ) {\n");
            scriptSb.append("println array[i] + \" \";\n");
            scriptSb.append("}\n");
            scriptSb.append("}\n");
            scriptSb.append("\n");
        }
        scriptSb.append("println \" \"\n\n");
    }

    public static Method getMethodAnnotatedWith( final Class< ? > klass, Class< ? extends Annotation> annotation ) {
        Method[] allMethods = klass.getDeclaredMethods();
        for( final Method method : allMethods ) {
            if (method.isAnnotationPresent(annotation)) {
                return method;
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private String stringFromObject( Object value ) throws Exception {
        if (value instanceof JTextField) {
            JTextField tf = (JTextField) value;
            return tf.getText();
        } else if (value instanceof JTextArea) {
            JTextArea tf = (JTextArea) value;
            return tf.getText();
        } else if (value instanceof JCheckBox) {
            JCheckBox tf = (JCheckBox) value;
            return tf.isSelected() ? "true" : "false";
        } else if (value instanceof JComboBox) {
            JComboBox tf = (JComboBox) value;
            String comboItem = tf.getSelectedItem().toString();
            // check if it is a layer first
            String layersString = getFromLayers(comboItem);
            if (layersString != null) {
                return layersString;
            }
            return comboItem;
        }
        return null;
    }

    //////////////////////////////////////////////////////////////////////
    // Methods that might be worth to override
    //////////////////////////////////////////////////////////////////////

    /**
     * If a list of file backed layers is available, override this and do the conversion.
     * 
     * @param comboItem the name of the layer.
     * @return if available the path of the file backed layer.
     */
    protected String getFromLayers( String comboItem ) {
        return null;
    }

    /**
     * Override if you are in a context that supports interaction.
     * 
     * @param component the component that listens to mouse interaction.
     */
    protected void addMouseListenerToContext( MouseListener mouseListener ) {
        // default does nothing.
    }

    /**
     * Override if you are in a context that supports interaction.
     * 
     * @param component the component to remove that listens to mouse interaction.
     */
    protected void removeMouseListenerFromContext( MouseListener mouseListener ) {
        // default does nothing.
    }

    /**
     * This might be evoked when the panel gets focus. 
     * 
     * <p>This might require a check on available layers and similar.
     */
    public void isVisibleTriggered() {
    }

    /**
     * Override if loading of layers is supported.
     * 
     * @param file
     */
    protected void loadRasterLayer( File file ) {
    }

    /**
     * Override if loading of layers is supported.
     * 
     * @param file
     */
    protected void loadVectorLayer( File file ) {
    }

    public static void main( String[] args ) throws Exception {
        GuiUtilities.setDefaultLookAndFeel();

        File libsFile = null;
        try {
            String libsPath = args[0];
            libsFile = new File(libsPath);
        } catch (Exception e1) {
            // IGNORE
        }
        if (libsFile == null || !libsFile.exists() || !libsFile.isDirectory()) {
            logger.error("The libraries folder is missing or not properly set.");
            libsFile = new File("/home/hydrologis/development/jgrasstools-git/extras/export/libs");
            // System.exit(1);
        }

        logger.info("Libraries folder used: " + libsFile.getAbsolutePath());

        JGrasstoolsModulesManager.getInstance().init();
        DefaultGuiBridgeImpl gBridge = new DefaultGuiBridgeImpl();
        gBridge.setLibsFolder(libsFile);
        final SpatialtoolboxController controller = new SpatialtoolboxController(gBridge);
        final JFrame frame = gBridge.showWindow(controller.asJComponent(), "JGrasstools' Spatial Toolbox");

        Class<SpatialtoolboxController> class1 = SpatialtoolboxController.class;
        ImageIcon icon = new ImageIcon(class1.getResource("/org/jgrasstools/images/hm150.png"));
        frame.setIconImage(icon.getImage());

        GuiUtilities.addClosingListener(frame, controller);
    }

}
