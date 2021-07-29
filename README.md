# AutoManagementProject
This project implements client server application for multiple clients. In program exists 2 kinds of clients usual users and administrators.
Users arrange borrowing of a corporate vehicle with administrator.
Every client is connected to the server, clients communicate with server by sending special information strings for example #INSERT. Server forms SQL query
and sends it to database. As soon as server receives any message to modify database it sends updated DB to all clients. There are several input validations inside interface.
Also settings are implemented so you can launch server and establish clients connections without accesing source codes.
Current version is in development so a lot of things left TODO, and a lot of bugs left to catch.
Examples of interface
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-07-30%20%D0%B2%2000.46.53.png)
