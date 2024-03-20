import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalTime;

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
                System.out.println("1. Enter a New Transaction");
                System.out.println("2. Create a Customer Account");
                System.out.println("3. Search for Vinyls by Genre, Artist");
                System.out.println("4. Add Discount to Products of a Specific Distributor");
                System.out.println("5. Find all transactions within the last month by Employee ID");
                System.out.println("6. Quit");

                System.out.print("\nEnter your choice: ");
                int choice = getIntInput();

                switch (choice) {
                    case 1:
                        //get the required fields for a transaction

                        System.out.println("Enter transaction total: ");
                        double total = getDoubleInput();

                        //get location
                        System.out.println("Please enter store location of transaction: ");
                        String location = getStringInput();

                        //create new row in transaction
                        int refNum = addTransaction(statement, total, location);

                        //after transaction is added need to update related tables
                        //create transaction items
                        System.out.println("We will now add the transaction items that make up the transaction.");
                        System.out.println("Please enter the pID of the first type of product purchased: ");
                        String pID = getStringInput();

                        //check that the pID exists
                        if(pidExists(statement, pID)){
                            //ask user for number of product purchased
                            System.out.println("Please enter the amount of the product purchased");
                            int quantity = getIntInput();
                            if(quantity < 0){
                                System.out.println("Invalid quantity entered. Please enter positive integer only.");
                                break;
                            }

                            //now update the 'is' table and the 'transaction item' table
                            updateTransactionItem(statement, pID, quantity, refNum);

                            System.out.println("Would you like to add another transaction item to the transaction? (Y/N)");
                            String yesNO = getStringInput();

                            while(yesNO.equals("'Y'")){
                                System.out.println("Please enter the pID of the next type of product purchased: ");
                                pID = getStringInput();

                                //check that the pID exists
                                if(!pidExists(statement, pID)){
                                    System.out.println("pID does not exist.");
                                    break;
                                }
                                //ask user for number of product purchased
                                System.out.println("Please enter the amount of the product purchased");
                                quantity = getIntInput();
                                if(quantity < 0){
                                    System.out.println("Invalid quantity entered. Please enter positive integer only.");
                                    break;
                                }

                                //now update the 'is' table and the 'transaction item' table
                                updateTransactionItem(statement, pID, quantity, refNum);

                                System.out.println("Would you like to add another transaction item to the transaction? (Y/N)");
                                yesNO = getStringInput();
                            }

                            if (yesNO.equals("'N'")){
                                System.out.println("Please enter the employee ID that completed the transaction.");
                                String eID = getStringInput();
                                if(!employeeExists(statement, eID)){
                                    //if employee does not exist
                                    break;
                                }
                                //otherwise add to completed table
                                addCompletedEmp(statement, refNum, eID);

                                System.out.println("Transaction added successfully!");


                            } else {
                                System.out.println("Not a valid input.");
                                break;
                            }
                        }

                        //update contains

                        //update completed (ask which employee did the transaction)

                        //ask if customer has customer account

                        //create new transaction method and update paidWith
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
                        System.out.println("Please select the distributor whose products you wish to add a discount to:");
                        //print the valid distributors
                        ResultSet distributors = getDistributors(statement);
                        if(distributors == null){
                            //error
                            break;
                        }
                        int i = 1;
                        //store the dIDs into a list to be able to index it
                        ArrayList<String> dIDs = new ArrayList<>();
                        while(distributors.next()){
                            String id = distributors.getString("dID");
                            String name = distributors.getString("Name");
                            dIDs.add(id);
                            System.out.println(i + ") ID: " + id + ", Name: " + name);
                            i++;
                        }

                        //get user input
                        int distChoice = getIntInput();

                        if(distChoice < 1 || distChoice >= i){
                            System.out.println("That is an invalid choice.");
                            break;
                        }

                        //otherwise it is okay
                        //now ask user for discount
                        System.out.println("Please enter the discount to apply to the products as a percentage (Please enter an integer from 0 to 100):");
                        int discount = getIntInput();
                        if (discount < 0 || discount >100){
                            System.out.println("Invalid discount value");
                            break;
                        }

                        //otherwise it is fine
                        //now we need to update all rows in the product table
                        updateDiscount(statement, (double) discount / 100, dIDs.get(distChoice-1));
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

    private static void addCompletedEmp(Statement statement, int refNum, String eID){
        String query = "INSERT INTO completed VALUES (%d, %s);";
        query = String.format(query, refNum, eID);

        try {
            statement.executeUpdate(query);
        } catch (Exception e) {
            System.out.println("Error: failed to add to 'Completed' table");
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

    private static ResultSet getDistributors(Statement statement){
        String query = "SELECT * FROM Distributor;";
        try {
            ResultSet rs = statement.executeQuery(query);
            return rs;
        } catch (Exception e){
            System.out.println("Error: execution of query failed.");
        }
        return null;
    }

    private static void updateDiscount(Statement statement, double discount, String dID){
        String query = """
                UPDATE Product 
                SET Discount = 
                    (CASE 
                        WHEN Discount + %.2f > 1 THEN 1 
                        ELSE Discount + %.2f 
                     END)
                WHERE pID IN (SELECT pID FROM distributedBy WHERE dID = '%s');
                """;

        //inject values
        query = String.format(query, discount, discount, dID);


        try{
            statement.executeUpdate(query);
            System.out.println("The discount has successfully been applied");
        } catch (Exception e){
            //e.printStackTrace();
            System.out.println("Error: SQL query failed to execute to update discount.");
        }
    }

    private static int addTransaction(Statement statement, double total, String location){
        //get date and time for the transaction
        LocalDate txnDate = LocalDate.now();
        LocalTime txnTime = LocalTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        String date = txnDate.format(dateFormatter);
        String time = txnTime.format(timeFormatter);

        // Create a Random object to create unique reference number
        Random random = new Random();
        // Generate a random 10-digit integer
        int referenceNum = random.nextInt(9000) + 1000;
        String query = "INSERT INTO Transaction VALUES (%d, %.2f, '%s', '%s', %s);";
        query = String.format(query, referenceNum, total, date, time, location);

        try {
            statement.executeUpdate(query);
        } catch (Exception e) {
            System.out.println("Error: failed to add transaction to Transaction table");
        }

        return referenceNum;
    }

    private static boolean employeeExists(Statement statement, String eID){
        String query = ("SELECT * FROM Employee WHERE eID = %s;");
        query = String.format(query, eID);
        try {
            ResultSet rs = statement.executeQuery(query);
            if(!rs.next()){
                //then there are no rows so pID provided does not exist
                System.out.println("Employee does not exist.");
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: failed to query pIDs in Product table");
            return false;
        }
    }

    private static boolean pidExists(Statement statement, String pid){
        String query = ("SELECT * FROM Product WHERE pID = %s;");
        query = String.format(query, pid);
        try {
            ResultSet rs = statement.executeQuery(query);
            if(!rs.next()){
                System.out.println("pID does not exist.");
                //then there are no rows so pID provided does not exist
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: failed to query pIDs in Product table");
            return false;
        }
    }

    private static void updateTransactionItem(Statement statement, String pID, int quantity, int refNum){
        //get a random tID
        Random random = new Random();
        // Generate a random 4-digit integer
        int tID = random.nextInt(9000) + 1000;

        //first create a new transaction item row
        String query1 = "INSERT INTO Transaction_Item VALUES ('%s', %d)";
        query1 = String.format(query1, String.valueOf(tID), quantity);

        try {
            statement.executeUpdate(query1);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: failed to add transaction item.");
            return;
        }

        //now we need to update the IS table
        String query2 = "INSERT INTO is VALUES('%s', %s);";
        query2 = String.format(query2, tID, pID);
        try {
            statement.executeUpdate(query2);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: failed to add to 'is' table.");
            return;
        }

        //now update the Contains table
        String query3 = "INSERT INTO Contains VALUES('%s', %d);";
        query3 = String.format(query3, tID, refNum);
        try {
            statement.executeUpdate(query3);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: failed to add to 'Contains' table.");
            return;
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

    private static double getDoubleInput() {
        double choice = -1.0; // Initialize choice with a default value
        try {
            String input = scanner.nextLine().trim(); // Trim leading and trailing whitespace
            choice = Double.parseDouble(input);
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