package com.vehicleApi.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.vehicleApi.model.User;
import com.vehicleApi.model.Vehicle;

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
	public User getUserByEmail(String email) {
		TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
		query.setParameter("email", email);
		try {
			return query.getSingleResult();
		} catch (NoResultException nre) {
			return null;
		}
	}
	
	@Override
	public User getUserByCpf(String cpf) {
		TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.cpf = :cpf", User.class);
		query.setParameter("cpf", cpf);
		try {
			return query.getSingleResult();
		} catch (NoResultException nre) {
			return null;
		}
		
	}
	
	@Override
	public List<User> getAll() {
		TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
		return query.getResultList();
	}
	
	@Override
	public User saveUser(User user) {
		em.getTransaction().begin();
		em.persist(user);
		em.getTransaction().commit();
		System.out.println("I am here");
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
