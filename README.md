# Sample Android Application Integrated with WSO2 Identity Server (WSO2 IS)

This repository contains a sample Android application that integrates with WSO2 Identity Server (WSO2 IS) for authentication using OAuth 2.0 Authorization Code Flow. It demonstrates a two-step login flow with username/password and passkey verification, enabling secure and seamless access to protected resources.

---

## Steps to Configure and Build the Application

### 1. Register the Mobile Application in WSO2 IS
To enable authentication, you must register your mobile application with WSO2 Identity Server:

1. **Log in to WSO2 IS Admin Portal**:  
   Access the WSO2 IS management console and navigate to the **Service Providers** section.

2. **Register the Application**:  
   - Provide a **Name** for your application.  
   - Add the following under **Authorized Redirect URIs**:
     ```
     com.example.myapplication://oauth
     ```
   - Click **Register** to complete the setup.

3. **Configure Login Flow**:  
   Define the multi-step authentication flow as follows:  
   - **Step 1**: Username and Password.  
   - **Step 2**: Passkey (e.g., an OTP or other form of second-factor authentication).

4. **Copy the Client ID and Client Secret**:  
   Note down the **Client ID** and **Client Secret** generated during registration. These values are required for application code updates.

---

### 2. Update the Mobile Application Code

After configuring the application in WSO2 IS, update the following files in the Android application source code:

#### **`MainActivity.java`**
1. Locate the `AuthorizeListener` class.
2. Update these variables:
   - **`clientId`**: Add the Client ID generated during the WSO2 IS registration.
   - **`authEndpoint`**: Provide the WSO2 IS authorization endpoint (e.g., `https://<your-wso2-is-domain>/oauth2/authorize`).
   - **`tokenEndpoint`**: Provide the WSO2 IS token endpoint (e.g., `https://<your-wso2-is-domain>/oauth2/token`).

#### **`UserInfoActivity.java`**
1. Locate the `handleAuthorizationResponse` and `performTokenRequest` methods:
   - Update the **client secret** with the value generated during the WSO2 IS registration.
2. Locate the `callUserInfo` method:
   - Update the **`userInfoEndpoint`** (e.g., `https://<your-wso2-is-domain>/oauth2/userinfo`).
3. Locate the `LogoutListener` class:
   - Update the **`logout_uri`** (e.g., `https://<your-wso2-is-domain>/oidc/logout`).

---

### 3. Build and Run the Application
1. Open the project in **Android Studio**.
2. Update the required fields in the provided classes.
3. Connect an Android device or launch an emulator (API level 24+ recommended).
4. Build and run the application to test the authentication flow.

---

## Key Features
- **Two-Step Authentication Flow**: Ensures security by combining username/password with a second authentication factor (passkey).
- **User Info Retrieval**: Fetches user details from the WSO2 IS user info endpoint.
- **Secure Logout**: Logs users out securely via the WSO2 IS logout endpoint.

---

## Prerequisites
- **Passkey Enrollment**:  
  Enroll your passkey via the My Account portal at:  `https://<your-wso2-is-domain>/myaccount`

---

## Requirements
- **WSO2 Identity Server 7**: Configured with OAuth 2.0 capabilities.
- **Development Tools**: Android Studio and Java Development Kit (JDK).
- **Supported Devices**: Android 7.0+ (API level 24+).
