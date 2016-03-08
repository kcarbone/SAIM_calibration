package org.micromanager.saim;

import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.CMMCore;
import java.util.prefs.Preferences;
import org.micromanager.api.ScriptInterface;

/**
 *
 * @author Kate
 */
public class PrefUtils {

    ScriptInterface gui_;
    CMMCore core_;
    Preferences prefs_;

    public final static String SERIALPORT = "serialport";
    public final static String TIRFDEVICE = "tirfdevice";
    public final static String TIRFPROP = "tirfprop";
    public final static String ZEROMOTORPOS = "motorposition";
    public final static String WAVELENGTH = "wavelength";
    public final static String SAMPLERI = "sampleri";
    public final static String IMMERSIONRI = "immersionri";
    public final static String STARTMOTORPOS = "startmotorposition";
    public final static String ENDMOTORPOS = "endmotorposition";
    public final static String NUMCALSTEPS = "numberofcalibrationsteps";
    public final static String ANGLESTEPSIZE = "anglestepsize";
    public final static String STARTANGLE = "startangle";
    public final static String DOUBLEZERO = "doublezero";
    public final static String FFSHOWIMAGES = "ffsaveimages";
    public final static String FFBACKGROUNDFILE = "ffBackgroundFile";
    public final static String FFDIRROOT = "ffdirroot";
    public final static String FFNAMEPREFIX = "ffnameprefix";
    public final static String ACQSAVEIMAGES = "acqsaveimages";
    public final static String ACQDIRROOT = "acqdirroot";
    public final static String ACQNAMEPREFIX = "acqnameprefix";
    public final static String CHANNEL = "channel";

    /**
     * Utility to convert channel group into PrefString
     *
     * @param prefs - Java prefs used to determine channel
     * @param gui
     * @return string for calibration key
     */
    public static String channelCoeffKey(final Preferences prefs, final ScriptInterface gui) {
        // Generate a key to which to store the calibration values
        CMMCore core = gui.getMMCore();
        String channelString;
        String grp;
        try {
            grp = core.getChannelGroup();
            channelString = "CALIBRATIONS-" + grp + "-" + core.getCurrentConfigFromCache(grp);
            prefs.put(PrefUtils.CHANNEL, core.getCurrentConfigFromCache(grp));
        } catch (Exception ex) {
            Logger.getLogger(PrefUtils.class.getName()).log(Level.SEVERE, null, ex);
            channelString = "CALIBRATIONS-";
        }
        return channelString;
    }
    
    public static String parseCal(int index, final Preferences prefs, final ScriptInterface gui){
        CMMCore core = gui.getMMCore();
        String channelString;
        String grp;
        String coeff;
        try {
            grp = core.getChannelGroup();
            channelString = "CALIBRATIONS-" + grp + "-"+ core.getCurrentConfigFromCache(grp);
            String calString = prefs.get(channelString, "");
            String parts[] = calString.split(" ");
            coeff = parts[index].replaceAll("\\[|\\,|\\]", "");
        } catch (Exception ex) {
            Logger.getLogger(PrefUtils.class.getName()).log(Level.SEVERE, null, ex);
            coeff = "";
        }
        return coeff;
    }
}
