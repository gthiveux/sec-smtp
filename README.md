RunMyProcess SEC SMTP Adapter
=============================

The "SMTP Adapter" is used to send emails through a local SMTP server. It requires the [sec Manager](https://github.com/runmyprocess/sec-manager) to be installed and running.  



##Install and Configure the Adapter
1. Make sure you have [java](http://www.oracle.com/technetwork/java/index.html) and [maven](http://maven.apache.org/) installed on your machine.
2. Download the sec-SMTP project and  run mvn clean install on the project's folder

run mvn clean install

	mvn clean install

3. copy the generated jar file (usually created on a generated "target" folder in the SMTP project's folder) to a folder of your choosing.
4. Create a "configFiles" folder in the jar file's path.
5. inside the "configFiles" folder you must create 2 config files: handler.config and the SMTP.config


The **handler.config** file should look like this:
    
        #Generic Protocol Configuration
        protocol = SMTP
        protocolClass = com.runmyprocess.sec.SMTP
        handlerHost = 127.0.0.1
        connectionPort = 5832
        managerHost = 127.0.0.1
        managerPort = 4444
        pingFrequency = 300
    
Where :  

* **protocol** is the name to identify our Adapter.
* **protocolClass** is the class of the Adapter.
* **handlerHost** is where the Adapter is running.
* **connectionPort** is the port of the adapter where data will be received and returned.
* **managerHost** is where the SEC is running. 
* **managerPort** is the port where the SEC is listening for ping registrations.
* **pingFrequency** is the frequency in which the manager will be pinged (at least three times smaller than what's configured in the manager).  

The **SMTP.config** file should look like this:
   
    #SMTP Configuration
    mail.smtp.auth=true
    mail.smtp.starttls.enable=true
    mail.smtp.host=smtp.gmail.com
    mail.smtp.port=587

You should replace these values with your **local SMTP server informaton**.

##Running and Testing the Adapter
You can now run the Adapter by executing the generated jar in the chosen path:

    java -jar SMTPAdapter.jar
    
If everything is configured correctly and the sec-Manager is running, you can now Post the manager to sent an smtp mail with the configured smtp provider.
The POST body should look like something like this:
    
	{
	"protocol":"SMTP",
	"data":{
			"username":"blabla@blabla.com",
			"password":{
				"encoder":"None",
				"password":"myPassword"
			},
			"from":"blabla@bla.com",
			"to":"blabla@bla.com",
			"subject":"HELLO",
			"body":"WORLD",
			"attachedFiles":[
				{
				"Name"="BLA.ext",
				"data"="SOME BASE 64 DATA" 
				},
				{
				"Name"="BLA2.ext",
				"data"="SOME OTHER BASE 64 DATA" 
				}
			]
		} 
	}
The password can be encoded in base64 by assigning "base64"  to "encoder" in the "password" object and sending the password as base64.

The expected return is a JSON object that should look like this :


	{
	"SECStatus":200,
	"Message":"Mail Sent!"
	}
