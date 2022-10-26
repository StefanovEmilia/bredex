package bredex.backendTest.rest.db;

import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import bredex.backendTest.rest.model.Position;
import bredex.backendTest.rest.model.User;

public class Database {

	private SessionFactory sessionFactory;

	public Database() {

		StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();

		sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
	}

	public boolean isEmailExists(String email) {

		boolean exists = false;

		Session session = sessionFactory.openSession();
		Transaction tr = session.beginTransaction();

		Query q = session.createQuery("SELECT u FROM User u WHERE email= :email", User.class);
		q.setParameter("email", email);
		List<User> users = q.getResultList();

		if (users.size() > 0) {
			exists = true;
		}

		tr.commit();
		session.close();

		return exists;
	}

	public void saveClient(User user) {

		Session session = sessionFactory.openSession();
		Transaction tr = session.beginTransaction();

		session.save(user);

		tr.commit();
		session.close();
	}

	public User getClientByApiKey(String apiKey) {

		User user = null;

		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();

		user = session.get(User.class, apiKey);

		tx.commit();
		session.close();

		return user;
	}

	public void savePosition(Position pos) {

		Session session = sessionFactory.openSession();
		Transaction tr = session.beginTransaction();

		session.save(pos);

		tr.commit();
		session.close();

	}

	public int getLatestPositionID() {

		int id = 0;

		Session session = sessionFactory.openSession();
		Transaction tr = session.beginTransaction();

		Query q = session.createQuery("SELECT p FROM Position p");
		List<Position> positions = q.getResultList();

		id = positions.get(positions.size() - 1).getId();

		tr.commit();
		session.close();

		return id;
	}

	public List<Position> getPositions(String keyword, String location) {

		List<Position> positions = null;

		Session session = sessionFactory.openSession();
		Transaction tr = session.beginTransaction();

		String query = "";
		Query q = null;

		// Check if we want to search location too
		if (location.length() > 0) {

			query = "SELECT p FROM Position p WHERE p.roleName LIKE :keyword AND p.location = :location";

			q = session.createQuery(query, Position.class);
			q.setParameter("keyword", "%" + keyword + "%");
			q.setParameter("location", location);

		} else {

			// If we don't want to search location, only the roles
			query = "SELECT p FROM Position p WHERE p.roleName LIKE :keyword";
			q = session.createQuery(query, Position.class);
			q.setParameter("keyword", "%" + keyword + "%");
		}

		positions = q.getResultList();

		tr.commit();
		session.close();

		return positions;
	}

	public Position getPositionById(int id) {

		Session session = sessionFactory.openSession();
		Transaction tr = session.beginTransaction();

		Position pos = session.get(Position.class, id);

		tr.commit();
		session.close();

		// Set the adverter's name by the API key
		// I think its a must, because the API key is a sensitive data, but the advertiser is an important detail
		User user = getClientByApiKey(pos.getAdverter());
		pos.setAdverter(user.getName());

		return pos;
	}

	public void close() {
		sessionFactory.close();
	}

}
