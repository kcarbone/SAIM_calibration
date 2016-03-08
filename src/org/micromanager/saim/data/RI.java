///////////////////////////////////////////////////////////////////////////////
//FILE:          RI.java
//PROJECT:       SAIM
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman
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


package org.micromanager.saim.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Use this class to get the refractive index of compounds of interest.
 * Refractive indices (RIs) depend on wavelength.  
 * This code uses tables from https://www.filmetrics.com.  These
 * tables are included in the jar file, and will be read when needed.
 * The RI will be deduced by linear interpolation and will be cached
 * for future use.
 * 
 * Use the class as follows:
 * double ri = RI.getRI(Compound.SILICON, 525.0);
 * 
 * @author nico
 */
public class RI {
 
   private static final String PATHINJAR = "/org/micromanager/saim/data/";
   
   public static enum Compound  { 
      SILICON ("siliconRI.txt"), 
      SILICONOXIDE ("siliconOxideRI.txt"),
      ACRYLIC ("acrylicRI.txt"),
      WATER ("waterRI.txt");
      
      // Hashmaps with cached values
      public static Map<Double, Double> siliconMap_ = 
              new HashMap<Double, Double>();
      public static Map<Double, Double> siliconOxideMap_ = 
              new HashMap<Double, Double>();
      public static Map<Double, Double> acrylicMap_ = 
              new HashMap<Double, Double>();
      public static Map<Double, Double> waterMap_ = 
              new HashMap<Double, Double>();
      
      private final String fileName_;
      Compound(String fileName) {
         fileName_ = fileName;
      }
      public String getFile() {
         return fileName_;
      }
      public static Map<Double, Double> getMap(Compound compound) {
         if (compound.equals(Compound.SILICON)) {
            return siliconMap_;
         }
         if (compound.equals(Compound.SILICONOXIDE)) {
            return siliconOxideMap_;
         }
         if (compound.equals(Compound.ACRYLIC)) {
            return acrylicMap_;
         }
         if (compound.equals(Compound.WATER)) {
            return waterMap_;
         }
         return null;
      }
   }
   
   /**
    * First checks the local cache for the value
    * If not found, get the value from the data file using interpolation
    * if needed.
    * @param compound 
    * @param waveLength
    * @return Refractive index of the given compound at the given wavelength
    */
   public static double getRI(Compound compound, double waveLength) {
      Map<Double, Double> compoundMap = Compound.getMap(compound);
      if (compoundMap.containsKey(waveLength))
         return compoundMap.get(waveLength);
      
      double ri = getRIFromFile(compound, waveLength);
      compoundMap.put(waveLength, ri);
      return ri;
   } 

   /**
    * Parse the file with refractive index information
    * file has the format:
    * 
    * Wavelength(nm)	n	k
    *210	1.5384	0
    *215	1.5332	0
    *208	1.046	2.944
    *208.7	1.066	2.937
    *209.4	1.07	2.963
    * 
    * @param compound
    * @param waveLength
    * @return 
    */
   private static double getRIFromFile(Compound compound, double waveLength) {
      InputStream input = RI.class.getResourceAsStream(
              PATHINJAR + compound.getFile());
      
      Scanner s = new Scanner(input);
      int counter = 0;
      ArrayList<Double> waveLengths = new ArrayList<Double>();
      ArrayList<Double> ris = new ArrayList<Double>();
      while (s.hasNext()) {
         if (s.hasNextDouble()) {
            waveLengths.add(s.nextDouble());
            if (s.hasNextDouble()) {
               ris.add(s.nextDouble());
            }
            // throw away the third column
            if (s.hasNextDouble()) {
               s.nextDouble();
            }
            if (waveLength <= waveLengths.get(counter) && counter > 0) {
               s.close();
               return interpolate(
                       waveLengths.get(counter - 1), waveLengths.get(counter),
                       ris.get(counter -1), ris.get(counter), 
                       waveLength);
            }
            counter++;
         } else {
            // read away the next token:
            s.next();
         }
      }
      
      // not found....
      s.close();
      return 0.0;
   }
   
   /**
    * Finds the y value associated with an x value given two known x,y points
    * 
    * @param x1 lowest x value
    * @param x2 highest x value
    * @param y1 lowest y value
    * @param y2 highest y value
    * @param xVal x value for which we want to know the y value
    * @return y value associated with xVal
    */
   public static double interpolate(
           double x1, double x2, double y1, double y2, double xVal) {
      return y1 + (y2 - y1) * (xVal - x1) / (x2 - x1);    
   }
           
   
   
}