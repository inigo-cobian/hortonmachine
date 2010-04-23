package org.jgrasstools.hortonmachine.models.hm;

import java.util.HashMap;
import java.util.List;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.io.eicalculator.EIAltimetry;
import org.jgrasstools.gears.io.eicalculator.EIAreas;
import org.jgrasstools.gears.io.eicalculator.EIEnergy;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.jgrasstools.hortonmachine.modules.hydrogeomorphology.energyindexcalculator.EnergyIndexCalculator;
import org.jgrasstools.hortonmachine.utils.HMTestCase;
import org.jgrasstools.hortonmachine.utils.HMTestMaps;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Test {@link EnergyIndexCalculator}.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestEnergyIndexCalculator extends HMTestCase {

    /**
     * TODO make this test a bit more serious.
     * 
     * @throws Exception
     */
    public void testEnergyIndexCalculator() throws Exception {
        HashMap<String, Double> envelopeParams = HMTestMaps.envelopeParams;
        CoordinateReferenceSystem crs = HMTestMaps.crs;

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);
        
        double[][] aspectData = HMTestMaps.aspectDataRadiants;
        GridCoverage2D aspectCoverage = CoverageUtilities.buildCoverage("aspect", aspectData, envelopeParams, crs, true);
        double[][] nablaData = HMTestMaps.nablaData0;
        GridCoverage2D nablaCoverage = CoverageUtilities.buildCoverage("nabla", nablaData, envelopeParams, crs, true);
        double[][] pitData = HMTestMaps.pitData;
        GridCoverage2D pitCoverage = CoverageUtilities.buildCoverage("pit", pitData, envelopeParams, crs, true);
        double[][] slopeData = HMTestMaps.slopeData;
        GridCoverage2D slopeCoverage = CoverageUtilities.buildCoverage("slope", slopeData, envelopeParams, crs, true);
        double[][] subbasinsData = HMTestMaps.basinDataNN0;
        GridCoverage2D subbasinsCoverage = CoverageUtilities.buildCoverage("subbasins", subbasinsData, envelopeParams, crs, true);

        EnergyIndexCalculator eiCalculator = new EnergyIndexCalculator();
        eiCalculator.inAspect = aspectCoverage;
        eiCalculator.inCurvatures = nablaCoverage;
        eiCalculator.inDem = pitCoverage;
        eiCalculator.inSlope = slopeCoverage;
        eiCalculator.inBasins = subbasinsCoverage;
        eiCalculator.pDt = 1;
        eiCalculator.pEi = 2;
        eiCalculator.pEs = 2;
        eiCalculator.pm = pm;

        eiCalculator.executeEnergyIndexCalculator();

        List<EIAltimetry> altimetricValues = eiCalculator.outAltimetry;
        List<EIEnergy> energeticValues = eiCalculator.outEnergy;
        List<EIAreas> areaValues = eiCalculator.outArea;

        EIAltimetry eiAltimetry = altimetricValues.get(0);
        assertEquals(1, eiAltimetry.basinId);
        assertEquals(0, eiAltimetry.altimetricBandId);
        assertEquals(737.5, eiAltimetry.elevationValue);
        assertEquals(75.0, eiAltimetry.bandRange);

        EIEnergy eiEnergy = energeticValues.get(0);
        assertEquals(1, eiEnergy.basinId);
        assertEquals(0, eiEnergy.energeticBandId);
        assertEquals(0, eiEnergy.virtualMonth);
        assertEquals(0.09808943859674346, eiEnergy.energyValue, 0.0001);

        EIAreas eiAreas = areaValues.get(0);
        assertEquals(1, eiAreas.basinId);
        assertEquals(0, eiAreas.altimetricBandId);
        assertEquals(0, eiAreas.energyBandId);
        assertEquals(0.0, eiAreas.areaValue, 0.0001);

    }

}
