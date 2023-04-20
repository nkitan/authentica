# authentica
This is a simple and transparently secure two-factor authentication app written in Java for Android. The app uses Time-based One-Time Password (TOTP) algorithm to generate one-time codes for use in two-factor authentication.

## Getting Started
To use this app, you can clone the repository and open it in Android Studio. The app is built with Gradle, so you can easily run it on an Android device or emulator.

```bash
git clone https://github.com/nkitan/authentica.git
cd authentica

# set gradlew as executable if not done already
chmod +x gradlew

# build using gradlew
./gradlew build
```
                               
## Features
This two-factor authentication app provides the following features:

* Add new accounts: you can add new accounts to the app by scanning a QR code or manually entering an account name and secret key.  
* Generate one-time codes: the app generates one-time codes using the TOTP algorithm, which can be used for two-factor authentication with supported services.    
* Copy codes: you can easily copy the one-time codes to the clipboard for use in other apps or websites.  
* Delete accounts: you can delete accounts from the app when they are no longer needed.  

## Usage
To add a new account to the app, you can either scan a QR code or manually enter an account name and secret key. The secret key can be obtained from the service that you want to use two-factor authentication with.  
  
* To generate a one-time code, simply select the account that you want to generate the code for, and the app will display the current one-time code for that account. You can copy the code to the clipboard and use it for two-factor authentication with the service.
  
* To delete an account, simply swipe left on the account that you want to delete and tap the "Delete" button.
  
## Security  
This two-factor authentication app is designed to be secure and protect your accounts. The app uses the TOTP algorithm to generate one-time codes, which is a widely-used and secure algorithm for two-factor authentication.

The app also uses encryption to protect your accounts and their secret keys. The secret keys are stored in the app's encrypted database, which is protected by a master password that you create when you first use the app.  
  
![build workflow status](https://github.com/nkitan/authentica/actions/workflows/gradle.yml/badge.svg)
