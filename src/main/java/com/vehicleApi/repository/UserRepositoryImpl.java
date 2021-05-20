package com.vehicleApi.repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.vehicleApi.model.User;

@Repository
public class UserRepositoryImpl implements UserRepository{

	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("MyDatabase");
	private static EntityManager em = entityManagerFactory.createEntityManager();
	
	
	public UserRepositoryImpl() {
		
	}
	
	public UserRepositoryImpl(EntityManager em) {
		UserRepositoryImpl.em = em;
	}
	
	@Override
	public User getUserById(int id) { 
		return em.find(User.class, id);
	}
	
	@Override
	public User getUserByName(String name) {
		TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.name = :name", User.class);
		query.setParameter("name", name);
		return query.getSingleResult();
	}
	
	@Override
	public User saveUser(User user) {
		em.persist(user);
		return user;
	}
	
	@Override
	public User updateUser(User user) {
		em.merge(user);
		return user;
	}
	
	@Override
	public void deleteUser(User user) {
		if (em.contains(user)) {
			em.remove(user);
		} else {
			em.merge(user);
		}
	}
	
}
