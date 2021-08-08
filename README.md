# AutoManagementProject
This project implements client server application for multiple clients. In program exists 2 kinds of clients usual users and administrators.
Users arrange borrowing of a corporate vehicle with administrator.
Every client is connected to the server, clients communicate with server by sending special information strings for example #INSERT. Server forms SQL query
and sends it to database. As soon as server receives any message to modify database it sends updated DB to all clients. There are several input validations inside interface.
Also settings are implemented so you can launch server and establish clients connections without accesing source codes.
Current version is in development so a lot of things left TODO, and a lot of bugs left to catch.
# Example of a client interface
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2020.35.14.png)
Next elements of interface are implemented in program's interface
* Text field for name input
* Text field for note input
* Text field for time input(field is restricted you cannot input invalid time)
* Send button to send request to server 
* Label to show number of free vehicles
When table in database is changed by anything, the table will be updated
*When response of an admin is received by client, prompt with ok button will appear after returning to working place worker will need to press ok, to finish his trip fix return time, and free vehicle that was assigned to him, after that client application will be closed*
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2020.35.47.png)
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2020.37.22.png)
In file dropdown menu two option
* reconnect (reconnects user to the server, in case there were problems on server side)
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2022.01.11.png)
* Settings (setup server options for establishing connection)
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2022.11.08.png)

# Example of an admin interface
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2020.36.12.png)
In administrator table Id and Name cannot be changed
* Admin is a dropdown menu where you can select descision to approve not approve or leave as it is request that were sent by clients
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2020.36.12.png)
* Note can be changed by admin by double clincking it and pressing enter to confirm return time can be edit but it's pointless because it will set automatically as soon as user presses ok when he returns from the trip
* Registration number is column that contains regestration number of a vehicle, when double clicked it drops down menu showing you all available vehicles' numbers
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2020.36.25.png)
* When administrator finished processing your request for vehicle he should press submit to send his decsion and vehicle number in case of approving a vehicle giveaway
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2020.45.06.png)
* In file menu there are 3 options Settings to set up server parameters for connection, reconnect button in case you need to reconnect to server, and regestration number edit.
### Registration number edit window
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2020.45.30.png)
### Settings window
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2020.45.23.png)

# Example of the server interface
![alt text](https://github.com/KabaevRoman/VehicleManagementProject/blob/master/Client%20Examples/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-08-08%20%D0%B2%2020.22.07.png)
In server interface window you can setup *parameteres of your network*. If remember all requests in database is set, *every query* sent by user and responded by admin *will be coppied into separate table*, in case you need to keep the records for some kind of reports. Number of clients show how many clients,(not admins) are currently connected to server.

