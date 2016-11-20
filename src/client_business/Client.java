package client_business;

import java.io.*;
import java.util.StringTokenizer;

import client_aview.ClientView;
import client_dataTransfer.ClientBasic;
import client_dataTransfer.ClientFactory;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class Client extends Thread {
	  private ClientFactory clientFactory = new ClientFactory();
  private ClientBasic cc;
  private ClientBasic fc;
  private ClientView clientView;
  private Map<String, ArrayList<String>> chatRecords = new HashMap<String, ArrayList<String>>();
  private boolean stopClient = false;
  private String username;

  public void setClientViewThread(ClientView cv) { clientView = cv; }

  public void run() {
    while (!stopClient) {
      // ...
    }
  }

  // ΪGUI���Ӱ�ť�ṩ����
  public void connect(String ip, int port, String username) {
    try {
      this.username = username;
      cc = clientFactory.getClient("ChatClient", ip, port, username,this);
      fc = clientFactory.getClient("FileClient", ip, port, username, this);
      chatRecords.put("GroupChat", new ArrayList<String>());
    } catch (Exception e) {
      //...
        System.out.println("error");
    }
  }

  // ΪGUI������Ϣ�ṩ����
  public void sendMessage(String message) {
	  System.out.println("cr size in c : " + chatRecords.size());
	System.out.println("c send! message is :" + message);
    if (message.equals("[OFFLINE]")) {
      cc.send(message);
      return;
    }
    StringTokenizer tokenizer = new StringTokenizer(message, "[#]");
    String command = tokenizer.nextToken();
    String msg = tokenizer.nextToken();
    if (command.equals("P2P")) {
      String usr = tokenizer.nextToken();
      chatRecords.get(usr).add(username + "[#]" + msg);
      // System.out.println(chatRecords.get(usr).get(chatRecords.get(usr).size()-1));
    } else {
      chatRecords.get("GroupChat").add(username + "[#]" + msg);
      // System.out.println(chatRecords.get("GroupChat").get(chatRecords.get("GroupChat").size()-1));
    }
    
    cc.send(message);
  }

  // ΪGUI�˳���ť�ṩ����
  public void disconnect() {
    cc.stopThread();
    fc.stopThread();
    stopClient = true;
  }

  // ��ȡ��Ӧ�û��������¼
  public List<String> getChatRecords(String username) {
    return chatRecords.get(username);
  }

 
  // ΪGUI�ķ����ļ���ť�ṩ����
  public void sendFile(String info, String filename) {
    fc.send(info, filename);
  }

  // Ϊ�����߳��յ�����Ϣ�ṩ����
  public void receiveMessage(String message) {
    StringTokenizer tokenizer = new StringTokenizer(message, "[#]");
    String command = tokenizer.nextToken();
    String usr = tokenizer.nextToken();
    if (command.equals("INFO") || command.equals("ONLINE")) {// �����û�����������
      String ip = tokenizer.nextToken();
      String port = tokenizer.nextToken();
      chatRecords.put(usr, new ArrayList<String>());
      clientView.updateGUI("ONLINE", usr, "");
    } else if (command.equals("GROUP")) {// Ⱥ����Ϣ
      String msg = tokenizer.nextToken();
      chatRecords.get("GroupChat").add(usr + "[#]" + msg);
      clientView.updateGUI("GROUP", msg, usr);
    } else if (command.equals("P2P")) {// ˽����Ϣ
      String msg = tokenizer.nextToken();
      chatRecords.get(usr).add(usr + "[#]" + msg);
      clientView.updateGUI("P2P", msg, usr);
    } else if (command.equals("OFFLINE")) {// ��������
      String ip = tokenizer.nextToken();
      String port = tokenizer.nextToken();
      chatRecords.remove(usr);
      clientView.updateGUI("OFFLINE", usr, "");
    } else if (command.equals("FILE")) {
      String filename = tokenizer.nextToken();
      String username = tokenizer.nextToken();
      clientView.updateGUI("FILE", username + "���㷢�����ļ�" + filename, "");
    }
  }
}