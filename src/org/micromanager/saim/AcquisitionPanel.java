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
import java.lang.Math;
import java.text.DecimalFormat;
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
    private final String COEFF3 = "acq.coeff3";
    private final String COEFF2 = "acq.coeff2";
    private final String COEFF1 = "acq.coeff1";
    private final String COEFF0 = "acq.coeff0";

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
    private JTextField coeff3Field_;
    private JTextField coeff2Field_;
    private JTextField coeff1Field_;
    private JTextField coeff0Field_;

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
        //x3 coefficient
        calPanel_.add(new JLabel("x^3: "));
        coeff3Field_ = new JTextField(
                prefs_.get(COEFF3, ""));
        setTextAttributes(coeff3Field_, componentSize);
        coeff3Field_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(COEFF3,
                        coeff3Field_.getText());
            }
        });
        calPanel_.add(coeff3Field_, "span, center, wrap");

        //x2 coefficient
        calPanel_.add(new JLabel("x^2: "));
        coeff2Field_ = new JTextField(
                prefs_.get(COEFF2, ""));
        setTextAttributes(coeff2Field_, componentSize);
        coeff2Field_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(COEFF2,
                        coeff2Field_.getText());
            }
        });
        calPanel_.add(coeff2Field_, "span, center, wrap");

        //x coefficient
        calPanel_.add(new JLabel("x: "));
        coeff1Field_ = new JTextField(
                prefs_.get(COEFF1, ""));
        setTextAttributes(coeff1Field_, componentSize);
        coeff1Field_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(COEFF3,
                        coeff1Field_.getText());
            }
        });
        calPanel_.add(coeff1Field_, "span, center, wrap");

        //x0 constant
        calPanel_.add(new JLabel("x^0: "));
        coeff0Field_ = new JTextField(
                prefs_.get(COEFF0, ""));
        setTextAttributes(coeff0Field_, componentSize);
        coeff0Field_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(COEFF0,
                        coeff0Field_.getText());
            }
        });
        calPanel_.add(coeff0Field_, "span, center, wrap");
        
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
   

    public void calibrationChanged(double x3, double x2, double x1, double x0){
        String coeff0 = new DecimalFormat("0.#########").format(x0);
        prefs_.put(COEFF0, coeff0);
        coeff0Field_.setText(coeff0);
        
        String coeff1 = new DecimalFormat("0.#########").format(x1);
        prefs_.put(COEFF1, coeff1);
        coeff1Field_.setText(coeff1);

        String coeff2 = new DecimalFormat("0.#########").format(x2);
        prefs_.put(COEFF2, coeff2);
        coeff2Field_.setText(coeff2);

        String coeff3 = new DecimalFormat("0.#########").format(x3);
        prefs_.put(COEFF3, coeff3);
        coeff3Field_.setText(coeff3);
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

    /**
     * User is supposed to set up the acquisition in the micromanager panel.
     * This function will acquire images at angle positions defined by
     * calibration.
     *
     */
    private void RunAcquisition() {

//        private static double tirfPosFromAngle (double angle) {
//	 // TirfPosition = slope * angle plus Offset
//            x3coeff = 0.016838755631180584; //*Math.pow(10,exponent);
//            x2coeff = 0.07731206778126176; //*Math.pow(10,exponent);
//            x1coeff = -370.17939250464156;
//            x0coeff = 31720.055884293408;
//
//            double pos = x3coeff * Math.pow(angle, 3)
//                    + x2coeff * Math.pow(angle, 2)
//                    + x1coeff * angle
//                    + x0coeff;
//            return pos;
//        }
//
//        // Set these variables to the correct values and leave
//        deviceName = "TITIRF";
//        propName = "Position";
//
// // Usually no need to edit below this line
//        pos = STARTANGLE;
//        endAngle = -1 * STARTANGLE;
//        nrAngles = Math.abs(STARTANGLE) * 2 / ANGLESTEPSIZE;
//
//        gui_.closeAllAcquisitions();
//        acq = gui_.getUniqueAcquisitionName(NAMEPREFIX);
//        gui_.openAcquisition(acq, "", 1, 1, nrAngles + 2, 1,
//                true, // Show
//                false); // Save
//
//// First take images from start to 90 degrees
//        pos = STARTANGLE;
//        nrAngles1 = nrAngles / 2;
//        int image = 0;
//        for (; image <= nrAngles1; image++) {
//            val = tirfPosFromAngle(pos);
//            gui_.message("Image: " + image + ", angle: " + pos + ", val: " + val);
//            core_.setProperty(deviceName, propName, val);
//            core_.waitForDevice(deviceName);
//            //gui.sleep(250);
//            core_.snapImage();
//            taggedImg = core_.getTaggedImage();
//            taggedImg.tags.put("Angle", pos);
//            gui_.addImageToAcquisition(acq, 0, 0, image, 0, taggedImg);
//            pos += angleStepSize;
//        }
//
//// then take images from 0 degrees to (0 - startposition) degrees
//        pos = 0;
//        nrAngles2 = nrAngles / 2 + 1;
//        for (; image <= nrAngles1 + nrAngles2; image++) {
//            val = tirfPosFromAngle(pos);
//            gui_.message("Image: " + image + ", angle: " + pos + ", val: " + val);
//            core_.setProperty(deviceName, propName, val);
//            core_.waitForDevice(deviceName);
//            //gui.sleep(250);
//            core_.snapImage();
//            taggedImg = core_.getTaggedImage();
//            taggedImg.tags.put("Angle", pos);
//            gui_.addImageToAcquisition(acq, 0, 0, image, 0, taggedImg);
//            pos += angleStepSize;
//        }
//
//        gui_.closeAcquisition(acq);

    }

}
