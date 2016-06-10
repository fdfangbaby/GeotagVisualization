package com.cn.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cn.entity.Taxi;

@Repository("taxiDao")
public class TaxiDaoImpl extends HibernateDaoSupport implements TaxiDao{

	@Resource
	public void setSuperSessionFactory(SessionFactory sessionFactory) {
		this.setSessionFactory(sessionFactory);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Taxi> Search(String taxiId, String time) {
		if (time==null)
		{
			return this.getSession().createQuery("from Taxi where Taxi_Id=? order by Time").setParameter(0, taxiId).list();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Taxi> getAll() {
		return this.getSession().createQuery("from Taxi order by Time").list();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set<String> getAllTaxiId() {
		List<Taxi> array = this.getSession().createQuery("from Taxi").list();
		Set<String> result = new HashSet();
		for(int i=0;i<array.size();i++){
			result.add(array.get(i).getTaxi_Id());
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set<String> GetTaxiIdByFuzzy(String fuzzyId){
		String fuzzy = "%"+fuzzyId;
		List<Taxi> array = this.getSession().createQuery("from Taxi where Taxi_Id like ?").setParameter(0, fuzzy).list();
		Set<String> result = new HashSet();
		for(int i=0;i<array.size();i++){
			result.add(array.get(i).getTaxi_Id());
		}
		return result;
	}

	@Transactional
	@Override
	public void Delete(Taxi taxi) {
		this.getHibernateTemplate().delete(taxi);
	}

}
