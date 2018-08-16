package vendor.objects;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import vendor.interfaces.Utils;

public class MessageEventDigger {
	
	private MessageReceivedEvent event;
	
	public MessageEventDigger(MessageReceivedEvent event){
		this.event = event;
	}
	
	public MessageReceivedEvent getEvent(){
		return this.event;
	}
	
	public JDA getJDA(){
		return this.getEvent().getJDA();
	}
	
	public String getGuildKey(){
		return Utils.buildKey(getGuildId());
	}
	
	public String getChannelKey(){
		return Utils.buildKey(getGuildKey(), getChannelId());
	}
	
	public String getUserKey(){
		return Utils.buildKey(getUserName(), getUserId());
	}
	
	public String getCommandKey(String commandName){
		return Utils.buildKey(getChannelKey(), commandName);
	}
	
	public Guild getGuild(){
		return event.getGuild();
	}
	
	public String getGuildId(){
		return getGuild().getId();
	}
	
	public TextChannel getChannel(){
		return event.getTextChannel();
	}
	
	public String getChannelId(){
		return getChannel().getId();
	}
	
	public Member getMember(){
		return event.getMember();
	}
	
	public User getUser(){
		return event.getAuthor();
	}
	
	public String getUserId(){
		return getUser().getId();
	}
	
	public String getUserName(){
		return getUser().getName();
	}
	
	public SelfUser getRunningBot(){
		return this.getJDA().getSelfUser();
	}
	
}
