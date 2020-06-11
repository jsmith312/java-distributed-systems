/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Launching a Sharded Distributed MongoDB
 */
public class MoviesGenerator {
    private static final String MONGO_DB_URL = "mongodb://127.0.0.1:27023";
    private static final String DB_NAME = "videodb";
    private static final String COLLECTION_NAME = "movies";

    private static final Random random = new Random();

    public static void main(String[] args) {
        MongoDatabase onlineSchoolDb = connectToMongoDB(MONGO_DB_URL, DB_NAME);
        generateMovies(10000, onlineSchoolDb, COLLECTION_NAME);
    }

    private static MongoDatabase connectToMongoDB(String url, String dbName) {
        MongoClient mongoClient = new MongoClient(new MongoClientURI(url));
        return mongoClient.getDatabase(dbName);
    }

    private static void generateMovies(int numberOfMovies, MongoDatabase database, String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);

        List<Document> documents = new ArrayList<>();
        System.out.println("Generating " + numberOfMovies + " movies");
        for (int movieIndex = 0; movieIndex < numberOfMovies; movieIndex++) {
            Document document = new Document();
            document.append("name", generateName())
                    .append("directors", generateDirectorNames())
                    .append("rating", generateRating())
                    .append("cast", generateCast());
            documents.add(document);
        }

        collection.insertMany(documents);

        System.out.println("Finished generating movies");
    }

    /**
     * Returns a list of random movie directors
     */
    private static List<String> generateDirectorNames() {
        int numberOfDirectors = random.nextInt(3) + 1;
        List<String> directors = new ArrayList<>(numberOfDirectors);

        for (int i = 0; i < numberOfDirectors; i++) {
            String firstName = generateName();
            String lastName = generateName();
            directors.add(firstName + " " + lastName);
        }

        return directors;
    }

    /**
     * Returns a random year
     */
    private static int generateYear() {
        return random.nextInt(119) + 1900;
    }

    /**
     * Returns a random rating
     */
    private static float generateRating() {
        return random.nextFloat() * 10.0f;
    }

    /**
     * Returns a list of random actors
     */
    private static List<String> generateCast() {
        int numberOfActors = random.nextInt(20) + 10;
        List<String> actors = new ArrayList<>(numberOfActors);

        for (int i = 0; i < numberOfActors; i++) {
            String firstName = generateName();
            String lastName = generateName();
            actors.add(firstName + " " + lastName);
        }

        return actors;
    }

    /**
     * Returns a random full name
     */
    private static String generateName() {
        StringBuilder name = new StringBuilder();

        name.append(RandomStringUtils.randomAlphabetic(1).toUpperCase());
        name.append(RandomStringUtils.randomAlphabetic(5, 10).toLowerCase());

        return name.toString();
    }
}
