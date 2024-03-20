import java.sql.*;
import java.util.Scanner;

class VinylJDBC {
    private static final String JDBC_URL = "jdbc:db2://winter2024-comp421.cs.mcgill.ca:50000/comp421";
    private static final String YOUR_USERID = "cs421g14";
    private static final String YOUR_PASSWORD = "v1nylRecord$";

    public static void main(String[] args) {
        String tableName = (args.length > 0) ? args[0] : "exampletbl";
        Connection con = null;
        Statement statement = null;

        try {
            DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());
            con = DriverManager.getConnection(JDBC_URL, YOUR_USERID, YOUR_PASSWORD);
            statement = con.createStatement();

            System.out.println(tableName);

            boolean exit = false;
            while (!exit) {
                System.out.println("\n*** MENU ***");
                System.out.println("1. Create Table");
                System.out.println("2. Create a Customer Account");
                System.out.println("3. Search for Vinyls by Genre, Artist");
                System.out.println("4. Update Table");
                System.out.println("5. Find all transactions within the last month by Employee ID");
                System.out.println("6. Quit");

                System.out.print("\nEnter your choice: ");
                int choice = getIntInput();

                switch (choice) {
                    case 1:
                        createTable(statement, tableName);
                        break;
                    case 2:
                        System.out.println("Please enter your email:");
                        String email = getStringInput();

                        while (!email.contains("@")) {
                            System.out.println("Invalid email format. Please enter a valid email address:");
                            email = getStringInput();
                        }
                        //  while no '@' in email, redo ask

                        System.out.println("Please enter your address:");
                        String address = getStringInput();

                        System.out.println("Please enter your first name:");
                        String fName = getStringInput();

                        System.out.println("Please enter your last name:");
                        String lName = getStringInput();

                        System.out.println("Please enter your phone number:");
                        String phoneNum = getStringInput();

                        while (!phoneNum.matches("'\\d{10}'")) {
                            System.out.println("Invalid phone number format. Please enter exactly 10 digits:");
                            phoneNum = getStringInput();
                        }
                        //  only and exactly 10 digits long. no string chars. otherwise reask the question

                        System.out.println("Please enter your loyalty points:");
                        int loyaltyPoints = getIntInput();

                        while (loyaltyPoints < 0) {
                            System.out.println("Invalid loyalty points. Please enter a positive integer:");
                            loyaltyPoints = getIntInput();
                        }
//                        must be int >0

                        enterUser(statement, "Customer_Account",email,address,fName,lName,phoneNum,loyaltyPoints);
                        break;
                    case 3:
                        System.out.println("Please enter the genre name:");
                        String gName = getStringInput();
                        System.out.println("Please enter the artist name:");
                        String aName = getStringInput();
                        searchVinyl(statement, tableName, gName, aName);
                        break;
                    case 4:
                        updateTable(statement, tableName);
                        break;
                    case 5:
                        System.out.println("Please enter today's date as YYYY-MM-DD:");
                        String today = getStringInput();
                        while (!today.matches("'\\d{4}-\\d{2}-\\d{2}'")) {
                            System.out.println("Invalid date format. Please enter today's date as YYYY-MM-DD:");
                            today = getStringInput();
                        }
//                        today = today.replace("'", "\"");

                        System.out.println("Please enter the employee's ID:");
                        String eID = getStringInput();

                        findTransactions(statement, tableName, today, eID);
                        break;
                    case 6:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createTable(Statement statement, String tableName) throws SQLException {
        try {
            String createSQL = "CREATE TABLE " + tableName + " (id INTEGER, name VARCHAR (25)) ";
            System.out.println(createSQL);
            statement.executeUpdate(createSQL);
            System.out.println("Table created successfully.");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void enterUser(Statement statement, String tableName, String email, String address, String fName, String lName, String phoneNum, int loyaltyPoints) throws SQLException {
//        INSERT INTO Customer_Account (email, address, FName, LName, phoneNum, loyaltyPoints) VALUES
        try {
            String insertSQL = "INSERT INTO " + tableName + " VALUES (" + email +","+ address+","+ fName+","+ lName+","+ phoneNum + "," + loyaltyPoints + ")";
            System.out.println(insertSQL);
            statement.executeUpdate(insertSQL);
            System.out.println("Insertion successful.");

        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void searchVinyl(Statement statement, String tableName, String genreName, String artistName) throws SQLException {
        try {
        String querySQL = """
            SELECT subquery.Title, subquery.ReleaseDate, subquery.PlaybackSpeed, subquery.pID
            FROM (
                SELECT va.Title, va.ReleaseDate, va.PlaybackSpeed, va.pID
                FROM Vinyl_Album va
                JOIN performedBy pb ON va.pID = pb.pID
                JOIN Artist a ON pb.aID = a.aID
                WHERE va.GenreName = """ + genreName + """
            ) AS subquery
            WHERE subquery.Title IN (
                SELECT va.Title
                FROM Vinyl_Album va
                JOIN performedBy pb ON va.pID = pb.pID
                JOIN Artist a ON pb.aID = a.aID
                WHERE a.Name = """ + artistName + ");";

//            System.out.println(querySQL);
            ResultSet rs = statement.executeQuery(querySQL);

            System.out.println("Query successful.");

            boolean foundResults = false;

            while (rs.next()) {
                foundResults = true;
                String title = rs.getString("Title");
                String rd = rs.getString("ReleaseDate");
                String pid = rs.getString("pID");
                int ps = rs.getInt("PlaybackSpeed");
                System.out.println("Title: " + title + " -- Release Date: " + rd + " -- PlaybackSpeed: " + ps + " -- pID: " + pid);
            }

            if (!foundResults) {
                System.out.println("No results found! :(");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void updateTable(Statement statement, String tableName) throws SQLException {
        try {
            String updateSQL = "UPDATE " + tableName + " SET NAME = 'Mimi' WHERE id = 3";
            System.out.println(updateSQL);
            statement.executeUpdate(updateSQL);
            System.out.println("Update successful.");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void findTransactions(Statement statement, String tableName, String today, String eID) throws SQLException {
        try {
//            String findTs = """
//            SELECT t.*, e.FName, e.LName
//            FROM Transaction t
//            JOIN completed c ON t.referenceNum = c.referenceNum
//            JOIN Employee e ON c.eID = e.eID
//            WHERE t.date >=DATE(""" + today +
//            """
//            ) - 30 DAYS
//            AND t.date <= """ + today +
//            " AND c.eID =" + eID + ";";
            String findTs= """
            SELECT t.*, e.FName, e.LName
            FROM Transaction t
            JOIN completed c ON t.referenceNum = c.referenceNum
            JOIN Employee e ON c.eID = e.eID
            WHERE t.date >= DATE(""" + today + """
            ) - 30 DAYS
            AND t.date <= DATE(""" + today + """
            )
            AND c.eID =""" +eID +";"
            ;

//            System.out.println(findTs);

            ResultSet rs = statement.executeQuery(findTs);

            System.out.println("Query successful.");

            boolean foundResults = false;

//            System.out.println(rs);

            while (rs.next()) {
                int ref = rs.getInt("referenceNum");
                float tot = rs.getFloat("total");
                String total= String.format("%.2f", tot);
                String date = rs.getString("date");
                String time = rs.getString("time");
                String loc = rs.getString("location");
                String efn = rs.getString("FName");
                String eln = rs.getString("LName");
                if (!foundResults){
                    System.out.println("Last month's transactions by employee " + efn + " " + eln + ":" );
                }
                foundResults = true;
                System.out.println("Transaction Reference Number: " + ref + " -- Total: $" + total + " -- Date: " + date + " -- Time: " + time + " -- Location: " + loc);
            }

            if (!foundResults) {
                System.out.println("No results found! :(");
            }


        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

// Other parts of your code...

    private static Scanner scanner = new Scanner(System.in);



    private static String getStringInput() {
        String input = "";
        try {
            input = scanner.nextLine().trim(); // Trim leading and trailing whitespace
        } catch (Exception e) {
            System.out.println("An error occurred while reading input.");
        }
        return "'" + input + "'";
    }


    private static int getIntInput() {
        int choice = -1;
        try {
            String input = scanner.nextLine().trim(); // Trim leading and trailing whitespace
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
        return choice;
    }
    private static void handleSQLException(SQLException e) {
        int sqlCode = e.getErrorCode();
        String sqlState = e.getSQLState();
        System.out.println("Error Code: " + sqlCode + "  sqlState: " + sqlState);
        System.out.println(e);
    }
}