package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;


import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class BotClient extends Client{

    public class BotSocketThread extends SocketThread{



        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            sendTextMessage("Привет всем. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            String[] nameAndTextArray = message.split(": ");
            if (nameAndTextArray.length > 1) {
                String name = nameAndTextArray[0];
                String text = nameAndTextArray[1];
                Calendar calendar = new GregorianCalendar();
                Date date = calendar.getTime();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                switch (text) {
                    case ("дата"): {
                        Format f = new SimpleDateFormat("d.MM.YYYY");
                        String s = f.format(date);
                        ConsoleHelper.writeMessage("Информация для " + name + ": " + s);
                        sendTextMessage("Информация для " + name + ": " + s);
                        break;
                    }
                    case ("день"): {
                        Format f = new SimpleDateFormat("d");
                        String s = f.format(date);
                        sendTextMessage("Информация для " + name + ": " + s);
                        break;
                    }
                    case ("месяц"): {
                        Format f = new SimpleDateFormat("MMMM");
                        String s = f.format(date);
                        sendTextMessage("Информация для " + name + ": " + s);
                        break;
                    }
                    case ("год"): {
                        Format f = new SimpleDateFormat("YYYY");
                        String s = f.format(date);
                        sendTextMessage("Информация для " + name + ": " + s);
                        break;
                    }
                    case ("время"): {
                        Format f = new SimpleDateFormat("H:mm:ss");
                        String s = f.format(date);
                        sendTextMessage("Информация для " + name + ": " + s);
                        break;
                    }
                    case ("час"): {
                        Format f = new SimpleDateFormat("H");
                        String s = f.format(date);
                        sendTextMessage("Информация для " + name + ": " + s);
                        break;
                    }
                    case ("минуты"): {
                        Format f = new SimpleDateFormat("m");
                        String s = f.format(date);
                        sendTextMessage("Информация для " + name + ": " + s);
                        break;
                    }
                    case ("секунды"): {
                        Format f = new SimpleDateFormat("s");
                        String s = f.format(date);
                        sendTextMessage("Информация для " + name + ": " + s);
                        break;
                    }

                }
//                if(text.contains("болтать") || text.contains("чат") || text.contains("разговор") || text.contains("говорить")|| text.contains("поболтать") || text.contains("поговорим") || text.contains("початимся")|| text.contains("поболтаем")|| text.contains("переписываемся")|| text.contains("переписываться")){
//                    try {
//                        chat00();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }


            }
        }

    }

    @Override
    protected SocketThread getSocketThread(){
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    @Override
    protected String getUserName(){
        int x = (int)(Math.random() * 100);
        return "date_bot_" + x;
    }





    public static void main(String[] args) {
        new BotClient().run();
    }

}
