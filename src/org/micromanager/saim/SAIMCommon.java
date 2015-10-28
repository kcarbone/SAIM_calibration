
package org.micromanager.saim;

import java.util.prefs.Preferences;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.micromanager.api.ScriptInterface;

/**
 * Functions that are used in multiple panels
 */
public class SAIMCommon {

   /**
    * Utility to convert angle to tirf positions using our polynomial equation
    * @param prefs - Java prefs used to store our coefficients
    * @param angle Desired angle
    * @return Tirf motor position
    */
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

   /**
    * This code runs the actual acquisition while flatfielding and when executing
    * an acquisition.  
    * 
    * @param gui MMScriptInterface
    * @param prefs Java Preferences used to store all our data
    * @param rootDir where to save this acquisition (if desired)
    * @param show whether or not to show this acquisition
    * @param save whether or not to sava this acquisition
    * @return
    * @throws Exception 
    */
   public static String runAcquisition(final ScriptInterface gui,
           final Preferences prefs, String rootDir, boolean show, boolean save) 
           throws Exception {
      
      CMMCore core = gui.getMMCore();
      double startAngle = Double.parseDouble(prefs.get(PrefStrings.STARTANGLE, "0.0"));
      if (startAngle > 0) {
         throw new Exception ("Start angle should be <= 0");
      }
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

      int nrFrames = nrAngles + 1;
      if (doubleZero) {
         nrFrames = nrAngles + 2;
      }

      gui.openAcquisition(acq, rootDir, 1, 1, nrFrames, 1, show, save); 

      boolean doubled = false;
      int frameNr = 0;
      for (double angle = startAngle; angle <= -startAngle; angle += angleStepSize) {
         double pos = SAIMCommon.tirfPosFromAngle(prefs, angle);
         gui.message("Angle: " + Double.toString(angle) + ", position: " + Double.toString(pos));
         core.setProperty(deviceName, propName, pos);
         core.waitForDevice(deviceName);
         //gui.sleep(250);
         core.snapImage();
         TaggedImage taggedImg = core.getTaggedImage();
         taggedImg.tags.put("Angle", angle);
         gui.addImageToAcquisition(acq, 0, 0, frameNr, 0, taggedImg);
         frameNr++;
         if (doubleZero && !doubled && (angle == 0) ) {
            angle = -angleStepSize;
            doubled = true;
         }
      }
      
      return acq;
   }
}
