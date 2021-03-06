package io.github.vhoyon.bot.app;

import io.github.ved.jrequester.Request;
import io.github.vhoyon.bot.consoles.TerminalConsole;
import io.github.vhoyon.bot.consoles.UIConsole;
import io.github.vhoyon.vramework.Vramework;
import io.github.vhoyon.vramework.interfaces.Console;
import io.github.vhoyon.vramework.modules.Audit;
import io.github.vhoyon.vramework.modules.Environment;
import io.github.vhoyon.vramework.modules.Logger;
import io.github.vhoyon.vramework.objects.AuditableFile;
import io.github.vhoyon.vramework.util.VrameworkTemplate;

/**
 * The main class for running Vhoyon's bot.
 * 
 * @version 1.0
 * @since 0.1.0
 * @author Stephano Mehawej
 */
public class Main {
	
	public static void main(String[] args){
		
		try{
			
			Request programRequest = new Request(args);
			
			programRequest.setOptionLink("d", "debug");
			programRequest.setOptionLink("i", "instant");
			programRequest.setOptionLink("t", "terminal");
			
			if(programRequest.hasError()){
				System.out.println(programRequest.getDefaultErrorMessage());
			}
			
			Vramework.build(programRequest.hasOption("d"));
			
			Audit.setOutputs(new AuditableFile("audit.txt", Vramework
					.runnableSystemPath()));
			
			Console console;
			
			if(programRequest.hasOption("t")){
				
				console = new TerminalConsole(){
					@Override
					public void onStart() throws Exception{
						Environment.refresh();
						
						VrameworkTemplate.botToken = Environment
								.getVar("BOT_TOKEN");
						
						VrameworkTemplate.startBot(this, new MessageListener());
					}
					
					@Override
					public void onStop() throws Exception{
						VrameworkTemplate.stopBot(this);
					}
					
					@Override
					public void onInitialized(){
						logLink();
					}
				};
				
			}
			else{
				
				console = new UIConsole(){
					@Override
					public void onStart() throws Exception{
						Environment.refresh();
						
						VrameworkTemplate.botToken = Environment
								.getVar("BOT_TOKEN");
						
						VrameworkTemplate.startBot(this, new MessageListener());
					}
					
					@Override
					public void onStop() throws Exception{
						VrameworkTemplate.stopBot(this);
					}
					
					@Override
					public void onInitialized(){
						logLink();
					}
				};
				
			}
			
			Logger.setSeparator(null);
			
			boolean startImmediately = programRequest.hasOption("i");
			
			// CAREFUL : This call blocks the main thread!
			console.initialize(startImmediately);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Logs a link to the linked loggers that makes this bot join a server of
	 * choice with the condition that the {@code CLIENT_ID} environment variable
	 * is not empty.
	 *
	 * @since v0.4.0
	 */
	private static void logLink(){
		
		String clientId = Environment.getVar("CLIENT_ID", null);
		
		if(clientId != null){
			Logger.log("Link to join the bot to a server :\n\n"
					+ "https://discordapp.com/oauth2/authorize?client_id="
					+ clientId + "&scope=bot&permissions=0", false);
		}
		
	}
	
}
