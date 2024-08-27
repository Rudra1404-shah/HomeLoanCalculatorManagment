import java.sql.*;
import java.util.*;



interface LoanCalculator {
    double calculateMonthlyPayment(double loanAmount, double interestRate, int tenureYears);
    double calculateTotalInterest(double loanAmount, double interestRate, int tenureYears);
    List<String> generateAmortizationSchedule(double loanAmount, double interestRate, int tenureYears);
}

class LoanDetails {
    int loanId;
    double loanAmount;
    double interestRate;
    int tenureYears;
    double monthlyPayment;
    double totalInterest;

    public LoanDetails(int loanId, double loanAmount, double interestRate, int tenureYears) {
        this.loanId = loanId;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.tenureYears = tenureYears;
    }
}

class BorrowerDetails {
    int borrowerId;
    String borrowerName;
    String email;
    String phoneNumber;

    public BorrowerDetails(int borrowerId, String borrowerName, String email, String phoneNumber) {
        this.borrowerId = borrowerId;
        this.borrowerName = borrowerName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}

class Node {
    LoanDetails data;
    Node next;

    public Node(LoanDetails data) {
        this.data = data;
        this.next = null;
    }
}

class LoanManagement {
    private Node head;

    public void addLoan(LoanDetails loan) {
        Node newNode = new Node(loan);
        if (head == null) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
    }

    public List<LoanDetails> getAllLoans() {
        List<LoanDetails> loans = new ArrayList<>();
        Node current = head;
        while (current != null) {
            loans.add(current.data);
            current = current.next;
        }
        return loans;
    }

    public LoanDetails getLoanById(int loanId) {
        Node current = head;
        while (current != null) {
            if (current.data.loanId == loanId) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }
}

class HomeLoanCalculator implements LoanCalculator {
    @Override
    public double calculateMonthlyPayment(double loanAmount, double interestRate, int tenureYears) {
        double monthlyRate = interestRate / 12 / 100;
        int tenureMonths = tenureYears * 12;
        return (loanAmount * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -tenureMonths));
    }

    @Override
    public double calculateTotalInterest(double loanAmount, double interestRate, int tenureYears) {
        double monthlyPayment = calculateMonthlyPayment(loanAmount, interestRate, tenureYears);
        return (monthlyPayment * tenureYears * 12) - loanAmount;
    }

    @Override
    public List<String> generateAmortizationSchedule(double loanAmount, double interestRate, int tenureYears) {
        List<String> schedule = new ArrayList<>();
        double monthlyPayment = calculateMonthlyPayment(loanAmount, interestRate, tenureYears);
        double balance = loanAmount;
        double monthlyRate = interestRate / 12 / 100;

        for (int i = 1; i <= tenureYears * 12; i++) {
            double interest = balance * monthlyRate;
            double principal = monthlyPayment - interest;
            balance -= principal;
            schedule.add(String.format("Month %d: Principal: %.2f, Interest: %.2f, Balance: %.2f", i, principal, interest, balance));
        }

        return schedule;
    }
}

class DatabaseManager {
    Scanner sc=new Scanner(System.in);
    static LoanManagement loanManagement = new LoanManagement();
    static HomeLoanCalculator calculator = new HomeLoanCalculator();
    static DatabaseManager dbManager = new DatabaseManager();
    private Connection connect() throws SQLException, ClassNotFoundException {
        String driver="com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://localhost:3306/homeloandb";
        String dbuser="root";
        String dbpass="";
        return DriverManager.getConnection(url,dbuser,dbpass);
    }

    public void createTables() throws SQLException, ClassNotFoundException {
        String createLoansTable = "CREATE TABLE IF NOT EXISTS Loans (" +
                "loan_id INTEGER  PRIMARY KEY ," +
                "loan_amount DECIMAL," +
                "interest_rate DECIMAL," +
                "tenure_years INTEGER," +
                "monthly_payment DECIMAL," +
                "total_interest DECIMAL)";
        String createCustomerTable = "CREATE TABLE IF NOT EXISTS Customer (" +
                "customer_id INTEGER AUTO_INCREMENT PRIMARY KEY," +
                "customer_name VARCHAR(50)," +
                "email VARCHAR(50)," +
                "phone_number VARCHAR(50))";

        String createadminTable="CREATE TABLE IF NOT EXISTS User ("+        
        "id INT  AUTO_INCREMENT PRIMARY KEY,"   +    
        "name VARCHAR(50) NOT NULL,"+
               "password VARCHAR(4) NOT NULL UNIQUE)";      
          
                      

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createLoansTable);
            stmt.execute(createCustomerTable);
            stmt.execute(createadminTable);
        }
    }
    public int checkLoanId(String name,String pass) throws SQLException, ClassNotFoundException
    {
        String sql="Select*from User where name=? and password=?";
        int loanId=0;
        try(Connection conn=connect();
        PreparedStatement pst=conn.prepareStatement(sql))
        {
            pst.setString(1, name);
            pst.setString(2, pass);
            ResultSet rs=pst.executeQuery();
            if (rs.next()) {
                loanId=rs.getInt(1);
            }
        }   
        return loanId;
    }
    public int checkBorrowerId() throws SQLException, ClassNotFoundException
    {
        int borrowerId =0;
        String sqll="Select * from Customer";
        try(Connection conn=connect();
        PreparedStatement pst=conn.prepareStatement(sqll))
        {
            ResultSet rs=pst.executeQuery();
            while (rs.next()) {
                borrowerId=rs.getInt("customer_id");
            }
        }
        return borrowerId;
    }

    public void insertLoan(int loan_id,double loanAmount,double interestRate,int tenureYears,double monthlyPayment,double totalInterest) throws SQLException, ClassNotFoundException {
        String insertLoan = "INSERT INTO Loans  VALUES (?,?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertLoan)) {
                pstmt.setInt(1, loan_id);
            pstmt.setDouble(2, loanAmount);
            pstmt.setDouble(3, interestRate);
            pstmt.setInt(4, tenureYears);
            pstmt.setDouble(5, monthlyPayment);
            pstmt.setDouble(6, totalInterest);
            pstmt.executeUpdate();
        }
    }

    public void insertBorrower(String borrowerName,String email,String phoneNumber) throws SQLException, ClassNotFoundException {
        String insertCustomer = "INSERT INTO Customer (customer_name, email, phone_number) VALUES ( ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertCustomer)) {
            pstmt.setString(1, borrowerName);
            pstmt.setString(2,email);
            pstmt.setString(3, phoneNumber);
            pstmt.executeUpdate();
        }
    }
    public LoanDetails getLoanById(int loanId) throws SQLException, ClassNotFoundException {
        String query = "SELECT * FROM Loans WHERE loan_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, loanId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                LoanDetails loan = new LoanDetails(
                        rs.getInt("loan_id"),
                        rs.getDouble("loan_amount"),
                        rs.getDouble("interest_rate"),
                        rs.getInt("tenure_years")
                );
                loan.monthlyPayment = rs.getDouble("monthly_payment");
                loan.totalInterest = rs.getDouble("total_interest");
                return loan;
            }
        }
        return null;
    }

    public void registerAdmin() throws ClassNotFoundException, SQLException
    {
        //changes made
        String sql="INSERT INTO user(name,password) VALUES(?,?)";
        try(Connection conn=connect())
             {
               System.out.println("---Enter Admin Details---");
                        System.out.println("Enter Admin Name");
                        String name=sc.nextLine();
                        
                        System.out.println("Enter Admin Password (4 Digits Only)");
                        String pass=sc.nextLine();
                        PreparedStatement pst1=conn.prepareStatement(sql);
                        pst1.setString(1, name);
                        pst1.setString(2, pass);
                        // pst1.setInt(3, 1);v changes madfe
                        int r=pst1.executeUpdate();
                        if (r>0) {
                            System.out.println("---Registration Completed---");
                            mainInterface();
                        }
                    }
    }

    public void registerUser() throws ClassNotFoundException, SQLException
    {
        String sql="INSERT INTO user(name,password) VALUES(?,?)";
        try(Connection conn=connect();
             PreparedStatement pst1=conn.prepareStatement(sql))
             {
               System.out.println("---Enter User Details---");
                        System.out.println("Enter User Name");
                        String name=sc.next();
                
                        System.out.println("Enter User Password");
                        String pass=sc.next();
                       
                        System.out.print("Enter Email: ");
                        String email = sc.next();
                        System.out.print("Enter Phone Number: ");
                        String phoneNumber = sc.next();
                        for (int i = 0; i < phoneNumber.length(); i++) {
                            char ch = phoneNumber.charAt(i);
                            if (ch < '0' || ch > '9') {
                                 System.out.println("You phone number is not Valid !!");
                                 return;
                            }
                        }
                        try {
                            dbManager.insertBorrower(name,email,phoneNumber);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        int borrowerId = dbManager.checkBorrowerId();
                        BorrowerDetails borrower = new BorrowerDetails(borrowerId, name, email, phoneNumber);
                        pst1.setString(1, name);
                        pst1.setString(2, pass);
                        int r=pst1.executeUpdate();
                        if (r>0) {
                            System.out.println("---Registration Completed---");
                            mainInterface();
                        }
                    }
    }

    void login() throws ClassNotFoundException {
        System.out.println("---Welcome to Login Page---");
        System.out.println("Please Enter Login Details-->");
        System.out.print("Enter username: ");
        String username = sc.next();
        System.out.print("Enter password: ");
        String password = sc.next();
        String query = "SELECT * FROM user WHERE name = ? AND password = ?";

        try(Connection conn=connect();
        PreparedStatement preparedStatement = conn.prepareStatement(query);) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int check=resultSet.getInt(1);
                if (check==1) {
                    runAdminInterface();
                }
                else 
                {
                    runUserInterface(username,password);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error logging in: " + e.getMessage());
        }
    }
    public void mainInterface() throws ClassNotFoundException, SQLException
    {
        

        System.out.println("---Welcome to Home Loan Interest Calculation System---");
        System.out.println("1] Login");
        System.out.println("2] Register");
        System.out.println("3] Exit");
        System.out.print("Enter your choice: ");
        int choice = sc.nextInt();
        sc.nextLine();//change made
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 3:
                System.out.println("Thanks For Visiting Our System !!");
                break;
            default:
                System.out.println("Invalid choice. Exiting...");
                break;
        }
    }

    public void register() throws ClassNotFoundException, SQLException
    {
        int choice;
        System.out.println("---Welcome to Registration Page---");
                System.out.println("Enter your choice");
                System.out.println("1] Register as a Admin");
                System.out.println("2] Register as User");
                choice=sc.nextInt();
                sc.nextLine();//change made

                if(choice==1)
                {
                    registerAdmin();
                }
                else if(choice==2)
                {
                    registerUser();
                }
                else
                {
                    mainInterface();
                }
            
    }
    public void runAdminInterface() throws ClassNotFoundException, SQLException {
        int choice;
        do {
            System.out.println("--Admin Page--");
            System.out.println("1] View recent Loan Calculation History");
            System.out.println("2] Remove Loan");
            System.out.println("3] View All Loans from Database");
            System.out.println("4] LogOut");
            System.out.println("Enter your Choice");
            choice = sc.nextInt();
            switch (choice) {
                case 1:
                    List<LoanDetails> allLoans = loanManagement.getAllLoans();
                    if (!allLoans.isEmpty()) {
                        for (LoanDetails l : allLoans) {
                            System.out.println("---Displaying recent Loan Calculation History---");
                            System.out.println("Loan ID: " + l.loanId);
                            System.out.println("Loan Amount: " + l.loanAmount);
                            System.out.println("Interest Rate: " + l.interestRate);
                            System.out.println("Tenure (years): " + l.tenureYears);
                            System.out.println("Monthly Payment: " + l.monthlyPayment);
                            System.out.println("Total Interest: " + l.totalInterest);
                            System.out.println("Total Amount to pay :" + (l.loanAmount + l.totalInterest));
                            System.out.println("----------------------------");
                        }
                    } else {
                        System.out.println("No loans found.");
                    }
                    break;
                case 2:
                    removeUser();
                    break;
                case 3:
                    printAllLoans();
                    break;
                case 4:
                    System.out.println("---Logging Out From Admin Page---");
                    mainInterface();
                    break;
                default:
                    System.out.println("Invalid Input");
                    break;
            }
        } while (choice != 4);
    }

    public void printAllLoans() throws SQLException, ClassNotFoundException {
        String procedureCall = "{CALL GetAllLoans()}";
        try (Connection conn = connect();
             CallableStatement cstmt = conn.prepareCall(procedureCall)) {
            
            ResultSet rs = cstmt.executeQuery();
            System.out.println("--- All Loan Details ---");
            while (rs.next()) {
                System.out.println("Loan ID: " + rs.getInt("loan_id"));
                System.out.println("Loan Amount: " + rs.getDouble("loan_amount"));
                System.out.println("Interest Rate: " + rs.getDouble("interest_rate"));
                System.out.println("Tenure (years): " + rs.getInt("tenure_years"));
                System.out.println("Monthly Payment: " + rs.getDouble("monthly_payment"));
                System.out.println("Total Interest: " + rs.getDouble("total_interest"));
                System.out.println("------------------------");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving loans: " + e.getMessage());
        }
    }
    
    public void runUserInterface(String username,String pass) throws ClassNotFoundException, SQLException
    {
        int choice;
        do {
            System.out.println("---User Page---");
            System.out.println("1. Calculate home loan Interest");
            System.out.println("2. Search LoanCalculation  by ID");
            System.out.println("3. LogOut");
            choice=sc.nextInt();
            switch (choice) {
                case 1:
                double totalAmountPay;

                System.out.println("---Enter Loan Details---");
                System.out.print("Enter Loan Amount: ");
                double loanAmount = sc.nextDouble();
                System.out.print("Enter Interest Rate: ");
                double interestRate = sc.nextDouble();
                System.out.print("Enter Tenure (years): ");
                int tenureYears = sc.nextInt();

                double monthlyPayment = calculator.calculateMonthlyPayment(loanAmount, interestRate, tenureYears);
                double totalInterest = calculator.calculateTotalInterest(loanAmount, interestRate, tenureYears);
                List<String> amortizationSchedule = calculator.generateAmortizationSchedule(loanAmount, interestRate, tenureYears);

               
                int loanId = dbManager.checkLoanId( username,pass);
                try {
                    dbManager.insertLoan(loanId,loanAmount,interestRate,tenureYears,monthlyPayment,totalInterest);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                LoanDetails loan = new LoanDetails(loanId, loanAmount, interestRate, tenureYears);

                loan.monthlyPayment = monthlyPayment;
                loan.totalInterest = totalInterest;
                loanManagement.addLoan(loan);
                System.out.println("---Calculations---");
                System.out.println("Monthly Payment: " + monthlyPayment);
                System.out.println("Total Interest: " + totalInterest);
                totalAmountPay=loanAmount+totalInterest;
                System.out.println("---Amortization Schedule---");
                for (String entry : amortizationSchedule) {
                    System.out.println(entry);
                }
                System.out.println("---------------------------");
                System.out.println("Total Amount to repay :"+totalAmountPay);
                dbManager.getUserDetails(loanId);
                System.out.println("---Calculations Completed---");
                break;
        
                case 2:
                 System.out.print("Enter Loan ID to search:- ");
                int searchLoanId = sc.nextInt();
                LoanDetails searchedLoan = null;
                try {
                    searchedLoan = dbManager.getLoanById(searchLoanId);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (searchedLoan != null) {
                    System.out.println("---Loan Details---");
                    System.out.println("Loan ID: " + searchedLoan.loanId);
                    System.out.println("Loan Amount: " + searchedLoan.loanAmount);
                    System.out.println("Interest Rate: " + searchedLoan.interestRate);
                    System.out.println("Tenure (years): " + searchedLoan.tenureYears);
                    System.out.println("Monthly Payment: " + searchedLoan.monthlyPayment);
                    System.out.println("Total Interest: " + searchedLoan.totalInterest);
                    System.out.println("Total Amount to Pay :"+searchedLoan.loanAmount+searchedLoan.totalInterest);
                    System.out.println("------------------");
                } else {
                    System.out.println("Loan with ID " + searchLoanId + " not found.");
                }
                break;

                case 3:System.out.println("Logging Out");
                mainInterface();
                break;
                default:
                    break;
            }
        } while (choice!=3);
    }

    public void getUserDetails(int id) throws ClassNotFoundException, SQLException
    {
        String sql="Select*from loans where loan_id=?";
        String sql1="Select*from customer where customer_id=?";
        try(Connection conn=connect();
            PreparedStatement pst=conn.prepareStatement(sql))
            {
                PreparedStatement pst1=conn.prepareStatement(sql1);
                pst.setInt(1, id);
                pst1.setInt(1, id);
                ResultSet rs=pst.executeQuery();
                ResultSet rs1=pst1.executeQuery();
                if (rs.next()) {
                    System.out.println("------------");
                    System.out.println("Your Loan ID :"+rs.getInt(1));
                    if (rs1.next()) {
                        System.out.println("Your Customer ID :"+rs1.getInt(1));
                    }
                }
            }
    }
    public void removeUser() throws SQLException, ClassNotFoundException
    {
        String sql="DELETE FROM LOANS WHERE LOAN_ID=?";
        String sql1="DELETE FROM Customer WHERE customer_ID=?";
        try(Connection conn=connect();
            PreparedStatement pst=conn.prepareStatement(sql))
            {
                System.out.println("---Enter details to Remove User---");
                            System.out.println("Enter loan id");
                            int idl=sc.nextInt();
                            System.out.println("Enter Customer id");
                            int idb=sc.nextInt();
                            if(idl==idb)
                            {
                                pst.setInt(1, idl);
                                PreparedStatement pst1=conn.prepareStatement(sql1);
                                pst1.setInt(1, idb);
                                conn.setAutoCommit(false);
                                System.out.println("Do you want to Remove User ?yes or no");
                                String ask=sc.next();
                                if (ask.equalsIgnoreCase("yes")) {
                                    pst.executeUpdate();
                                    pst1.executeUpdate();
                                    System.out.println("----------");
                                    conn.commit();   
                                }                   
                                else
                                {
                                    conn.rollback();
                                }            
                                      
                            }
                            else
                            {
                                System.out.println("Loan Id != Customer Id");
                            }
                        }
    }
}

public class HomeLoanInterestCalculationSystem {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        DatabaseManager dbManager = new DatabaseManager();
        try {
            dbManager.createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dbManager.mainInterface();
    }
}
