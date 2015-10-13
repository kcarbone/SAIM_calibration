 ///////////////////////////////////////////////////////////////////////////////
//FILE:          FlatFieldPanel
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

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.ZProjector;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import mmcorej.TaggedImage;
import net.miginfocom.swing.MigLayout;
import org.micromanager.api.ScriptInterface;
import org.micromanager.saim.gui.GuiUtils;
import org.micromanager.utils.FileDialogs;
import org.micromanager.MMStudio;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.acquisition.MMImageCache;
import org.micromanager.api.ImageCache;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMScriptException;

/**
 *
 * @author nico
 */
public class FlatFieldPanel extends JPanel implements ICalibrationObserver {

    ScriptInterface gui_;
    CMMCore core_;
    Preferences prefs_;

    private final String ANGLESTEPSIZE = "acq.anglestepsize";
    private final String STARTANGLE = "acq.startangle";
    private final String DOUBLEZERO = "acq.doulbezero";
    private final String SAVEIMAGES = "acq.saveimages";
    private final String DIRROOT = "acq.dirroot";
    private final String NAMEPREFIX = "acq.nameprefix";
    private final String COEFF3 = "acq.coeff3";
    private final String COEFF2 = "acq.coeff2";
    private final String COEFF1 = "acq.coeff1";
    private final String COEFF0 = "acq.coeff0";

    private final JSpinner angleStepSizeSpinner_;
    private final JTextField startAngleField_;
    private final JCheckBox doubleZeroCheckBox_;
    private final JPanel calPanel_;
    private final JCheckBox saveImagesCheckBox_;

    private final JFileChooser dirRootChooser_;
    private final JTextField dirRootField_;
    private final JButton dirRootButton_;
    private final JTextField namePrefixField_;
    private final JToggleButton runButton_;
    private JTextField coeff3Field_;
    private JTextField coeff2Field_;
    private JTextField coeff1Field_;
    private JTextField coeff0Field_;

    public FlatFieldPanel(ScriptInterface gui, Preferences prefs) {
        super(new MigLayout(
                "",
                ""));
        gui_ = gui;
        core_ = gui_.getMMCore();
        prefs_ = prefs;
        this.setName("FlatField");

        // Setup Panel
        JPanel setupPanel = new JPanel(new MigLayout(
                "", ""));
        setupPanel.setBorder(GuiUtils.makeTitledBorder("Setup"));
        final Dimension componentSize = new Dimension(150, 30);

        // set angle step size
        setupPanel.add(new JLabel("Angle Step Size (degrees):"));
        angleStepSizeSpinner_ = new JSpinner(new SpinnerNumberModel(
                prefs_.getDouble(ANGLESTEPSIZE, 100), 0, 180, 0.1));
        angleStepSizeSpinner_.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                prefs_.putDouble(ANGLESTEPSIZE, (Double) angleStepSizeSpinner_.getValue());
            }
        });
        setupPanel.add(angleStepSizeSpinner_, "span, growx, wrap");

        // set start angle
        setupPanel.add(new JLabel("Start Angle:"));
        startAngleField_ = new JTextField(
                prefs_.get(STARTANGLE, "0"));
        setTextAttributes(startAngleField_, componentSize);
        startAngleField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(STARTANGLE, startAngleField_.getText());
            }
        });
        setupPanel.add(startAngleField_, "span, growx, wrap");
        setupPanel.add(new JLabel("Start angle must be divisible by angle step size."), "span 2, wrap");

        // set double zero position
        doubleZeroCheckBox_ = new JCheckBox("Double Zero Position");
        doubleZeroCheckBox_.setSelected(true);
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
                prefs_.put(COEFF3, coeff3Field_.getText());
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
                prefs_.put(COEFF2, coeff2Field_.getText());
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
                prefs_.put(COEFF3, coeff1Field_.getText());
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
                prefs_.put(COEFF0, coeff0Field_.getText());
            }
        });
        calPanel_.add(coeff0Field_, "span, center, wrap");

        // FlatField Panel
        JPanel flatfieldPanel = new JPanel(new MigLayout(
                "", ""));
        flatfieldPanel.setBorder(GuiUtils.makeTitledBorder("FlatField"));
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
                prefs_.put(DIRROOT, dirRootChooser_.getName());
            }
        });

        // set directory root text field
        flatfieldPanel.add(new JLabel("Selecting \"Run FlatField\" will prompt you to take multiple"), "span, wrap");
        flatfieldPanel.add(new JLabel("SAIM acquisitions at different positions for correction"), "span, wrap");
        flatfieldPanel.add(new JLabel("of final images."), "span, wrap" );
        flatfieldPanel.add(new JLabel("Directory Root:"));
        dirRootField_ = new JTextField(
                prefs_.get(DIRROOT, ""));
        setTextAttributes(dirRootField_, componentSize);
        dirRootField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(DIRROOT, dirRootField_.getText());
            }
        });
        flatfieldPanel.add(dirRootField_);

        //set directory chooser button
        dirRootButton_ = new JButton("...");
        dirRootButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setRootDirectory();
            }
        });
        flatfieldPanel.add(dirRootButton_, "wrap");

        // set name prefix
        flatfieldPanel.add(new JLabel("Name Prefix:"));
        namePrefixField_ = new JTextField(
                prefs_.get(NAMEPREFIX, ""));
        setTextAttributes(namePrefixField_, componentSize);
        namePrefixField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(NAMEPREFIX, namePrefixField_.getText());
            }
        });
        flatfieldPanel.add(namePrefixField_, "span, growx, wrap");

        // set save images
        saveImagesCheckBox_ = new JCheckBox("Save Images");
        saveImagesCheckBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (saveImagesCheckBox_.isSelected()) {
                    prefs_.putBoolean(SAVEIMAGES, true);
                } else {
                    prefs_.putBoolean(SAVEIMAGES, false);
                }
            }
        });
        flatfieldPanel.add(saveImagesCheckBox_, "span 2, growx, wrap");

        // set run button
        runButton_ = new JToggleButton("Run FlatField");
        runButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO add your handling code here:
                if (runButton_.isSelected()) {
                    runButton_.setText("Abort FlatField");
                    RunFlatField();
                } else {
                    runButton_.setText("Run FlatField");
                }
            }
        });
        flatfieldPanel.add(runButton_, "span 3, center, wrap");

        // Combine them all
        add(setupPanel, "span, growx, wrap");
        add(calPanel_, "span, growx, wrap");
        add(flatfieldPanel, "span, growx, wrap");
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

    @Override
    public void calibrationChanged(double x3, double x2, double x1, double x0) {
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
    * User is supposed to set up the acquisition in the micromanager panel. This
    * function will acquire images at angle positions defined by calibration.
    *
    */
   private String RunAcquisition() {
      double startAngle = Double.parseDouble(startAngleField_.getText());
      double angleStepSize = prefs_.getDouble(ANGLESTEPSIZE, 0);
      String acq = "";
      if (startAngle % angleStepSize == 0) {
         try {
            // Set these variables to the correct values and leave
            final String deviceName = "TITIRF";
            final String propName = "Position";

            // Usually no need to edit below this line
            double tempnrAngles = Math.abs(startAngle) * 2 / angleStepSize;
            int nrAngles = (Integer) Math.round((float) tempnrAngles);

            //gui_.closeAllAcquisitions();
            acq = gui_.getUniqueAcquisitionName(namePrefixField_.getText());

            int frames = nrAngles + 1;
            if (doubleZeroCheckBox_.isSelected()) {
               frames = nrAngles + 2;
            }

            if (saveImagesCheckBox_.isSelected()) {
               gui_.openAcquisition(acq,
                       dirRootField_.getText(), 1, 1, frames, 1,
                       true, // Show
                       true); // Save <--change this to save files in root directory
            } else {
               gui_.openAcquisition(acq,
                       dirRootField_.getText(), 1, 1, frames, 1,
                       true, // Show
                       false); // Save <--change this to save files in root directory
            }

            // First take images from start to 90 degrees
            double pos = startAngle;
            int nrAngles1 = nrAngles / 2;
            for (int a = 0;
                    a <= nrAngles1;
                    a++) {
               double val = tirfPosFromAngle(pos);
               gui_.message("Image: " + Integer.toString(a) + ", angle: " + Double.toString(pos) + ", val: " + Double.toString(val));
               core_.setProperty(deviceName, propName, val);
               core_.waitForDevice(deviceName);
               //gui.sleep(250);
               core_.snapImage();
               TaggedImage taggedImg = core_.getTaggedImage();
               taggedImg.tags.put("Angle", pos);
               gui_.addImageToAcquisition(acq, 0, 0, a, 0, taggedImg);
               pos += angleStepSize;
            }

                // if doubleZeroCheckBox is selected, take images from 0 degrees
            //to (0 - startposition) degrees
            double pos1 = angleStepSize;
            int nrAngles2 = nrAngles / 2;
            if (doubleZeroCheckBox_.isSelected()) {
               pos1 = 0;
               nrAngles2 = nrAngles / 2 + 1;
            }

            for (int b = 0;
                    b <= nrAngles2;
                    b++) {
               double val = tirfPosFromAngle(pos1);
               gui_.message("Image: " + Integer.toString(b) + ", angle: " + Double.toString(pos1) + ", val: " + Double.toString(val));
               core_.setProperty(deviceName, propName, val);
               core_.waitForDevice(deviceName);
               //gui.sleep(250);
               core_.snapImage();
               TaggedImage taggedImg = core_.getTaggedImage();
               taggedImg.tags.put("Angle", pos1);
               gui_.addImageToAcquisition(acq, 0, 0, b + nrAngles1, 0, taggedImg);
               pos1 += angleStepSize;
            }

            //gui_.closeAcquisition(acq);
         } catch (Exception ex) {
            //ex.printStackTrace();
            ij.IJ.log(ex.getMessage());
            ij.IJ.error("Something went wrong.  Aborting!");
         } finally {
            runButton_.setSelected(false);
            runButton_.setText("Run FlatField");
         }
      } else {
         ij.IJ.error("Start angle is not divisible by angle step size!");
         runButton_.setSelected(false);
         runButton_.setText("Run FlatField");
      }
      return acq;
   }




    private int tirfPosFromAngle(double angle) {
        // TirfPosition = slope * angle plus Offset
        // Output motor position must be an integer to be interpreted by TITIRF

        double tempPos = (Double.parseDouble(coeff3Field_.getText()) * Math.pow(angle, 3)
                + Double.parseDouble(coeff2Field_.getText()) * Math.pow(angle, 2)
                + Double.parseDouble(coeff1Field_.getText()) * angle
                + Double.parseDouble(coeff0Field_.getText()));
        int pos = Math.round((float) tempPos);
        return pos;
    }

     /**
     * User is supposed to set up the acquisition in the micromanager panel.
     * This function will prompt the user to move the stage to 5 positions and
     * will acquire a SAIM scan (RunAcquisition) at each position
     *
     */
   private void RunFlatField() {

      class AcqThread extends Thread {

         AcqThread(String threadName) {
            super(threadName);
         }

         @Override
         public void run() {
            int count = 1;
            List<String> acqs = new ArrayList<String>();
            while (true) {
               GenericDialog okWindow = new GenericDialog("FlatField Image " + Integer.toString(count));
               okWindow.setCancelLabel("Done");
               okWindow.addMessage("Move stage to new position and click OK to start acquisition.");
               okWindow.showDialog();
               count = count + 1;
               if (okWindow.wasCanceled()) {
                  runButton_.setSelected(false);
                  runButton_.setText("Run FlatField");
                  break;
               }
               acqs.add(RunAcquisition());

            }
            //start with list of acqs, calculate median
            if (acqs.isEmpty()) {
               return;
            }
            try {
               String acq = gui_.getUniqueAcquisitionName("Flatfield");
               String[] availableNames = gui_.getAcquisitionNames();
               MMAcquisition mAcq = gui_.getAcquisition(acqs.get(0));
               ImageCache cache = gui_.getAcquisitionImageCache(acqs.get(0));
               gui_.openAcquisition(acq,
                       dirRootField_.getText(), 1, 1, mAcq.getSlices(), 1,
                       true, // Show
                       true); // Save <--change this to save files in root directory
               for (int slice = 0; slice < mAcq.getSlices(); slice++) {
                  // gui_.addImageToAcquisition(acq, 0, 0, slice, 0, cache.getImage(0, 0, slice, 0));
                  ImageStack stack = new ImageStack(mAcq.getWidth(), mAcq.getHeight(), acqs.size());
                  for (int xyPos = 0; xyPos < acqs.size(); xyPos++) {
                     ImageProcessor proc = ImageUtils.makeProcessor(
                             gui_.getAcquisitionImageCache(acqs.get(xyPos)).getImage(0, slice, 0, 0));
                     stack.setProcessor(proc, xyPos + 1);
                  }
                  //make median image from set of ImageProcessors
                  ImagePlus iPlus = new ImagePlus("t", stack);
                  ZProjector zProj = new ZProjector(iPlus);
                  zProj.setMethod(ZProjector.MEDIAN_METHOD);
                  zProj.doProjection();
                  ImagePlus median = zProj.getProjection();
                  // the projection gives a 32 bit result.  Convert to 16 bit so that we can stick it back into our acquisition
                  ImageConverter ic = new ImageConverter(median);
                  ic.convertToGray16();
                  TaggedImage tImg = ImageUtils.makeTaggedImage(median.getProcessor());
                  MDUtils.setPixelType(tImg.tags, median.getType());
                  gui_.addImageToAcquisition(acq, 0, 0, slice, 0, tImg);
               }
            } catch (MMScriptException ex) {
               ex.printStackTrace();
               ij.IJ.error("Something went wrong while calculating median image");
            } catch (Exception ex) {
               ex.printStackTrace();
            }
            finally {
               gui_.closeAllAcquisitions();
            }
         }
      }
      
      AcqThread acqT = new AcqThread("SAIM Acquisition");
      acqT.start();

   }
}
