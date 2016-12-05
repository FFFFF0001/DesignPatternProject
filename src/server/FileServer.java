package server;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class FileServer extends Thread {
  public static int port = -1;
  private static FileServer instance = null;
  public static FileServer getFileServer() throws IOException {
	  if(instance == null) {
		  if(port == -1)port = 8081;
		  instance = new FileServer(port);
	  }
	  return instance;
  }
  private List<ClientThread> clients = new ArrayList<ClientThread>();
  private ServerSocket server;
  private FileServer(int port) throws IOException {
    server = new ServerSocket(port);
    System.out.println("FileServer start at 127.0.0.1:" + port);
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
    private DataInputStream getter;
    private DataOutputStream sender;
    private String username;

    public DataOutputStream getSender() { return sender; }
    public int getPort() { return sock.getPort(); }
    public String getIP() { return sock.getInetAddress().getHostAddress(); }
    public String getUsername() { return username; }
    
    public ClientThread(Socket sock) {
      try {
        this.sock = sock;
        this.getter = new DataInputStream(sock.getInputStream());
        this.sender = new DataOutputStream(sock.getOutputStream());
        this.username = getter.readUTF();
        System.out.println("FileServer: [ONLINE] [" + username + "] [" + getIP() + ":" + getPort() + "]");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Ⱥ���ļ�����
    private void sendFileToAllUser(byte[] buff, int length) {
      try {
        for (int i = 0; i < clients.size(); ++i) {
          if (clients.get(i).getUsername().equals(this.username))
            continue;
          clients.get(i).getSender().write(buff, 0, length);
          clients.get(i).getSender().flush();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Ⱥ���ļ�������Ϣ���ļ������ļ�����
    private void sendBasicInfoToAllUser(String fileInfo, long fileLength) {
      try {
        for (int i = 0; i < clients.size(); ++i) {
          if (clients.get(i).getUsername().equals(this.username))
            continue;
          clients.get(i).getSender().writeUTF(fileInfo);
          clients.get(i).getSender().flush();
          clients.get(i).getSender().writeLong(fileLength);
          clients.get(i).getSender().flush();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // �����ļ����ݸ��ض��û�
    private void sendFileToSpecificUser(byte[] buff, int length, String dest) {
      try {
        for (int i = 0; i < clients.size(); ++i) {
          if (clients.get(i).getUsername().equals(dest)) {
            clients.get(i).getSender().write(buff, 0, length);
            clients.get(i).getSender().flush();
            break;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // �����ļ�������Ϣ���ض��û�
    private void sendBasicInfoToSpecificUser(String fileInfo, long fileLength, String dest) {
      try {
        for (int i = 0; i < clients.size(); ++i) {
          if (clients.get(i).getUsername().equals(dest)) {
            clients.get(i).getSender().writeUTF(fileInfo);
            clients.get(i).getSender().flush();
            clients.get(i).getSender().writeLong(fileLength);
            clients.get(i).getSender().flush();
            break;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void run() {
      byte[] buff = new byte[1024];
      while (true) {
        try {
          String message = getter.readUTF();
          if (message.equals("[OFFLINE]")) {
            offline();
            return;
          }
          StringTokenizer stringTokenizer = new StringTokenizer(message, "[#]");
          String command = stringTokenizer.nextToken();
          String fileName = stringTokenizer.nextToken();
          String dest = null;
          boolean toAll = false;
          long fileLength = getter.readLong();
          if (command.equals("GROUP")) {// Ⱥ���ļ�
            sendBasicInfoToAllUser("GROUP[#]" + fileName + "[#]" + username, fileLength);
            toAll = true;
          } else if (command.equals("P2P")) {// ˽���ļ�
            dest = stringTokenizer.nextToken();
            sendBasicInfoToSpecificUser("P2P[#]" + fileName + "[#]" + username, fileLength, dest);
          }
          int length = 0, total = 0;
          while (total < fileLength) {
            length = getter.read(buff);
            total += length;
            if (toAll)
              sendFileToAllUser(buff, length);
            else
              sendFileToSpecificUser(buff, length, dest);
          }
        } catch (IOException e) {
          offline();
          return;
        }
      }
    }

    // �û����ߣ��ر�socket��Դ���Ƴ����߳�
    private void offline() {
      try {
        System.out.println("FileServer: [OFFLINE] [" + username + "] [" + getIP() + ":" + getPort() + "]");
        getter.close();
        sender.close();
        sock.close();
        for (int i = 0; i < clients.size(); ++i) {
          if (clients.get(i).getUsername().equals(this.username)) {
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
