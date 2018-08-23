package vendor.utilities.sanitizers;

public interface TextSanitizer {
	
	static String sanitizeValue(Object value){
		
		if(value == null)
			return "";
		
		return value.toString();
		
	}
	
}
