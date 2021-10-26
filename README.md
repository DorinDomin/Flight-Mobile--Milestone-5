# Flight-Mobile--Milestone-5
The purpose of this extercise- We created an android app, combained with NetCore server. The goal is to control the "Flight Simulator" via andriod app. The app sends a command, that passes to the server and next to the simulator. The app works the same in the other direction. our server in connected to one server only.


## How it works:

1. Download FlightSimulator (Or any other simulator) in the next link- https://flightgear.en.uptodown.com/windows. 
Open the simulator, go to Setting, then go to 'additional setting' and add the next line: "--telnet=socket,in,10,127.0.0.1,5403,tcp --httpd=8080".

NOTE! you can choose to use python server. We added a server to this project for your convenience.

2. Open the WebCore server- running the server will open a new html window with the communication information, such as the lock host port (should be 5 letters).
3. Log in to the app server using the local host from the server.
4. Next you should see screen with the joystick and sliders (similar to milestone 3). Pressing the different control tools will send a new command to the server and to the simulator.

## Version Control

We used Git for this project

https://github.com/DorinDomin/Flight-Mobile--Milestone-5

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.


## Authors
Dorin Domin, Netanel Tamir, Matan Grossman and Itay Heibron
