package server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Logger;

import static server.AuthService.connect;

public class ConsoleServer {

	private static final Logger LOGGER=Logger.getLogger(String.valueOf(ConsoleServer.class));

	private Vector<ClientHandler> users;

	public ConsoleServer() {
		users = new Vector<>();
		ServerSocket server = null; // наша сторона
		Socket socket = null; // удаленная (remote) сторона

		try {
			connect();
			server = new ServerSocket(6001);
			System.out.println("Server started");
			LOGGER.info("Сервер запустился");
			while (true) {
				socket = server.accept();
				System.out.printf("Client [%s] try to connect\n", socket.getInetAddress());
				LOGGER.info("Клиент " +socket.getInetAddress()+ " подключается");
				new ClientHandler(this, socket);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				System.out.printf("Client [%s] disconnected", socket.getInetAddress());
				LOGGER.info("Клиент "+ socket.getInetAddress() + " отключился");
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			AuthService.disconnect();
		}
	}

	public void subscribe(ClientHandler client) {
		users.add(client);
		System.out.println(String.format("User [%s] connected", client.getNickname()));
		LOGGER.info("Пользователь " + client.getNickname() +" подключился");
		broadcastClientsList();
	}

	public void unsubscribe(ClientHandler client) {
		users.remove(client);
		System.out.println(String.format("User [%s] disconnected", client.getNickname()));
		LOGGER.info("Пользователь " + client.getNickname() +" отключился");
		broadcastClientsList();
	}

	public void broadcastMessage(ClientHandler from, String str) {
		for (ClientHandler c : users) {
			if (!c.checkBlackList(from.getNickname())) {
				c.sendMsg(str);
				AuthService.addMessage(str,c.getNickname());
				LOGGER.info("Пользователь " + from.getNickname() +" прислал сообщение");
			}
		}
	}

	public boolean isNickBusy(String nick) {
		for (ClientHandler c : users) {
			if (c.getNickname().equals(nick)) {
				return true;
			}
		}
		return false;
	}

	public void sendPrivateMsg(ClientHandler nickFrom, String nickTo, String msg) {
		for (ClientHandler c : users) {
			if (c.getNickname().equals(nickTo)) {
				if (c.checkBlackList(nickFrom.getNickname())) { //запрет сообщений от ClientBlackList
					break;
				}
				if (!nickFrom.getNickname().equals(nickTo)) {
					c.sendMsg(nickFrom.getNickname() + ": [Send for " + nickTo + "] " + msg);
					nickFrom.sendMsg(nickFrom.getNickname() + ": [Send for " + nickTo + "] " + msg);
				}
			}
		}
	}

	private void broadcastClientsList() {
		StringBuilder sb = new StringBuilder();
		sb.append("/clientList ");
		for (ClientHandler c : users) {
			sb.append(c.getNickname() + " ");
		}
		String out = sb.toString();
		for (ClientHandler c : users) {
			c.sendMsg(out);
		}
	}

}