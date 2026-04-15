package fr.wdes;

import java.util.logging.Level;

public class logger {



public static void warn(String msg,Throwable thrown){
	Launcher.getInstance().LOGGER.log(Level.WARNING, msg, thrown);
}
public static void warn(String msg){
	Launcher.getInstance().LOGGER.log(Level.WARNING, msg);
}
public static void warn(Exception msg){
	Launcher.getInstance().LOGGER.log(Level.WARNING, msg.toString());
}
public static void warn(int msg) {
	Launcher.getInstance().LOGGER.log(Level.WARNING, msg+"");
}
public static void info(String msg,Throwable thrown){
	Launcher.getInstance().LOGGER.log(Level.INFO, msg, thrown);
}
public static void info(String msg){
	Launcher.getInstance().LOGGER.log(Level.INFO, msg);
}

public static void info(Exception msg){

	Launcher.getInstance().LOGGER.log(Level.INFO, msg.toString());
}
public static void info(int msg) {

	Launcher.getInstance().LOGGER.log(Level.INFO, msg+"");

}
}