package com.geekbrains.server.authorization;

import java.util.Map;

import static com.geekbrains.server.authorization.JdbcConnector.users;

public class InMemoryAuthServiceImpl implements AuthService {



    public InMemoryAuthServiceImpl() {

//        users.put("login1", new UserData("login1", "password1", "first_user"));
//        users.put("login2", new UserData("login2", "password2", "second_user"));
//        users.put("login3", new UserData("login3", "password3", "third_user"));
    }



    @Override
    public void start() {
        System.out.println("Сервис аутентификации инициализирован");
    }

    @Override
    public synchronized String getNickNameByLoginAndPassword(String login, String password) throws Exception {
        UserData user = users.get(login);
        JdbcConnector.logIn();
        for (Map.Entry<String, UserData> current : users.entrySet()) {
            // Ищем пользователя по логину и паролю, если нашли то возвращаем никнэйм
            if (user != null && user.getPassword().equals(password)) {
                return user.getNickName();
            }
        }
        return null;
    }

    @Override
    public void end() {
        System.out.println("Сервис аутентификации отключен");
    }
}
