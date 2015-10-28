
package org.micromanager.saim;

import java.util.prefs.Preferences;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.micromanager.api.ScriptInterface;

/**
 * Functions that are used in multiple panels
 */
public class SAIMCommon {

   public static int tirfPosFromAngle(Preferences prefs, double angle) {
      // TirfPosition = slope * angle plus Offset
      // Output motor position must be an integer to be interpreted by TITIRF

      double tempPos = (Double.parseDouble(prefs.get(PrefStrings.COEFF3, "")) * Math.pow(angle, 3)
              + Double.parseDouble(prefs.get(PrefStrings.COEFF2, "")) * Math.pow(angle, 2)
              + Double.parseDouble(prefs.get(PrefStrings.COEFF1, "")) * angle
              + Double.parseDouble(prefs.get(PrefStrings.COEFF0, "")));
      int pos = Math.round((float) tempPos);
      return pos;
   }

   public static String RunAcquisition(final ScriptInterface gui,
           final Preferences prefs, String rootDir, boolean show, boolean save) 
           throws Exception {
      
      CMMCore core = gui.getMMCore();
      double startAngle = Double.parseDouble(prefs.get(PrefStrings.STARTANGLE, "0.0"));
      double angleStepSize = prefs.getDouble(PrefStrings.ANGLESTEPSIZE, 0);
      boolean doubleZero = Boolean.parseBoolean(prefs.get(PrefStrings.DOUBLEZERO, ""));
      if (startAngle % angleStepSize != 0) {
         throw new Exception("Start angle is not divisible by the angle step size");
      }
      // Set these variables to the correct values and leave
      final String deviceName = prefs.get(PrefStrings.TIRFDEVICE, "");
      final String propName = prefs.get(PrefStrings.TIRFPROP, "");

      // Usually no need to edit below this line
      double tempnrAngles = Math.abs(startAngle) * 2 / angleStepSize;
      int nrAngles = (Integer) Math.round((float) tempnrAngles);

      //gui_.closeAllAcquisitions();
      String acq = gui.getUniqueAcquisitionName("FlatField");

      int frames = nrAngles + 1;
      if (doubleZero) {
         frames = nrAngles + 2;
      }

      gui.openAcquisition(acq,
              rootDir, 1, 1, frames, 1,
              show, // Show
              save); // Save <--change this to save files in root directory

      // First take images from start to 90 degrees
      double pos = startAngle;
      int nrAngles1 = nrAngles / 2;
      for (int a = 0;
              a <= nrAngles1;
              a++) {
         double val = SAIMCommon.tirfPosFromAngle(prefs, pos);
         gui.message("Image: " + Integer.toString(a) + ", angle: " + Double.toString(pos) + ", val: " + Double.toString(val));
         core.setProperty(deviceName, propName, val);
         core.waitForDevice(deviceName);
         //gui.sleep(250);
         core.snapImage();
         TaggedImage taggedImg = core.getTaggedImage();
         taggedImg.tags.put("Angle", pos);
         gui.addImageToAcquisition(acq, 0, 0, a, 0, taggedImg);
         pos += angleStepSize;
      }

      // if doubleZeroCheckBox is selected, take images from 0 degrees
      // to (0 - startposition) degrees
      double pos1 = angleStepSize;
      int nrAngles2 = nrAngles / 2;
      if (doubleZero) {
         pos1 = 0;
         nrAngles2 = nrAngles / 2 + 1;
      }

      for (int b = 0;
              b <= nrAngles2;
              b++) {
         double val = SAIMCommon.tirfPosFromAngle(prefs, pos1);
         gui.message("Image: " + Integer.toString(b) + ", angle: " + Double.toString(pos1) + ", val: " + Double.toString(val));
         core.setProperty(deviceName, propName, val);
         core.waitForDevice(deviceName);
         //gui.sleep(250);
         core.snapImage();
         TaggedImage taggedImg = core.getTaggedImage();
         taggedImg.tags.put("Angle", pos1);
         gui.addImageToAcquisition(acq, 0, 0, b + nrAngles1, 0, taggedImg);
         pos1 += angleStepSize;
      }

      return acq;
   }
}
