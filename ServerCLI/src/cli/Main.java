package cli;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {

        boolean running = true;
        Scanner sc = new Scanner(System.in);
        Controller controller = new Controller();
        while (running) {
            System.out.println("settings - настроить параметры подключения\nstart - запустить сервер\n/help - список команд");
            String choice = sc.next();
            switch (choice) {
                case "settings": {
                    System.out.println("список команд:\n1.all - установить все настройки сервера\n" +
                            "2.dbaddress - установить адрес/хоста базы данных\n" +
                            "3.dbname - установить имя базы данных\n" +
                            "4.dbusername - установить имя пользователя базы данных\n" +
                            "5.dbpassword - установить пароль базы данных\n" +
                            "6.userport - установить порт для клиентов\n" +
                            "7.adminport - установить порт для администраторов\n");
                    String option = sc.next();
                    switch (option) {
                        case "all": {
                            System.out.println("Введите адрес/хоста базы данных");
                            String dbAddress = sc.next();
                            System.out.println("Введите название базы данных");
                            String dbName = sc.next();
                            System.out.println("Введите имя пользователя базы данных");
                            String dbUser = sc.next();
                            System.out.println("Введите пароль базы данных");
                            String dbPassword = sc.next();
                            System.out.println("Введите порт, который будет использоваться для подключения клиентов");
                            String portUser = sc.next();
                            System.out.println("Введите порт, который будет использоваться для подключения администраторов" +
                                    "(должен отличаться от пользовательского порта)");
                            String portAdmin = sc.next();
                            controller.setSettings(dbAddress, dbName, dbUser, dbPassword, portUser, portAdmin);
                            break;
                        }
                        case "dbaddress":
                            System.out.println("Введите адрес/хоста базы данных");
                            controller.setDbAddressString(sc.next());
                            break;
                        case "dbname":
                            System.out.println("Введите название базы данных");
                            controller.setDbNameString(sc.next());
                            break;
                        case "dbusername":
                            System.out.println("Введите имя пользователя базы данных");
                            controller.setDbUserString(sc.next());
                            break;
                        case "dbpassword":
                            System.out.println("Введите пароль базы данных");
                            controller.setDbPasswordString(sc.next());
                            break;
                        case "userport":
                            System.out.println("Введите порт, который будет использоваться для подключения клиентов");
                            controller.setPortUserString(sc.next());
                            break;
                        case "adminport":
                            System.out.println("Введите порт, который будет использоваться для подключения администраторов" +
                                    "(должен отличаться от пользовательского порта)");
                            controller.setPortAdminString(sc.next());
                            break;
                        default:
                            break;
                    }
                    break;
                }
                case "start": {
                    controller.initServer();
                    break;
                }
                case "saveon": {
                    controller.serverUser.saveToggledMode(true);
                    break;
                }
                case "saveoff": {
                    controller.serverUser.saveToggledMode(false);
                }
                case "stop": {
                    controller.shutdown();
                    controller.closeProgram();
                    running = false;
                    break;
                }
                case "truncate": {
                    controller.serverPdo.resetData();
                    break;
                }
                case "showsettings": {
                    controller.printSettings();
                    break;
                }
                case "/help": {
                    System.out.println("1.settings - установить настройки сервера\n" +
                            "2.start - запустить сервер\n" +
                            "3.restart - перезапустить сервер\n" +
                            "4.saveon - включить режим сохранения всех входящих запросов\n" +
                            "5.saveoff - выключить режим сохранения всех входящих запросов\n" +
                            "6.stop - остановить сервер\n" +
                            "7.truncate - опустошить таблицу запросов\n");
                    break;
                }
                default:
                    break;
            }
        }
    }
}
