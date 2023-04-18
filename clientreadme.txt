How to run the client:
First you must install the Java Development Kit on your system downloads can be found here: https://www.oracle.com/java/technologies/downloads/
You will also need the Java Runtime Enviornment installed which can be found here: https://www.java.com/en/download/
First you must compile the .java files into java class files, this can be done by running this command: 
javac -cp json-20230227.jar ProgrammingAssignment2Client.java ResponseWindow.java
Now you'll want to make a jar file this can be done by executing this command:
jar cfm client.jar MANIFEST.MF ClientFiles/*
Finally you can now run the client program by entering this command:
java -jar client.jar