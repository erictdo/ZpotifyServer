package main.java.core;

import main.java.dfs.DFSCommand;
import main.java.model.Music;
import main.java.model.User;
import main.java.utils.Deserializer;

import java.util.List;

public class Server {

  public static Deserializer d;
  public static List<Music> musicList;
  public static List<User> userList;

  public static void main(String args[]) {
    try {
      System.out.println("Initializing Server...");
      System.out.println("~~~");
//      System.out.println("Starting DFSCommand");
//      try {
//        new DFSCommand();
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//      System.out.println("~~~");

      System.out.println("Starting Server Communication Protocol");
      ServerCommunicationProtocol scp = new ServerCommunicationProtocol();
      scp.start();
      System.out.println("Ending Server Communication Protocol");

    } catch (Exception e) {
      System.out.println(e);
    }

  }
}