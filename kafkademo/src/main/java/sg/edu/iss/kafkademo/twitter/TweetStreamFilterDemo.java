package sg.edu.iss.kafkademo.twitter;

import java.util.Properties;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import com.google.gson.JsonParser;

import sg.edu.iss.kafkademo.util.KafkaConstants;

public class TweetStreamFilterDemo {

	public static void main(String[] args) {
		// create properties
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.BOOTSTRAPSERVERS);
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "demo-kafka-streams");
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, KafkaConstants.KSERIALIZER);
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaConstants.KSERIALIZER);

        // create a topology
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // input topic
        KStream<String, String> inputTopic = streamsBuilder.stream("twitter_tweets");
        KStream<String, String> filteredStream = inputTopic.filter(
                // filter for tweets which has a user of over 10000 followers
                (k, jsonTweet) ->  extractUserFollowersInTweet(jsonTweet) > 10000
        );
        filteredStream.to("important_tweets");

        // build the topology
        KafkaStreams kafkaStreams = new KafkaStreams(
                streamsBuilder.build(),
                properties
        );

        // start our streams application
        kafkaStreams.start();
    }

    private static JsonParser jsonParser = new JsonParser();

    private static Integer extractUserFollowersInTweet(String tweetJson){
        // gson library
        try {
            return jsonParser.parse(tweetJson)
                    .getAsJsonObject()
                    .get("user")
                    .getAsJsonObject()
                    .get("followers_count")
                    .getAsInt();
        }
        catch (NullPointerException e){
            return 0;
        }


	}

}
