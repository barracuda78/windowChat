package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args){

        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("сервер запущен");
            while(true){
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket){
            this.socket = socket;
        }



        @Override
        public void run(){
                //в котором будет происходить обмен сообщениями с клиентом.
            //1. Выводить сообщение, что установлено новое соединение с удаленным адресом, который можно получить с помощью метода getRemoteSocketAddress().
            //1. Метод run() должен выводить на экран сообщение о том, что было установлено соединение с удаленным адресом (используя метод getRemoteSocketAddress()).
            ConsoleHelper.writeMessage("установлено новое соединение с удаленным адресом: " + socket.getRemoteSocketAddress());
            //2. Создавать Connection, используя поле socket.
            //2. Метод run() должен создавать новое соединение (connection) используя в качестве параметра поле socket.
            //3. Вызывать метод, реализующий рукопожатие с клиентом, сохраняя имя нового клиента.
            //3. Метод run() должен вызывать метод serverHandshake() используя в качестве параметра только что созданное соединение; результатом будет имя пользователя (userName).
            //Connection connection = null;
            String userName = null;
            try (Connection connection = new Connection(socket)){
                //connection = new Connection(socket);
                 userName = serverHandshake(connection);
                //4. Рассылать всем участникам чата информацию об имени присоединившегося участника (сообщение с типом USER_ADDED).
                //Подумай, какой метод подойдет для этого лучше всего.
                //4. Метод run() должен вызывать метод sendBroadcastMessage() используя в качестве параметра новое сообщение (MessageType.USER_ADDED, userName).
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                //5. Сообщать новому участнику о существующих участниках.
                //5. Метод run() должен вызывать метод notifyUsers() используя в качестве параметров connection и userName.
                notifyUsers(connection, userName);
                //6. Запускать главный цикл обработки сообщений сервером.
                //6. Метод run() должен вызывать метод serverMainLoop используя в качестве параметров connection и userName.
                serverMainLoop(connection, userName);
                //7. Прежде чем завершиться, метод run() должен удалять из connectionMap запись соответствующую userName,
                // и отправлять всем участникам чата сообщение о том, что текущий пользователь был удален.
                //7. Обеспечить закрытие соединения при возникновении исключения.

                if(userName!=null)
                    connectionMap.remove(userName);
                connection.close();
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                ConsoleHelper.writeMessage("соединение с удаленным адресом закрыто");
                //8. Отловить все исключения типа IOException и ClassNotFoundException, вывести в консоль информацию, что произошла ошибка при обмене данными с удаленным адресом.
                //8. Метод run() должен корректно обрабатывать исключения IOException и ClassNotFoundException.
                //9. После того как все исключения обработаны, если п.11.3 отработал и возвратил нам имя, мы должны удалить запись для этого имени из connectionMap
                // и разослать всем остальным участникам сообщение с типом USER_REMOVED и сохраненным именем.
                //10. Последнее, что нужно сделать в методе run() - вывести сообщение, информирующее что соединение с удаленным адресом закрыто.
            }catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }
        }

        //+++1. В классе Handler должен присутствовать метод private String serverHandshake(Connection connection).
        //+++2. Метод serverHandshake должен отправлять запрос имени используя метод send класса Connection.
        //+++3. До тех пор, пока тип сообщения полученного в ответ не будет равен MessageType.USER_NAME, запрос имени должен быть выполнен снова.
        //+++4. В случае, если в ответ пришло пустое имя, запрос имени должен быть выполнен снова.
        //+++5. В случае, если имя уже содержится в connectionMap, запрос имени должен быть выполнен снова.
        //+++6. После успешного проведения всех проверок, метод serverHandshake должен добавлять новую пару (имя, соединение) в connectionMap
        //+++6.1. и отправлять сообщение о том, что имя было принято.
        //7. Метод serverHandshake должен возвращать имя нового клиента с которым было установлено соединение.

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{

            do {
                //сервер отправил запрос пользователю:
                connection.send(new Message(MessageType.NAME_REQUEST, "Сообщите Ваше имя."));
                //ждем ответное сообщение от юзера.
                Message answer = connection.receive();
                //Если тип сообщения MessageType.USER_NAME
                String userName = "";
                if (answer.getType() == MessageType.USER_NAME) {
                    userName = answer.getData();
                } else
                    continue;
                //в теле оператора if из сообщения answer извлекается имя пользователя,
                // которое проверяется на пустую строку
                if(userName.equals(""))
                    continue;
                //и проверяется на уникальность.
                if(connectionMap.containsKey(userName))
                    continue;

                // При успешном завершении всех проверок имя пользователя вместе с соединением добавляется в отображение,
                connectionMap.put(answer.getData(), connection);
                // клиенту отправляется сообщение ,
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                ConsoleHelper.writeMessage("имя было принято");
                // метод завершается, возвращая строку с именем подключённого пользователя.
                return answer.getData();

            } while (true);

        }

        //---2) У каждого элемента из п.1 получить имя клиента, сформировать команду с типом USER_ADDED и полученным именем.
        //---3) Отправить сформированную команду через connection.
        //---4) Команду с типом USER_ADDED и именем равным userName отправлять не нужно, пользователь и так имеет информацию о себе.
        private void notifyUsers(Connection connection, String userName) throws IOException{
            for(Map.Entry<String, Connection> pair : connectionMap.entrySet()){
                String name = pair.getKey();
                if(name.equals(userName))
                    continue;
                Message message = new Message(MessageType.USER_ADDED, name);
                connection.send(message);
            }
        }

        //---1. В классе Handler должен быть создан метод private void serverMainLoop(Connection connection, String userName).
        //---2. Метод serverMainLoop() должен в бесконечном цикле получать сообщения от клиента (используя метод receive() класса Connection).
        //---2. Если принятое сообщение - это текст (тип TEXT), то формировать новое текстовое сообщение путем конкатенации: имени клиента, двоеточия, пробела и текста сообщения.
        //---3. Если сообщение, полученное методом serverMainLoop(), имеет тип MessageType.TEXT,
        // ---то должно быть отправлено новое сообщение всем участникам чата используя метод sendBroadcastMessage() (форматирование сообщения описано в условии).
        //4. Если сообщение, полученное методом serverMainLoop(), имеет тип отличный от MessageType.TEXT, метод sendBroadcastMessage() не должен быть вызван, и должно быть выведено сообщение об ошибке.
        //4. Если принятое сообщение не является текстом, вывести сообщение об ошибке
        //5. Организовать бесконечный цикл, внутрь которого перенести функционал пунктов 10.1-10.4.
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while(true){
                Message message = connection.receive();
                if(message.getType() == MessageType.TEXT){
                    Message messageToAll = new Message(MessageType.TEXT, userName + ": " + message.getData());
                    sendBroadcastMessage(messageToAll);
                } else{
                    ConsoleHelper.writeMessage("ошибка. Сообщение не является текстом.");
                }
            }
        }
    }


    public static void sendBroadcastMessage(Message message){
        for(Map.Entry<String, Connection> pair : connectionMap.entrySet()){
            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Извините, мы не смогли отправить Ваше сообщение.");
            }
        }
    }

}
