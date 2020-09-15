package com.javarush.task.task30.task3008.client;


import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;
import java.io.IOException;
import java.net.Socket;


public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;


    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }


    public class SocketThread extends Thread{


        @Override
        public void run(){
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();
            try (Socket socket = new Socket(serverAddress, serverPort);
                 Connection connection = new Connection(socket)){
                Client.this.connection = connection;
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }

        }

        //void processIncomingMessage(String message) - должен выводить текст message в консоль.
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);

        }

        //void informAboutAddingNewUser(String userName) - должен выводить в консоль информацию о том, что участник с именем userName присоединился к чату.
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage("участник с именем " + userName + " присоединился к чату.");
        }

        //void informAboutDeletingNewUser(String userName) - должен выводить в консоль, что участник с именем userName покинул чат.
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage("участник с именем " + userName + " покинул чат.");
        }

        //4) void notifyConnectionStatusChanged(boolean clientConnected) - этот метод должен:
        //а) Устанавливать значение поля clientConnected внешнего объекта Client в соответствии с переданным параметром.
        //б) Оповещать (пробуждать ожидающий) основной поток класса Client.
        //
        //Подсказка: используй синхронизацию на уровне текущего объекта внешнего класса и метод notify().
        //Для класса SocketThread внешним классом является класс Client.
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        //Этот метод будет представлять клиента серверу.
        //
        //Он должен:
        //а) В цикле получать сообщения, используя соединение connection.
        //б) Если тип полученного сообщения NAME_REQUEST (сервер запросил имя),
              // запросить ввод имени пользователя с помощью метода getUserName(),
              // создать новое сообщение с типом MessageType.USER_NAME и введенным именем,
              // отправить сообщение серверу.
        //в) Если тип полученного сообщения MessageType.NAME_ACCEPTED (сервер принял имя), значит сервер принял имя клиента, нужно об этом сообщить главному потоку, он этого очень ждет.
        //Сделай это с помощью метода notifyConnectionStatusChanged(), передав в него true.
        //После этого выйди из метода.
        //г) Если пришло сообщение с каким-либо другим типом, кинь исключение IOException("Unexpected MessageType").
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while(true){
                Message message = connection.receive();
                if(message.getType() == MessageType.NAME_REQUEST){
                    String name = getUserName();
                    Message messageToServer = new Message(MessageType.USER_NAME, name);
                    connection.send(messageToServer);
                } else if(message.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                } else {
                    throw new IOException("Unexpected MessageType");
                }

            }
        }

        //Этот метод будет реализовывать главный цикл обработки сообщений сервера. Внутри метода:
        //а) ---Получи сообщение от сервера, используя соединение connection.
        //б) ---Если это текстовое сообщение (тип MessageType.TEXT), обработай его с помощью метода processIncomingMessage().
        //в) ---Если это сообщение с типом MessageType.USER_ADDED, обработай его с помощью метода informAboutAddingNewUser().
        //г) ---Если это сообщение с типом MessageType.USER_REMOVED, обработай его с помощью метода informAboutDeletingNewUser().
        //д) Если клиент получил сообщение какого-либо другого типа, брось исключение IOException("Unexpected MessageType").
        //е) Размести код из предыдущих пунктов внутри бесконечного цикла.
        //Цикл будет завершен автоматически если произойдет ошибка (будет брошено исключение) или поток, в котором работает цикл, будет прерван.
        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while(true) {
                Message messageFromServer = connection.receive();
                if (messageFromServer.getType() == MessageType.TEXT) {
                    processIncomingMessage(messageFromServer.getData());
                } else if (messageFromServer.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(messageFromServer.getData());
                } else if (messageFromServer.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(messageFromServer.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }


    }


    //run() должен создавать вспомогательный поток SocketThread, ожидать пока тот установит соединение с сервером, а после этого в цикле считывать сообщения с консоли и отправлять их серверу.
    //Условием выхода из цикла будет отключение клиента или ввод пользователем команды 'exit'.
    //Для информирования главного потока, что соединение установлено во вспомогательном потоке, используй методы wait() и notify() объекта класса Client.
    //Реализация метода run должна:
    //---а) Создавать новый сокетный поток с помощью метода getSocketThread().
    //---2. Метод run() должен создавать и запускать новый поток, полученный с помощью метода getSocketThread().
    //---3. Поток созданный с помощью метода getSocketThread() должен быть отмечен как демон (setDaemon(true)).
    //г) Заставить текущий поток ожидать, пока он не получит нотификацию из другого потока.
    //4. После запуска нового socketThread метод run() должен ожидать до тех пор, пока не будет пробужден.
    //Подсказка: используй wait() и синхронизацию на уровне объекта.
    //Если во время ожидания возникнет исключение, сообщи об этом пользователю и выйди из программы.

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("во время ожидания возникла ошибка. Выход из программы.");
                System.exit(100);
                // e.printStackTrace();
            }
            //д) После того, как поток дождался нотификации, проверь значение clientConnected.
            //Если оно true - выведи "Соединение установлено.
            //Для выхода наберите команду 'exit'.".
            //Если оно false - выведи "Произошла ошибка во время работы клиента.".
            if(clientConnected){
                ConsoleHelper.writeMessage("Соединение установлено.\n" +
                        "Для выхода наберите команду 'exit'.");
            } else{
                ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            }
            //е) Считывай сообщения с консоли пока клиент подключен.
            //Если будет введена команда 'exit', то выйди из цикла.
            //ж) После каждого считывания, если метод shouldSendTextFromConsole() возвращает true, отправь считанный текст с помощью метода sendTextMessage().
            while(clientConnected){
                String s = ConsoleHelper.readString();
                if(s.equals("exit"))
                    break;
                if(shouldSendTextFromConsole()){
                    sendTextMessage(s);
                }
            }
        }
    }



        //1. String getServerAddress() - должен запросить ввод адреса сервера у пользователя и вернуть введенное значение.
        //Адрес может быть строкой, содержащей ip, если клиент и сервер запущен на разных машинах или 'localhost', если клиент и сервер работают на одной машине.
        //1. Метод getServerAddress() должен возвращать строку (адрес сервера), считанную с консоли.
        //Метод getServerAddress() возвращает некорректное значение. Убедись, что ты используешь метод readString() класса ConsoleHelper.
    protected String getServerAddress(){
        ConsoleHelper.writeMessage("введите адрес сервера");
        return ConsoleHelper.readString();
    }

    //2. int getServerPort() - должен запрашивать ввод порта сервера и возвращать его.
    protected int getServerPort(){
        ConsoleHelper.writeMessage("введите номер порта сервера");
        return ConsoleHelper.readInt();
    }

    //String getUserName() - должен запрашивать и возвращать имя пользователя.
    protected String getUserName(){
        ConsoleHelper.writeMessage("введите имя пользователя");
        return ConsoleHelper.readString();
    }

    //4. boolean shouldSendTextFromConsole() - в данной реализации клиента всегда должен возвращать true (мы всегда отправляем текст введенный в консоль).
    //Этот метод может быть переопределен, если мы будем писать какой-нибудь другой клиент, унаследованный от нашего, который не должен отправлять введенный в консоль текст.
    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    //SocketThread getSocketThread() - должен создавать и возвращать новый объект класса SocketThread.
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    //6. void sendTextMessage(String text) - создает новое текстовое сообщение, используя переданный текст и отправляет его серверу через соединение connection.
    //Если во время отправки произошло исключение IOException, то необходимо вывести информацию об этом пользователю и присвоить false полю clientConnected.
    //5. Метод sendTextMessage() должен создавать и отправлять новое текстовое сообщение используя connection
    // и устанавливать флаг clientConnected в false, если во время отправки или создания сообщения возникло исключение IOException.
    protected void sendTextMessage(String text) {

        try {
            Message message = new Message(MessageType.TEXT, text);
            connection.send(message);
        } catch (IOException e) {
            clientConnected = false;
            //ConsoleHelper.writeMessage("во время отправки произошло исключение IOException");
            //e.printStackTrace();
        }
    }

}
