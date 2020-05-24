import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BotMain extends ListenerAdapter {

    private Map<String, Integer> messageAmount = new HashMap<>();
    private Map<String, Long> lastTimeMilis = new HashMap<>();
    private Set<String> spamChannel = new HashSet<>();
    private int messageMax = 60;
    private boolean enabled = true;


    public static void main(String [] args) throws LoginException, IOException {

        BufferedReader br = new BufferedReader(new FileReader("config.txt"));

        String token = br.readLine();
        JDA api = JDABuilder.createDefault(token).addEventListeners(new BotMain()).setActivity(Activity.playing("Type !sp help for commands!")).build();

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        int previousSlowmode = event.getTextChannel().getSlowmode();

        if(enabled) {

            if (!lastTimeMilis.containsKey(event.getTextChannel().getId())) {

                lastTimeMilis.put(event.getTextChannel().getId(), System.currentTimeMillis());

            } else {

                if (System.currentTimeMillis() - lastTimeMilis.get(event.getTextChannel().getId()) >= 60000) {

                    lastTimeMilis.replace(event.getTextChannel().getId(), System.currentTimeMillis());

                    if (messageAmount.containsKey(event.getTextChannel().getId())) {

                        messageAmount.replace(event.getTextChannel().getId(), 0);

                    }

                }

            }

            if (messageAmount.containsKey(event.getTextChannel().getId())) {

                messageAmount.replace(event.getTextChannel().getId(), messageAmount.get(event.getTextChannel().getId()) + 1);

                if (messageAmount.get(event.getTextChannel().getId()) > messageMax && !spamChannel.contains(event.getTextChannel().getId())) {

                    event.getTextChannel().sendMessage("Slow down buddy! Slowmode will be enabled for 1 minute!").queue();
                    event.getTextChannel().getManager().setSlowmode(Math.min(TextChannel.MAX_SLOWMODE, Math.max(5, previousSlowmode*2))).queue();

                    messageAmount.replace(event.getTextChannel().getId(), 0);

                    try{

                        Thread.sleep(60000);

                    }catch (InterruptedException e){

                        e.printStackTrace();

                    }

                    event.getTextChannel().getManager().setSlowmode(previousSlowmode).queue();
                    event.getTextChannel().sendMessage("Please try to send messages at a speed which people can read them. Thank you!").queue();

                }

            }else{

                messageAmount.put(event.getTextChannel().getId(), 1);

            }

        }else{

            event.getTextChannel().getManager().setSlowmode(previousSlowmode);

        }

        if(event.getMessage().getContentRaw().length() > 4 && event.getMessage().getContentRaw().substring(0, 3).equals("!sp") && !event.getAuthor().isBot() && event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_CHANNEL)){

            String[] command = event.getMessage().getContentRaw().substring(4).split(" ");

            if(command.length == 1){

                if(command[0].toLowerCase().equals("help")){

                    event.getTextChannel().sendMessage("```\nsetrate <a number>: sets the maximum number of messages in a minute allowed (5 to 99)\nsetspam: sets channel as spam channel (which disables the slowmoding)\nbotdisable: disables the slowmoding for all (keeps other settings such as spam channels)\nbotenable: enables the slowmoding\n```").queue();

                }

                if(command[0].toLowerCase().equals("botdisable")){

                    enabled = false;

                }

                if(command[0].toLowerCase().equals("botenable")){

                    enabled = true;

                }

                if(command[0].toLowerCase().equals("setspam")){

                    if(spamChannel.contains(event.getTextChannel().getId())){

                        event.getTextChannel().sendMessage("This channel is no longer marked a spam channel.").queue();
                        spamChannel.remove(event.getTextChannel().getId());

                    }else{

                        event.getTextChannel().sendMessage("This channel was marked as a spam channel. Slowmoding is disabled.").queue();
                        spamChannel.add(event.getTextChannel().getId());

                    }

                }

            }else if(command.length == 2){

                if(command[0].toLowerCase().equals("setrate")){

                    if(command[1].length() > 2) command[1] = command[1].substring(0, 2);

                    String validChar = "0123456789";

                    if(command[1].length() == 1 || validChar.contains(command[1].charAt(0) + "") && validChar.contains(command[1].charAt(1) + "")){

                        int newRate = Integer.parseInt(command[1]);

                        messageMax = Math.max(newRate, 5);

                        event.getTextChannel().sendMessage("Max rate has changed to " + messageMax + " messages per minute.").queue();

                    }

                }

            }

        }

    }

}
