package org.hortonmachine.gears.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geotools.coverage.grid.GridCoverage2D;
import org.hortonmachine.gears.io.stac.HMStacAsset;
import org.hortonmachine.gears.utils.HMTestCase;
import org.hortonmachine.gears.utils.RegionMap;
import org.junit.Ignore;

public class TestStacAsset extends HMTestCase {
    private ObjectMapper mapper = new ObjectMapper();

    protected void setUp() throws Exception {

    }

    // Asset JSONs obtained from the documentation, reformatted and modified
    // https://github.com/stac-extensions/raster/blob/main/examples/item-sentinel2.json#L177

    public void testCreateValidStacAsset() throws JsonProcessingException {
        String assetJSON = "{\"title\":\"Band 1 (coastal) BOA reflectance\",\"type\":\"image/tiff; application=geotiff; profile=cloud-optimized\",\"roles\":[\"data\"],\"gsd\":60,\"eo:bands\":[{\"name\":\"B01\",\"common_name\":\"coastal\",\"center_wavelength\":0.4439,\"full_width_half_max\":0.027}],\"href\":\"https://sentinel-cogs.s3.us-west-2.amazonaws.com/sentinel-s2-l2a-cogs/33/S/VB/2021/2/S2B_33SVB_20210221_0_L2A/B01.tif\",\"proj:shape\":[1830,1830],\"proj:transform\":[60,0,399960,0,-60,4200000,0,0,1],\"raster:bands\":[{\"data_type\":\"uint16\",\"spatial_resolution\":60,\"bits_per_sample\":15,\"nodata\":0,\"statistics\":{\"minimum\":1,\"maximum\":20567,\"mean\":2339.4759595597,\"stddev\":3026.6973619954,\"valid_percent\":99.83}}]}";
        JsonNode node = mapper.readTree(assetJSON);

        HMStacAsset asset = new HMStacAsset("B01", node);

        assertTrue(asset.isValid());
        assertEquals("B01", asset.getId());
        assertEquals("Band 1 (coastal) BOA reflectance", asset.getTitle());
        assertEquals("image/tiff; application=geotiff; profile=cloud-optimized", asset.getType());
        assertEquals("https://sentinel-cogs.s3.us-west-2.amazonaws.com/sentinel-s2-l2a-cogs/33/S/VB/2021/2/S2B_33SVB_20210221_0_L2A/B01.tif", asset.getAssetUrl());
        assertEquals(0.0, asset.getNoValue());
    }

    public void testCreateInvalidStacAssetTypeInformationNotAvailable() throws JsonProcessingException {
        String assetJSON = "{\"title\":\"Band 1 (coastal) BOA reflectance\",\"roles\":[\"data\"],\"gsd\":60,\"eo:bands\":[{\"name\":\"B01\",\"common_name\":\"coastal\",\"center_wavelength\":0.4439,\"full_width_half_max\":0.027}],\"href\":\"https://sentinel-cogs.s3.us-west-2.amazonaws.com/sentinel-s2-l2a-cogs/33/S/VB/2021/2/S2B_33SVB_20210221_0_L2A/B01.tif\",\"proj:shape\":[1830,1830],\"proj:transform\":[60,0,399960,0,-60,4200000,0,0,1],\"raster:bands\":[{\"data_type\":\"uint16\",\"spatial_resolution\":60,\"bits_per_sample\":15,\"nodata\":0,\"statistics\":{\"minimum\":1,\"maximum\":20567,\"mean\":2339.4759595597,\"stddev\":3026.6973619954,\"valid_percent\":99.83}}]}";
        JsonNode node = mapper.readTree(assetJSON);

        HMStacAsset asset = new HMStacAsset("B01", node);

        assertFalse(asset.isValid());
        assertEquals("type information not available", asset.getNonValidReason());
    }

    public void testCreateInvalidStacAssetNotACOG() throws JsonProcessingException {
        String assetJSON = "{\"title\":\"Band 1 (coastal) BOA reflectance\",\"type\":\"image/tiff;\",\"roles\":[\"data\"],\"gsd\":60,\"eo:bands\":[{\"name\":\"B01\",\"common_name\":\"coastal\",\"center_wavelength\":0.4439,\"full_width_half_max\":0.027}],\"href\":\"https://sentinel-cogs.s3.us-west-2.amazonaws.com/sentinel-s2-l2a-cogs/33/S/VB/2021/2/S2B_33SVB_20210221_0_L2A/B01.tif\",\"proj:shape\":[1830,1830],\"proj:transform\":[60,0,399960,0,-60,4200000,0,0,1],\"raster:bands\":[{\"data_type\":\"uint16\",\"spatial_resolution\":60,\"bits_per_sample\":15,\"nodata\":0,\"statistics\":{\"minimum\":1,\"maximum\":20567,\"mean\":2339.4759595597,\"stddev\":3026.6973619954,\"valid_percent\":99.83}}]}";
        JsonNode node = mapper.readTree(assetJSON);

        HMStacAsset asset = new HMStacAsset("B01", node);

        assertFalse(asset.isValid());
        assertEquals("not a COG", asset.getNonValidReason());
    }

    public void testCreateStacAssetRasterBandsMetadataMissingIsValid() throws JsonProcessingException {
        String assetJSON = "{\"title\":\"Band 1 (coastal) BOA reflectance\",\"type\":\"image/tiff; application=geotiff; profile=cloud-optimized\",\"roles\":[\"data\"],\"gsd\":60,\"eo:bands\":[{\"name\":\"B01\",\"common_name\":\"coastal\",\"center_wavelength\":0.4439,\"full_width_half_max\":0.027}],\"href\":\"https://sentinel-cogs.s3.us-west-2.amazonaws.com/sentinel-s2-l2a-cogs/33/S/VB/2021/2/S2B_33SVB_20210221_0_L2A/B01.tif\",\"proj:shape\":[1830,1830],\"proj:transform\":[60,0,399960,0,-60,4200000,0,0,1],\"raster:bands\":[]}";
        JsonNode node = mapper.readTree(assetJSON);

        HMStacAsset asset = new HMStacAsset("B01", node);

        assertTrue(asset.isValid());
    }

    public void testReadRasterFromHTTP() throws Exception {
        String assetJSON = "{\"title\":\"Band 1 (coastal) BOA reflectance\",\"type\":\"image/tiff; application=geotiff; profile=cloud-optimized\",\"roles\":[\"data\"],\"gsd\":60,\"eo:bands\":[{\"name\":\"B01\",\"common_name\":\"coastal\",\"center_wavelength\":0.4439,\"full_width_half_max\":0.027}],\"href\":\"https://sentinel-cogs.s3.us-west-2.amazonaws.com/sentinel-s2-l2a-cogs/33/S/VB/2021/2/S2B_33SVB_20210221_0_L2A/B01.tif\",\"proj:shape\":[1830,1830],\"proj:transform\":[60,0,399960,0,-60,4200000,0,0,1],\"raster:bands\":[{\"data_type\":\"uint16\",\"spatial_resolution\":60,\"bits_per_sample\":15,\"nodata\":0,\"statistics\":{\"minimum\":1,\"maximum\":20567,\"mean\":2339.4759595597,\"stddev\":3026.6973619954,\"valid_percent\":99.83}}]}";
        JsonNode node = mapper.readTree(assetJSON);
        String assetId = "rainfall";
        HMStacAsset hmAsset = new HMStacAsset(assetId, node);
        RegionMap regionMap = RegionMap.fromBoundsAndGrid(1640000.0, 1640200.0, 5140000.0, 5140160.0, 20, 16);

        GridCoverage2D grid = hmAsset.readRaster(regionMap);

        assertNotNull(grid);
    }

    @Ignore("We need to find a proper endpoint to test and add the credentials.")
    public void testReadRasterFromS3() throws Exception {
        String assetJSON = "{\"href\":\"s3://deafrica-input-datasets/rainfall_chirps_monthly/chirps-v2.0_1981.01.tif\",\"type\":\"image/tiff; application=geotiff; profile=cloud-optimized\",\"title\":\"rainfall\",\"eo:bands\":[{\"name\":\"rainfall\"}],\"proj:epsg\":4326,\"proj:shape\":[1600,1500],\"proj:transform\":[0.05000000074505806,0,-20,0,-0.05000000074505806,40,0,0,1],\"roles\":[\"data\"]}},\"bbox\":[-20,-40.000001192092896,55.00000111758709,40],\"stac_extensions\":[\"https://stac-extensions.github.io/eo/v1.1.0/schema.json\",\"https://stac-extensions.github.io/projection/v1.1.0/schema.json\"],\"collection\":\"rainfall_chirps_monthly\"}";
        JsonNode node = mapper.readTree(assetJSON);
        String assetId = "rainfall";
        String region = "af-south-1";
        HMStacAsset hmAsset = new HMStacAsset(assetId, node);
        RegionMap regionMap = RegionMap.fromBoundsAndGrid(1640000.0, 1640200.0, 5140000.0, 5140160.0, 20, 16);
        hmAsset.readRaster(regionMap, region);
    }
}
