package webapp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vehicleApi.model.Cliente;
import com.vehicleApi.model.User;
import com.vehicleApi.model.Vehicle;


@WebServlet(urlPatterns = "/login.do")
public class LoginServlet extends HttpServlet {
	
	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("MyDatabase");
	private static EntityManager entityManager = entityManagerFactory.createEntityManager();
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Yahoo!!!!!!!!</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("My First Servlet");
		out.println("</body>");
		out.println("</html>");
		
		// FIND
		Cliente cliente = entityManager.find(Cliente.class, 1);
		System.out.println("User name: " + cliente.getName());
		
		// INSERT
		//Cliente user = new Cliente();
		//user.setName("Not Here");
		
		//entityManager.getTransaction().begin();
		//entityManager.persist(user);
		//entityManager.getTransaction().commit();

		// DELETE
		//Cliente user = entityManager.find(Cliente.class, 2);
		//entityManager.getTransaction().begin();
		//entityManager.remove(user);
		//entityManager.getTransaction().commit();
		
		// MODIFY
		//Cliente user = new Cliente();
		//user.setId(1);
		//user.setName("I am changed...");
		//entityManager.getTransaction().begin();
		//entityManager.merge(user);
		//entityManager.getTransaction().commit();
		
		User user = new User();
		user.setName("Gabriel Muller");
		user.setCpf("09419887910");
		user.setEmail("gabriel_goetz@hotmail.com");
		java.util.Date date = new java.util.Date();
		java.sql.Date dateSql = new java.sql.Date(date.getTime());
		user.setBirthday(dateSql);
		
		Vehicle vehicle = new Vehicle(user);
		vehicle.setBrand("Nissan");
		vehicle.setModel("March");
		vehicle.setYear(2013);
		
		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(vehicle);
		entityManager.getTransaction().commit();
		
		entityManager.close();
		entityManagerFactory.close();
		
	}

}