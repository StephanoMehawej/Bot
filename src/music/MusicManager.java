package music;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.core.entities.Guild;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import framework.Command;

public class MusicManager {
	
	private static MusicManager musicManager;
	
	private final AudioPlayerManager manager = new DefaultAudioPlayerManager();
	private final Map<String, MusicPlayer> players = new HashMap<>();
	
	private MusicManager(){
		AudioSourceManagers.registerRemoteSources(manager);
		AudioSourceManagers.registerLocalSource(manager);
	}
	
	public static MusicManager get(){
		
		if(musicManager == null){
			musicManager = new MusicManager();
		}
		
		return musicManager;
		
	}
	
	public synchronized MusicPlayer getPlayer(Guild guild){
		
		if(!players.containsKey(guild.getId()))
			players.put(guild.getId(), new MusicPlayer(manager.createPlayer(), guild));
		
		return players.get(guild.getId());
		
	}
	
	public void loadTrack(final Command command, final String source){
		
		MusicPlayer player = getPlayer(command.getGuild());
		
		command.getGuild().getAudioManager()
				.setSendingHandler(player.getAudioHandler());
		
		manager.loadItemOrdered(player, source, new AudioLoadResultHandler(){
			
			@Override
			public void trackLoaded(AudioTrack track){
				command.sendMessage(track.getInfo().title + " has been added.");
				
				player.playTrack(track);
			}
			
			@Override
			public void playlistLoaded(AudioPlaylist playlist){
				
				StringBuilder builder = new StringBuilder();
				
				builder.append("Playlist ").append(playlist.getName())
						.append(" has been added \n");
				
				for(int i = 0; i < playlist.getTracks().size(); i++){
					AudioTrack track = playlist.getTracks().get(i);
					
					builder.append("\nAdded track `#" + (i + 1) + "` **->** ").append(track.getInfo().title);
					
					player.playTrack(track);
				}
				
				command.sendMessage(builder.toString());
				
			}
			
			@Override
			public void noMatches(){
				command.sendMessage("The song \"" + source
						+ "\" has not been found.");
			}
			
			@Override
			public void loadFailed(FriendlyException exeption){
				command.sendMessage("Cannot load the song : "
						+ exeption.getMessage());
			}
			
		});
		
	}
	
}