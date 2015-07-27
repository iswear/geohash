package com.zmg.geohash;

import java.util.ArrayList;

public class Encoder {
	private char[] dictionary = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};  
	private static Encoder instance = null;
	private Encoder(){
		
	}
	/*
	 * 经纬度编码
	 * 
	 */
	public String encoderCoordinate16(double weidu,double jindu){
		int weidu1 = (int)(weidu*10000000);
		int jindu1 = (int) ((weidu>=0)?(10000000.0*jindu*Math.cos(weidu/90*Math.PI/2)):(-10000000.0*jindu*Math.cos(weidu/90*Math.PI/2)));
		
		char[] encodestring = new char[16];
		for(int i=0,position=0,value=0;i<32;++i){
			int weidutemp = (weidu1>>i&1);
			int jindutemp = (jindu1>>i&1);
			int tail = i%2;
			value |= (weidutemp<<(2*tail) | jindutemp<<(2*tail+1));
			if(tail == 1 || i==31){
				encodestring[15-position] = this.dictionary[value];
				position++;
				value = 0;
			}
		}
		return String.valueOf(encodestring);
	}
	
	/*
	 * 返回附近八个点编码
	 * 说明：这里的八个点是横竖距离是以2*distance为边长的正方形四周的的八个点
	 * 
	 */
	public String[] getNearByEncoders(double weidu,double jindu, double distance){
		String[] nearbypoints = new String[8];
		
		double jinduspace = distance*(Math.cos(weidu/90*Math.PI/2))/1100000;
		double weiduspace = distance/1100000;
		
		double tempjindu1 = (jindu+jinduspace > 180)?180:(jindu+jinduspace);
		double tempjindu2 = (jindu-jinduspace < -180)?-180:(jindu-jinduspace);
		double tempweidu1 = (weidu+weiduspace > 90)?90:(weidu+weiduspace);
		double tempweidu2 = (weidu-weiduspace < -90)?-90:(weidu-weiduspace);
		
		nearbypoints[0] = this.encoderCoordinate16(weidu, tempjindu1);
		nearbypoints[1] = this.encoderCoordinate16(weidu, tempjindu2);
		nearbypoints[2] = this.encoderCoordinate16(tempweidu1, jindu);
		nearbypoints[3] = this.encoderCoordinate16(tempweidu2, jindu);
		
		nearbypoints[4] = this.encoderCoordinate16(tempweidu1, tempjindu1);
		nearbypoints[5] = this.encoderCoordinate16(tempweidu1, tempjindu2);
		nearbypoints[6] = this.encoderCoordinate16(tempweidu2, tempjindu1);
		nearbypoints[7] = this.encoderCoordinate16(tempweidu2, tempjindu2);
		return nearbypoints;
	}
	
	/*
	 * 
	 * 返回周边八个点加自身点的编码搜索前缀
	 * 说明：通过这八个点的搜索前缀通过数据库搜索得到的数据并不是严格限制再distance范围内
	 * 因进位，范围成幂增长，周边八个点的近似选择等造成世纪搜索范围比distance大，因此通过
	 * like搜索之后如果要进行精确查找和排序，还需要进行耳机距离计算以及排序
	 * 
	 * 
	 */
	
	public String[] getNearByPrefix(double weidu,double jindu,double distance){
		String point = this.encoderCoordinate16(weidu, jindu);
		String[] pointsnearby = this.getNearByEncoders(weidu, jindu, distance);
		return this.getNearByPrefix(point, pointsnearby, distance);
	}
	public String[] getNearByPrefix(String point, String[] nearbypoints, double distance){
		double tempdistance = distance * 10;
		double rate = 4;
		int level = 0;
		while(tempdistance > 1.0){
			tempdistance = tempdistance / rate;
			level += 1;
		}
		level += 2;
		
		ArrayList<String> nearbyprefixes = new ArrayList<String>();
		nearbyprefixes.add(point.substring(0,point.length()-level));
		int nearbyprefixsize = 1;
		for(int i=0;i<nearbypoints.length;++i){
			String nearbyprefix = nearbypoints[i].substring(0, nearbypoints[i].length()-level);
			int j;
			for(j=0;j<nearbyprefixsize;++j){
				if(nearbyprefix.equals(nearbyprefixes.get(j))){
					break;
				}
			}
			if(j == nearbyprefixsize){
				nearbyprefixes.add(nearbyprefix);
				nearbyprefixsize ++;
			}
		}
		String[] retvalue = new String[1];
		return nearbyprefixes.toArray(retvalue);
	}
	
	public static Encoder getInstance(){
		if(Encoder.instance == null){
			Encoder.instance = new Encoder();
		}
		return Encoder.instance;
	}
}