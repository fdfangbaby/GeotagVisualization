package com.cn.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.cn.dao.TaxiDao;
import com.cn.dao.TurningPointDao;
import com.cn.entity.Taxi;
import com.cn.entity.TurningPoint;

@Service("taxiService")
public class TaxiServiceImpl implements TaxiService {

	private TaxiDao taxiDao;
	
	private TurningPointDao turningPointDao;

	public TurningPointDao getTurningPointDao() {
		return turningPointDao;
	}

	@Resource
	public void setTurningPointDao(TurningPointDao turningPointDao) {
		this.turningPointDao = turningPointDao;
	}

	public TaxiDao getTaxiDao() {
		return taxiDao;
	}

	@Resource
	public void setTaxiDao(TaxiDao taxiDao) {
		this.taxiDao = taxiDao;
	}

	@Override
	public List<Taxi> search(String taxiId, String time) {
		List<Taxi> result = taxiDao.Search(taxiId, time);
		transform(result);
		return result;
	}

	@Override
	public List<Taxi> getAll() {
		return taxiDao.getAll();
	}

	@Override
	public Set<String> getAllTaxiId( ) {
		return taxiDao.getAllTaxiId();
	}

	@Override
	public void transform(List<Taxi> list) {
		for(int i=0;i<list.size();i++)
		{
			double longtitude = Double.parseDouble(list.get(i).getLongtitude())+0.0045;
			list.get(i).setLongtitude(Double.toString(longtitude));
			double latitude = Double.parseDouble(list.get(i).getLatitude())-0.002;
			list.get(i).setLatitude(Double.toString(latitude));
		}
	}

	@Override
	public void GenerateTurningPoints(List<String> taxiIds) {
		List<TurningPoint> turningPoints = new ArrayList<TurningPoint>();
		System.out.println("ids:"+taxiIds.size());
		for(int i = 0; i<taxiIds.size();i++){
			List<Taxi> array = taxiDao.Search(taxiIds.get(i), null);
			//handle single trajectory of one car
			for(int j=1;j<array.size();j++){
				Taxi pre = array.get(j-1);
				Taxi aft = array.get(j);
				if (!pre.getState().equals(aft.getState())) {
					turningPoints.add(
							new TurningPoint(pre.getLongtitude(), pre.getLatitude(), pre.getState(), pre.getTime(), pre.getTaxi_Id(), pre.getSpeed(), null));
					turningPoints.add(
							new TurningPoint(aft.getLongtitude(), aft.getLatitude(), aft.getState(), aft.getTime(), aft.getTaxi_Id(), aft.getSpeed(), null));
				}
//				if (pre.getState().equals(aft.getState())&&pre.getLongtitude().equals(aft.getLongtitude()) && pre.getLatitude().equals(aft.getLatitude())
//						&& !pre.getTime().equals(aft.getTime()) && pre.getSpeed().equals("0")&& aft.getSpeed().equals("0")) {
//					turningPoints.add(
//							new TurningPoint(aft.getLongtitude(), aft.getLatitude(), aft.getState(), aft.getTime(), aft.getTaxi_Id(), aft.getSpeed()));
//					
//				}
			}
		}
		//save all turningpoints
		for(int i=0;i<turningPoints.size();i++){
			turningPointDao.save(turningPoints.get(i));
		}
		
	}

	@Override
	public List<List<Taxi>> GetFragment(List<String> list) {
		List<List<Taxi>> result= new ArrayList<>();
		for(int i=0;i<list.size();i++)
		{
			List<Taxi> array = taxiDao.Search(list.get(i), null);
			int trajectoryTotalSize = array.size();
			int fragmentSize = 0;
			List<Taxi> fragment = null;
			for(int j=0;j<trajectoryTotalSize;j++)
			{
				Taxi currentTaxi = array.get(j);
				double cos=1;
				if(j > 0 && j < trajectoryTotalSize - 1){
				double xA = Double.parseDouble(currentTaxi.getLatitude())-Double.parseDouble(array.get(j-1).getLatitude());
				double yA = Double.parseDouble(currentTaxi.getLongtitude())-Double.parseDouble(array.get(j-1).getLongtitude());
				double xB = Double.parseDouble(array.get(j+1).getLatitude())-Double.parseDouble(currentTaxi.getLatitude());
				double yB = Double.parseDouble(array.get(j+1).getLatitude())-Double.parseDouble(currentTaxi.getLatitude());
				cos = (xA*xB+yA*yB)/(Math.sqrt(xA*xA+yA*yA)*Math.sqrt(xB*xB+yB*yB));
				}
				//ignore no-load point
				if (currentTaxi.getState().equals("0"))
					continue;
				//start a new fragment
				else if ((j == 0 && currentTaxi.getState().equals("1"))
						|| (j > 0 && currentTaxi.getState().equals("1") && array.get(j - 1).getState().equals("0"))) {
					if (fragment!=null&&fragmentSize != 0&&fragment.size()>2)
					{
						transform(fragment);
						result.add(fragment);
					}
						
					fragment = new ArrayList<>();
					fragment.add(currentTaxi);
					fragmentSize++;
					continue;
				}
				//找到特征点
				else if (j > 0 && j < trajectoryTotalSize - 1 && currentTaxi.getState().equals("1")
						&& array.get(j - 1).getState().equals("1") && array.get(j + 1).getState().equals("1")
						&& cos <= 0) {
					fragment.add(currentTaxi);
					if(fragment!=null&&fragment.size()>2) result.add(fragment);
					fragment = new ArrayList<>();
					fragmentSize++;
					fragment.add(currentTaxi);
					continue;
				}
//				else if (j > 0
//						&& ((Integer.parseInt(currentTaxi.getAngle())
//								- Integer.parseInt(array.get(j - 1).getAngle())) >= 90)
//						&& currentTaxi.getState().equals("1") && array.get(j - 1).getState().equals("1")) {
//					fragment.add(currentTaxi);
//					if(fragment.size()>1) result.add(fragment);
//					fragment = new ArrayList<>();
//					fragmentSize++;
//					fragment.add(currentTaxi);
//					continue;
//				}
				else if (currentTaxi.getState().equals("1")) fragment.add(currentTaxi);
			}
			//add the last fragment
			if(fragment!=null&&fragment.size()>2) {
				transform(fragment);
				result.add(fragment);
			}
		}
		return result;
	}
	

	@Override
	public double[][] GetSimilarity(List<List<Taxi>> trajectory) {
		int size = trajectory.size();
		double[][] similarity = new double[size][size];
		for(int i=0;i<size;i++){
			for (int j = 0; j < size; j++) {
				similarity[i][j] = GetDirectionSensitiveHausdorffDistance(trajectory.get(i), trajectory.get(j));
			}
		}
		return similarity;
	}

	@Override
	public double getHausdorffDistance(List<Taxi> t1, List<Taxi> t2) {
		double HD=0.0;
		double hAB=0.0;
		double hBA=0.0;
		int lenA=t1.size();
		int lenB=t2.size();
		double[]  minHA= new double[lenA];
		double[]  minHB = new double[lenB];
		
		for(int i=0;i<lenA;i++)
		{
			minHA[i] = GetDistance(Double.parseDouble(t1.get(i).getLongtitude()),
					Double.parseDouble(t1.get(i).getLatitude()), Double.parseDouble(t2.get(0).getLongtitude()),
					Double.parseDouble(t2.get(0).getLatitude()));

			for (int j = 1; j < lenB; j++)
			{
				double distance = GetDistance(Double.parseDouble(t1.get(i).getLongtitude()),
						Double.parseDouble(t1.get(i).getLatitude()), Double.parseDouble(t2.get(j).getLongtitude()),
						Double.parseDouble(t2.get(j).getLatitude()));
				if(distance<minHA[i])
					minHA[i]=distance;
			}
		}
		hAB=minHA[0];
		for(int i=1;i<minHA.length;i++)
		{
			if(minHA[i]>hAB)
				hAB=minHA[i];
				
		}
		
		for(int i=0;i<lenB;i++)
		{
			minHB[i]=GetDistance(Double.parseDouble(t2.get(i).getLongtitude()),
					Double.parseDouble(t2.get(i).getLatitude()), Double.parseDouble(t1.get(0).getLongtitude()),
					Double.parseDouble(t1.get(0).getLatitude()));
			for(int j=1;j<lenA;j++)
			{
				double distance = GetDistance(Double.parseDouble(t2.get(i).getLongtitude()),
						Double.parseDouble(t2.get(i).getLatitude()), Double.parseDouble(t1.get(j).getLongtitude()),
						Double.parseDouble(t1.get(j).getLatitude()));
				if(distance<minHB[i])
					minHB[i]=distance;
			}
		}
		hBA=minHB[0];
		for(int i=1;i<minHB.length;i++)
		{
			if(minHB[i]>hBA)
				hBA=minHB[i];
				
		}
		
		if(hAB<hBA)
			HD=hBA;
		else
			HD=hAB;
	
		return HD;
	}

	public double GetDirectionSensitiveHausdorffDistance(List<Taxi> TA, List<Taxi> TB){
		double scoreAB=0, scoreBA=0, closestDistanceSumAToB = 0, closestDistanceSumBToA = 0;
		int numOfTA = TA.size();
		int TASegmentSize = numOfTA-1;
		int numOfTB = TB.size();
		int TBSegmentSize = numOfTB-1;
		int closestIndex = 0;
		//each point in A to TB
		for(int i = 0;i<numOfTA;i++){
			double closestDistance = Double.MAX_VALUE;
			Taxi taxi = TA.get(i);
			for(int j = closestIndex;j<TBSegmentSize;j++){
				double x1 = Double.parseDouble(TB.get(j).getLatitude());
				double y1 = Double.parseDouble(TB.get(j).getLongtitude());
				double x2 = Double.parseDouble(TB.get(j+1).getLatitude());
				double y2 = Double.parseDouble(TB.get(j+1).getLongtitude());
				double x0 = Double.parseDouble(taxi.getLatitude());
				double y0 = Double.parseDouble(taxi.getLongtitude());
				double tempDistance = PointToLine(x1,y1,x2,y2,x0,y0);
				if(tempDistance<closestDistance){
					closestIndex = j;
					closestDistance = tempDistance;
				}
			}
			closestDistanceSumAToB = closestDistanceSumAToB + closestDistance;
		}
		scoreAB = closestDistanceSumAToB/numOfTA;
		
		//each point in B to TA
		closestIndex = 0;
		for(int i = 0;i<numOfTB;i++){
			double closestDistance = Double.MAX_VALUE;
			Taxi taxi = TB.get(i);
			for(int j = closestIndex;j<TASegmentSize;j++){
				double x1 = Double.parseDouble(TA.get(j).getLatitude());
				double y1 = Double.parseDouble(TA.get(j).getLongtitude());
				double x2 = Double.parseDouble(TA.get(j+1).getLatitude());
				double y2 = Double.parseDouble(TA.get(j+1).getLongtitude());
				double x0 = Double.parseDouble(taxi.getLatitude());
				double y0 = Double.parseDouble(taxi.getLongtitude());
				double tempDistance = PointToLine(x1,y1,x2,y2,x0,y0);
				if(tempDistance<closestDistance){
					closestIndex = j;
					closestDistance = tempDistance;
				}
			}
			closestDistanceSumBToA = closestDistanceSumBToA + closestDistance;
		}
		scoreBA = closestDistanceSumBToA/numOfTB;
		
		return (scoreAB+scoreBA)/2;
	}
	
	//point to line shortest distance
	private double PointToLine(double x1, double y1, double x2, double y2, double x0, double y0){
		  double space = 0;
          double a, b, c;
          a = GetDistance(x1, y1, x2, y2);// 线段的长度
          b = GetDistance(x1, y1, x0, y0);// (x1,y1)到点的距离
          c = GetDistance(x2, y2, x0, y0);// (x2,y2)到点的距离
          if (c <= 0.000001 || b <= 0.000001) {
             space = 0;
             return space;
          }
          if (a <= 0.000001) {
             space = b;
             return space;
          }
          if (c * c >= a * a + b * b) {
             space = b;
             return space;
          }
          if (b * b >= a * a + c * c) {
             space = c;
             return space;
          }
          double p = (a + b + c) / 2;// 半周长
          double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
          space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
          return space;
	}
	
	/** 
	 * 计算地球上任意两点(经纬度)距离 
	 *  
	 * @param long1 
	 *            第一点经度 
	 * @param lat1 
	 *            第一点纬度 
	 * @param long2 
	 *            第二点经度 
	 * @param lat2 
	 *            第二点纬度 
	 * @return 返回距离 单位：米 
	 */  
	public static double GetDistance(double long1, double lat1, double long2, double lat2) {
		double a, b, R;
		R = 6378.137; // 地球半径km
		lat1 = lat1 * Math.PI / 180.0;
		lat2 = lat2 * Math.PI / 180.0;
		a = lat1 - lat2;
		b = (long1 - long2) * Math.PI / 180.0;
		double d;
		double sa2, sb2;
		sa2 = Math.sin(a / 2.0);
		sb2 = Math.sin(b / 2.0);
		d = 2 * R * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));
		return d;
	}

	@Override
	public void DeleteDuplicateTaxi(List<String> id) {
		for(int i = 0;i<id.size();i++){
			List<Taxi> array = taxiDao.Search(id.get(i), null);
			int trajectorySize = array.size();
			for(int j = 1;j<trajectorySize;j++){
				Taxi pre = array.get(j-1);
				Taxi aft = array.get(j);
				
				if(pre.getTaxi_Id().equals(aft.getTaxi_Id())
						&&pre.getTime().equals(aft.getTime())) taxiDao.Delete(pre);
				else if (pre.getTaxi_Id().equals(aft.getTaxi_Id())
						&&pre.getAngle().equals(aft.getAngle())
						&&pre.getLatitude().equals(aft.getLatitude())
						&&pre.getLongtitude().equals(aft.getLongtitude())
						&&pre.getSpeed().equals(aft.getSpeed())
						&&pre.getState().equals(aft.getState())) {
					taxiDao.Delete(pre);
				}
			}
			
		}
		for(int i = 0;i<id.size();i++){
			List<Taxi> array = taxiDao.Search(id.get(i), null);
			if(array.size()<3){
				for(int j = 0;j<array.size();j++) taxiDao.Delete(array.get(j));
			}
		}
	}
}
