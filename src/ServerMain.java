import server.ChatServer;
import server.FileServer;
import java.io.IOException;

public class ServerMain {
  public static void main(String args[]) {
    if (args.length != 1) {
      //System.out.println("���������в����и����˿ںţ�java Server [port]");
      //return;
    }
    try {
      //int port = Integer.parseInt(args[0]);
      int port = 8080;
      ChatServer cs = new ChatServer(port);
      FileServer fs = new FileServer(port + 1);
      cs.start();
      fs.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
