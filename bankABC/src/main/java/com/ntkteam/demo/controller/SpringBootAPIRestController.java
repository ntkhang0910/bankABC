package com.ntkteam.demo.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.ntkteam.demo.model.ModelUser;

@RestController
@RequestMapping("/hello")
public class SpringBootAPIRestController 
{
	final String uri = "http://localhost:8080/oauth2/test/ntkteam?access_token=v12dd4c6-21c3-573d-a68e-1914ceefc21f";
	
	// can split to service
    String jdbcURL = "jdbc:mysql://localhost:3306/test";
    String username = "user";
    String password = "password";
    //
    
    int batchSize = 20;
    Connection connection = null;
	
	// Call RESAPI from serviceBankReconciliationSystem get csv file 
	@RequestMapping(value = {"/getCsvFromBankReconciliation"}, method = RequestMethod.GET)
	public void getCsvFromBankReconciliation(@RequestParam(value = "name")String name) 
	{	    

        
		//Get csv from service banking
	    RestTemplate restTemplate = new RestTemplate();
	    String csvFilePath = restTemplate.getForObject(uri, String.class);
	    //	
	    System.out.println(csvFilePath);
	    
	    if (!csvFilePath.isEmpty()) {
	        try {
	        	 
	            connection = DriverManager.getConnection(jdbcURL, username, password);
	            connection.setAutoCommit(false);
	 
	            String sql = "INSERT INTO review (date, content, amount, type) VALUES (?, ?, ?, ?)";
	            PreparedStatement statement = connection.prepareStatement(sql);
	 
	            BufferedReader lineReader = new BufferedReader(new FileReader(csvFilePath));
	            String lineText = null;
	 
	            int count = 0;
	 
	            lineReader.readLine(); // skip header line
	 
	            while ((lineText = lineReader.readLine()) != null) {
	                String[] data = lineText.split(",");
	                String date = data[0];
	                String content = data[1];
	                String amount = data[2];
	                String type = data[3];
	 
	                statement.setString(1, date);
	                statement.setString(2, content);
	                statement.setString(3, amount);
	                statement.setString(4, type);

	                statement.addBatch();
	 
	                if (count % batchSize == 0) {
	                    statement.executeBatch();
	                }
	            }
	 
	            lineReader.close();
	 
	            // execute the remaining queries
	            statement.executeBatch();
	 
	            connection.commit();
	            connection.close();
	 
	        } catch (IOException ex) {
	            System.err.println(ex);
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	 
	            try {
	                connection.rollback();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	 
	      }
	    }
	}
	
//	// UC1 :-  http://localhost:8080/hello
//	@RequestMapping(value = {"", "/", "/home"})
//	public String sayHello()
//	{
//		return "Hello From ntkteam!!!";
//	}
//	
//	// UC2 :- http://localhost:8080/hello/query?name=Tom
//	@RequestMapping(value = {"/query"}, method = RequestMethod.GET)
//	public String sayHello(@RequestParam(value = "name")String name) 
//	{
//		return "Hello " + name ;
//	}
//	
//	// UC3 :- http://localhost:8080/hello/param/Tom
//	@GetMapping("/param/{name}")
//	public String sayHelloParam(@PathVariable String name)
//	{
//		return "Hello " + name + "!" ;
//	}
//	
//	// UC4 :- http://localhost:8080/hello/post : {"firstName": "Tom","lastName": "Lucky"}
//	@PostMapping("/post")
//	public String sayHello(@RequestBody ModelUser user) 
//	{
//		return "Hello " + user.getFirstName() + " " + user.getLastName();
//	}
//	
//	// UC5 :- http://localhost:8080/hello/put/Tom/?lastName=Lucky
//	@PutMapping("/put/{firstName}")
//	public String sayHello(@PathVariable String firstName, @RequestParam(value = "lastName")String lastName)
//	{
//		return "Hello " + firstName + " " + lastName ;
//	}
	