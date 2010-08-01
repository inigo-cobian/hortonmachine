package org.jgrasstools.gears;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrasstools.gears.utils.HMTestCase;
import org.jgrasstools.gears.utils.math.interpolation.Interpolator;
import org.jgrasstools.gears.utils.math.interpolation.LinearListInterpolator;
import org.jgrasstools.gears.utils.math.interpolation.PolynomialInterpolator;
/**
 * Test interpolation.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestInterpolators extends HMTestCase {

    private static final double DELTA = 0.00000001;

    public void testListInterpolation() throws Exception {

        List<Double> xList = new ArrayList<Double>();
        List<Double> yList = new ArrayList<Double>();

        xList.add(1.0);
        xList.add(2.0);
        xList.add(3.0);

        yList.add(1.0);
        yList.add(2.0);
        yList.add(3.0);

        LinearListInterpolator dischargeScaleInterpolator = new LinearListInterpolator(xList, yList);

        Double interpolated = dischargeScaleInterpolator.linearInterpolateX(2.0);
        assertEquals(2.0, interpolated, DELTA);
        interpolated = dischargeScaleInterpolator.linearInterpolateX(1.5);
        assertEquals(1.5, interpolated, DELTA);
        interpolated = dischargeScaleInterpolator.linearInterpolateX(0.0);
        assertTrue(Double.isNaN(interpolated));
        interpolated = dischargeScaleInterpolator.linearInterpolateX(4.0);
        assertTrue(Double.isNaN(interpolated));

        Collections.reverse(yList);

        interpolated = dischargeScaleInterpolator.linearInterpolateX(2.0);
        assertEquals(2.0, interpolated, DELTA);
        interpolated = dischargeScaleInterpolator.linearInterpolateX(1.5);
        assertEquals(2.5, interpolated, DELTA);
        interpolated = dischargeScaleInterpolator.linearInterpolateX(0.0);
        assertTrue(Double.isNaN(interpolated));
        interpolated = dischargeScaleInterpolator.linearInterpolateX(4.0);
        assertTrue(Double.isNaN(interpolated));
    }

    public void testPolynomialInterpolator() {
        // approximate e^1.4

        // samples
        List<Double> xList = new ArrayList<Double>();
        List<Double> yList = new ArrayList<Double>();
        xList.add(1.12);
        yList.add(3.0648541);
        xList.add(1.55);
        yList.add(4.71147);
        xList.add(1.25);
        yList.add(3.4903429);
        xList.add(1.92);
        yList.add(6.820958);
        xList.add(1.33);
        yList.add(3.7810435);
        xList.add(1.75);
        yList.add(5.754603);

        Interpolator interp = new PolynomialInterpolator(xList, yList);

        assertEquals(Math.exp(1.4), interp.getInterpolated(1.4), 0.0001);
    }

}