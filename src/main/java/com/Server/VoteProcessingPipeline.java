package com.Server;
import org.apache.beam.sdk.io.kafka.KafkaIO;

import org.apache.kafka.common.serialization.StringDeserializer;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.schemas.transforms.Group;
import org.apache.beam.sdk.schemas.transforms.Select;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.*;
import org.apache.beam.sdk.values.*;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VoteProcessingPipeline {

    /**
     * The logger to output status messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(VoteProcessingPipeline.class);

    public interface Options extends PipelineOptions {
    }

    public static void main(String[] args) {
        Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
        run(options);
    }

    static class JsonToVoteElement extends DoFn<String, VoteElement> {

        @ProcessElement
        public void processElement(@Element String json, OutputReceiver<VoteElement> r) throws Exception {
            Gson gson = new Gson();
	        VoteElement voteElement = gson.fromJson(json, VoteElement.class);
            r.output(voteElement);    
        }
    }

    /**
     * A Beam schema for counting pageviews per minute
     */

    public static String fn(String key, String value) {
        return value;
    }

    public static class ExtractValuesFunction extends SimpleFunction<KV<String, String>, String> {
        @Override
        public String apply(KV<String, String> input) {
            return fn(input.getKey(), input.getValue());
        }
    }

    public static String fn_key(String key, String value) {
        return key;
    }

    public static class ExtractKeysFunction extends SimpleFunction<KV<String, String>, String> {
        @Override
        public String apply(KV<String, String> input) {
            return fn_key(input.getKey(), input.getValue());
        }
    }

     
    public static PipelineResult run(Options options) {

        Pipeline pipeline = Pipeline.create(options);

	    PCollection<VoteElement> voteElement = pipeline
                .apply("ReadMessage", KafkaIO.<String, String>read()
                        .withBootstrapServers("localhost:9092")
                        .withTopic("vote-events-topic")
                        .withKeyDeserializer(StringDeserializer.class)
                        .withValueDeserializer(StringDeserializer.class)
                        .withoutMetadata())

                .apply(MapElements.via(new ExtractValuesFunction()))
                        
                .apply("ParseJson", ParDo.of(new JsonToVoteElement()));
            
        PCollection<Row> voteCounts = voteElement
                .apply(Window.<VoteElement>into(FixedWindows.of(Duration.standardSeconds(5)))
                        .triggering(Repeatedly.forever(AfterProcessingTime.pastFirstElementInPane()
                                .plusDelayOf(Duration.standardSeconds(5))))
                        .withAllowedLateness(Duration.ZERO, Window.ClosingBehavior.FIRE_ALWAYS)
                        .accumulatingFiredPanes())
                .apply(Group.<VoteElement>byFieldNames("vote_id", "vote").aggregateField("number_vote", Sum.ofIntegers(), "total_vote"))
                .apply(Select.<Row>fieldNames("key.vote_id", "key.vote", "value.total_vote"));
        
        voteCounts.apply("WriteToMySQL", ParDo.of(new DoFn<Row, Void>() {
            private final String url = "jdbc:mysql://localhost:3306/votes";
            private final String user = "root";
            private final String password = "AchRAf5321";

            @ProcessElement
            public void processElement(@Element Row row, OutputReceiver<Void> out) {
                try (Connection connection = DriverManager.getConnection(url, user, password)) {
                    String vote = row.getString("vote");
                    int totalVote = row.getInt32("total_vote");
                    String strTableName = "votes." + row.getString("vote_id");

                    String strSQLQuery = String.format("UPDATE %s SET %s = %s + %d",
                            strTableName, vote, vote, totalVote);

                    System.out.println(strSQLQuery);
                    try  {
                    	Statement statement = connection.createStatement();
                        statement.executeUpdate(strSQLQuery);
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }

                    // Avis : Pas nécessaire de fermer explicitement la connexion, elle sera automatiquement fermée avec le try-with-resources
                    // connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Avis : Gérer l'exception de manière appropriée, peut-être envoyer à un service de journalisation ou lancer une exception personnalisée
                }
            }
        }));



        LOG.info("Building pipeline...");

        return pipeline.run();
    }
}