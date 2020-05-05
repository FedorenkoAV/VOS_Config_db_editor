/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vos_config_db_editor;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JFileChooser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javafx.scene.input.Clipboard;
//import javafx.scene.input.ClipboardContent;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author User
 */
public class MainJFrame extends javax.swing.JFrame {

    Path pathFile = null;
    Connection connection = null;
    Statement statement;
    public static final String PATH_TO_PROPERTIES = "config.ini";
    FileInputStream fileInputStream = null;
    FileOutputStream fileOutputStream = null;
    Properties prop;
    String path = "";
    // Стили редактора
    private Style bold = null; // стиль заголовка
    private Style normal = null; // стиль текста

    private final String STYLE_heading = "heading",
            STYLE_normal = "normal",
            FONT_style = "Monospaced";

    /**
     * Creates new form MainJFrame
     */
    public MainJFrame() {
        try {
            initComponents();
            // Определение стилей редактора
            createStyles(jTextPaneLog);
//        changeDocumentStyle(jTextPaneLog);
            appendText("Начнем");
            //инициализируем специальный объект Properties
            //типа Hashtable для удобной работы с данными
            prop = new Properties();
            File paramsFile = new File(PATH_TO_PROPERTIES);
            if (!paramsFile.exists()) {
                appendText("Нет файла параметров, создаем новый.");
                paramsFile.createNewFile();
                prop.setProperty("path", "");
            }
            //обращаемся к файлу и получаем данные
            fileInputStream = new FileInputStream(PATH_TO_PROPERTIES);
            //fileOutputStream = new FileOutputStream(PATH_TO_PROPERTIES);
            prop.load(fileInputStream);
            if (prop.containsKey("path")) {
                path = prop.getProperty("path");
            } else {
                prop.setProperty("path", "");
            }
            if (path != null && !"".equals(path)) {//path != null && path != ""
//                jFormattedTextFieldPath.setText(path);
                jFileChooserOpen.setCurrentDirectory(new File(path));
                appendText("Читаем значения из файла параметров.");
            }

            // load the sqlite-JDBC driver using the current class loader
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            appendBoldText("Ошибка загрузки драйвера sqlite-JDBC.\r\n" + ex.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, "Ошибка загрузки драйвера sqlite-JDBC", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            appendBoldText("Ошибка открытия файла параметров.\r\n" + ex.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, "Ошибка открытия файла параметров: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
        appendText("Инициализировали драйвер org.sqlite.JDBC");

    }

    private void appendText(String str) {

        insertText(jTextPaneLog, str + "\r\n", normal);

    }

    private void appendBoldText(String str) {
        insertText(jTextPaneLog, str + "\r\n" + jTextPaneLog.getContentType() + "\r\n", bold);
    }

    /**
     * Процедура формирования стилей редактора
     *
     * @param editor редактор
     */
    private void createStyles(JTextPane editor) {
        // Создание стилей
        normal = editor.addStyle(STYLE_normal, null);
        StyleConstants.setFontFamily(normal, FONT_style);
        StyleConstants.setFontSize(normal, 12);
        // Наследуем свойстdо FontFamily
        bold = editor.addStyle(STYLE_heading, normal);
//        StyleConstants.setFontSize(heading, 12);
        StyleConstants.setBold(bold, true);
        StyleConstants.setForeground(bold, Color.red);
    }

    /**
     * Процедура добавления в редактор строки определенного стиля
     *
     * @param editor редактор
     * @param string строка
     * @param style стиль
     */
    private void insertText(JTextPane editor, String string, Style style) {
        try {
            Document doc = editor.getDocument();
            doc.insertString(doc.getLength(), string, style);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyCustomFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File file) {
            // Allow only directories, or files with "VOS.Config.db" name
            return file.isDirectory() || file.getAbsolutePath().endsWith("VOS.Config.db");
        }

        @Override
        public String getDescription() {
            // This description will be displayed in the dialog,
            // hard-coded = ugly, should be done via I18N
            return "VOS.Config.db";
        }
    }

    void readBaseValues() {
        if (connection == null) {
            appendText("Не могу считать значения из базы, так как база не подключена.");
            return;
        }
        try {
            appendText("Читаем значения из базы.");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            ResultSet rs;
            String readString;
            String readValue = "";
            rs = statement.executeQuery("select Value from Tbl_Config where Key='LOGAGENT/FTP'");
            while (rs.next()) {
                readValue = rs.getString("Value");
                readString = "LOGAGENT/FTP = " + readValue;
                appendText(readString);
            }
            jTextFieldlLogAgent.setText(readValue);
            rs = statement.executeQuery("select Value from Tbl_Config where Key='TSC_ExtraNetIP'");
            while (rs.next()) {
                readValue = rs.getString("Value");
                readString = "TSC_ExtraNetIP = " + readValue;
                appendText(readString);
            }
            jTextFieldlTSC_ExtraNetIP.setText(readValue);
            rs = statement.executeQuery("select Value from Tbl_Config where Key='AGENT_IP'");
            while (rs.next()) {
                readValue = rs.getString("Value");
                readString = "AGENT_IP = " + readValue;
                appendText(readString);
            }
            jTextFieldAGENT_IP.setText(readValue);
            jTextFieldIP.setText(readValue);
            rs = statement.executeQuery("select Value from Tbl_Config where (Tag='TSC' and Key='TSC_ID')");
            while (rs.next()) {
                readValue = rs.getString("Value");
                readString = "TSC_ID = " + readValue;
                appendText(readString);
            }
            jTextField5.setText(readValue);
            rs = statement.executeQuery("select Value from Tbl_Config where (Tag='Agent' and Key='TSC_ID')");
            while (rs.next()) {
                readValue = rs.getString("Value");
                readString = "TSC_ID = " + readValue;
                appendText(readString);
            }
            jTextField6.setText(readValue);
            jTextFieldBSNumber.setText(readValue);

            rs = statement.executeQuery("select Value from Tbl_Config where Key='SICLAI'");
            while (rs.next()) {
                readValue = rs.getString("Value");
                readString = "SICLAI = " + readValue;
                appendText(readString);
            }
            jTextField7.setText(readValue);
            connection.commit();
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex1);
                appendBoldText("SQLException при отмене внесения изменений в базу данных.\r\n" + ex.getMessage());
                javax.swing.JOptionPane.showMessageDialog(this, "SQLException при отмене внесения изменений в базу данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
            appendBoldText("Ошибка при чтении значении из базы данных.\r\n" + ex.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, "Ошибка при чтении значении из базы данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                statement.close();
            } catch (SQLException ex) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
                appendBoldText("Ошибка закрытия объекта Statement.\r\n" + ex.getMessage());
                javax.swing.JOptionPane.showMessageDialog(this, "Ошибка закрытия объекта Statement.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void closeBase() {
        try {
            if (connection != null) {
                statement.close();
                connection.close();
                System.out.println("База успешно закрыта.");
            }
        } catch (SQLException ex) {
            // connection close failed.
            System.out.println("Ошибка закрытия базы данных.");
            System.out.println(ex.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, "Ошибка закрытия базы данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    void prepareToExit() {
        try {
            // TODO add your handling code here:
            closeBase();
            if (fileOutputStream != null) {
                prop.store(fileOutputStream, "");
                fileOutputStream.close();
                System.out.println("Сохраняем " + PATH_TO_PROPERTIES);
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException ex) {
            appendBoldText("Ошибка при сохранении файла параметров.\r\n" + ex.getMessage());
            Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
            javax.swing.JOptionPane.showMessageDialog(this, "Ошибка при сохранении файла параметров.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    void copyToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection clipData;
        StyledDocument doc = jTextPaneLog.getStyledDocument();
        try {
            clipData = new StringSelection(doc.getText(0, doc.getLength()));
            clipboard.setContents(clipData, null);
        } catch (BadLocationException ex) {
            Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    class HtmlTransferable implements Transferable {

        private String htmlText;

        public HtmlTransferable(String htmlText) {
            this.htmlText = htmlText;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            return new DataFlavor[]{DataFlavor.fragmentHtmlFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            return flavor.equals(DataFlavor.fragmentHtmlFlavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            if (flavor.equals(DataFlavor.fragmentHtmlFlavor)) {
                return htmlText;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooserOpen = new javax.swing.JFileChooser();
        jFileChooserSave = new javax.swing.JFileChooser();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItemCopy = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jFormattedTextFieldPath = new javax.swing.JFormattedTextField();
        jButtonOpenBD = new javax.swing.JButton();
        jTextFieldIP = new javax.swing.JTextField();
        jTextFieldlLogAgent = new javax.swing.JTextField();
        jTextFieldlTSC_ExtraNetIP = new javax.swing.JTextField();
        jLabelIP = new javax.swing.JLabel();
        jLabelLogAgent = new javax.swing.JLabel();
        jLabelTSC_ExtraNetIP = new javax.swing.JLabel();
        jLabelBSNumber = new javax.swing.JLabel();
        jTextFieldBSNumber = new javax.swing.JTextField();
        jLabelAGENT_IP = new javax.swing.JLabel();
        jTextFieldAGENT_IP = new javax.swing.JTextField();
        jLabelTSC = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabelAgent = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabelSiclai = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jButtonEditBS = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPaneLog = new javax.swing.JTextPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        jFileChooserOpen.setAcceptAllFileFilterUsed(false);
        jFileChooserOpen.setApproveButtonText("Открыть");
        jFileChooserOpen.setApproveButtonToolTipText("");
        jFileChooserOpen.setCurrentDirectory(new java.io.File("D:\\#Kurs\\#DMR\\ПНР БС DMR Уса-Ухта-Ярославль\\Temp"));
        jFileChooserOpen.setDialogTitle("Укажите путь к файлу VOS.Config.db");
        jFileChooserOpen.setFileFilter(new MyCustomFilter());
        jFileChooserOpen.setDragEnabled(true);
        jFileChooserOpen.setName(""); // NOI18N

        jMenuItemCopy.setText("Копировать");
        jMenuItemCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemCopy);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("VOS Config db editor");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jFormattedTextFieldPath.setText("Путь к базе VOS.Config.db");

        jButtonOpenBD.setText("Открыть");
        jButtonOpenBD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenBDActionPerformed(evt);
            }
        });

        jTextFieldIP.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldIP.setText("255.255.255.255");

        jTextFieldlLogAgent.setEditable(false);
        jTextFieldlLogAgent.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldlLogAgent.setText("255.255.255.255/255.255.255.255");
        jTextFieldlLogAgent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldlLogAgentActionPerformed(evt);
            }
        });

        jTextFieldlTSC_ExtraNetIP.setEditable(false);
        jTextFieldlTSC_ExtraNetIP.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldlTSC_ExtraNetIP.setText("255.255.255.255");

        jLabelIP.setText("IP Адрес:");

        jLabelLogAgent.setText("LOGAGENT/FTP");

        jLabelTSC_ExtraNetIP.setText("TSC_ExtraNetIP");

        jLabelBSNumber.setText("Номер БС");

        jTextFieldBSNumber.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldBSNumber.setText("999");

        jLabelAGENT_IP.setText("AGENT_IP");

        jTextFieldAGENT_IP.setEditable(false);
        jTextFieldAGENT_IP.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldAGENT_IP.setText("255.255.255.255");

        jLabelTSC.setText("TSC_ID TSC");

        jTextField5.setEditable(false);
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField5.setText("999");

        jLabelAgent.setText("TSC_ID Agent");

        jTextField6.setEditable(false);
        jTextField6.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField6.setText("999");

        jLabelSiclai.setText("SICLAI");

        jTextField7.setEditable(false);
        jTextField7.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField7.setText("999");

        jButtonEditBS.setText("Внести изменения");
        jButtonEditBS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditBSActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonOpenBD)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabelIP)
                                .addGap(18, 18, 18)
                                .addComponent(jTextFieldIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelTSC_ExtraNetIP)
                                    .addComponent(jLabelLogAgent)
                                    .addComponent(jLabelAGENT_IP))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextFieldAGENT_IP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextFieldlTSC_ExtraNetIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextFieldlLogAgent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelAgent)
                                    .addComponent(jLabelTSC)
                                    .addComponent(jLabelSiclai))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabelBSNumber)
                                .addGap(18, 18, 18)
                                .addComponent(jTextFieldBSNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonEditBS))))
                    .addComponent(jFormattedTextFieldPath, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 436, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFormattedTextFieldPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonOpenBD))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelIP)
                    .addComponent(jTextFieldIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelBSNumber)
                    .addComponent(jTextFieldBSNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonEditBS))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelLogAgent)
                            .addComponent(jTextFieldlLogAgent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelTSC_ExtraNetIP)
                            .addComponent(jTextFieldlTSC_ExtraNetIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelAGENT_IP)
                            .addComponent(jTextFieldAGENT_IP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelTSC)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelAgent)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelSiclai)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setText("Автор программы - Федоренко Александр.");

        new SmartScroller(jScrollPane2);

        jTextPaneLog.setContentType(""); // NOI18N
        jTextPaneLog.setToolTipText("");
        jTextPaneLog.setComponentPopupMenu(jPopupMenu1);
        jTextPaneLog.setDragEnabled(true);
        jTextPaneLog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextPaneLogMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTextPaneLog);

        jMenu1.setText("Файл");

        jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemExit.setText("Выход");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Правка");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Копировать лог");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOpenBDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenBDActionPerformed
        // TODO add your handling code here:
        int returnVal = jFileChooserOpen.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) { //Если файл был выбран, то
            File file = jFileChooserOpen.getSelectedFile(); //Берем выбраный файл (файл еще не открыт)
            pathFile = file.toPath();
            jFormattedTextFieldPath.setText(pathFile.toString());
            try {
                fileOutputStream = new FileOutputStream(PATH_TO_PROPERTIES);//Вносим новый путь в файл параметров
                prop.setProperty("path", pathFile.toString());
            } catch (FileNotFoundException ex) {
                appendBoldText("Не могу внести изменения в файл параметров.\r\n" + ex.getMessage());
                javax.swing.JOptionPane.showMessageDialog(this, "Не могу внести изменения в файл параметров.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            if (returnVal == JFileChooser.CANCEL_OPTION) {
                appendText("Выбор файла отменен");
                return;
            } else {
                appendText("В процессы выбора файла произошла ошибка.");
                return;
            }
        }
//        Connection connection = null;
        try {
            // create a database connection
            if (connection != null) {//Если какая-то база уже открыта, то закрываем ее.
                appendText("Закрываем ранее открытую базу данных.");
                connection.close();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + jFormattedTextFieldPath.getText());
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            appendText("Соединение с базой VOS.Config.db установлено");
            prop.setProperty("path", pathFile.toString());

//            prop.store(fileOutputStream, "");
            readBaseValues();
        } catch (SQLException ex) {
            // if the error message is "out of memory", 
            // it probably means no database file is found
            appendBoldText("Ошибка при открытии базы данных.\r\n" + ex.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, "Ошибка при открытии базы данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);

        } finally {
//            try {
//                if (connection != null) {
//
//                    connection.close();
//                }
//            } catch (SQLException e) {
//                // connection close failed.
//                System.err.println(e);
//            }
        }

    }//GEN-LAST:event_jButtonOpenBDActionPerformed

    private void jTextFieldlLogAgentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldlLogAgentActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldlLogAgentActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        prepareToExit();
    }//GEN-LAST:event_formWindowClosing

    private void jButtonEditBSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditBSActionPerformed
        // TODO add your handling code here:
        if (connection == null) {
            appendBoldText("Не могу внести изменения в базу, так как база не подключена.");
            javax.swing.JOptionPane.showMessageDialog(this, "Не могу внести изменения в базу, так как база не подключена.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            // TODO add your handling code here:
            ResultSet rs;
            String oldValue = jTextFieldAGENT_IP.getText();
            String newValue = jTextFieldIP.getText();
            StringBuilder newValueSB = new StringBuilder(newValue);
            int dotCounter = 0;
            for (int i = 0; i < newValueSB.length(); i++) {
                if (newValueSB.charAt(i) == '.') {
                    dotCounter++;
                }
            }
            if (dotCounter != 3) {
                javax.swing.JOptionPane.showMessageDialog(this, "Неправильный IP-адрес.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                appendBoldText("Неправильный IP-адрес.");
                return;
            }
            int dotIndex;
            String tmpStr;
            int tmpInt;
            try {
                for (int i = 0; i < 3; i++) {
                    dotIndex = newValueSB.indexOf(".");
                    if (dotIndex == -1) {
                        //Ошибка
                    }
                    tmpStr = newValueSB.substring(0, dotIndex);

                    tmpInt = Integer.parseInt(tmpStr);
                    if (tmpInt < 0) {
                        javax.swing.JOptionPane.showMessageDialog(this, "Неправильный IP-адрес.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        appendBoldText("Неправильный IP-адрес.");
                        return;
                    }
                    if (tmpInt > 255) {
                        javax.swing.JOptionPane.showMessageDialog(this, "Неправильный IP-адрес.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        appendBoldText("Неправильный IP-адрес.");
                        return;
                    }
                    appendText(tmpStr);
                    newValueSB.delete(0, dotIndex + 1);
                    //beginIndex = dotIndex;                
                }
                tmpStr = newValueSB.toString();
                tmpInt = Integer.parseInt(tmpStr);
                if (tmpInt < 0) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Неправильный IP-адрес.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    appendBoldText("Неправильный IP-адрес.");
                    return;
                }
                if (tmpInt > 255) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Неправильный IP-адрес.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    appendBoldText("Неправильный IP-адрес.");
                    return;
                }
                appendText(tmpStr);
            } catch (NumberFormatException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, newValue + " Неправильный IP-адрес.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                appendBoldText(newValue + " Неправильный IP-адрес.");
                //            printStackTraceElements(ex);
                return;
            }
            appendText("Записываем новый IP " + newValue + " в базу.");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate("update Tbl_Config set Value=replace(Value,\"" + oldValue + "\",\"" + newValue + "\");");
            newValue = jTextFieldBSNumber.getText();
            if (newValue.length() > 2) {
                javax.swing.JOptionPane.showMessageDialog(this, "Неправильный номер базовой станции.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    appendBoldText("Номер базовой станции не может быть меньше 1.");
                    return;
            }
            try {
                tmpInt = Integer.parseInt(newValue);
                if (tmpInt < 1) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Номер базовой станции не может быть меньше 1.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    appendBoldText("Номер базовой станции не может быть меньше 1.");
                    return;
                }
                if (tmpInt > 99) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Номер базовой станции не может быть больше 99.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    appendBoldText("Номер базовой станции не может быть больше 99.");
                    return;
                }

            } catch (NumberFormatException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, newValue + " Не могу преобразовать в число.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                appendBoldText(newValue + " Не могу преобразовать в число.");
                //            printStackTraceElements(ex);
                return;
            }

            appendText("Записываем новый номер BS " + newValue + " в базу.");
            statement.executeUpdate("UPDATE Tbl_Config SET Value='" + newValue + "' WHERE Key=\"TSC_ID\";");
            if (newValue.length() == 1) {
                newValue = "0" + newValue;
            }
            statement.executeUpdate("UPDATE Tbl_Config SET Value='3" + newValue + "' WHERE Key=\"SICLAI\";");
            connection.commit();
            readBaseValues();
//            statement.close();
        } catch (SQLException ex) {

            try {
                connection.rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex1);
                appendBoldText("SQLException при отмене внесения изменений в базу данных.\r\n" + ex.getMessage());
                javax.swing.JOptionPane.showMessageDialog(this, "SQLException при отмене внесения изменений в базу данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

            Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
            appendBoldText("SQLException при внесении изменений в базу данных.\r\n" + ex.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, "SQLException при внесении изменений в базу данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                statement.close();
            } catch (SQLException ex) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
                appendBoldText("Ошибка закрытия объекта Statement.\r\n" + ex.getMessage());
                javax.swing.JOptionPane.showMessageDialog(this, "Ошибка закрытия объекта Statement.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_jButtonEditBSActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        // TODO add your handling code here:
        prepareToExit();
        System.exit(0);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyActionPerformed
        // TODO add your handling code here:
        copyToClipboard();
    }//GEN-LAST:event_jMenuItemCopyActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        copyToClipboard();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jTextPaneLogMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextPaneLogMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextPaneLogMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows Classic".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonEditBS;
    private javax.swing.JButton jButtonOpenBD;
    private javax.swing.JFileChooser jFileChooserOpen;
    private javax.swing.JFileChooser jFileChooserSave;
    private javax.swing.JFormattedTextField jFormattedTextFieldPath;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelAGENT_IP;
    private javax.swing.JLabel jLabelAgent;
    private javax.swing.JLabel jLabelBSNumber;
    private javax.swing.JLabel jLabelIP;
    private javax.swing.JLabel jLabelLogAgent;
    private javax.swing.JLabel jLabelSiclai;
    private javax.swing.JLabel jLabelTSC;
    private javax.swing.JLabel jLabelTSC_ExtraNetIP;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItemCopy;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextFieldAGENT_IP;
    private javax.swing.JTextField jTextFieldBSNumber;
    private javax.swing.JTextField jTextFieldIP;
    private javax.swing.JTextField jTextFieldlLogAgent;
    private javax.swing.JTextField jTextFieldlTSC_ExtraNetIP;
    private javax.swing.JTextPane jTextPaneLog;
    // End of variables declaration//GEN-END:variables
}
