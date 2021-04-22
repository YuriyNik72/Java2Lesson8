package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ClientHandler {

	private ConsoleServer server;
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	private String nickname;

	// черный список у пользователя, а не у сервера
	List<String> blackList;

	public ClientHandler(ConsoleServer server, Socket socket) {
		try {
			this.server = server;
			this.socket = socket;
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
			this.blackList = AuthService.getBlackListByNickname(nickname);

			new Thread(() -> {
				boolean isExit = false;
				try {
					socket.setSoTimeout(120000);
					while (true) {
						String str = in.readUTF();
						if (str.startsWith("/auth")){
							String[] tokens = str.split(" ");
							String nick = AuthService.getNicknameByLoginAndPass(tokens[1], tokens[2]);
							if (nick != null) {
								if (!server.isNickBusy(nick)) {
									sendMsg("/auth-OK");
									setNickname(nick);
									socket.setSoTimeout(0);
									server.subscribe(ClientHandler.this);
									break;
								} else {
									sendMsg("Учетная запись уже используется");
								}
							} else {
								sendMsg("Неверный логин/пароль");
							}
						}
						// регистрация
						if (str.startsWith("/signup ")) {
							String[] tokens = str.split(" ");
							int result = AuthService.addUser(tokens[1], tokens[2], tokens[3]);
							if (result > 0) {
								sendMsg("Successful registration");
							} else {
								sendMsg("Registration failed");
							}
						}
						// выход
						if ("/end".equals(str)) {
							isExit = true;
							break;
						}
					}

					if (!isExit) {
						while (true) {
							String str = in.readUTF();
							// для всех служебных команд и личных сообщений
							if (str.startsWith("/") || str.startsWith("@")) {
								if ("/end".equalsIgnoreCase(str)){
									// для оповещения клиента, т.к. без сервера клиент работать не должен
									out.writeUTF("/serverClosed");
									System.out.println("Client (" + socket.getInetAddress() + ") exited");
									break;
								}
								// вторая часть ДЗ. выполнение
								if (str.startsWith("@")) {
									String[] tokens = str.split(" ", 2);
									server.sendPrivateMsg(this, tokens[0].substring(1), tokens[1]);
								}
								// черный список для пользователя. но пока что только в рамках одного запуска программы
								if (str.startsWith("/blacklist ")) {
									String[] tokens = str.split(" ");
									if(AuthService.getBlackListByNickname(nickname).contains(tokens[1])){
										if(AuthService.deleteUserFromBlackList(nickname,tokens[1])==1){
											sendMsg("You exclude "+ tokens[1]+" from blacklist");
										}else{
											sendMsg("Something wrong! Cannot exclude.");
										}
									}else {
										if(AuthService.addUserToBlackList(nickname,tokens[1])==1){
											blackList.add(tokens[1]);
											sendMsg("You added " + tokens[1] + " to blacklist");
										}else {
											sendMsg("Something wrong! Cannot add.");
										}

									}

								}
							} else {
								server.broadcastMessage(this, nickname +": " + str);
							}
							System.out.println("Client (" + socket.getInetAddress() + "): " + str);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					server.unsubscribe(this);
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMsg(String msg) {
		try {
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public boolean checkBlackList(String nickname) {
		return blackList.contains(nickname);
	}

	public int message(String nickTo, String message, String nickFrom, Connection connection){
		PreparedStatement ps=null;
		try {
			ps=connection.prepareStatement("INSERT INTO messagehistory (nickTo, massage, nickFrom) VALUES (?,?,?)");
			ps.setString(1,nickTo);
			ps.setString(2,message);
			ps.setString(3,nickFrom);
			return ps.executeUpdate();
		}catch (SQLException e){
			e.printStackTrace();
		}finally {
			statementClose(ps);
		}
		return 0;
	}

	private void statementClose(PreparedStatement ps) {
		try {
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}



}