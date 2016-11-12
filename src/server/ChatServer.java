package server;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ChatServer extends Thread {
  private List<ClientThread> clients = new ArrayList<ClientThread>();
  private ServerSocket server;
  public ChatServer(int port) throws IOException {
    server = new ServerSocket(port);
    System.out.println("ChatServer start at 127.0.0.1:" + port);
  }
  // ������������
  public void run() {
    while (true) {
      try {
        ClientThread client = new ClientThread(server.accept());
        client.start();
        clients.add(client);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // Ϊ�ͻ����ṩ������߳���
  private class ClientThread extends Thread {
    private Socket sock;
    private BufferedReader getter;
    private PrintWriter sender;
    private String username;

    public PrintWriter getSender() { return sender; }
    public int getPort() { return sock.getPort(); }
    public String getIP() { return sock.getInetAddress().getHostAddress(); }
    public String getUsername() { return username; }
    public String getIdentifier() { return sock.getInetAddress().getHostAddress() + ":" + sock.getPort(); }

    public ClientThread(Socket sock) {
      try {
        this.sock = sock;
        this.getter = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        this.sender = new PrintWriter(sock.getOutputStream(), true);
        this.username = getter.readLine();
        // ���������û���ϢȺ�������������������û�
        sendToAllUser("ONLINE[#]" + username + "[#]" + getIP() + "[#]" + getPort());
        // ����ǰ�����û���Ϣ���͸��������û�
        for (int i = 0; i < clients.size(); ++i) {
          if (clients.get(i).getIdentifier().equals(this.getIdentifier()))
            continue;
          sender.println("INFO[#]" + clients.get(i).getUsername() + "[#]"
            + clients.get(i).getIP() + "[#]" + clients.get(i).getPort());
        }
        sender.flush();
        System.out.println("ChatServer: [ONLINE] [" + username + "] [" + getIdentifier() + "]");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Ⱥ����Ϣ
    private void sendToAllUser(String message) {
      for (int i = 0; i < clients.size(); ++i) {
        if (clients.get(i).getIdentifier().equals(this.getIdentifier()))
          continue;
        clients.get(i).getSender().println(message);
        clients.get(i).getSender().flush();
      }
    }

    // ˽����Ϣ
    private void sendToSpecificUser(String message, String dest) {
      for (int i = 0; i < clients.size(); ++i) {
        if (clients.get(i).getUsername().equals(dest)) {
          clients.get(i).getSender().println(message);
          clients.get(i).getSender().flush();
        }
      }
    }

    public void run() {
      String line;
      while (true) {
        try {
          line = getter.readLine();
          if (line.equals("[OFFLINE]")) {// �û�����
            offline();
            return;
          } else {// ��ͨ��Ϣ
            System.out.println(line);
            StringTokenizer tokenizer = new StringTokenizer(line, "[#]");
            String command = tokenizer.nextToken();
            String message = tokenizer.nextToken();
            if (command.equals("GROUP")) {// Ⱥ����Ϣ
              sendToAllUser("GROUP[#]" + username + "[#]" + message);
            } else {// ˽����Ϣ
              String dest = tokenizer.nextToken();
              sendToSpecificUser("P2P[#]" + username + "[#]" + message, dest);
            }
          }
        } catch (Exception e) {
          offline();
          return;
        }
      }
    }

    // �û����ߣ�ת�������û����ر�socket��Դ���Ƴ����߳�
    private void offline() {
      try {
        sendToAllUser("OFFLINE[#]" + username + "[#]" + getIP() + "[#]" + getPort());
        System.out.println("ChatServer: [OFFLINE] [" + username + "] [" + getIdentifier() + "]");
        getter.close();
        sender.close();
        sock.close();
        for (int i = 0; i < clients.size(); ++i) {
          if (clients.get(i).getIdentifier().equals(this.getIdentifier())) {
            clients.remove(i);
            return;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
