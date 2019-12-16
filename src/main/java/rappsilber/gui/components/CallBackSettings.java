/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rappsilber.gui.components;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import rappsilber.config.AbstractRunConfig;
import rappsilber.config.LocalProperties;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import rappsilber.ms.statistics.utils.UpdatableChar;
import rappsilber.ms.statistics.utils.UpdateableInteger;
import rappsilber.utils.Version;
import rappsilber.utils.XiVersion;
import rappsilber.utils.xibioedacuk_cert;

/**
 *
 * @author Lutz Fischer <lfischer@staffmail.ed.ac.uk>
 */
public class CallBackSettings extends javax.swing.JPanel {

    String m_userid;
    private String USER_AGENT = "XISEARCH_VERSION_CHECK 1.0";
    
    private static String VersionURL = "https://xi3.bio.ed.ac.uk/downloads/xiSEARCH/version.php";
    private static String CheckVersionProperty = "xiSEARCH_CheckForNewVersion";
    private static String ReportSearchProperty = "xiSEARCH_ReportSearch";
    private static String UserIDProperty = "xiSEARCH_UserID";
    
    private class SetResponse {
        public void set(boolean checkVersion) {
            ckCheckVersion.setSelected(checkVersion);
            Object ret = LocalProperties.setProperty(CheckVersionProperty, ckCheckVersion.isSelected() ? "1" : "0");
        }
    }
    private static ArrayList<SetResponse> snycedcheckVersion = new ArrayList<>();
    /**
     * Creates new form CallBackSettings
     */
    public CallBackSettings() {
        initComponents();
        String settingNV = LocalProperties.getProperty(CheckVersionProperty);
        ckCheckVersion.setSelected(AbstractRunConfig.getBoolean(settingNV, true));
        m_userid = LocalProperties.getProperty(UserIDProperty);
        if (m_userid == null) {
            m_userid = java.util.UUID.randomUUID().toString();
            LocalProperties.setProperty(UserIDProperty, m_userid);
        }
        snycedcheckVersion.add(new SetResponse());
//        if (settingNV != null && ckCheckVersion.isSelected()) {
//            doCallBack(-1);
//        }
        //ckCheckVersionActionPerformed(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ckCheckVersion = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        ckCheckVersion.setText("Check for new Version");
        ckCheckVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckCheckVersionActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("When ticked, you will be informed if a new version of xiSEARCH is available for download. For funding purposes we will then record the country associated with your IP-address and a randomized (non-personalized) ID. This data is only used for creating a count of individual xi-users.");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ckCheckVersion)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ckCheckVersion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ckCheckVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckCheckVersionActionPerformed
        setResponse(ckCheckVersion.isSelected());
    }//GEN-LAST:event_ckCheckVersionActionPerformed

    public void doCallBack(final int numberProteins) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    if (ckCheckVersion.isSelected()) {
                        String surl = VersionURL + "?";
                        if (numberProteins > 0) {
                            surl += "searching=true&";
                        }
                        surl += "user=" + m_userid;
                        
                        SSLContext x = null;
                        try {
                            x =  xibioedacuk_cert.getXi3SSLContext();
                        } catch(CertificateException|IOException|KeyStoreException|NoSuchAlgorithmException|UnrecoverableKeyException|KeyManagementException ex ) {

                        }
                        URL url = new URL(surl);
                        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                        conn.setSSLSocketFactory(x.getSocketFactory());
                        
                        Logger.getLogger(CallBackSettings.class.getName()).log(Level.INFO, "Checking for new Version with: " +surl );
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        String sVersion = in.readLine();
                        StringBuilder resp = new StringBuilder(sVersion + "\n");
                        while ((inputLine = in.readLine()) != null)
                            resp.append(inputLine).append("\n");
                        in.close();
                        Logger.getLogger(CallBackSettings.class.getName()).log(Level.INFO, "Response:"  + resp.toString());
                        if (sVersion != null) {
                            Logger.getLogger(CallBackSettings.class.getName()).log(Level.INFO, "latest online version: " + sVersion);
                            Version onlineVersion = new Version(sVersion);
                            if (onlineVersion.compareTo(XiVersion.version) > 0) {
                                Logger.getLogger(CallBackSettings.class.getName()).log(Level.WARNING, "New Version Available: " + onlineVersion);
                                if ((!java.awt.GraphicsEnvironment.isHeadless())
                                        && Desktop.isDesktopSupported()
                                        && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                    if (JOptionPane.showConfirmDialog(CallBackSettings.this,
                                            "New Version of xiSEARCH is available", "New Version",
                                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                        try {
                                            Desktop.getDesktop().browse(new URI("https://rappsilberlab.org/software/xisearch/"));
                                        } catch (URISyntaxException ex) {
                                            Logger.getLogger(CallBackSettings.class.getName()).log(Level.WARNING, "Could not open web-browser!", ex);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CallBackSettings.class.getName()).log(Level.WARNING, "Version check failed");
                }
            }
        };
        
        Thread vc = new Thread(runnable);
        vc.setName("VersionCheck");
        vc.setDaemon(true);
        vc.start();
    }

    public static Boolean showForm() {
        
        if (GraphicsEnvironment.isHeadless()) {
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName()).log(Level.WARNING, "CANT SHOW CONFIRMATION DIALOG");
            return null;
        }
        if (SwingUtilities.isEventDispatchThread())  {
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName()).log(Level.WARNING, "CANT SHOW CONFIRMATION DIALOG");
            throw new UnsupportedOperationException("This Method should not be run from the EventDispachThread");
        }
        final Object lock = new Object();
        final JFrame window = new JFrame("Check For New Version");
        final CallBackSettings cbs = new CallBackSettings();
        JPanel pButtons = new JPanel(new BorderLayout());
        JButton ok = new JButton("OK");

        window.getContentPane().setLayout(new BorderLayout());
        window.add(cbs,BorderLayout.CENTER);
        window.getContentPane().add(pButtons,BorderLayout.SOUTH);
        pButtons.add(ok,BorderLayout.WEST);
        window.pack();
        
        final UpdateableInteger response = new UpdateableInteger(0);
        
        ok.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                window.setVisible(false);
                if (cbs.ckCheckVersion.isSelected()) {
                    response.value=1;
                } else {
                    response.value=-1;
                }
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                synchronized (lock) {
                    //window.setVisible(false);
                    lock.notify();
                }
            }
            
        });
        window.setVisible(true);
        // som thread to detect the window closing event
        Thread waitResponse = new Thread(new Runnable() {
            public void run() {
                while (window.isVisible()) {
                    try {
                        synchronized(lock) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Logger.getLogger(CallBackSettings.class.getName()).log(Level.INFO,"Window Closed");
                }
            }
        });
        
        
        waitResponse.start();
        try {
            waitResponse.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(CallBackSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
        /* Create and display the form */
        if (response.value != 0 ) {
            boolean r = response.value == 1; 
            setResponse(r);
            return r;
        }
        return null;
    }

    public static void setResponse(boolean r) {
        for (SetResponse rs : snycedcheckVersion) {
            rs.set(r);
        }
    }
    
    

    public static Boolean showFormIfNeeded() {
        String checkVersion = LocalProperties.getProperty(CheckVersionProperty);
        if (checkVersion == null) {
            return showForm();
        } 
        return AbstractRunConfig.getBoolean(checkVersion, true);
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox ckCheckVersion;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
