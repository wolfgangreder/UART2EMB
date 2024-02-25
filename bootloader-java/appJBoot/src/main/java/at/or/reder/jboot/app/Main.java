/*
 * Copyright 2024 Wolfgang Reder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.or.reder.jboot.app;

import at.or.reder.jboot.Bootloader;
import at.or.reder.jboot.BootloaderFactory;
import at.or.reder.jboot.MemorySpace;
import at.or.reder.jboot.io.Source;
import at.or.reder.jboot.io.SourceFactories;
import at.or.reder.jboot.io.SourceFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import lombok.extern.java.Log;

/**
 *
 * @author Wolfgang Reder
 */
@Log
public class Main extends javax.swing.JFrame
{

  ExecutorService executorService;

  /** Creates new form Main */
  public Main()
  {
    executorService = Executors.newFixedThreadPool(1);
    initComponents();
  }

  private Bootloader openBootloader() throws IOException
  {
    Optional<BootloaderFactory> factory = BootloaderFactory.findFactory(BootloaderFactory.ID_JBOOT);
    if (factory.isPresent()) {
      Map<String, String> properties = Map.of(BootloaderFactory.PROP_CONNECTION,
                                              edOutput.getText(),
                                              BootloaderFactory.PROP_SPEED,
                                              "115200");
      return factory.get().createBootloader(properties);
    }
    return null;
  }

  private void pickFile()
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new FileFilter()
    {
      @Override
      public boolean accept(File f)
      {
        if (f.isDirectory()) {
          return true;
        }
        String name = f.getName().toLowerCase();
        return name.endsWith(".hex") || name.endsWith(".eep") || name.endsWith(".elf");
      }

      @Override
      public String getDescription()
      {
        return "Binary Code (*.hex,*.eep,*.elf)";
      }

    });
    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      edInput.setText(chooser.getSelectedFile().toURI().toString());
    }
  }

  private URI getSelectedURI()
  {
    try {
      return new URI(edInput.getText());
    } catch (URISyntaxException ex) {
      log.log(Level.SEVERE,
              "getSelectedURI",
              ex);
    }
    return null;
  }

  private String getExtension(URI uri)
  {
    String name = uri.getPath();
    int dotPos = name.lastIndexOf('.');
    if (dotPos >= 0) {
      return name.substring(dotPos + 1);
    }
    return "";
  }

  private MemorySpace getSelectedSpace()
  {
    if (rdFlash.isSelected()) {
      return MemorySpace.FLASH;
    }
    if (rdEEProm.isSelected()) {
      return MemorySpace.EEPROM;
    }
    return null;
  }

  private void onUpload(Bootloader bootloader,
                        MemorySpace space,
                        long bytesSent,
                        long bytesToSend)
  {
    SwingUtilities.invokeLater(() -> {
      jProgressBar1.setMaximum((int) bytesToSend);
      jProgressBar1.setValue((int) bytesSent);
      lbMemories.setText(Long.toUnsignedString(bytesSent) + " / " + Long.toUnsignedString(bytesToSend));
    });
  }

  private void writeToDeviceSwing()
  {
    executorService.submit(this::writeToDevice);
  }

  private void writeToDevice()
  {
    edMessages.setText(null);
    URI uri = getSelectedURI();
    if (uri != null) {
      MemorySpace selectedSpace = getSelectedSpace();
      if (selectedSpace != null) {
        SourceFactory sourceFactory = SourceFactories.getSourceByExtension(getExtension(uri)).orElse(null);
        if (sourceFactory != null) {
          try {
            Source source = sourceFactory.createSource(uri);
            if (source.getSupportedSpaces().contains(selectedSpace)) {
              try (Bootloader bootloader = openBootloader();
                      InputStream is = source.openStream(selectedSpace)) {
                if (bootloader != null) {
                  bootloader.connect();
                  bootloader.upload(selectedSpace,
                                    is,
                                    this::onUpload);
                }
              }
            }
          } catch (IOException ex) {
            log.log(Level.SEVERE,
                    "writeToDevice",
                    ex);
            try {
              edMessages.getDocument().insertString(edMessages.getDocument().getLength(),
                                                    ex.getMessage(),
                                                    null);
            } catch (BadLocationException bex) {
              log.log(Level.SEVERE,
                      "writeToDevice",
                      bex);
            }
          }
        }
      }
    }
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    javax.swing.ButtonGroup buttonGroup1 = new javax.swing.ButtonGroup();
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    jScrollPane1 = new javax.swing.JScrollPane();
    javax.swing.JPanel jPanel1 = new javax.swing.JPanel();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    jLabel1.setLabelFor(edInput);
    jLabel1.setText("Input");

    edInput.setText("file:/home/wolfi/eagle/UART2EMB/Firmware.X/dist/default/production/Firmware.X.production.hex");

    btPickInput.setText("...");
    btPickInput.setMargin(new java.awt.Insets(0, 2, 0, 2));
    btPickInput.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btPickInputActionPerformed(evt);
      }
    });

    jLabel2.setText("Output");

    edMessages.setColumns(20);
    edMessages.setRows(5);
    jScrollPane1.setViewportView(edMessages);

    btWrite.setText("Write");
    btWrite.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btWriteActionPerformed(evt);
      }
    });
    jPanel1.add(btWrite);

    btVerify.setText("Verify");
    jPanel1.add(btVerify);

    jProgressBar1.setStringPainted(true);

    buttonGroup1.add(rdFlash);
    rdFlash.setText("Flash");

    buttonGroup1.add(rdEEProm);
    rdEEProm.setText("EEPROM");

    edOutput.setText("/dev/ttyS0");

    lbMemories.setText("jLabel3");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(12, 12, 12)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(rdFlash)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(rdEEProm)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(edOutput, javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(edInput, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btPickInput)
            .addGap(12, 12, 12))))
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
          .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(lbMemories)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(edInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btPickInput))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(edOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(8, 8, 8)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(rdFlash)
          .addComponent(rdEEProm))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lbMemories)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void btPickInputActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btPickInputActionPerformed
  {//GEN-HEADEREND:event_btPickInputActionPerformed
    pickFile();
  }//GEN-LAST:event_btPickInputActionPerformed

  private void btWriteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btWriteActionPerformed
  {//GEN-HEADEREND:event_btWriteActionPerformed
    writeToDeviceSwing();
  }//GEN-LAST:event_btWriteActionPerformed

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    /* Create and display the form */
    java.awt.EventQueue.invokeLater(() -> new Main().setVisible(true));
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private final javax.swing.JButton btPickInput = new javax.swing.JButton();
  private final javax.swing.JButton btVerify = new javax.swing.JButton();
  private final javax.swing.JButton btWrite = new javax.swing.JButton();
  private final javax.swing.JTextField edInput = new javax.swing.JTextField();
  private final javax.swing.JTextArea edMessages = new javax.swing.JTextArea();
  private final javax.swing.JTextField edOutput = new javax.swing.JTextField();
  private javax.swing.JLabel jLabel2;
  private final javax.swing.JProgressBar jProgressBar1 = new javax.swing.JProgressBar();
  private javax.swing.JScrollPane jScrollPane1;
  private final javax.swing.JLabel lbMemories = new javax.swing.JLabel();
  private final javax.swing.JRadioButton rdEEProm = new javax.swing.JRadioButton();
  private final javax.swing.JRadioButton rdFlash = new javax.swing.JRadioButton();
  // End of variables declaration//GEN-END:variables
}
