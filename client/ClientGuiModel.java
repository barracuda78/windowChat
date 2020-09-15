package com.javarush.task.task30.task3008.client;
// паттерн MVC (Model-View-Controller).
//1) класс ClientGuiModel в пакете client.


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientGuiModel {
    //2) Добавил в него множество(set) строк в качестве final поля allUserNames.
    //В нем будет храниться список всех участников чата. Проинициализируй его.
    private final Set<String> allUserNames = new HashSet<>();

    //3) Добавил поле String newMessage, в котором будет храниться новое сообщение, которое получил клиент.
    private String newMessage;

    //4) Добавил геттер для allUserNames, запретив модифицировать возвращенное множество.

    public Set<String> getAllUserNames(){
        return Collections.unmodifiableSet(allUserNames);
    }

    //5) Добавил сеттер и геттер для поля newMessage.
    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }


    //6) Добавил метод void addUser(String newUserName), который должен добавлять имя участника во множество, хранящее всех участников.
    public void addUser(String newUserName){
        allUserNames.add(newUserName);
    }

    //7) Добавил метод void deleteUser(String userName), который будет удалять имя участника из множества.
    public void deleteUser(String userName){
        allUserNames.remove(userName);
    }
    
}
