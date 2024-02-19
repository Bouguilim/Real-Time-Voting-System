// AdminBean.java
package com.journaldev.jsf.beans;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.journaldev.jsf.model.Vote;
import com.journaldev.jsf.util.DataConnect;
import com.journaldev.jsf.util.SessionUtils;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

@ManagedBean
public class AdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String voteTitle;
    private String voteQuestion;
    private String answers;
    private static final String KAFKA_TOPIC = "vote-events-topic";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    
    public String getVoteTitle() {
        return voteTitle;
    }

    public void setVoteTitle(String voteTitle) {
        this.voteTitle = voteTitle;
    }

    public String getVoteQuestion() {
        return voteQuestion;
    }

    public void setVoteQuestion(String voteQuestion) {
        this.voteQuestion = voteQuestion;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }
    
    @SuppressWarnings("resource")
	public String createVote() {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DataConnect.getConnection("votes");

            String tableName = getVoteTitle();

            String createTableSQL = "CREATE TABLE `votes`.`" + tableName + "` ("
                    + "question VARCHAR(255) NOT NULL";

            // Add columns for each answer with an initial count of 0
            String[] answersArray = getAnswers().split(",");
            for (String answer : answersArray) {
                createTableSQL += ", " + answer.trim() + " INT DEFAULT 0";
            }

            createTableSQL += ");";

            // Execute the SQL statement to create the table
            ps = con.prepareStatement(createTableSQL);
            ps.executeUpdate();

            // Insert the question into the table
            String insertQuestionSQL = "INSERT INTO `votes`.`" + tableName + "` (question) VALUES (?)";
            ps = con.prepareStatement(insertQuestionSQL);
            ps.setString(1, getVoteQuestion());
            ps.executeUpdate();
            
            con = DataConnect.getConnection("myvoteapp");
            insertQuestionSQL = "UPDATE `myvoteapp`.`users` SET remvotes = CONCAT(remvotes, ?);";
            ps = con.prepareStatement(insertQuestionSQL);
            ps.setString(1, "," + tableName);
            ps.executeUpdate();
            

            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Vote created successfully!", null));
            return "admin"; 
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating vote: " + e.getMessage(), null));
            return null;
        } finally {
            DataConnect.close(con);
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    
    private String selectedVoteTitle;

    public String getSelectedVoteTitle() {
		return selectedVoteTitle;
	}

	public void setSelectedVoteTitle(String selectedVoteTitle) {
		this.selectedVoteTitle = selectedVoteTitle;
	}

    public List<SelectItem> getAvailableVotes() {
        List<SelectItem> availableVotes = new ArrayList<>();

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = DataConnect.getConnection("votes");
            stmt = (Statement) con.createStatement();
            rs = stmt.executeQuery("SHOW TABLES FROM `votes`");

            while (rs.next()) {
                String tableName = rs.getString(1);
                availableVotes.add(new SelectItem(tableName, tableName));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataConnect.close(con);
        }

        return availableVotes;
    }

    public String deleteVote() {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DataConnect.getConnection("votes");

            for (SelectItem availableVote : getAvailableVotes()) {
                String tableName = availableVote.getValue().toString();

                if (tableName.equals(getSelectedVoteTitle())) {
                    String dropTableSQL = "DROP TABLE IF EXISTS `votes`.`" + tableName+"`";
                    ps = con.prepareStatement(dropTableSQL);
                    ps.executeUpdate();

                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Vote deleted successfully!", null));
                    return "admin";
                }
            }


            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "No matching vote found to delete.", null));
            return null;

        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deleting vote: " + e.getMessage(), null));
            return null;
        } finally {
            DataConnect.close(con);
        }
    }
    
    
    public ArrayList<Vote> getUserVotes() {
        ArrayList<Vote> userVotes = new ArrayList<>();
        String userId = SessionUtils.getUserId();

        if (userId != null) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;

            try {
                connection = (Connection) DataConnect.getConnection("myvoteapp");

                String extractVotesSQL = "SELECT remvotes FROM `myvoteapp`.`users` WHERE uid = ?";
                preparedStatement = (PreparedStatement) connection.prepareStatement(extractVotesSQL);
                preparedStatement.setString(1, userId);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String remvotesValue = resultSet.getString("remvotes");

                    String[] voteTableNames = remvotesValue.split(",");
                    if (voteTableNames.length > 0 && voteTableNames[0].isEmpty()) {
                        voteTableNames = Arrays.copyOfRange(voteTableNames, 1, voteTableNames.length);
                    }

                    for (String tableName : voteTableNames) {
                        Vote vote = extractTableData(tableName);
                        userVotes.add(vote);
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error extracting votes for user: " + e.getMessage());
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (preparedStatement != null) preparedStatement.close();
                    if (connection != null) DataConnect.close(connection);
                } catch (SQLException e) {
                    System.out.println("Error closing resources: " + e.getMessage());
                }
            }
        }
        return userVotes;
    }
	
	public static Vote extractTableData(String tableName) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		String title = tableName;
	    String question = "";
	    List<String> options = new ArrayList<>();

		try {
			connection = (Connection) DataConnect.getConnection("votes");

			// Query to select all rows from the specified table
			String query = "SELECT * FROM `votes`.`" + tableName + "`";
			preparedStatement = (PreparedStatement) connection.prepareStatement(query);
			resultSet = preparedStatement.executeQuery();

			ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			String columnName = "";
			for (int i = 2; i <= columnCount; i++ ) {
			  columnName = metaData.getColumnName(i);
			  options.add(columnName);
			}

			if (resultSet.next()) {
				String firstColumnValue = resultSet.getString(1);
				question = firstColumnValue;
			}
		} catch (Exception ex) {
			System.out.println("Error extracting table data: " + ex.getMessage());
		} finally {
			try {
				if (resultSet != null)
					resultSet.close();
				if (preparedStatement != null)
					preparedStatement.close();
				if (connection != null)
					DataConnect.close(connection);
			} catch (Exception ex) {
				System.out.println("Error closing resources: " + ex.getMessage());
			}
		}
		return new Vote(title, question, options);
	}

	
	private Map<String, String> selectedAnswers = new HashMap<>();

    public Map<String, String> getSelectedAnswers() {
        return selectedAnswers;
    }

    public void setSelectedAnswers(Map<String, String> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }
    
    private static Producer<String, String> createKafkaProducer() {

        java.util.Properties kafkaProps = new java.util.Properties();
        kafkaProps.put("bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS);
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        return new KafkaProducer<String, String>(kafkaProps);
    }
    
    private static void sendKafkaEvent(String message, Producer<String, String> kafkaProducer) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(KAFKA_TOPIC, message);

        try {
            kafkaProducer.send(record);
            System.out.println("Message envoyé avec succès à " + record.key());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du message à " + record.key() + ": " + e.getMessage());

        }
    }
    
    public String submitVotes() {
    	Producer<String, String> kafkaProducer = createKafkaProducer();
    	for (Map.Entry<String, String> entry : selectedAnswers.entrySet()) {
            String title = entry.getKey();
            String selectedOption = entry.getValue();
            if (selectedOption != null) {
            	try {
            		sendKafkaEvent("{'vote_id' : " + title + ", 'vote' : " + selectedOption + ", 'number_vote' : 1}", kafkaProducer);
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }
        }

        String userId = SessionUtils.getUserId();
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DataConnect.getConnection("myvoteapp");

            // Construct the new remvotes value by removing the voted titles
            List<String> votedTitles = new ArrayList<>(selectedAnswers.keySet());
            String remvotesValue = getUpdatedRemvotes(userId, votedTitles);

            // Update the remvotes column for the user
            String updateRemvotesSQL = "UPDATE `myvoteapp`.`users` SET remvotes = ? WHERE uid = ?";
            preparedStatement = connection.prepareStatement(updateRemvotesSQL);
            preparedStatement.setString(1, remvotesValue);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating remvotes column: " + e.getMessage());
        } finally {
            // Close resources
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) DataConnect.close(connection);
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }

        // Reset selectedAnswers if needed
        selectedAnswers.clear();


        return "user"; // Specify the appropriate navigation outcome.
    }
    
 // Helper method to update remvotes
    private String getUpdatedRemvotes(String userId, List<String> votedTitles) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DataConnect.getConnection("myvoteapp");

            // Fetch the current remvotes value for the user
            String fetchRemvotesSQL = "SELECT remvotes FROM `myvoteapp`.`users` WHERE uid = ?";
            preparedStatement = connection.prepareStatement(fetchRemvotesSQL);
            preparedStatement.setString(1, userId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String currentRemvotes = resultSet.getString("remvotes");

                // Process currentRemvotes to remove voted titles
                List<String> remainingTitles = new ArrayList<>(Arrays.asList(currentRemvotes.split(",")));
                remainingTitles.removeAll(votedTitles);

                // Construct the updated remvotes value
                return String.join(",", remainingTitles);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching remvotes value: " + e.getMessage());
        } finally {
            // Close resources
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) DataConnect.close(connection);
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }

        return ""; // Return an empty string if an error occurs
    }
    
}
