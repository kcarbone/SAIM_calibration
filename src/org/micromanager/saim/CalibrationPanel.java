 ///////////////////////////////////////////////////////////////////////////////
//FILE:          CalibrationPanel
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
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
import mmcorej.DeviceType;
import mmcorej.StrVector;
import net.miginfocom.swing.MigLayout;
import org.jfree.data.xy.XYSeries;
import org.micromanager.api.ScriptInterface;
import org.micromanager.saim.fit.Fitter;
import org.micromanager.saim.gui.GuiUtils;
import org.micromanager.saim.plot.PlotUtils;

/**
 *
 * @author nico
 */
public class CalibrationPanel extends JPanel {

    private final ScriptInterface gui_;
    private final Preferences prefs_;
    private final CMMCore core_;

    private final JTextField zeroMotorPosField_;
    private final JComboBox serialPortBox_;
    private final JComboBox tirfDeviceBox_;
    private final JComboBox tirfPropBox_;
    private final JLabel offsetLabel_;
    private final JTextField sampleRIField_;
    private final JTextField immersionRIField_;
    private final JTextField startMotorPosField_;
    private final JTextField endMotorPosField_;
    private final JSpinner numberOfCalibrationStepsSpinner_;
    private final JToggleButton runButton_;
    private final JLabel fitLabel_;

    public CalibrationPanel(ScriptInterface gui, Preferences prefs) throws Exception {
        super(new MigLayout(
                "",
                ""));
        gui_ = gui;
        core_ = gui_.getMMCore();
        prefs_ = prefs;
        this.setName("Calibration");

        // Setup Panel
        JPanel setupPanel = new JPanel(new MigLayout(
                "", ""));
        setupPanel.setBorder(GuiUtils.makeTitledBorder("Setup"));

        final Dimension componentSize = new Dimension(150, 30);

        setupPanel.add(new JLabel("Select Serial Port"));
        serialPortBox_ = new JComboBox();
        serialPortBox_.setMaximumSize(componentSize);
        serialPortBox_.setMinimumSize(componentSize);
        StrVector serialPorts = core_.getLoadedDevicesOfType(DeviceType.SerialDevice);
        for (int i = 0; i < serialPorts.size(); i++) {
            serialPortBox_.addItem(serialPorts.get(i));
        }
        if (serialPorts.size() > 0) {
            serialPortBox_.setSelectedItem(prefs_.get(PrefStrings.SERIALPORT, serialPorts.get(0)));
        }
        serialPortBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                prefs_.put(PrefStrings.SERIALPORT, (String) serialPortBox_.getSelectedItem());
            }
        });
        setupPanel.add(serialPortBox_, "wrap");

        setupPanel.add(new JLabel("Select TIRF motor device"));
        tirfDeviceBox_ = new JComboBox();
        tirfDeviceBox_.setMaximumSize(componentSize);
        tirfDeviceBox_.setMinimumSize(componentSize);
        StrVector genericPorts = core_.getLoadedDevicesOfType(DeviceType.GenericDevice);
        for (int i = 0; i < genericPorts.size(); i++) {
            tirfDeviceBox_.addItem(genericPorts.get(i));
        }
        StrVector stagePorts = core_.getLoadedDevicesOfType(DeviceType.StageDevice);
        for (int i = 0; i < stagePorts.size(); i++) {
            tirfDeviceBox_.addItem(stagePorts.get(i));
        }
        tirfDeviceBox_.setSelectedItem(prefs_.get(PrefStrings.TIRFDEVICE, ""));
        tirfDeviceBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                prefs_.put(PrefStrings.TIRFDEVICE, (String) tirfDeviceBox_.getSelectedItem());
                try {
                    StrVector deviceProps = core_.getDevicePropertyNames(prefs_.get(PrefStrings.TIRFDEVICE, ""));
                    DefaultComboBoxModel model = (DefaultComboBoxModel) tirfPropBox_.getModel();
                    model.removeAllElements();
                    for (int i = 0; i < deviceProps.size(); i++) {
                        model.addElement(deviceProps.get(i));
                    }
                } catch (Exception ex) {
                    Logger.getLogger(CalibrationPanel.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        setupPanel.add(tirfDeviceBox_, "wrap");

        //Add TIRF motor position property name
        setupPanel.add(new JLabel("Select position property"));
        tirfPropBox_ = new JComboBox();
        tirfPropBox_.setMaximumSize(componentSize);
        tirfPropBox_.setMinimumSize(componentSize);
        tirfPropBox_.setSelectedItem(prefs_.get(PrefStrings.TIRFPROP, ""));
        tirfPropBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                prefs_.put(PrefStrings.TIRFPROP, (String) tirfPropBox_.getSelectedItem());
            }
        });
        setupPanel.add(tirfPropBox_, "wrap");

        // set zero motor position for calculating offset
        setupPanel.add(new JLabel("Set 0 Deg. Motor Position:"));
        zeroMotorPosField_ = new JTextField("0");
        setTextAttributes(zeroMotorPosField_, componentSize);
        zeroMotorPosField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(PrefStrings.ZEROMOTORPOS, zeroMotorPosField_.getText());
            }
        });
        setupPanel.add(zeroMotorPosField_, "span, growx, wrap");

        // calculate offset button
        JButton calcOffsetButton = new JButton("Calculate Offset");
        calcOffsetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RunOffsetCalc();
            }
        });
        setupPanel.add(calcOffsetButton, "span 2, center, wrap");

        setupPanel.add(new JLabel("Detector Offset:"));
        offsetLabel_ = new JLabel("");
        setupPanel.add(offsetLabel_, "wrap");

        // Calibrate Panel
        JPanel calibratePanel = new JPanel(new MigLayout(
                "", ""));
        calibratePanel.setBorder(GuiUtils.makeTitledBorder("Calibrate"));
        final Dimension calBoxSize = new Dimension(130, 30);

        // sampleRI
        calibratePanel.add(new JLabel("Refractive Index of Sample:"));
        sampleRIField_ = new JTextField("0");
        setTextAttributes(sampleRIField_, calBoxSize);
        sampleRIField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(PrefStrings.SAMPLERI, sampleRIField_.getText());
            }
        });
        calibratePanel.add(sampleRIField_, "span, growx, wrap");

        // immersion RI
        calibratePanel.add(new JLabel("Refractive Index of Immersion Medium:"));
        immersionRIField_ = new JTextField("0");
        setTextAttributes(immersionRIField_, calBoxSize);
        immersionRIField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(PrefStrings.IMMERSIONRI, immersionRIField_.getText());
            }
        });
        calibratePanel.add(immersionRIField_, "span, growx, wrap");

        // start motor position
        calibratePanel.add(new JLabel("Start Motor Position:"));
        startMotorPosField_ = new JTextField("0");
        setTextAttributes(startMotorPosField_, calBoxSize);
        startMotorPosField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(PrefStrings.STARTMOTORPOS, startMotorPosField_.getText());
            }
        });
        calibratePanel.add(startMotorPosField_, "span, growx, wrap");

        calibratePanel.add(new JLabel("End Motor Position:"));
        endMotorPosField_ = new JTextField("0");
        setTextAttributes(endMotorPosField_, calBoxSize);
        endMotorPosField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(PrefStrings.ENDMOTORPOS, endMotorPosField_.getText());
            }
        });
        calibratePanel.add(endMotorPosField_, "span, growx, wrap");

        calibratePanel.add(new JLabel("Number of Calibration Steps"));
        numberOfCalibrationStepsSpinner_ = new JSpinner(new SpinnerNumberModel(100, 0, 400, 1));
        numberOfCalibrationStepsSpinner_.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                prefs_.putInt(PrefStrings.NUMCALSTEPS, (Integer) numberOfCalibrationStepsSpinner_.getValue());
            }
        });
        calibratePanel.add(numberOfCalibrationStepsSpinner_, "span, growx, wrap");

        runButton_ = new JToggleButton("Run Calibration");
        runButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO add your handling code here:
                if (runButton_.isSelected()) {
                    runButton_.setText("Abort Calibration");
                    RunCalibration();
                } else {
                    runButton_.setText("Run Calibration");
                }
            }
        });
        calibratePanel.add(runButton_, "span 2, center, wrap");

        fitLabel_ = new JLabel("Polynomial fit result: ");
        calibratePanel.add(fitLabel_, "span 2, wrap");

        // Combine them all
        add(setupPanel, "span, growx, wrap");
        add(calibratePanel, "span, growx, wrap");
        UpdatePrefs();
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
     * User is supposed to direct the laser beam so that it goes straight up
     * This function opens the shutter and reads out the position of the beam on
     * both CCDs. These values determine the 0 angle in the final polynomial fit
     *
     */
    private void RunOffsetCalc() {
        final double zeroPos = Double.parseDouble(PrefStrings.ZEROMOTORPOS);
        try {
            core_.setShutterOpen(true);
            Point2D.Double offsetVal = takeSnapshot(zeroPos, "Offset Scan");
            core_.setShutterOpen(false);
            if (offsetVal != null) {
                Double offset = offsetVal.x - offsetVal.y;
                ij.IJ.log("Detector offset: " + offset + "\n");
                String printOffset = new DecimalFormat("#.###").format(offset);
                offsetLabel_.setText("" + printOffset);
            }
        } catch (Exception ex) {
            ij.IJ.log(ex.getMessage() + ", Failed to open/close the shutter");
        }
    }

    /**
     * Opens the shutter, reads out the center position on the top and bottom
     * CCD, returns these position (as a Point2D.Double
     *
     * @param pos position of the "TIRF" motor
     * @param plotTitle String used as title in the plot of the CCD readout
     * @return Point2D.double. x = bottom CCD, y = top CCD
     */
    private Point2D.Double takeSnapshot(double pos, String plotTitle) {
        int i = 0;
        try {
            //Set up communication with devices
            final String port = serialPortBox_.getSelectedItem().toString();
            final String deviceName = tirfDeviceBox_.getSelectedItem().toString();
            final String propName = "Position";
            //Initialize xyseries to collect pixel intensity values
            XYSeries dect1readings = new XYSeries("lower", false, true);
            XYSeries dect2readings = new XYSeries("upper", false, true);
            //Set motor position
            core_.setProperty(deviceName, propName, pos);
            core_.waitForDevice(deviceName);
            ij.IJ.log("Pos: " + pos);
            //Send command to calibration device, record pixel intensity vales
            core_.setSerialPortCommand(port, "1", "");
            for (i = 0; i < 1536; i++) {
                String answer = core_.getSerialPortAnswer(port, "\n");
                String[] vals = answer.trim().split("\\t");
                if (vals.length == 2) {
                    int dect1px = Integer.valueOf(vals[0]);
                    int dect2px = Integer.valueOf(vals[1]);
                    dect1readings.add(i, dect1px);
                    dect2readings.add(i, dect2px);
                } else {
                    System.out.println("Val is not 2: " + answer);
                }
            }

            //shuffle values of detector 1 to match physical layout of pixels
            int size = dect1readings.getItemCount();
            XYSeries dect1readingsFlip = new XYSeries("lower", false, true);
            for (int a = 0; a < size; a++) {
                Number pxvalue = dect1readings.getY(size - 1 - a);
                dect1readingsFlip.add(a, pxvalue);
            }
            //setup plotting detector readings
            PlotUtils myPlotter = new PlotUtils(prefs_);
            XYSeries[] toPlot = new XYSeries[4];
            toPlot[0] = dect1readingsFlip;
            toPlot[1] = dect2readings;
            boolean[] showShapes = {true, true, false, false};

            //Fit result to a gaussian
            double[] result1 = Fitter.fit(dect1readingsFlip, Fitter.FunctionType.Gaussian, null);
            toPlot[2] = Fitter.getFittedSeries(dect1readingsFlip, Fitter.FunctionType.Gaussian, result1);
            ij.IJ.log("Dectector 1 Mean: " + result1[1] + "\n");
            double[] result2 = Fitter.fit(dect2readings, Fitter.FunctionType.Gaussian, null);
            toPlot[3] = Fitter.getFittedSeries(dect2readings, Fitter.FunctionType.Gaussian, result2);
            ij.IJ.log("Dectector 2 Mean: " + result2[1] + "\n");

            //Plot detector readings and gaussian fits
            myPlotter.plotDataN(plotTitle, toPlot, "Pixel", "Intensity", showShapes, "Pos: " + pos);

            //Return gaussian means as detectorMeans object
            return new Point2D.Double(result1[1], result2[1]);

        } catch (Exception ex) {
            ij.IJ.log(ex.getMessage() + "\nRan until # " + i);
        }
        return null;
    }

    /**
     * Runs the calibration itself in its own thread.
     *
     *
     */
    private void RunCalibration() {

        class CalThread extends Thread {

            CalThread(String threadName) {
                super(threadName);
            }

            @Override
            public void run() {
                // Editable variables for calibration
                final double startPosition = Double.parseDouble(PrefStrings.STARTMOTORPOS);
                final double endPosition = Double.parseDouble(PrefStrings.ENDMOTORPOS);
                final int nrAngles = prefs_.getInt(PrefStrings.NUMCALSTEPS, 0);
                final double angleStepSize = (endPosition - startPosition) / nrAngles;
                int i = 0;
                try {
                    //Take image of laser position
                    XYSeries dect1gaussianMeans = new XYSeries(new Double(nrAngles), false, true);
                    XYSeries dect2gaussianMeans = new XYSeries(new Double(nrAngles), false, true);
                    core_.setShutterOpen(true);
                    double pos = startPosition;
                    for (int angle = 0; angle <= nrAngles; angle++) {
                        //Check state of user Abort button 
                        if (runButton_.isSelected()) {
                        } else {
                            throw new Exception("User aborted calibration");
                        }
                        Point2D.Double laserPos = takeSnapshot(pos, "Saim Scan");
                        if (laserPos != null) {
                            dect1gaussianMeans.add(pos, laserPos.x);
                            dect2gaussianMeans.add(pos, laserPos.y);
                            pos = pos + angleStepSize;
                        }
                    }
                    //R ead offset if calculated
                    Double detectorOffset;
                    if ((offsetLabel_.getText()) != null) {
                        detectorOffset = Double.parseDouble(offsetLabel_.getText());
                    } else {
                        detectorOffset = 0.0;
                    }
                    //Determine angle of laser light at each motor position
                    XYSeries observedAngles = new XYSeries("angles", false, true);
                    for (int l = 0; l <= nrAngles; l++) {
                        Double motorPosition = dect1gaussianMeans.getX(l).doubleValue();
                        Double dect1val = dect1gaussianMeans.getY(l).doubleValue();
                        Double dect2val = dect2gaussianMeans.getY(l).doubleValue() + detectorOffset;
                        //pixel center to center distance is 63.5 um
                        double xdisp = (dect1val.floatValue() - dect2val.floatValue()) * 0.0635;
                        //detector1 center to detector2 center is 20.64 mm
                        double ydisp = 20.64;
                        Double observedAngle = Math.toDegrees(Math.atan(xdisp / ydisp));
                        //Snells law correction angle of laser light for refractive index 
                        //Refractive indeces: acrylic = 1.49, water = 1.33, user input = RI
                        //determine true angle coming out of objective (correct for acrylic)
                        double immersionRI = Double.parseDouble(PrefStrings.IMMERSIONRI);
                        double sampleRI = Double.parseDouble(PrefStrings.SAMPLERI);
                        Double firstCorrect = snellIt(observedAngle, 1.49, immersionRI);
                        //determine true angle hitting the sample (correct for water/buffer)
                        Double trueAngle = snellIt(firstCorrect, immersionRI, sampleRI);
                        observedAngles.add(trueAngle, motorPosition);
                    }
                    //Plot calibration curve
                    PlotUtils myPlotter2 = new PlotUtils(prefs_);
                    double[] calCurve = Fitter.fit(observedAngles, Fitter.FunctionType.Pol3, null);
                    XYSeries[] toPlot = new XYSeries[2];
                    toPlot[0] = observedAngles;
                    toPlot[1] = Fitter.getFittedSeries(toPlot[0], Fitter.FunctionType.Pol3, calCurve);
                    boolean[] showShapes = {true, false};
                    myPlotter2.plotDataN("Calibration Curve", toPlot, "True Angle", "Position", showShapes, "");
                    String coeff3 = new DecimalFormat("0.##E0").format(calCurve[3]);
                    prefs_.put(PrefStrings.COEFF3, coeff3);
                    String coeff2 = new DecimalFormat("0.##E0").format(calCurve[2]);
                    prefs_.put(PrefStrings.COEFF2, coeff2);
                    String coeff1 = new DecimalFormat("0.##E0").format(calCurve[1]);
                    prefs_.put(PrefStrings.COEFF1, coeff1);
                    String offset = new DecimalFormat("#.##").format(calCurve[0]);
                    prefs_.put(PrefStrings.COEFF0, offset);
                    fitLabel_.setText("y = " + coeff3 + "* x^3 + " + coeff2 + "* x^2 + " + coeff1 + "x + " + offset);
                    ij.IJ.log("y = " + calCurve[3] + "* x^3 + " + calCurve[2] + "* x^2 + " + calCurve[1] + "x + " + calCurve[0]);

                } catch (Exception ex) {
                    ij.IJ.log(ex.getMessage() + "\nRan until # " + i);
                } finally {
                    try {
                        core_.setShutterOpen(false);
                    } catch (Exception ex) {
                        ij.IJ.log(ex.getMessage());
                    }
                    runButton_.setText("Run Calibration");
                    runButton_.setSelected(false);
                }
            }
        }

        CalThread calt = new CalThread("SAIM Callibration");
        calt.start();
    }

    //Snell's Law function
    private static double snellIt(double startAngle, double startRI, double endRI) {
        Double trueAngle = Math.toDegrees(Math.asin((startRI / endRI) * Math.sin(Math.toRadians(startAngle))));
        return trueAngle;
    }    

//function to update panel with stored preferences values
    private void UpdatePrefs() {
        zeroMotorPosField_.setText(prefs_.get(PrefStrings.ZEROMOTORPOS, ""));
        serialPortBox_.setSelectedItem(prefs_.get(PrefStrings.SERIALPORT, ""));
        tirfDeviceBox_.setSelectedItem(prefs_.get(PrefStrings.TIRFDEVICE, ""));
        tirfPropBox_.setSelectedItem(prefs_.get(PrefStrings.TIRFPROP, ""));
        sampleRIField_.setText(prefs_.get(PrefStrings.SAMPLERI, ""));
        immersionRIField_.setText(prefs_.get(PrefStrings.IMMERSIONRI, ""));
        startMotorPosField_.setText(prefs_.get(PrefStrings.STARTMOTORPOS, ""));
        endMotorPosField_.setText(prefs_.get(PrefStrings.ENDMOTORPOS, ""));
        numberOfCalibrationStepsSpinner_.setValue(Integer.parseInt(prefs_.get(PrefStrings.NUMCALSTEPS, "")));
    }
}
