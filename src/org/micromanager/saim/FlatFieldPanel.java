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
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.ImageCalculator;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.prefs.Preferences;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import net.miginfocom.swing.MigLayout;
import org.micromanager.api.ScriptInterface;
import org.micromanager.saim.gui.GuiUtils;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.saim.gui.DragFileToTextField;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.MMScriptException;

/**
 *
 * @author nico
 */
public class FlatFieldPanel extends JPanel {

    ScriptInterface gui_;
    CMMCore core_;
    Preferences prefs_;

    private final JSpinner angleStepSizeSpinner_;
    private final JTextField startAngleField_;
    private final JCheckBox doubleZeroCheckBox_;
    private final JPanel calPanel_;
    private final JCheckBox ffShowImagesCheckBox_;
    private final JToggleButton runButton_;
    private final FileDialog backgroundFileChooser_;
    private final JTextField backgroundFileField_;
    private final JButton backgroundFileButton_;
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
        angleStepSizeSpinner_ = new JSpinner(new SpinnerNumberModel(1.0, 0, 180, 0.1));
        angleStepSizeSpinner_.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                prefs_.putDouble(PrefStrings.ANGLESTEPSIZE, (Double) angleStepSizeSpinner_.getValue());
            }
        });
        setupPanel.add(angleStepSizeSpinner_, "span, growx, wrap");

        // set start angle
        setupPanel.add(new JLabel("Start Angle:"));
        startAngleField_ = new JTextField("");
        setTextAttributes(startAngleField_, componentSize);

        startAngleField_.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                prefs_.put(PrefStrings.STARTANGLE, startAngleField_.getText());
            }
        });
        setupPanel.add(startAngleField_, "span, growx, wrap");
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
        final Dimension calBoxSize = new Dimension(130, 30);

        //Set calibration values
        //x3 coefficient
        calPanel_.add(new JLabel("x^3: "));
        coeff3Field_ = new JTextField("");
        setTextAttributes(coeff3Field_, componentSize);
        coeff3Field_.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                prefs_.put(PrefStrings.COEFF3, coeff3Field_.getText());
            }
        });
        calPanel_.add(coeff3Field_, "span, center, wrap");

        //x2 coefficient
        calPanel_.add(new JLabel("x^2: "));
        coeff2Field_ = new JTextField("");
        setTextAttributes(coeff2Field_, componentSize);
        coeff2Field_.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                prefs_.put(PrefStrings.COEFF2, coeff2Field_.getText());
            }
        });
        calPanel_.add(coeff2Field_, "span, center, wrap");

        //x coefficient
        calPanel_.add(new JLabel("x: "));
        coeff1Field_ = new JTextField("");
        setTextAttributes(coeff1Field_, componentSize);
        coeff1Field_.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                prefs_.put(PrefStrings.COEFF1, coeff1Field_.getText());
            }
        });
        calPanel_.add(coeff1Field_, "span, center, wrap");

        //x0 constant
        calPanel_.add(new JLabel("x^0: "));
        coeff0Field_ = new JTextField("");
        setTextAttributes(coeff0Field_, componentSize);
        coeff0Field_.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                prefs_.put(PrefStrings.COEFF0, coeff0Field_.getText());
            }
        });
        calPanel_.add(coeff0Field_, "span, center, wrap");

        // FlatField Panel
        JPanel flatfieldPanel = new JPanel(new MigLayout(
                "", ""));
        flatfieldPanel.setBorder(GuiUtils.makeTitledBorder("FlatField"));


        // background file file chooser
        backgroundFileChooser_ = new FileDialog( (JFrame) 
                SwingUtilities.getWindowAncestor(this));
        backgroundFileChooser_.setDirectory( new java.io.File(".").getPath());
        backgroundFileChooser_.setTitle("Background Image");
        
        flatfieldPanel.add(new JLabel("Background Image"));
        backgroundFileField_ = new JTextField("");
        setTextAttributes(backgroundFileField_, componentSize);
        backgroundFileField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(PrefStrings.FFDIRROOT, backgroundFileField_.getText());
            }
        });
        DropTarget dt = new DropTarget (backgroundFileField_, 
                new DragFileToTextField(backgroundFileField_, false));
        flatfieldPanel.add(backgroundFileField_);

        // background file chooser button
        backgroundFileButton_ = new JButton("...");
        backgroundFileButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               backgroundFileChooser_.setVisible(true);
               String file = backgroundFileChooser_.getFile();
               String directory = backgroundFileChooser_.getDirectory();
               if (file != null && directory != null)
                  backgroundFileField_.setText(directory + File.separator + file);
                  prefs_.put(PrefStrings.FFDIRROOT, backgroundFileField_.getText());
            }
        });
        flatfieldPanel.add(backgroundFileButton_, "wrap");

        
        // Give instructions
        flatfieldPanel.add(new JLabel("Selecting \"Run FlatField\" will prompt you to take multiple"), "span, wrap");
        flatfieldPanel.add(new JLabel("SAIM acquisitions at different positions for correction"), "span, wrap");
        flatfieldPanel.add(new JLabel("of final images."), "span, wrap");

        // create show images checkbox
        ffShowImagesCheckBox_ = new JCheckBox("Show Images");
        ffShowImagesCheckBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ffShowImagesCheckBox_.isSelected()) {
                    prefs_.putBoolean(PrefStrings.FFSHOWIMAGES, true);
                } else {
                    prefs_.putBoolean(PrefStrings.FFSHOWIMAGES, false);
                }
            }
        });
        flatfieldPanel.add(ffShowImagesCheckBox_, "span 2, growx, wrap");

        // create run button
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
        UpdateFromPrefs();
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
    private String RunAcquisition() {
        double startAngle = Double.parseDouble(startAngleField_.getText());
        double angleStepSize = prefs_.getDouble(PrefStrings.ANGLESTEPSIZE, 0);
        String acq = "";
        if (startAngle % angleStepSize == 0) {
            try {
                // Set these variables to the correct values and leave
                final String deviceName = prefs_.get(PrefStrings.TIRFDEVICE, "");
                final String propName = prefs_.get(PrefStrings.TIRFPROP, "");

                // Usually no need to edit below this line
                double tempnrAngles = Math.abs(startAngle) * 2 / angleStepSize;
                int nrAngles = (Integer) Math.round((float) tempnrAngles);

                //gui_.closeAllAcquisitions();
                acq = gui_.getUniqueAcquisitionName("FlatField");

                int frames = nrAngles + 1;
                if (doubleZeroCheckBox_.isSelected()) {
                    frames = nrAngles + 2;
                }

                if (ffShowImagesCheckBox_.isSelected()) {
                    gui_.openAcquisition(acq,
                            "", 1, 1, frames, 1,
                            true, // Show
                            false); // Save <--change this to save files in root directory
                } else {
                    gui_.openAcquisition(acq,
                            "", 1, 1, frames, 1,
                            false, // Show
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
               GenericDialog okWindow = new NonBlockingGenericDialog("FlatField Image " + Integer.toString(count));
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

            ImageStack flatFieldStack = null;

            try {
               MMAcquisition mAcq = gui_.getAcquisition(acqs.get(0));
               flatFieldStack = new ImageStack(mAcq.getWidth(),
                       mAcq.getHeight(), mAcq.getSlices());
                                    // get the background image from file.
               // this should be a single frame of the same dimensions as the acquisitions
               ImagePlus background = null;
               String backgroundFile = backgroundFileField_.getText();
               if (backgroundFile != null && backgroundFile.length() > 0) {
                  background = ij.IJ.openImage(backgroundFile);
                  if (background.getWidth() != mAcq.getWidth()
                          || background.getHeight() != mAcq.getHeight()
                          || background.getBytesPerPixel() != mAcq.getByteDepth()) {
                     ij.IJ.showMessage("Background file is of different size or type then the just acquired images.  Ignoring background");
                     background = null;
                  } else {
                           // since our median image will be 32-bit, we need to convert
                     // the backgroun image to 32-bit as well
                     background.setProcessor(background.getProcessor().convertToFloatProcessor());
                  }
               }
               for (int slice = 0; slice < mAcq.getSlices(); slice++) {
                  
                  // make a stack to use to calculate the median of all our flatfield acquisitions
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

                  // subtract background here
                  if (background != null && background.getProcessor() != null) {
                     ImageProcessor mf = median.getProcessor();
                     for (int i = 0; i < mf.getPixelCount(); i++) {
                        mf.setf(i, mf.getf(i) - background.getProcessor().getf(i));
                     }
                  }

                  // Normalize the median image so that the average is 1:
                  ImageStatistics stats = median.getStatistics();
                  float mean = (float) stats.mean;
                  ImageProcessor iProc = median.getProcessor();
                  for (int i = 0; i < iProc.getPixelCount(); i++) {
                     iProc.setf(i, iProc.getf(i) / mean);
                  }
                  flatFieldStack.setProcessor(median.getProcessor(), slice + 1);
               }
            } catch (MMScriptException ex) {
               ex.printStackTrace();
               ij.IJ.error("Something went wrong while calculating median image");
            } catch (Exception ex) {
               ex.printStackTrace();
               ij.IJ.error("Something went wrong while calculating median image");
            } finally {
               gui_.closeAllAcquisitions();
               
               // show the flatfield Stack
               if (flatFieldStack != null) {
                  ImagePlus flatField = new ImagePlus("flatField",
                          flatFieldStack);
                  flatField.show();
               }

            }
         }
      }

      AcqThread acqT = new AcqThread("SAIM Acquisition");
      acqT.start();

   }

    //function to add preferences values to each field that uses them
   private void UpdateFromPrefs() {
      angleStepSizeSpinner_.setValue(Double.parseDouble(prefs_.get(PrefStrings.ANGLESTEPSIZE, "")));
      startAngleField_.setText(prefs_.get(PrefStrings.STARTANGLE, ""));
      doubleZeroCheckBox_.setSelected(Boolean.parseBoolean(prefs_.get(PrefStrings.DOUBLEZERO, "")));
      ffShowImagesCheckBox_.setSelected(Boolean.parseBoolean(prefs_.get(PrefStrings.FFSHOWIMAGES, "")));
      coeff3Field_.setText(prefs_.get(PrefStrings.COEFF3, ""));
      coeff2Field_.setText(prefs_.get(PrefStrings.COEFF2, ""));
      coeff1Field_.setText(prefs_.get(PrefStrings.COEFF1, ""));
      coeff0Field_.setText(prefs_.get(PrefStrings.COEFF0, ""));
   }
}
