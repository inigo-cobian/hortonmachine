/*
 * GNU GPL v3 License
 *
 * Copyright 2016 Marialaura Bancheri
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hortonmachine.hmachine.modules.statistics.kriging.nextgen;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.libs.modules.HMModel;
import org.hortonmachine.hmachine.modules.statistics.kriging.utils.MaxDistance;
import org.hortonmachine.hmachine.modules.statistics.kriging.variogram.VariogramFunction;
import org.hortonmachine.hmachine.modules.statistics.kriging.variogram.VariogramFunctionFitter;
import org.hortonmachine.hmachine.modules.statistics.kriging.variogram.theoretical.ITheoreticalVariogram;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Documentation;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.UI;

@Description("Teorethical semivariogram models.")
@Documentation("vgm.html")
@Author(name = "Giuseppe Formetta, Adami Francesco, Marialaura Bancheri", contact = " http://www.ing.unitn.it/dica/hp/?user=rigon")
@Keywords("Kriging, Hydrology")
@Label(HMConstants.STATISTICS)
@Name("kriging")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class OmsTheoreticalVariogram extends HMModel {

    @Description("Experimental Variogram")
    @In
    public HashMap<Integer, double[]> inExperimentalVariogramMap;

    @Description("Theoretical Variogram type.")
    @UI("combo:" + ITheoreticalVariogram.TYPES)
    @In
    public String pTheoreticalVariogramType = ITheoreticalVariogram.EXPONENTIAL;

    @Description("The Sill value fitted for the selected model.")
    @Out
    public double outSill;

    @Description("The Range value fitted for the selected model.")
    @Out
    public double outRange;

    @Description("The Nugget value fitted for the selected model")
    @Out
    public double outNugget;

    @Description("The Theoretical Variogram. The double array is of the form [distance, variance]")
    @Out
    public HashMap<Integer, double[]> outTheoreticalVariogram = new HashMap<Integer, double[]>();

    @Execute
    public void process() throws Exception {
        // set some initial values
        Collection<double[]> allValues = inExperimentalVariogramMap.values();
        double maxVariance = Double.NEGATIVE_INFINITY;
        double minDistance = Double.POSITIVE_INFINITY;
        double maxDistance = Double.NEGATIVE_INFINITY;
//        for( double[] ds : allValues ) {
        int count  = 0;
        int countNuggetValues  = 0;
        int countSillValues  = 0;
        double meanFirstThreeVariogramValues = 0.0;
        double meanLastFiveVariogramValues = 0.0;
        for( double[] ds : allValues ) { 
                 if(count != 0){
                    maxVariance = Math.max(ds[1], maxVariance); 
                    minDistance = Math.min(ds[0], minDistance); 
                    maxDistance = Math.max(ds[0], maxVariance);
                    if (count <= 3) {
                    	meanFirstThreeVariogramValues = meanFirstThreeVariogramValues + ds[1];
						countNuggetValues ++;
                    }
                    if (count >= allValues.size() - 5) {
                    	meanLastFiveVariogramValues = meanLastFiveVariogramValues + ds[1];
						countSillValues ++;
					}
                 } 
                count++;
         }
        
        double defaultRange = maxDistance / 3.0;
        double defaultNugget = meanFirstThreeVariogramValues / countNuggetValues;
    	double defaultSill = meanLastFiveVariogramValues / countSillValues;
    	
//        double initSill = 0.8 * maxVariance;
//        double initRange = 1.2 * minDistance;
//        double initNugget = 0.0;
        double initSill = defaultSill;
        double initRange = defaultRange;
        double initNugget = defaultNugget;

        VariogramFunction variogramFunction = new VariogramFunction(pTheoreticalVariogramType);
        VariogramFunctionFitter fitter = new VariogramFunctionFitter(variogramFunction, initSill, initRange, initNugget);
        
        double[] sillRangeNugget = fitter.fit(allValues);
        /*
         * If the fitting procedure returns a null object -> proceed with the default parameters:
         * the range parameter is taken as 1/3 of the maximum sample variogram distance 
         * the nugget parameter is taken as the mean of the first three sample variogram values 
         * the partial sill is taken as the mean of the last five sample variogram values.
         */

        if (sillRangeNugget == null || sillRangeNugget.length == 0) {
        	System.out.println("Fitting procedure did not converged, using default parameters.");
        	outRange = defaultRange;
        	outNugget = defaultNugget;
        	outSill = defaultSill;
			
		} else {
			outSill = sillRangeNugget[0];
			outRange = sillRangeNugget[1];
			outNugget = sillRangeNugget[2];
		}

        for( Entry<Integer, double[]> entry : inExperimentalVariogramMap.entrySet() ) {
            Integer id = entry.getKey();
            double[] values = entry.getValue();
            double distance = values[0];

            ITheoreticalVariogram modelVGM = ITheoreticalVariogram.create(pTheoreticalVariogramType);
            modelVGM.init(distance, sillRangeNugget[0], sillRangeNugget[1], sillRangeNugget[2]);
            double variance = modelVGM.computeSemivariance();

            outTheoreticalVariogram.put(id, new double[]{distance, variance});
        }

    }

}
