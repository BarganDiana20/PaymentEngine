### Payment Engine - Payment Processing App

Implementation of a payment engine with Java Spring Boot framework, REST API and web interface, designed to securely initiate and process wallet payments, manage one or more wallets per user, and track historical transactions (credit and debit) and wallet balances in real time,  with data stored in a MySQL database.

##### The application contains: 
- 🔐 Secure user registration and authentication.
- 💼 Wallet management operations per user: create wallets, view wallet details, update balances (after transferring or receiving money between users).
- 🔄💸📜 Payment operations: initiate and process payments, view payment status, with detailed transaction history.
---
#### Project architecture: 
- 📱 **Client side**
Mobile-style web interface built with HTML, CSS, and JavaScript that allows users to authenticate, manage wallets, initiate payments, and monitor balances and transaction history. 
    - **User interface:**
        - 🏠 Home
        - 🔐 Authentication: user registration and login using username or e-mail address and password
        - 👤 User profile: full name, username, email;
        - 📊 Dashboard: displays the primary wallet balance and quick access to key actions such as new payments and wallet management;
        - <i class="fa-brands fa-amazon-pay"></i> 💸 New Payment: initiate payments from user wallets by entering the recipient’s wallet code, amount, and description, with the option to schedule the payment for a future date and time;
        - 💼 Wallets: create wallets, view all user wallets, access wallets details (name, code, current balance, status), and recent or transaction history (credit and debit);
        - 🚪<i class="fa-solid fa-arrow-right-from-bracket"></i> Logout: securely ends the session and redirects to the login page
    - **Admin interface:**
        - 🔐 Authentication: admin login;
        - 📊 Dashboard: view user transactions by username and wallet transactions by wallet code;
        - 🚪<i class="fa-solid fa-arrow-right-from-bracket"></i> Logout: securely ends the session and redirects to the login page
- 🖥️ **Server side**
Spring Boot backend exposing REST APIs and connecting to MySQL. 
    - 🔀 **Controller Layer**: exposes API endpoints, handles incoming HTTP requests, validates data, delegates business logic to the service layer, and returns responses to the client;
    - 🧠 **Service Layer**: implements business logic and application rules.
        - 📜 _User service_
        - 📜 _Wallet service_
        - 📜 _Payment service_
        - 📜 _Transaction service_
    - 🗃️ **Repository Layer:** interact with MySQL database
        - 📜 _User repository_
        - 📜 _Wallet repository_
        - 📜 _Payment repository_
        - 📜 _Transaction repository_
    - 🧩 **Models:** defines the domain entities representing the core data structures of the application (users, wallets, payments, and transactions);
    - ⚙️ **Configuration:**  application settings
        - 📜 _Security config:_ configures Spring Security for API endpoints, with authentication and authorization ready for future rules
        - 📜 _Database config:_ sets up JDBI for MySQL, including plugins, data source, and SQL query logging
    - 🔐 **Security**: 
        - 📜 _Password Encoder:_ handles password hashing and verification using BCrypt, ensuring secure storage of passwords and their correct validation during authentication.
    - 🔄 **Scheduler:** automatically processes user‑scheduled payments once their scheduled time is reached, updating wallet balances and marking payments as COMPLETED or FAILED depending on available funds.
    <br>
- 🛠️⚙️ **Unit testing:**
  - User Management & Authentication tests
  - Password Security & Hashing tests 
  - Payment processing logic tests
  - Wallet operations tests
  - Transactions handling tests

#### User Interface Flow:

<table style="width: 100%; table-layout: fixed;">
  <tr>
    <td style = "font-size: 14px; font-weight: bold;">Home → Login → Register account</td>
    <td style = "font-size: 14px; font-weight: bold;">Login→ Dashboard → Wallets → Transactions </td>
  </tr>
  <tr>
    <td><img src="src\main\resources\static\images\home-login-register.gif" style="width: 300px; max-width: 100%;"></td>
    <td><img src="src\main\resources\static\images\login-dashboard-wallets-transactions.gif" style="width: 300px; max-width: 100%;"></td>
  </tr>  
</table>


<table style="width: 100%; table-layout: fixed;">
  <tr>
    <td style = "font-size: 14px; font-weight: bold;">Dashboard → Instant payment → Wallets → Transactions</td>
    <td style = "font-size: 14px; font-weight: bold;">Dashboard → Scheduled payment → Wallets → Transactions</td>
  </tr>
  <tr>
    <td><img src="src\main\resources\static\images\login-dashboard-instantpayment-wallets-transactions.gif" style="width: 300px; max-width: 100%;"></td>
    <td><img src="src\main\resources\static\images\login-dashboard-schedulepayment-wallets-transactions.gif" style="width: 300px; max-width: 100%;"></td>
  </tr>  
</table>

<br>

#### Admin Interface Flow:
This application also includes an interface intended for a user with the role of **ADMIN**. However, in order to access it, an administrator account must first be created. The ADMIN account cannot be created from the UI, as the application is protected against situations where regular users could assign administrative privileges to themselves.

<p align="center"><img src="src\main\resources\static\images\login-dashboardAdmin.gif" style="width: 230px; max-width: 100%;"></p>

Therefore, an ADMIN user account must be created **_exclusively_** through a manual HTTP request, for example using a tool such as Postman.

🔧 _Example HTTP Request for creating a user with the ADMIN role_
```markdown id="p7t3kl"
POST:  http://localhost:8080/users/register
Content-Type: application/json

{   "firstName": "Gabriela",
    "lastName": "Ionescu",
    "username": "gabriela_ionescu",
    "email": "gabriela.ionescu@gmail.com",
    "password": "parola3345",
    "role": "ADMIN"
}
```

After sending this request, the user is created in the database with the name **payment_engine** in MySQL. 
The backend ensures that any account registered through the UI is automatically assigned the USER role, preventing unauthorized privilege escalation and keeping administrative access strictly controlled.

---
#### ⚙️ Configuration
The application requires a MySQL database connection. Before running the application, configure your local database credentials in the `application.properties` file.
