package com.cn.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.entity.CompareEntity;
import com.cn.entity.SearchEntity;
import com.cn.entity.Taxi;
import com.cn.service.TaxiService;

@Controller
@RequestMapping("/taxi")
public class TaxiController {

	private TaxiService taxiService;
	
	private List<String> taxiIdList ;

	public TaxiService getTaxiService() {
		return taxiService;
	}

	@Resource
	public void setTaxiService(TaxiService taxiService) {
		this.taxiService = taxiService;
	}
	
	@RequestMapping(value = "/single", method = RequestMethod.POST)
	public @ResponseBody List<Taxi> ShowAllData(@RequestBody SearchEntity searchEntity) {
		System.out.println("enter");
		System.out.println(searchEntity.getTaxiId());
		List<Taxi> result;
		if(searchEntity.getTaxiId()==null)result = taxiService.getAll();
		else result = taxiService.search(searchEntity.getTaxiId(), null);
		return result;
	}
	
	@RequestMapping(value = "/taxiid", method = RequestMethod.POST)
	public @ResponseBody Set<String> GetTaxiId(@RequestBody SearchEntity searchEntity) {
		System.out.println("enter id,index:"+searchEntity.getIndex());
		int index = searchEntity.getIndex();
		if(index==0)
		{
			taxiIdList = new ArrayList<String>(taxiService.getAllTaxiId());
			Set<String> result = new HashSet<>();
			for(int i = 0;i<10;i++)
			{
				result.add(taxiIdList.get(index*10+i));
			}
			return result;
		}
		else
		{
			Set<String> result = new HashSet<>();
			for(int i = 0;i<10;i++)
			{
				result.add(taxiIdList.get(index*10+i));
			}
			return result;
		}
	}
	
	@RequestMapping(value = "/alltaxiid", method = RequestMethod.POST)
	public @ResponseBody Set<String> GetAllTaxiId() {
		return taxiService.getAllTaxiId();
	}
	
	@RequestMapping(value = "/all", method = RequestMethod.POST)
	public @ResponseBody List<Taxi> GetAllTaxi()
	{
		System.out.println("enter all");
		return taxiService.getAll();
	}
	
	@RequestMapping(value="/compare", method = RequestMethod.POST)
	public @ResponseBody CompareEntity ComparePath(@RequestBody SearchEntity searchEntity){
		System.out.println("enter compare");
		CompareEntity result = new CompareEntity();
		List<List<Taxi>> trajectory = taxiService.GetFragment(searchEntity.getComparedTaxiIds());
		result.setFragment(trajectory);
		result.setSimilarity(taxiService.GetSimilarity(trajectory));
		return result;
		
	}
	
	@RequestMapping(value="/test/generate")
	public @ResponseBody String Test()
	{
		taxiService.GenerateTurningPoints(taxiIdList);  
		//taxiService.deleteDuplicate();
		return "hello";
	}
	
	@RequestMapping(value="/test/delete", method = RequestMethod.POST)
	public @ResponseBody String DeleteDuplicateTaxi(){
		taxiService.DeleteDuplicateTaxi(taxiIdList);
		return "hello";
	} 
}
