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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import mmcorej.CharVector;
import mmcorej.DeviceType;
import mmcorej.StrVector;
import net.miginfocom.swing.MigLayout;
import org.jfree.data.xy.XYSeries;
import org.micromanager.api.ScriptInterface;
import org.micromanager.saim.exceptions.SAIMException;
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

    //private final JTextField zeroMotorPosField_;
    private final JComboBox serialPortBox_;
    private final JComboBox tirfDeviceBox_;
    private final JComboBox tirfPropBox_;
    //private final JLabel offsetLabel_;
    private final JTextField sampleRIField_;
    private final JTextField immersionRIField_;
    private final JTextField startMotorPosField_;
    private final JTextField endMotorPosField_;
    private final JSpinner numberOfCalibrationStepsSpinner_;
    private final JToggleButton runButton_;
    private final JLabel channelField_;
    private final JButton updateChannelButton_;
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

        setupPanel.add(new JLabel("Select Serial Port:"));
        serialPortBox_ = new JComboBox();
        serialPortBox_.setMaximumSize(componentSize);
        serialPortBox_.setMinimumSize(componentSize);
        StrVector serialPorts = core_.getLoadedDevicesOfType(DeviceType.SerialDevice);
        for (int i = 0; i < serialPorts.size(); i++) {
            serialPortBox_.addItem(serialPorts.get(i));
        }
        if (serialPorts.size() > 0) {
            serialPortBox_.setSelectedItem(prefs_.get(PrefUtils.SERIALPORT, serialPorts.get(0)));
        }
        serialPortBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                prefs_.put(PrefUtils.SERIALPORT, (String) serialPortBox_.getSelectedItem());
            }
        });
        setupPanel.add(serialPortBox_, "span, wrap");

        setupPanel.add(new JLabel("Select TIRF motor device:"));
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
        tirfDeviceBox_.setSelectedItem(prefs_.get(PrefUtils.TIRFDEVICE, ""));
        setupPanel.add(tirfDeviceBox_, "span, wrap");

        //Add TIRF motor position property name
        setupPanel.add(new JLabel("Select position property:"));
        tirfPropBox_ = new JComboBox();
        tirfPropBox_.setMaximumSize(componentSize);
        tirfPropBox_.setMinimumSize(componentSize);
        setupPanel.add(tirfPropBox_, "wrap");

        tirfDeviceBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updateDeviceGUI();
            }
        });

        // Calibrate Panel
        JPanel runPanel = new JPanel(new MigLayout(
                "", ""));
        runPanel.setBorder(GuiUtils.makeTitledBorder("Run"));
        final Dimension calBoxSize = new Dimension(130, 30);

        // sampleRI
        runPanel.add(new JLabel("Refractive Index of Sample:"));
        sampleRIField_ = new JTextField("0");
        setTextAttributes(sampleRIField_, calBoxSize);
        GuiUtils.tieTextFieldToPrefs(prefs_, sampleRIField_, PrefUtils.SAMPLERI);
        runPanel.add(sampleRIField_, "span, growx, wrap");

        // immersion RI
        runPanel.add(new JLabel("Refractive Index of Immersion Medium:"));
        immersionRIField_ = new JTextField("0");
        setTextAttributes(immersionRIField_, calBoxSize);
        GuiUtils.tieTextFieldToPrefs(prefs_, immersionRIField_, PrefUtils.IMMERSIONRI);
        runPanel.add(immersionRIField_, "span, growx, wrap");

        // start motor position
        runPanel.add(new JLabel("Start Motor Position:"));
        startMotorPosField_ = new JTextField("0");
        setTextAttributes(startMotorPosField_, calBoxSize);
        GuiUtils.tieTextFieldToPrefs(prefs_, startMotorPosField_, PrefUtils.STARTMOTORPOS);
        runPanel.add(startMotorPosField_, "span, growx, wrap");

        runPanel.add(new JLabel("End Motor Position:"));
        endMotorPosField_ = new JTextField("0");
        setTextAttributes(endMotorPosField_, calBoxSize);
        GuiUtils.tieTextFieldToPrefs(prefs_, endMotorPosField_, PrefUtils.ENDMOTORPOS);
        runPanel.add(endMotorPosField_, "span, growx, wrap");

        runPanel.add(new JLabel("Number of Calibration Steps"));
        numberOfCalibrationStepsSpinner_ = new JSpinner(new SpinnerNumberModel(100, 0, 400, 1));
        numberOfCalibrationStepsSpinner_.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                prefs_.putInt(PrefUtils.NUMCALSTEPS, (Integer) numberOfCalibrationStepsSpinner_.getValue());
            }
        });
        runPanel.add(numberOfCalibrationStepsSpinner_, "span, growx, wrap");

        runButton_ = new JToggleButton("Run Calibration");
        runButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO add your handling code here:
                if (runButton_.isSelected()) {
                    runButton_.setText("Abort Calibration");
                    runCalibration();
                } else {
                    runButton_.setText("Run Calibration");
                }
            }
        });
        
        // set zero motor position for calculating offset
        //setupPanel.add(new JLabel("Set 0 Deg. Motor Position:"));
        //zeroMotorPosField_ = new JTextField("0");
        //setTextAttributes(zeroMotorPosField_, componentSize);
        //GuiUtils.tieTextFieldToPrefs(prefs_, zeroMotorPosField_, PrefUtils.ZEROMOTORPOS);
        //setupPanel.add(zeroMotorPosField_, "span, growx, wrap");

        // calculate offset button
        JButton calcOffsetButton = new JButton("Detect Once");
        calcOffsetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runOffsetCalc();
            }
        });
        runPanel.add(calcOffsetButton, "span, center, wrap");
        runPanel.add(runButton_, "span, center, wrap");


        //setupPanel.add(new JLabel("Detector Offset:"));
        //offsetLabel_ = new JLabel("");
        //setupPanel.add(offsetLabel_, "wrap");

        
        // Calibration Panel
        JPanel calibrationPanel = new JPanel(new MigLayout(
                "", ""));
        calibrationPanel.setBorder(GuiUtils.makeTitledBorder("Current Calibration"));
        
        //Current channel
        channelField_ = new JLabel("Channel: ");
        calibrationPanel.add(channelField_);
        updateChannelButton_ = new JButton("Update");
        updateChannelButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateChannelCalibration();
            }
        });
        calibrationPanel.add(updateChannelButton_, "span, center, wrap");

        fitLabel_ = new JLabel("Polynomial fit result: ");
        calibrationPanel.add(fitLabel_, "span 2, wrap");

        // Combine them all
        add(setupPanel, "span, growx, wrap");
        add(runPanel, "span, growx, wrap");
        add(calibrationPanel, "span, growx, wrap");
        updateGUIFromPrefs();
        updateChannelCalibration();

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

    private void updateChannelCalibration() {
        try {
            String channelGroup = core_.getChannelGroup();
            prefs_.put(PrefUtils.CHANNEL, channelGroup + ": " + core_.getCurrentConfig(channelGroup));
            channelField_.setText(prefs_.get(PrefUtils.CHANNEL, ""));
            String coeff3 = new DecimalFormat("0.###E0").format(
                    Double.parseDouble(PrefUtils.parseCal(3, prefs_, gui_)));
            String coeff2 = new DecimalFormat("0.###E0").format(
                    Double.parseDouble(PrefUtils.parseCal(2, prefs_, gui_)));
            String coeff1 = new DecimalFormat("0.###E0").format(
                    Double.parseDouble(PrefUtils.parseCal(1, prefs_, gui_)));
            String offset = new DecimalFormat("#.##").format(
                    Double.parseDouble(PrefUtils.parseCal(0, prefs_, gui_)));
            fitLabel_.setText("y = " + coeff3 + "* x^3 + " + coeff2 + "* x^2 + " + coeff1 + "x + " + offset);
        } catch (Exception ex) {
            ij.IJ.log(ex.getMessage());
            fitLabel_.setText("Uncalibrated");
        }
    }

    /**
     * User is supposed to direct the laser beam so that it goes straight up
     * This function opens the shutter and reads out the position of the beam on
     * both CCDs. These values determine the 0 angle in the final polynomial fit
     *
     */
    private void runOffsetCalc() {
        //final double zeroPos = Double.parseDouble(prefs_.get(PrefUtils.ZEROMOTORPOS, "0.0"));
        final String deviceName = tirfDeviceBox_.getSelectedItem().toString();
        final String propName = tirfPropBox_.getSelectedItem().toString();
        //Initialize xyseries to collect pixel intensity values

        double currentPos = 0;
        try {
            currentPos = Double.parseDouble(core_.getPropertyFromCache(deviceName, propName));
        } catch (Exception ex) {
            ij.IJ.log("Motor position cannot be converted to double, is setup correct?");
        }
        try {
            core_.setShutterOpen(true);
            Point2D.Double offsetVal = takeSnapshot(currentPos, "Intensity Profile");
            core_.setShutterOpen(false);
            if (offsetVal != null) {
                Double offset = offsetVal.x - offsetVal.y;
                ij.IJ.log("Detector offset: " + offset + "\n");
                //String printOffset = new DecimalFormat("#.###").format(offset);
                //offsetLabel_.setText("" + printOffset);
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

            // Read any junk remaining in serial port buffer
            CharVector tmp = core_.readFromSerialPort(port);
            if (!tmp.isEmpty()) {
                ij.IJ.log("Found " + tmp.size() + " characters in serial port buffer");
            }

            // log time for optimization purposes (can deleted afterwards)
            long startTime = System.currentTimeMillis();

            //Send command to calibration device, record pixel intensity vales
            core_.setSerialPortCommand(port, "1", "");

            // read binary data from Arduino
            byte[] buffer = new byte[6144];
            int charsRead = 0;
            long timeOut = System.currentTimeMillis() + 4500;
            while (charsRead < 6144 && System.currentTimeMillis() < timeOut) {
                tmp = core_.readFromSerialPort(port);
                for (int j = 0; j < tmp.size(); j++) {
                    buffer[charsRead + j] = (byte) tmp.get(j);
                }
                charsRead += tmp.size();
            }
            if (charsRead != 6144) {
                throw new Exception("Device did not send epected data: Received only " + charsRead + " bytes");
            }
            //ij.IJ.log("Device needed " + (System.currentTimeMillis() - startTime) + " ms to acquired and send the data");
            for (i = 0; i < 1536; i++) {
                short dect1px = shortFrom2Bytes(buffer[i * 2], buffer[i * 2 + 1]);
                short dect2px = shortFrom2Bytes(buffer[3072 + i * 2],
                        buffer[3072 + i * 2 + 1]);
                dect1readings.add(i, dect1px);
                dect2readings.add(i, dect2px);
            }

            //Not needed for calibrator verson 3.0 and beyong
            //shuffle values of detector 1 to match physical layout of pixels
            //int size = dect1readings.getItemCount();
            //XYSeries dect1readingsFlip = new XYSeries("lower", false, true);
            //for (int a = 0; a < size; a++) {
            //    Number pxvalue = dect1readings.getY(size - 1 - a);
            //    dect1readingsFlip.add(a, pxvalue);
            //}
            //setup plotting detector readings
            PlotUtils myPlotter = new PlotUtils(prefs_);
            XYSeries[] toPlot = new XYSeries[4];
            toPlot[0] = dect1readings;
            toPlot[1] = dect2readings;
            boolean[] showShapes = {true, true, false, false};

            //Fit result to a gaussian
            double[] result1 = new double[4];
            double[] result2 = new double[4];
            toPlot[2] = new XYSeries(3);
            toPlot[3] = new XYSeries(4);
            try {
                result1 = Fitter.fit(dect1readings, Fitter.FunctionType.Gaussian, null);
                toPlot[2] = Fitter.getFittedSeries(dect1readings, Fitter.FunctionType.Gaussian, result1);
                ij.IJ.log("Dectector 1 Mean: " + result1[1] + "\n");
                result2 = Fitter.fit(dect2readings, Fitter.FunctionType.Gaussian, null);
                toPlot[3] = Fitter.getFittedSeries(dect2readings, Fitter.FunctionType.Gaussian, result2);
                ij.IJ.log("Dectector 2 Mean: " + result2[1] + "\n");
            } catch (Exception ex) {
                ij.IJ.log("Fit failed");
            }

            //Plot detector readings and gaussian fits
            myPlotter.plotDataN(plotTitle, toPlot, "Pixel", "Intensity", showShapes, "Pos: " + pos);

            //Return gaussian means as detectorMeans object
            return new Point2D.Double(result1[1], result2[1]);

        } catch (Exception ex) {
            ex.printStackTrace();;
            ij.IJ.log(ex.getMessage() + "\nRan until # " + i);
        }
        return null;
    }

    private short shortFrom2Bytes(byte byte1, byte byte2) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(byte1);
        bb.put(byte2);
        return bb.getShort(0);
    }

    /**
     * Runs the calibration itself in its own thread.
     *
     *
     */
    private void runCalibration() {

        class CalThread extends Thread {

            CalThread(String threadName) {
                super(threadName);
            }

            @Override
            public void run() {

                try {
                    //Check for channel group before running calibration
                    if (core_.getChannelGroup().equals("")) {
                        ij.IJ.error("Set channel group in Multi-D Acquisition Panel");
                        throw new SAIMException("Channel group is not defined");
                    }
                    //Check for offset before running calibration
                    Double detectorOffset = 0.0;
                    //try {
                    //    Double tmp = Double.parseDouble(offsetLabel_.getText());
                    //} catch (Exception e) {
                    //    ij.IJ.error("Calculate offset before running calibration");
                    //    throw new SAIMException("Offset was not set");
                    //}
                    //if ((offsetLabel_.getText()) != null) {
                    //    detectorOffset = Double.parseDouble(offsetLabel_.getText());
                    //} else {
                    //    detectorOffset = 0.0;
                    //}
                    // Parse editable variables for calibration
                    double startPosition;
                    String tmpString = "";
                    try {
                        tmpString = prefs_.get(PrefUtils.STARTMOTORPOS, "0.0");
                        startPosition = Double.parseDouble(tmpString);
                    } catch (NumberFormatException nfe) {
                        ij.IJ.error("Failed to parse Start Motor Position \"" + tmpString
                                + "\" to a numeric value");
                        throw new SAIMException("Failure parsing start motor position");
                    }
                    double endPosition;
                    try {
                        tmpString = prefs_.get(PrefUtils.ENDMOTORPOS, "0.0");
                        endPosition = Double.parseDouble(tmpString);
                    } catch (NumberFormatException nfe) {
                        ij.IJ.error("Failed to parse End Motor Position \"" + tmpString
                                + "\" to a numeric value");
                        throw new SAIMException("Failure parsing end motor position");
                    }
                    final int nrAngles = prefs_.getInt(PrefUtils.NUMCALSTEPS, 0);
                    final double angleStepSize = (endPosition - startPosition) / nrAngles;
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

                    //Determine angle of laser light at each motor position
                    XYSeries observedAngles = new XYSeries("angles", false, true);
                    for (int l = 0; l <= nrAngles; l++) {
                        Double motorPosition = dect1gaussianMeans.getX(l).doubleValue();
                        Double dect1val = dect1gaussianMeans.getY(l).doubleValue();
                        Double dect2val = detectorOffset + dect2gaussianMeans.getY(l).doubleValue();
                        //pixel center to center distance is 63.5 um 
                        double xdisp = (dect1val.floatValue() - dect2val.floatValue()) * 0.0635;
                        //detector1 center to detector2 center is 12.95 mm (old detector design was 20.64 mm)
                        double ydisp = 12.95;
                        Double observedAngle = Math.toDegrees(Math.atan(xdisp / ydisp));
                        //Snells law correction angle of laser light for refractive index 
                        //Refractive indeces: acrylic = 1.49, water = 1.33, user input = RI
                        //determine true angle coming out of objective (correct for acrylic)
                        double immersionRI = Double.parseDouble(prefs_.get(PrefUtils.IMMERSIONRI, "1.33"));
                        double sampleRI = Double.parseDouble(prefs_.get(PrefUtils.SAMPLERI, "1.33"));
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
                    String calString = java.util.Arrays.toString(calCurve);
                    String chString = PrefUtils.channelCoeffKey(prefs_, gui_);
                    prefs_.put(chString, calString);
                    //Print cal as stored in preferences (for debugging)
                    //ij.IJ.log(chString + prefs_.get(chString, ""));
                    //Print parsed coeffs (for debugging)
                    //ij.IJ.log(PrefUtils.parseCal(3, prefs_, gui_));
                    //ij.IJ.log(PrefUtils.parseCal(2, prefs_, gui_));
                    //ij.IJ.log(PrefUtils.parseCal(1, prefs_, gui_));
                    //ij.IJ.log(PrefUtils.parseCal(0, prefs_, gui_));

                    String coeff3 = new DecimalFormat("0.###E0").format(Double.parseDouble(PrefUtils.parseCal(3, prefs_, gui_)));
                    String coeff2 = new DecimalFormat("0.###E0").format(Double.parseDouble(PrefUtils.parseCal(2, prefs_, gui_)));
                    String coeff1 = new DecimalFormat("0.###E0").format(Double.parseDouble(PrefUtils.parseCal(1, prefs_, gui_)));
                    String offset = new DecimalFormat("#.##").format(Double.parseDouble(PrefUtils.parseCal(0, prefs_, gui_)));

                    prefs_.put(PrefUtils.CHANNEL, core_.getCurrentConfig("Channel"));
                    channelField_.setText("Channel: " + prefs_.get(PrefUtils.CHANNEL, ""));
                    fitLabel_.setText("y = " + coeff3 + "* x^3 + " + coeff2 + "* x^2 + " + coeff1 + "x + " + offset);
                    ij.IJ.log("Channel: " + core_.getCurrentConfig("Channel"));
                    ij.IJ.log("y = " + PrefUtils.parseCal(3, prefs_, gui_) + "* x^3 + " + PrefUtils.parseCal(2, prefs_, gui_) + "* x^2 + " + PrefUtils.parseCal(1, prefs_, gui_) + "x + " + PrefUtils.parseCal(0, prefs_, gui_));

                } catch (Exception ex) {
                    ij.IJ.log(ex.getMessage());
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
    public final void updateGUIFromPrefs() {
        //zeroMotorPosField_.setText(prefs_.get(PrefUtils.ZEROMOTORPOS, "0.0"));
        serialPortBox_.setSelectedItem(prefs_.get(PrefUtils.SERIALPORT, ""));
        tirfDeviceBox_.setSelectedItem(prefs_.get(PrefUtils.TIRFDEVICE, ""));
        updateDeviceGUI();
        //tirfPropBox_.setSelectedItem(prefs_.get(PrefStrings.TIRFPROP, ""));
        sampleRIField_.setText(prefs_.get(PrefUtils.SAMPLERI, ""));
        immersionRIField_.setText(prefs_.get(PrefUtils.IMMERSIONRI, ""));
        startMotorPosField_.setText(prefs_.get(PrefUtils.STARTMOTORPOS, ""));
        endMotorPosField_.setText(prefs_.get(PrefUtils.ENDMOTORPOS, ""));
        numberOfCalibrationStepsSpinner_.setValue(Integer.parseInt(
                prefs_.get(PrefUtils.NUMCALSTEPS, "1")));
        updateChannelCalibration();
    }

    /**
     * Updates the device property dropdown box when the TIRF motor device
     * dropdown changes
     */
    private void updateDeviceGUI() {
        prefs_.put(PrefUtils.TIRFDEVICE, (String) tirfDeviceBox_.getSelectedItem());
        try {
            StrVector deviceProps = core_.getDevicePropertyNames(prefs_.get(PrefUtils.TIRFDEVICE, ""));
            DefaultComboBoxModel model = (DefaultComboBoxModel) tirfPropBox_.getModel();
            if (model != null) {
                for (ActionListener al : tirfPropBox_.getActionListeners()) {
                    tirfPropBox_.removeActionListener(al);
                }
                model.removeAllElements();
                for (int i = 0; i < deviceProps.size(); i++) {
                    model.addElement(deviceProps.get(i));
                }
                String propBoxItem = prefs_.get(PrefUtils.TIRFPROP, "");
                tirfPropBox_.setSelectedItem(propBoxItem);
                ActionListener[] als = tirfPropBox_.getActionListeners();
                if (als.length == 0) {
                    tirfPropBox_.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            Object selectedItem = tirfPropBox_.getSelectedItem();
                            if (selectedItem != null) {
                                prefs_.put(PrefUtils.TIRFPROP, (String) selectedItem);
                            }

                        }
                    });
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CalibrationPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
