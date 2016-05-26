/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package org.jgrasstools.nww.layers;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.mercator.*;
import gov.nasa.worldwind.util.*;

import java.net.*;

/**
 * @version $Id: OSMMapnikLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OSMMapnikLayer extends BasicMercatorTiledImageLayer {
    public OSMMapnikLayer() {
        super(makeLevels());
    }

    private static LevelSet makeLevels() {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/OSM-Mercator/OpenStreetMap Mapnik");
        params.setValue(AVKey.SERVICE, "http://a.tile.openstreetmap.org/");
        params.setValue(AVKey.DATASET_NAME, "h");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.NUM_LEVELS, 22);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45d)));
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());

        return new LevelSet(params);
    }

    private static class URLBuilder implements TileUrlBuilder {
        public URL getURL( Tile tile, String imageFormat ) throws MalformedURLException {
            int zoom = tile.getLevelNumber() + 3;
            int x = tile.getColumn();
            int y = (1 << (tile.getLevelNumber()) + 3) - 1 - tile.getRow();
         
            return new URL(tile.getLevel().getService() + zoom + "/" + x + "/" + y + ".png");
        }
    }

    @Override
    public String toString() {
        return "OpenStreetMap";
    }
}
