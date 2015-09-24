/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.saim;

/**
 *
 * @author Kate
 */
public interface ICalibrationObserver {
    void calibrationChanged(double x3, double x2, double x1, double x0);
}
