# Real-Time-Voting-System

# Real-Time-Voting-System

Our primary goal is to design a distributed platform that enables voters to engage in electronic voting processes with maximum assurance of security, efficiency, and responsiveness. Beyond providing a technical solution, we explore various usage scenarios to showcase the versatility and applicability of our system. From student elections to virtual polls in gaming or social media realms, to corporate ballots, our system aims to adapt to diverse electoral contexts.

## Project Structure

Our project's architecture is carefully designed to ensure coherence, reliability, and efficiency. Here's an overview of the key components and their roles:

![alt text](https://github.com/Bouguilim/Real-Time-Voting-System/blob/main/project_pipeline.png?raw=true)

- **Sockets**: The client communicates with the server through socket mechanisms.
- **Apache Kafka**: Data flows from the client to Apache Kafka, which is used for real-time data streaming.
- **Apache Beam Pipeline**: Data streamed through Kafka is processed via a pipeline defined by Apache Beam. This pipeline enables the definition and execution of data processing workflows.
- **MySQL Database**: Processed data is stored in the MySQL database.
- **RMI Server**: The architecture includes an RMI server used to provide authentication services. RMI (Remote Method Invocation) is a Java API that performs object-oriented equivalent of remote procedure calls (RPC).
- **Grafana**: Data from the MySQL database is directed to Grafana, which is a tool used for data visualization and analysis.
- **User Interface**: The user interface is built using Jakarta EE technologies.

## User Interface

The graphical user interface, developed using JavaServer Faces (JSF), serves as a central element for user-system interaction. JSF, a Java framework dedicated to web graphical user interface development, offers a modular structure for creating a user-friendly and intuitive interface. It supports displaying relevant data, collecting user votes, and real-time transmission to the database. Here is some screenshots of it :

![alt text](https://github.com/Bouguilim/Real-Time-Voting-System/blob/main/login.png?raw=true)
![alt text](https://github.com/Bouguilim/Real-Time-Voting-System/blob/main/admin_space.png?raw=true)
![alt text](https://github.com/Bouguilim/Real-Time-Voting-System/blob/main/user.png?raw=true)
![alt text](https://github.com/Bouguilim/Real-Time-Voting-System/blob/main/dashboard.png?raw=true)
