package vendor.modules;

import vendor.Framework;
import vendor.abstracts.AbstractTerminalConsole;
import vendor.abstracts.Module;
import vendor.exceptions.BadFileContentException;
import vendor.interfaces.Console;
import vendor.ui.NotificationUI;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Environment extends Module {
	
	private static final String ENV_FILE_NAME = ".env";
	private static final String ENV_EXAMPLE_FILE_NAME = "example.env";
	
	private static HashMap<String, String> envVars;
	
	private final String WARNINGS = "WARNINGS";
	private final String SPACED_KEY_ERRORS = "SPACED_KEY_ERRORS";
	private final String LINE_ERRORS = "LINE_ERRORS";
	
	public void build() throws Exception{
		
		try{
			
			BufferedReader reader = getEnvFile();
			
			envVars = new HashMap<>();
			
			String line;
			
			for(int i = 1; (line = reader.readLine()) != null; i++){
				
				// Remove comment from line if there's content in the line
				if(line.length() != 0){
					Matcher matcher = Pattern.compile("(\\s)*#.*")
							.matcher(line);
					int index = matcher.find() ? matcher.start() : -1;
					
					if(index != -1)
						line = line.substring(0, index);
				}
				
				if(line.length() != 0){
					
					String[] keyValue = line.trim().split("=", 2);
					
					if(keyValue.length != 2){
						
						addError(LINE_ERRORS, "Line " + i + ": \"" + line
								+ "\"");
						
					}
					else{
						
						if(keyValue[1].length() == 0){
							addWarning(WARNINGS, "The variable keyed \""
									+ keyValue[0] + "\" at line " + i
									+ " is empty, is that supposed to be..?");
						}
						
						if(keyValue[0].contains(" ")){
							
							addError(SPACED_KEY_ERRORS, "Line " + i + ": \""
									+ line + "\"");
							
						}
						
					}
					
					if(!hasErrors()){
						envVars.put(keyValue[0].toLowerCase(), keyValue[1]);
					}
					
				}
				
			}
			
			reader.close();
			
			handleIssues();
			
		}
		catch(IOException e){
			throw new FileNotFoundException(
					"An unexpected problem happened while reading the line of the file.");
		}
		
	}
	
	public static <EnvVar> EnvVar getVar(String key){
		return getVar(key, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <EnvVar> EnvVar getVar(String key, EnvVar defaultValue){
		if(envVars == null){
			
			if(Framework.isDebugging())
				Logger.log("A call to get a variable environment has been used but the Environment is not yet set! Make sure you have built the Framework or call Environment.build() manually! Using the defaultObject provided in the meantime.",
						Logger.LogType.WARNING);
			
			return defaultValue;
			
		}
		
		String value = envVars.get(key.toLowerCase());
		
		if(value == null || value.equals("")){
			return defaultValue;
		}
		
		if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")){
			return (EnvVar)Boolean.valueOf(value);
		}
		else if(value.matches("^[0-9]+$")){
			
			try{
				Integer number = Integer.valueOf(value);
				
				return (EnvVar)number;
			}
			catch(NumberFormatException e){}
			
		}
		else if(value.matches("^[0-9]+(\\.[0-9]+)?$")){
			
			try{
				Double number = Double.valueOf(value);
				
				return (EnvVar)number;
			}
			catch(NumberFormatException e){}
			
		}
		
		return (EnvVar)(String)value;
	}
	
	public static boolean hasVar(String key){
		return envVars.containsKey(key.toLowerCase());
	}
	
	@Override
	protected void handleIssuesLogic() throws Exception{
		
		StringBuilder builder = new StringBuilder();
		
		if(!hasErrors()){
			
			builder.append("WARNINGS!");
			
			ArrayList<String> warnings = getWarnings(WARNINGS);
			
			if(warnings != null){
				
				builder.append("\n");
				
				for(String warning : warnings){
					builder.append("\n").append(warning);
				}
				
			}
			
			System.err.println(builder.append("\n").toString());
			
		}
		else{
			
			builder.append("ERRORS!");
			
			ArrayList<String> spaceErrors = getErrors(SPACED_KEY_ERRORS);
			
			if(spaceErrors != null){
				
				builder.append("\n\nSome keys ("
						+ spaceErrors.size()
						+ ") has space(s) in them : Keys must NOT contain spaces.\n");
				
				for(String error : spaceErrors){
					builder.append("\n").append(error);
				}
				
			}
			
			ArrayList<String> lineErrors = getErrors(LINE_ERRORS);
			
			if(lineErrors != null){
				
				builder.append("\n\nSome variables ("
						+ lineErrors.size()
						+ ") are not well formatted. Please follow the \"KEY=value\" format.\n");
				
				for(String error : lineErrors){
					builder.append("\n").append(error);
				}
				
			}
			
			throw new BadFileContentException(builder.toString());
			
		}
		
	}
	
	private BufferedReader getEnvFile(){
		
		InputStream inputStream;
		
		try{
			
			String systemEnvFilePath = Framework.runnableSystemPath()
					+ ENV_FILE_NAME;
			
			inputStream = new FileInputStream(new File(systemEnvFilePath));
			
		}
		catch(FileNotFoundException e){
			
			inputStream = Framework.class.getResourceAsStream("/"
					+ ENV_FILE_NAME);
			
			if(inputStream == null){
				
				Console console;
				
				if(Framework.isRunningFromTerminal()){
					
					console = new AbstractTerminalConsole(){
						@Override
						public void initialize(){
							Logger.setOutputs(this);
						}
					};
					
				}
				else{
					
					console = new NotificationUI();
					
				}
				
				try{
					console.initialize();
					
					confirmEnvFileCreation(console);
				}
				catch(Exception nothing){}
				finally{
					System.exit(0);
				}
				
			}
			
		}
		
		InputStreamReader streamReader = new InputStreamReader(inputStream,
				StandardCharsets.UTF_8);
		
		BufferedReader reader = new BufferedReader(streamReader);
		
		return reader;
		
	}
	
	private void confirmEnvFileCreation(Console console){
		
		int choice = console
				.getConfirmation(
						"No environment file has been detected, do you want to create one now?",
						Console.QuestionType.YES_NO);
		
		switch(choice){
		case Console.YES:
			
			try{
				
				String envFilePath = buildSystemEnvFile();
				
				Logger.log(
						"Please go fill the environment file with your own informations and start this program again!"
								+ "\n"
								+ "Path of the file created : \""
								+ envFilePath + "\"", Logger.LogType.INFO,
						false);
				
			}
			catch(IOException e){
				Logger.log(e);
			}
			
			break;
		case Console.NO:
			Logger.log("No environment added, bot stopping.",
					Logger.LogType.INFO, false);
			break;
		}
		
	}
	
	private String buildSystemEnvFile() throws IOException{
		
		InputStream exampleFileStream = Framework.class.getResourceAsStream("/"
				+ ENV_EXAMPLE_FILE_NAME);
		
		byte[] buffer = new byte[exampleFileStream.available()];
		exampleFileStream.read(buffer);
		
		String systemEnvFilePath = Framework.runnableSystemPath()
				+ ENV_FILE_NAME;
		
		File targetFile = new File(systemEnvFilePath);
		OutputStream outStream = new FileOutputStream(targetFile);
		outStream.write(buffer);
		
		outStream.close();
		
		return systemEnvFilePath;
		
	}
	
}
