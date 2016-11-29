package utilities;

import java.text.DecimalFormat;
import java.util.Map;

public class Utilities {
	
	public static double DEVIATION = 0.00000001;
	
	private static DecimalFormat formatter = new DecimalFormat("#.###");
	
	public static String format(double d) {
		return formatter.format(d);
	}
	
	public static <T> String format(Map<T, Double> map) {
		String output = "{";
		String delim = "";
		for(Map.Entry<T, Double> entry : map.entrySet()) {
			output += delim + entry.getKey() + "=" + Utilities.format(entry.getValue());
			delim = ",";
		}
		output += "}";
		return output;
	}
	
}
