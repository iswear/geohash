package com.zmg.geohash;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by iswear on 2017/4/22.
 */
public class GeoHashUtil {

    // 单位厘米
    private static final int EARTH_RADIUS = 637139300;

    // 单位厘米
    private static final double DISTANCE_PER_LAT = EARTH_RADIUS * 2 * Math.PI / 360;

    // 单位厘米
    private static final double DISTANCE_PER_LNG_IN_EQUATOR = EARTH_RADIUS * 2 * Math.PI / 360;

    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 计算两点之间直线距离
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double latAng1 = lat1 * Math.PI / 180;
        double latAng2 = lat2 * Math.PI / 180;
        double lngAng1 = lng1 * Math.PI / 180;
        double lngAng2 = lng2 * Math.PI / 180;

        double deltaLatDisPow = Math.pow(Math.abs(Math.sin(latAng1) - Math.sin(latAng2)) * EARTH_RADIUS, 2);

        double deltaLng = Math.abs(lngAng1 - lngAng2);
        if (deltaLng > Math.PI) {
            deltaLng = 2 * Math.PI - deltaLng;
        }
        double radius1 = EARTH_RADIUS * Math.cos(latAng1);
        double radius2 = EARTH_RADIUS * Math.cos(latAng2);

        double deltaLngDisPow = Math.pow(radius1, 2) + Math.pow(radius2, 2) - 2 * radius1 * radius2 * Math.cos(deltaLng);
        return Math.sqrt(deltaLatDisPow + deltaLngDisPow) / 100;
    }

    /**
     * 对指定经纬度进行16位编码
     * @param lat
     * @param lng
     * @return
     *
     */
    public String encodeCoordinate(double lat, double lng) {
        int scaleLat = (int)(lat * DISTANCE_PER_LAT);
        int scaleLng = (int)(lng * DISTANCE_PER_LNG_IN_EQUATOR);
        char[] encodeArr = new char[16];
        for (int i = 0, position = 0; i < 32; i += 2, ++position) {
            int latTemp1 = (scaleLat >> i & 1);
            int lngTemp1 = (scaleLng >> i & 1);
            int latTemp2 = (scaleLat >> (i + 1) & 1);
            int lngTemp2 = (scaleLng >> (i + 1) & 1);
            int hexIndex = latTemp1 | (lngTemp1 << 1) | (latTemp2 << 2) | (lngTemp2 << 3);
            encodeArr[15 - position] = HEX_CHAR[hexIndex];
        }
        return String.valueOf(encodeArr);
    }

    /**
     * 返回周围八个点的编码
     * @param lat 纬度
     * @param lng 经度
     * @param distance 范围(米)
     * @return
     */
    public String[] encodeAroundCoordinate(double lat, double lng, double distance) {
        String[] aroundPoints = new String[8];

        double latSpace = distance * 100 / DISTANCE_PER_LAT;
        double lngSpace;
        if ((Math.cos(lat * Math.PI / 180)) != 0) {
            lngSpace = 360;
        } else {
            lngSpace = distance * 100 / DISTANCE_PER_LNG_IN_EQUATOR / (Math.cos(lat * Math.PI / 180));
        }

        double lat1 = (lat + latSpace > 90) ? 90 : (lat + latSpace);
        double lat2 = (lat - latSpace < -90) ? -90 : (lat - latSpace);
        double lng1 = (lng + lngSpace > 180) ? 180 : (lng + lngSpace);
        double lng2 = (lng - lngSpace <-180) ? -180 : (lng - lngSpace);

        aroundPoints[0] = this.encodeCoordinate(lat, lng1);
        aroundPoints[1] = this.encodeCoordinate(lat, lng2);
        aroundPoints[2] = this.encodeCoordinate(lat1, lng);
        aroundPoints[3] = this.encodeCoordinate(lat2, lng);

        aroundPoints[4] = this.encodeCoordinate(lat1, lng1);
        aroundPoints[5] = this.encodeCoordinate(lat1, lng2);
        aroundPoints[6] = this.encodeCoordinate(lat2, lng1);
        aroundPoints[7] = this.encodeCoordinate(lat2, lng2);

        return aroundPoints;
    }

    /**
     * 返回前缀
     * @param lat
     * @param lng
     * @param distance
     * @return
     */
    public String[] getAroundEncoderPrefix(double lat, double lng, double distance) {
        String centerEncoder = this.encodeCoordinate(lat, lng);
        String[] aroundEncoders = this.encodeAroundCoordinate(lat, lng, distance);
        return this.getAroundEncoderPrefix(centerEncoder, aroundEncoders, distance);
    }

    /**
     * 返回前缀
     * @param centerEncoder
     * @param aroundEncoders
     * @param distance
     * @return
     *
     * 进度范围从  3cm,3*4cm ... 3*4^ncm
     */
    private String[] getAroundEncoderPrefix(String centerEncoder, String[] aroundEncoders, double distance) {
        double tempDistance = distance * 100;
        int level = 1;
        while (tempDistance > 3.0) {
            tempDistance = tempDistance / 4;
            level += 1;
        }
        Set<String> aroundPrefixes = new HashSet<>();
        aroundPrefixes.add(centerEncoder.substring(0, centerEncoder.length() - level));
        for (String aroundEncoder : aroundEncoders) {
            String aroundPrefix = aroundEncoder.substring(0, aroundEncoder.length() - level);
            aroundPrefixes.add(aroundPrefix);
        }
        String[] retValue = new String[aroundPrefixes.size()];
        return aroundPrefixes.toArray(retValue);
    }



    public static GeoHashUtil create() {
        return new GeoHashUtil();
    }


}
