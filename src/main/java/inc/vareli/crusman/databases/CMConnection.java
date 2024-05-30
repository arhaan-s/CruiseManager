package inc.vareli.crusman.databases;

import inc.vareli.crusman.databases.Ship.*;
import inc.vareli.crusman.databases.Trip.*;

import java.util.List;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import java.sql.*;


/**
 * A wrapper around java.sql.Connection to encapsulate it for use in CrusMan
 * @author Arhaan Sami 3751940
 */
public class CMConnection {
	private Connection connector;
	/**
	 * Creates a database connection, and sets up the tables to be used in all database operations. 
	 * If tables already exist, it does not create duplicates.
	 */
	public CMConnection(String url, String loginID, String loginPass) throws IllegalArgumentException { 
		try {
			connector = DriverManager.getConnection(url, loginID, loginPass);
			String shipCreator = "Create table CruiseShip (" +
							"shipID int unsigned not null primary key, " +
							"interiorRooms int unsigned not null, " + 
							"outsideRooms int unsigned not null, " + 
							"balconyRooms int unsigned not null, " + 
							"suites int unsigned not null)";
			String tripCreator = "Create table Trip (" + 
							"tripID int unsigned not null primary key, " + 
							"shipID int unsigned not null, " + 
							"drinkFees float unsigned not null, " + 
							"mealFees float unsigned not null, " + 
							"foreign key(shipID) references CruiseShip(shipID))";
			String portCreator = "Create table Port (" +
							"portName varchar(100) not null, " +
							"tripID int unsigned not null, " + 
							"arrivalDate date, departureDate date, " + 
							"primary key(portName, tripID), " + 
							"foreign key(tripID) references Trip(tripID))";
			String ticketCreator = "Create table Ticket (" + 
							"ticketID int unsigned not null primary key, " + 
							"tripID int unsigned not null, " + 
							"customerName varchar(255) not null, " + 
							"mealPackageFlag int unsigned not null, " + 
							"drinkPackageFlag int unsigned not null, " + 
							"roomNumber int unsigned not null, " + 
							"foreign key(tripID) references Trip(tripID))";
			String roomCreator = "Create table RoomInfo (" + 
							"roomType varchar(100) not null, " + 
							"tripID int unsigned not null, " + 
							"fees double unsigned not null, " + 
							"occupancy int unsigned not null, " + 
							"primary key(roomType, tripID), " + 
							"foreign key(tripID) references Trip(tripID))";
			String[] createStatements = {shipCreator, tripCreator, 
										portCreator, ticketCreator, roomCreator};
			String[] tableNames = {"CruiseShip", "Trip", 
										"Port", "Ticket", "RoomInfo"};
			Statement stmt = connector.createStatement();
			DatabaseMetaData dbm = connector.getMetaData();
			for(int i = 0; i < createStatements.length; i++) {
				ResultSet tables = dbm.getTables(null, null, tableNames[i], null);
				if(!tables.next()) {
					stmt.executeUpdate(createStatements[i]);
				}
			}
		} catch (SQLException sqle) {
			throw new IllegalArgumentException(sqle.getMessage());
		}
	}

	/**
	 * Creates a ship object and enters its data into the database.
	 * @param roomCounts The numbers of room of each type
	 * @return A ship object with the ID and specified room counts*/
	public Ship createShip(Map<RoomType,Integer> roomCounts) throws IllegalArgumentException {
		String retrieveID = "select shipID from CruiseShip";
		long id = 1;
		try {
			PreparedStatement retrieveStatement = 
								connector.prepareStatement(retrieveID);
			ResultSet idSet = retrieveStatement.executeQuery();
			while(idSet.next()) {
				id = idSet.getInt("shipID") + 1;
			}
		} catch(SQLException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		
		try {
			String insert = "insert into CruiseShip values (?,?,?,?,?)";
			PreparedStatement insertStatement = connector.prepareStatement(insert);
			insertStatement.setLong(1, id);
			insertStatement.setInt(2, roomCounts.get(RoomType.INTERIOR));
			insertStatement.setInt(3, roomCounts.get(RoomType.OUTSIDE));
			insertStatement.setInt(4, roomCounts.get(RoomType.BALCONY));
			insertStatement.setInt(5, roomCounts.get(RoomType.SUITE));
			insertStatement.executeUpdate();
		} catch (SQLException sqle) {
			throw new IllegalArgumentException("Invalid inputs for createShip()");
		}
		return new Ship(id, roomCounts);
	}
	
	/**
	 * Queries the CruiseShip table to retrieve the list of ships in the database
	 * @return A list that contains data of all ships in the database.
	 */
	public List<Ship> queryShip() throws IllegalArgumentException {
		List<Ship> shipList = new ArrayList<Ship>();
		String queryStatement = "select * from CruiseShip";
		try {	
			PreparedStatement retrieveStatement = 
								connector.prepareStatement(queryStatement);
			ResultSet shipSet = retrieveStatement.executeQuery();			
			while(shipSet.next()) {
				EnumMap<RoomType,Integer> roomCounts = 
									new EnumMap<RoomType,Integer>(RoomType.class);
				int id = shipSet.getInt("shipID");
				roomCounts.put(RoomType.INTERIOR, shipSet.getInt("interiorRooms"));
				roomCounts.put(RoomType.OUTSIDE, shipSet.getInt("outsideRooms"));
				roomCounts.put(RoomType.BALCONY, shipSet.getInt("balconyRooms"));
				roomCounts.put(RoomType.SUITE, shipSet.getInt("suites"));
				shipList.add(new Ship(id, roomCounts));
			}
		} catch(SQLException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		return shipList;
	}

	/**
	 * Creates a trip object, and enters data into Trip, Port and RoomInfo.
	 * @param temp A TripBuilder that contains a Ship object for the Trip.
	 * @return A trip object containing data from the tripBuilder.
	 */
	public Trip createTrip(TripBuilder temp) {
		long tripID = 1;
		String retrieveID = "select MAX(tripID) as maxTripID from Trip";
		try {
			PreparedStatement retrieveStatement = 
								connector.prepareStatement(retrieveID);
			ResultSet idSet = retrieveStatement.executeQuery();
			if(idSet.next()) {
				if(idSet.getInt("maxTripID") != 0){
					tripID = idSet.getInt("maxTripID") + 1;
				}
			}
		} catch(SQLException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		Trip toReturn = temp.build(tripID);
		try {
			String insert = "insert into Trip values (?,?,?,?)";
			PreparedStatement insertStatement = 
								connector.prepareStatement(insert);
			insertStatement.setLong(1, tripID);
			insertStatement.setLong(2, toReturn.SHIP.ID);
			insertStatement.setDouble(3, toReturn.COSTS.get(Service.DRINKS));
			insertStatement.setDouble(4, toReturn.COSTS.get(Service.MEALS));
			insertStatement.executeUpdate();
			
			for(int i = 0; i < toReturn.PORTS.size(); i++) {
				insert = "insert into Port values (?,?,?,?)";
				insertStatement = connector.prepareStatement(insert);
				insertStatement.setString(1, toReturn.PORTS.get(i).location);
				insertStatement.setLong(2, tripID);
				insertStatement.setDate(3, new java.sql.Date(
									toReturn.PORTS.get(i).arrival.getTime()));
				insertStatement.setDate(4, new java.sql.Date(
									toReturn.PORTS.get(i).departure.getTime()));
				insertStatement.executeUpdate();
			}

			for(RoomType currentType : RoomType.values()){
				insert = "insert into RoomInfo values (?,?,?,?)";
				insertStatement = connector.prepareStatement(insert);
				insertStatement.setString(1, currentType.name());
				insertStatement.setLong(2, tripID);
				insertStatement.setDouble(3, toReturn.COSTS.get(currentType));
				insertStatement.setInt(4, toReturn.SHIP.getTotalOccupancy(currentType));
				insertStatement.executeUpdate();
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		return toReturn;
	}
	
	/**
	 * Queries the CruiseShip, Trip, Port and RoomInfo tables to retrieve data to
	 * retrieve data for all Trips in the database.
	 * @return A list of all Trips in the database.
	 */
	public List<Trip> queryTrip(){
		List<Trip> tripList = new ArrayList<Trip>();
		long tripID;
		try {
			String retrieveTripDetails = "select * from Trip";	
			PreparedStatement tripStatement = 
								connector.prepareStatement(retrieveTripDetails);
			ResultSet tripSet = tripStatement.executeQuery();
			while(tripSet.next()) {
				tripID = tripSet.getLong("tripID");
				long shipId = tripSet.getLong("shipID");
				double drinkFee = tripSet.getDouble("drinkFees");
				double mealFee = tripSet.getDouble("mealFees");

				String retrieveShipDetails = "select CruiseShip.*, TripID from " + 
										"CruiseShip natural join Trip " + 
										"where TripID = " + tripID;
				PreparedStatement shipStatement = 
										connector.prepareStatement(retrieveShipDetails);
				ResultSet shipSet = shipStatement.executeQuery();
				EnumMap<RoomType,Integer> roomCounts = 
										new EnumMap<RoomType,Integer>(RoomType.class);
				while(shipSet.next()){
					roomCounts.put(RoomType.INTERIOR, shipSet.getInt("interiorRooms"));
					roomCounts.put(RoomType.OUTSIDE, shipSet.getInt("outsideRooms"));
					roomCounts.put(RoomType.BALCONY, shipSet.getInt("balconyRooms"));
					roomCounts.put(RoomType.SUITE, shipSet.getInt("suites"));
				}

				TripBuilder builder = new TripBuilder(new Ship(shipId, roomCounts));
				String retrieveRoomDetails = "select * from RoomInfo " + 
										"where TripID = " + tripID;
				PreparedStatement roomStatement = 
										connector.prepareStatement(retrieveRoomDetails);
				ResultSet roomSet = roomStatement.executeQuery();
				while(roomSet.next()){
					builder.addCost(RoomType.valueOf(roomSet.getString("RoomType")),
									roomSet.getInt("fees"));
				}

				String retrievePortDetails = "select * from Port where TripID = " +
										tripID + " order by departureDate";
				PreparedStatement portStatement = 
										connector.prepareStatement(retrievePortDetails);
				ResultSet portSet = portStatement.executeQuery();
				while(portSet.next()){
					builder.addPort(new java.util.Date(
								portSet.getDate("arrivalDate").getTime()),
								new java.util.Date(
								portSet.getDate("departureDate").getTime()), 
								portSet.getString("portName"), 
								portSet.getString("portName"));
				}
				builder.addCost(Service.MEALS, mealFee);
				builder.addCost(Service.DRINKS, drinkFee);
				
				tripList.add(builder.build(tripID));
			}
		} catch(SQLException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		return tripList;
	}

	/**
	 * Utilised for booking a trip, enters booking data into the Ticket table 
	 * and returns a ticket.
	 * @param tripIn The trip to be booked
	 * @param customerName The name of the customer
	 * @param mealSelect Whether or not a meal package is selected
	 * @param drinkSelect Whether or not a drink package is selected
	 * @param roomSelect The type of room that is selected
	 * @return A text output of the ticket details
	 */
	public String bookTrip(Trip tripIn, String customerName, boolean mealSelect, 
						boolean drinkSelect, RoomType roomSelect){
		String toReturn;
		long ticketID = 1;
		int roomNumber = tripIn.addPerson(roomSelect);
		String retrieveID = "select MAX(TicketID) as maxTicketID from Ticket";
		try {
			PreparedStatement retrieveStatement = 
											connector.prepareStatement(retrieveID);
			ResultSet idSet = retrieveStatement.executeQuery();
			if(idSet.next()) {
				if(idSet.getInt("maxTicketID") != 0) {
					ticketID = idSet.getInt("maxTicketID") + 1;
				}
			}
		} catch(SQLException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		try{
			Statement updateStatment = connector.createStatement();
			String updateOccupancy = "update RoomInfo set occupancy = occupancy + 1 " +
								"where roomType ='" + roomSelect.name() + 
								"' AND tripID = " + tripIn.ID; 
			updateStatment.executeUpdate(updateOccupancy);
			String createTicket = "insert into Ticket values(?, ?, ?, ?, ?, ?)";
			PreparedStatement insertStatement = 
										connector.prepareStatement(createTicket);
			insertStatement.setLong(1, ticketID);
			insertStatement.setLong(2, tripIn.ID);
			insertStatement.setString(3, customerName);
			
			if(mealSelect) 
				insertStatement.setInt(4, 1);
			else
				insertStatement.setInt(4, 0);
			
			if(drinkSelect) 
				insertStatement.setInt(5, 1);
			else
				insertStatement.setInt(5, 0);
			
			insertStatement.setInt(6, roomNumber);
			toReturn = "Ticket ID: " + ticketID + "\nTrip ID: " + tripIn.ID +
						"\nRoom Number: " + roomNumber +
						"\nCustomer Name: " + customerName + 
						"\nMeal Package Selected: " + ((mealSelect) ? "Yes" : "No") +
						"\nDrink Package Selected: " + ((drinkSelect) ? "Yes" : "No");
			insertStatement.executeUpdate();
		}catch(SQLException e){
			throw new IllegalArgumentException(e.getMessage());
		}
		return toReturn;
	}

	/**
	 * Provides a string of occupancy per trip 
	 * @return a string showing total occupancy for every trip.
	 */
	public String tripOccupancy(){
		String outString = "";
		List<Trip> tripList = this.queryTrip();
		String retrieveStats = "select SUM(occupancy), TripID from RoomInfo where TripID = ";
		try{
			for(int i = 0; i<tripList.size(); i++){
				PreparedStatement query = connector.prepareStatement(retrieveStats + 
									tripList.get(i).ID);
				ResultSet statSet = query.executeQuery();
				while(statSet.next()){
					outString += "\nTrip ID: " + statSet.getString("tripID") + 
						"\nTotal occupancy: " + statSet.getInt("SUM(occupancy)") + "\n";
				}
			}
		}catch(SQLException e){
			throw new IllegalArgumentException(e.getMessage());
		}
		return outString;
	}
}