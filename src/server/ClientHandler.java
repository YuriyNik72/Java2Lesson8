package server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler {

	private ConsoleServer server;
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	private String nickname;

	// черный список у пользователя, а не у сервера
	List<String> blackList;
	List<String> historyList;
	String message;

	public ClientHandler(ConsoleServer server, Socket socket) {
		try {
			this.server = server;
			this.socket = socket;
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
			this.blackList = AuthService.getBlackListByNickname(nickname);
			this.historyList=AuthService.getMessage(message, nickname);

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
									readHistory(100); //вывод сообщений из файла
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
									//запись сообщений в БД
								if(str.startsWith("/getHistory")){
									//String message="Привет";
									//List<String> historyList= AuthService.getMessage(message, nickname);
									sendMsg("----History Loaded----");
									for (int i = 0; i <= historyList.size() ; i++) {
										sendMsg(historyList.get(i));
										System.out.println(historyList.get(i));
									}
									historyList.clear();
								}
							} else {
								server.broadcastMessage(this, nickname +": " + str);
								writeHistory(str); //запись сообщений в файл
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

	public void readHistory(int n){
		File file =new File("src/server/history.txt");
		try{
			RandomAccessFile raf=new RandomAccessFile(file,"r");
			long length = file.length()-1;
			int readLine=0;
			StringBuilder sb =new StringBuilder();
			for(long i=length;i >=0; i--){
				raf.seek(i);
				char c=(char) raf.read();
				if(c == '\n'){
					readLine++;
					if(readLine == n){
						break;
					}
				}
				sb.append(c);
			}
			sendMsg(String.valueOf(sb.reverse()));
		} catch (IOException e) {
			e.printStackTrace();
			}
	}

	public void writeHistory (String msg){
		try(FileWriter writer = new FileWriter("src/server/history.txt", true))
		{
			// запись всей строки
			String text =(nickname + ": " + msg + "\n");
			writer.write(text);
			// запись по символам
//			writer.append('\n');
//			writer.append('E');
			writer.flush();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

}