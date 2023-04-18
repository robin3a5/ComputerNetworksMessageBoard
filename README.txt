Alexander Robinson and Connor Hyde
Server instructions:

	To run the server, you'll need to have python installed. If you don't already, navigate to https://www.python.org/downloads/ and download/install the latest version.
	Once that's installed, you can open up an administrator command prompt and change the directory to wherever the 'Proj2Server.py' script is located with cd.
	Finally, type 'python Proj2Server.py', and that should start up the server in the terminal.

How to run the client:
First you must install the Java Development Kit on your system downloads can be found here: https://www.oracle.com/java/technologies/downloads/
You will also need the Java Runtime Enviornment installed which can be found here: https://www.java.com/en/download/

First you must compile the .java files into java class files, this can be done by running these commands:
cd ClientFiles 
javac -cp json-20230227.jar ProgrammingAssignment2Client.java ResponseWindow.java

Now you'll want to make a jar file this can be done by executing these commands:
cd ..
jar cfm client.jar MANIFEST.MF ClientFiles/*

Finally you can now run the client program by entering this command:
java -jar client.jar
