import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

public class Application {

    private static final String MONGO_DB_URL =
        "mongodb://127.0.0.1:27017,127.0.0.1:27018,127.0.0.1:27019/?replicaSet=rs0";
    private static final String DB_NAME = "online-school";
    private static final double MIN_GPA = 90.0;

    public static void main(String[] args) {
        String courseName = args[0];
        String studentName = args[1];
        int age = Integer.parseInt(args[2]);
        double gpa = Double.parseDouble(args[3]);

        MongoDatabase onlineSchool = connectToMongoDB(MONGO_DB_URL, DB_NAME);

        enroll(onlineSchool, courseName, studentName, age, gpa);
    }

    private static void enroll(MongoDatabase database,
                               String courseName,
                               String studentName,
                               int age,
                               double gpa) {
        if (!isValidCourse(database, courseName)) {
            System.out.println("Invalid course" + courseName);
            return;
        }

        MongoCollection<Document> courseCollection = database.getCollection(courseName)
            .withWriteConcern(WriteConcern.MAJORITY)
            .withReadPreference(ReadPreference.primaryPreferred());

        if (courseCollection.find(eq("name", studentName)).first() != null) {
            System.out.println("Student " + studentName + " already enrolled");
            return;
        }

        if (gpa < MIN_GPA) {
            System.out.println("Please improve your grades");
            return;
        }

        courseCollection.insertOne(new Document("name", studentName)
            .append("age", age)
            .append("gpa", gpa));

        System.out.println("Student " + studentName + " was successfully enrolled in " + courseName);

        for (Document document : courseCollection.find()) {
            System.out.println(document);
        }

    }

    private static boolean isValidCourse(MongoDatabase database, String courseName) {
        for (String collection : database.listCollectionNames()) {
            if(collection.equals(courseName)) {
                return true;
            }
        }
        return false;
    }

    public static MongoDatabase connectToMongoDB(String url, String dbName) {
        MongoClient mongoClient = new MongoClient(new MongoClientURI(url));
        return mongoClient.getDatabase(dbName);
    }
}
