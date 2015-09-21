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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

/**
 *
 * @author nico
 */
public class AcquisitionPanel extends JPanel {
    ScriptInterface gui_;
    CMMCore core_;
    Preferences prefs_;

    private final String ANGLESTEPSIZE = "acq.anglestepsize";
    private final String STARTANGLE = "acq.startangle";
    private final String ENDANGLE = "acq.endangle";
    private final String DOUBLEZERO = "acq.doulbezero";
    private final String DIRROOT = "acq.dirroot";
    private final String NAMEPREFIX = "acq.nameprefix";
    private final String SAVFORMAT = "acq.savformat";

    private final JSpinner angleStepSizeSpinner_;
    private final JTextField startAngleField_;
    private final JTextField endAngleField_;
    private final JCheckBox doubleZeroCheckBox_;
    private final JTextField dirRootField_;
    private final JTextField namePrefixField_;
    private final JToggleButton runButton_;

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

        // Acquire Panel
        JPanel acquirePanel = new JPanel(new MigLayout(
                "", ""));
        acquirePanel.setBorder(GuiUtils.makeTitledBorder("Acquire"));
        final Dimension acqBoxSize = new Dimension(130, 30);

        // set angle step size
        setupPanel.add(new JLabel("Angle Step Size (degrees):"));
        angleStepSizeSpinner_ = new JSpinner(new SpinnerNumberModel(
                prefs_.getInt(ANGLESTEPSIZE, 100), 0, 400, 1));
        angleStepSizeSpinner_.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                prefs_.putInt(ANGLESTEPSIZE, (Integer) angleStepSizeSpinner_.getValue());
            }
        });
        setupPanel.add(angleStepSizeSpinner_, "span, growx, wrap");
        
        // set start angle
        setupPanel.add(new JLabel("Start Angle:"));
        startAngleField_ = new JTextField(
                ((Integer) (prefs_.getInt(STARTANGLE, 0))).toString());
        setTextAttributes(startAngleField_, componentSize);
        startAngleField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.putDouble(STARTANGLE, Integer.parseInt(
                        startAngleField_.getText()));
            }
        });
        setupPanel.add(startAngleField_, "span, growx, wrap");

        // set end angle
        setupPanel.add(new JLabel("End Angle:"));
        endAngleField_ = new JTextField(
                ((Integer) (prefs_.getInt(ENDANGLE, 0))).toString());
        setTextAttributes(endAngleField_, componentSize);
        endAngleField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.putDouble(ENDANGLE, Integer.parseInt(
                        endAngleField_.getText()));
            }
        });
        setupPanel.add(endAngleField_, "span, growx, wrap");

        // set double zero position
        doubleZeroCheckBox_ = new JCheckBox("Double Zero Position: ");
        doubleZeroCheckBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO add your handling code here:
                if (doubleZeroCheckBox_.isSelected()) {
                } else {
                }
            }
        });
        setupPanel.add(doubleZeroCheckBox_, "span, growx, wrap");

        // set directory root
        acquirePanel.add(new JLabel("Directory Root:"));
        dirRootField_ = new JTextField(
                ((Integer) (prefs_.getInt(DIRROOT, 0))).toString());
        setTextAttributes(dirRootField_, componentSize);
        dirRootField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.putDouble(DIRROOT, Integer.parseInt(
                        dirRootField_.getText()));
            }
        });
        acquirePanel.add(dirRootField_, "span, growx, wrap");
        
        // set name prefix
        acquirePanel.add(new JLabel("Name Prefix:"));
        namePrefixField_ = new JTextField(
                ((Integer) (prefs_.getInt(NAMEPREFIX, 0))).toString());
        setTextAttributes(namePrefixField_, componentSize);
        namePrefixField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.putDouble(NAMEPREFIX, Integer.parseInt(
                        namePrefixField_.getText()));
            }
        });
        acquirePanel.add(namePrefixField_, "span, growx, wrap");
        
        // set run button
        runButton_ = new JToggleButton("Run Acquisition");
        runButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO add your handling code here:
                if (runButton_.isSelected()) {
                    runButton_.setText("Abort Acquisition");
                    RunAcquisition();
                } else {
                    runButton_.setText("Run Acquisition");
            }
         }
      });
      acquirePanel.add(runButton_, "span 2, center, wrap");
      
       // Combine them all
      add(setupPanel,"span, growx, wrap");
      add(acquirePanel, "span, growx, wrap");
      
      
    }

    /**
     * Utility function to set attributes for JTextFields in the dialog
     *
     * @param jtf JTextField whose attributes will be set
     * @param size Desired minimum size
     */
    private void setTextAttributes(JTextField jtf, Dimension size) {
        jtf.setHorizontalAlignment(JTextField.RIGHT);
        jtf.setMinimumSize(size);
    }

    /**
    * User is supposed to set up the acquisition in the micromanager panel.
    * This function will acquire images at angle positions defined by calibration. 
    * 
    */
   private void RunAcquisition() {
      try {
         core_.setShutterOpen(true);
         core_.setShutterOpen(false);
      } catch (Exception ex) {
         ij.IJ.log(ex.getMessage() + ", Failed to open/close the shutter");
      }
   }
    
}
