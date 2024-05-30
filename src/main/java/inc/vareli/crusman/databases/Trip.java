package inc.vareli.crusman.databases;

import inc.vareli.crusman.databases.Ship.RoomType;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.time.Duration;
import java.util.TimeZone;
import java.text.NumberFormat;

/**
 * Represents a Trip a user can buy a ticket to go on etc.
 * 
 * @author Sebastien Peterson-Boudreau sebastien.peterson.boudreau@unb.ca
 */
public class Trip {
	
	/**
	 * A unique identifier for the Trip.
	 * Protected so CMConnection can write it to the DB
	 */
	protected final long ID;

	/**
	 * The ship that this trip ison.
	 * Protected so that CMConnection ca write to the DB
	 */
	protected final Ship SHIP;

	/**
	 * A list of ports that the trip will go on.
	 * Protected so CMConnection can write to the DB
	 */
	protected final List<Port> PORTS;

	/**
	 * A mapping of different costs to how much they cost for this particular
	 * trip.
	 * Protected so CMConnection can write them to the DB.
	 */
	protected final Map<CostType,Double> COSTS;

	private Trip(long ID, Ship ship, List<Port> ports, Map<CostType,Double> costs) {
		this.ID = ID;
		this.SHIP = ship;
		this.PORTS = ports;
		this.COSTS = costs;
	}

	/**
	 * Adds a person to a room of this type.
	 * The maximum capacity of the room depends on the duration of the trip.
	 * @param type - the type of the room
	 * @returns if there was space for them or not
	 */
	public int addPerson(RoomType type) {
		long days = this.getDuration();
		return SHIP.addPerson(type, (days <= 2) ? 5 : 4);
	}

	/**
	 * gets the duration of this trip
	 * @return a long value representing the number of days long this trip is
	 */
	public long getDuration() {
		int z = PORTS.size()-1;
		return Duration.between(
						PORTS.get(0).departure.toInstant(),
						PORTS.get(z).arrival.toInstant()
					).toDays();
	}

	public double getTotalFees() {
		double total = 0;
		for (CostType cost : COSTS.keySet()) {
			total += COSTS.get(cost);
		}
		return total;
	}

	@Override
	public String toString() {
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		StringBuilder builder = new StringBuilder();

		builder.append(this.SHIP.toString());
		for (CostType cost : COSTS.keySet()) {
			builder.append(cost.toString() + ": " + nf.format(COSTS.get(cost)) + "\n");
		}
		builder.append("TOTAL FEES: " + nf.format(this.getTotalFees()) + "\n");

		int z = this.PORTS.size()-1;
		builder.append(this.PORTS.get(0).location + ", " + this.PORTS.get(0).departure + "\n");
		builder.append(this.PORTS.get(z).location + ", " + this.PORTS.get(z).arrival + "\n");
		return builder.toString();
	}

	/*
	 * Solution to the timezone issue: the timezone is stored with the port
	 * and will be interpreted at the display level (GUI stuff) to show the
	 * correct timezone for the situation.
	 * Calculations and stuff use UTC-0 (unix time)
	 */
	protected static class Port { 
		public String location;
		public TimeZone zone;
		public Date arrival;
		public Date departure;

		public Port(Date arrival, Date departure, String location, String zoneId) {
			this.arrival = arrival;
			this.departure = departure;
			this.location = location;
			this.zone = TimeZone.getTimeZone(zoneId);
		}
	}

	/**
	 * An enum of the different services you must pay for on a trip
	 */
	public enum Service implements CostType {
		MEALS("Meals"),
		DRINKS("Drinks");

		private String name;

		private Service(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * A class to build a Trip because they are too complex to construct with
	 * a simple constructor
	 */
	public static class TripBuilder {
		private Ship ship;
		private List<Port> ports;
		private Map<CostType,Double> costs;

		/**
		 * Constructor. Initialized ID and ship as these are non-null values
		 */
		public TripBuilder(Ship ship) {
			this.ship = ship;
			this.ports = new ArrayList<Port>();
			this.costs = new HashMap<CostType,Double>();
		}

		/**
		 * adds a port to the list of ports in this trip
		 * @param arrival - the date and time of arrival
		 * @param departure - the date and time of arrival for this port
		 * @param locaion - a String containing the name of the location this port is at
		 * @param zoneId - the timezone identifier for this location's timezone. Must
		 * be in correct format according to java.util.TimeZone and java.util.ZoneId
		 * @return this TripBuilder object, allowing chaining
		 * @see <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TimeZone.html">java.util.TimeZone</a>
		 * @see <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ZoneId.html">java.util.ZoneId</a>
		 */
		public TripBuilder addPort(Date arrival, Date departure, String location, String zoneId) {
			ports.add(new Port(arrival, departure, location,zoneId));
			return this;
		}

		/**
		 * adds a cost to this trip, associating a CostType with the amount it costs.
		 * A cost type can be a type of room or a type of service.
		 * @param type - the type of cost, what room type or what service it is
		 * @param amount - how much this cost is
		 * @return this TripBuilder object, allowing chaining
		 */
		public TripBuilder addCost(CostType type, double amount) {
			costs.put(type, amount);//should we check for overwriting?
			return this;
		}

		/**
		 * builds the Trip that this TripBuilder specifies
		 * Protected so that only CMConnection can create Trips, ensuring that
		 * everything is written to the DB
		 * @return the Trip built
		 */
		protected Trip build(long ID) {
			return new Trip(ID, ship, ports, costs);
		}
	}
}
