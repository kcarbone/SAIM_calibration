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

/**
 *
 * @author nico
 */
public class AcquisitionPanel extends JPanel implements ICalibrationObserver{

    ScriptInterface gui_;
    CMMCore core_;
    Preferences prefs_;

    private final String ANGLESTEPSIZE = "acq.anglestepsize";
    private final String STARTANGLE = "acq.startangle";
    private final String ENDANGLE = "acq.endangle";
    private final String DOUBLEZERO = "acq.doulbezero";
    private final String DIRROOT = "acq.dirroot";
    private final String NAMEPREFIX = "acq.nameprefix";

    private final JSpinner angleStepSizeSpinner_;
    private final JTextField startAngleField_;
    private final JTextField endAngleField_;
    private final JCheckBox doubleZeroCheckBox_;
    private final JPanel calPanel_;

    private final JFileChooser dirRootChooser_;
    private final JTextField dirRootField_;
    private final JButton dirRootButton_;
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
        doubleZeroCheckBox_ = new JCheckBox("Double Zero Position");
        doubleZeroCheckBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (doubleZeroCheckBox_.isSelected()) {
                    prefs_.putBoolean(DOUBLEZERO, true);
                } else {
                    prefs_.putBoolean(DOUBLEZERO, false);
                }
            }
        });
        setupPanel.add(doubleZeroCheckBox_, "span 2, growx, wrap");

        // Calibration Values
        calPanel_ = new JPanel(new MigLayout(
                "", ""));
        calPanel_.setBorder(GuiUtils.makeTitledBorder("Calibration Values"));
        final Dimension calBoxSize = new Dimension(130, 30);
        
        //Set calibration values
        
        
        // Acquire Panel
        JPanel acquirePanel = new JPanel(new MigLayout(
                "", ""));
        acquirePanel.setBorder(GuiUtils.makeTitledBorder("Acquire"));
        final Dimension acqBoxSize = new Dimension(130, 30);

        // set directory root file chooser
        dirRootChooser_ = new JFileChooser(
                prefs_.get(DIRROOT, ""));
        dirRootChooser_.setCurrentDirectory(new java.io.File("."));
        dirRootChooser_.setDialogTitle("Directory Root");
        dirRootChooser_.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirRootChooser_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.putDouble(DIRROOT, Integer.parseInt(dirRootChooser_.getName()));
            }
        });

        // set directory root text field
        acquirePanel.add(new JLabel("Directory Root:"));
        dirRootField_ = new JTextField(
                prefs_.get(DIRROOT, ""));
        setTextAttributes(dirRootField_, componentSize);
        dirRootField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(DIRROOT,
                        dirRootField_.getText());
            }
        });
        acquirePanel.add(dirRootField_);

        //set directory chooser button
        dirRootButton_ = new JButton("...");
        dirRootButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setRootDirectory();
            }
        });
        acquirePanel.add(dirRootButton_, "wrap");

        // set name prefix
        acquirePanel.add(new JLabel("Name Prefix:"));
        namePrefixField_ = new JTextField(
                prefs_.get(NAMEPREFIX, ""));
        setTextAttributes(namePrefixField_, componentSize);
        namePrefixField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(NAMEPREFIX,
                        namePrefixField_.getText());
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
        acquirePanel.add(runButton_, "span 3, center, wrap");

        // Combine them all
        add(setupPanel, "span, growx, wrap");
        add(calPanel_, "span, growx, wrap");
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
     * This function will acquire images at angle positions defined by
     * calibration.
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

    public void calibrationChanged(double x3, double x2, double x1, double x0){
        calPanel_.add(new JLabel(Double.toString(x3) + "x^3"), "span, center, wrap");
        calPanel_.add(new JLabel(Double.toString(x2)+ "x^2"), "span, center, wrap");
        calPanel_.add(new JLabel(Double.toString(x1)+ "x"), "span, center, wrap");
        calPanel_.add(new JLabel(Double.toString(x0)), "span, center, wrap");
    }
    
    protected void setRootDirectory() {
        File result = FileDialogs.openDir(null,
                "Please choose a directory root for image data",
                MMStudio.MM_DATA_SET);
        if (result != null) {
            dirRootField_.setText(result.getAbsolutePath());
            //acqEng_.setRootName(result.getAbsolutePath());
        }
    }

}
