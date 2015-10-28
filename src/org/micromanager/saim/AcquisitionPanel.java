 ///////////////////////////////////////////////////////////////////////////////
//FILE:          AcquisitionPanel
//PROJECT:       SAIM-calibration
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman, Kate Carbone
//
// COPYRIGHT:    University of California, San Francisco 2015
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
package org.micromanager.saim;

import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mmcorej.CMMCore;
import net.miginfocom.swing.MigLayout;
import org.micromanager.api.ScriptInterface;
import org.micromanager.saim.gui.GuiUtils;
import org.micromanager.utils.FileDialogs;
import org.micromanager.MMStudio;
import org.micromanager.saim.exceptions.SAIMException;
import org.micromanager.saim.gui.DragFileToTextField;

/**
 *
 * @author nico
 */
public class AcquisitionPanel extends JPanel {

   ScriptInterface gui_;
   CMMCore core_;
   Preferences prefs_;

   private final JSpinner angleStepSizeSpinner_;
   private final JTextField startAngleField_;
   private final JCheckBox doubleZeroCheckBox_;
   private final JPanel calPanel_;
   private final JCheckBox saveImagesCheckBox_;

   private final JFileChooser acqdirRootChooser_;
   private final JTextField acqdirRootField_;
   private final JButton acqdirRootButton_;
   private final JTextField acqnamePrefixField_;
   private final JToggleButton runButton_;
   private final JTextField coeff3Field_;
   private final JTextField coeff2Field_;
   private final JTextField coeff1Field_;
   private final JTextField coeff0Field_;

   public AcquisitionPanel(ScriptInterface gui, Preferences prefs) {
      super(new MigLayout(
              "",
              ""));
      gui_ = gui;
      core_ = gui_.getMMCore();
      prefs_ = prefs;
      this.setName("Acquisition");

      // Setup Panel
      JPanel setupPanel = new JPanel(new MigLayout(
              "", ""));
      setupPanel.setBorder(GuiUtils.makeTitledBorder("Setup"));
      final Dimension componentSize = new Dimension(150, 30);


      // set start angle
      setupPanel.add(new JLabel("Start Angle:"));
      startAngleField_ = new JTextField("");
      GuiUtils.setTextAttributes(startAngleField_, componentSize);
      GuiUtils.tieTextFieldToPrefs(prefs, startAngleField_, PrefStrings.STARTANGLE);
      setupPanel.add(startAngleField_, "span, growx, wrap");
      
      // set angle step size
      setupPanel.add(new JLabel("Angle Step Size (degrees):"));
      angleStepSizeSpinner_ = new JSpinner(new SpinnerNumberModel(
              1.0, 0, 180, 0.1));
      angleStepSizeSpinner_.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            prefs_.putDouble(PrefStrings.ANGLESTEPSIZE, (Double) angleStepSizeSpinner_.getValue());
         }
      });
      setupPanel.add(angleStepSizeSpinner_, "span, growx, wrap");
      
      setupPanel.add(new JLabel("Start angle must be divisible by angle step size."), "span 2, wrap");

      // set double zero position
      doubleZeroCheckBox_ = new JCheckBox("Double Zero Position");
      doubleZeroCheckBox_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (doubleZeroCheckBox_.isSelected()) {
               prefs_.putBoolean(PrefStrings.DOUBLEZERO, true);
            } else {
               prefs_.putBoolean(PrefStrings.DOUBLEZERO, false);
            }
         }
      });
      setupPanel.add(doubleZeroCheckBox_, "span 2, growx, wrap");

      // Calibration Values
      calPanel_ = new JPanel(new MigLayout(
              "", ""));
      calPanel_.setBorder(GuiUtils.makeTitledBorder("Calibration Values"));

      //Set calibration values
      //x3 coefficient
      calPanel_.add(new JLabel("<html>x<sup>3</sup>: </html>"));
      coeff3Field_ = new JTextField("");
      GuiUtils.setTextAttributes(coeff3Field_, componentSize);
      GuiUtils.tieTextFieldToPrefs(prefs, coeff3Field_, PrefStrings.COEFF3);
      calPanel_.add(coeff3Field_, "span, center, wrap");

      //x2 coefficient
      calPanel_.add(new JLabel("<html>x<sup>2</sup>: </html>"));
      coeff2Field_ = new JTextField("");
      GuiUtils.setTextAttributes(coeff2Field_, componentSize);
      GuiUtils.tieTextFieldToPrefs(prefs, coeff2Field_, PrefStrings.COEFF2);
      calPanel_.add(coeff2Field_, "span, center, wrap");

      //x coefficient
      calPanel_.add(new JLabel("x: "));
      coeff1Field_ = new JTextField("");
      GuiUtils.setTextAttributes(coeff1Field_, componentSize);
      GuiUtils.tieTextFieldToPrefs(prefs, coeff1Field_, PrefStrings.COEFF1);
      calPanel_.add(coeff1Field_, "span, center, wrap");

      //x0 constant
      calPanel_.add(new JLabel("<html>x<sup>0</sup>: </html>"));
      coeff0Field_ = new JTextField("");
      GuiUtils.setTextAttributes(coeff0Field_, componentSize);
      GuiUtils.tieTextFieldToPrefs(prefs, coeff0Field_, PrefStrings.COEFF0);
      calPanel_.add(coeff0Field_, "span, center, wrap");

      // Acquire Panel
      JPanel acquirePanel = new JPanel(new MigLayout(
              "", ""));
      acquirePanel.setBorder(GuiUtils.makeTitledBorder("Acquire"));

      // set directory root file chooser
      acqdirRootChooser_ = new JFileChooser("");
      acqdirRootChooser_.setCurrentDirectory(new java.io.File("."));
      acqdirRootChooser_.setDialogTitle("Directory Root");
      acqdirRootChooser_.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      acqdirRootChooser_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            prefs_.put(PrefStrings.ACQDIRROOT, acqdirRootChooser_.getName());
         }
      });

      // set directory root text field
      acquirePanel.add(new JLabel("Directory Root:"));
      acqdirRootField_ = new JTextField("");
      GuiUtils.setTextAttributes(acqdirRootField_, componentSize);
      acqdirRootField_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            prefs_.put(PrefStrings.ACQDIRROOT, acqdirRootField_.getText());
         }
      });
      DropTarget dt = new DropTarget(acqdirRootField_,
              new DragFileToTextField(acqdirRootField_, true, prefs, PrefStrings.ACQDIRROOT));
      acquirePanel.add(acqdirRootField_);

      //set directory chooser button
      acqdirRootButton_ = new JButton("...");
      acqdirRootButton_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            setRootDirectory();
         }
      });
      acquirePanel.add(acqdirRootButton_, "wrap");

      // set name prefix
      acquirePanel.add(new JLabel("Name Prefix:"));
      acqnamePrefixField_ = new JTextField("");
      GuiUtils.setTextAttributes(acqnamePrefixField_, componentSize);
      acqnamePrefixField_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            prefs_.put(PrefStrings.ACQNAMEPREFIX, acqnamePrefixField_.getText());
         }
      });
      acquirePanel.add(acqnamePrefixField_, "span, growx, wrap");

      // set save images
      saveImagesCheckBox_ = new JCheckBox("Save Images");
      saveImagesCheckBox_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (saveImagesCheckBox_.isSelected()) {
               prefs_.putBoolean(PrefStrings.ACQSAVEIMAGES, true);
            } else {
               prefs_.putBoolean(PrefStrings.ACQSAVEIMAGES, false);
            }
         }
      });
      acquirePanel.add(saveImagesCheckBox_, "span 2, growx, wrap");

      // set run button
      runButton_ = new JToggleButton("Run Acquisition");
      runButton_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            // TODO add your handling code here:
            if (runButton_.isSelected()) {
               runButton_.setText("Abort Acquisition");
               runAcquisition();
            } else {
               runButton_.setText("Run Acquisition");
            }
         }
      });
      acquirePanel.add(runButton_, "span 3, center, wrap");

      // Combine them all
      add(setupPanel, "span, growx, wrap");
      add(calPanel_, "span, growx, wrap");
      add(acquirePanel, "span, growx, wrap");
      updateGUIFromPrefs();

   }

   protected void setRootDirectory() {
      File result = FileDialogs.openDir(null,
              "Please choose a directory root for image data",
              MMStudio.MM_DATA_SET);
      if (result != null) {
         acqdirRootField_.setText(result.getAbsolutePath());
      }
   }

   /**
    * User is supposed to set up the acquisition in the micro-manager panel.
    * This function will acquire images at angle positions defined by
    * calibration.
    *
    */
   private void runAcquisition() {
      class AcqThread extends Thread {

         AcqThread(String threadName) {
            super(threadName);
         }

         @Override
         public void run() {
            String acq;
            try {
               acq = SAIMCommon.runAcquisition(gui_, prefs_, acqdirRootField_.getText(),
                       acqnamePrefixField_.getText(), true, saveImagesCheckBox_.isSelected());
               gui_.closeAcquisition(acq);
            } catch (SAIMException saimEx) {
               ij.IJ.error(saimEx.getMessage());
            } catch (Exception ex) {
               ij.IJ.log(ex.getMessage());
               ij.IJ.error("Something went wrong.  Aborting!");
            } finally {
               runButton_.setSelected(false);
               runButton_.setText("Run Acquisition");
            }
         }

      }
      AcqThread acqT = new AcqThread("SAIM Acquisition");
      acqT.start();

   }
   
   //function to add preferences values to each field that uses them
   public final void updateGUIFromPrefs() {
        angleStepSizeSpinner_.setValue(Double.parseDouble(prefs_.get(PrefStrings.ANGLESTEPSIZE, "0.0")));
        startAngleField_.setText(prefs_.get(PrefStrings.STARTANGLE, ""));
        doubleZeroCheckBox_.setSelected(Boolean.parseBoolean(prefs_.get(PrefStrings.DOUBLEZERO, "")));
        saveImagesCheckBox_.setSelected(Boolean.parseBoolean(prefs_.get(PrefStrings.ACQSAVEIMAGES, "")));
        acqdirRootField_.setText(prefs_.get(PrefStrings.ACQDIRROOT, ""));
        acqnamePrefixField_.setText(prefs_.get(PrefStrings.ACQNAMEPREFIX, ""));
        coeff3Field_.setText(prefs_.get(PrefStrings.COEFF3, ""));
        coeff2Field_.setText(prefs_.get(PrefStrings.COEFF2, ""));
        coeff1Field_.setText(prefs_.get(PrefStrings.COEFF1, ""));
        coeff0Field_.setText(prefs_.get(PrefStrings.COEFF0, ""));
    }

}
