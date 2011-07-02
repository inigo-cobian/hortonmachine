package org.jgrasstools.hortonmachine.models.hm;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrasstools.gears.io.shapefile.ShapefileFeatureReader;
import org.jgrasstools.gears.io.timedependent.TimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.TimeSeriesIteratorWriter;
import org.jgrasstools.hortonmachine.modules.statistics.kriging.Kriging;
import org.jgrasstools.hortonmachine.utils.HMTestCase;
import org.jgrasstools.hortonmachine.utils.HMTestMaps;

/**
 * Test the kriging model.
 * 
 * @author daniele andreis
 * 
 */
public class TestKriging extends HMTestCase {

    private File stazioniFile;
    private File puntiFile;
    private File krigingRainFile;
    private String interpolatedRainPath;
    private File krigingRain2File;
    private File krigingRain3File;

    @Override
    protected void setUp() throws Exception {

        URL stazioniUrl = this.getClass().getClassLoader().getResource("rainstations.shp");
        stazioniFile = new File(stazioniUrl.toURI());
        URL puntiUrl = this.getClass().getClassLoader().getResource("basins_passirio_width0.shp");

        puntiFile = new File(puntiUrl.toURI());
        URL krigingRainUrl = this.getClass().getClassLoader().getResource("rain_test.csv");
        krigingRainFile = new File(krigingRainUrl.toURI());

        URL krigingRain2Url = this.getClass().getClassLoader().getResource("rain_test2A.csv");
        krigingRain2File = new File(krigingRain2Url.toURI());

        URL krigingRain3Url = this.getClass().getClassLoader().getResource("rain_test3A.csv");
        krigingRain3File = new File(krigingRain3Url.toURI());

        File interpolatedRainFile = new File(krigingRainFile.getParentFile(), "kriging_interpolated.csv");
        interpolatedRainPath = interpolatedRainFile.getAbsolutePath();
        // interpolatedRainPath = interpolatedRainPath.replaceFirst("target",
        // "src" + File.separator + File.separator + "test");
        String replacement = "src" + File.separator + File.separator + "test";
        interpolatedRainPath = interpolatedRainPath.replaceFirst("target", replacement);

        interpolatedRainPath = interpolatedRainPath.replaceFirst("test-classes", "resources");

        super.setUp();
    }
    // /////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////TEST 1
    // PASSA////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////
    public void testKriging() throws Exception {

        ShapefileFeatureReader stationsReader = new ShapefileFeatureReader();
        stationsReader.file = stazioniFile.getAbsolutePath();
        stationsReader.readFeatureCollection();
        SimpleFeatureCollection stationsFC = stationsReader.geodata;

        ShapefileFeatureReader interpolatedPointsReader = new ShapefileFeatureReader();
        interpolatedPointsReader.file = puntiFile.getAbsolutePath();
        interpolatedPointsReader.readFeatureCollection();
        SimpleFeatureCollection interpolatedPointsFC = interpolatedPointsReader.geodata;

        TimeSeriesIteratorReader reader = new TimeSeriesIteratorReader();
        reader.file = krigingRainFile.getAbsolutePath();
        reader.idfield = "ID";
        reader.tStart = "2000-01-01 00:00";
        reader.tTimestep = 60;
        // reader.tEnd = "2000-01-01 00:00";
        reader.fileNovalue = "-9999";

        reader.initProcess();

        Kriging kriging = new Kriging();
        kriging.pm = pm;

        kriging.inStations = stationsFC;
        kriging.fStationsid = "ID_PUNTI_M";

        kriging.inInterpolate = interpolatedPointsFC;
        kriging.fInterpolateid = "netnum";

        // it doesn't execute the model with log value.
        kriging.doLogarithmic = false;
        /*
         * Set up the model in order to use the variogram with an explicit
         * integral scale and variance.
         */
        // kriging.pVariance = 3.5;
        // kriging.pIntegralscale = new double[]{10000, 10000, 100};
        kriging.defaultVariogramMode = 1;
        kriging.pA = 123537.0;
        kriging.pNug = 0.0;
        kriging.pS = 1.678383;
        /*
         * Set up the model in order to run with a FeatureCollection as point to
         * interpolated. In this case only 2D.
         */
        kriging.pMode = 0;
        kriging.pSemivariogramType = 1;

        TimeSeriesIteratorWriter writer = new TimeSeriesIteratorWriter();
        writer.file = interpolatedRainPath;

        writer.tStart = reader.tStart;
        writer.tTimestep = reader.tTimestep;
        int j = 0;
        while( reader.doProcess ) {
            reader.nextRecord();
            HashMap<Integer, double[]> id2ValueMap = reader.outData;
            kriging.inData = id2ValueMap;
            kriging.executeKriging();
            /*
             * Extract the result.
             */
            HashMap<Integer, double[]> result = kriging.outData;
            Set<Integer> pointsToInterpolateResult = result.keySet();
            Iterator<Integer> iteratorTest = pointsToInterpolateResult.iterator();

            int iii = 0;

            while( iteratorTest.hasNext() && iii < 12 ) {
                double expected;
                if (j == 0) {
                    expected = 0.3390869;
                } else if (j == 1) {
                    expected = 0.2556174;
                } else if (j == 2) {
                    expected = 0.2428944;
                } else if (j == 3) {
                    expected = 0.2613782;
                } else if (j == 4) {
                    expected = 0.3112850;
                } else if (j == 5) {
                    expected = 0.2983679;
                } else if (j == 6) {
                    expected = 0.3470377;
                } else if (j == 7) {
                    expected = 0.3874065;
                } else if (j == 8) {
                    expected = 0.2820323;
                } else if (j == 9) {
                    expected = 0.1945515;
                } else if (j == 10) {
                    expected = 0.1698022;
                } else if (j == 11) {
                    expected = 0.2405134;
                } else if (j == 12) {
                    expected = 0.2829313;
                } else {
                    expected = 1.0;
                }

                int id = iteratorTest.next();
                double[] actual = result.get(id);
                iii += 1;

                assertEquals(expected, actual[0], 0.001);
                j = j + 1;
            }
            iii = 0;
            j = 0;
            writer.inData = result;
            writer.writeNextLine();

        }

        reader.close();
        writer.close();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////FINE TEST
    // 1PASSA////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////TEST 2
    // PASSA////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Run the kriging models.
     * 
     * <p>
     * This is the case which all the station have the same value.
     * </p>
     * @throws Exception 
     * @throws Exception
     */
    public void testKriging2() throws Exception {
        ShapefileFeatureReader stationsReader = new ShapefileFeatureReader();
        stationsReader.file = stazioniFile.getAbsolutePath();
        stationsReader.readFeatureCollection();
        SimpleFeatureCollection stationsFC = stationsReader.geodata;

        ShapefileFeatureReader interpolatedPointsReader = new ShapefileFeatureReader();
        interpolatedPointsReader.file = puntiFile.getAbsolutePath();
        interpolatedPointsReader.readFeatureCollection();
        SimpleFeatureCollection interpolatedPointsFC = interpolatedPointsReader.geodata;

        TimeSeriesIteratorReader reader = new TimeSeriesIteratorReader();
        reader.file = krigingRain2File.getAbsolutePath();
        reader.idfield = "ID";
        reader.tStart = "2000-01-01 00:00";
        reader.tTimestep = 60;
        // reader.tEnd = "2000-01-01 00:00";
        reader.fileNovalue = "-9999";

        reader.initProcess();

        Kriging kriging = new Kriging();
        kriging.pm = pm;

        kriging.inStations = stationsFC;
        kriging.fStationsid = "ID_PUNTI_M";

        kriging.inInterpolate = interpolatedPointsFC;
        kriging.fInterpolateid = "netnum";

        // it doesn't execute the model with log value.
        kriging.doLogarithmic = false;
        /*
         * Set up the model in order to use the variogram with an explicit integral scale and variance.
         */
        kriging.pVariance = 0.5;
        kriging.pIntegralscale = new double[]{10000, 10000, 100};
        /*
         * Set up the model in order to run with a FeatureCollection as point to interpolated. In this case only 2D.
         */
        kriging.pMode = 0;

        TimeSeriesIteratorWriter writer = new TimeSeriesIteratorWriter();
        writer.file = interpolatedRainPath;

        writer.tStart = reader.tStart;
        writer.tTimestep = reader.tTimestep;

        while( reader.doProcess ) {
            reader.nextRecord();
            HashMap<Integer, double[]> id2ValueMap = reader.outData;
            kriging.inData = id2ValueMap;
            kriging.executeKriging();
            /*
             * Extract the result.
             */
            HashMap<Integer, double[]> result = kriging.outData;
            Set<Integer> pointsToInterpolateResult = result.keySet();
            Iterator<Integer> iterator = pointsToInterpolateResult.iterator();
            while( iterator.hasNext() ) {
                int id = iterator.next();
                double[] actual = result.get(id);
                assertEquals(1.0, actual[0], 0);
            }
            writer.inData = result;
            writer.writeNextLine();
        }

        reader.close();
        writer.close();
    }
    // /////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////FINE TEST 2
    // PASSA////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////// TEST 3
    // PASSA////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////
    // /**
    // * Run the kriging models.
    // *
    // * <p>
    // * This is the case that defaultMode=0.
    // * </p>
    // * @throws Exception
    // * @throws Exception
    // */
    public void testKriging4() throws Exception {
        ShapefileFeatureReader stationsReader = new ShapefileFeatureReader();
        stationsReader.file = stazioniFile.getAbsolutePath();
        stationsReader.readFeatureCollection();
        SimpleFeatureCollection stationsFC = stationsReader.geodata;

        ShapefileFeatureReader interpolatedPointsReader = new ShapefileFeatureReader();
        interpolatedPointsReader.file = puntiFile.getAbsolutePath();
        interpolatedPointsReader.readFeatureCollection();
        SimpleFeatureCollection interpolatedPointsFC = interpolatedPointsReader.geodata;

        TimeSeriesIteratorReader reader = new TimeSeriesIteratorReader();
        reader.file = krigingRainFile.getAbsolutePath();
        reader.idfield = "ID";
        reader.tStart = "2000-01-01 00:00";
        reader.tTimestep = 60;
        // reader.tEnd = "2000-01-01 00:00";
        reader.fileNovalue = "-9999";

        reader.initProcess();

        Kriging kriging = new Kriging();
        kriging.pm = pm;

        kriging.inStations = stationsFC;
        kriging.fStationsid = "ID_PUNTI_M";

        kriging.inInterpolate = interpolatedPointsFC;
        kriging.fInterpolateid = "netnum";

        // it doesn't execute the model with log value.
        kriging.doLogarithmic = false;
        /*
        * Set up the model in order to use the variogram with an explicit integral scale and
        variance.
        */
        kriging.pVariance = 3.5;
        kriging.pIntegralscale = new double[]{10000, 10000, 100};
        /*
        * Set up the model in order to run with a FeatureCollection as point to interpolated. In this
        case only 2D.
        */
        kriging.pMode = 0;

        kriging.doIncludezero = false;
        TimeSeriesIteratorWriter writer = new TimeSeriesIteratorWriter();
        writer.file = interpolatedRainPath;

        writer.tStart = reader.tStart;
        writer.tTimestep = reader.tTimestep;

        while( reader.doProcess ) {
            reader.nextRecord();
            HashMap<Integer, double[]> id2ValueMap = reader.outData;
            kriging.inData = id2ValueMap;
            kriging.executeKriging();
            /*
            * Extract the result.
            */
            HashMap<Integer, double[]> result = kriging.outData;
            double[][] test = HMTestMaps.outKriging4;
            for( int i = 0; i < test.length; i++ ) {
                double actual = result.get((int) test[i][0])[0];
                double expected = test[i][1];
                assertEquals(expected, actual, 0.01);
            }

            writer.inData = result;
            writer.writeNextLine();
        }

        reader.close();
        writer.close();
    }
    // /////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////FINE TEST 3
    // PASSA////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////// TEST 4
    // PASSA////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////
    /**
    * Run the kriging models.
    *
    * <p>
    * This is the case which there is only one station.
    * </p>
    * @throws Exception
    * @throws Exception
    */
    public void testKriging5() throws Exception {
        ShapefileFeatureReader stationsReader = new ShapefileFeatureReader();
        stationsReader.file = stazioniFile.getAbsolutePath();
        stationsReader.readFeatureCollection();
        SimpleFeatureCollection stationsFC = stationsReader.geodata;

        ShapefileFeatureReader interpolatedPointsReader = new ShapefileFeatureReader();
        interpolatedPointsReader.file = puntiFile.getAbsolutePath();
        interpolatedPointsReader.readFeatureCollection();
        SimpleFeatureCollection interpolatedPointsFC = interpolatedPointsReader.geodata;

        TimeSeriesIteratorReader reader = new TimeSeriesIteratorReader();
        reader.file = krigingRain3File.getAbsolutePath();
        reader.idfield = "ID";
        reader.tStart = "2000-01-01 00:00";
        reader.tTimestep = 60;
        // reader.tEnd = "2000-01-01 00:00";
        reader.fileNovalue = "-9999";

        reader.initProcess();

        Kriging kriging = new Kriging();
        kriging.pm = pm;

        kriging.inStations = stationsFC;
        kriging.fStationsid = "ID_PUNTI_M";

        kriging.inInterpolate = interpolatedPointsFC;
        kriging.fInterpolateid = "netnum";

        // it doesn't execute the model with log value.
        kriging.doLogarithmic = false;
        /*
        * Set up the model in order to use the variogram with an explicit integral scale and
        variance.
        */
        kriging.pVariance = 0.5;
        kriging.pIntegralscale = new double[]{10000, 10000, 100};
        /*
        * Set up the model in order to run with a FeatureCollection as point to interpolated. In this
        case only 2D.
        */
        kriging.pMode = 0;

        TimeSeriesIteratorWriter writer = new TimeSeriesIteratorWriter();
        writer.file = interpolatedRainPath;

        writer.tStart = reader.tStart;
        writer.tTimestep = reader.tTimestep;
        int j = 0;
        while( reader.doProcess ) {
            reader.nextRecord();
            HashMap<Integer, double[]> id2ValueMap = reader.outData;
            kriging.inData = id2ValueMap;
            kriging.executeKriging();
            /*
            * Extract the result.
            */
            HashMap<Integer, double[]> result = kriging.outData;
            Set<Integer> pointsToInterpolateResult = result.keySet();
            Iterator<Integer> iteratorTest = pointsToInterpolateResult.iterator();
            double expected;
            if (j == 0) {
                expected = 10.0;
            } else if (j == 1) {
                expected = 15;
            } else if (j == 2) {
                expected = 1;
            } else if (j == 3) {
                expected = 2;
            } else if (j == 4) {
                expected = 2;
            } else if (j == 5) {
                expected = 0;
            } else if (j == 6) {
                expected = 0;
            } else if (j == 7) {
                expected = 23;
            } else if (j == 8) {
                expected = 50;
            } else if (j == 9) {
                expected = 70;
            } else if (j == 10) {
                expected = 30;
            } else if (j == 11) {
                expected = 10;
            } else if (j == 12) {
                expected = 2;
            } else {
                expected = 1.0;
            }

            while( iteratorTest.hasNext() ) {
                int id = iteratorTest.next();
                double[] actual = result.get(id);

                assertEquals(expected, actual[0], 0);
            }
            writer.inData = result;
            writer.writeNextLine();
            j++;
        }

        reader.close();
        writer.close();
    }
    // /////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////FINE TEST 4
    // PASSA////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void tearDown() throws Exception {
        File remove = new File(interpolatedRainPath);
        if (remove.exists()) {
            if (!remove.delete()) {
                remove.deleteOnExit();
            }
        }

        super.tearDown();
    }

}
